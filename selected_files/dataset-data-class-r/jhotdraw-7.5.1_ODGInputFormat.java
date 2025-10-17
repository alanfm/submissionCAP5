public class ODGInputFormat {
    private final DataProcessor dataProcessor;
    private final Renderer renderer;

    public ODGInputFormat(DataProcessor dataProcessor, Renderer renderer) {
        this.dataProcessor = dataProcessor;
        this.renderer = renderer;
    }

    public void processInput(String input) {
        dataProcessor.process(input);
        renderer.render(dataProcessor.getData());
    }
}

class DataProcessor {
    private String data;

    public void process(String input) {
        // Lógica para processar os dados de entrada
        this.data = input.trim();
    }

    public String getData() {
        return data;
    }
}

class Renderer {
    public void render(String data) {
        // Lógica para renderizar os dados processados
        System.out.println("Rendering: " + data);
    }
}