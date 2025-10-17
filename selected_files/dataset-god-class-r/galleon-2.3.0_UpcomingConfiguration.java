// Classe dedicada à validação de configurações
class ConfigurationValidator {
    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty();
    }

    public static boolean isValidItems(List<String> items) {
        return items != null && !items.isEmpty();
    }
}

// Classe dedicada ao gerenciamento de itens
class ItemManager {
    private List<String> items;

    public ItemManager() {
        this.items = new ArrayList<>();
    }

    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<String> newItems) {
        if (ConfigurationValidator.isValidItems(newItems)) {
            this.items = new ArrayList<>(newItems);
        } else {
            throw new IllegalArgumentException("A lista de itens não pode ser nula ou vazia.");
        }
    }

    public void addItem(String item) {
        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException("O item não pode ser nulo ou vazio.");
        }
        items.add(item);
    }

    public void removeItem(String item) {
        items.remove(item);
    }
}

// Versão refatorada da classe principal
public class UpcomingConfiguration implements AppConfiguration, Serializable {
    private String mName;
    private final ItemManager itemManager;

    public UpcomingConfiguration() {
        this.itemManager = new ItemManager();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        if (ConfigurationValidator.isValidName(name)) {
            this.mName = name;
        } else {
            throw new IllegalArgumentException("O nome da configuração não pode ser nulo ou vazio.");
        }
    }

    public List<String> getItems() {
        return itemManager.getItems();
    }

    public void setItems(List<String> items) {
        itemManager.setItems(items);
    }

    public void addItem(String item) {
        itemManager.addItem(item);
    }

    public void removeItem(String item) {
        itemManager.removeItem(item);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mName", mName)
                .append("items", itemManager.getItems())
                .toString();
    }
}