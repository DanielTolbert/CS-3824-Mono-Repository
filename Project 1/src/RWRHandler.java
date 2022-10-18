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
import java.util.Scanner;
import java.util.stream.Collectors;

public class RWRHandler {

    private PathParser.PathType pathType;
    private double[][] adjacencyMatrix;
    private double[] precision;
    private double[] recall;

    public RWRHandler( PathParser.PathType pathType ) {
        this.pathType = pathType;
        convertGraphToAdjacencyMatrix();
        readFluxFromFiles();
        computeRWRPrecisionAndRecall();
    }

    private void computeRWRPrecisionAndRecall() {
        precision = new double[pathType.getRwrFluxMappedEdges().size()];
        recall = new double[pathType.getRwrFluxMappedEdges().size()];
        int truePositives = 0;
        int i = 0;
        for ( EdgeWrapper rwrFluxMappedEdge : pathType.getRwrFluxMappedEdges() ) {
            if ( i >= pathType.getRwrFluxMappedEdges().size() ) break;
            Edge current  = pathType.getGraph().getEdge( String.format( "%s%s", rwrFluxMappedEdge.getTailID(), rwrFluxMappedEdge.getHeadID() ));
            if ( current != null ) {
                truePositives++;
            }
            recall[i] = ((double)truePositives)/pathType.getRwrFluxMappedEdges().size();
            precision[i] = ((double)truePositives)/i;
            i++;
        }
    }

    public double getPrecision(int idx) {
        return precision[idx];
    }

    public double getRecall(int idx) {
        return recall[idx];
    }

    private void readFluxFromFiles() {

        File file = new File( String.format( "RWRResults/%s edge fluxes.txt", pathType.getName() ) );
        ArrayList<EdgeWrapper> fluxMap = new ArrayList<>(  );
        try {
            Scanner scanner = new Scanner( file );
            String header = scanner.nextLine();
            while ( scanner.hasNext() ) {
                String[] info = scanner.nextLine().split( "\\s+" );
                EdgeWrapper edgeWrapper = new EdgeWrapper( info[0], info[1] );
                edgeWrapper.setFlux( Double.parseDouble(info[2]) );
                fluxMap.add( edgeWrapper );
            }

        } catch ( Exception e ) {
            e.printStackTrace();
        }
        pathType.setRwrFluxMappedEdges( fluxMap );
    }

    private void convertGraphToAdjacencyMatrix() {
        Graph g = pathType.getGraph();
        int dimension = g.getNodeSet().size();
        adjacencyMatrix = new double[dimension][dimension];

        for ( int i = 0; i < dimension; i++ ) {
            for ( int i1 = 0; i1 < dimension; i1++ ) {
                adjacencyMatrix[i][i1] = 0;
            }
        }
        for ( Edge edge : g.getEdgeSet() ) {
            int sourceIdx = edge.getSourceNode().getIndex();
            int targetIndex = edge.getTargetNode().getIndex();
            adjacencyMatrix[sourceIdx][targetIndex] = 1;
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
            if ( rowSum == 2 ) rowSum--;
            diagonalMatrix[i][i] = rowSum;
//            System.out.println(diagonalMatrix[i][i]);
        }
        return diagonalMatrix;
    }

    public SimpleMatrix computeInverseMultiply() {
        SimpleMatrix A = new SimpleMatrix( adjacencyMatrix );
        SimpleMatrix D = new SimpleMatrix( computeDiagonalMatrix() );
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
        System.out.println("Begin " + pathType.getName());
        File file = new File( String.format( "RWRResults\\%s edge fluxes.txt", pathType.getName() ) );
        //calculate fluxes and sort
        ArrayList<EdgeWrapper> edges = new ArrayList<>(  );
        int toCalc = pathType.getGraph().getEdgeSet().size();
        for ( Edge edge : pathType.getGraph().getEdgeSet() ) {
//            System.out.println(toCalc + " left...");
            edges.add( new EdgeWrapper( edge ) );
            edges.get( edges.size() - 1 ).setFlux( calculateFlux( edges.get( edges.size() - 1 ) ) );
            toCalc--;
        }

        edges = edges.stream().sorted( new Comparator<>() {
            @Override
            public int compare( EdgeWrapper o1, EdgeWrapper o2 ) {
//                return ( int ) ( o1.getFlux() - o2.getFlux() );
                return Double.compare( o1.getFlux(), o2.getFlux() );
            }
        } ).collect( Collectors.toCollection(ArrayList::new));
        try {
            if ( file.exists() ) file.delete();
            file.createNewFile();
            FileWriter fileWriter = new FileWriter( file );
            fileWriter.write( "tail\thead\tedge flux\n" );
            for ( EdgeWrapper edge : edges ) {
                fileWriter.write( String.format( "%s\t%s\t%s\n", edge.getActualEdge().getSourceNode(), edge.getActualEdge().getTargetNode(), edge.getFlux() ) );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        System.out.println("Finished " + pathType.getName());
    }

    private double calculateFlux(EdgeWrapper edge) {
        SimpleMatrix p = computePVector();
        int weight = 1;// ( Integer ) edge.getSourceNode().getAttribute( "weight" );
        int u = edge.getActualEdge().getSourceNode().getIndex();
        int v = edge.getActualEdge().getTargetNode().getIndex();
        int outDegree = pathType.getGraph().getNode( u ).getOutDegree();
        double pu = p.get( u );
        double flux = (pu * weight) / (outDegree);
        edge.setFlux( flux );
        return flux;
    }


}
