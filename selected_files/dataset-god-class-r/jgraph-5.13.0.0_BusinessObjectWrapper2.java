// Classe dedicada à manipulação de nós de árvore
class TreeNodeHandler {
    public static DefaultMutableTreeNode createTreeNode(Object userObject) {
        return new DefaultMutableTreeNode(userObject);
    }

    public static String getNodeLabel(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof BusinessObjectWrapper2) {
            return ((BusinessObjectWrapper2) userObject).getLabel();
        }
        return userObject.toString();
    }
}

// Versão refatorada da classe principal
public class BusinessObjectWrapper2 {
    private String label;
    private CellView[] portviews;
    private DefaultMutableTreeNode value;

    public BusinessObjectWrapper2(String label, DefaultMutableTreeNode value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CellView[] getPortviews() {
        return portviews;
    }

    public void setPortviews(CellView[] portviews) {
        this.portviews = portviews;
    }

    public DefaultMutableTreeNode getValue() {
        return value;
    }

    public void setValue(DefaultMutableTreeNode value) {
        this.value = value;
    }

    public DefaultMutableTreeNode toTreeNode() {
        return TreeNodeHandler.createTreeNode(this);
    }

    @Override
    public String toString() {
        return label != null ? label : "Unlabeled Business Object";
    }
}