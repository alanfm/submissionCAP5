import javax.swing.*;
import java.awt.*;

public class SimpleStorage {
    private final UIPanelFactory panelFactory;

    public SimpleStorage() {
        this.panelFactory = new UIPanelFactory();
    }

    public JPanel createTaskFieldPanel(Language language) {
        return panelFactory.createTaskFieldPanel(language);
    }

    public JPanel createResFieldPanel(Language language) {
        return panelFactory.createResFieldPanel(language);
    }
}

class UIPanelFactory {
    public JPanel createTaskFieldPanel(Language language) {
        JPanel taskFieldPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        addComponent(taskFieldPanel, new JLabel(language.getText("length")), gbc, 7, 0, 1, 1);
        addComponent(taskFieldPanel, new JCheckBox(), gbc, 0, 1, 1, 1);
        addComponent(taskFieldPanel, new JLabel(language.getText("dateOfBegining")), gbc, 1, 1, 1, 1);
        addComponent(taskFieldPanel, new JCheckBox(), gbc, 3, 1, 1, 1);
        addComponent(taskFieldPanel, new JLabel(language.getText("dateOfEnd")), gbc, 4, 1, 1, 1);

        return taskFieldPanel;
    }

    public JPanel createResFieldPanel(Language language) {
        JPanel resFieldPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        addComponent(resFieldPanel, new JCheckBox(), gbc, 0, 0, 1, 1);
        addComponent(resFieldPanel, new JLabel(language.getText("id")), gbc, 1, 0, 1, 1);
        addComponent(resFieldPanel, new JCheckBox(), gbc, 3, 0, 1, 1);
        addComponent(resFieldPanel, new JLabel(language.getText("name")), gbc, 4, 0, 1, 1);

        return resFieldPanel;
    }

    private void addComponent(JPanel panel, Component component, GridBagConstraints gbc, int x, int y, int width, int height) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        panel.add(component, gbc);
    }
}