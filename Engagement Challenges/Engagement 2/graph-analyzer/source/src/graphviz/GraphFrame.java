/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphviz;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * This frame shows the toolbar and the graph.
 */
public class GraphFrame extends JFrame {

    /**
     * Constructs a graph frame that displays a given graph.
     *
     * @param graph the graph to display
     */
    public GraphFrame(final Graph graph) {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.graph = graph;

        constructFrameComponents();
        // set up menus

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openFile();
            }
        });
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                saveFile();
            }
        });
        fileMenu.add(saveItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                panel.removeSelected();
            }
        });

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(deleteItem);
        menuBar.add(editMenu);
    }

    /**
     * Constructs the tool bar and graph panel.
     */
    private void constructFrameComponents() {
        toolBar = new ToolBar(graph);
        panel = new GraphPanel(toolBar, graph);
        scrollPane = new JScrollPane(panel);
        Container contentPane = getContentPane();
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Asks the user to open a graph file.
     */
    private void openFile() {
        // let user select file

        JFileChooser fileChooser = new JFileChooser();
        int r = fileChooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            // open the file that the user selected
            try {
                File file = fileChooser.getSelectedFile();
                ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream(file));
                graph = (Graph) in.readObject();
                in.close();
                Container contentPane = getContentPane();
                contentPane.remove(scrollPane);
                contentPane.remove(toolBar);
                constructFrameComponents();
                validate();
                repaint();
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(null,
                        exception);
            } catch (ClassNotFoundException exception) {
                JOptionPane.showMessageDialog(null,
                        exception);
            }
        }
    }

    /**
     * Saves the current graph in a file.
     */
    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this)
                == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                ObjectOutputStream out = new ObjectOutputStream(
                        new FileOutputStream(file));
                out.writeObject(graph);
                out.close();
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(null,
                        exception);
            }
        }
    }

    private Graph graph;
    private GraphPanel panel;
    private JScrollPane scrollPane;
    private ToolBar toolBar;

    public static final int FRAME_WIDTH = 600;
    public static final int FRAME_HEIGHT = 400;
}
