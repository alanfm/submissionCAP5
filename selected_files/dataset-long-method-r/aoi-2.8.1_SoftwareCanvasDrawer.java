import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.geom.Shape;

public class SoftwareCanvasDrawer implements CanvasDrawer {
    private PixelBuffer pixelBuffer;
    private ShapeRenderer shapeRenderer;
    private ImageRenderer imageRenderer;
    private MeshRenderer meshRenderer;

    public SoftwareCanvasDrawer(ViewerCanvas view) {
        this.pixelBuffer = new PixelBuffer(view.getBounds());
        this.shapeRenderer = new ShapeRenderer();
        this.imageRenderer = new ImageRenderer(pixelBuffer);
        this.meshRenderer = new MeshRenderer(pixelBuffer);
    }

    @Override
    public void drawShape(Shape shape, Color color) {
        shapeRenderer.draw(shape, color);
    }

    @Override
    public void fillShape(Shape shape, Color color) {
        shapeRenderer.fill(shape, color);
    }

    @Override
    public void drawString(String text, int x, int y, Color color) {
        shapeRenderer.drawString(text, x, y, color);
    }

    @Override
    public void drawImage(Image image, int x, int y) {
        imageRenderer.drawImage(image, x, y);
    }

    @Override
    public void renderMesh(RenderingMesh mesh, VertexShader shader, Camera cam, boolean closed, boolean[] hideFace) {
        meshRenderer.render(mesh, shader, cam, closed, hideFace);
    }

    public BufferedImage getImage() {
        return pixelBuffer.getImage();
    }
}

public class PixelBuffer {
    private int[] pixels;
    private int width, height;
    private BufferedImage image;

    public PixelBuffer(Rectangle bounds) {
        this.width = bounds.width;
        this.height = bounds.height;
        this.pixels = new int[width * height];
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void setPixel(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y * width + x] = color;
        }
    }

    public BufferedImage getImage() {
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }
}

public class ShapeRenderer {
    public void draw(Shape shape, Color color) {
        Graphics2D g2d = (Graphics2D) Toolkit.getDefaultToolkit().createImage(1, 1).getGraphics();
        g2d.setColor(color);
        g2d.draw(shape);
        g2d.dispose();
    }

    public void fill(Shape shape, Color color) {
        Graphics2D g2d = (Graphics2D) Toolkit.getDefaultToolkit().createImage(1, 1).getGraphics();
        g2d.setColor(color);
        g2d.fill(shape);
        g2d.dispose();
    }

    public void drawString(String text, int x, int y, Color color) {
        Graphics2D g2d = (Graphics2D) Toolkit.getDefaultToolkit().createImage(1, 1).getGraphics();
        g2d.setColor(color);
        g2d.drawString(text, x, y);
        g2d.dispose();
    }
}

public class ImageRenderer {
    private PixelBuffer pixelBuffer;

    public ImageRenderer(PixelBuffer pixelBuffer) {
        this.pixelBuffer = pixelBuffer;
    }

    public void drawImage(Image image, int x, int y) {
        ImageRecord record = getCachedImage(image);
        if (record == null) return;

        int[] imagePixels = record.getPixels();
        int width = record.getWidth();
        int height = record.getHeight();

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int pixel = imagePixels[j * width + i];
                if ((pixel & 0xFF000000) != 0) {
                    pixelBuffer.setPixel(x + i, y + j, pixel);
                }
            }
        }
    }

    private ImageRecord getCachedImage(Image image) {
        // Lógica para obter ou criar um registro de imagem em cache.
        return new ImageRecord(image);
    }
}

public class MeshRenderer {
    private PixelBuffer pixelBuffer;

    public MeshRenderer(PixelBuffer pixelBuffer) {
        this.pixelBuffer = pixelBuffer;
    }

    public void render(RenderingMesh mesh, VertexShader shader, Camera cam, boolean closed, boolean[] hideFace) {
        Vec3[] vertices = mesh.getVertices();
        RenderingTriangle[] triangles = mesh.getTriangles();

        for (RenderingTriangle triangle : triangles) {
            if (hideFace != null && hideFace[triangle.getIndex()]) continue;

            Vec3 v1 = vertices[triangle.getV1()];
            Vec3 v2 = vertices[triangle.getV2()];
            Vec3 v3 = vertices[triangle.getV3()];

            renderTriangle(v1, v2, v3, cam);
        }
    }

    private void renderTriangle(Vec3 v1, Vec3 v2, Vec3 v3, Camera cam) {
        // Lógica para renderizar um triângulo no buffer de pixels.
    }
}

public class ImageRecord {
    private int[] pixels;
    private int width, height;

    public ImageRecord(Image image) {
        this.width = image.getWidth(null);
        this.height = image.getHeight(null);
        this.pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public int[] getPixels() {
        return pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}