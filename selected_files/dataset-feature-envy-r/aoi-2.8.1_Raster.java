public class Raster implements Renderer, Runnable {
    private final Renderer renderer;
    private final LightingCalculator lightingCalculator;
    private final TriangleRasterizer triangleRasterizer;
    private final ThreadManagerWrapper threadManagerWrapper;
    private final ImageProcessor imageProcessor;

    public Raster() {
        this.renderer = new Renderer();
        this.lightingCalculator = new LightingCalculator();
        this.triangleRasterizer = new TriangleRasterizer();
        this.threadManagerWrapper = new ThreadManagerWrapper();
        this.imageProcessor = new ImageProcessor();
    }

    @Override
    public void renderScene(Scene theScene, Camera camera, RenderListener rl, SceneCamera sceneCamera) {
        renderer.initializeRender(camera, sceneCamera);
        threadManagerWrapper.runThreads(() -> renderer.renderObjects(theScene.getObjects()));
        imageProcessor.createFinalImage(renderer.getFragments());
    }

    @Override
    public void run() {
        renderer.updateRenderProgress();
    }
}

public class Renderer {
    private Camera camera;
    private Fragment[] fragments;

    public void initializeRender(Camera camera, SceneCamera sceneCamera) {
        this.camera = camera;
        this.fragments = new Fragment[camera.getWidth() * camera.getHeight()];
        Arrays.fill(fragments, Fragment.BACKGROUND_FRAGMENT);
    }

    public void renderObjects(ObjectInfo[] objects) {
        for (ObjectInfo obj : objects) {
            renderObject(obj);
        }
    }

    private void renderObject(ObjectInfo obj) {
        // Implementação simplificada
    }

    public Fragment[] getFragments() {
        return fragments;
    }

    public void updateRenderProgress() {
        // Atualiza o progresso da renderização
    }
}

public class LightingCalculator {
    public RGBColor calculateDiffuse(Vec3 normal, Vec3 lightDir, RGBColor materialColor) {
        float intensity = Math.max(0, normal.dot(lightDir));
        return materialColor.scale(intensity);
    }

    public RGBColor calculateSpecular(Vec3 normal, Vec3 lightDir, Vec3 viewDir, RGBColor materialColor, float shininess) {
        Vec3 reflectDir = lightDir.reflect(normal);
        float specularIntensity = (float) Math.pow(Math.max(0, reflectDir.dot(viewDir)), shininess);
        return materialColor.scale(specularIntensity);
    }
}

public class TriangleRasterizer {
    public void rasterizeTriangle(Vec3 v1, Vec3 v2, Vec3 v3, RGBColor color, Fragment[] fragments) {
        // Implementação simplificada
    }
}

public class ThreadManagerWrapper {
    public void runThreads(Runnable task) {
        ThreadManager threads = new ThreadManager(10, task);
        threads.run();
        threads.finish();
    }
}

public class ImageProcessor {
    public BufferedImage createFinalImage(Fragment[] fragments) {
        int width = (int) Math.sqrt(fragments.length);
        int height = width;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Fragment fragment = fragments[y * width + x];
                image.setRGB(x, y, fragment.getColor().getRGB());
            }
        }

        return image;
    }
}