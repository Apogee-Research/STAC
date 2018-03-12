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
import infotrader.parser.model.StringTree;
import infotrader.parser.model.Note;
import java.util.List;


/**
 * Parser for a list of { Note} objects
 * 
 * @author frizbog
 *
 */
class NoteListParser extends AbstractParser<List<Note>> {

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
    NoteListParser(InfoTraderParser gedcomParser, StringTree stringTree, List<Note> loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void parse() {
        Note note;
        if (stringTree.getId() == null && referencesAnotherNode(stringTree)) {
            note = getNote(stringTree.getValue());
            loadInto.add(note);
            return;
        } else if (stringTree.getId() == null) {
            note = new Note();
            loadInto.add(note);
        } else {
            if (referencesAnotherNode(stringTree)) {
                addWarning("NOTE line has both an XREF_ID (" + stringTree.getId() + ") and SUBMITTER_TEXT (" + stringTree.getValue()
                        + ") value between @ signs - treating SUBMITTER_TEXT as string, not a cross-reference");
            }
            note = getNote(stringTree.getId());
        }
        note.getLines(true).add(stringTree.getValue());
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                /*if (Tag.NOTE.equalsText(ch.getTag())) {
                    new NoteListParser(gedcomParser, ch, loadInto.(true)).parse();
                }*/
                if (Tag.CONCATENATION.equalsText(ch.getTag())) {
                    if (note.getLines().isEmpty()) {
                        note.getLines(true).add(ch.getValue());
                    } else {
                        String lastNote = note.getLines().get(note.getLines().size() - 1);
                        if (lastNote == null || lastNote.length() == 0) {
                            note.getLines().set(note.getLines().size() - 1, ch.getValue());
                        } else {
                            note.getLines().set(note.getLines().size() - 1, lastNote + ch.getValue());
                        }
                    }
                } else if (Tag.CONTINUATION.equalsText(ch.getTag())) {
                    note.getLines(true).add(ch.getValue() == null ? "" : ch.getValue());
                }  else if (Tag.RECORD_ID_NUMBER.equalsText(ch.getTag())) {
                    note.setRecIdNumber(new StringWithCustomTags(ch));
                } else {
                    unknownTag(ch, note);
                }
            }
        }
    }

    /**
     * Get a note by its xref, adding it to the gedcom collection of notes if needed.
     * 
     * @param xref
     *            the xref of the note
     * @return the note with the specified xref
     */
    private Note getNote(String xref) {
        Note note;
        note = InfoTraderParser.getInfoTrader().getNotes().get(xref);
        if (note == null) {
            note = new Note();
            note.setXref(xref);
            InfoTraderParser.getInfoTrader().getNotes().put(xref, note);
        }
        return note;
    }

}
