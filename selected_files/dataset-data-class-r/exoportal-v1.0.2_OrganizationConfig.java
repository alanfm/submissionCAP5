public class OrganizationConfig {
    private final ConfigValidator validator;
    private final ConfigStorage storage;

    public OrganizationConfig(ConfigValidator validator, ConfigStorage storage) {
        this.validator = validator;
        this.storage = storage;
    }

    public void updateConfig(Map<String, String> newConfig) {
        validator.validate(newConfig);
        storage.save(newConfig);
    }

    public Map<String, String> getConfig() {
        return storage.load();
    }
}

class ConfigValidator {
    public void validate(Map<String, String> config) {
        if (config == null || config.isEmpty()) {
            throw new IllegalArgumentException("Config cannot be null or empty");
        }
        // Adicionar validações específicas aqui
    }
}

class ConfigStorage {
    public void save(Map<String, String> config) {
        // Lógica para salvar a configuração
    }

    public Map<String, String> load() {
        // Lógica para carregar a configuração
        return new HashMap<>();
    }
}