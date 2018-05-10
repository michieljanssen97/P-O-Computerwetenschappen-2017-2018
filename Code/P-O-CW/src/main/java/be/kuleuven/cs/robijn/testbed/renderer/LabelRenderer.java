package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Resources;

import be.kuleuven.cs.robijn.testbed.renderer.bmfont.BMFont;
import be.kuleuven.cs.robijn.worldObjects.Label3D;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.RenderableString;
import be.kuleuven.cs.robijn.worldObjects.Camera;
import be.kuleuven.cs.robijn.worldObjects.PerspectiveCamera;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;

import org.joml.Matrix4f;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class LabelRenderer implements AutoCloseable {
    private OpenGLRenderer renderer;
    private ShaderProgram fontShader;
    private Texture colorMapTex;
    private ByteBuffer colorMapBuffer;
    private final HashMap<BMFont, HashMap<String, Model>> labelCache = new HashMap<>();

    LabelRenderer(OpenGLRenderer renderer){
        this.renderer = renderer;
    }

    void updateLabelCache(WorldObject root){
        root.getDescendantsStream()
                .filter(o -> o instanceof Label3D)
                .map(o -> (Label3D)o)
                .forEach(label -> {
                    if(label.getFont() == null || !(label.getFont() instanceof BMFont)){
                        label.setFont(renderer.loadFont(null));
                    }
                    BMFont font = (BMFont)label.getFont();
                    HashMap<String, Model> cache = labelCache.computeIfAbsent(font, f -> new HashMap<>());
                    cache.computeIfAbsent(label.getText(), text -> {
                        RenderableString renderableString = font.layoutString(text);
                        Texture bakedStringTex = font.getRenderer().bake(renderableString);
                        return new Model(Billboard.createBillboardMesh(bakedStringTex), bakedStringTex, getFontShader());
                    });
                });
    }

    void renderLabel(Label3D label, Matrix4f viewProjectionMatrix, Camera camera) {
        //RenderDoc.get().startFrameCapture(renderer.getHGLRC(), 0);

        Model textModel = labelCache.get(label.getFont()).get(label.getText());
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

        float scale;
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
