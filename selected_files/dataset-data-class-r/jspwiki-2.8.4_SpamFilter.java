public class SpamFilter {
    private final RuleProcessor ruleProcessor;
    private final Logger logger;

    public SpamFilter(RuleProcessor ruleProcessor, Logger logger) {
        this.ruleProcessor = ruleProcessor;
        this.logger = logger;
    }

    public boolean isSpam(String content, WikiContext context) {
        return ruleProcessor.applyRules(content, context);
    }
}

class RuleProcessor {
    private final RuleSet ruleSet;

    public RuleProcessor(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    public boolean applyRules(String content, WikiContext context) {
        for (Rule rule : ruleSet.getRules()) {
            if (rule.matches(content)) {
                return true; // Spam detected
            }
        }
        return false; // No spam detected
    }
}

class RuleSet {
    private final List<Rule> rules;

    public RuleSet(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }
}

class Rule {
    private final String pattern;

    public Rule(String pattern) {
        this.pattern = pattern;
    }

    public boolean matches(String content) {
        return content.contains(pattern);
    }
}