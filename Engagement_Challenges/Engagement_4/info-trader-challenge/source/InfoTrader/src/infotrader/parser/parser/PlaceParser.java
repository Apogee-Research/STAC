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

import infotrader.parser.model.Place;
import infotrader.parser.model.StringWithCustomTags;
import infotrader.parser.model.StringTree;
import infotrader.parser.model.Note;
import java.util.List;


/**
 * Parser for { Place} objects
 * 
 * @author frizbog
 */
class PlaceParser extends AbstractParser<Place> {

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
    PlaceParser(InfoTraderParser gedcomParser, StringTree stringTree, Place loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    @Override
    void parse() {
        loadInto.setPlaceName(stringTree.getValue());
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.FORM.equalsText(ch.getTag())) {
                    loadInto.setPlaceFormat(new StringWithCustomTags(ch));
                }  else if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<Note> notes = loadInto.getNotes(true);
                    new NoteListParser(InfoTraderParser, ch, notes).parse();
                } else if (Tag.CONCATENATION.equalsText(ch.getTag())) {
                    loadInto.setPlaceName(loadInto.getPlaceName() + (ch.getValue() == null ? "" : ch.getValue()));
                } else if (Tag.CONTINUATION.equalsText(ch.getTag())) {
                    loadInto.setPlaceName(loadInto.getPlaceName() + "\n" + (ch.getValue() == null ? "" : ch.getValue()));
                } else if (Tag.MAP.equalsText(ch.getTag())) {
                    if (g55()) {
                        addWarning("InfoTrader version is 5.5 but a map coordinate was specified on a place on line " + ch.getLineNum()
                                + ", which is a InfoTrader 5.5.1 feature." + "  Data loaded but cannot be re-written unless GEDCOM version changes.");
                    }
                    if (ch.getChildren() != null) {
                        for (StringTree gch : ch.getChildren()) {
                            if (Tag.LATITUDE.equalsText(gch.getTag())) {
                                loadInto.setLatitude(new StringWithCustomTags(gch));
                            } else if (Tag.LONGITUDE.equalsText(gch.getTag())) {
                                loadInto.setLongitude(new StringWithCustomTags(gch));
                            } else {
                                unknownTag(gch, loadInto);
                            }
                        }
                    }
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }

    }

}
