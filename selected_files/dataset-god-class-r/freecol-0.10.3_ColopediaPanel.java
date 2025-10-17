// Versão refatorada da classe principal
public class ColopediaPanel extends FreeColPanel implements TreeSelectionListener {
    private JPanel detailPanel;
    private JTree tree;

    public ColopediaPanel(Canvas parent, PanelType panelType, FreeColGameObjectType objectType) {
        super(parent);
        initialize(panelType, objectType);
    }

    public void initialize(PanelType panelType, FreeColGameObjectType type) {
        detailPanel.removeAll();
        DefaultMutableTreeNode node = nodeMap.get(panelType.toString());
        tree.expandPath(new TreePath(node.getPath()));
        selectDetail(panelType, type);
        detailPanel.validate();
    }

    private void selectDetail(PanelType panelType, FreeColGameObjectType type) {
        detailPanel.removeAll();
        if (type instanceof GoodsType) {
            DetailBuilder.buildGoodsDetail((GoodsType) type, detailPanel);
        } else if (type instanceof BuildingType) {
            DetailBuilder.buildBuildingDetail((BuildingType) type, detailPanel);
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    @Override
    public void valueChanged(TreeSelectionEvent event) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        ColopediaTreeItem item = (ColopediaTreeItem) node.getUserObject();
        selectDetail(item.getPanelType(), item.getFreeColGameObjectType());
    }

    private JTree buildTree() {
        return TreeBuilder.buildTree(getSpecification());
    }
}

// Classe dedicada à manipulação de detalhes de objetos FreeCol
class DetailBuilder {
    public static void buildGoodsDetail(GoodsType type, JPanel detailPanel) {
        detailPanel.setLayout(new MigLayout("wrap 4", "[]20[]"));
        JLabel name = localizedLabel(type.getNameKey());
        name.setFont(smallHeaderFont);
        detailPanel.add(name, "span, align center, wrap 40");

        if (type.isFarmed()) {
            List<TileImprovementType> improvements = new ArrayList<>();
            List<Modifier> modifiers = new ArrayList<>();
            for (TileImprovementType improvementType : getSpecification().getTileImprovementTypeList()) {
                Modifier productionModifier = improvementType.getProductionModifier(type);
                if (productionModifier != null) {
                    improvements.add(improvementType);
                    modifiers.add(productionModifier);
                }
            }
            detailPanel.add(localizedLabel("colopedia.goods.improvedBy"), "newline 20, top");
            for (int index = 0; index < improvements.size(); index++) {
                String constraints = (index == 0) ? "span" : "skip, span";
                detailPanel.add(localizedLabel(StringTemplate.template("colopedia.goods.improvement")
                        .addName("%name%", improvements.get(index))
                        .addName("%amount%", getModifierAsString(modifiers.get(index)))), constraints);
            }
        }
    }

    public static void buildBuildingDetail(BuildingType buildingType, JPanel detailPanel) {
        detailPanel.setLayout(new MigLayout("wrap 4", "[]20[]"));
        JLabel name = localizedLabel(buildingType.getNameKey());
        name.setFont(smallHeaderFont);
        detailPanel.add(name, "span, align center, wrap 40");

        GoodsType inputType = buildingType.getConsumedGoodsType();
        GoodsType outputType = buildingType.getProducedGoodsType();
        if (outputType != null) {
            detailPanel.add(localizedLabel("colopedia.buildings.production"), "newline");
            if (inputType != null) {
                detailPanel.add(getGoodsButton(inputType), "span, split 3");
                detailPanel.add(new JLabel("\u2192"), "span");
            }
            detailPanel.add(getGoodsButton(outputType));
        }
    }
}

// Classe dedicada à construção da árvore de navegação
class TreeBuilder {
    public static JTree buildTree(Specification specification) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ColopediaTreeItem(null, Messages.message("menuBar.colopedia")));
        DefaultMutableTreeNode terrain = new DefaultMutableTreeNode(new ColopediaTreeItem(PanelType.TERRAIN));
        buildTerrainSubtree(terrain, specification);
        root.add(terrain);

        DefaultMutableTreeNode buildings = new DefaultMutableTreeNode(new ColopediaTreeItem(PanelType.BUILDINGS));
        buildBuildingSubtree(buildings, specification);
        root.add(buildings);

        DefaultMutableTreeNode fathers = new DefaultMutableTreeNode(new ColopediaTreeItem(PanelType.FATHERS));
        buildFathersSubtree(fathers, specification);
        root.add(fathers);

        JTree tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setCellRenderer(new ColopediaTreeCellRenderer());
        return tree;
    }

    private static void buildTerrainSubtree(DefaultMutableTreeNode parent, Specification specification) {
        // Lógica para construir subárvore de terrenos
    }

    private static void buildBuildingSubtree(DefaultMutableTreeNode parent, Specification specification) {
        // Lógica para construir subárvore de edifícios
    }

    private static void buildFathersSubtree(DefaultMutableTreeNode parent, Specification specification) {
        // Lógica para construir subárvore de pais fundadores
    }
}
