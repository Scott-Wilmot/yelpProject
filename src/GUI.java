import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame implements ActionListener {

    JButton button;
    JTextField textField;
    JTextArea textArea;
    String businessName;
    boolean inputExists = false;

    GUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());

        button = new JButton("Submit");
        button.addActionListener(this);

        textField = new JTextField();
        textField.setPreferredSize(new Dimension(450, 40));
        textField.setFont(new Font("Consolas", Font.PLAIN, 35));

        textArea = new JTextArea();
        textArea.setText("Waiting for input...");
        textArea.setPreferredSize(new Dimension(450, 40));

        this.add(button);
        this.add(textField);
        this.add(textArea);
        this.pack();
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            String text = textField.getText();
            businessName = text;
            inputExists = true;
        }
    }

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.setVisible(true);
    }

}
