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

import infotrader.parser.model.CharacterSet;
import infotrader.parser.model.SourceSystem;
import infotrader.parser.model.StringWithCustomTags;
import infotrader.parser.model.Header;
import infotrader.parser.model.StringTree;

/**
 * A parser for { Header} objects
 * 
 * @author frizbog
 */
class HeaderParser extends AbstractParser<Header> {

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
    HeaderParser(InfoTraderParser gedcomParser, StringTree stringTree, Header loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    @Override
    void parse() {
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.SOURCE.equalsText(ch.getTag())) {
                    SourceSystem sourceSystem = new SourceSystem();
                    loadInto.setSourceSystem(sourceSystem);
                    new SourceSystemParser(InfoTraderParser, ch, sourceSystem).parse();
                } else if (Tag.DATE.equalsText(ch.getTag())) {
                    loadInto.setDate(new StringWithCustomTags(ch));
                    // one optional time subitem is the only possibility here
                    if (ch.getChildren() != null && !ch.getChildren().isEmpty()) {
                        loadInto.setTime(new StringWithCustomTags(ch.getChildren().get(0)));
                    }
                } else if (Tag.CHARACTER_SET.equalsText(ch.getTag())) {
                    loadInto.setCharacterSet(new CharacterSet());
                    loadInto.getCharacterSet().setCharacterSetName(new StringWithCustomTags(ch));
                    // one optional version subitem is the only possibility here
                    if (ch.getChildren() != null && !ch.getChildren().isEmpty()) {
                        loadInto.getCharacterSet().setVersionNum(new StringWithCustomTags(ch.getChildren().get(0)));
                    }
                } else if (Tag.SUBMITTER.equalsText(ch.getTag())) {
                    loadInto.setSubmitter(getSubmitter(ch.getValue()));
                } else if (Tag.FILE.equalsText(ch.getTag())) {
                    loadInto.setFileName(new StringWithCustomTags(ch));
                }  else if (Tag.LANGUAGE.equalsText(ch.getTag())) {
                    loadInto.setLanguage(new StringWithCustomTags(ch));
                } else if (Tag.PLACE.equalsText(ch.getTag())) {
                    loadInto.setPlaceHierarchy(new StringWithCustomTags(ch.getChildren().get(0)));
                } else if (Tag.NOTE.equalsText(ch.getTag())) {
                    new NoteListParser(InfoTraderParser, ch, loadInto.getNotes(true)).parse();
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }
    }
}
