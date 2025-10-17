public class XMLSchedulingDataProcessor implements ErrorHandler {
    private final XMLParser xmlParser;
    private final DirectiveProcessor directiveProcessor;
    private final JobScheduler jobScheduler;
    private final ValidationManager validationManager;

    public XMLSchedulingDataProcessor(ClassLoadHelper classLoadHelper) throws ParserConfigurationException {
        this.xmlParser = new XMLParser(classLoadHelper);
        this.directiveProcessor = new DirectiveProcessor();
        this.jobScheduler = new JobScheduler();
        this.validationManager = new ValidationManager();
    }

    public void processFileAndScheduleJobs(String fileName, Scheduler scheduler) throws Exception {
        Document document = xmlParser.parseFile(fileName);
        validationManager.validate(document);

        directiveProcessor.executePreProcessingCommands(scheduler);
        jobScheduler.scheduleJobs(document, scheduler);
    }

    public void processStreamAndScheduleJobs(InputStream stream, String systemId, Scheduler scheduler) throws Exception {
        Document document = xmlParser.parseStream(stream, systemId);
        validationManager.validate(document);

        directiveProcessor.executePreProcessingCommands(scheduler);
        jobScheduler.scheduleJobs(document, scheduler);
    }
}

public class XMLParser {
    private final DocumentBuilder docBuilder;
    private final XPath xpath;

    public XMLParser(ClassLoadHelper classLoadHelper) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        docBuilder = factory.newDocumentBuilder();

        NamespaceContext nsContext = createNamespaceContext();
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);
    }

    public Document parseFile(String fileName) throws SAXException, IOException {
        InputSource is = new InputSource(getInputStream(fileName));
        return docBuilder.parse(is);
    }

    public Document parseStream(InputStream stream, String systemId) throws SAXException, IOException {
        InputSource is = new InputSource(stream);
        is.setSystemId(systemId);
        return docBuilder.parse(is);
    }

    private NamespaceContext createNamespaceContext() {
        return new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if ("q".equals(prefix)) return "http://www.quartz-scheduler.org/xml/JobSchedulingData";
                return XMLConstants.NULL_NS_URI;
            }

            public Iterator getPrefixes(String namespaceURI) {
                throw new UnsupportedOperationException();
            }

            public String getPrefix(String namespaceURI) {
                throw new UnsupportedOperationException();
            }
        };
    }
}

public class DirectiveProcessor {
    public void executePreProcessingCommands(Scheduler scheduler) throws SchedulerException {
        // Exemplo de implementação simplificada
        for (String group : jobGroupsToDelete) {
            scheduler.deleteJobGroup(group);
        }
        for (String group : triggerGroupsToDelete) {
            scheduler.deleteTriggerGroup(group);
        }
    }
}

public class JobScheduler {
    public void scheduleJobs(Document document, Scheduler scheduler) throws SchedulerException {
        NodeList jobNodes = (NodeList) evaluateXPath("/q:job-scheduling-data/q:schedule/q:job", document);
        for (int i = 0; i < jobNodes.getLength(); i++) {
            Node jobNode = jobNodes.item(i);
            JobDetail jobDetail = parseJobDetail(jobNode);
            scheduler.addJob(jobDetail, true);
        }

        NodeList triggerNodes = (NodeList) evaluateXPath("/q:job-scheduling-data/q:schedule/q:trigger/*", document);
        for (int i = 0; i < triggerNodes.getLength(); i++) {
            Node triggerNode = triggerNodes.item(i);
            Trigger trigger = parseTrigger(triggerNode);
            scheduler.scheduleJob(trigger);
        }
    }

    private JobDetail parseJobDetail(Node jobNode) {
        // Implementação simplificada
        return new JobDetail();
    }

    private Trigger parseTrigger(Node triggerNode) {
        // Implementação simplificada
        return new Trigger();
    }

    private Object evaluateXPath(String expression, Document document) {
        try {
            return XPathFactory.newInstance().newXPath().evaluate(expression, document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Erro ao avaliar XPath", e);
        }
    }
}

public class ValidationManager {
    public void validate(Document document) {
        // Implementação simplificada
        if (!isValid(document)) {
            throw new IllegalArgumentException("Documento inválido");
        }
    }

    private boolean isValid(Document document) {
        // Lógica de validação
        return true;
    }
}

