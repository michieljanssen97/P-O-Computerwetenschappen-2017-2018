package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.RenderTask;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glFlush;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL32.GL_ALREADY_SIGNALED;
import static org.lwjgl.opengl.GL32.GL_CONDITION_SATISFIED;

public class OpenGLRenderTask implements RenderTask {
    private final long syncObject;
    private boolean isDone;

    public OpenGLRenderTask(long syncObject){
        this.syncObject = syncObject;
    }

    public boolean isDone(){
        if(isDone){
            return true;
        }

        int[] length = new int[1];
        int[] signaled = new int[1];
        glGetSynciv(syncObject, GL_SYNC_STATUS, length, signaled);
        if(glGetError() != GL_NO_ERROR){
            throw new RuntimeException("An OpenGL error occurred.");
        }
        isDone = signaled[0] == GL_SIGNALED;
        if(isDone){
            glDeleteSync(syncObject);
        }
        return isDone;
    }

    public void waitUntilFinished(){
        if(isDone){
            return;
        }

        int waitResult;
        do {
            waitResult = glClientWaitSync(syncObject, GL_SYNC_FLUSH_COMMANDS_BIT, 2000);
        } while(waitResult == GL_TIMEOUT_EXPIRED);
        glDeleteSync(syncObject);

        if(waitResult != GL_CONDITION_SATISFIED && waitResult != GL_ALREADY_SIGNALED){
            throw new RuntimeException("waiting for gpu failed: "+waitResult);
        }

        isDone = true;
    }
}
