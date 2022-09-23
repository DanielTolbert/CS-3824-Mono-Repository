import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import scala.collection.parallel.ParIterableLike;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class DijkstraHandler {

    private int uniqueEdges = 0;
    private HashSet<Edge> uniqueRankedEdges;

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
            Dijkstra dijkstra = new Dijkstra( Dijkstra.Element.EDGE, null, "weight");
            dijkstra.init( current );
            int rank = 1;
            for ( Node sourceNode : value.getSourceNodes() ) {
                for ( Node sinkNode : value.getSinkNodes() ) {
                    dijkstra.setSource( current.getNode( sinkNode.getNodeSymbol() ) );
                    dijkstra.compute();
                    double length = dijkstra.getPathLength( current.getNode(sourceNode.getNodeSymbol()) );
                    StringBuilder sb = new StringBuilder(  );
                    sb.append( String.format( "%s\t%s ", rank, length ) );
                    Collection<org.graphstream.graph.Node> l = dijkstra.getPath( current.getNode( sourceNode.getNodeSymbol() ) ).getNodeSet();
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
        uniqueRankedEdges = new HashSet<>(  );
        for ( GraphPath shortestPath : value.getShortestPaths() ) {
            uniqueRankedEdges.addAll( shortestPath.getEdges() );
        }
        uniqueEdges = uniqueRankedEdges.size();
        //file creation
        try {
            File rankedEdgesFile = new File( String.format("DijkstraResults\\%s Ranked Edges.txt", value.getName()) );
            if ( rankedEdgesFile.exists() ) rankedEdgesFile.delete();
            rankedEdgesFile.createNewFile();
            FileWriter fileWriter = new FileWriter( rankedEdgesFile );
            fileWriter.write( String.format( "#tail\thead\tKSP index\n" ) );
            int rank = 1;
            for ( Edge uniqueRankedEdge : uniqueRankedEdges ) {
                fileWriter.write( String.format( "%s\t%s\t%s\n", uniqueRankedEdge.tailID, uniqueRankedEdge.headID, rank ) );
                rank++;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    public void computeDijkstraPrecisionAndRecall(  ) {
        recall = new Double[uniqueRankedEdges.size()];
        precision = new Double[uniqueRankedEdges.size()];
        int truePositives = 0;
        ArrayList<Edge> rankedEdgesAsArray = new ArrayList<>( uniqueRankedEdges );
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
