// Copyright (c) 1996-2009 The Regents of the University of California. All
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
// File: ModeCreateEdge.java
// Classes: ModeCreateEdge
// Original Author: ics125 spring 1996
// $Id: ModeCreateEdge.java 1265 2009-08-19 05:57:56Z mvw $
package org.tigris.gef.base;

import java.awt.event.MouseEvent;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;

import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.MutableGraphModel;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigLine;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.undo.Memento;
import org.tigris.gef.undo.UndoManager;
import org.tigris.gef.util.Localizer;

/**
 * A Mode to interpret user input while creating an edge. Basically mouse down
 * starts creating an edge from a source port Fig, mouse motion paints a
 * rubber-band line, mouse up finds the destination port and finishes creating
 * the edge and makes an FigEdge and sends it to the back of the Layer.
 *
 * The argument "edgeClass" determines the type if edge to suggest that the
 * Editor's GraphModel construct. The GraphModel is responsible for actually
 * making an edge in the underlying model and connecting it to other model
 * elements.
 */
public class ModeCreateEdge extends ModeCreate {

    private static final long serialVersionUID = -3551773848229016093L;

    /**
     * The NetPort where the arc is painted from
     */
    private Object startPort;

    /**
     * The Fig that presents the starting NetPort
     */
    private Fig _startPortFig;

    /**
     * The FigNode on the NetNode that owns the start port
     */
    private FigNode _sourceFigNode;

    /**
     * The new NetEdge that is being created
     */
    private Object _newEdge;

    private static Log LOG = LogFactory.getLog(ModeCreateEdge.class);

    // //////////////////////////////////////////////////////////////
    // constructor
    public ModeCreateEdge() {
        super();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created ModeCreateEdge");
        }
    }

    public ModeCreateEdge(Editor par) {
        super(par);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created ModeCreateEdge for Editor");
        }
    }

    // //////////////////////////////////////////////////////////////
    // Mode API
    public String instructions() {
        return Localizer.localize("GefBase", "ModeCreateEdgeInstructions");
    }

    // //////////////////////////////////////////////////////////////
    // ModeCreate API
    /**
     * Create the new item that will be drawn. In this case I would rather
     * create the FigEdge when I am done. Here I just create a rubberband
     * FigLine to show during dragging.
     */
    public Fig createNewItem(MouseEvent me, int snapX, int snapY) {
        return new FigLine(snapX, snapY, 0, 0, Globals.getPrefs()
                .getRubberbandColor());
    }

    // //////////////////////////////////////////////////////////////
    // event handlers
    /**
     * On mousePressed determine what port the user is dragging from. The
     * mousePressed event is sent via ModeSelect.
     */
    public void mousePressed(MouseEvent me) {
        if (me.isConsumed()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("MousePressed but rejected as already consumed");
            }
            return;
        }

        UndoManager.getInstance().addMementoLock(this);
        int x = me.getX(), y = me.getY();
        Editor ce = Globals.curEditor();
        Fig underMouse = ce.hit(x, y);
        if (underMouse == null) {
            // org.graph.commons.logging.LogFactory.getLog(null).info("bighit");
            underMouse = ce.hit(x - 16, y - 16, 32, 32);
        }
        if (underMouse == null) {
            done();
            me.consume();
            if (LOG.isDebugEnabled()) {
                LOG.debug("MousePressed but nothing under - consumed");
            }
            return;
        }
        if (!(underMouse instanceof FigNode)) {
            done();
            me.consume();
            if (LOG.isDebugEnabled()) {
                LOG.debug("MousePressed but not a FigNode under - consumed");
            }
            return;
        }
        _sourceFigNode = (FigNode) underMouse;
        startPort = _sourceFigNode.deepHitPort(x, y);
        if (startPort == null) {
            done();
            me.consume();
            if (LOG.isDebugEnabled()) {
                LOG.debug("MousePressed but no port found - consumed");
            }
            return;
        }
        _startPortFig = _sourceFigNode.getPortFig(startPort);
        if (LOG.isDebugEnabled()) {
            LOG.debug("MousePressed start port set");
        }
        super.mousePressed(me);
    }

    /**
     * On mouseReleased, find the destination port, ask the GraphModel to
     * connect the two ports. If that connection is allowed, then construct a
     * new FigEdge and add it to the Layer and send it to the back.
     */
    public void mouseReleased(MouseEvent me) {
        if (me.isConsumed()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("MouseReleased but rejected as already consumed");
            }
            return;
        }
        if (_sourceFigNode == null) {
            done();
            me.consume();
            if (LOG.isDebugEnabled()) {
                LOG
                        .debug("MouseReleased but no source found so consuming and leaving");
            }
            return;
        }

        UndoManager.getInstance().startChain();

        int x = me.getX(), y = me.getY();
        Editor ce = Globals.curEditor();
        Fig f = ce.hit(x, y);
        if (f == null) {
            f = ce.hit(x - 16, y - 16, 32, 32);
        }
        GraphModel gm = ce.getGraphModel();
        if (!(gm instanceof MutableGraphModel)) {
            f = null;
        }

        if (f instanceof FigNode) {
            MutableGraphModel mgm = (MutableGraphModel) gm;
            // needs-more-work: potential class cast exception
            FigNode destFigNode = (FigNode) f;
            // If its a FigNode, then check within the
            // FigNode to see if a port exists
            Object foundPort = destFigNode.deepHitPort(x, y);

            if (foundPort != null && mgm.canConnect(startPort, foundPort)) {
                Fig destPortFig = destFigNode.getPortFig(foundPort);
                Class edgeClass = (Class) getArg("edgeClass");
                if (edgeClass != null) {
                    _newEdge = mgm.connect(startPort, foundPort, edgeClass);
                } else {
                    _newEdge = mgm.connect(startPort, foundPort);
                }

                // Calling connect() will add the edge to the GraphModel and
                // any LayerPersectives on that GraphModel will get a
                // edgeAdded event and will add an appropriate FigEdge
                // (determined by the GraphEdgeRenderer).
                if (null != _newEdge) {
                    ce.damageAll();
                    _sourceFigNode.damage();
                    destFigNode.damage();
                    _newItem = null;

                    Layer layer = ce.getLayerManager().getActiveLayer();

                    FigEdge fe = (FigEdge) layer.presentationFor(_newEdge);
                    fe.setSourcePortFig(_startPortFig);
                    fe.setSourceFigNode(_sourceFigNode);
                    fe.setDestPortFig(destPortFig);
                    fe.setDestFigNode(destFigNode);
                    endAttached(fe);

                    if (fe != null) {
                        ce.getSelectionManager().select(fe);
                    }
                    done();
                    me.consume();
                    if (LOG.isDebugEnabled()) {
                        LOG
                                .debug("MouseReleased Edge created and event consumed");
                    }
                    return;
                }
            }
        }
        _sourceFigNode.damage();
        ce.damageAll();
        _newItem = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("MouseReleased not on FigNode. Event consumed anyway.");
        }
        // UndoManager.getInstance().removeMementoLock(this);
        done();
        me.consume();
    }

    /**
     * Get the NetPort where the arc is painted from
     *
     * @return Returns the startPort.
     */
    protected Object getStartPort() {
        return startPort;
    }

    protected void endAttached(FigEdge fe) {
        UndoManager.getInstance().removeMementoLock(this);
        if (UndoManager.getInstance().isGenerateMementos()) {
            UndoManager.getInstance().addMemento(
                    new CreateEdgeMemento(editor, _newEdge, fe));
        }
        UndoManager.getInstance().addMementoLock(this);
    }

    class CreateEdgeMemento extends Memento {

        private FigEdge edgePlaced;
        private Object edge;
        private Editor editor;

        CreateEdgeMemento(Editor ed, Object edge, FigEdge edgePlaced) {
            this.edgePlaced = edgePlaced;
            this.edge = edge; // Eventually need to pass a collection of
            // objects instead...
            this.editor = ed;
        }

        public void undo() {
            UndoManager.getInstance().addMementoLock(this);
            editor.getSelectionManager().deselect(edgePlaced);
            if (edge != null) {
                ((MutableGraphModel) editor.getGraphModel()).removeEdge(edge);
            }
            editor.remove(edgePlaced);
            UndoManager.getInstance().removeMementoLock(this);
        }

        public void redo() {
            UndoManager.getInstance().addMementoLock(this);
            editor.add(edgePlaced);
            GraphModel gm = editor.getGraphModel();
            if (edge != null) {
                ((MutableGraphModel) gm).addEdge(edge);
            }
            editor.getSelectionManager().select(edgePlaced);
            UndoManager.getInstance().removeMementoLock(this);
        }

        public void dispose() {
        }

        public String toString() {
            return (isStartChain() ? "*" : " ") + "PlaceMemento "
                    + edgePlaced.getBounds();
        }
    }
}
