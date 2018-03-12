/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package infotrader.parser.parser;

import infotrader.parser.model.SupportedVersion;
import infotrader.parser.model.StringWithCustomTags;
import infotrader.parser.model.Repository;
import infotrader.parser.model.AbstractElement;
import infotrader.parser.model.StringTree;
import infotrader.parser.model.Submitter;
import infotrader.parser.model.Multimedia;
import infotrader.parser.model.InfoTrader;
import infotrader.parser.model.Source;
import java.util.List;


/**
 * A base class for all Parser subclasses.
 * 
 * @author frizbog
 */
abstract class AbstractParser<T> {
    /** The { StringTree} to be parsed */
    protected final StringTree stringTree;

    /** *  a reference to the root { InfoTraderParser} */
    protected InfoTraderParser InfoTraderParser;

    /** a reference to the object we are loading data into */
    protected final T loadInto;

    AbstractParser(InfoTraderParser InfoTraderParser, StringTree stringTree, T loadInto) {
        this.InfoTraderParser = InfoTraderParser;
        this.stringTree = stringTree;
        this.loadInto = loadInto;
    }

    /**
     * Add an error to the errors collection on the root parser
     * 
     * @param string
     *            the text of the error
     */
    protected void addError(String string) {
        InfoTraderParser.getErrors().add(string);
    }

    /**
     * Add a warning to the warnings collection on the root parser
     * 
     * @param string
     *            the text of the error
     */
    protected void addWarning(String string) {
        InfoTraderParser.getWarnings().add(string);
    }

    /**
     * Returns true if and only if the Gedcom data says it is for the 5.5 standard.
     * 
     * @return true if and only if the Gedcom data says it is for the 5.5 standard.
     */
    protected final boolean g55() {
        InfoTrader g = InfoTraderParser.getInfoTrader();
        return g != null && g.getHeader() != null && g.getHeader().getInfoTraderVersion() != null && SupportedVersion.V5_5.equals(g.getHeader().getInfoTraderVersion()
                .getVersionNumber());
    }

    

    /**
     * Get a multimedia item by its xref, adding it to the gedcom collection of multimedia items if needed.
     * 
     * @param xref
     *            the xref of the multimedia item
     * @return the multimedia item with the specified xref
     */
    protected Multimedia getMultimedia(String xref) {
        Multimedia m;
        m = InfoTraderParser.getInfoTrader().getMultimedia().get(xref);
        if (m == null) {
            m = new Multimedia();
            m.setXref(xref);
            InfoTraderParser.getInfoTrader().getMultimedia().put(xref, m);
        }
        return m;
    }

    /**
     * Get a repository by its xref, adding it to the gedcom collection of repositories if needed.
     * 
     * @param xref
     *            the xref of the repository
     * @return the repository with the specified xref
     */
    protected Repository getRepository(String xref) {
        Repository r = InfoTraderParser.getInfoTrader().getRepositories().get(xref);
        if (r == null) {
            r = new Repository();
            r.setXref(xref);
            InfoTraderParser.getInfoTrader().getRepositories().put(xref, r);
        }
        return r;

    }

    /**
     * Get a source by its xref, adding it to the gedcom collection of sources if needed.
     * 
     * @param xref
     *            the xref of the source
     * @return the source with the specified xref
     */
    protected Source getSource(String xref) {
        Source src = InfoTraderParser.getInfoTrader().getSources().get(xref);
        if (src == null) {
            src = new Source(xref);
            InfoTraderParser.getInfoTrader().getSources().put(src.getXref(), src);
        }
        return src;
    }

    /**
     * Get a submitter by their xref, adding them to the gedcom collection of submitters if needed.
     * 
     * @param xref
     *            the xref of the submitter
     * @return the submitter with the specified xref
     */
    protected Submitter getSubmitter(String xref) {
        Submitter s;
        s = InfoTraderParser.getInfoTrader().getSubmitters().get(xref);
        if (s == null) {
            s = new Submitter();
            s.setName(new StringWithCustomTags("UNSPECIFIED"));
            s.setXref(xref);
            InfoTraderParser.getInfoTrader().getSubmitters().put(xref, s);
        }
        return s;
    }

    /**
     * Load multiple (continued) lines of text from a string tree node
     * 
     * @param stringTreeWithLinesOfText
     *            the node
     * @param listOfString
     *            the list of string to load into
     * @param element
     *            the parent element to which the <code>listOfString</code> belongs
     */
    protected void loadMultiLinesOfText(StringTree stringTreeWithLinesOfText, List<String> listOfString, AbstractElement element) {
        if (stringTreeWithLinesOfText.getValue() != null) {
            listOfString.add(stringTreeWithLinesOfText.getValue());
        }
        if (stringTreeWithLinesOfText.getChildren() != null) {
            for (StringTree ch : stringTreeWithLinesOfText.getChildren()) {
                if (Tag.CONTINUATION.equalsText(ch.getTag())) {
                    if (ch.getValue() == null) {
                        listOfString.add("");
                    } else {
                        listOfString.add(ch.getValue());
                    }
                } else if (Tag.CONCATENATION.equalsText(ch.getTag())) {
                    // If there's no value to concatenate, ignore it
                    if (ch.getValue() != null) {
                        if (listOfString.isEmpty()) {
                            listOfString.add(ch.getValue());
                        } else {
                            listOfString.set(listOfString.size() - 1, listOfString.get(listOfString.size() - 1) + ch.getValue());
                        }
                    }
                } else {
                    unknownTag(ch, element);
                }
            }
        }
    }

    /**
     * Returns true if the node passed in uses a cross-reference to another node
     * 
     * @param st
     *            the node
     * @return true if and only if the node passed in uses a cross-reference to another node
     */
    protected boolean referencesAnotherNode(StringTree st) {
        if (st.getValue() == null) {
            return false;
        }
        int r1 = st.getValue().indexOf('@');
        if (r1 == -1) {
            return false;
        }
        int r2 = st.getValue().indexOf('@', r1);
        return r2 > -1;
    }

    /**
     * <p>
     * Default handler for a tag that the parser was not expecting to see.
     * </p>
     * <ul>
     * <li>If the tag begins with an underscore, it is a user-defined tag, which is stored in the customTags collection
     * of the passed in element, and returns.</li>
     * <li>If { GedcomParser#isStrictCustomTags()} parsing is turned off (i.e., == false), it is treated as a
     * user-defined tag (despite the lack of beginning underscore) and treated like any other user-defined tag.</li>
     * <li>If { GedcomParser#isStrictCustomTags()} parsing is turned on (i.e., == true), it is treated as bad tag
     * and an error is logged in the { GedcomParser#getErrors()} collection.</li>
     * </ul>
     * 
     * @param node
     *            the node containing the unknown tag.
     * @param element
     *            the element that the node is part of, so if it's a custom tag, this unknown tag can be added to this
     *            node's collection of custom tags
     */
    protected void unknownTag(StringTree node, AbstractElement element) {
        if (node.getTag().length() > 0 && (node.getTag().charAt(0) == '_') || !InfoTraderParser.isStrictCustomTags()) {
            element.getCustomTags(true).add(node);
            return;
        }

        StringBuilder sb = new StringBuilder(64); // Min size = 64
        sb.append("Line ").append(node.getLineNum()).append(": Cannot handle tag ");
        sb.append(node.getTag());
        StringTree st = node;
        while (st.getParent() != null) {
            st = st.getParent();
            sb.append(", child of ").append(st.getTag() == null ? null : st.getTag());
            if (st.getId() != null) {
                sb.append(" ").append(st.getId());
            }
            sb.append(" on line ").append(st.getLineNum());
        }
        addError(sb.toString());
    }

    /**
     * Parse the string tree passed into the constructor, and load it into the object model
     */
    abstract void parse();
}
