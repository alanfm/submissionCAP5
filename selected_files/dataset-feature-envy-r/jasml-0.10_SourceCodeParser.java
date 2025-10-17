public class SourceCodeParser implements Scannable {
    private final Scanner scanner;
    private final ConstantPoolGenerator cpl = new ConstantPoolGenerator();
    private final ClassParser classParser;
    private final MethodParser methodParser;
    private final FieldParser fieldParser;

    public SourceCodeParser(File file) throws ParsingException {
        this.scanner = new Scanner(file);
        this.classParser = new ClassParser(scanner, cpl);
        this.methodParser = new MethodParser(scanner, cpl);
        this.fieldParser = new FieldParser(scanner, cpl);
    }

    public SourceCodeParser(String content) throws ParsingException {
        this.scanner = new Scanner(content);
        this.classParser = new ClassParser(scanner, cpl);
        this.methodParser = new MethodParser(scanner, cpl);
        this.fieldParser = new FieldParser(scanner, cpl);
    }

    public JavaClass parse() throws ParsingException {
        JavaClass javaClass = classParser.parseClass();
        javaClass.constantPool = cpl.getConstantPool();
        javaClass.constant_pool_count = javaClass.constantPool.getConstantPoolCount();
        return javaClass;
    }
}

public class ClassParser {
    private final Scanner scanner;
    private final ConstantPoolGenerator cpl;

    public ClassParser(Scanner scanner, ConstantPoolGenerator cpl) {
        this.scanner = scanner;
        this.cpl = cpl;
    }

    public JavaClass parseClass() throws ParsingException {
        JavaClass javaClass = new JavaClass();
        parseClassSignature(javaClass);
        javaClass.fields = fieldParser.parseFields();
        javaClass.methods = methodParser.parseMethods();
        javaClass.attributes = attributeParser.parseAttributes();
        return javaClass;
    }

    private void parseClassSignature(JavaClass javaClass) throws ParsingException {
        // Implementação simplificada
        javaClass.access_flags = parseAccessFlags();
        javaClass.this_class = cpl.addUtf8(scanner.token());
        javaClass.super_class = cpl.addUtf8(scanner.nextToken());
    }

    private short parseAccessFlags() {
        // Implementação simplificada
        return 0; // Exemplo de flags de acesso
    }
}

public class MethodParser {
    private final Scanner scanner;
    private final ConstantPoolGenerator cpl;

    public MethodParser(Scanner scanner, ConstantPoolGenerator cpl) {
        this.scanner = scanner;
        this.cpl = cpl;
    }

    public Method[] parseMethods() throws ParsingException {
        ArrayList<Method> methods = new ArrayList<>();
        while (scanner.tokenType() == TokenType.Method) {
            methods.add(parseMethod());
        }
        return methods.toArray(new Method[0]);
    }

    private Method parseMethod() throws ParsingException {
        Method method = new Method();
        method.access_flags = parseAccessFlags();
        method.name_index = cpl.addUtf8(scanner.token());
        method.descriptor_index = cpl.addUtf8(scanner.nextToken());
        method.attributes = parseAttributes();
        return method;
    }

    private Attribute[] parseAttributes() throws ParsingException {
        ArrayList<Attribute> attributes = new ArrayList<>();
        while (scanner.tokenType() == TokenType.Attribute) {
            attributes.add(attributeParser.parseAttribute());
        }
        return attributes.toArray(new Attribute[0]);
    }
}

public class FieldParser {
    private final Scanner scanner;
    private final ConstantPoolGenerator cpl;

    public FieldParser(Scanner scanner, ConstantPoolGenerator cpl) {
        this.scanner = scanner;
        this.cpl = cpl;
    }

    public Field[] parseFields() throws ParsingException {
        ArrayList<Field> fields = new ArrayList<>();
        while (scanner.tokenType() == TokenType.Field) {
            fields.add(parseField());
        }
        return fields.toArray(new Field[0]);
    }

    private Field parseField() throws ParsingException {
        Field field = new Field();
        field.access_flags = parseAccessFlags();
        field.name_index = cpl.addUtf8(scanner.token());
        field.descriptor_index = cpl.addUtf8(scanner.nextToken());
        field.attributes = parseAttributes();
        return field;
    }
}

public class AttributeParser {
    public Attribute parseAttribute() throws ParsingException {
        String attributeName = scanner.token();
        if (attributeName.equals("Code")) {
            return parseCodeAttribute();
        } else if (attributeName.equals("Deprecated")) {
            return new Attribute_Deprecated();
        } else {
            throw new ParsingException("Unsupported attribute: " + attributeName);
        }
    }

    private Attribute_Code parseCodeAttribute() throws ParsingException {
        Attribute_Code code = new Attribute_Code();
        code.max_stack = parseInteger(scanner.nextToken());
        code.max_locals = parseInteger(scanner.nextToken());
        code.code_length = parseInteger(scanner.nextToken());
        code.codes = parseBytecode();
        return code;
    }

    private byte[] parseBytecode() {
        // Implementação simplificada
        return new byte[0];
    }
}