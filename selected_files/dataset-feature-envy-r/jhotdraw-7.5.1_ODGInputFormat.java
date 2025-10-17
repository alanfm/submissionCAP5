public class ODGInputFormat implements InputFormat {
    private final ODGElementParser elementParser;
    private final ODGStyleManager styleManager;
    private final ODGFigureFactory figureFactory;

    public ODGInputFormat() {
        this.elementParser = new ODGElementParser();
        this.styleManager = new ODGStyleManager();
        this.figureFactory = new ODGFigureFactory(styleManager);
    }

    @Override
    public void read(Transferable t, Drawing drawing, boolean replace) throws UnsupportedFlavorException, IOException {
        InputStream in = (InputStream) t.getTransferData(new DataFlavor("application/vnd.oasis.opendocument.graphics", "Image SVG"));
        try {
            elementParser.parse(in, drawing, replace);
        } finally {
            in.close();
        }
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getPrimaryType().equals("application") && flavor.getSubType().equals("vnd.oasis.opendocument.graphics");
    }
}

public class ODGElementParser {
    public void parse(InputStream in, Drawing drawing, boolean replace) throws IOException {
        // Implementação simplificada
        IXMLElement document = parseDocument(in);
        for (IXMLElement elem : document.getChildren()) {
            String name = elem.getName();
            if ("line".equals(name)) {
                drawing.add(readLineElement(elem));
            } else if ("polygon".equals(name)) {
                drawing.add(readPolygonElement(elem));
            }
            // Outros tipos de elementos...
        }
    }

    private IXMLElement parseDocument(InputStream in) throws IOException {
        // Parsing do documento XML
        return new NanoXMLDocument(in);
    }

    private ODGFigure readLineElement(IXMLElement elem) throws IOException {
        Point2D.Double p1 = new Point2D.Double(toLength(elem.getAttribute("x1", "0"), 1), toLength(elem.getAttribute("y1", "0"), 1));
        Point2D.Double p2 = new Point2D.Double(toLength(elem.getAttribute("x2", "0"), 1), toLength(elem.getAttribute("y2", "0"), 1));
        Map<AttributeKey, Object> attributes = styleManager.getAttributes(elem.getAttribute("style-name", null), "graphic");
        return new ODGPathFigure(p1, p2, attributes);
    }

    private ODGFigure readPolygonElement(IXMLElement elem) throws IOException {
        String[] coords = toWSOrCommaSeparatedArray(elem.getAttribute("points", null));
        Point2D.Double[] points = new Point2D.Double[coords.length / 2];
        for (int i = 0; i < coords.length; i += 2) {
            points[i / 2] = new Point2D.Double(toNumber(coords[i]), toNumber(coords[i + 1]));
        }
        Map<AttributeKey, Object> attributes = styleManager.getAttributes(elem.getAttribute("style-name", null), "graphic");
        return new ODGPolygonFigure(points, attributes);
    }

    private double toLength(String value, double defaultValue) {
        return value != null ? Double.parseDouble(value) : defaultValue;
    }

    private double toNumber(String value) {
        return Double.parseDouble(value);
    }

    private String[] toWSOrCommaSeparatedArray(String str) {
        return str.split("(\\s*,\\s*|\\s+)");
    }
}

public class ODGStyleManager {
    public Map<AttributeKey, Object> getAttributes(String styleName, String type) {
        // Implementação simplificada
        Map<AttributeKey, Object> attributes = new HashMap<>();
        if (styleName != null) {
            // Carrega atributos com base no nome do estilo
        }
        return attributes;
    }
}

public class ODGFigureFactory {
    private final ODGStyleManager styleManager;

    public ODGFigureFactory(ODGStyleManager styleManager) {
        this.styleManager = styleManager;
    }

    public ODGFigure createLineFigure(Point2D.Double p1, Point2D.Double p2, Map<AttributeKey, Object> attributes) {
        return new ODGPathFigure(p1, p2, attributes);
    }

    public ODGFigure createPolygonFigure(Point2D.Double[] points, Map<AttributeKey, Object> attributes) {
        return new ODGPolygonFigure(points, attributes);
    }
}

public class GeometryTransformer {
    public AffineTransform readViewBoxTransform(IXMLElement elem) {
        // Implementação simplificada
        return new AffineTransform();
    }

    public EnhancedPath toEnhancedPath(String str) throws IOException {
        // Implementação simplificada
        return new EnhancedPath();
    }
}