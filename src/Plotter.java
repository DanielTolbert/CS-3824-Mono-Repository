import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

public class Plotter extends ApplicationFrame {

    public Plotter( String s, JFreeChart chart) {
        super(s);
        JPanel jpanel = createDemoPanel(chart);
        jpanel.setPreferredSize(new Dimension(640, 480));
        add(jpanel);
    }

    public static JPanel createDemoPanel(JFreeChart chart) {
        JFreeChart jfreechart = chart;
        Shape cross = ShapeUtilities.createDiagonalCross(3, 1);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesShape(0, cross);
        renderer.setSeriesPaint(0, Color.BLACK);
        return new ChartPanel(jfreechart);
    }


    public static void main(String args[], JFreeChart chart) {
        Plotter scatterplotdemo4 = new Plotter("Precision and Recall", chart);
        scatterplotdemo4.pack();
        RefineryUtilities.centerFrameOnScreen(scatterplotdemo4);
        scatterplotdemo4.setVisible(true);
    }
}
