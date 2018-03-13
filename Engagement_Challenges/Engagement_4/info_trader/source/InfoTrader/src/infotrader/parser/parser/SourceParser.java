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

import infotrader.parser.model.SourceData;
import infotrader.parser.model.RepositoryCitation;
import infotrader.parser.model.StringWithCustomTags;
import infotrader.parser.model.EventRecorded;
import infotrader.parser.model.SourceCallNumber;
import infotrader.parser.model.StringTree;
import infotrader.parser.model.Note;
import infotrader.parser.model.Multimedia;
import infotrader.parser.model.Source;
import java.util.List;


/**
 * Parser for { Source} objects
 * 
 * @author frizbog
 */
class SourceParser extends AbstractParser<Source> {

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
    SourceParser(InfoTraderParser gedcomParser, StringTree stringTree, Source loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    @Override
    void parse() {
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.DATA_FOR_SOURCE.equalsText(ch.getTag())) {
                    loadInto.setData(new SourceData());
                    loadSourceData(ch, loadInto.getData());
                } else if (Tag.TITLE.equalsText(ch.getTag())) {
                    loadMultiLinesOfText(ch, loadInto.getTitle(true), loadInto);
                } else if (Tag.TEXT.equalsText(ch.getTag())) {
                    loadMultiLinesOfText(ch, loadInto.getSourceText(true), loadInto);
                } else if (Tag.ABBREVIATION.equalsText(ch.getTag())) {
                    loadInto.setSourceFiledBy(new StringWithCustomTags(ch));
                } else if (Tag.AUTHORS.equalsText(ch.getTag())) {
                    loadMultiLinesOfText(ch, loadInto.getOriginatorsAuthors(true), loadInto);
                } else if (Tag.REPOSITORY.equalsText(ch.getTag())) {
                    loadInto.setRepositoryCitation(loadRepositoryCitation(ch));
                } else if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<Note> notes = loadInto.getNotes(true);
                    new NoteListParser(InfoTraderParser, ch, notes).parse();
                } else if (Tag.OBJECT_MULTIMEDIA.equalsText(ch.getTag())) {
                    List<Multimedia> multimedia = loadInto.getMultimedia(true);
                    new MultimediaLinkParser(InfoTraderParser, ch, multimedia).parse();
                }  else if (Tag.RECORD_ID_NUMBER.equalsText(ch.getTag())) {
                    loadInto.setRecIdNumber(new StringWithCustomTags(ch));
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }
    }

    /**
     * Load a reference to a repository in a source, from a string tree node
     * 
     * @param repo
     *            the node
     * @return the RepositoryCitation loaded
     */
    private RepositoryCitation loadRepositoryCitation(StringTree repo) {
        RepositoryCitation r = new RepositoryCitation();
        r.setRepositoryXref(repo.getValue());
        if (repo.getChildren() != null) {
            for (StringTree ch : repo.getChildren()) {
                if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<Note> notes = r.getNotes(true);
                    new NoteListParser(InfoTraderParser, ch, notes).parse();
                } else if (Tag.CALL_NUMBER.equalsText(ch.getTag())) {
                    SourceCallNumber scn = new SourceCallNumber();
                    r.getCallNumbers(true).add(scn);
                    scn.setCallNumber(new StringWithCustomTags(ch.getValue()));
                    if (ch.getChildren() != null) {
                        for (StringTree gch : ch.getChildren()) {
                            if (Tag.MEDIA.equalsText(gch.getTag())) {
                                scn.setMediaType(new StringWithCustomTags(gch));
                            } else {
                                unknownTag(gch, scn.getCallNumber());
                            }
                        }
                    }
                } else {
                    unknownTag(ch, r);
                }
            }
        }
        return r;
    }

    /**
     * Load data for a source from a string tree node into a source data structure
     * 
     * @param dataNode
     *            the node
     * @param sourceData
     *            the source data structure
     */
    private void loadSourceData(StringTree dataNode, SourceData sourceData) {
        if (dataNode.getChildren() != null) {
            for (StringTree ch : dataNode.getChildren()) {
                if (Tag.NOTE.equalsText(ch.getTag())) {
                    List<Note> notes = sourceData.getNotes(true);
                    new NoteListParser(InfoTraderParser, ch, notes).parse();
                } else {
                    unknownTag(ch, sourceData);
                }
            }
        }
    }

    /**
     * Load the data for a recorded event from a string tree node
     * 
     * @param dataNode
     *            the node
     * @param sourceData
     *            the source data
     */
    private void loadSourceDataEventRecorded(StringTree dataNode, SourceData sourceData) {
        EventRecorded e = new EventRecorded();
        sourceData.getEventsRecorded(true).add(e);
        e.setEventType(dataNode.getValue());
        if (dataNode.getChildren() != null) {
            for (StringTree ch : dataNode.getChildren()) {
                if (Tag.DATE.equalsText(ch.getTag())) {
                    e.setDatePeriod(new StringWithCustomTags(ch));
                } else if (Tag.PLACE.equalsText(ch.getTag())) {
                    e.setJurisdiction(new StringWithCustomTags(ch));
                } else {
                    unknownTag(ch, sourceData);
                }
            }
        }

    }

}
