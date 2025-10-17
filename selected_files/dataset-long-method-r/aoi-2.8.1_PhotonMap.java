import java.util.List;

public class PhotonMap {
    private PhotonStorage photonStorage;
    private RadianceEstimator radianceEstimator;

    public PhotonMap() {
        this.photonStorage = new PhotonStorage();
        this.radianceEstimator = new RadianceEstimator(photonStorage);
    }

    public void storePhoton(Photon photon) {
        photonStorage.store(photon);
    }

    public void buildTree() {
        photonStorage.buildTree();
    }

    public Color estimateRadiance(Point position, int numPhotons) {
        return radianceEstimator.estimate(position, numPhotons);
    }
}

public class PhotonStorage {
    private List<Photon> photonList;
    private KDTree treeRoot;

    public PhotonStorage() {
        this.photonList = new ArrayList<>();
    }

    public void store(Photon photon) {
        photonList.add(photon);
    }

    public void buildTree() {
        // Lógica para construir uma árvore KD ou BVH a partir da lista de fótons.
        treeRoot = KDTreeBuilder.build(photonList);
    }

    public List<Photon> findNearestPhotons(Point position, int numPhotons) {
        if (treeRoot == null) {
            throw new IllegalStateException("Tree not built yet.");
        }
        return treeRoot.findNearest(position, numPhotons);
    }
}

public class KDTreeBuilder {
    public static KDTree build(List<Photon> photons) {
        // Lógica para construir uma árvore KD a partir da lista de fótons.
        return new KDTree(photons);
    }
}

public class KDTree {
    private List<Photon> photons;

    public KDTree(List<Photon> photons) {
        this.photons = photons;
    }

    public List<Photon> findNearest(Point position, int numPhotons) {
        // Lógica para encontrar os fótons mais próximos usando a árvore KD.
        return photons.stream()
                .sorted((p1, p2) -> Double.compare(p1.getPosition().distanceTo(position), p2.getPosition().distanceTo(position)))
                .limit(numPhotons)
                .toList();
    }
}

public class RadianceEstimator {
    private PhotonStorage photonStorage;

    public RadianceEstimator(PhotonStorage photonStorage) {
        this.photonStorage = photonStorage;
    }

    public Color estimate(Point position, int numPhotons) {
        List<Photon> nearestPhotons = photonStorage.findNearestPhotons(position, numPhotons);
        double totalRadiance = 0.0;
        for (Photon photon : nearestPhotons) {
            totalRadiance += photon.getRadiance();
        }
        double averageRadiance = totalRadiance / numPhotons;
        return new Color(averageRadiance, averageRadiance, averageRadiance);
    }
}

public class Photon {
    private Point position;
    private Color radiance;

    public Photon(Point position, Color radiance) {
        this.position = position;
        this.radiance = radiance;
    }

    public Point getPosition() {
        return position;
    }

    public double getRadiance() {
        return radiance.getIntensity();
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setRadiance(Color radiance) {
        this.radiance = radiance;
    }
}

public class Point {
    private double x, y, z;

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double distanceTo(Point other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}

public class Color {
    private double r, g, b;

    public Color(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public double getIntensity() {
        return (r + g + b) / 3.0;
    }
}