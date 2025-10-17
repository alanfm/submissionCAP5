// Classe dedicada à manipulação de vértices
class MeshVertexHandler {
    public static void scaleVertices(MeshVertex[] vertices, double xscale, double yscale, double zscale) {
        for (MeshVertex vertex : vertices) {
            vertex.r.x *= xscale;
            vertex.r.y *= yscale;
            vertex.r.z *= zscale;
        }
    }

    public static Vec3 calculateNormal(Vec3 v1, Vec3 v2, Vec3 v3) {
        Vec3 vec1 = v2.minus(v1);
        Vec3 vec2 = v3.minus(v1);
        Vec3 normal = vec1.cross(vec2);
        normal.normalize();
        return normal;
    }
}

// Classe dedicada à subdivisão de malhas
class MeshSubdivider {
    public static SplineMesh subdivide(SplineMesh mesh, double tolerance) {
        // Lógica para subdividir a malha
        System.out.println("Subdividing mesh with tolerance: " + tolerance);
        return new SplineMesh(); // Placeholder
    }
}

// Classe dedicada à renderização de malhas
class MeshRenderer {
    public static RenderingMesh getRenderingMesh(SplineMesh mesh, double tolerance, boolean interactive) {
        // Lógica para gerar a malha de renderização
        System.out.println("Generating rendering mesh with tolerance: " + tolerance);
        return new RenderingMesh(); // Placeholder
    }
}

// Versão refatorada da classe principal
public class SplineMesh extends Object3D implements Mesh {
    private MeshVertex[] vertex;
    private float[] usmoothness;
    private float[] vsmoothness;
    private int usize;
    private int vsize;
    private Skeleton skeleton;
    private BoundingBox bounds;

    public SplineMesh(Vec3[][] vertices, float[] usmoothness, float[] vsmoothness, int smoothingMethod, boolean uclosed, boolean vclosed) {
        this.usmoothness = usmoothness;
        this.vsmoothness = vsmoothness;
        this.usize = vertices.length;
        this.vsize = vertices[0].length;
        this.vertex = new MeshVertex[usize * vsize];

        for (int i = 0; i < usize; i++) {
            for (int j = 0; j < vsize; j++) {
                vertex[i + usize * j] = new MeshVertex(vertices[i][j]);
            }
        }

        this.skeleton = new Skeleton();
    }

    public void scale(double xscale, double yscale, double zscale) {
        MeshVertexHandler.scaleVertices(vertex, xscale, yscale, zscale);
        bounds = null; // Invalidate bounds after scaling
    }

    public RenderingMesh getRenderingMesh(double tolerance, boolean interactive) {
        return MeshRenderer.getRenderingMesh(this, tolerance, interactive);
    }

    public SplineMesh subdivide(double tolerance) {
        return MeshSubdivider.subdivide(this, tolerance);
    }

    public TriangleMesh convertToTriangleMesh(double tolerance) {
        // Lógica para converter para TriangleMesh
        System.out.println("Converting to TriangleMesh with tolerance: " + tolerance);
        return new TriangleMesh(); // Placeholder
    }

    public void setSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public BoundingBox findBounds() {
        if (bounds == null) {
            double minx = Double.MAX_VALUE, maxx = Double.MIN_VALUE;
            double miny = Double.MAX_VALUE, maxy = Double.MIN_VALUE;
            double minz = Double.MAX_VALUE, maxz = Double.MIN_VALUE;

            for (MeshVertex v : vertex) {
                if (v.r.x < minx) minx = v.r.x;
                if (v.r.x > maxx) maxx = v.r.x;
                if (v.r.y < miny) miny = v.r.y;
                if (v.r.y > maxy) maxy = v.r.y;
                if (v.r.z < minz) minz = v.r.z;
                if (v.r.z > maxz) maxz = v.r.z;
            }

            bounds = new BoundingBox(minx, maxx, miny, maxy, minz, maxz);
        }
        return bounds;
    }
}