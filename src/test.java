import javax.swing.*;
import javax.tools.Tool;
import java.awt.*;
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

//        PHT pht = new PHT();
//        Graph graph = new Graph();
//        graph.buildShortestPathTree(graph.nodes.get(0), null);
//        graph.nodesInformation();

        UI ui = new UI();

    }

}

class UI extends JFrame {

    UI () {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension panelSize = new Dimension((int) (screenSize.getWidth() / 2), (int) screenSize.getHeight());

        /*
        Section for displaying similar business/clustering functionality
         */
        JPanel similarPanel = new JPanel(new FlowLayout());
        similarPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        similarPanel.setPreferredSize(panelSize);

        // Area for the user to input the business they want to search
        JTextField businessInput = new JTextField();
        businessInput.setPreferredSize(new Dimension(600, 40));
        businessInput.setFont(new Font("Consolas", Font.PLAIN, 25));
        similarPanel.add(businessInput);

        // Button to submit the business that's being searched
        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(200, 40));
        similarPanel.add(searchButton, BorderLayout.SOUTH);

        // Area to display similar businesses and cluster
        JTextArea textArea = new JTextArea("Awaiting Input...");
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(800, 40));
        similarPanel.add(textArea);

        // Scroll area to display all searchable business names
        JTextArea businessNames = new JTextArea(50,72);
        businessNames.setEditable(false);
        //for business name in pht -> businessNames.append(name + "\n");
        JScrollPane scrollPane = new JScrollPane(businessNames);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        similarPanel.add(scrollPane);

        /*
        Section for displaying node and pathfinding functionality
         */
        JPanel nodePanel = new JPanel(new FlowLayout());
        similarPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        similarPanel.setPreferredSize(panelSize);

        // Source node area
        JLabel sourceLabel = new JLabel("Source:");
        sourceLabel.setPreferredSize(new Dimension(200, 40));
        nodePanel.add(sourceLabel);
        
        JTextField sourceInput = new JTextField();
        sourceInput.setPreferredSize(new Dimension(600, 40));
        sourceInput.setFont(new Font("Consolas", Font.PLAIN, 25));
        nodePanel.add(sourceInput);

        // Destination node area
        JLabel destinationLabel = new JLabel("Destination:");
        destinationLabel.setPreferredSize(new Dimension(600, 40));
        nodePanel.add(destinationLabel);

        JTextField destinationInput = new JTextField();
        destinationInput.setPreferredSize(new Dimension(600, 40));
        destinationInput.setFont(new Font("Consolas", Font.PLAIN, 25));
        nodePanel.add(destinationInput);

        // Add both panels to frame
        this.add(similarPanel);
        this.add(nodePanel);

        // Set changes and expand window to fullscreen by default
        this.pack();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
    }

}