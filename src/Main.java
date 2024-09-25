import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


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
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               String command = e.getActionCommand();
               String input = text.getText();

              switch (command){
                  case "Save":
                      try(
                          BufferedWriter bw = new BufferedWriter(new FileWriter("data.txt"))){
                      bw.write(input);

                      }
                        catch (IOException ex){
                            ex.printStackTrace();
                        }

                      break;
                  case "Load":
                        try(BufferedReader br = new BufferedReader(new FileReader("data.txt"))){
                            String line = br.readLine();
                            text.setText(line);
                        }
                        catch (IOException ex){
                            ex.printStackTrace();
                        }
                      break;
                  case "Add":
                      String[] data = input.split(",");
                        int sum = 0;
                        for (String s : data){
                            sum += Integer.parseInt(s);
                        }
                        text.setText(String.valueOf(sum));
                      break;
                  default:
                      break;
              }

            }
        };

        add.addActionListener(actionListener);
        save.addActionListener(actionListener);
        load.addActionListener(actionListener);
    }
}