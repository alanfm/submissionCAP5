import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PushbackInputStream;

public class CMapParser {
    private TokenParser tokenParser;
    private CMapBuilder cmapBuilder;

    public CMapParser() {
        this.tokenParser = new TokenParser();
        this.cmapBuilder = new CMapBuilder();
    }

    public CMap parse(InputStream input) throws IOException {
        PushbackInputStream cmapStream = new PushbackInputStream(input);
        CMap result = cmapBuilder.createCMap();

        Object previousToken = null;
        Object token;

        while ((token = tokenParser.parseNextToken(cmapStream)) != null) {
            if (token instanceof Operator) {
                handleOperator(result, (Operator) token, previousToken);
            } else {
                previousToken = token;
            }
        }

        return result;
    }

    private void handleOperator(CMap result, Operator operator, Object previousToken) {
        switch (operator.getOp()) {
            case BEGIN_CODESPACE_RANGE:
                cmapBuilder.addCodespaceRanges(result, (Number) previousToken, tokenParser, cmapStream);
                break;
            case BEGIN_BASE_FONT_CHAR:
                cmapBuilder.addBaseFontChars(result, (Number) previousToken, tokenParser, cmapStream);
                break;
            case BEGIN_BASE_FONT_RANGE:
                cmapBuilder.addBaseFontRanges(result, (Number) previousToken, tokenParser, cmapStream);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator.getOp());
        }
    }
}

public class TokenParser {
    private byte[] tokenParserByteBuffer = new byte[512];

    public Object parseNextToken(PushbackInputStream cmapStream) throws IOException {
        // Lógica para analisar o próximo token do fluxo de entrada.
        // Retorna tokens como números, strings ou operadores.
        return null;
    }
}


public class CMapBuilder {
    public CMap createCMap() {
        // Lógica para criar um novo objeto CMap.
        return new CMap();
    }

    public void addCodespaceRanges(CMap result, Number count, TokenParser tokenParser, PushbackInputStream cmapStream) {
        for (int i = 0; i < count.intValue(); i++) {
            byte[] startRange = (byte[]) tokenParser.parseNextToken(cmapStream);
            byte[] endRange = (byte[]) tokenParser.parseNextToken(cmapStream);

            CodespaceRange range = new CodespaceRange();
            range.setStart(startRange);
            range.setEnd(endRange);

            result.addCodespaceRange(range);
        }
    }

    public void addBaseFontChars(CMap result, Number count, TokenParser tokenParser, PushbackInputStream cmapStream) {
        for (int i = 0; i < count.intValue(); i++) {
            byte[] inputCode = (byte[]) tokenParser.parseNextToken(cmapStream);
            Object nextToken = tokenParser.parseNextToken(cmapStream);

            if (nextToken instanceof byte[]) {
                String value = createStringFromBytes((byte[]) nextToken);
                result.addMapping(inputCode, value);
            } else if (nextToken instanceof LiteralName) {
                result.addMapping(inputCode, ((LiteralName) nextToken).getName());
            } else {
                throw new IllegalArgumentException("Unexpected token type: " + nextToken);
            }
        }
    }

    public void addBaseFontRanges(CMap result, Number count, TokenParser tokenParser, PushbackInputStream cmapStream) {
        // Lógica para adicionar intervalos de fontes base ao CMap.
    }

    private String createStringFromBytes(byte[] bytes) {
        // Lógica para converter bytes em uma string.
        return new String(bytes);
    }
}

public class Operator {
    private String op;

    public Operator(String theOp) {
        this.op = theOp;
    }

    public String getOp() {
        return op;
    }
}

public class CodespaceRange {
    private byte[] start;
    private byte[] end;

    public void setStart(byte[] start) {
        this.start = start;
    }

    public void setEnd(byte[] end) {
        this.end = end;
    }

    public boolean contains(byte[] code) {
        // Lógica para verificar se o código está dentro do intervalo.
        return false;
    }
}

public class CMap {
    private List<CodespaceRange> codespaceRanges = new ArrayList<>();
    private List<Mapping> mappings = new ArrayList<>();

    public void addCodespaceRange(CodespaceRange range) {
        codespaceRanges.add(range);
    }

    public void addMapping(byte[] inputCode, String value) {
        mappings.add(new Mapping(inputCode, value));
    }

    public String lookup(byte[] code) {
        for (Mapping mapping : mappings) {
            if (mapping.matches(code)) {
                return mapping.getValue();
            }
        }
        return null;
    }
}

public class Mapping {
    private byte[] inputCode;
    private String value;

    public Mapping(byte[] inputCode, String value) {
        this.inputCode = inputCode;
        this.value = value;
    }

    public boolean matches(byte[] code) {
        return java.util.Arrays.equals(inputCode, code);
    }

    public String getValue() {
        return value;
    }
}