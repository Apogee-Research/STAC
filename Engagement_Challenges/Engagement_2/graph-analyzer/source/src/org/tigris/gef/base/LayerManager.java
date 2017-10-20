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
// File: LayerManager.java
// Classes: LayerManager
// Original Author: jrobbins@ics.uci.edu
// $Id: LayerManager.java 1191 2008-12-11 00:31:10Z bobtarling $
package org.tigris.gef.base;

import graphviz.TestCircleMath.ScaledRectangle;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigPainter;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class implements a kind of Layer that contains other Layers. Layer's can
 * be nested in an is-part-of tree. That tree can be walked to paint the
 * contents of the view, find what the user clicked on, find a layer by name,
 * save the contents to a file, etc.
 */
public class LayerManager implements java.io.Serializable {

    private static final long serialVersionUID = -4133017459593099807L;

    /**
     * The Layer's contained within this LayerManager.
     */
    public List<Layer> _layers = new ArrayList<Layer>();

    /**
     * In most editors one Layer is the active layer and all mouse clicks go to
     * the contents of that layer. For now I assume this, but I would like to
     * avoid this assumption in the future.
     */
    private Layer _activeLayer;

    /**
     * Should only the active layer be repainted?
     */
    private boolean _paintActiveOnly = true;

    /**
     * Should only the known layers be repainted?
     */
    private boolean _paintLayers = true;

    /**
     * in 0.13 will become private use getEditor()
     */
    public Editor _editor = null;

    // //////////////////////////////////////////////////////////////
    // constructors and related methods
    /**
     * Construct a new LayerManager with no sublayers.
     */
    public LayerManager(Editor editor) {
        _editor = editor;
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    /**
     * Add a sublayer to this layer.
     */
    public void addLayer(Layer lay, boolean makeActive) {
        if (findLayerNamed(lay.getName()) == null) {
            _editor.getModeManager().leaveAll();
            _layers.add(lay);
            lay.addEditor(_editor);
            if (makeActive) {
                setActiveLayer(lay);
            }
        }
    }

    public void addLayer(Layer lay) {
        addLayer(lay, true);
    }

    public void removeAllLayers() {
        _layers.clear();
        _activeLayer = null;
    }

    public void replaceLayer(Layer oldLayer, Layer newLayer) {
        _editor.getModeManager().leaveAll();

        oldLayer.removeEditor(_editor);
        int oldIndex = _layers.indexOf(oldLayer);

        _layers.set(oldIndex, newLayer);
        newLayer.addEditor(_editor);
        if (_activeLayer == oldLayer) {
            setActiveLayer(newLayer);
        }
    }

    public void replaceActiveLayer(Layer layer) {
        if (_activeLayer == null) {
            addLayer(layer, true);
        } else {
            replaceLayer(_activeLayer, layer);
        }
    }

    /**
     * Remove a sublayer to this layer.
     */
    public void removeLayer(Layer lay) {
        _layers.remove(lay);
        lay.removeEditor(_editor);
        if (_activeLayer == lay) {
            if (_layers.size() >= 1) {
                _activeLayer = (Layer) _layers.get(0);
            } else {
                _activeLayer = null;
            }
        }
    }

    /**
     * Find a layer with the given name somewhere in the layer tree.
     */
    public Layer findLayerNamed(String aName) {
        int count = _layers.size();
        for (int layerIndex = 0; layerIndex < count; ++layerIndex) {
            Layer curLayer = (Layer) _layers.get(layerIndex);
            if (aName.equals(curLayer.getName())) {
                return curLayer;
            }
        }
        return null;
    }

    /**
     * Make one of my layers the active one.
     */
    public void setActiveLayer(Layer lay) {
        //if (_activeLayer != null && _activeLayer.isAlwaysOnTop())
        //    return;

        if (_layers.contains(lay)) {
            _activeLayer = lay;
        } else {
            org.graph.commons.logging.LogFactory.getLog(null).info("That layer is not one of my layers");
        }
    }

    /**
     * Reply which layer is the active one. In case LayerManager's are nested,
     * this works recursively.
     */
    public Layer getActiveLayer() {
        return _activeLayer;
    }

    /**
     * When an editor or some tool wants to look at all the Figs that are
     * contained in this layer, reply the contents of my active layer. Maybe
     * this should really reply _all_ the contents of all layers.
     */
    public List<Fig> getContents() {
        if (_activeLayer == null) {
            return Collections.emptyList();
        } else {
            return _activeLayer.getContents();
        }
    }

    /**
     * When an editor or some tool wants to look at all the Figs that are
     * contained in this layer, reply the contents of my active layer. Maybe
     * this should really reply _all_ the contents of all layers. use
     * getContents with no args
     */
    public List getContents(List oldList) {
        return (_activeLayer == null) ? null : _activeLayer.getContents();
    }

    // //////////////////////////////////////////////////////////////
    // painting methods
    public void paint(GraphicsSpecial sg, Graphics g) { // kept for backwards compatibility
        paint(sg, g, null);
    }


    /*
     * Paint the contents of this LayerManager using a given painter by painting
     * all layers.
     */
    public void paint(GraphicsSpecial sg, Graphics g, FigPainter painter) {
        if (!_paintLayers) {
            return;
        }

        if (_paintActiveOnly) {
            _activeLayer.paint(sg, g, painter);
        } else {

            Layer currentActiveLayer = _activeLayer;

            boolean alwaysOnTopState = currentActiveLayer.isAlwaysOnTop();
            currentActiveLayer.setAlwaysOnTop(false);
            int count = _layers.size();
            for (int layerIndex = 0; layerIndex < count; ++layerIndex) {
                Layer tmpLayer = (Layer) _layers.get(layerIndex);
                setActiveLayer(tmpLayer);
                tmpLayer.paint(g, painter);
            }
            setActiveLayer(currentActiveLayer);
            currentActiveLayer.setAlwaysOnTop(alwaysOnTopState);
        }
    }

    /**
     * Paint the contents of this LayerManager by painting all layers.
     */
    public void paint(Graphics g) { // kept for backwards compatibility
        paint(g, null);
    }

    /**
     * Paint the contents of this LayerManager using a given painter by painting
     * all layers.
     */
    public void paint(Graphics g, FigPainter painter) {
        if (!_paintLayers) {
            return;
        }

        if (_paintActiveOnly) {
            _activeLayer.paint(g, painter);
        } else {

            Layer currentActiveLayer = _activeLayer;

            boolean alwaysOnTopState = currentActiveLayer.isAlwaysOnTop();
            currentActiveLayer.setAlwaysOnTop(false);
            int count = _layers.size();
            for (int layerIndex = 0; layerIndex < count; ++layerIndex) {
                Layer tmpLayer = (Layer) _layers.get(layerIndex);
                setActiveLayer(tmpLayer);
                tmpLayer.paint(g, painter);
            }
            setActiveLayer(currentActiveLayer);
            currentActiveLayer.setAlwaysOnTop(alwaysOnTopState);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Layer API
    /**
     * When the user tries to add a new Fig to a LayerManager, pass that
     * addition along to my active layer.
     */
    public void add(Fig f) {
        if (_activeLayer != null) {
            _activeLayer.add(f);
        }
    }

    /**
     * When the user tries to remove a new Fig from a LayerManager, pass that
     * removal along to my active layer.
     */
    public void remove(Fig f) {
        if (_activeLayer != null) {
            _activeLayer.remove(f);
        }
    }

    /**
     * See comments above, this message is passed to my active layer.
     */
    public void removeAll() {
        if (_activeLayer != null) {
            _activeLayer.removeAll();
        }
    }

    /**
     * See comments above, this message is passed to my active layer. in 0.13
     * use getFigs()
     */
    public Enumeration elements() {
        return (_activeLayer == null) ? null : _activeLayer.elements();
    }

    /**
     * See comments above, this message is passed to my active layer.
     */
    public Fig hit(Rectangle r) {
        return (_activeLayer == null) ? null : _activeLayer.hit(r);
    }

    /**
     * Try to find a FigNode instance that presents the given Net-level object.
     */
    public Fig presentationFor(Object obj) {
        Fig f = null;
        int count = _layers.size();
        for (int layerIndex = 0; layerIndex < count; ++layerIndex) {
            Layer sub = (Layer) _layers.get(layerIndex);
            f = sub.presentationFor(obj);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    /**
     * See comments above, this message is passed to my active layer.
     */
    public void sendToBack(Fig f) {
        if (_activeLayer != null) {
            _activeLayer.sendToBack(f);
        }
    }

    /**
     * See comments above, this message is passed to my active layer.
     */
    public void bringForward(Fig f) {
        if (_activeLayer != null) {
            _activeLayer.bringForward(f);
        }
    }

    /**
     * See comments above, this message is passed to my active layer.
     */
    public void sendBackward(Fig f) {
        if (_activeLayer != null) {
            _activeLayer.sendBackward(f);
        }
    }

    /**
     * See comments above, this message is passed to my active layer.
     */
    public void bringToFront(Fig f) {
        if (_activeLayer != null) {
            _activeLayer.bringToFront(f);
        }
    }

    /**
     * See comments above, this message is passed to my active layer.
     */
    public void reorder(Fig f, int function) {
        if (_activeLayer != null) {
            _activeLayer.reorder(f, function);
        }
    }

    public void setEditor(Editor ed) {
        _editor = ed;
        int layerCount = _layers.size();
        for (int layerIndex = 0; layerIndex < layerCount; ++layerIndex) {
            Layer layer = (Layer) _layers.get(layerIndex);
            layer.addEditor(ed);
        }
    }

    public Editor getEditor() {
        return _editor;
    }

    public void preSave() {
        int layerCount = _layers.size();
        for (int layerIndex = 0; layerIndex < layerCount; ++layerIndex) {
            Layer layer = (Layer) _layers.get(layerIndex);
            layer.preSave();
        }
    }

    public void postSave() {
        int layerCount = _layers.size();
        for (int layerIndex = 0; layerIndex < layerCount; ++layerIndex) {
            Layer layer = (Layer) _layers.get(layerIndex);
            layer.postSave();
        }
    }

    public void postLoad() {
        int layerCount = _layers.size();
        for (int layerIndex = 0; layerIndex < layerCount; ++layerIndex) {
            Layer layer = (Layer) _layers.get(layerIndex);
            layer.postLoad();
        }
    }

    public void setPaintActiveOnly(boolean activeOnly) {
        _paintActiveOnly = activeOnly;
    }

    public boolean getPaintActiveOnly() {
        return _paintActiveOnly;
    }

    public void setPaintLayers(boolean paintLayers) {
        _paintLayers = paintLayers;
    }

    public boolean getPaintLayers() {
        return _paintLayers;
    }

    public void setScale(double scale) {
        int layerCount = _layers.size();
        for (int layerIndex = 0; layerIndex < layerCount; ++layerIndex) {
            Layer layer = (Layer) _layers.get(layerIndex);
            layer.setScale(scale);
        }
    }

    Layer getLayer(String containername) {
        Iterator<Layer> it = _layers.iterator();
        while (it.hasNext()) {

            Layer nextlayer = it.next();
            if (nextlayer.getName().equals(containername)) {
                return nextlayer;
            }
        }
        return null;
    }
}
