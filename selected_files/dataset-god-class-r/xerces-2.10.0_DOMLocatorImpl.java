// Classe dedicada à validação de campos
class DOMLocatorValidator {
    public static void validateLineNumber(int lineNumber) {
        if (lineNumber < -1) {
            throw new IllegalArgumentException("O número da linha deve ser maior ou igual a -1.");
        }
    }

    public static void validateColumnNumber(int columnNumber) {
        if (columnNumber < -1) {
            throw new IllegalArgumentException("O número da coluna deve ser maior ou igual a -1.");
        }
    }

    public static void validateOffset(int offset) {
        if (offset < -1) {
            throw new IllegalArgumentException("O offset deve ser maior ou igual a -1.");
        }
    }

    public static void validateURI(String uri) {
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("A URI não pode ser nula ou vazia.");
        }
    }
}

// Versão refatorada da classe principal
public class DOMLocatorImpl implements DOMLocator {
    private int lineNumber;
    private int columnNumber;
    private Node relatedNode;
    private String uri;
    private int byteOffset;
    private int utf16Offset;

    // Construtor principal
    public DOMLocatorImpl(int lineNumber, int columnNumber, int utf16Offset, String uri, int byteOffset, Node relatedNode) {
        setLineNumber(lineNumber);
        setColumnNumber(columnNumber);
        setUtf16Offset(utf16Offset);
        setUri(uri);
        setByteOffset(byteOffset);
        setRelatedNode(relatedNode);
    }

    // Getters e Setters com validação
    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        DOMLocatorValidator.validateLineNumber(lineNumber);
        this.lineNumber = lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        DOMLocatorValidator.validateColumnNumber(columnNumber);
        this.columnNumber = columnNumber;
    }

    @Override
    public Node getRelatedNode() {
        return relatedNode;
    }

    public void setRelatedNode(Node relatedNode) {
        this.relatedNode = relatedNode;
    }

    @Override
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        DOMLocatorValidator.validateURI(uri);
        this.uri = uri;
    }

    @Override
    public int getByteOffset() {
        return byteOffset;
    }

    public void setByteOffset(int byteOffset) {
        DOMLocatorValidator.validateOffset(byteOffset);
        this.byteOffset = byteOffset;
    }

    @Override
    public int getUtf16Offset() {
        return utf16Offset;
    }

    public void setUtf16Offset(int utf16Offset) {
        DOMLocatorValidator.validateOffset(utf16Offset);
        this.utf16Offset = utf16Offset;
    }

    @Override
    public String toString() {
        return "DOMLocatorImpl{" +
                "lineNumber=" + lineNumber +
                ", columnNumber=" + columnNumber +
                ", relatedNode=" + relatedNode +
                ", uri='" + uri + '\'' +
                ", byteOffset=" + byteOffset +
                ", utf16Offset=" + utf16Offset +
                '}';
    }
}