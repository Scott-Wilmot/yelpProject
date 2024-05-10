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
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension panelSize = new Dimension((int) (screenSize.getWidth() / 2), (int) screenSize.getHeight());

        JPanel similarPanel = new JPanel(new BorderLayout());
        similarPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        similarPanel.setSize(panelSize);



        this.add(similarPanel);
        this.pack();
        this.setVisible(true);
    }

}