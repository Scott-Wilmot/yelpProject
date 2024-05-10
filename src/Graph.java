import java.io.*;
import java.util.*;

class Edge implements Comparable<Edge>, Serializable {
    Node src, dst;
    double weight;
    Edge(Node src, Node dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public int compareTo(Edge o) {
        return Double.compare(weight, o.weight);
    }
}

class Node implements Comparable<Node>, Serializable {
    String ID;
    Collection<Edge> edges = new ArrayList<>();
    Node parent;
    double best;
    int pqIndex;
    Node(String ID) {
        this.ID = ID;
    }

    void addEdge(Node src, Node dst) throws IOException, ClassNotFoundException {
        Edge e = new Edge(src, dst);
        edges.add(e);
    }

    boolean containEdge(Node dst) {
        for (Edge e : edges) {
            if (e.dst.ID.equals(dst.ID)) { return true; } // Return true if there is a found match in edges
        }
        return false; // Return false if dst not found in edges
    }

    @Override
    public int compareTo(Node o) {
        return Double.compare(best, o.best);
    }
}

public class Graph {

    Graph () throws IOException, ClassNotFoundException {
        String nodeFile = "NODES.ser";
        if (new File(nodeFile).isFile()) { // If a serialized node file can be found
            nodes = (ArrayList<Node>) new ObjectInputStream(new FileInputStream(nodeFile)).readObject();
        }
        else { // Will create edges and serialize them to a file
            getNodes(new PHT());
            createEdges();
        }
    }

    Business getBusiness(String ID) throws IOException, ClassNotFoundException {
        return (Business) new ObjectInputStream(new FileInputStream("businesses\\" + ID + ".ser")).readObject();
    }

    void nodeNames() throws IOException, ClassNotFoundException {
        for (Node node : nodes) {
            System.out.println(getBusiness(node.ID).name);
        }
    }

    void nodesInformation() throws IOException, ClassNotFoundException {
        for (Node node : nodes) {
            System.out.println(node.ID + ", " + node.best + ", " + getBusiness(node.ID).similarityValue);
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

    // Creates edges for all Nodes in nodes list
    void createEdges() throws IOException, ClassNotFoundException {
        int count = 0;
        // Loop through nodes list to assign edges for every node
        for (Node srcNode : nodes) {
            Node[] closestNodes = new Node[4]; // Arbitrary number of closest neighbors
            Business srcBusiness = getBusiness(srcNode.ID);
            // Loop through nodes again to find closest nodes
            for (Node dstNode : nodes) {
                if (dstNode.ID.equals(srcNode.ID)) { continue; } // Skip this iteration if src and dst node are the same OR the dstNode is already an edge in srcNode
                // Setup dstDiff value to save unnecessary repeat calculations
                Business dstBusiness = getBusiness(dstNode.ID);
                double dstDiff = haversine(srcBusiness, dstBusiness);
                // Now compare dstNode to each closest node to see if a swap is needed
                for (int i = 0; i < closestNodes.length; i++) {
                    // Case: null value in closest nodes index -> fill with dstNode
                    if (closestNodes[i] == null) { closestNodes[i] = dstNode; }
                    Business closeBusiness = getBusiness(closestNodes[i].ID);
                    double closeDiff = haversine(srcBusiness, closeBusiness);
                    if (dstDiff <= closeDiff) {
                        //System.out.println("Inserting: " + dstDiff + " in place of: " + closeDiff);
                        for (int j = closestNodes.length - 1; j > i; j--) {
                            closestNodes[j] = closestNodes[j - 1];;
                        }
                        closestNodes[i] = dstNode;
                        break;
                    }
                }
            }
            for (Node node : closestNodes) {
                System.out.print(haversine(srcBusiness, getBusiness(node.ID)) + ", ");
            }
            // Map closest nodes to edges in the source node
            for (Node dstNode : closestNodes) {
                srcNode.addEdge(srcNode, dstNode); // Creates an edge from src to a closest node
            }
            //break; // Use this to stop after one nodes edges generate
            System.out.println(count);
            count++;
        }
        // Serialize list of nodes so you don't need to wait 5 min for each program execution
        new ObjectOutputStream(new FileOutputStream("NODES.ser")).writeObject(nodes);
    }

    public Hashtable<String, Float> getTF(Node node) throws IOException, ClassNotFoundException {
        //words array stuff
        Business b = getBusiness(node.ID);
        String text = b.review;
        ArrayList<String> words = new ArrayList<>(Arrays.asList(text.split("\\W+")));
        words.replaceAll(String::toLowerCase);  //Set to lowercase to make Target and target the same when analyzed
        float wordCount = words.size();
        //Set up two tables: occurrences and termFrequencies with occurrences -> termFrequencies
        Hashtable<String, Integer> occurrenceTable = new Hashtable<>();   //This will represent our occurrences
        Hashtable<String, Float> termFrequencies = new Hashtable<>();


        while (!words.isEmpty()) {  //Run through words list which accounts for changing array size, maybe implement in getBusinesses
            String word = words.get(0); //This acts as the word to find frequency for
            int occurrences = 0;    //count for occurrences of given word
            for (String s : words) {    //Now cycle through words array and match
                if (s.equals(word)) {   //Just .equals should work due to .replaceAll() being done earlier
                    occurrences++;
                }
            }
            occurrenceTable.put(word, occurrences);   //Place our occurrences in a table...
            words.removeAll(Collections.singleton(word));   //...and subsequently remove given word from words
        }
        //Now get keySet from occurrences and use values to quickly calculate tf
        Set<String> keySet = occurrenceTable.keySet();
        for (String s : keySet) {
            float occurrences = occurrenceTable.get(s);
            float frequency = occurrences / wordCount;
            termFrequencies.put(s, frequency);
        }

        return termFrequencies;
    }

    Hashtable<String, Float> getIDF(Node root) throws IOException, ClassNotFoundException {
        Business rootBusiness = getBusiness(root.ID);
        HashSet<String> wordSet = rootBusiness.getWordSet();

        Hashtable<String, Integer> docOccurrences = new Hashtable<>(); //Variable for representing num of documents a word occurs in
        Hashtable<String, Float> idf_table = new Hashtable<>();  //Will later represent the log(D/t) values

        for (Node node : nodes) { // For each node in the graph
            Business b = getBusiness(node.ID);
            for (String word : wordSet) { // Check if the nodes wordset contains a word from the source businesses wordset
                if (b.getWordSet().contains(word)) {
                    if (!docOccurrences.containsKey(word)) {docOccurrences.put(word, 1);}
                    else {docOccurrences.put(word, docOccurrences.get(word) + 1);}
                }
            }
        }

        for (String s : docOccurrences.keySet()) {
            float occurrences = docOccurrences.get(s);
            float idf = (float) Math.log10(1000 / occurrences);
            idf_table.put(s, idf);
        }
        return idf_table;
    }

    void assignSimilarityValues(Node root) throws IOException, ClassNotFoundException {
        Hashtable<String, Float> idfTable = getIDF(root);
        for (Node node : nodes) { // For every node
            float sum = 0F;
            //Business business = getBusiness(node.ID); // Get nodes correlated business
            Hashtable<String, Float> tfTable = getTF(node);
            for (String word : tfTable.keySet()) { // For each word in the businesses tfTable...
                if (idfTable.containsKey(word)) {sum += idfTable.get(word) * tfTable.get(word);} // If the idfTable contains the word, add tf-idf to sum
            }
            Business b = getBusiness(node.ID);
            b.similarityValue = sum; // Set the businesses similarity value to the sum of all tf-idf
            new ObjectOutputStream(new FileOutputStream("businesses\\" + node.ID + ".ser")).writeObject(b); // Update the business serializable
        }
    }

    void calculateEdgeWeights() throws IOException, ClassNotFoundException {
        for (Node node : nodes) { // For every node in graph
            for (Edge edge : node.edges) { // For every edge in a node
                // Fetch related businesses and make weight the difference in the nodes similarity values
                Business srcBusiness = getBusiness(edge.src.ID);
                Business dstBusiness = getBusiness(edge.dst.ID);
                edge.weight = Math.abs(srcBusiness.similarityValue - dstBusiness.similarityValue);
            }
        }
    }

    void buildShortestPathTree(Node root, Node destination) {
        PQ pq = new PQ(nodes, root);
        Node p;
        while ((p = pq.poll()) != null) { // Continuously pull nodes from PQ until no remaining nodes
            if (p == destination) break;
            for (Edge e : p.edges) { // For all edges of a node
                Node s = e.src, d = e.dst; // Source and Destination nodes
                double w = s.best + e.weight; //
                if (w < d.best) { // If the new weight to dst is lesser than dst's current best, change d's parent and best score
                    d.parent = s;
                    d.best = w;
                    pq.resift(d);
                }
            }
        }
    }

    /*
    This is the method which combines all components of the graph class in order to find the path between source and destination
    This is what the GUI should call when dealing with pathing
     */
    void findPath(Node source, Node destination) throws IOException, ClassNotFoundException {
        assignSimilarityValues(source);
        calculateEdgeWeights();
        buildShortestPathTree(source, destination);
    }

    ArrayList<Node> nodes = new ArrayList<>(); // Maybe change back to a collection later

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
