import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import java.util.Hashtable;
import java.util.Iterator;
import org.jdom.Element;
import java.util.List;
import java.util.ArrayList;

public class MenuHelper {
    private static final Log log = LogFactory.getLog(MenuHelper.class);
    private static Hashtable<String, ParsedMenu> menus = new Hashtable<>();
    private MenuBuilder menuBuilder;
    private PermissionValidator permissionValidator;

    static {
        try {
            initializeMenus();
        } catch (Exception ex) {
            log.error("Error parsing menu configs", ex);
        }
    }

    public MenuHelper() {
        this.menuBuilder = new MenuBuilder();
        this.permissionValidator = new PermissionValidator();
    }

    public static void initializeMenus() throws Exception {
        menus.put("editor", unmarshall(MenuHelper.class.getResourceAsStream("/org/apache/roller/weblogger/ui/struts2/editor/editor-menu.xml")));
        menus.put("admin", unmarshall(MenuHelper.class.getResourceAsStream("/org/apache/roller/weblogger/ui/struts2/admin/admin-menu.xml")));
    }

    public Menu getMenu(String menuId, String currentAction, User user, Weblog weblog) {
        if (menuId == null) {
            return null;
        }

        ParsedMenu menuConfig = menus.get(menuId);
        if (menuConfig != null) {
            return menuBuilder.buildMenu(menuConfig, currentAction, user, weblog);
        }
        return null;
    }

    private static ParsedMenu unmarshall(InputStream inputStream) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(inputStream);
        Element rootElement = document.getRootElement();
        return parseMenu(rootElement);
    }

    private static ParsedMenu parseMenu(Element rootElement) {
        ParsedMenu parsedMenu = new ParsedMenu();
        for (Element tabElement : rootElement.getChildren("tab")) {
            parsedMenu.addTab(MenuParser.elementToParsedTab(tabElement));
        }
        return parsedMenu;
    }
}

public class MenuBuilder {
    public Menu buildMenu(ParsedMenu menuConfig, String currentAction, User user, Weblog weblog) {
        Menu menu = new Menu();

        Iterator<ParsedTab> tabsIter = menuConfig.getTabs().iterator();
        while (tabsIter.hasNext()) {
            ParsedTab configTab = tabsIter.next();

            if (isTabAllowed(configTab, user, weblog)) {
                MenuTab tab = createMenuTab(configTab, currentAction);
                menu.addTab(tab);
            }
        }

        return menu;
    }

    private boolean isTabAllowed(ParsedTab configTab, User user, Weblog weblog) {
        if (!configTab.isEnabled()) {
            return false;
        }

        if (!PermissionValidator.isPermitted(configTab.getPerm(), user, weblog)) {
            return false;
        }

        return true;
    }

    private MenuTab createMenuTab(ParsedTab configTab, String currentAction) {
        MenuTab tab = new MenuTab();
        tab.setKey(configTab.getName());

        boolean firstItem = true;
        for (ParsedTabItem configTabItem : configTab.getTabItems()) {
            if (isItemAllowed(configTabItem)) {
                MenuTabItem tabItem = createMenuTabItem(configTabItem, currentAction);
                tab.addItem(tabItem);

                if (firstItem) {
                    tab.setAction(tabItem.getAction());
                    firstItem = false;
                }
            }
        }

        return tab;
    }

    private boolean isItemAllowed(ParsedTabItem configTabItem) {
        return configTabItem.isEnabled() && configTabItem.isAccessible();
    }

    private MenuTabItem createMenuTabItem(ParsedTabItem configTabItem, String currentAction) {
        MenuTabItem tabItem = new MenuTabItem();
        tabItem.setKey(configTabItem.getName());
        tabItem.setAction(configTabItem.getAction());
        tabItem.setSelected(isSelected(currentAction, configTabItem));
        return tabItem;
    }

    private boolean isSelected(String currentAction, ParsedTabItem configTabItem) {
        return currentAction != null && currentAction.equals(configTabItem.getAction());
    }
}

public class PermissionValidator {
    public static boolean isPermitted(String perm, User user, Weblog weblog) {
        // Lógica para verificar permissões com base no usuário e weblog.
        return true; // Exemplo simplificado
    }

    public static boolean getBooleanProperty(String property) {
        // Lógica para obter propriedades booleanas do sistema.
        return Boolean.parseBoolean(property);
    }
}

public class MenuParser {
    public static ParsedTab elementToParsedTab(Element element) {
        ParsedTab tab = new ParsedTab();
        tab.setName(element.getAttributeValue("name"));
        tab.setPerm(element.getAttributeValue("perms"));
        tab.setRole(element.getAttributeValue("roles"));
        tab.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        tab.setDisabledProperty(element.getAttributeValue("disabledProperty"));

        List<?> menuItems = element.getChildren("menu-item");
        Iterator<?> iter = menuItems.iterator();
        while (iter.hasNext()) {
            Element menuItemElement = (Element) iter.next();
            tab.addItem(elementToParsedTabItem(menuItemElement));
        }

        return tab;
    }

    public static ParsedTabItem elementToParsedTabItem(Element element) {
        ParsedTabItem item = new ParsedTabItem();
        item.setName(element.getAttributeValue("name"));
        item.setAction(element.getAttributeValue("action"));
        item.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        item.setDisabledProperty(element.getAttributeValue("disabledProperty"));
        return item;
    }
}

public class ParsedMenu {
    private List<ParsedTab> tabs = new ArrayList<>();

    public void addTab(ParsedTab tab) {
        tabs.add(tab);
    }

    public List<ParsedTab> getTabs() {
        return tabs;
    }
}

public class ParsedTab {
    private String name;
    private String perm;
    private String role;
    private String enabledProperty;
    private String disabledProperty;
    private List<ParsedTabItem> tabItems = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPerm(String perm) {
        this.perm = perm;
    }

    public String getPerm() {
        return perm;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setEnabledProperty(String enabledProperty) {
        this.enabledProperty = enabledProperty;
    }

    public String getEnabledProperty() {
        return enabledProperty;
    }

    public void setDisabledProperty(String disabledProperty) {
        this.disabledProperty = disabledProperty;
    }

    public String getDisabledProperty() {
        return disabledProperty;
    }

    public void addItem(ParsedTabItem item) {
        tabItems.add(item);
    }

    public List<ParsedTabItem> getTabItems() {
        return tabItems;
    }

    public boolean isEnabled() {
        return PermissionValidator.getBooleanProperty(enabledProperty);
    }
}