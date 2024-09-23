import javax.swing.*;
import java.awt.*;


public class Main{
    public static void main(String[] args){
        JFrame frame = new JFrame("Load and save");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 400);
        frame.setLayout(new FlowLayout());
        frame.setVisible(true);

        JButton save = new JButton("Save");
        JButton load = new JButton("Load");
        JButton add = new JButton("Add");

        TextField text = new TextField(20);
        frame.add(text);
        frame.add(add);
        frame.add(save);
        frame.add(load);
    }
}