package graph.layout.adv;

//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 
import graph.Node;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Dialog with a simple graph visualization, displaying graph nodes as circles
 * of specified sizes and colors at specified positions.
 *
 * @author Andreas Noack
 * @version 21.01.2008
 */
public class GraphFrame extends JFrame {

    /**
     * Constructs the dialog.
     *
     * @param nodeToPosition map from each graph node to its position. Each
     * position array must have at least two elements, one for the horizontal
     * position and one for the vertical position.
     * @param nodeToCluster map from each graph node to its cluster, which is
     * used as color (hue) of its representing circle. The diameter of the
     * circle is the square root of the node weight, thus the circle areas are
     * proportional to the node weights.
     */
    public GraphFrame(Map<Node, double[]> nodeToPosition, Map<Node, Integer> nodeToCluster) {
        setTitle("LinLogLayout");
        setSize(getToolkit().getScreenSize().width / 2, getToolkit().getScreenSize().height / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        final GraphCanvas canvas = new GraphCanvas(nodeToPosition, nodeToCluster);
        getContentPane().add(BorderLayout.CENTER, canvas);

        JPanel southPanel = new JPanel(new BorderLayout());
        final JLabel commentLabel = new JLabel(
                "Click right to search. "
                + "Move the mouse cursor over a node to display its name.");
        southPanel.add(BorderLayout.CENTER, commentLabel);
        final JCheckBox labelBox = new JCheckBox("Show all names.", false);
        labelBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                canvas.setLabelEnabled(labelBox.isSelected());
            }
        });
        southPanel.add(BorderLayout.EAST, labelBox);
        getContentPane().add(BorderLayout.SOUTH, southPanel);
    }
}

/**
 * Canvas for a simple graph visualization.
 *
 * @author Andreas Noack
 */
class GraphCanvas extends JComponent {

    /**
     * Map from each node to its position.
     */
    private final Map<Node, double[]> nodeToPosition;
    /**
     * Minimum and maximum positions of the nodes.
     */
    private double minX, maxX, minY, maxY;
    /**
     * Map from each node to its cluster.
     */
    private final Map<Node, Integer> nodeToCluster;
    /**
     * Maximum cluster of the nodes.
     */
    private int maxCluster;
    /**
     * Nodes whose names are displayed.
     */
    private Set<Node> labelledNodes = new HashSet<Node>();
    /**
     * If <code>true</code>, all node names are displayed.
     */
    private boolean labelsEnabled = false;
    /**
     * Node name searched by the user.
     */
    private String searchedName = null;

    /**
     * Constructs the canvas.
     */
    public GraphCanvas(Map<Node, double[]> nodeToPosition, Map<Node, Integer> nodeToCluster) {
        this.nodeToPosition = nodeToPosition;
        this.nodeToCluster = nodeToCluster;

        // determine minimum and maximum positions of the nodes
        minX = Float.MAX_VALUE;
        maxX = -Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        maxY = -Float.MAX_VALUE;
        for (Node node : nodeToPosition.keySet()) {
            double[] position = nodeToPosition.get(node);
            double diameter = Math.sqrt(node.weight);
            minX = Math.min(minX, position[0] - diameter / 2);
            maxX = Math.max(maxX, position[0] + diameter / 2);
            minY = Math.min(minY, position[1] - diameter / 2);
            maxY = Math.max(maxY, position[1] + diameter / 2);
        }

        // determine maximum cluster of the nodes
        maxCluster = 0;
        for (int cluster : nodeToCluster.values()) {
            maxCluster = Math.max(cluster, maxCluster);
        }

        // show name of nodes when the mouse cursor is over them
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                labelledNodes = nodesAt(event.getX(), event.getY());
                repaint();
            }
        });
        // right click opens popup menu
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopup(e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Shows a popup menu at the specified position.
     */
    private void showPopup(int posX, int posY) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Search...");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchedName = JOptionPane.showInputDialog("Search for node:");
                repaint();
            }
        });
        menu.add(item);
        menu.show(this, posX, posY);
    }

    /**
     * Returns the nodes at the specified position.
     */
    private Set<Node> nodesAt(int x, int y) {
        double scale = Math.min(getWidth() / (maxX - minX), getHeight() / (maxY - minY));

        Set<Node> result = new HashSet<Node>();
        for (Node node : nodeToPosition.keySet()) {
            int positionX = (int) Math.round((nodeToPosition.get(node)[0] - minX) * scale);
            int positionY = (int) Math.round((nodeToPosition.get(node)[1] - minY) * scale);
            int diameter = (int) Math.round(Math.sqrt(node.weight) * scale);
            if (x >= positionX - diameter / 2 && x <= positionX + diameter / 2
                    && y >= positionY - diameter / 2 && y <= positionY + diameter / 2) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Show names of all or only mouse-pointed nodes.
     */
    public void setLabelEnabled(boolean labelsEnabled) {
        this.labelsEnabled = labelsEnabled;
        repaint();
    }

    /**
     * Invoked by Swing to draw components.
     */
    public void paint(Graphics g) {
        double scale = Math.min(getWidth() / (maxX - minX), getHeight() / (maxY - minY));

        // draw nodes as circles
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        for (Node node : nodeToPosition.keySet()) {
            float hue = nodeToCluster.get(node) / (float) (maxCluster + 1);
            g.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
            int positionX = (int) Math.round((nodeToPosition.get(node)[0] - minX) * scale);
            int positionY = (int) Math.round((nodeToPosition.get(node)[1] - minY) * scale);
            int diameter = (int) Math.round(Math.sqrt(node.weight) * scale);
            g.fillOval(positionX - diameter / 2, positionY - diameter / 2, diameter, diameter);
        }

        final int FONT_SIZE = 10;
        g.setFont(g.getFont().deriveFont(FONT_SIZE));
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        for (Node node : nodeToPosition.keySet()) {
            if (labelledNodes.contains(node)) {
                continue;
            }
            if (!node.name.equalsIgnoreCase(searchedName) && !labelsEnabled) {
                continue;
            }

            g.setColor(node.name.equalsIgnoreCase(searchedName) ? Color.RED : Color.BLACK);
            int positionX = (int) Math.round((nodeToPosition.get(node)[0] - minX) * scale);
            int positionY = (int) Math.round((nodeToPosition.get(node)[1] - minY) * scale);
            g.drawString(node.name,
                    Math.min(positionX, getWidth() - g.getFontMetrics().stringWidth(node.name)),
                    Math.max(positionY, FONT_SIZE));
        }

        if (!labelledNodes.isEmpty()) {
            Node firstNode = labelledNodes.iterator().next();
            int positionX = (int) Math.round((nodeToPosition.get(firstNode)[0] - minX) * scale);
            int positionY = (int) Math.round((nodeToPosition.get(firstNode)[1] - minY) * scale);
            positionY = Math.max(positionY, FONT_SIZE);
            for (Node node : labelledNodes) {
                g.setColor(node.name.equalsIgnoreCase(searchedName) ? Color.RED : Color.BLACK);
                g.drawString(node.name,
                        Math.min(positionX, getWidth() - g.getFontMetrics().stringWidth(node.name)),
                        positionY);
                positionY += FONT_SIZE;
            }
        }
    }

}
