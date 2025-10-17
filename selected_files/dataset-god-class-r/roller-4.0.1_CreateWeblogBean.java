// Classe dedicada à validação de campos
class WeblogValidator {
    public static void validateHandle(String handle) {
        if (handle == null || handle.isEmpty()) {
            throw new IllegalArgumentException("O identificador do weblog não pode ser nulo ou vazio.");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("O nome do weblog não pode ser nulo ou vazio.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("O endereço de e-mail fornecido é inválido.");
        }
    }

    public static void validateLocale(String locale) {
        if (locale == null || locale.isEmpty()) {
            throw new IllegalArgumentException("O localidade (locale) não pode ser nulo ou vazio.");
        }
    }

    public static void validateTimeZone(String timeZone) {
        if (timeZone == null || timeZone.isEmpty()) {
            throw new IllegalArgumentException("O fuso horário não pode ser nulo ou vazio.");
        }
    }
}

// Versão refatorada da classe principal
public class CreateWeblogBean {
    private String handle;
    private String name;
    private String description;
    private String emailAddress;
    private String locale;
    private String timeZone;
    private String theme;

    public CreateWeblogBean(String handle, String name, String description, String emailAddress, String locale, String timeZone, String theme) {
        this.handle = handle;
        this.name = name;
        this.description = description;
        this.emailAddress = emailAddress;
        this.locale = locale;
        this.timeZone = timeZone;
        this.theme = theme;

        validate();
    }

    private void validate() {
        WeblogValidator.validateHandle(handle);
        WeblogValidator.validateName(name);
        WeblogValidator.validateEmail(emailAddress);
        WeblogValidator.validateLocale(locale);
        WeblogValidator.validateTimeZone(timeZone);
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        WeblogValidator.validateHandle(handle);
        this.handle = handle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        WeblogValidator.validateName(name);
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        WeblogValidator.validateEmail(emailAddress);
        this.emailAddress = emailAddress;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        WeblogValidator.validateLocale(locale);
        this.locale = locale;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        WeblogValidator.validateTimeZone(timeZone);
        this.timeZone = timeZone;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return "CreateWeblogBean{" +
                "handle='" + handle + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", locale='" + locale + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", theme='" + theme + '\'' +
                '}';
    }
}