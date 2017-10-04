package com.cyberpointllc.stac.textcrunchr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

public class WindowOutputHandler extends OutputHandler {

    private JTextArea textArea = new  JTextArea();

    public void do_conclude() {
        do_concludeHelper();
    }

    private void createAndShowGUI() {
        createAndShowGUIHelper();
    }

    private JComponent tCPanel() {
        JComponent panel = new  JPanel(new  BorderLayout());
        final JList<Object> fileList = new  JList<Object>(sortedFiles.toArray());
        // Pull up the text when we click on a filename
        MouseListener mouseListener = new  MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                String selected = (String) fileList.getSelectedValue();
                StringBuilder outputString = new  StringBuilder();
                String path = namesToPaths.get(selected);
                List<TCResult> sampleResults = results.get(path);
                for (TCResult result : sampleResults) {
                    outputString.append(result.getName() + ":\n" + result.getValue() + "\n\n");
                }
                //Clear the current text
                textArea.setText(null);
                //set the new text
                textArea.setText(outputString.toString());
                //Scroll to the top
                textArea.setCaretPosition(0);
            }
        };
        fileList.addMouseListener(mouseListener);
        // Only allow one file to be selected at a time
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Set up the text window
        textArea.setEditable(false);
        JScrollPane scroll = new  JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        // May need to scroll if we have a lot of files
        JScrollPane fileListScroll = new  JScrollPane(fileList);
        panel.add(fileListScroll, BorderLayout.LINE_START);
        panel.add(scroll, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new  Dimension(800, 500));
        return panel;
    }

    private void do_concludeHelper() {
        // Do this for thread safety, just in case
        javax.swing.SwingUtilities.invokeLater(new  Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    private void createAndShowGUIHelper() {
        JFrame frame = new  JFrame("TextCrunchr Output");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JComponent panel = tCPanel();
        panel.setOpaque(true);
        frame.setContentPane(panel);
        frame.pack();
        frame.setPreferredSize(new  Dimension(800, 500));
        frame.setVisible(true);
    }
}
