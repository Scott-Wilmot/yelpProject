import java.io.*;
import java.util.*;

class Edge implements Comparable<Edge> {
    Node src, dst;
    double weight;
    Edge(Node src, Node dst) throws IOException, ClassNotFoundException {
        this.src = src;
        this.dst = dst;
        //Fetch serialized businesses based off of Node ID strings to calculate the weight
        Business srcBusiness = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + src.ID + ".ser")).readObject();
        Business dstBusiness = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + dst.ID + ".ser")).readObject();
        this.weight = Math.abs(srcBusiness.similarityValue - dstBusiness.similarityValue); // Weight is determined by difference between similarity values
    }

    @Override
    public int compareTo(Edge o) {
        return 0;
    }
}

class Node implements Comparable<Node> {
    String ID;
    Collection<Edge> edges;
    Node parent;
    double best;
    int pqIndex;
    Node(String ID) {
        this.ID = ID;
    }

    @Override
    public int compareTo(Node o) {
        return 0;
    }
}

public class Graph implements Serializable {

//    static class Node {
//        String ID;
//        Edge[] edges = new Edge[4];
//        Node(String ID) {
//            this.ID = ID;
//        }
//    }

//    class Edge {
//        Node src, dst;
//        float weight;
//        Edge(Node src, Node dst) {
//            this.src = src;
//            this.dst = dst;
//            weight = 0;
//        }
//    }

    Business getBusiness(String ID) throws IOException, ClassNotFoundException {
        return (Business) new ObjectInputStream(new FileInputStream("businesses\\" + ID + ".ser")).readObject();
    }

    void printAllNodes() {
        for (Node node : nodes) {
            System.out.println(node.ID);
        }
    }

    // Calculated the distance between two businesses on a globe, the lesser the value the closer (duh)
    double haversine(Business b1, Business b2) {
        double lat1 = Math.toRadians(b1.latitude);
        double lat2 = Math.toRadians(b2.latitude);
        double dLat = Math.toRadians(b1.latitude - b2.latitude);
        double dLon = Math.toRadians(b1.longitude - b2.longitude);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double d = 2 * 6371 * Math.asin(Math.sqrt(a)); // Uses 6371 to represent earths radius in km, may not be necessary

        return d;
    }

    // This puts every 10th node from PHT into the nodes list
    // This should be used for Graph initialization when there's no serialized edges
    void getNodes(PHT pht) throws IOException, ClassNotFoundException {
        int count = 0;
        for (long l : pht.indexArray.index) {
            Bucket bucket = pht.getBucket(l);
            for (String key : bucket.keys) {
                if (key == null) break;
                if (count % 10 == 0) {
                    nodes.add(new Node(bucket.get(key)));
                }
                count++;
            }
        }
    }

    // Use this to initialize each nodes closest neighbors
    void createEdges(PHT pht) throws IOException, ClassNotFoundException {
        int count = 0;
        for (Node node : nodes) { // For each node...
            Business nodeBusiness = getBusiness(node.ID);
            String[] businessIDs = new String[4];

            for (long l : pht.indexArray.index) { // For each bucket in PHT...
                Bucket bucket = pht.getBucket(l);

                for (String key : bucket.keys) { // For each business in Bucket...
                    if (key == null) break; //You've reached the end of the list...next bucket
                    Business business = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + bucket.get(key) + ".ser")).readObject();

                    // Now compare to the existing edges to check if business is closer
                    for (int i = 0; i < businessIDs.length; i++) { // For each edge in edges
                        // Case: Business-nodeBusiness match -> skip iteration
                        if (business.ID.equals(nodeBusiness.ID)) {break;}
                        // Case: Auto populate on null entry
                        if (businessIDs[i] == null) {
                            businessIDs[i] = business.ID;
                            break;
                        }
                        // Calculating distances between node and the two points that ae competeing (use haversine)
                        double node_existing_diff = haversine(nodeBusiness, getBusiness(businessIDs[i])); // Difference between an established similar node and source node
                        double node_new_diff = haversine(nodeBusiness, business); // Difference between new testing node and source node
                        if (node_new_diff <= node_existing_diff) {
                            for (int j = businessIDs.length - 1; j > i; j--) {
                                businessIDs[j] = businessIDs[j - 1];
                            }
                            businessIDs[i] = business.ID;
                            break;
                        }
                    }
                }
            }
            // Cast BusinessIDs to Edge here?
            System.out.println(count);
            count++;
        }

    }

    void buildMinimumSpanningTree(Node root, Node destination) {
        PQ pq = new PQ(nodes, root);
        Node p;
        while ((p = pq.poll()) != null) {
            //if (p == destination) break;
            for (Edge e : p.edges) {
                Node s = e.src, d = e.dst;
                double w = s.best + e.weight;
                if (w < d.best) {
                    d.parent = s;
                    d.best = w;
                    pq.resift(d);
                }
            }
        }
    }


    ArrayList<Node> nodes = new ArrayList<>(); // Maybe change back to a collection later
    ArrayList<Edge> edges = new ArrayList<>();

}

// Priority Queue class for Dijkstras implementation
class PQ {
    final Node[] array;
    int size;
    static int leftOf(int k) { return (k << 1) + 1; }
    static int rightOf(int k) { return leftOf(k) + 1; }
    static int parentOf(int k) { return (k - 1) >>> 1; }
    PQ(Collection<Node> nodes, Node root) {
        array = new Node[nodes.size()];
        root.best = 0;
        root.pqIndex = 0;
        array[0] = root;
        int k = 1;
        for (Node p : nodes) {
            p.parent = null;
            if (p != root) {
                p.best = Double.MAX_VALUE; // Initializes the Nodes total path cost to infinity
                array[k] = p; p.pqIndex = k++; // Places Node into the PQ and assigns an index to the Node for tracking
            }
        }
        size = k;
    }
    void resift(Node x) {
        int k = x.pqIndex;
        assert (array[k] == x);
        while (k > 0) {
            int parent = parentOf(k);
            Node p = array[parent];
            if (x.compareTo(p) >= 0)
                break;
            array[k] = p; p.pqIndex = k; // Swaps parent of node x into its place
            k = parent;
        }
        array[k] = x; x.pqIndex = k; // Then puts x into its parents place
    }
    Node poll() {
        int n = size;
        if (n == 0) return null;
        Node least = array[0];
        if(least.best == Double.MAX_VALUE) return null;
        size = --n;
        if (n > 0) {
            Node x = array[n]; array[n] = null;
            int k = 0, child;  // while at least a left child
            while ((child = leftOf(k)) < n) {
                Node c = array[child];
                int right = child + 1;
                if (right < n) {
                    Node r = array[right];
                    if (c.compareTo(r) > 0) {
                        c = r;
                        child = right;
                    }
                }
                if (x.compareTo(c) <= 0) {break;}
                array[k] = c; c.pqIndex = k;
                k = child;
            }
            array[k] = x; x.pqIndex = k;
        }
        return least;
    }
}
