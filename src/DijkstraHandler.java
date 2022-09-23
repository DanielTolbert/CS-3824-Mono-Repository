import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class DijkstraHandler {

    private int uniqueEdges = 0;
    private HashSet<EdgeWrapper> uniqueRankedEdgeWrappers;

    private Double[] precision;
    private Double[] recall;

    private PathParser.PathType value;

    public DijkstraHandler( PathParser.PathType value) {
        this.value = value;
        outputShortestPaths();
        createRankedEdges();
        computeDijkstraPrecisionAndRecall();
    }

    public void outputShortestPaths() {
        File file = new File( String.format( "DijkstraResults\\%s Shortest Paths.txt", value.getName() ) );
        if ( file.exists() ) file.delete();
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter( file );
            fileWriter.write( "#KSP\tpath_length path\n" );
            Graph current = value.getGraph();
            Dijkstra dijkstra = new Dijkstra( Dijkstra.Element.EDGE, "length", "weight");
            dijkstra.init( current );
            int rank = 1;
            for ( Node sourceNode : value.getSourceNodes() ) {
                dijkstra.setSource( current.getNode( sourceNode.getNodeSymbol() ) );
                dijkstra.compute();
                for ( Node sinkNode : value.getSinkNodes() ) {
                    double length = dijkstra.getPathLength( current.getNode(sinkNode.getNodeSymbol()) );
                    StringBuilder sb = new StringBuilder(  );
                    sb.append( String.format( "%s\t%s ", rank, length ) );
                    Collection<org.graphstream.graph.Node> l = dijkstra.getPath( current.getNode( sinkNode.getNodeSymbol() ) ).getNodeSet();
                    value.getShortestPaths().add( new GraphPath( l ) );
                    if ( !l.isEmpty() ) {
                        for ( org.graphstream.graph.Node node : l ) {
                            sb.append( node.getId() ).append( "|" );
                        }
                        sb.deleteCharAt( sb.length() - 1 ).append( "\n" );
                        fileWriter.write( sb.toString() );
                        rank++;
                    }
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void createRankedEdges(  ) {
        uniqueRankedEdgeWrappers = new HashSet<>(  );
        for ( GraphPath shortestPath : value.getShortestPaths() ) {
            uniqueRankedEdgeWrappers.addAll( shortestPath.getEdgeWrappers() );
        }
        uniqueEdges = uniqueRankedEdgeWrappers.size();
        //file creation
        try {
            File rankedEdgesFile = new File( String.format("DijkstraResults\\%s Ranked Edges.txt", value.getName()) );
            if ( rankedEdgesFile.exists() ) rankedEdgesFile.delete();
            rankedEdgesFile.createNewFile();
            FileWriter fileWriter = new FileWriter( rankedEdgesFile );
            fileWriter.write( String.format( "#tail\thead\tKSP index\n" ) );
            int rank = 1;
            for ( EdgeWrapper uniqueRankedEdgeWrapper : uniqueRankedEdgeWrappers ) {
                fileWriter.write( String.format( "%s\t%s\t%s\n", uniqueRankedEdgeWrapper.tailID, uniqueRankedEdgeWrapper.headID, rank ) );
                rank++;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    public void computeDijkstraPrecisionAndRecall(  ) {
        recall = new Double[uniqueRankedEdgeWrappers.size()];
        precision = new Double[uniqueRankedEdgeWrappers.size()];
        int truePositives = 0;
        ArrayList<EdgeWrapper> rankedEdgesAsArray = new ArrayList<>( uniqueRankedEdgeWrappers );
        for ( int i = 0; i < value.getShortestPaths().size() && i < rankedEdgesAsArray.size(); i++ ) {
            if ( value.getShortestPaths().get( i ).containsEdge( rankedEdgesAsArray.get( i ) ) ) {
                truePositives++;
            }
            recall[i] = ((double)truePositives) / uniqueEdges;
            precision[i] = ((double)truePositives) / i;
        }
    }

    public double getDijkstraPrecision(int rank) {
        return precision[rank] == null ? Double.NaN : precision[rank];
    }
    public double getDijkstraRecall(int rank) {
        return recall[rank] == null ? Double.NaN : recall[rank];
    }

    public int getUniqueEdges() {
        return uniqueEdges;
    }
}
