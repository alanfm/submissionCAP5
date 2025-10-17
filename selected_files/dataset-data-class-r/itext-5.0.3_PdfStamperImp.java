public class PdfStamperImp {
    private final ContentWriter contentWriter;
    private final EntityProcessor entityProcessor;

    public PdfStamperImp(ContentWriter contentWriter, EntityProcessor entityProcessor) {
        this.contentWriter = contentWriter;
        this.entityProcessor = entityProcessor;
    }

    public void addAnnotation(String annotation) {
        contentWriter.writeAnnotation(annotation);
    }

    public void processCharacters(char[] chars, int start, int length) {
        entityProcessor.processCharacters(chars, start, length);
    }
}

class ContentWriter {
    private final Writer writer;

    public ContentWriter(Writer writer) {
        this.writer = writer;
    }

    public void writeAnnotation(String annotation) {
        try {
            writer.write(annotation);
        } catch (IOException e) {
            throw new RuntimeException("Error writing annotation", e);
        }
    }

    public void writeCData(String data) {
        try {
            writer.write("<![CDATA[" + data + "]]>");
        } catch (IOException e) {
            throw new RuntimeException("Error writing CDATA", e);
        }
    }
}

class EntityProcessor {
    private final CharInfo charInfo;
    private final EncodingInfo encodingInfo;

    public EntityProcessor(CharInfo charInfo, EncodingInfo encodingInfo) {
        this.charInfo = charInfo;
        this.encodingInfo = encodingInfo;
    }

    public void processCharacters(char[] chars, int start, int length) {
        for (int i = start; i < start + length; i++) {
            if (charInfo.shouldMapTextChar(chars[i])) {
                // Process mapped characters
            }
            if (!encodingInfo.isInEncoding(chars[i])) {
                // Handle characters not in encoding
            }
        }
    }
}

class CharInfo {
    public boolean shouldMapTextChar(char ch) {
        // Lógica para verificar se o caractere deve ser mapeado
        return false;
    }
}

class EncodingInfo {
    public boolean isInEncoding(char ch) {
        // Lógica para verificar se o caractere está na codificação
        return true;
    }
}