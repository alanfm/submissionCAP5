import java.io.File;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class UML2JagGenerator {
    private ModelInterpreter modelInterpreter;
    private ConfigGenerator configGenerator;
    private EntityBuilder entityBuilder;
    private SessionBuilder sessionBuilder;

    public UML2JagGenerator() {
        this.modelInterpreter = new ModelInterpreter();
        this.configGenerator = new ConfigGenerator();
        this.entityBuilder = new EntityBuilder();
        this.sessionBuilder = new SessionBuilder();
    }

    public void generateXML(String xmiFileName, String outputDir) {
        SimpleModel model = modelInterpreter.loadModel(xmiFileName);
        Root root = generateConfig(model);
        configGenerator.writeConfigToFile(root, outputDir, model.getName());
    }

    private Root generateConfig(SimpleModel model) {
        Root root = new Root();
        configGenerator.createDataSource(model, root.datasource);
        configGenerator.createConfig(model, root.config, root.app, root.paths);
        entityBuilder.createEntities(model, root.entities);
        sessionBuilder.createSessionEJBs(model, root.sessions);
        return root;
    }
}

public class ModelInterpreter {
    public SimpleModel loadModel(String xmiFileName) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new File(xmiFileName));
            return new SimpleModel(doc);
        } catch (Exception e) {
            throw new RuntimeException("Error loading UML model", e);
        }
    }
}

public class ConfigGenerator {
    public void createDataSource(SimpleModel model, DataSource ds) {
        // Lógica para criar fontes de dados a partir do modelo.
    }

    public void createConfig(SimpleModel model, Config config, App app, Paths paths) {
        // Lógica para criar configurações gerais da aplicação.
    }

    public void writeConfigToFile(Root root, String outputDir, String fileName) {
        try (OutputStream outputStream = new FileOutputStream(new File(outputDir, fileName + ".xml"))) {
            String xmlContent = root.toXML();
            outputStream.write(xmlContent.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Error writing config file", e);
        }
    }
}

public class EntityBuilder {
    public void createEntities(SimpleModel model, Map<String, Entity> entities) {
        for (SimpleUmlClass umlClass : model.getClasses()) {
            if ("entity".equals(umlClass.getStereotype())) {
                Entity entity = new Entity();
                entity.setName(umlClass.getName());
                entity.setDescription(umlClass.getTaggedValue("documentation"));
                entities.put(entity.getName(), entity);

                for (SimpleAttribute attribute : umlClass.getAttributes()) {
                    Field field = new Field();
                    field.setName(attribute.getName());
                    field.setType(attribute.getType());
                    entity.addField(field);
                }
            }
        }
    }
}

public class SessionBuilder {
    public void createSessionEJBs(SimpleModel model, Map<String, Session> sessions) {
        for (SimpleUmlClass umlClass : model.getClasses()) {
            if ("session".equals(umlClass.getStereotype())) {
                Session session = new Session();
                session.setName(umlClass.getName());
                session.setDescription(umlClass.getTaggedValue("documentation"));
                sessions.put(session.getName(), session);

                for (SimpleOperation operation : umlClass.getOperations()) {
                    BusinessMethod method = new BusinessMethod();
                    method.setName(operation.getName());
                    method.setDescription(operation.getTaggedValue("documentation"));
                    session.addBusinessMethod(method);
                }
            }
        }
    }
}

public class Root {
    public DataSource datasource = new DataSource();
    public Config config = new Config();
    public App app = new App();
    public Paths paths = new Paths();
    public Map<String, Entity> entities = new HashMap<>();
    public Map<String, Session> sessions = new HashMap<>();

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<skelet>\n");
        xml.append(datasource.toXML()).append("\n");
        xml.append(config.toXML()).append("\n");
        xml.append(app.toXML()).append("\n");
        xml.append(paths.toXML()).append("\n");

        for (Entity entity : entities.values()) {
            xml.append(entity.toXML()).append("\n");
        }

        for (Session session : sessions.values()) {
            xml.append(session.toXML()).append("\n");
        }

        xml.append("</skelet>");
        return xml.toString();
    }
}

public class Entity {
    private String name;
    private String description;
    private List<Field> fields = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addField(Field field) {
        fields.add(field);
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<entity name=\"").append(name).append("\" description=\"").append(description).append("\">\n");
        for (Field field : fields) {
            xml.append(field.toXML()).append("\n");
        }
        xml.append("</entity>");
        return xml.toString();
    }
}

public class Field {
    private String name;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toXML() {
        return "<field name=\"" + name + "\" type=\"" + type + "\"/>";
    }
}