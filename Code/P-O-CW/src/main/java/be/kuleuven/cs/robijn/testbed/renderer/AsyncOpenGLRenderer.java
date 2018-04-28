package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.worldObjects.Camera;
import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;
import be.kuleuven.cs.robijn.worldObjects.PerspectiveCamera;

import java.util.concurrent.*;

public class AsyncOpenGLRenderer implements Renderer {
    public static AsyncOpenGLRenderer create() {
        AsyncOpenGLRenderer renderer = new AsyncOpenGLRenderer();
        renderer.initialize();
        return renderer;
    }

    private final ExecutorService executor;
    private OpenGLRenderer renderer;

    private AsyncOpenGLRenderer(){
        executor = Executors.newSingleThreadExecutor((runnable) -> {
            Thread thread = new Thread(runnable);
            thread.setName("OpenGL Context Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    private void initialize(){
        try {
            executor.submit(() -> renderer = OpenGLRenderer.create()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FrameBuffer createFrameBuffer(int width, int height) {
        try {
            FrameBuffer frameBuffer = executor.submit(() -> renderer.createFrameBuffer(width, height)).get();
            return new AsyncFrameBuffer(frameBuffer);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PerspectiveCamera createPerspectiveCamera() {
        return renderer.createPerspectiveCamera();
    }

    @Override
    public OrthographicCamera createOrthographicCamera() {
        return renderer.createOrthographicCamera();
    }

    @Override
    public Font loadFont(String fontName) {
        try {
            return executor.submit(() -> renderer.loadFont(fontName)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RenderTask startRender(WorldObject worldRoot, FrameBuffer asyncFrameBuffer, Camera camera, Semaphore worldStateLock) {
        if(!(asyncFrameBuffer instanceof AsyncFrameBuffer)){
            throw new IllegalArgumentException("Wrong framebuffer type");
        }
        FrameBuffer frameBuffer = ((AsyncFrameBuffer)asyncFrameBuffer).getFrameBuffer();

        //Start render
        Future<RenderTask> asyncRenderTask = executor.submit(() -> renderer.startRender(worldRoot, frameBuffer, camera, worldStateLock));

        //Monitor render completion
        final Semaphore lock = new Semaphore(0);
        executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if(asyncRenderTask.get().isDone()){
                    lock.release();
                }else{
                    executor.submit(this);
                }
                return null;
            }
        });

        return new RenderTask() {
            private boolean isDone;

            @Override
            public boolean isDone() {
                if(!isDone){
                    isDone = lock.availablePermits() > 0;
                }
                return isDone;
            }

            @Override
            public void waitUntilFinished() {
                if(isDone()){
                    return;
                }

                try {
                    lock.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                isDone = true;
            }
        };
    }

    @Override
    public void clearDebugObjects() {
        executor.submit(renderer::clearDebugObjects);
    }

    @Override
    public void close() {
        try {
            executor.submit(renderer::close).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private class AsyncFrameBuffer implements FrameBuffer {
        private final FrameBuffer frameBuffer;

        public AsyncFrameBuffer(FrameBuffer frameBuffer){
            this.frameBuffer = frameBuffer;
        }

        public FrameBuffer getFrameBuffer(){
            return frameBuffer;
        }

        @Override
        public int getWidth() {
            return frameBuffer.getWidth();
        }

        @Override
        public int getHeight() {
            return frameBuffer.getHeight();
        }

        @Override
        public Future<byte[]> readPixels(byte[] data) {
            return executor.submit(() -> frameBuffer.readPixels(data).get());
        }

        @Override
        public void close() {
            executor.submit(frameBuffer::close);
        }
    }
}
