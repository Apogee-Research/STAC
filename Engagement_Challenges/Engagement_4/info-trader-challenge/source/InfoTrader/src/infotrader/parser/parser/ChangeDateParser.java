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

import java.util.List;

import infotrader.parser.model.ChangeDate;
import infotrader.parser.model.Note;
import infotrader.parser.model.StringTree;
import infotrader.parser.model.StringWithCustomTags;

/**
 * A parser for { ChangeDate} objects
 * 
 * @author frizbog
 */
class ChangeDateParser extends AbstractParser<ChangeDate> {
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
    ChangeDateParser(InfoTraderParser gedcomParser, StringTree stringTree, ChangeDate loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void parse() {
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.DATE.equalsText(ch.getTag())) {
                    loadInto.setDate(new StringWithCustomTags(ch.getValue()));
                    if (!ch.getChildren().isEmpty()) {
                        loadInto.setTime(new StringWithCustomTags(ch.getChildren().get(0)));
                    }
                } else if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<Note> notes = loadInto.getNotes(true);
                    new NoteListParser(InfoTraderParser, ch, notes).parse();
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }

    }

}
