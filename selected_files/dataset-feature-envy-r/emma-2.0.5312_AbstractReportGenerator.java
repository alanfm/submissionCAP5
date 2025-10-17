public abstract class AbstractReportGenerator implements IReportGenerator {
    protected final ConfigurationManager configurationManager;
    protected final ItemSorter itemSorter;
    protected final MetricCalculator metricCalculator;
    protected final Validator validator;
    protected final LoggerWrapper loggerWrapper;

    public AbstractReportGenerator() {
        this.configurationManager = new ConfigurationManager();
        this.itemSorter = new ItemSorter();
        this.metricCalculator = new MetricCalculator();
        this.validator = new Validator();
        this.loggerWrapper = new LoggerWrapper();
    }

    @Override
    public void initialize(IMetaData metaData, ICoverageData coverageData, SourcePathCache cache, IProperties properties) throws EMMARuntimeException {
        configurationManager.initialize(properties, getType());
        validator.validateDebugInfo(metaData, configurationManager.getSettings());
        metricCalculator.computeMetrics(configurationManager.getSettings(), metaData);
    }

    @Override
    public void generateReport(OutputStream out) throws IOException {
        // Implementação específica para subclasses
    }
}

public class ConfigurationManager {
    private ReportProperties.ParsedProperties settings;

    public void initialize(IProperties properties, String type) throws EMMARuntimeException {
        settings = ReportProperties.parseProperties(properties, type);
    }

    public ReportProperties.ParsedProperties getSettings() {
        return settings;
    }
}

public class ItemSorter {
    public List<Item> sortItems(List<Item> items, ItemComparator[] comparators) {
        List<Item> sortedItems = new ArrayList<>(items);
        sortedItems.sort(new CompositeComparator(comparators));
        return sortedItems;
    }

    private static class CompositeComparator implements Comparator<Item> {
        private final ItemComparator[] comparators;

        public CompositeComparator(ItemComparator[] comparators) {
            this.comparators = comparators;
        }

        @Override
        public int compare(Item o1, Item o2) {
            for (ItemComparator comparator : comparators) {
                int result = comparator.compare(o1, o2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    }
}

public class MetricCalculator {
    public void computeMetrics(ReportProperties.ParsedProperties settings, IMetaData metaData) {
        int[] metrics = settings.getMetrics();
        if (metrics == null || metrics.length == 0) {
            throw new IllegalArgumentException("No metrics defined in settings.");
        }

        for (int metric : metrics) {
            if (metric == -1) {
                continue; // Pass/fail check not required
            }
            // Compute specific metric
        }
    }
}

public class Validator {
    public void validateDebugInfo(IMetaData metaData, ReportProperties.ParsedProperties settings) {
        boolean debugInfoWarning = false;

        if (!metaData.hasSrcFileData() && settings.getViewType() == IReportDataView.HIER_SRC_VIEW) {
            debugInfoWarning = true;
            Logger.getLogger().warning("Not all instrumented classes were compiled with source file debug data.");
            settings.setViewType(IReportDataView.HIER_CLS_VIEW);
        }

        if (!metaData.hasLineNumberData()) {
            debugInfoWarning = true;
            Logger.getLogger().warning("Line coverage requested but not all classes have line number debug data.");
        }

        if (debugInfoWarning) {
            Logger.getLogger().warning("Some debug data is missing. Adjusting report settings accordingly.");
        }
    }
}

public class LoggerWrapper {
    private final Logger logger;

    public LoggerWrapper() {
        this.logger = Logger.getLogger();
    }

    public void logWarning(String message) {
        logger.warning(message);
    }

    public void logError(String message) {
        logger.error(message);
    }
}