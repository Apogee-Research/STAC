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

import infotrader.parser.model.Address;
import infotrader.parser.model.Corporation;
import infotrader.parser.model.StringTree;
import infotrader.parser.model.StringWithCustomTags;

/**
 * Parser for { Corporation} objects
 * 
 * @author frizbog
 */
class CorporationParser extends AbstractParser<Corporation> {

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
    CorporationParser(InfoTraderParser gedcomParser, StringTree stringTree, Corporation loadInto) {
        super(gedcomParser, stringTree, loadInto);
    }

    @Override
    void parse() {
        loadInto.setBusinessName(stringTree.getValue());
        if (stringTree.getChildren() != null) {
            for (StringTree ch : stringTree.getChildren()) {
                if (Tag.ADDRESS.equalsText(ch.getTag())) {
                    Address address = new Address();
                    loadInto.setAddress(address);
                    new AddressParser(InfoTraderParser, ch, address).parse();
                } else if (Tag.PHONE.equalsText(ch.getTag())) {
                    loadInto.getPhoneNumbers(true).add(new StringWithCustomTags(ch));
                } else if (Tag.WEB_ADDRESS.equalsText(ch.getTag())) {
                    loadInto.getWwwUrls(true).add(new StringWithCustomTags(ch));
                    if (g55()) {
                        addWarning("InfoTrader version is 5.5 but WWW URL was specified for the corporation in the source system on line " + ch.getLineNum()
                                + ", which is a InfoTrader 5.5.1 feature." + "  Data loaded but cannot be re-written unless GEDCOM version changes.");
                    }
                }  else if (Tag.EMAIL.equalsText(ch.getTag())) {
                    loadInto.getEmails(true).add(new StringWithCustomTags(ch));
                    if (g55()) {
                        addWarning("InfoTrader version is 5.5 but emails was specified for the corporation in the source system on line " + ch.getLineNum()
                                + ", which is a InfoTrader 5.5.1 feature." + "  Data loaded but cannot be re-written unless GEDCOM version changes.");
                    }
                } else {
                    unknownTag(ch, loadInto);
                }
            }
        }
    }

}
