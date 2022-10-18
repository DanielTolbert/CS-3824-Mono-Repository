import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;

public class GraphDisplay {

    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 1000;
    private static HashMap<PathParser.PathType, Graph> graphMap;
    private static JFreeChart chart;

    private static void zoomGraphMouseWheelMoved( MouseWheelEvent mwe, ViewPanel viewPanel ) {
            if (mwe.getWheelRotation() > 0) {
                double new_view_percent = viewPanel.getCamera().getViewPercent() + 0.05;
                viewPanel.getCamera().setViewPercent(new_view_percent);
            } else if (mwe.getWheelRotation() < 0) {
                double current_view_percent = viewPanel.getCamera().getViewPercent();
                if(current_view_percent > 0.05){
                    viewPanel.getCamera().setViewPercent(current_view_percent - 0.05);
                }
            }
//        }
    }

    public static void main( String[] args ) {
        showPrecisionAndRecall();
        outputShortestPaths();
        showPrecisionAndRecallShortestPaths();
        plotRWR();

    }

    private static void plotRWR() {
        JFreeChart chart = ChartFactory.createScatterPlot( "Precision and Recall: RWR", "Recall", "Precision", outputFluxesAndGraph(), PlotOrientation.VERTICAL, true, true, false );
        Plotter.main( new String[]{""}, chart );
    }

    private static XYSeriesCollection outputFluxesAndGraph() {
        XYSeriesCollection data = new XYSeriesCollection(  );
        for ( PathParser.PathType value : PathParser.PathType.values() ) {
                RWRHandler handler = new RWRHandler( value );
                XYSeries series = new XYSeries( value.getName() );
                for ( int i = 0; i < value.getRwrFluxMappedEdges().size(); i++ ) {
                    double precision = handler.getPrecision(i);
                    double recall = handler.getRecall(i);
                    if ( precision <= 1 && recall <= 1 ) {
                        series.add( recall, precision );
                    }
                }
                data.addSeries( series );
        }
        return data;
    }

    private static void showPrecisionAndRecallShortestPaths() {
        JFreeChart chart = ChartFactory.createScatterPlot( "Precision and Recall: Shortest Paths", "Recall", "Precision", makeShortestPathDataset(), PlotOrientation.VERTICAL, true, true, false );
        Plotter.main( new String[]{""}, chart );

    }

    private static XYDataset makeShortestPathDataset() {
        XYSeriesCollection data = new XYSeriesCollection(  );
        for ( PathParser.PathType value : PathParser.PathType.values() ) {
            XYSeries series = new XYSeries( value.getName() );
            for ( int i = 0; i < value.getDijkstraHandler().getUniqueEdges(); i++ ) {
                double precision = value.getDijkstraHandler().getDijkstraPrecision( i );
                double recall = value.getDijkstraHandler().getDijkstraRecall( i );
                if ( precision <= 1 && recall <= 1 ) {
                    series.add( recall, precision );
                }
            }
            data.addSeries( series );
        }
        return data;
    }

    private static void outputShortestPaths() {
        for ( PathParser.PathType value : PathParser.PathType.values() ) {
            DijkstraHandler handler = new DijkstraHandler( value );
            value.setDijkstraHandler( handler );
        }
    }

    private static void showPrecisionAndRecall() {
        JFreeChart chart = ChartFactory.createScatterPlot( "Precision and Recall", "Recall", "Precision", constructAllGraphs(), PlotOrientation.VERTICAL, true, true, false );
        Plotter.main( new String[]{""}, chart );
    }

    private static XYDataset  constructAllGraphs() {
        graphMap = new HashMap<>(  );
        XYSeriesCollection data = new XYSeriesCollection(  );
        for ( PathParser.PathType pathType : PathParser.PathType.values() ) {
            pathType.setGraph( PathParser.populateGraph( pathType ) );
            pathType.setPathLinkerResult( new PathLinkerResults(pathType ) );
            XYSeries series = new XYSeries( pathType.getName() );
            for ( int i = 0; i < pathType.getPathLinkerResults().getRankedEdges().size(); i++ ) {
                if ( !pathType.getPathLinkerResults().getRankedEdges().get( i ).isEmpty() ) {
                    double precision = pathType.getPathLinkerResults().getPrecision( i );
                    double recall = pathType.getPathLinkerResults().getRecall( i );
                    if ( precision <= 1 && recall <= 1 ) {
                        series.add( recall, precision );

                    }
                }
            }
            data.addSeries( series );

//            pathType.setChart( )
        }
//        chart = ChartFactory.createScatterPlot("Precision and Recall", "Recall", "Precision", data, PlotOrientation.VERTICAL, true, true, false);
//        JFreeChart chart = ChartFactory.createScatterPlot();
        return data;
    }

    private static void setup(PathParser.PathType pathType ) {
        JFrame jFrame = new JFrame( pathType.getName() );
        jFrame.setLayout( new GridLayout(  ) );
        jFrame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        jFrame.setBounds( 0,0,WINDOW_WIDTH, WINDOW_HEIGHT );
        jFrame.setPreferredSize( new Dimension( WINDOW_WIDTH, WINDOW_HEIGHT ) );

        JPanel jPanel = new JPanel(  );
        jPanel.setLayout( new GridLayout(  ) );
        jFrame.add( jPanel );

        Graph g = graphMap.get( pathType );

        Viewer viewer = new Viewer( g, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD );
        viewer.enableAutoLayout();
        ViewPanel view_panel = viewer.addDefaultView(false);
        Rectangle rec = jPanel.getBounds();
        view_panel.setBounds(0, 0, rec.width, rec.height);
        view_panel.setPreferredSize(new Dimension(rec.width, rec.height));
        jPanel.add(view_panel);

        view_panel.addMouseWheelListener( mwe -> GraphDisplay.zoomGraphMouseWheelMoved(mwe, view_panel) );
        jFrame.setVisible( true );

    }

}
