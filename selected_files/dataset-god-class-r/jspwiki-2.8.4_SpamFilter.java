// Classe dedicada à manipulação de listas de palavras proibidas
class WordListParser {
    private final PatternCompiler compiler = new Perl5Compiler();

    public Collection<Pattern> parseWordList(String list) {
        List<Pattern> compiledPatterns = new ArrayList<>();
        if (list != null) {
            StringTokenizer tokenizer = new StringTokenizer(list, " \t");
            while (tokenizer.hasMoreTokens()) {
                String pattern = tokenizer.nextToken();
                try {
                    compiledPatterns.add(compiler.compile(pattern));
                } catch (MalformedPatternException e) {
                    Logger.getLogger(SpamFilter.class).debug("Malformed spam filter pattern: " + pattern);
                }
            }
        }
        return compiledPatterns;
    }

    public Collection<Pattern> parseBlacklist(String blacklistContent) {
        List<Pattern> compiledPatterns = new ArrayList<>();
        if (blacklistContent != null) {
            String[] lines = blacklistContent.split("\n");
            for (String line : lines) {
                try {
                    compiledPatterns.add(compiler.compile(line.trim()));
                } catch (MalformedPatternException e) {
                    Logger.getLogger(SpamFilter.class).debug("Malformed blacklist pattern: " + line);
                }
            }
        }
        return compiledPatterns;
    }
}

// Classe dedicada à verificação de padrões de spam
class SpamPatternChecker {
    private final PatternMatcher matcher = new Perl5Matcher();

    public boolean matchesAnyPattern(String content, Collection<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (matcher.matches(content, pattern)) {
                return true;
            }
        }
        return false;
    }
}

// Classe dedicada à integração com Akismet
class AkismetIntegration {
    private final Akismet akismet;

    public AkismetIntegration(String apiKey, String baseUrl) {
        this.akismet = new Akismet(apiKey, baseUrl);
    }

    public boolean isSpam(HttpServletRequest request, Change change) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");
        String permalink = ""; // URL da página sendo editada
        String commentType = "edit"; // Tipo de comentário
        String commentAuthor = ""; // Nome do autor
        String commentAuthorEmail = "";
        String commentAuthorURL = "";
        return akismet.commentCheck(ipAddress, userAgent, referrer, permalink, commentType, commentAuthor, commentAuthorEmail, commentAuthorURL, change.toString(), null);
    }
}

// Versão refatorada da classe principal
public class SpamFilter extends BasicPageFilter {
    private final WordListParser wordListParser = new WordListParser();
    private final SpamPatternChecker patternChecker = new SpamPatternChecker();
    private final AkismetIntegration akismetIntegration;
    private final Vector<Host> temporaryBanList = new Vector<>();
    private final int maxUrls;
    private final int limitSinglePageChanges;
    private final int limitSimilarChanges;
    private final int banTime;
    private Collection<Pattern> spamPatterns;
    private Date lastRebuild = new Date(0L);

    public SpamFilter(Properties properties) {
        this.maxUrls = Integer.parseInt(properties.getProperty(PROP_MAXURLS, "10"));
        this.limitSinglePageChanges = Integer.parseInt(properties.getProperty(PROP_PAGECHANGES, "5"));
        this.limitSimilarChanges = Integer.parseInt(properties.getProperty("similarchanges", "2"));
        this.banTime = Integer.parseInt(properties.getProperty("bantime", "60"));

        String apiKey = properties.getProperty(PROP_AKISMET_API_KEY);
        String baseUrl = properties.getProperty("baseurl");
        this.akismetIntegration = apiKey != null && !apiKey.isEmpty() ? new AkismetIntegration(apiKey, baseUrl) : null;
    }

    @Override
    public String preSave(WikiContext context, String content) throws RedirectException {
        cleanBanList();
        refreshBlacklists(context);

        Change change = getChange(context, content);
        if (!ignoreThisUser(context)) {
            checkBanList(context, change);
            checkSinglePageChange(context, content, change);
            checkPatternList(context, content, change);
        }

        checkAkismet(context, change);
        log(context, ACCEPT, "-", change.toString());
        return content;
    }

    private void checkPatternList(WikiContext context, String content, Change change) throws RedirectException {
        if (spamPatterns != null && patternChecker.matchesAnyPattern(content, spamPatterns)) {
            throw new RedirectException("Conteúdo contém padrões de spam.", getRedirectPage(context));
        }
    }

    private void checkAkismet(WikiContext context, Change change) throws RedirectException {
        if (akismetIntegration != null) {
            HttpServletRequest request = context.getHttpRequest();
            if (request != null && akismetIntegration.isSpam(request, change)) {
                throw new RedirectException("Akismet detectou spam.", getRedirectPage(context));
            }
        }
    }

    private synchronized void cleanBanList() {
        long now = System.currentTimeMillis();
        Iterator<Host> iterator = temporaryBanList.iterator();
        while (iterator.hasNext()) {
            Host host = iterator.next();
            if (host.getReleaseTime() < now) {
                iterator.remove();
            }
        }
    }

    private void refreshBlacklists(WikiContext context) {
        // Lógica para atualizar listas de spam
    }

    private static final String PROP_MAXURLS = "maxurls";
    private static final String PROP_PAGECHANGES = "pagechangesinminute";
    private static final String PROP_AKISMET_API_KEY = "akismet-apikey";

    private static class Change {
        private String m_change;
        private int m_adds;
        private int m_removals;
    }

    private static class Host {
        private final String address;
        private final long releaseTime;

        public Host(String address, Long releaseTime) {
            this.address = address;
            this.releaseTime = releaseTime != null ? releaseTime : System.currentTimeMillis() + 60 * 1000;
        }

        public String getAddress() {
            return address;
        }

        public long getReleaseTime() {
            return releaseTime;
        }
    }
}