/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.image;

import graph.Edge;
import graph.Node;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import org.tigris.gef.base.CmdAdjustGrid;
import org.tigris.gef.base.CmdAdjustGuide;
import org.tigris.gef.base.CmdAdjustPageBreaks;
import org.tigris.gef.base.CmdAlign;
import org.tigris.gef.base.CmdCopy;
import org.tigris.gef.base.CmdDeleteFromModel;
import org.tigris.gef.base.CmdDisplayGUI;
import org.tigris.gef.base.CmdDistribute;
import org.tigris.gef.base.CmdExit;
import org.tigris.gef.base.CmdGroup;
import org.tigris.gef.base.CmdOpen;
import org.tigris.gef.base.CmdPaste;
import org.tigris.gef.base.CmdPrint;
import org.tigris.gef.base.CmdReorder;
import org.tigris.gef.base.CmdSave;
import org.tigris.gef.base.CmdSavePNG;
import org.tigris.gef.base.CmdSavePS;
import org.tigris.gef.base.CmdSelectInvert;
import org.tigris.gef.base.CmdSelectNext;
import org.tigris.gef.base.CmdShowProperties;
import org.tigris.gef.base.CmdUngroup;
import org.tigris.gef.base.CmdUseReshape;
import org.tigris.gef.base.CmdUseResize;
import org.tigris.gef.base.CmdUseRotate;
import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Globals;
import org.tigris.gef.base.Layer;
import org.tigris.gef.base.LayerDiagram;
import org.tigris.gef.base.ModeSelect;
import org.tigris.gef.event.ModeChangeEvent;
import org.tigris.gef.event.ModeChangeListener;
import org.tigris.gef.graph.GraphEdgeRenderer;
import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.GraphNodeRenderer;
import org.tigris.gef.graph.presentation.DefaultGraphModel;
import org.tigris.gef.graph.presentation.JGraph;
import org.tigris.gef.persistence.ScalableGraphics;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigCircle;
import org.tigris.gef.presentation.FigLine;
import org.tigris.gef.presentation.FigPainter;
import org.tigris.gef.ui.IStatusBar;
import org.tigris.gef.ui.PaletteFig;
import org.tigris.gef.ui.ToolBar;
import org.tigris.gef.util.Localizer;
import org.tigris.gef.util.ResourceLoader;
import user.commands.Cmd;

/**
 *
 * @author user
 */
public class CmdGUIDisplay implements Cmd {

    Editor editor;
    public static JFrame mainFrame;
    public static List<DrawingCanvas> loadlayers;

    public CmdGUIDisplay(Editor editor) {
        this.editor = editor;
    }

    public void setFrame(List<DrawingCanvas> cs) {
        mainFrame = new JFrame("Graphics demo");
        mainFrame.setSize(2000, 2000);
        
        mainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    mainFrame.dispose();
                }

                public void windowClosed(WindowEvent event) {
                    System.exit(0);
                }
            });
        
        Iterator<DrawingCanvas> it = cs.iterator();
        while (it.hasNext()) {
            DrawingCanvas c = it.next();
            if (c.isVisible()) {
                mainFrame.getContentPane().add(c);
            }
        }
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    @Override
    public Object runCmd(Map inputs) {

        loadlayers = new ArrayList<DrawingCanvas>();
        CmdDisplayGUI guicmd = new CmdDisplayGUI(editor, loadlayers);
        Globals.curEditor().executeCmd(guicmd, null);

        
        setFrame(loadlayers);

        //classDiagramDemo.graphFrame.pack();
        return null;
    }

    public class GefGraphFrame extends JFrame
            implements IStatusBar, Cloneable, ModeChangeListener {

        /**
         * The toolbar (shown at top of window).
         */
        private ToolBar _toolbar = new PaletteFig();
        /**
         * The graph pane (shown in middle of window).
         */
        private JGraph _graph;
        /**
         * A statusbar (shown at bottom ow window).
         */
        private JLabel _statusbar = new JLabel(" ");

        private JPanel _mainPanel = new JPanel(new BorderLayout());
        private JPanel _graphPanel = new JPanel(new BorderLayout());
        private JMenuBar _menubar = new JMenuBar();

        /**
         * Contruct a new JGraphFrame with the title "untitled" and a new
         * DefaultGraphModel.
         */
        public GefGraphFrame() {
            this("untitled");
        }

        public GefGraphFrame(boolean init_later) {
            super("untitled");
            if (!init_later) {
                init(new JGraph());
            }
        }

        /**
         * Contruct a new JGraphFrame with the given title and a new
         * DefaultGraphModel.
         */
        public GefGraphFrame(String title) {
            this(title, new JGraph());
        }

        public GefGraphFrame(String title, Editor ed) {
            this(title, new JGraph(ed));
        }

        /**
         * Contruct a new JGraphFrame with the given title and given JGraph. All
         * JGraphFrame contructors call this one.
         */
        public GefGraphFrame(String title, JGraph jg) {
            super(title);
            init(jg);
        }

        public void init() {
            init(new JGraph());
        }

        public void init(JGraph jg) {
            _graph = jg;
            Container content = getContentPane();
            setUpMenus();
            content.setLayout(new BorderLayout());
            content.add(_menubar, BorderLayout.NORTH);
            _graphPanel.add(_graph, BorderLayout.CENTER);
            _graphPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

            _mainPanel.add(_toolbar, BorderLayout.NORTH);
            _mainPanel.add(_graphPanel, BorderLayout.CENTER);
            content.add(_mainPanel, BorderLayout.CENTER);
            content.add(_statusbar, BorderLayout.SOUTH);
            setSize(2000, 2000);
            _graph.addModeChangeListener(this);
        }

        /**
         * Contruct a new JGraphFrame with the titleed with the given
         * GraphModel.
         */
        public GefGraphFrame(String title, GraphModel gm) {
            this(title);
            setGraphModel(gm);
        }

        /**
         * Contruct a new JGraphFrame with the title "untitled" and the given
         * GraphModel.
         */
        public GefGraphFrame(GraphModel gm) {
            this("untitled");
            setGraphModel(gm);
        }
        ////////////////////////////////////////////////////////////////
        // Cloneable implementation

        public Object clone() {
            return null; //needs-more-work
        }
        ////////////////////////////////////////////////////////////////
        // accessors

        public JGraph getGraph() {
            return _graph;
        }

        public GraphEdgeRenderer getGraphEdgeRenderer() {
            return _graph.getEditor().getGraphEdgeRenderer();
        }

        public GraphModel getGraphModel() {
            return _graph.getGraphModel();
        }

        public GraphNodeRenderer getGraphNodeRenderer() {
            return _graph.getEditor().getGraphNodeRenderer();
        }

        public JMenuBar getJMenuBar() {
            return _menubar;
        }

        public ToolBar getToolBar() {
            return _toolbar;
        }
        ////////////////////////////////////////////////////////////////
        // ModeChangeListener implementation

        public void modeChange(ModeChangeEvent mce) {
            //org.graph.commons.logging.LogFactory.getLog(null).info("TabDiagram got mode change event");
            if (!Globals.getSticky() && Globals.mode() instanceof ModeSelect) {
                _toolbar.unpressAllButtons();
            }
        }

        public void setGraph(JGraph g) {
            _graph = g;
        }

        public void setGraphEdgeRenderer(GraphEdgeRenderer rend) {
            _graph.getEditor().setGraphEdgeRenderer(rend);
        }

        public void setGraphModel(GraphModel gm) {
            _graph.setGraphModel(gm);
        }

        public void setGraphNodeRenderer(GraphNodeRenderer rend) {
            _graph.getEditor().setGraphNodeRenderer(rend);
        }

        public void setJMenuBar(JMenuBar mb) {
            _menubar = mb;
            getContentPane().add(_menubar, BorderLayout.NORTH);
        }

        public void setToolBar(ToolBar tb) {
            _toolbar = tb;
            _mainPanel.add(_toolbar, BorderLayout.NORTH);
        }

        /**
         * Set up the menus and keystrokes for menu items. Subclasses can
         * override this, or you can use setMenuBar().
         */
        protected void setUpMenus() {
            JMenuItem openItem,
                    saveItem,
                    printItem,
                    exitItem;
            JMenuItem deleteItem, copyItem, pasteItem;
            JMenuItem groupItem, ungroupItem;
            JMenuItem toBackItem, backwardItem, toFrontItem, forwardItem;

            JMenu file = new JMenu(Localizer.localize("GefBase", "File"));
            file.setMnemonic('F');
            _menubar.add(file);
            //file.add(new CmdNew());
            openItem = file.add(new CmdOpen());
            saveItem = file.add(new CmdSave());
            CmdPrint cmdPrint = new CmdPrint();
            printItem = file.add(cmdPrint);
            exitItem = file.add(new CmdExit());

            JMenu edit = new JMenu(Localizer.localize("GefBase", "Edit"));
            edit.setMnemonic('E');
            _menubar.add(edit);

            JMenu select = new JMenu(Localizer.localize("GefBase", "Select"));
            edit.add(select);
            select.add(new CmdSelectNext(false));
            select.add(new CmdSelectNext(true));
            select.add(new CmdSelectInvert());

            edit.addSeparator();

            copyItem = edit.add(new CmdCopy());
            copyItem.setMnemonic('C');
            pasteItem = edit.add(new CmdPaste());
            pasteItem.setMnemonic('P');

            deleteItem = edit.add(new CmdDeleteFromModel());
            edit.addSeparator();
            edit.add(new CmdUseReshape());
            edit.add(new CmdUseResize());
            edit.add(new CmdUseRotate());

            JMenu view = new JMenu(Localizer.localize("GefBase", "View"));
            _menubar.add(view);
            view.setMnemonic('V');
            //view.add(new CmdSpawn());
            view.add(new CmdShowProperties());
            view.addSeparator();
            view.add(new CmdAdjustGrid());
            view.add(new CmdAdjustGuide());
            view.add(new CmdAdjustPageBreaks());

            JMenu arrange = new JMenu(Localizer.localize("GefBase", "Arrange"));
            _menubar.add(arrange);
            arrange.setMnemonic('A');
            groupItem = arrange.add(new CmdGroup());
            groupItem.setMnemonic('G');
            ungroupItem = arrange.add(new CmdUngroup());
            ungroupItem.setMnemonic('U');

            JMenu align = new JMenu(Localizer.localize("GefBase", "Align"));
            arrange.add(align);
            align.add(new CmdAlign(CmdAlign.ALIGN_TOPS));
            align.add(new CmdAlign(CmdAlign.ALIGN_BOTTOMS));
            align.add(new CmdAlign(CmdAlign.ALIGN_LEFTS));
            align.add(new CmdAlign(CmdAlign.ALIGN_RIGHTS));
            align.add(new CmdAlign(CmdAlign.ALIGN_H_CENTERS));
            align.add(new CmdAlign(CmdAlign.ALIGN_V_CENTERS));
            align.add(new CmdAlign(CmdAlign.ALIGN_TO_GRID));

            JMenu distribute
                    = new JMenu(Localizer.localize("GefBase", "Distribute"));
            arrange.add(distribute);
            distribute.add(new CmdDistribute(CmdDistribute.H_SPACING));
            distribute.add(new CmdDistribute(CmdDistribute.H_CENTERS));
            distribute.add(new CmdDistribute(CmdDistribute.V_SPACING));
            distribute.add(new CmdDistribute(CmdDistribute.V_CENTERS));

            JMenu reorder = new JMenu(Localizer.localize("GefBase", "Reorder"));
            arrange.add(reorder);
            toBackItem = reorder.add(new CmdReorder(CmdReorder.SEND_TO_BACK));
            toFrontItem = reorder.add(new CmdReorder(CmdReorder.BRING_TO_FRONT));
            backwardItem = reorder.add(new CmdReorder(CmdReorder.SEND_BACKWARD));
            forwardItem = reorder.add(new CmdReorder(CmdReorder.BRING_FORWARD));

            JMenu nudge = new JMenu(Localizer.localize("GefBase", "Nudge"));
            arrange.add(nudge);

            KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK);
            KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
            KeyStroke ctrlP = KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK);
            KeyStroke altF4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK);

            KeyStroke delKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
            KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
            KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
            KeyStroke ctrlG = KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK);
            KeyStroke ctrlU = KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK);
            KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK);
            KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
            KeyStroke sCtrlB
                    = KeyStroke.getKeyStroke(
                            KeyEvent.VK_B,
                            KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
            KeyStroke sCtrlF
                    = KeyStroke.getKeyStroke(
                            KeyEvent.VK_F,
                            KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);

            //newItem.setAccelerator(ctrlN);
            openItem.setAccelerator(ctrlO);
            saveItem.setAccelerator(ctrlS);
            printItem.setAccelerator(ctrlP);
            exitItem.setAccelerator(altF4);

            deleteItem.setAccelerator(delKey);
            //undoItem.setAccelerator(ctrlZ);
            //cutItem.setAccelerator(ctrlX);
            copyItem.setAccelerator(ctrlC);
            pasteItem.setAccelerator(ctrlV);

            groupItem.setAccelerator(ctrlG);
            ungroupItem.setAccelerator(ctrlU);

            toBackItem.setAccelerator(sCtrlB);
            toFrontItem.setAccelerator(sCtrlF);
            backwardItem.setAccelerator(ctrlB);
            forwardItem.setAccelerator(ctrlF);

        }
        ////////////////////////////////////////////////////////////////
        // display related methods

        public void setVisible(boolean b) {
            super.setVisible(b);
            if (b) {
                Globals.setStatusBar(this);
            }
        }
        ////////////////////////////////////////////////////////////////
        // IStatusListener implementation

        /**
         * Show a message in the statusbar.
         */
        public void showStatus(String msg) {
            if (_statusbar != null) {
                _statusbar.setText(msg);
            }
        }
    }

    public class ClassDiagramDemo {

        private GefGraphFrame graphFrame;

        public ClassDiagramDemo() {
            // init localizer and resourceloader
            ////////////////////////////////////////////////////////////////
            // constructors

            Localizer.addResource(
                    "GefBase",
                    "org.tigris.gef.base.BaseResourceBundle");
            Localizer.addResource(
                    "GefPres",
                    "org.tigris.gef.presentation.PresentationResourceBundle");
            Localizer.addLocale(Locale.getDefault());
            Localizer.switchCurrentLocale(Locale.getDefault());
            ResourceLoader.addResourceExtension("gif");
            ResourceLoader.addResourceLocation("/org/tigris/gef/Images");
            ResourceLoader.addResourceLocation("/org/tigris/gefdemo/uml/Images");
            GraphModel gm = new DefaultGraphModel();//UmlGraphModel();

            graphFrame = new GefGraphFrame("Class Diagram", gm);
            graphFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    graphFrame.dispose();
                }

                public void windowClosed(WindowEvent event) {
                    System.exit(0);
                }
            });
            graphFrame.setToolBar(new PaletteFig()); //needs-more-work

            /*ClassDiagramRenderer renderer = new ClassDiagramRenderer();
             graphFrame.getGraph().setGraphNodeRenderer(renderer);
             graphFrame.getGraph().setGraphEdgeRenderer(renderer);
        
             try {
             graphFrame.getGraphModel().setConnectionConstrainer(ConnectionConstrainer.getInstance());
             } catch (GraphModelException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             }*/
            graphFrame.setBounds(10, 10, 1000, 1000);
            graphFrame.setVisible(true);

        }
    }

    

}
