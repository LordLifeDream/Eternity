package de.lldgames.eternity;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcessOutputViewer extends JFrame {
    JPanel list;
    Process process;
    public ProcessOutputViewer() {
        setTitle("Process Output");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void setupInput(){
        JTextField field = new JTextField();
        field.setToolTipText("process input");
        field.addActionListener((e)->{
            try {
                String text = field.getText();
                process.getOutputStream().write(text.getBytes());
                appendColored(">> "+text, Color.BLUE);
                field.setText("");
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
        this.add(field, BorderLayout.SOUTH);
        this.revalidate();
    }

    public void displayApp(App app){
        this.displayProcessOutput(app.process);
        this.setTitle("Process output: " + app.name);
        this.setupInput();
    }

    private void onMessage(String msg){
        SwingUtilities.invokeLater(() -> {
            appendColored(msg + "\n", Color.BLACK);
        });
    }
    private void onError(String err){
        SwingUtilities.invokeLater(() -> {
            appendColored("[ERROR] " + err + "\n", Color.RED);
        });
    }

    private void appendColored(String msg, Color c) {
        if(list.getComponents().length>100) list.remove(0);
        JPanel masterP = new JPanel();
        masterP.setLayout(new BoxLayout(masterP, BoxLayout.X_AXIS));
        masterP.setAlignmentX(0);
        JLabel label = new JLabel(msg);
        label.setForeground(c);
        JLabel timeLabel = new JLabel(LocalDateTime.now().toString());
        timeLabel.setForeground(Color.GRAY);
        masterP.add(timeLabel);
        JLabel seperator = new JLabel(" | ");
        masterP.add(seperator);
        masterP.add(label);
        list.add(masterP);
        list.revalidate();
    }

    public void displayProcessOutput(Process process) {
        this.process = process;
        new Thread(() -> {
            try (InputStream inputStream = process.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String finalLine = line;
                    this.onMessage(finalLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try (InputStream errorStream = process.getErrorStream();
                 InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
                 BufferedReader bufferedReader = new BufferedReader(errorStreamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String finalLine = line;
                    this.onError(finalLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
