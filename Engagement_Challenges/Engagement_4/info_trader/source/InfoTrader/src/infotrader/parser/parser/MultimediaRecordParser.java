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

import infotrader.parser.model.StringWithCustomTags;
import infotrader.parser.model.FileReference;
import infotrader.parser.model.StringTree;
import infotrader.parser.model.Note;
import infotrader.parser.model.Multimedia;
import java.util.ArrayList;
import java.util.List;


/**
 * @author frizbog
 *
 */
class MultimediaRecordParser extends AbstractParser<Multimedia> {

    /**
     * Constructor
     * 
     * @param gedcomParser
     *            a reference to the root { InfoTraderParser}
     * @param stringTree
     *            { StringTree} to be parsed
     * @param loadInto
     *            the object we are loading data into
     */
    MultimediaRecordParser(InfoTraderParser gedcomParser, StringTree stringTree, Multimedia loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void parse() {
        int fileTagCount = 0;
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.FILE.equalsText(ch.getTag())) {
                    fileTagCount++;
                }
            }
        }
        if (fileTagCount > 0) {
            if (g55()) {
                addWarning("InfoTrader version was 5.5, but a 5.5.1-style multimedia record was found at line " + stringTree.getLineNum() + ". "
                        + "Data will be loaded, but might have problems being written until the version is for the data is changed to 5.5.1");
            }
            loadMultimediaRecord551(stringTree);
        } else {
            if (!g55()) {
                addWarning("InfoTrader version is 5.5.1, but a 5.5-style multimedia record was found at line " + stringTree.getLineNum() + ". "
                        + "Data will be loaded, but might have problems being written until the version is for the data is changed to 5.5.1");
            }
            loadMultimediaRecord55(stringTree);
        }

    }

    /**
     * Load a GEDCOM 5.5-style multimedia record (that could be referenced from another object) from a string tree node.
     * This corresponds to the MULTIMEDIA_RECORD structure in the GEDCOM 5.5 spec.
     * 
     * @param obje
     *            the OBJE node being loaded
     */
    private void loadMultimediaRecord55(StringTree obje) {
        if (obje.getChildren() == null) {
            addError("Root level multimedia record at line " + obje.getLineNum() + " had no child records");
        } else {
            for (StringTree ch : obje.getChildren()) {
                if (Tag.FORM.equalsText(ch.getTag())) {
                    loadInto.setEmbeddedMediaFormat(new StringWithCustomTags(ch));
                } else if (Tag.TITLE.equalsText(ch.getTag())) {
                    loadInto.setEmbeddedTitle(new StringWithCustomTags(ch));
                } else if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<Note> notes = loadInto.getNotes(true);
                    new NoteListParser(InfoTraderParser, ch, notes).parse();
                } else if (Tag.BLOB.equalsText(ch.getTag())) {
                    loadMultiLinesOfText(ch, loadInto.getBlob(true), loadInto);
                    if (!g55()) {
                        addWarning("InfoTrader version is 5.5.1, but a BLOB tag was found at line " + ch.getLineNum() + ". "
                                + "Data will be loaded but will not be writeable unless InfoTrader version is changed to 5.5.1");
                    }
                } else if (Tag.OBJECT_MULTIMEDIA.equalsText(ch.getTag())) {
                    List<Multimedia> continuedObjects = new ArrayList<Multimedia>();
                    new MultimediaLinkParser(InfoTraderParser, ch, continuedObjects).parse();
                    loadInto.setContinuedObject(continuedObjects.get(0));
                    if (!g55()) {
                        addWarning("InfoTrader version is 5.5.1, but a chained OBJE tag was found at line " + ch.getLineNum() + ". "
                                + "Data will be loaded but will not be writeable unless InfoTrader version is changed to 5.5.1");
                    }
                } else if (Tag.RECORD_ID_NUMBER.equalsText(ch.getTag())) {
                    loadInto.setRecIdNumber(new StringWithCustomTags(ch));
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }

    }

    /**
     * Load a GEDCOM 5.5.1-style multimedia record (that could be referenced from another object) from a string tree
     * node. This corresponds to the MULTIMEDIA_RECORD structure in the GEDCOM 5.5.1 spec.
     * 
     * @param obje
     *            the OBJE node being loaded
     */
    private void loadMultimediaRecord551(StringTree obje) {
        Multimedia m = getMultimedia(obje.getId());
        if (obje.getChildren() != null) {
            for (StringTree ch : obje.getChildren()) {
                if (Tag.FILE.equalsText(ch.getTag())) {
                    FileReference fr = new FileReference();
                    m.getFileReferences(true).add(fr);
                    new FileReference551Parser(InfoTraderParser, ch, fr).parse();
                } else if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<Note> notes = m.getNotes(true);
                    new NoteListParser(InfoTraderParser, ch, notes).parse();
                } else if (Tag.RECORD_ID_NUMBER.equalsText(ch.getTag())) {
                    m.setRecIdNumber(new StringWithCustomTags(ch));
                } else {
                    unknownTag(ch, m);
                }

            }
        }

    }

}
