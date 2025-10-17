// Classe dedicada à manipulação de formas
class ShapeHandler {
    public static void writeShape(Shape shape, SWFShape swfShape) throws IOException {
        shape.hasAlpha = true;
        shape.writeShape(swfShape);
    }

    public static Rectangle getShapeBounds(Shape shape) {
        return shape.getRect();
    }
}

// Classe dedicada à definição de símbolos
class SymbolDefiner {
    public static int defineMorphShape(Movie movie, SWFTagTypes definitionWriter, Shape shape1, Shape shape2) throws IOException {
        int id = getNextId(movie);
        SWFShape swfShape = definitionWriter.tagDefineMorphShape(id, ShapeHandler.getShapeBounds(shape1), ShapeHandler.getShapeBounds(shape2));

        ShapeHandler.writeShape(shape1, swfShape);
        ShapeHandler.writeShape(shape2, swfShape);

        return id;
    }

    private static int getNextId(Movie movie) {
        // Lógica para obter o próximo ID disponível
        return movie.getNextId();
    }
}

// Versão refatorada da classe principal
public class MorphShape extends Symbol {
    protected Shape shape1;
    protected Shape shape2;

    public MorphShape(Shape shape1, Shape shape2) {
        this.shape1 = shape1;
        this.shape2 = shape2;
    }

    public Shape getShape1() {
        return shape1;
    }

    public Shape getShape2() {
        return shape2;
    }

    public void setShape1(Shape shape1) {
        this.shape1 = shape1;
    }

    public void setShape2(Shape shape2) {
        this.shape2 = shape2;
    }

    @Override
    protected int defineSymbol(Movie movie, SWFTagTypes timelineWriter, SWFTagTypes definitionWriter) throws IOException {
        return SymbolDefiner.defineMorphShape(movie, definitionWriter, shape1, shape2);
    }
}