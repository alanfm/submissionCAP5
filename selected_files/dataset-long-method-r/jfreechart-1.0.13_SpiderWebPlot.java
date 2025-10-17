import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.data.category.CategoryDataset;

public class SpiderWebPlot extends Plot {
    private DataProcessor dataProcessor;
    private Renderer renderer;
    private LegendGenerator legendGenerator;

    public SpiderWebPlot(CategoryDataset dataset) {
        this.dataProcessor = new DataProcessor(dataset);
        this.renderer = new Renderer();
        this.legendGenerator = new LegendGenerator();
    }

    @Override
    public void draw(Graphics2D g2, Rectangle2D area, PlotState parentState, PlotRenderingInfo info) {
        // Ajusta a área para margens
        RectangleInsets insets = getInsets();
        insets.trim(area);

        if (info != null) {
            info.setPlotArea(area);
            info.setDataArea(area);
        }

        // Desenha o fundo e o contorno
        drawBackground(g2, area);
        drawOutline(g2, area);

        // Processa os dados e desenha o gráfico
        int seriesCount = dataProcessor.getSeriesCount();
        int categoryCount = dataProcessor.getCategoryCount();

        if (seriesCount > 0 && categoryCount > 0) {
            dataProcessor.calculateMaxValue(seriesCount, categoryCount);
            renderer.drawWeb(g2, area, seriesCount, categoryCount, dataProcessor.getMaxValue());
            renderer.drawSeries(g2, area, seriesCount, categoryCount, dataProcessor);
        } else {
            drawNoDataMessage(g2, area);
        }
    }

    @Override
    public LegendItemCollection getLegendItems() {
        return legendGenerator.generateLegendItems(dataProcessor.getDataset(), getSeriesPaint(), getSeriesOutlinePaint());
    }
}

public class DataProcessor {
    private CategoryDataset dataset;
    private double maxValue;

    public DataProcessor(CategoryDataset dataset) {
        this.dataset = dataset;
        this.maxValue = 0;
    }

    public int getSeriesCount() {
        return dataset.getRowCount();
    }

    public int getCategoryCount() {
        return dataset.getColumnCount();
    }

    public Number getPlotValue(int series, int category) {
        return dataset.getValue(series, category);
    }

    public void calculateMaxValue(int seriesCount, int categoryCount) {
        for (int series = 0; series < seriesCount; series++) {
            for (int category = 0; category < categoryCount; category++) {
                Number value = getPlotValue(series, category);
                if (value != null && value.doubleValue() > maxValue) {
                    maxValue = value.doubleValue();
                }
            }
        }
    }

    public double getMaxValue() {
        return maxValue;
    }

    public CategoryDataset getDataset() {
        return dataset;
    }
}

public class Renderer {
    public void drawWeb(Graphics2D g2, Rectangle2D area, int seriesCount, int categoryCount, double maxValue) {
        Point2D center = new Point2D.Double(area.getCenterX(), area.getCenterY());
        double radius = Math.min(area.getWidth(), area.getHeight()) / 2.0;

        for (int i = 0; i < categoryCount; i++) {
            double angle = getAngle(i, categoryCount);
            Point2D endPoint = getPointOnCircle(center, radius, angle);
            Line2D line = new Line2D.Double(center, endPoint);
            g2.draw(line);
        }
    }

    public void drawSeries(Graphics2D g2, Rectangle2D area, int seriesCount, int categoryCount, DataProcessor dataProcessor) {
        Point2D center = new Point2D.Double(area.getCenterX(), area.getCenterY());
        double radius = Math.min(area.getWidth(), area.getHeight()) / 2.0;

        for (int series = 0; series < seriesCount; series++) {
            Polygon polygon = new Polygon();
            for (int category = 0; category < categoryCount; category++) {
                double angle = getAngle(category, categoryCount);
                Number value = dataProcessor.getPlotValue(series, category);
                double normalizedValue = value != null ? value.doubleValue() / dataProcessor.getMaxValue() : 0;
                Point2D point = getPointOnCircle(center, radius * normalizedValue, angle);
                polygon.addPoint((int) point.getX(), (int) point.getY());
            }
            g2.drawPolygon(polygon);
        }
    }

    private double getAngle(int index, int totalCategories) {
        return (2 * Math.PI / totalCategories) * index;
    }

    private Point2D getPointOnCircle(Point2D center, double radius, double angle) {
        double x = center.getX() + radius * Math.cos(angle);
        double y = center.getY() + radius * Math.sin(angle);
        return new Point2D.Double(x, y);
    }
}

public class LegendGenerator {
    public LegendItemCollection generateLegendItems(CategoryDataset dataset, Paint[] seriesPaint, Paint[] seriesOutlinePaint) {
        LegendItemCollection result = new LegendItemCollection();

        for (int series = 0; series < dataset.getRowCount(); series++) {
            String label = dataset.getRowKey(series).toString();
            Shape shape = new Rectangle(10, 10);
            Paint paint = seriesPaint[series % seriesPaint.length];
            Paint outlinePaint = seriesOutlinePaint[series % seriesOutlinePaint.length];

            LegendItem item = new LegendItem(label, null, null, null, shape, paint, outlinePaint);
            result.add(item);
        }

        return result;
    }
}