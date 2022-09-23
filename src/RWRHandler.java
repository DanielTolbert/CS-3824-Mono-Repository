import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.AutomaticSimpleMatrixConvert;
import org.ejml.simple.SimpleMatrix;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import scala.Int;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class RWRHandler {

    private PathParser.PathType pathType;
    private double[][] adjacencyMatrix;

    public RWRHandler( PathParser.PathType pathType ) {
        this.pathType = pathType;
        convertGraphToAdjacencyMatrix();
        outputFlux();
    }

    private void convertGraphToAdjacencyMatrix() {
        Graph g = pathType.getGraph();
        int dimension = g.getNodeSet().size();
        adjacencyMatrix = new double[dimension][dimension];

        for ( int i = 0; i < dimension; i++ ) {
            for ( int i1 = 0; i1 < dimension; i1++ ) {
                adjacencyMatrix[i][i1] = g.getNode( i ).hasEdgeBetween( i1 ) ? 1 : 0;
                System.out.println(adjacencyMatrix[i][i1]);
            }
        }
    }

    private double[][] computeDiagonalMatrix() {
        double[][] diagonalMatrix = new double[adjacencyMatrix.length][adjacencyMatrix.length];
        for ( int i = 0; i < diagonalMatrix.length; i++ ) {
            int rowSum = 1;
            for ( int i1 = 0; i1 < diagonalMatrix.length; i1++ ) {
                diagonalMatrix[i][i1] = 0;
                if ( ! (i1 == i) ) rowSum += adjacencyMatrix[i][i1];
            }
            diagonalMatrix[i][i] = rowSum;
//            System.out.println(diagonalMatrix[i][i]);
        }
        return diagonalMatrix;
    }

    public SimpleMatrix computeInverseMultiply() {
        SimpleMatrix A = new SimpleMatrix( adjacencyMatrix );
        SimpleMatrix D = new SimpleMatrix( computeDiagonalMatrix() );
        System.out.println( pathType.getName() );
        return (D.invert()
                .mult( A )).transpose();
    }

    public double[][] computeSVector() {
        double[][] sVector = new double[adjacencyMatrix.length][1];
        for ( int i = 0; i < sVector.length; i++ ) {
            int finalI = i;
            if ( pathType.getSinkNodes().stream().map( Node::getNodeSymbol ).anyMatch( e -> e.equals( pathType.getGraph().getNode( finalI ).getId() ) )) {
                sVector[i][0] = 1d / pathType.getSinkNodes().size();
            } else {
                sVector[i][0] = 0;
            }
        }
        return sVector;
    }

    public SimpleMatrix computePVector() {
        double q = 0.5;
        SimpleMatrix I = SimpleMatrix.identity( adjacencyMatrix.length );
        SimpleMatrix s = new SimpleMatrix( computeSVector() );
        return (I.minus((computeInverseMultiply().transpose()).scale( 1 - q ))).invert().mult( s.scale( q ) );
    }

    public void outputFlux() {
        File file = new File( String.format( "RWR Results\\%s edge fluxes", pathType.getName() ) );
        //calculate fluxes and sort
        ArrayList<org.graphstream.graph.Edge> edges = new ArrayList<>( pathType.getGraph().getEdgeSet() );
        edges = edges.stream().sorted( new Comparator<>() {
            @Override
            public int compare( Edge o1, Edge o2 ) {
                return ( int ) ( calculateFlux( o1 ) - calculateFlux( o2 ) );
            }
        } ).collect( Collectors.toCollection(ArrayList::new));
        try {
            if ( file.exists() ) file.delete();
            file.createNewFile();
            FileWriter fileWriter = new FileWriter( file );
            fileWriter.write( "tail\thead\tedge flux" );
            for ( Edge edge : edges ) {
                fileWriter.write( String.format( "%s\t%s\t%s\n", edge.getSourceNode(), edge.getTargetNode(), calculateFlux( edge ) ) );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private double calculateFlux(org.graphstream.graph.Edge edge) {
        SimpleMatrix p = computePVector();
        int weight = ( Integer ) edge.getSourceNode().getAttribute( "weight" );
        int u = pathType.getGraph().getNode( edge.getSourceNode() ).getIndex();
        int v = pathType.getGraph().getNode( edge.getTargetNode() ).getIndex();
        int outDegree = pathType.getGraph().getNode( u ).getOutDegree();
        double pu = p.get( u );
        return (pu * weight) / (outDegree);
    }


}
