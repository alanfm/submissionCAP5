// Classe dedicada à manipulação de buffers
class BufferHandler {
    private final byte[] buffer;
    private int position;

    public BufferHandler(int bufferSize) {
        this.buffer = new byte[bufferSize];
        this.position = 0;
    }

    public void write(byte[] data) throws IOException {
        if (data.length > buffer.length - position) {
            flushBuffer();
        }
        System.arraycopy(data, 0, buffer, position, data.length);
        position += data.length;
    }

    public void flushBuffer(OutputStream outputStream) throws IOException {
        if (position > 0) {
            outputStream.write(buffer, 0, position);
            position = 0;
        }
    }

    public void reset() {
        position = 0;
    }
}

// Classe dedicada à codificação de caracteres
class CharacterEncoder {
    private final String encoding;

    public CharacterEncoder(String encoding) {
        this.encoding = encoding;
    }

    public byte[] encode(String text) throws UnsupportedEncodingException {
        return text.getBytes(encoding);
    }

    public byte[] encode(char[] chars) throws UnsupportedEncodingException {
        return new String(chars).getBytes(encoding);
    }
}

// Versão refatorada da classe principal
public class ToStream {
    private final OutputStream outputStream;
    private final BufferHandler bufferHandler;
    private final CharacterEncoder characterEncoder;

    public ToStream(OutputStream outputStream, String encoding, int bufferSize) {
        this.outputStream = outputStream;
        this.bufferHandler = new BufferHandler(bufferSize);
        this.characterEncoder = new CharacterEncoder(encoding);
    }

    public void writeData(String data) throws IOException {
        byte[] encodedData = characterEncoder.encode(data);
        bufferHandler.write(encodedData);
    }

    public void writeData(char[] data) throws IOException {
        byte[] encodedData = characterEncoder.encode(data);
        bufferHandler.write(encodedData);
    }

    public void flush() throws IOException {
        bufferHandler.flushBuffer(outputStream);
    }

    public void reset() {
        bufferHandler.reset();
    }

    public void startElement(String elementName) throws IOException {
        writeData("<" + elementName + ">");
    }

    public void endElement(String elementName) throws IOException {
        writeData("</" + elementName + ">");
    }

    @Override
    public void close() throws IOException {
        flush();
        outputStream.close();
    }
}