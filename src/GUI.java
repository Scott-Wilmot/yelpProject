import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

class GUI extends JFrame implements ActionListener {

    // UI components
    JPanel mainPanel;
    JTextField businessInput;
    JButton searchButton;
    JTextArea textArea;
    JTextArea businessNames;
    JScrollPane scrollPane;
    JLabel sourceLbl;
    JTextField sourceInput;
    JLabel destinationLbl;
    JTextField destinationInput;
    JButton pathButton;
    JTextArea pathDisplay;
    JTextArea nodeNames;
    JScrollPane nodeScrollPane;
    JLabel setsLabel;
    JTextArea setsDisplay;

    // Data Structures
    PHT pht;
    Graph graph;

    GUI () throws IOException, ClassNotFoundException {
        initializeDataStructs();
        initializeUI();
        populateBusinessNames(); populateNodeNames(); setDisjointSetsText();
        graph.printDisjointSet(graph.getNode("z9wCTHYI2VZy9YIblwSsgg"));
    }

    void initializeUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension panelSize = new Dimension((int) (screenSize.getWidth() / 2), (int) screenSize.getHeight());

        /*
        Section for displaying similar business/clustering functionality
         */
        mainPanel = new JPanel(new FlowLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        mainPanel.setPreferredSize(panelSize);

        // Area for the user to input the business they want to search
        businessInput = new JTextField();
        businessInput.setPreferredSize(new Dimension(800, 40));
        businessInput.setFont(new Font("Consolas", Font.PLAIN, 25));
        mainPanel.add(businessInput);

        // Button to submit the business that's being searched
        searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(200, 40));
        searchButton.addActionListener(this);
        mainPanel.add(searchButton, BorderLayout.SOUTH);

        // Area to display similar businesses and cluster
        textArea = new JTextArea("Awaiting Input...");
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(1000, 40));
        mainPanel.add(textArea);

        // Scroll area to display all searchable business names
        businessNames = new JTextArea(20,120);
        businessNames.setEditable(false);
        //for business name in pht -> businessNames.append(name + "\n");
        scrollPane = new JScrollPane(businessNames);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane);

        // Label and input box for the source node
        sourceLbl = new JLabel("Source:");
        sourceLbl.setPreferredSize(new Dimension(200, 40));
        mainPanel.add(sourceLbl);

        sourceInput = new JTextField();
        sourceInput.setPreferredSize(new Dimension(1200, 40));
        mainPanel.add(sourceInput);

        // Label and input box for the destination node
        destinationLbl = new JLabel("Destination:");
        destinationLbl.setPreferredSize(new Dimension(200, 40));
        mainPanel.add(destinationLbl);

        destinationInput = new JTextField();
        destinationInput.setPreferredSize(new Dimension(1200, 40));
        mainPanel.add(destinationInput);

        // Path display label and box
        pathButton = new JButton("Get Path");
        pathButton.setPreferredSize(new Dimension(200, 40));
        pathButton.addActionListener(this);
        mainPanel.add(pathButton);

        pathDisplay = new JTextArea("Waiting for input...");
        pathDisplay.setEditable(false);
        pathDisplay.setPreferredSize(new Dimension(1200, 40));
        mainPanel.add(pathDisplay);

        // Scroll area to display node names
        nodeNames = new JTextArea(15,120);
        nodeNames.setEditable(false);
        //for node in graph -> display name,ID pair;
        nodeScrollPane = new JScrollPane(nodeNames);
        nodeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        nodeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(nodeScrollPane);

        // Representation for disjoint sets count
        setsLabel = new JLabel("Disjoint Sets:");
        setsLabel.setPreferredSize(new Dimension(200, 40));
        mainPanel.add(setsLabel);

        setsDisplay = new JTextArea();
        setsDisplay.setEditable(false);
        setsDisplay.setFont(new Font("Consolas", Font.PLAIN, 25));
        setsDisplay.setPreferredSize(new Dimension(200, 40));
        mainPanel.add(setsDisplay);

        // Add panel to frame
        this.add(mainPanel);

        // Set changes and expand window to fullscreen by default
        this.pack();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
    }

    void initializeDataStructs() throws IOException, ClassNotFoundException {
        pht = new PHT();
        graph = new Graph();
    }

    void populateBusinessNames() throws IOException, ClassNotFoundException {
        for (long l : pht.indexArray.index) {
            Bucket bucket = pht.getBucket(l);
            for (String key : bucket.keys) {
                if (key == null) { break; }
                Business b = (Business) new ObjectInputStream(new FileInputStream("businesses\\" + bucket.get(key) + ".ser")).readObject();
                businessNames.append(b.name + "\n");
            }
        }
    }

    void populateNodeNames() throws IOException, ClassNotFoundException {
        for (Node n : graph.nodes) {
            Business b = graph.getBusiness(n.ID);
            nodeNames.append(b.name + ", " + n.ID + "\n");
        }
    }

    void setDisjointSetsText() {
        setsDisplay.append(Integer.toString(graph.disjointSetCount));
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pathButton) { // What to do on path button click
            String srcText = sourceInput.getText();
            String dstText = destinationInput.getText();
            System.out.println("Source txt: " + srcText + ", dest txt: " + dstText);
            Node srcNode = graph.getNode(srcText);
            Node dstNode = graph.getNode(dstText);
            try {
                String pathText = graph.findPath(srcNode, dstNode);
                if (!(pathText == null)) { // If graph could find a path
                    pathDisplay.setText(pathText);
                }
                else { // No path case
                    pathDisplay.setText("No Path between nodes");
                }
            } catch (IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        else if (e.getSource() == searchButton) {
            String businessName = businessInput.getText();
            String businessPath;
            try {
                businessPath = "businesses\\" + pht.get(businessName) + ".ser";
            } catch (IOException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            Business inputBusiness;
            Hashtable<String, Float> idfTable = new Hashtable<>();

            if (new File(businessPath).isFile()) { // If the user gives a valid business name
                try {
                    Business business = (Business) new ObjectInputStream(new FileInputStream(businessPath)).readObject();
                    idfTable = recommendationSystem.getIDF(pht, business);
                    recommendationSystem.getSimilarityTables(pht, idfTable);

                    // Grouping
                    Group inputGroup = null;
                    ArrayList<Group> groups = recommendationSystem.clusterSimilarityValues(pht);
                    for (Group g : groups) {
                        if (g.contains(business.name)) {
                            inputGroup = g;
                            break;
                        }
                    }

                    ArrayList<Business> simBusinesses = recommendationSystem.mostSimilarBusinesses(business, pht);
                    String bothBusinesses = simBusinesses.get(0).name + ", " + simBusinesses.get(1).name;

                    textArea.setText("Similar Businesses: " + bothBusinesses + "\nCluster: " + inputGroup.center.name);

                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else { // If the user inputs a non-existent business
                textArea.setText("Bad business input");
            }

        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        GUI gui = new GUI();
    }

}
