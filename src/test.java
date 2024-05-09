import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class test {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

//        int[] ints = {3, 5, 6, 8};
//
//        int replacement = 7;
//        for (int i = 0; i < ints.length; i++) {
//            //Replacement case, else do nothing
//            if (replacement <= ints[i]) {
//                for (int j = ints.length - 1; j > i; j--) {
//                    ints[j] = ints[j - 1];
//                }
//                ints[i] = replacement;
//                break;
//            }
//        }
//
//        System.out.println(Arrays.toString(ints));

//        PHT pht = new PHT();
//        Graph graph = new Graph();
//        graph.getNodes(pht);
//        graph.createEdges(pht);
//        graph.buildMinimumSpanningTree(graph.nodes.get(0), null);

        PHT pht = new PHT();
        Graph graph = new Graph();
        graph.getNodes(pht);
        graph.createEdges();

    }

}
