package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Camera;
import be.kuleuven.cs.robijn.common.PerspectiveCamera;
import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.Font;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.Label3D;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.RenderableString;
import com.github.wouterdek.jrenderdoc.RenderDoc;
import com.google.common.cache.*;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL33.glBindSampler;

public class LabelRenderer implements AutoCloseable {
    private OpenGLRenderer renderer;
    private ShaderProgram fontShader;
    private final LoadingCache<Font, Cache<String, Texture>> labelCache;

    LabelRenderer(OpenGLRenderer renderer){
        this.renderer = renderer;

        labelCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(10)
                .build(new CacheLoader<Font, Cache<String, Texture>>() {
                    @Override
                    public Cache<String, Texture> load(Font font) {
                        return CacheBuilder.newBuilder()
                                .concurrencyLevel(1)
                                .maximumSize(100)
                                .removalListener((RemovalListener<String, Texture>) e -> {
                                    e.getValue().close();
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
                    Cache<String, Texture> cache = labelCache.getUnchecked(label.getFont());
                    boolean valueInCache = cache.getIfPresent(label.getText()) != null;
                    if(!valueInCache){
                        RenderableString renderableString = label.getFont().layoutString(label.getText());
                        Texture bakedString = label.getFont().getRenderer().bake(renderableString);
                        cache.put(label.getText(), bakedString);
                    }
                });
    }

    void renderLabel(Label3D label, Matrix4f viewProjectionMatrix, Camera camera) {
        RenderDoc.get().startFrameCapture(renderer.getHGLRC(), 0);

        Texture textTexture = labelCache.getUnchecked(label.getFont()).getIfPresent(label.getText());

        Model billboard = new Model(Billboard.create(textTexture).getMesh(), textTexture, getFontShader());

        Texture colorMap = Texture.createEmpty(new int[]{256}, 3);
        ByteBuffer data = ByteBuffer.allocateDirect(256*3);
        byte[] color2 = new byte[]{(byte)0xff, (byte)0xff, (byte)0xff};
        byte[] color1 = new byte[]{(byte)0x00, (byte)0x00, (byte)0x00};
        for(int i = 0; i < 100; i++){
            data.put(color1);
        }
        for(int i = 0; i < 156; i++){
            data.put(color2);
        }
        colorMap.setData(data);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_1D, colorMap.getTextureId());

        float scale = 1.0f;
        if(camera instanceof PerspectiveCamera){
            scale = (float)camera.getWorldPosition().subtract(label.getWorldPosition()).getNorm();
            scale /= 7.071068f; //Compensate for default viewing distance
            scale *= .75f; //Compensate for perspective
        }else{
            scale = 3f;
        }
        renderer.renderModel(billboard, viewProjectionMatrix, Billboard.generateModelMatrix(camera, label.getWorldPosition(), scale));

        billboard.getMesh().close();
        colorMap.close();

        RenderDoc.get().endFrameCapture(renderer.getHGLRC(), 0);
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

    @Override
    public void close() {
        if(fontShader != null){
            fontShader.close();
        }
    }
}
