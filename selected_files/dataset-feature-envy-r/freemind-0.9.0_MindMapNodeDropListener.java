public class MindMapNodeDropListener implements DropTargetListener {
    private final MindMapController mindMapController;
    private final DropValidator dropValidator;
    private final DropHandler dropHandler;

    public MindMapNodeDropListener(MindMapController controller) {
        this.mindMapController = controller;
        this.dropValidator = new DropValidator(controller);
        this.dropHandler = new DropHandler(controller);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (!dropValidator.isDragAcceptable(dtde)) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        // Implementação padrão
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // Implementação padrão
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // Implementação padrão
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            if (!dropValidator.isDropAcceptable(dtde)) {
                dtde.rejectDrop();
                return;
            }

            dtde.acceptDrop(dtde.getDropAction());
            dropHandler.handleDrop(dtde);
            dtde.dropComplete(true);
        } catch (Exception e) {
            UIFeedback.showError("Drop exception: " + e.getMessage());
            dtde.dropComplete(false);
        }
    }
}

public class DropValidator {
    private final MindMapController mindMapController;

    public DropValidator(MindMapController controller) {
        this.mindMapController = controller;
    }

    public boolean isDragAcceptable(DropTargetDragEvent ev) {
        return ev.isDataFlavorSupported(DataFlavor.stringFlavor) ||
               ev.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor);
    }

    public boolean isDropAcceptable(DropTargetDropEvent event) {
        MainView mainView = (MainView) event.getDropTargetContext().getComponent();
        MindMapNode targetNode = mainView.getNodeView().getModel();
        MindMapNode selectedNode = mindMapController.getSelected();

        return targetNode.isWriteable() &&
               targetNode != selectedNode &&
               !targetNode.isDescendantOf(selectedNode);
    }
}

public class DropHandler {
    private final MindMapController mindMapController;

    public DropHandler(MindMapController controller) {
        this.mindMapController = controller;
    }

    public void handleDrop(DropTargetDropEvent dtde) throws Exception {
        Transferable transferable = dtde.getTransferable();
        MainView mainView = (MainView) dtde.getDropTargetContext().getComponent();
        NodeView targetNodeView = mainView.getNodeView();
        MindMapNode targetNode = targetNodeView.getModel();

        if (dtde.isLocalTransfer()) {
            handleLocalDrop(transferable, targetNode, mainView, dtde.getDropAction());
        } else {
            handleExternalDrop(transferable, targetNode, mainView);
        }
    }

    private void handleLocalDrop(Transferable transferable, MindMapNode targetNode, MainView mainView, int dropAction) throws Exception {
        if (dropAction == DnDConstants.ACTION_MOVE) {
            mindMapController.cut();
        } else {
            mindMapController.copy();
        }

        mainView.selectAsTheOnlyOneSelected(targetNodeView);
        mindMapController.paste(transferable, targetNode,
                                mainView.dropAsSibling(mainView.getLocation().getX()),
                                mainView.dropPosition(mainView.getLocation().getX()));
    }

    private void handleExternalDrop(Transferable transferable, MindMapNode targetNode, MainView mainView) throws Exception {
        mindMapController.paste(transferable, targetNode,
                                mainView.dropAsSibling(mainView.getLocation().getX()),
                                mainView.dropPosition(mainView.getLocation().getX()));
    }
}

public class LinkManager {
    private final MindMapController mindMapController;

    public LinkManager(MindMapController controller) {
        this.mindMapController = controller;
    }

    public void createLinks(MindMapNode targetNode) {
        List<MindMapNodeModel> selectedNodes = mindMapController.getView().getSelecteds();

        if (selectedNodes.size() >= 5) {
            int confirmation = UIFeedback.showConfirmation(
                "Create " + selectedNodes.size() + " links to the same node?"
            );

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }
        }

        for (MindMapNodeModel selectedNode : selectedNodes) {
            mindMapController.addLink(selectedNode, targetNode);
        }
    }
}

public class UIFeedback {
    public static void showError(String message) {
        System.err.println(message);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static int showConfirmation(String message) {
        return JOptionPane.showConfirmDialog(null, message, "Confirmation", JOptionPane.YES_NO_OPTION);
    }
}