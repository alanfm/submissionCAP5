// Classe dedicada à manipulação de elementos de linha
class LineElementHandler {
    public static ODGFigure readLineElement(IXMLElement elem, ODGStylesReader styles) throws IOException {
        Point2D.Double p1 = new Point2D.Double(
                toLength(elem.getAttribute("x1", SVG_NAMESPACE, "0"), 1),
                toLength(elem.getAttribute("y1", SVG_NAMESPACE, "0"), 1)
        );
        Point2D.Double p2 = new Point2D.Double(
                toLength(elem.getAttribute("x2", SVG_NAMESPACE, "0"), 1),
                toLength(elem.getAttribute("y2", SVG_NAMESPACE, "0"), 1)
        );

        String styleName = elem.getAttribute("style-name", DRAWING_NAMESPACE, null);
        Map<AttributeKey, Object> attributes = styles.getAttributes(styleName, "graphic");

        return createLineFigure(p1, p2, attributes);
    }

    private static ODGFigure createLineFigure(Point2D.Double p1, Point2D.Double p2, Map<AttributeKey, Object> attributes) throws IOException {
        ODGPathFigure figure = new ODGPathFigure();
        figure.setBounds(p1, p2);
        figure.setAttributes(attributes);
        return figure;
    }
}

// Classe dedicada à manipulação de elementos de polígono
class PolygonElementHandler {
    public static ODGFigure readPolygonElement(IXMLElement elem, ODGStylesReader styles) throws IOException {
        AffineTransform viewBoxTransform = readViewBoxTransform(elem);
        String[] coords = toWSOrCommaSeparatedArray(elem.getAttribute("points", DRAWING_NAMESPACE, null));
        Point2D.Double[] points = parsePoints(coords, viewBoxTransform);

        String styleName = elem.getAttribute("style-name", DRAWING_NAMESPACE, null);
        Map<AttributeKey, Object> attributes = styles.getAttributes(styleName, "graphic");

        return createPolygonFigure(points, attributes);
    }

    private static Point2D.Double[] parsePoints(String[] coords, AffineTransform viewBoxTransform) {
        Point2D.Double[] points = new Point2D.Double[coords.length / 2];
        for (int i = 0; i < coords.length; i += 2) {
            Point2D.Double p = new Point2D.Double(toNumber(coords[i]), toNumber(coords[i + 1]));
            points[i / 2] = (Point2D.Double) viewBoxTransform.transform(p, p);
        }
        return points;
    }

    private static ODGFigure createPolygonFigure(Point2D.Double[] points, Map<AttributeKey, Object> attributes) throws IOException {
        ODGFigure figure = new ODGPolygonFigure();
        figure.setPoints(points);
        figure.setAttributes(attributes);
        return figure;
    }
}

// Versão refatorada da classe principal
public class ODGInputFormat implements InputFormat {
    private static final boolean DEBUG = true;

    @Override
    public ODGFigure readElement(IXMLElement elem, ODGStylesReader styles) throws IOException {
        String name = elem.getName();

        if ("line".equals(name)) {
            return LineElementHandler.readLineElement(elem, styles);
        } else if ("polygon".equals(name)) {
            return PolygonElementHandler.readPolygonElement(elem, styles);
        } else if ("path".equals(name)) {
            return PathElementHandler.readPathElement(elem, styles);
        }

        if (DEBUG) {
            System.out.println("ODGInputFormat.readElement(" + elem + ") not implemented.");
        }
        return null;
    }

    private static AffineTransform readViewBoxTransform(IXMLElement elem) {
        // Lógica para ler a transformação do viewBox
        return new AffineTransform();
    }

    private static double toLength(String value, String namespace, String defaultValue) {
        // Conversão de valor para comprimento
        return Double.parseDouble(value != null ? value : defaultValue);
    }

    private static double toNumber(String value) {
        // Conversão de valor para número
        return Double.parseDouble(value);
    }

    private static String[] toWSOrCommaSeparatedArray(String str) throws IOException {
        String[] result = str.split("(\\s*,\\s*|\\s+)");
        return result.length == 1 && result[0].isEmpty() ? new String[0] : result;
    }
}