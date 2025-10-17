// Classe dedicada à validação de propriedades
class PropertyValidator {
    public static boolean isValidTaskID(int taskID) {
        return taskID > 0;
    }

    public static boolean isValidPropertyID(String propertyID) {
        return propertyID != null && !propertyID.isEmpty();
    }
}

// Classe dedicada ao gerenciamento de propriedades
class PropertyManager {
    private final List<CustomPropertiesStructure> properties;

    public PropertyManager() {
        this.properties = new ArrayList<>();
    }

    public void addProperty(CustomPropertiesStructure property) {
        if (!PropertyValidator.isValidTaskID(property.getTaskID())) {
            throw new IllegalArgumentException("ID de tarefa inválido.");
        }
        if (!PropertyValidator.isValidPropertyID(property.getTaskPropertyID())) {
            throw new IllegalArgumentException("ID de propriedade inválido.");
        }
        properties.add(property);
    }

    public List<CustomPropertiesStructure> getProperties() {
        return Collections.unmodifiableList(properties);
    }
}

// Versão refatorada da classe principal
public class CustomPropertiesTagHandler implements TagHandler, ParsingListener {
    private final TaskManager taskManager;
    private final ParsingContext parsingContext;
    private final PropertyManager propertyManager;
    private final CustomColumnsStorage columnStorage;

    public CustomPropertiesTagHandler(ParsingContext context, TaskManager taskManager, CustomColumnsStorage columnStorage) {
        this.parsingContext = context;
        this.taskManager = taskManager;
        this.propertyManager = new PropertyManager();
        this.columnStorage = columnStorage;
    }

    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) {
        if (qName.equals("customproperty")) {
            loadProperty(attrs);
        }
    }

    @Override
    public void endElement(String namespaceURI, String sName, String qName) {
        // Nada a fazer aqui.
    }

    @Override
    public void parsingStarted() {
        // Nada a fazer aqui.
    }

    @Override
    public void parsingFinished() {
        for (CustomPropertiesStructure cps : propertyManager.getProperties()) {
            try {
                Task task = taskManager.getTaskById(cps.getTaskID());
                CustomColumn cc = columnStorage.getColumnById(cps.getTaskPropertyID());
                Object value = parseValue(cc.getType(), cps.getValue());
                task.getCustomValues().setValue(cc.getName(), value);
            } catch (InvalidDateException | CustomColumnsException e) {
                if (!GPLogger.log(e)) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    private void loadProperty(Attributes attrs) {
        if (attrs == null) {
            return;
        }

        CustomPropertiesStructure cps = new CustomPropertiesStructure();
        cps.setTaskID(parsingContext.getTaskID());
        cps.setTaskPropertyID(attrs.getValue("taskproperty-id"));
        cps.setValue(attrs.getValue("value"));

        propertyManager.addProperty(cps);
    }

    private Object parseValue(String type, String value) throws InvalidDateException {
        if ("date".equals(type)) {
            return DateParser.parse(value);
        }
        return value;
    }

    // Classe interna refatorada para incluir comportamentos
    private static class CustomPropertiesStructure {
        private int taskID;
        private String taskPropertyID;
        private String value;

        public int getTaskID() {
            return taskID;
        }

        public void setTaskID(int taskID) {
            this.taskID = taskID;
        }

        public String getTaskPropertyID() {
            return taskPropertyID;
        }

        public void setTaskPropertyID(String taskPropertyID) {
            this.taskPropertyID = taskPropertyID;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}