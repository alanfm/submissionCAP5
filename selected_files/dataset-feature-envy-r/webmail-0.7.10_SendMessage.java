public class SendMessage implements Plugin, URLHandler, ConfigurationListener {
    private Storage store;
    private WebMailServer parent;
    private Session mailSession;

    public SendMessage(WebMailServer parent) {
        this.parent = parent;
        this.store = parent.getStorage();
        init();
    }

    protected void init() {
        Properties props = new Properties();
        props.put("mail.host", store.getConfig("SMTP HOST"));
        props.put("mail.smtp.host", store.getConfig("SMTP HOST"));
        mailSession = Session.getInstance(props, null);
    }

    @Override
    public XHTMLDocument handleRequest(Session session, HTTPRequestHeader head) throws Exception {
        try {
            MessageSender sender = new MessageSender(session, store, mailSession);
            return sender.sendMessage(head);
        } catch (Exception e) {
            store.log(Storage.LOG_ERR, e);
            throw new DocumentNotFoundException("Could not send message. (Reason: " + e.getMessage() + ")");
        }
    }

    @Override
    public String provides() {
        return "message send";
    }

    @Override
    public String requires() {
        return "composer";
    }
}

public class MessageSender {
    private final Session session;
    private final Storage store;
    private final javax.mail.Session mailSession;

    public MessageSender(Session session, Storage store, javax.mail.Session mailSession) {
        this.session = session;
        this.store = store;
        this.mailSession = mailSession;
    }

    public XHTMLDocument sendMessage(HTTPRequestHeader head) throws MessagingException, UnsupportedEncodingException {
        MimeMessage msg = new MimeMessage(mailSession);
        MessageBuilder builder = new MessageBuilder(session, store);

        // Constrói a mensagem
        builder.buildFrom(msg);
        builder.buildRecipients(msg, head);
        builder.buildHeaders(msg, head);
        builder.buildContent(msg, head);

        // Salva e envia a mensagem
        boolean saveSuccess = saveSentMessage(msg);
        boolean sendSuccess = sendMail(msg);

        return generateResultPage(saveSuccess, sendSuccess);
    }

    private boolean saveSentMessage(MimeMessage msg) {
        if (session.getUser().wantsSaveSent()) {
            try {
                Folder folder = session.getFolder(session.getUser().getSentFolder());
                folder.appendMessages(new Message[]{msg});
                return true;
            } catch (Exception e) {
                store.log(Storage.LOG_WARN, "Failed to save sent message: " + e.getMessage());
            }
        }
        return false;
    }

    private boolean sendMail(MimeMessage msg) {
        try {
            Transport.send(msg);
            return true;
        } catch (SendFailedException e) {
            session.handleTransportException(e);
            return false;
        }
    }

    private XHTMLDocument generateResultPage(boolean saveSuccess, boolean sendSuccess) {
        User user = session.getUserModel();
        return new XHTMLDocument(session.getModel(), store.getStylesheet("sendresult.xsl", user.getPreferredLocale(), user.getTheme()));
    }
}

public class MessageBuilder {
    private final Session session;
    private final Storage store;

    public MessageBuilder(Session session, Storage store) {
        this.session = session;
        this.store = store;
    }

    public void buildFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        Address from = createAddress(session.getUser().getEmail(), session.getUser().getFullName());
        msg.addFrom(new Address[]{from});
    }

    public void buildRecipients(MimeMessage msg, HTTPRequestHeader head) throws MessagingException, UnsupportedEncodingException {
        addRecipients(msg, Message.RecipientType.TO, head.getContent("TO"));
        addRecipients(msg, Message.RecipientType.CC, head.getContent("CC"));
        addRecipients(msg, Message.RecipientType.BCC, head.getContent("BCC"));
    }

    private void addRecipients(MimeMessage msg, Message.RecipientType type, String content) throws MessagingException, UnsupportedEncodingException {
        if (content != null && !content.trim().isEmpty()) {
            Address[] addresses = parseAddresses(content);
            msg.addRecipients(type, addresses);
        }
    }

    private Address[] parseAddresses(String content) throws UnsupportedEncodingException {
        StringTokenizer tokenizer = new StringTokenizer(content, ",;");
        Address[] addresses = new Address[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            addresses[i++] = new InternetAddress(token);
        }
        return addresses;
    }

    public void buildHeaders(MimeMessage msg, HTTPRequestHeader head) throws UnsupportedEncodingException {
        msg.setHeader("X-Mailer", WebMailServer.getVersion() + ", SendMessage plugin v" + SendMessage.VERSION);
        msg.setHeader("Subject", encodeText(head.getContent("SUBJECT")));
        if (head.isContentSet("REPLY-TO")) {
            msg.setHeader("Reply-To", encodeText(head.getContent("REPLY-TO")));
        }
        msg.setSentDate(new Date(System.currentTimeMillis()));
    }

    public void buildContent(MimeMessage msg, HTTPRequestHeader head) throws MessagingException {
        String body = head.getContent("BODY");
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body, "utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        msg.setContent(multipart);
    }

    private Address createAddress(String email, String fullName) throws UnsupportedEncodingException {
        Locale locale = session.getUserModel().getPreferredLocale();
        return new InternetAddress(
            TranscodeUtil.transcodeThenEncodeByLocale(email, null, locale),
            TranscodeUtil.transcodeThenEncodeByLocale(fullName, null, locale)
        );
    }

    private String encodeText(String text) throws UnsupportedEncodingException {
        return TranscodeUtil.transcodeThenEncodeByLocale(text, "ISO8859_1", session.getUserModel().getPreferredLocale());
    }
}

public class TranscodeUtil {
    public static String transcodeThenEncodeByLocale(String text, String encoding, Locale locale) throws UnsupportedEncodingException {
        if (text == null) return null;
        String charset = encoding != null ? encoding : "utf-8";
        return MimeUtility.encodeText(new String(text.getBytes(charset), "utf-8"), "utf-8", null);
    }
}