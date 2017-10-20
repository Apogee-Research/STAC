// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
package org.tigris.gef.graph.presentation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.tigris.gef.base.*;
import org.tigris.gef.ui.*;
import org.tigris.gef.undo.RedoAction;
import org.tigris.gef.undo.UndoAction;
import org.tigris.gef.event.*;
import org.tigris.gef.graph.*;
import org.tigris.gef.util.*;

/**
 * A window that displays a toolbar, a connected graph editing pane, and a
 * status bar.
 */
public class JGraphFrame extends JFrame implements IStatusBar, Cloneable,
        ModeChangeListener {

    private static final long serialVersionUID = -8167010467922210977L;
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
    public JGraphFrame() {
        this("untitled");
    }

    public JGraphFrame(boolean init_later) {
        super("untitled");
        if (!init_later) {
            init(new JGraph());
        }
    }

    /**
     * Contruct a new JGraphFrame with the given title and a new
     * DefaultGraphModel.
     */
    public JGraphFrame(String title) {
        this(title, new JGraph());
    }

    public JGraphFrame(String title, Editor ed) {
        this(title, new JGraph(ed));
    }

    /**
     * Contruct a new JGraphFrame with the given title and given JGraph. All
     * JGraphFrame contructors call this one.
     */
    public JGraphFrame(String title, JGraph jg) {
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
        setSize(300, 250);
        _graph.addModeChangeListener(this);
    }

    /**
     * Contruct a new JGraphFrame with the title "untitled" and the given
     * GraphModel.
     */
    public JGraphFrame(GraphModel gm) {
        this("untitled");
        setGraphModel(gm);
    }

    // //////////////////////////////////////////////////////////////
    // Cloneable implementation
    public Object clone() {
        return null; // needs-more-work
    }

    // //////////////////////////////////////////////////////////////
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

    // //////////////////////////////////////////////////////////////
    // ModeChangeListener implementation
    public void modeChange(ModeChangeEvent mce) {
        // org.graph.commons.logging.LogFactory.getLog(null).info("TabDiagram got mode change event");
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
     * Set up the menus and keystrokes for menu items. Subclasses can override
     * this, or you can use setMenuBar().
     */
    protected void setUpMenus() {
        JMenuItem openItem, saveItem, printItem, exitItem;
        JMenuItem deleteItem, copyItem, pasteItem;
        JMenuItem groupItem, ungroupItem;
        JMenuItem toBackItem, backwardItem, toFrontItem, forwardItem;

        JMenu file = new JMenu(Localizer.localize("GefBase", "File"));
        file.setMnemonic('F');
        _menubar.add(file);
        // file.add(new CmdNew());
        openItem = file.add(new CmdOpen());
        saveItem = file.add(new CmdSave());
        file.add(new CmdSavePGML());
        file.add(new CmdSaveSVG());
        CmdPrint cmdPrint = new CmdPrint();
        printItem = file.add(cmdPrint);
        file.add(new CmdPrintPageSetup(cmdPrint));
        file.add(new CmdOpenWindow("org.tigris.gef.base.PrefsEditor",
                "Preferences..."));
        // file.add(new CmdClose());
        exitItem = file.add(new CmdExit());

        JMenu edit = new JMenu(Localizer.localize("GefBase", "Edit"));
        edit.setMnemonic('E');
        _menubar.add(edit);

        JMenuItem undoItem = edit.add(new UndoAction(Localizer.localize(
                "GefBase", "Undo")));
        undoItem.setMnemonic(Localizer.localize("GefBase", "UndoMnemonic")
                .charAt(0));
        JMenuItem redoItem = edit.add(new RedoAction(Localizer.localize(
                "GefBase", "Redo")));
        redoItem.setMnemonic(Localizer.localize("GefBase", "RedoMnemonic")
                .charAt(0));

        JMenu select = new JMenu(Localizer.localize("GefBase", "Select"));
        edit.add(select);
        select.add(new CmdSelectAll());
        select.add(new CmdSelectNext(false));
        select.add(new CmdSelectNext(true));
        select.add(new CmdSelectInvert());

        edit.addSeparator();

        copyItem = edit.add(new CmdCopy());
        copyItem.setMnemonic('C');
        pasteItem = edit.add(new CmdPaste());
        pasteItem.setMnemonic('P');

        deleteItem = edit.add(new CmdRemoveFromGraph());
        edit.addSeparator();
        edit.add(new CmdUseReshape());
        edit.add(new CmdUseResize());
        edit.add(new CmdUseRotate());

        JMenu view = new JMenu(Localizer.localize("GefBase", "View"));
        _menubar.add(view);
        view.setMnemonic('V');
        //view.add(new CmdSpawn());
        view.add(new CmdShowProperties());
        // view.addSeparator();
        // view.add(new CmdZoomIn());
        // view.add(new CmdZoomOut());
        // view.add(new CmdZoomNormal());
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
        align.add(new AlignAction(AlignAction.ALIGN_TOPS));
        align.add(new AlignAction(AlignAction.ALIGN_BOTTOMS));
        align.add(new AlignAction(AlignAction.ALIGN_LEFTS));
        align.add(new AlignAction(AlignAction.ALIGN_RIGHTS));
        align.add(new AlignAction(AlignAction.ALIGN_H_CENTERS));
        align.add(new AlignAction(AlignAction.ALIGN_V_CENTERS));
        align.add(new AlignAction(AlignAction.ALIGN_TO_GRID));

        JMenu distribute = new JMenu(Localizer
                .localize("GefBase", "Distribute"));
        arrange.add(distribute);
        distribute.add(new DistributeAction(DistributeAction.H_SPACING));
        distribute.add(new DistributeAction(DistributeAction.H_CENTERS));
        distribute.add(new DistributeAction(DistributeAction.V_SPACING));
        distribute.add(new DistributeAction(DistributeAction.V_CENTERS));

        JMenu reorder = new JMenu(Localizer.localize("GefBase", "Reorder"));
        arrange.add(reorder);
        toBackItem = reorder.add(new CmdReorder(CmdReorder.SEND_TO_BACK));
        toFrontItem = reorder.add(new CmdReorder(CmdReorder.BRING_TO_FRONT));
        backwardItem = reorder.add(new CmdReorder(CmdReorder.SEND_BACKWARD));
        forwardItem = reorder.add(new CmdReorder(CmdReorder.BRING_FORWARD));

        JMenu nudge = new JMenu(Localizer.localize("GefBase", "Nudge"));
        arrange.add(nudge);
        nudge.add(new NudgeAction(NudgeAction.LEFT));
        nudge.add(new NudgeAction(NudgeAction.RIGHT));
        nudge.add(new NudgeAction(NudgeAction.UP));
        nudge.add(new NudgeAction(NudgeAction.DOWN));

        KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlP = KeyStroke.getKeyStroke(KeyEvent.VK_P,
                KeyEvent.CTRL_MASK);
        KeyStroke altF4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                KeyEvent.ALT_MASK);

        KeyStroke delKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        KeyStroke ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlY = KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlG = KeyStroke.getKeyStroke(KeyEvent.VK_G,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlU = KeyStroke.getKeyStroke(KeyEvent.VK_U,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B,
                KeyEvent.CTRL_MASK);
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                KeyEvent.CTRL_MASK);
        KeyStroke sCtrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B,
                KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
        KeyStroke sCtrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);

        openItem.setAccelerator(ctrlO);
        saveItem.setAccelerator(ctrlS);
        printItem.setAccelerator(ctrlP);
        exitItem.setAccelerator(altF4);

        deleteItem.setAccelerator(delKey);
        undoItem.setAccelerator(ctrlZ);
        redoItem.setAccelerator(ctrlY);
        copyItem.setAccelerator(ctrlC);
        pasteItem.setAccelerator(ctrlV);

        groupItem.setAccelerator(ctrlG);
        ungroupItem.setAccelerator(ctrlU);

        toBackItem.setAccelerator(sCtrlB);
        toFrontItem.setAccelerator(sCtrlF);
        backwardItem.setAccelerator(ctrlB);
        forwardItem.setAccelerator(ctrlF);

    }

    // //////////////////////////////////////////////////////////////
    // display related methods
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            Globals.setStatusBar(this);
        }
    }

    // //////////////////////////////////////////////////////////////
    // IStatusListener implementation
    /**
     * Show a message in the statusbar.
     */
    public void showStatus(String msg) {
        if (_statusbar != null) {
            _statusbar.setText(msg);
        }
    }
} /* end class JGraphFrame */
