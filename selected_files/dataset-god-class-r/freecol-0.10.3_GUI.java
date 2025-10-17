// Classe dedicada à renderização do mapa
class MapRenderer {
    public static void displayBaseTile(Graphics2D g, Tile tile, ImageLibrary lib) {
        if (tile != null) {
            g.drawImage(lib.getTerrainImage(tile.getType(), tile.getX(), tile.getY()), 0, 0, null);
            if (tile.isExplored() && !tile.isLand() && tile.getStyle() > 0) {
                int edgeStyle = tile.getStyle() >> 4;
                if (edgeStyle > 0) {
                    g.drawImage(lib.getBeachEdgeImage(edgeStyle), 0, 0, null);
                }
                int cornerStyle = tile.getStyle() & 15;
                if (cornerStyle > 0) {
                    g.drawImage(lib.getBeachCornerImage(cornerStyle), 0, 0, null);
                }
            }
        }
    }

    public static void drawGrid(Graphics2D g, GeneralPath gridPath, Stroke gridStroke) {
        g.setStroke(gridStroke);
        g.setColor(Color.BLACK);
        g.draw(gridPath);
    }

    public static void drawCursor(Graphics2D g, Image cursorImage) {
        g.drawImage(cursorImage, 0, 0, null);
    }
}

// Classe dedicada ao gerenciamento de mensagens
class MessageManager {
    private final Vector<GUIMessage> messages;

    public MessageManager(int messageCount) {
        this.messages = new Vector<>(messageCount);
    }

    public synchronized void addMessage(GUIMessage message) {
        if (messages.size() == MESSAGE_COUNT) {
            messages.remove(0);
        }
        messages.add(message);
    }

    public GUIMessage getMessage(int index) {
        return messages.get(index);
    }

    public int getMessageCount() {
        return messages.size();
    }

    public void displayMessages(Graphics2D g, Font font, int screenWidth, int screenHeight) {
        Image stringImage = createStringImage(g, messages.get(0).getMessage(), messages.get(0).getColor(), font);
        int yy = screenHeight - 300 - getMessageCount() * stringImage.getHeight(null);
        int xx = 40;

        for (int i = 0; i < getMessageCount(); i++) {
            GUIMessage message = getMessage(i);
            g.drawImage(createStringImage(g, message.getMessage(), message.getColor(), font), xx, yy, null);
            yy += stringImage.getHeight(null);
        }
    }

    private Image createStringImage(Graphics2D g, String text, Color color, Font font) {
        // Lógica para criar uma imagem a partir de texto
        return null; // Placeholder
    }
}

// Versão refatorada da classe principal
public class GUI {
    private final FreeColClient freeColClient;
    private final Dimension size;
    private final ImageLibrary lib;
    private TerrainCursor cursor;
    private ViewMode viewMode;
    private final MessageManager messageManager;
    private Tile selectedTile;
    private Tile focus;
    private Unit activeUnit;
    private PathNode gotoPath;
    private boolean gotoStarted;

    public GUI(FreeColClient freeColClient, Dimension size, ImageLibrary lib) {
        this.freeColClient = freeColClient;
        this.size = size;
        this.lib = lib;
        this.messageManager = new MessageManager(MESSAGE_COUNT);
        this.cursor = new TerrainCursor();
        this.viewMode = new ViewMode(this);
        logger.info("GUI created.");
    }

    public void display(Graphics2D g) {
        if (freeColClient.getGame() != null && freeColClient.getGame().getMap() != null && focus != null && freeColClient.isInGame()) {
            removeOldMessages();
            displayMap(g);
        } else {
            if (freeColClient.isMapEditor()) {
                g.setColor(Color.black);
                g.fillRect(0, 0, size.width, size.height);
            } else {
                Image bgImage = ResourceManager.getImage("CanvasBackgroundImage", size);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, freeColClient.getCanvas());
                }
            }
        }
    }

    private void displayMap(Graphics2D g) {
        Map map = freeColClient.getGame().getMap();
        for (int column = firstColumn; column <= lastColumn; column++) {
            for (int row = firstRow; row <= lastRow; row++) {
                Tile tile = map.getTile(column, row);
                if (tile != null) {
                    MapRenderer.displayBaseTile(g, tile, lib);
                    if (viewMode.displayTileCursor(tile)) {
                        MapRenderer.drawCursor(g, cursorImage);
                    }
                }
            }
        }

        if (freeColClient.getClientOptions().getBoolean(ClientOptions.DISPLAY_GRID)) {
            MapRenderer.drawGrid(g, gridPath, gridStroke);
        }

        messageManager.displayMessages(g, ResourceManager.getFont("NormalFont", 12f), size.width, size.height);
    }

    public void addMessage(GUIMessage message) {
        messageManager.addMessage(message);
        freeColClient.getCanvas().repaint(0, 0, getWidth(), getHeight());
    }

    public void setFocus(Tile focus) {
        this.focus = focus;
        forceReposition();
        freeColClient.getCanvas().repaint(0, 0, getWidth(), getHeight());
    }

    private void forceReposition() {
        bottomRow = -1;
    }

    public int getWidth() {
        return size.width;
    }

    public int getHeight() {
        return size.height;
    }

    private void removeOldMessages() {
        // Lógica para remover mensagens antigas
    }
}