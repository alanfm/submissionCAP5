public class FactoryProperties {
    private final ElementFactory elementFactory;
    private final StyleManager styleManager;
    private final FontManager fontManager;

    public FactoryProperties(FontProvider fontImp) {
        this.elementFactory = new ElementFactory();
        this.styleManager = new StyleManager();
        this.fontManager = new FontManager(fontImp);
    }

    public Chunk createChunk(String text, ChainedProperties props) {
        Font font = fontManager.getFont(props);
        return elementFactory.createChunk(text, font, props);
    }

    public Paragraph createParagraph(ChainedProperties props) {
        Paragraph paragraph = elementFactory.createParagraph();
        styleManager.applyParagraphStyle(paragraph, props);
        return paragraph;
    }

    public ListItem createListItem(ChainedProperties props) {
        ListItem listItem = elementFactory.createListItem();
        styleManager.applyParagraphStyle(listItem, props);
        return listItem;
    }
}

public class ElementFactory {
    public Chunk createChunk(String text, Font font, ChainedProperties props) {
        float size = font.getSize() / 2;
        Chunk chunk = new Chunk(text, font);

        if (props.hasProperty("sub")) {
            chunk.setTextRise(-size);
        } else if (props.hasProperty("sup")) {
            chunk.setTextRise(size);
        }

        chunk.setHyphenation(getHyphenation(props));
        return chunk;
    }

    public Paragraph createParagraph() {
        return new Paragraph();
    }

    public ListItem createListItem() {
        return new ListItem();
    }

    private HyphenationEvent getHyphenation(ChainedProperties props) {
        String hyphenation = props.getProperty("hyphenation");
        return hyphenation != null ? new HyphenationAuto(hyphenation) : null;
    }
}

public class StyleManager {
    public void applyParagraphStyle(Paragraph paragraph, ChainedProperties props) {
        setAlignment(paragraph, props.getProperty("align"));
        setLeading(paragraph, props.getProperty("leading"));
        setSpacing(paragraph, props.getProperty("before"), props.getProperty("after"), props.getProperty("extraparaspace"));
    }

    private void setAlignment(Paragraph paragraph, String align) {
        if ("center".equalsIgnoreCase(align)) {
            paragraph.setAlignment(Element.ALIGN_CENTER);
        } else if ("right".equalsIgnoreCase(align)) {
            paragraph.setAlignment(Element.ALIGN_RIGHT);
        } else if ("justify".equalsIgnoreCase(align)) {
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
        }
    }

    private void setLeading(Paragraph paragraph, String leading) {
        if (leading == null) {
            paragraph.setLeading(0, 1.5f);
            return;
        }

        try {
            StringTokenizer tokenizer = new StringTokenizer(leading, " ,");
            float fixed = Float.parseFloat(tokenizer.nextToken());
            float multiplied = tokenizer.hasMoreTokens() ? Float.parseFloat(tokenizer.nextToken()) : 0;
            paragraph.setLeading(fixed, multiplied);
        } catch (Exception e) {
            paragraph.setLeading(0, 1.5f);
        }
    }

    private void setSpacing(Paragraph paragraph, String before, String after, String extra) {
        try {
            if (before != null) paragraph.setSpacingBefore(Float.parseFloat(before));
            if (after != null) paragraph.setSpacingAfter(Float.parseFloat(after));
            if (extra != null) paragraph.setExtraParagraphSpace(Float.parseFloat(extra));
        } catch (Exception e) {
            // Ignora valores inválidos
        }
    }
}

public class FontManager {
    private final FontProvider fontImp;

    public FontManager(FontProvider fontImp) {
        this.fontImp = fontImp;
    }

    public Font getFont(ChainedProperties props) {
        String face = props.getProperty("face");
        int style = getFontStyle(props);
        float size = getFontSize(props);
        BaseColor color = getColor(props.getProperty("color"));

        return fontImp.getFont(face, BaseFont.WINANSI, true, size, style, color);
    }

    private int getFontStyle(ChainedProperties props) {
        int style = 0;
        if (props.hasProperty("italic")) style |= Font.ITALIC;
        if (props.hasProperty("bold")) style |= Font.BOLD;
        if (props.hasProperty("underline")) style |= Font.UNDERLINE;
        if (props.hasProperty("strikethru")) style |= Font.STRIKETHRU;
        return style;
    }

    private float getFontSize(ChainedProperties props) {
        String size = props.getProperty("size");
        return size != null ? Float.parseFloat(size) : 12;
    }

    private BaseColor getColor(String color) {
        return color != null ? Markup.decodeColor(color) : null;
    }
}

public class PropertyParser {
    public static Map<String, String> parseAttributes(String style) {
        Map<String, String> attributes = new HashMap<>();
        String[] pairs = style.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                attributes.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return attributes;
    }
}