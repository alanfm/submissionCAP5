// Classe dedicada à validação de atributos
class AttributeValidator {
    public static void validateAttribute(Attr attr, XSAttributeDecl attrDecl) throws XMLSchemaException {
        if (attrDecl == null) {
            throw new XMLSchemaException("Atributo inválido: " + attr.getName());
        }
        XSSimpleType dataType = (XSSimpleType) attrDecl.getTypeDefinition();
        if (dataType == null) {
            throw new XMLSchemaException("Tipo de dado inválido para o atributo: " + attr.getName());
        }
    }

    public static boolean isAttributeAllowed(String attrName, Container attrList) {
        return attrList.containsKey(attrName);
    }
}

// Classe dedicada à manipulação de namespaces
class NamespaceResolver {
    public static void resolveNamespace(Element element, Attr[] attrs, NamespaceSupport nsSupport) {
        for (Attr attr : attrs) {
            String prefix = DOMUtil.getLocalName(attr);
            String uri = DOMUtil.getValue(attr);
            if (prefix != null && !uri.isEmpty()) {
                nsSupport.declarePrefix(prefix, uri);
            }
        }
    }
}

// Classe dedicada ao gerenciamento de erros
class ErrorReporter {
    public static void reportSchemaError(String errorCode, Object[] errorArgs, Element element) {
        System.err.println("Erro de schema: " + errorCode + ", Argumentos: " + Arrays.toString(errorArgs));
        // Lógica para registrar o erro no sistema de logs
    }
}

// Versão refatorada da classe principal
public class XSAttributeChecker {
    private final XSDHandler schemaHandler;
    private SymbolTable symbolTable;
    private final Hashtable<String, Vector<String>> nonSchemaAttrs = new Hashtable<>();
    private final Vector<String> namespaceList = new Vector<>();
    private final boolean[] seen = new boolean[ATTIDX_COUNT];

    public XSAttributeChecker(XSDHandler schemaHandler) {
        this.schemaHandler = schemaHandler;
    }

    public void reset(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        nonSchemaAttrs.clear();
    }

    public Object[] checkAttributes(Element element, boolean isGlobal, XSDocumentInfo schemaDoc) {
        return checkAttributes(element, isGlobal, schemaDoc, false);
    }

    public Object[] checkAttributes(Element element, boolean isGlobal, XSDocumentInfo schemaDoc, boolean enumAsQName) {
        if (element == null) {
            return null;
        }

        Attr[] attrs = DOMUtil.getAttrs(element);
        NamespaceResolver.resolveNamespace(element, attrs, schemaDoc.fNamespaceSupport);

        String uri = DOMUtil.getNamespaceURI(element);
        SchemaGrammar grammar = schemaDoc.fGrammarBucket.getGrammar(uri);
        if (grammar == null) {
            return null;
        }

        Object[] attrValues = getAvailableArray();
        processAttributes(element, attrs, grammar, attrValues);
        return attrValues;
    }

    private void processAttributes(Element element, Attr[] attrs, SchemaGrammar grammar, Object[] attrValues) {
        Container attrList = getAttributeList(element);
        for (Attr attr : attrs) {
            String attrName = attr.getName();
            if (!AttributeValidator.isAttributeAllowed(attrName, attrList)) {
                ErrorReporter.reportSchemaError("s4s-att-not-allowed", new Object[]{element.getNodeName(), attrName}, element);
                continue;
            }

            XSAttributeDecl attrDecl = grammar.getGlobalAttributeDecl(attrName);
            try {
                AttributeValidator.validateAttribute(attr, attrDecl);
                attrValues[attrDecl.getValueIndex()] = attr.getValue();
            } catch (XMLSchemaException e) {
                ErrorReporter.reportSchemaError("cos-nonambig", new Object[]{attrName}, element);
            }
        }
    }

    private Container getAttributeList(Element element) {
        // Lógica para obter a lista de atributos permitidos para o elemento
        return new Container();
    }

    private Object[] getAvailableArray() {
        // Lógica para obter um array disponível para armazenar valores de atributos
        return new Object[ATTIDX_COUNT];
    }

    public void checkNonSchemaAttributes(XSGrammarBucket grammarBucket) {
        Iterator<Map.Entry<String, Vector<String>>> entries = nonSchemaAttrs.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Vector<String>> entry = entries.next();
            String attrRName = entry.getKey();
            Vector<String> values = entry.getValue();

            String attrURI = attrRName.substring(0, attrRName.indexOf(','));
            String attrLocal = attrRName.substring(attrRName.indexOf(',') + 1);

            SchemaGrammar grammar = grammarBucket.getGrammar(attrURI);
            if (grammar == null) {
                continue;
            }

            XSAttributeDecl attrDecl = grammar.getGlobalAttributeDecl(attrLocal);
            if (attrDecl == null) {
                continue;
            }

            for (String value : values) {
                try {
                    AttributeValidator.validateAttribute(null, attrDecl);
                } catch (XMLSchemaException e) {
                    ErrorReporter.reportSchemaError("cos-nonambig", new Object[]{attrLocal}, null);
                }
            }
        }
    }
}