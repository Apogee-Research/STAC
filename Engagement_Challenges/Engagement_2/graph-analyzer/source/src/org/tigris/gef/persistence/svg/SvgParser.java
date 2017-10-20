package org.tigris.gef.persistence.svg;

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
import java.util.*;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;

import org.tigris.gef.base.*;
import org.tigris.gef.presentation.*;
import org.tigris.gef.graph.presentation.*;
import org.tigris.gef.graph.*;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class SvgParser extends DefaultHandler {

    // //////////////////////////////////////////////////////////////
    // static variables
    public static SvgParser SINGLETON = new SvgParser();

    // //////////////////////////////////////////////////////////////
    // instance variables
    protected Diagram _diagram = null;
    protected int _nestedGroups = 0;
    protected HashMap _figRegistry = null;
    protected Map _ownerRegistry = new HashMap();

    // //////////////////////////////////////////////////////////////
    // XML element handlers
    private int _elementState = 0;
    private static final int DEFAULT_STATE = 0;
    private static final int TEXT_STATE = 1;
    private static final int LINE_STATE = 2;
    private static final int POLY_STATE = 3;
    private static final int NODE_STATE = 4;
    private static final int EDGE_STATE = 5;
    private static final int PRIVATE_STATE = 6;

    private static final int PRIVATE_NODE_STATE = 64;
    private static final int PRIVATE_EDGE_STATE = 65;
    private static final int TEXT_NODE_STATE = 14;
    private static final int TEXT_EDGE_STATE = 15;

    private FigLine _currentLine = null;
    private int _x1Int = 0;
    private int _y1Int = 0;
    private FigText _currentText = null;
    private StringBuffer _textBuf = null;
    private FigPoly _currentPoly = null;
    private FigNode _currentNode = null;
    private FigEdge _currentEdge = null;
    private String[] _entityPaths = {"/org/tigris/gef/xml/dtd/"};

    // //////////////////////////////////////////////////////////////
    // constructors
    protected SvgParser() {
    }

    public void characters(char[] ch, int start, int length) {
        if ((_elementState == TEXT_STATE || _elementState == PRIVATE_STATE
                || _elementState == TEXT_NODE_STATE
                || _elementState == TEXT_EDGE_STATE
                || _elementState == PRIVATE_NODE_STATE || _elementState == PRIVATE_EDGE_STATE)
                && _textBuf != null) {
            _textBuf.append(ch, start, length);
        }
    }

    protected Color colorByName(String name, Color defaultColor) {
        if (name.equalsIgnoreCase("white")) {
            return Color.white;
        }
        if (name.equalsIgnoreCase("lightGray")) {
            return Color.lightGray;
        }
        if (name.equalsIgnoreCase("gray")) {
            return Color.gray;
        }
        if (name.equalsIgnoreCase("darkGray")) {
            return Color.darkGray;
        }
        if (name.equalsIgnoreCase("black")) {
            return Color.black;
        }
        if (name.equalsIgnoreCase("red")) {
            return Color.red;
        }
        if (name.equalsIgnoreCase("pink")) {
            return Color.pink;
        }
        if (name.equalsIgnoreCase("orange")) {
            return Color.orange;
        }
        if (name.equalsIgnoreCase("yellow")) {
            return Color.yellow;
        }
        if (name.equalsIgnoreCase("green")) {
            return Color.green;
        }
        if (name.equalsIgnoreCase("magenta")) {
            return Color.magenta;
        }
        if (name.equalsIgnoreCase("cyan")) {
            return Color.cyan;
        }
        if (name.equalsIgnoreCase("blue")) {
            return Color.blue;
        }
        try {
            return Color.decode(name);
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("invalid color code string: " + name);
        }
        return defaultColor;
    }

    private void edgeStateStartElement(String tagName, Attributes attrList) {
        if (tagName.equals("desc")) {
        } else if (tagName.equals("title")) {
        } else if (tagName.equals("path")) {
            Fig p = handlePath(attrList);
            _currentEdge.setFig(p);
            ((FigPoly) p).setComplete(true);
            _currentEdge.calcBounds();
            if (_currentEdge instanceof FigEdgePoly) {
                ((FigEdgePoly) _currentEdge).setInitiallyLaidOut(true);
            }
        } else if (tagName.equals("switch")) {
            _elementState = PRIVATE_EDGE_STATE;
            _textBuf = new StringBuffer();
        } else if (tagName.equals("text")) {
            _elementState = TEXT_EDGE_STATE;
            _textBuf = new StringBuffer();
            Fig p = handleText(attrList);
        }
    }

    public void endElement(String elementName) {
        switch (_elementState) {
            case 0:
                if ("g".equals(elementName)) {
                    _nestedGroups--;
                }
                break;

            case POLY_STATE:
                if (elementName.equals("path")) {
                    _elementState = DEFAULT_STATE;
                    _currentPoly = null;
                }
                break;

            case LINE_STATE:
                if (elementName.equals("line")) {
                    _elementState = DEFAULT_STATE;
                    _currentLine = null;
                }
                break;

            case TEXT_STATE:
                if (elementName.equals("text")) {
                    _currentText.setText(_textBuf.toString());
                    _elementState = DEFAULT_STATE;
                    _currentText = null;
                    _textBuf = null;
                }
                break;

            case TEXT_NODE_STATE:
                if (elementName.equals("text")) {
                    _currentText.setText(_textBuf.toString());
                    _elementState = NODE_STATE;
                    _currentText = null;
                    _textBuf = null;
                }
                break;

            case TEXT_EDGE_STATE:
                if (elementName.equals("text")) {
                    _currentText.setText(_textBuf.toString());
                    _elementState = EDGE_STATE;
                    _currentText = null;
                    _textBuf = null;
                }
                break;

            case NODE_STATE:
                _elementState = DEFAULT_STATE;
                _currentNode = null;
                _textBuf = null;
                break;

        // case EDGE_STATE:
            // _elementState = DEFAULT_STATE;
            // _currentEdge = null;
            // _textBuf = null;
            // break;
            case PRIVATE_STATE:
                privateStateEndElement(elementName);
                _textBuf = null;
                _elementState = DEFAULT_STATE;
                break;

            case PRIVATE_NODE_STATE:
                privateStateEndElement(elementName);
                _textBuf = null;
                _elementState = NODE_STATE;
                break;

            case PRIVATE_EDGE_STATE:
                privateStateEndElement(elementName);
                _textBuf = null;
                _elementState = EDGE_STATE;
                break;
        }
    }

    protected Fig findFig(String uri) {
        Fig f = null;
        if (uri.indexOf(".") == -1) {
            f = (Fig) _figRegistry.get(uri);
        } else {
            StringTokenizer st = new StringTokenizer(uri, ".");
            String figNum = st.nextToken();
            f = (Fig) _figRegistry.get(figNum);
            if (f instanceof FigEdge) {
                return ((FigEdge) f).getFig();
            }
            while (st.hasMoreElements()) {
                if (f instanceof FigGroup) {
                    String subIndex = st.nextToken();
                    int i = Integer.parseInt(subIndex);
                    f = ((FigGroup) f).getFigAt(i);
                }
            }
        }
        return f;
    }

    // needs-more-work: find object in model
    protected Object findOwner(String uri) {
        Object own = _ownerRegistry.get(uri);
        return own;
    }

    protected String[] getEntityPaths() {
        return _entityPaths;
    }

    // needs-more-work: make an instance of the named class
    protected GraphModel getGraphModelFor(String desc) {
        org.graph.commons.logging.LogFactory.getLog(null).info("should be: " + desc);
        return new DefaultGraphModel();
    }

    protected FigCircle handleEllipse(Attributes attrList) {
        FigCircle f = new FigCircle(0, 0, 50, 50);
        setAttrs(f, attrList);

        String cx = attrList.getValue("cx");
        String cy = attrList.getValue("cy");
        String rx = attrList.getValue("rx");
        String ry = attrList.getValue("ry");

        int cxInt = (cx == null || cx.equals("")) ? 0 : Integer.parseInt(cx);
        int cyInt = (cy == null || cy.equals("")) ? 0 : Integer.parseInt(cy);
        int rxInt = (rx == null || rx.equals("")) ? 10 : Integer.parseInt(rx);
        int ryInt = (ry == null || ry.equals("")) ? 10 : Integer.parseInt(ry);

        f.setBounds(cxInt - rxInt, cyInt - ryInt, rxInt * 2, ryInt * 2);

        return f;
    }

    /*
     * Returns Fig rather than FigGroups because this is also used for FigEdges.
     */
    protected Fig handleGroup(Attributes attrList) {
        Fig f = null;

        String clsNameBounds = attrList.getValue("class");
        StringTokenizer st = new StringTokenizer(clsNameBounds, ",;[] ");
        String clsName = translateClassName(st.nextToken());
        String xStr = null, yStr = null, wStr = null, hStr = null;

        if (st.hasMoreElements()) {
            xStr = st.nextToken();
            yStr = st.nextToken();
            wStr = st.nextToken();
            hStr = st.nextToken();
        }

        try {
            Class nodeClass = Class.forName(translateClassName(clsName));
            f = (Fig) nodeClass.newInstance();
            if (xStr != null && !xStr.equals("")) {
                int x = Integer.parseInt(xStr);
                int y = Integer.parseInt(yStr);
                int w = Integer.parseInt(wStr);
                int h = Integer.parseInt(hStr);

                f.setBounds(x, y, w, h);
            }

            if (f instanceof FigNode) {
                FigNode fn = (FigNode) f;
                _currentNode = fn;
                _elementState = NODE_STATE;
                _textBuf = new StringBuffer();
            }

            if (f instanceof FigEdge) {
                _currentEdge = (FigEdge) f;
                _elementState = EDGE_STATE;
            }
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("Exception in handleGroup");
            ex.printStackTrace();
        } catch (NoSuchMethodError ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("No constructor() in class " + clsName);
            ex.printStackTrace();
        }
        setAttrs(f, attrList);

        return f;
    }

    protected FigLine handleLine(Attributes attrList) {
        _currentLine = new FigLine(0, 0, 100, 100);
        setAttrs(_currentLine, attrList);
        _x1Int = 0;
        _y1Int = 0;
        _elementState = LINE_STATE;
        return _currentLine;
    }

    protected FigPoly handlePath(Attributes attrList) {
        String type = attrList.getValue("class");

        FigPoly f = null;
        if (type.equals("org.tigris.gef.presentation.FigPoly")) {
            f = new FigPoly();
        } else if (type.equals("org.tigris.gef.presentation.FigSpline")) {
            f = new FigSpline();
        } else if (type.equals("org.tigris.gef.presentation.FigInk")) {
            f = new FigInk();
        }
        if (f != null) {
            // Set the default attributes
            setAttrs(f, attrList);

            _currentPoly = f;
            _elementState = POLY_STATE;

            // Set the path data
            String path = attrList.getValue("d");
            int x = -1;
            int y = -1;

            try {
                StringReader reader = new StringReader(path);
                StreamTokenizer tokenizer = new StreamTokenizer(reader);

                int tok = tokenizer.nextToken();
                while (tok != StreamTokenizer.TT_EOF) {
                    if (tok == StreamTokenizer.TT_NUMBER) {
                        if (x == -1) {
                            x = (int) tokenizer.nval;
                        } else {
                            y = (int) tokenizer.nval;
                        }

                        // Add the point
                        if (x != -1 && y != -1) {
                            f.addPoint(x, y);
                            x = -1;
                            y = -1;
                        }
                    }

                    tok = tokenizer.nextToken();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return f;

    }

    protected Fig handlePolyLine(Attributes attrList) {
        String clsName = translateClassName(attrList.getValue("description"));
        if (clsName != null && clsName.indexOf("FigLine") != -1) {
            return handleLine(attrList);
        } else {
            return handlePath(attrList);
        }
    }

    protected FigRect handleRect(Attributes attrList) {
        FigRect f;
        String cornerRadius = attrList.getValue("rx");
        if (cornerRadius == null || cornerRadius.equals("")) {
            f = new FigRect(0, 0, 80, 80);
        } else {
            f = new FigRRect(0, 0, 80, 80);
            int rInt = Integer.parseInt(cornerRadius);
            ((FigRRect) f).setCornerRadius(rInt);
        }
        setAttrs(f, attrList);
        return f;
    }

    protected void handleSVG(Attributes attrList) {
        String name = attrList.getValue("id");
        String clsName = attrList.getValue("class");
        try {
            if (clsName != null && !clsName.equals("")) {
                initDiagram(clsName);
            }
            if (name != null && !name.equals("")) {
                _diagram.setName(name);
            }
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("Exception in handleSVG");
        }
    }

    protected FigText handleText(Attributes attrList) {
        FigText f = new FigText(100, 100, 90, 45);
        setAttrs(f, attrList);

        _elementState = TEXT_STATE;
        _textBuf = new StringBuffer();
        _currentText = f;

        String style = attrList.getValue("style");
        if (style != null) {
            String font = parseStyle("font", style);
            if (font != null) {
                f.setFontFamily(font);
            }
            String size = parseStyle("font-size", style);
            if (size != null) {
                int s = Integer.parseInt(size);
                f.setFontSize(s);
            }
        }
        return f;
    }

    // //////////////////////////////////////////////////////////////
    // internal methods
    protected void initDiagram(String diagDescr) {
        String clsName = diagDescr;
        String initStr = null;
        int bar = diagDescr.indexOf("|");
        if (bar != -1) {
            clsName = diagDescr.substring(0, bar);
            initStr = diagDescr.substring(bar + 1);
        }

        String newClassName = translateClassName(clsName);
        try {
            Class cls = Class.forName(newClassName);
            _diagram = (Diagram) cls.newInstance();

            if (initStr != null && !initStr.equals("")) {
                _diagram.initialize(findOwner(initStr));
            }
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("could not set diagram type to " + newClassName);
            ex.printStackTrace();
        }
    }

    protected void lineStateStartElement(String tagName, Attributes attrList) {
        if (_currentLine != null) {
            if (tagName.equals("desc")) {
            } else if (tagName.equals("title")) {
            }
            if (tagName.equals("moveto")) {
                String x1 = attrList.getValue("x");
                String y1 = attrList.getValue("y");
                _x1Int = (x1 == null || x1.equals("")) ? 0 : Integer
                        .parseInt(x1);
                _y1Int = (y1 == null || y1.equals("")) ? 0 : Integer
                        .parseInt(y1);
                _currentLine.setX1(_x1Int);
                _currentLine.setY1(_y1Int);
            } else if (tagName.equals("lineto")) {
                String x2 = attrList.getValue("x");
                String y2 = attrList.getValue("y");
                int x2Int = (x2 == null || x2.equals("")) ? _x1Int : Integer
                        .parseInt(x2);
                int y2Int = (y2 == null || y2.equals("")) ? _y1Int : Integer
                        .parseInt(y2);
                _currentLine.setX2(x2Int);
                _currentLine.setY2(y2Int);
            }
        }
    }

    private void nodeStateStartElement(String tagName, Attributes attrList) {
        if (tagName.equals("desc")) {
        } else if (tagName.equals("title")) {
        } else if (tagName.equals("switch")) {
            _textBuf = new StringBuffer();
            _elementState = PRIVATE_NODE_STATE;
        } else if (tagName.equals("text")) {
            _textBuf = new StringBuffer();
            _elementState = TEXT_NODE_STATE;
            Fig p = handleText(attrList);
        }
    }

    protected Color parseColor(String name, Color defaultColor) {
        try {
            int start = name.indexOf("rgb", 0);
            if (start != -1) {
                start = name.indexOf("(", start);
                int end = name.indexOf(",", start);
                if (start != -1) {
                    start++;
                    int red = Integer.parseInt(name.substring(start, end)
                            .trim());
                    start = end + 1;
                    end = name.indexOf(",", start);
                    int green = Integer.parseInt(name.substring(start, end)
                            .trim());
                    start = end + 1;
                    end = name.indexOf(")", start);
                    int blue = Integer.parseInt(name.substring(start, end)
                            .trim());

                    // org.graph.commons.logging.LogFactory.getLog(null).info("[SVGParser] parseColor:
                    // ("+red+","+green+","+blue+")");
                    return new Color(red, green, blue);
                }
                return defaultColor;
            }
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("invalid rgb() sequence: " + name);
            return defaultColor;
        }

        if (name.equalsIgnoreCase("none")) {
            return null;
        }
        if (name.equalsIgnoreCase("white")) {
            return Color.white;
        }
        if (name.equalsIgnoreCase("lightGray")) {
            return Color.lightGray;
        }
        if (name.equalsIgnoreCase("gray")) {
            return Color.gray;
        }
        if (name.equalsIgnoreCase("darkGray")) {
            return Color.darkGray;
        }
        if (name.equalsIgnoreCase("black")) {
            return Color.black;
        }
        if (name.equalsIgnoreCase("red")) {
            return Color.red;
        }
        if (name.equalsIgnoreCase("pink")) {
            return Color.pink;
        }
        if (name.equalsIgnoreCase("orange")) {
            return Color.orange;
        }
        if (name.equalsIgnoreCase("yellow")) {
            return Color.yellow;
        }
        if (name.equalsIgnoreCase("green")) {
            return Color.green;
        }
        if (name.equalsIgnoreCase("magenta")) {
            return Color.magenta;
        }
        if (name.equalsIgnoreCase("cyan")) {
            return Color.cyan;
        }
        if (name.equalsIgnoreCase("blue")) {
            return Color.blue;
        }

        try {
            return Color.decode(name);
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("invalid color code string: " + name);
        }
        return defaultColor;
    }

    /**
     * This method parses the 'style' attribute for a particular field
     *
     * @return java.lang.String
     * @param field java.lang.String
     * @param style java.lang.String
     */
    protected String parseStyle(String field, String style) {
        field += ":";
        int start = style.indexOf(field, 0);
        if (start != -1) {
            int end = style.indexOf(";", start);
            if (end != -1) {
                return style.substring(start + field.length(), end).trim();
            } else {
                return style.substring(start + field.length(),
                        style.length() - 1).trim();
            }
        }

        return null;
    }

    private void polyStateStartElement(String tagName, Attributes attrList) {
        if (_currentPoly != null) {
            if (tagName.equals("desc")) {
            } else if (tagName.equals("title")) {
            } else if (tagName.equals("moveto")) {
                String x1 = attrList.getValue("x");
                String y1 = attrList.getValue("y");
                _x1Int = (x1 == null || x1.equals("")) ? 0 : Integer
                        .parseInt(x1);
                _y1Int = (y1 == null || y1.equals("")) ? 0 : Integer
                        .parseInt(y1);
                // _currentLine.setX1(_x1Int);
                // _currentLine.setY1(_y1Int);
                _currentPoly.addPoint(_x1Int, _y1Int);
            } else if (tagName.equals("lineto")) {
                String x2 = attrList.getValue("x");
                String y2 = attrList.getValue("y");
                int x2Int = (x2 == null || x2.equals("")) ? _x1Int : Integer
                        .parseInt(x2);
                int y2Int = (y2 == null || y2.equals("")) ? _y1Int : Integer
                        .parseInt(y2);
                // _currentLine.setX2(x2Int);
                // _currentLine.setY2(y2Int);
                _currentPoly.addPoint(x2Int, y2Int);
            }
        }
    }

    private void privateStateEndElement(String tagName) {
        if (_currentNode != null) {
            if (_currentEdge != null) {
                _currentEdge = null;
            }

            _currentNode.setPrivateData(_textBuf.toString());

            String body = _textBuf.toString();
            StringTokenizer st2 = new StringTokenizer(body, "=\"' \t\n");
            Fig encloser = null;

            while (st2.hasMoreElements()) {
                String t = st2.nextToken();
                String v = "no such fig";
                if (st2.hasMoreElements()) {
                    v = st2.nextToken();
                }
                if (t.equals("enclosingFig")) {
                    encloser = findFig(v);
                }
                if (t.equals("text")) {
                }
            }
            _currentNode.setEnclosingFig(encloser);
        }

        if (_currentEdge != null) {
            Fig spf = null;
            Fig dpf = null;
            FigNode sfn = null;
            FigNode dfn = null;
            String body = _textBuf.toString();

            StringTokenizer st2 = new StringTokenizer(body, "=\"' \t\n");

            while (st2.hasMoreElements()) {
                String t = st2.nextToken();
                String v = st2.nextToken();
                if (t.equals("sourcePortFig")) {
                    spf = findFig(v);
                }
                if (t.equals("destPortFig")) {
                    dpf = findFig(v);
                }
                if (t.equals("sourceFigNode")) {
                    sfn = (FigNode) findFig(v);
                }
                if (t.equals("destFigNode")) {
                    dfn = (FigNode) findFig(v);
                }
            }

            _currentEdge.setSourcePortFig(spf);
            _currentEdge.setDestPortFig(dpf);
            _currentEdge.setSourceFigNode(sfn);
            _currentEdge.setDestFigNode(dfn);
        }
    }

    // //////////////////////////////////////////////////////////////
    // main parsing methods
    public synchronized Diagram readDiagram(URL url) {
        try {
            InputStream is = url.openStream();
            String filename = url.getFile();
            org.graph.commons.logging.LogFactory.getLog(null).info("=======================================");
            org.graph.commons.logging.LogFactory.getLog(null).info("== READING DIAGRAM: " + url);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            initDiagram("org.tigris.gef.base.Diagram");
            _figRegistry = new HashMap();
            SAXParser pc = factory.newSAXParser();
            InputSource source = new InputSource(is);
            source.setSystemId(url.toString());
            pc.parse(source, this);
            source = null;
            is.close();
            return _diagram;
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("Exception in readDiagram");
            ex.printStackTrace();
        }
        return null;
    }

    public InputSource resolveEntity(java.lang.String publicId,
            java.lang.String systemId) {
        InputSource source = null;
        try {
            java.net.URL url = new java.net.URL(systemId);
            try {
                source = new InputSource(url.openStream());
                source.setSystemId(systemId);
                if (publicId != null) {
                    source.setPublicId(publicId);
                }
            } catch (java.io.IOException e) {
                if (systemId.endsWith(".dtd")) {
                    int i = systemId.lastIndexOf('/');
                    i++; // go past '/' if there, otherwise advance to 0
                    String[] entityPaths = getEntityPaths();
                    InputStream is = null;
                    for (int pathIndex = 0; pathIndex < entityPaths.length
                            && is == null; pathIndex++) {
                        String DTD_DIR = entityPaths[pathIndex];
                        is = getClass().getResourceAsStream(
                                DTD_DIR + systemId.substring(i));
                        if (is == null) {
                            try {
                                is = new FileInputStream(DTD_DIR.substring(1)
                                        + systemId.substring(i));
                            } catch (Exception ex) {
                            }
                        }
                    }
                    if (is != null) {
                        source = new InputSource(is);
                        source.setSystemId(systemId);
                        if (publicId != null) {
                            source.setPublicId(publicId);
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }

        //
        // returning an "empty" source is better than failing
        //
        if (source == null) {
            source = new InputSource();
            source.setSystemId(systemId);
        }
        return source;
    }

    // //////////////////////////////////////////////////////////////
    // internal parsing methods
    protected void setAttrs(Fig f, Attributes attrList) {
        String name = attrList.getValue("id");
        if (name != null && !name.equals("")) {
            _figRegistry.put(name, f);
        }
        String x = attrList.getValue("x");
        if (x != null && !x.equals("")) {
            String y = attrList.getValue("y");
            String w = attrList.getValue("width");
            String h = attrList.getValue("height");
            int xInt = Integer.parseInt(x);
            int yInt = (y == null || y.equals("")) ? 0 : Integer.parseInt(y);
            int wInt = (w == null || w.equals("")) ? 20 : Integer.parseInt(w);
            int hInt = (h == null || h.equals("")) ? 20 : Integer.parseInt(h);
            f.setBounds(xInt, yInt, wInt, hInt);
        }

        // Parse Style
        String style = attrList.getValue("style");
        if (style != null) {
            String fillcolor = parseStyle("fill", style);
            // org.graph.commons.logging.LogFactory.getLog(null).info("[SVGParser] setAttrs: fillcolor = " +
            // fillcolor);
            if (fillcolor != null && !fillcolor.equals("")) {
                f.setFillColor(parseColor(fillcolor, Color.blue));
            }

            if (!(f instanceof FigNode)) {
                String linewidth = parseStyle("stroke-width", style);
                if (linewidth != null && !linewidth.equals("")) {
                    f.setLineWidth(Integer.parseInt(linewidth));
                }
                String strokecolor = parseStyle("stroke", style);
                if (strokecolor != null && !strokecolor.equals("")) {
                    f.setLineColor(parseColor(strokecolor, Color.blue));
                }

                // String fill = e.getAttribute("fill-texture");
                // if (fill != null && !fill.equals(""))
                // f.setFilled(fill.equals("1") || fill.startsWith("t"));
                String dasharray = parseStyle("stroke-dash-array", style);
                if (dasharray != null && !dasharray.equals("")
                        && !dasharray.equals("1")) {
                    f.setDashed(true);
                }
            }
        }

        setOwnerAttr(f, attrList);
    }

    protected void setOwnerAttr(Fig f, Attributes attrList) {
        try {
            String owner = attrList.getValue("href");
            if (owner != null && !owner.equals("")) {
                f.setOwner(findOwner(owner));
            }
        } catch (Exception ex) {
            org.graph.commons.logging.LogFactory.getLog(null).info("could not set owner");
        }
    }

    // //////////////////////////////////////////////////////////////
    // accessors
    public void setOwnerRegistry(Map owners) {
        _ownerRegistry = owners;
    }

    public void startElement(String elementName, Attributes attrList) {
        switch (_elementState) {
            case DEFAULT_STATE:
                if ("g".equals(elementName)) {
                    _nestedGroups++;
                    _diagram.add(handleGroup(attrList));
                } else if (elementName.equals("svg")) {
                    handleSVG(attrList);
                } else if (_nestedGroups == 0) {
                    if (elementName.equals("path")) {
                        _diagram.add(handlePolyLine(attrList));
                    } else if (elementName.equals("ellipse")) {
                        _diagram.add(handleEllipse(attrList));
                    } else if (elementName.equals("rect")) {
                        _diagram.add(handleRect(attrList));
                    } else if (elementName.equals("text")) {
                        _elementState = TEXT_STATE;
                        _diagram.add(handleText(attrList));
                    } else if (elementName.equals("line")) {

                    } else if (elementName.equals("line")) { /*
                         * just gets rid of
                         * the error msgs
                         */

                    } else if (elementName.equals("path")) { /*
                         * just gets rid of
                         * the error msgs
                         */

                    } else {
                        org.graph.commons.logging.LogFactory.getLog(null).info("unknown top-level tag: " + elementName);
                    }
                } else if (_nestedGroups > 0) {
                    // org.graph.commons.logging.LogFactory.getLog(null).info("skipping nested " + elementName);
                }
                break;

            case LINE_STATE:
                lineStateStartElement(elementName, attrList);
                break;

            case POLY_STATE:
                polyStateStartElement(elementName, attrList);
                break;

            case NODE_STATE:
                nodeStateStartElement(elementName, attrList);
                break;

            case EDGE_STATE:
                edgeStateStartElement(elementName, attrList);
                break;
        }
    }

    protected String translateClassName(String oldName) {
        return oldName;
    }
} /* end class SVGParser */
