package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Camera;
import be.kuleuven.cs.robijn.common.PerspectiveCamera;
import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.Font;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.Label3D;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.RenderableString;
import com.google.common.cache.*;
import org.joml.Matrix4f;

import java.awt.*;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class LabelRenderer implements AutoCloseable {
    private OpenGLRenderer renderer;
    private ShaderProgram fontShader;
    private Texture colorMapTex;
    private ByteBuffer colorMapBuffer;
    private final LoadingCache<Font, Cache<String, Model>> labelCache;

    LabelRenderer(OpenGLRenderer renderer){
        this.renderer = renderer;

        labelCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(10)
                .build(new CacheLoader<Font, Cache<String, Model>>() {
                    @Override
                    public Cache<String, Model> load(Font font) {
                        return CacheBuilder.newBuilder()
                                .concurrencyLevel(1)
                                .maximumSize(100)
                                .removalListener((RemovalListener<String, Model>) e -> {
                                    e.getValue().getMesh().close();
                                    e.getValue().getTexture().close();
                                })
                                .build();
                    }
                });
    }

    void updateLabelCache(WorldObject root){
        root.getDescendantsStream()
                .filter(o -> o instanceof Label3D)
                .map(o -> (Label3D)o)
                .forEach(label -> {
                    Cache<String, Model> cache = labelCache.getUnchecked(label.getFont());
                    boolean valueInCache = cache.getIfPresent(label.getText()) != null;
                    if(!valueInCache){
                        RenderableString renderableString = label.getFont().layoutString(label.getText());
                        Texture bakedStringTex = label.getFont().getRenderer().bake(renderableString);
                        Model billboardModel = new Model(Billboard.createBillboardMesh(bakedStringTex), bakedStringTex, getFontShader());
                        cache.put(label.getText(), billboardModel);
                    }
                });
    }

    void renderLabel(Label3D label, Matrix4f viewProjectionMatrix, Camera camera) {
        //RenderDoc.get().startFrameCapture(renderer.getHGLRC(), 0);

        Model textModel = labelCache.getUnchecked(label.getFont()).getIfPresent(label.getText());
        Texture colorMap = getColorMap();
        ByteBuffer data = getColorMapBuffer();

        data.rewind();

        for (int i = 0; i < Label3D.MAX_COLORS; i++){
            Color c = label.getColor(i);
            data.put((byte)c.getRed());
            data.put((byte)c.getGreen());
            data.put((byte)c.getBlue());
        }

        colorMap.setData(data);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_1D, colorMap.getTextureId());

        float scale = 1.0f;
        if(camera instanceof PerspectiveCamera){
            scale = (float)Math.pow(camera.getWorldPosition().subtract(label.getWorldPosition()).getNorm(), 0.65f);
            //scale /= 7.071068f; //Compensate for default viewing distance
            scale /= 4.5f; //Compensate for default viewing distance
            //scale *= .75f; //Compensate for perspective
        }else{
            scale = 3f;
        }
        renderer.renderModel(textModel, viewProjectionMatrix, Billboard.generateModelMatrix(camera, label.getWorldPosition(), scale));

        //RenderDoc.get().endFrameCapture(renderer.getHGLRC(), 0);
    }

    private ShaderProgram getFontShader(){
        if(fontShader == null){
            try(Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/text/vertex.glsl"))){
                try(Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/text/fragment.glsl"))){
                    fontShader = ShaderProgram.link(vertexShader, fragmentShader);
                }
            }
        }
        return fontShader;
    }

    private Texture getColorMap(){
        if(colorMapTex == null){
            colorMapTex = Texture.createEmpty(new int[]{256}, 3);
        }
        return colorMapTex;
    }

    private ByteBuffer getColorMapBuffer(){
        if(colorMapBuffer == null){
            colorMapBuffer = ByteBuffer.allocateDirect(256*3); //256 pixels of RGB
        }
        return colorMapBuffer;
    }

    @Override
    public void close() {
        if(fontShader != null){
            fontShader.close();
        }
        if(colorMapTex != null){
            colorMapTex.close();
        }
    }
}
