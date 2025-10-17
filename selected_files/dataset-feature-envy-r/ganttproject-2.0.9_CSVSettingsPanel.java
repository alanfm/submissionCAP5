public class CSVSettingsPanel extends GeneralOptionPanel {
    private final ComponentInitializer componentInitializer;
    private final SettingsManager settingsManager;

    public CSVSettingsPanel(GanttProject parent) {
        super(
            GanttProject.correctLabel(GanttLanguage.getInstance().getText("csvexport")),
            GanttLanguage.getInstance().getText("settingsCVSExport"),
            parent
        );

        this.settingsManager = new SettingsManager(parent.getOptions().getCSVOptions());
        this.componentInitializer = new ComponentInitializer(this);

        initializeComponents();
    }

    private void initializeComponents() {
        vb.add(new JSeparator());
        componentInitializer.initializeGeneralOptions();
        componentInitializer.initializeTaskOptions();
        componentInitializer.initializeResourceOptions();
        settingsManager.loadInitialValues();
    }

    @Override
    public void initialize() {
        settingsManager.loadInitialValues();
    }
}

public class ComponentInitializer {
    private final CSVSettingsPanel panel;

    public ComponentInitializer(CSVSettingsPanel panel) {
        this.panel = panel;
    }

    public void initializeGeneralOptions() {
        JPanel genePanel = new JPanel(new BorderLayout());
        JLabel lblSeparatedField = new JLabel(GanttLanguage.getInstance().getText("separatedFields"));
        lblSeparatedField.setFont(new Font(lblSeparatedField.getFont().getFontName(), Font.BOLD, lblSeparatedField.getFont().getSize()));
        genePanel.add(lblSeparatedField, BorderLayout.WEST);
        panel.vb.add(genePanel);

        JPanel fixedPanel = new JPanel(new BorderLayout());
        fixedPanel.add(panel.bFixedSize = new JRadioButton(), BorderLayout.WEST);
        fixedPanel.add(new JLabel(GanttLanguage.getInstance().getText("fixedWidth")), BorderLayout.CENTER);
        panel.vb.add(fixedPanel);

        JPanel separatedPanel = new JPanel(new BorderLayout());
        separatedPanel.add(panel.bSeparatedText = new JRadioButton(), BorderLayout.WEST);
        separatedPanel.add(new JLabel(GanttLanguage.getInstance().getText("separated")), BorderLayout.CENTER);
        panel.vb.add(separatedPanel);

        // Adicionar outros componentes...
    }

    public void initializeTaskOptions() {
        JPanel taskPanel = new JPanel(new BorderLayout());
        JLabel lblTaskField = new JLabel(GanttLanguage.getInstance().getText("taskFields"));
        lblTaskField.setFont(new Font(lblTaskField.getFont().getFontName(), Font.BOLD, lblTaskField.getFont().getSize()));
        taskPanel.add(lblTaskField, BorderLayout.WEST);
        panel.vb.add(taskPanel);

        JPanel taskFieldPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.right = 15;
        gbc.insets.left = 10;
        gbc.insets.top = 10;

        addUsingGBL(taskFieldPanel, panel.cbTaskID = new JCheckBox(), gbc, 0, 0, 1, 1);
        addUsingGBL(taskFieldPanel, new JLabel(GanttLanguage.getInstance().getText("id")), gbc, 1, 0, 1, 1);
        addUsingGBL(taskFieldPanel, panel.cbTaskName = new JCheckBox(), gbc, 3, 0, 1, 1);
        addUsingGBL(taskFieldPanel, new JLabel(GanttLanguage.getInstance().getText("name")), gbc, 4, 0, 1, 1);
        panel.vb.add(taskFieldPanel);

        // Adicionar outros campos de tarefa...
    }

    public void initializeResourceOptions() {
        JPanel resPanel = new JPanel(new BorderLayout());
        JLabel lblResField = new JLabel(GanttLanguage.getInstance().getText("resFields"));
        lblResField.setFont(new Font(lblResField.getFont().getFontName(), Font.BOLD, lblResField.getFont().getSize()));
        resPanel.add(lblResField, BorderLayout.WEST);
        panel.vb.add(resPanel);

        JPanel resFieldPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.right = 15;
        gbc.insets.left = 10;
        gbc.insets.top = 10;

        addUsingGBL(resFieldPanel, panel.cbResID = new JCheckBox(), gbc, 0, 0, 1, 1);
        addUsingGBL(resFieldPanel, new JLabel(GanttLanguage.getInstance().getText("id")), gbc, 1, 0, 1, 1);
        addUsingGBL(resFieldPanel, panel.cbResName = new JCheckBox(), gbc, 3, 0, 1, 1);
        addUsingGBL(resFieldPanel, new JLabel(GanttLanguage.getInstance().getText("colName")), gbc, 4, 0, 1, 1);
        panel.vb.add(resFieldPanel);

        // Adicionar outros campos de recurso...
    }

    private void addUsingGBL(Container container, Component component, GridBagConstraints gbc, int gridx, int gridy, int gridwidth, int gridheight) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        container.add(component, gbc);
    }
}

public class SettingsManager {
    private final CSVOptions csvOptions;

    public SettingsManager(CSVOptions csvOptions) {
        this.csvOptions = csvOptions;
    }

    public void loadInitialValues() {
        csvOptions.setExportTaskID(csvOptions.isExportTaskID());
        csvOptions.setExportTaskName(csvOptions.isExportTaskName());
        csvOptions.setExportTaskStartDate(csvOptions.isExportTaskStartDate());
        csvOptions.setExportTaskEndDate(csvOptions.isExportTaskEndDate());
        csvOptions.setExportTaskPercent(csvOptions.isExportTaskPercent());
        csvOptions.setExportTaskDuration(csvOptions.isExportTaskDuration());
        csvOptions.setExportTaskWebLink(csvOptions.isExportTaskWebLink());
        csvOptions.setExportTaskResources(csvOptions.isExportTaskResources());
        csvOptions.setExportTaskNotes(csvOptions.isExportTaskNotes());
        csvOptions.setExportResourceID(csvOptions.isExportResourceID());
        csvOptions.setExportResourceName(csvOptions.isExportResourceName());
        csvOptions.setExportResourceMail(csvOptions.isExportResourceMail());
        csvOptions.setExportResourcePhone(csvOptions.isExportResourcePhone());
        csvOptions.setExportResourceRole(csvOptions.isExportResourceRole());
    }
}

public class OptionHandler {
    public static void handleCheckbox(JCheckBox checkBox, boolean value) {
        checkBox.setSelected(value);
    }

    public static void handleRadioButton(JRadioButton radioButton, boolean value) {
        radioButton.setSelected(value);
    }
}