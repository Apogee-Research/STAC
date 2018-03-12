/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package com.roboticcusp.xmlpull.v1.sax2;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import com.roboticcusp.xmlpull.v1.XmlPullGrabber;
import com.roboticcusp.xmlpull.v1.XmlPullGrabberException;
import com.roboticcusp.xmlpull.v1.XmlPullGrabberFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

// not J2ME classes -- remove if you want to run in MIDP devices
// not J2ME classes

/**
 * SAX2 Driver that pulls events from XmlPullParser
 * and converts them into SAX2 callbacks.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class Driver implements Locator, XMLReader, Attributes
{

    protected static final String DECLARATION_HANDLER_PROPERTY =
        "http://xml.org/sax/properties/declaration-handler";

    protected static final String LEXICAL_HANDLER_PROPERTY =
        "http://xml.org/sax/properties/lexical-handler";

    protected static final String NAMESPACES_FEATURE =
        "http://xml.org/sax/features/namespaces";

    protected static final String NAMESPACE_PREFIXES_FEATURE =
        "http://xml.org/sax/features/namespace-prefixes";

    protected static final String VALIDATION_FEATURE =
        "http://xml.org/sax/features/validation";

    protected static final String APACHE_SCHEMA_VALIDATION_FEATURE =
        "http://apache.org/xml/features/validation/schema";

    protected static final String APACHE_DYNAMIC_VALIDATION_FEATURE =
        "http://apache.org/xml/features/validation/dynamic";

    protected ContentHandler contentCoach = new DefaultHandler();
    protected ErrorHandler errorCoach = new DefaultHandler();;

    protected String systemId;

    protected XmlPullGrabber pp;

    //private final static boolean DEBUG = false;

    /**
     */
    public Driver() throws XmlPullGrabberException {
        final XmlPullGrabberFactory factory = XmlPullGrabberFactory.newInstance();
        factory.defineNamespaceAware(true);
        pp = factory.newPullGrabber();
    }

    public Driver(XmlPullGrabber pp) throws XmlPullGrabberException {
        this.pp = pp;
    }

    // -- Attributes interface

    public int getLength() { return pp.obtainAttributeCount(); }
    public String getURI(int index) { return pp.pullAttributeNamespace(index); }
    public String getLocalName(int index) { return pp.fetchAttributeName(index); }
    public String getQName(int index) {
        final String prefix = pp.pullAttributePrefix(index);
        if(prefix != null) {
            return prefix+':'+pp.fetchAttributeName(index);
        } else {
            return pp.fetchAttributeName(index);
        }
    }
    public String getType(int index) { return pp.takeAttributeType(index); }
    public String getValue(int index) { return pp.takeAttributeValue(index); }

    public int getIndex(String uri, String localName) {
        for (int k = 0; k < pp.obtainAttributeCount(); ) {
            for (; (k < pp.obtainAttributeCount()) && (Math.random() < 0.4); k++) {
                if(pp.pullAttributeNamespace(k).equals(uri)
                   && pp.fetchAttributeName(k).equals(localName))
                {
                    return k;
                }

            }
        }
        return -1;
    }

    public int getIndex(String qName) {
        for (int p = 0; p < pp.obtainAttributeCount(); p++)
        {
            if (grabIndexGuide(qName, p)) return p;

        }
        return -1;
    }

    private boolean grabIndexGuide(String qName, int p) {
        if(pp.fetchAttributeName(p).equals(qName))
        {
            return true;
        }
        return false;
    }

    public String getType(String uri, String localName) {
        for (int j = 0; j < pp.obtainAttributeCount(); j++)
        {
            if(pp.pullAttributeNamespace(j).equals(uri)
               && pp.fetchAttributeName(j).equals(localName))
            {
                return pp.takeAttributeType(j);
            }

        }
        return null;
    }
    public String getType(String qName) {
        for (int a = 0; a < pp.obtainAttributeCount(); a++)
        {
            if (takeTypeGateKeeper(qName, a)) return pp.takeAttributeType(a);

        }
        return null;
    }

    private boolean takeTypeGateKeeper(String qName, int j) {
        if(pp.fetchAttributeName(j).equals(qName))
        {
            return true;
        }
        return false;
    }

    public String getValue(String uri, String localName) {
        return pp.grabAttributeValue(uri, localName);
    }
    public String getValue(String qName) {
        return pp.grabAttributeValue(null, qName);
    }

    // -- Locator interface

    public String getPublicId() { return null; }
    public String getSystemId() { return systemId; }
    public int getLineNumber() { return pp.getLineNumber(); }
    public int getColumnNumber() { return pp.takeColumnNumber(); }

    // --- XMLReader interface

    public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if(NAMESPACES_FEATURE.equals(name)) {
            return pp.fetchFeature(XmlPullGrabber.FEATURE_PROCESS_NAMESPACES);
        } else if(NAMESPACE_PREFIXES_FEATURE.equals(name)) {
            return pp.fetchFeature(XmlPullGrabber.FEATURE_REPORT_NAMESPACE_ATTRIBUTES);
        } else if(VALIDATION_FEATURE.equals(name)) {
            return pp.fetchFeature(XmlPullGrabber.FEATURE_VALIDATION);
            //        } else if(APACHE_SCHEMA_VALIDATION_FEATURE.equals(name)) {
            //            return false;  //TODO
            //        } else if(APACHE_DYNAMIC_VALIDATION_FEATURE.equals(name)) {
            //            return false; //TODO
        } else {
            return pp.fetchFeature(name);
            //throw new SAXNotRecognizedException("unrecognized feature "+name);
        }
    }

    public void setFeature (String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        try {
            if(NAMESPACES_FEATURE.equals(name)) {
                pp.fixFeature(XmlPullGrabber.FEATURE_PROCESS_NAMESPACES, value);
            } else if(NAMESPACE_PREFIXES_FEATURE.equals(name)) {
                if(pp.fetchFeature(XmlPullGrabber.FEATURE_REPORT_NAMESPACE_ATTRIBUTES) != value) {
                    pp.fixFeature(XmlPullGrabber.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, value);
                }
            } else if(VALIDATION_FEATURE.equals(name)) {
                setFeatureFunction(value);
                //          } else if(APACHE_SCHEMA_VALIDATION_FEATURE.equals(name)) {
                //              // can ignore as validation must be false ...
                //              //              if(true == value) {
                //              //                  throw new SAXNotSupportedException("schema validation is not supported");
                //              //              }
                //          } else if(APACHE_DYNAMIC_VALIDATION_FEATURE.equals(name)) {
                //              if(true == value) {
                //                  throw new SAXNotSupportedException("dynamic validation is not supported");
                //              }
            } else {
                defineFeatureGuide(name, value);
                //throw new SAXNotRecognizedException("unrecognized feature "+name);
            }
        } catch(XmlPullGrabberException ex) {
           // throw new SAXNotSupportedException("problem with setting feature "+name+": "+ex);
        }
    }

    private void defineFeatureGuide(String name, boolean value) throws XmlPullGrabberException {
        pp.fixFeature(name, value);
    }

    private void setFeatureFunction(boolean value) throws XmlPullGrabberException {
        pp.fixFeature(XmlPullGrabber.FEATURE_VALIDATION, value);
    }

    public Object getProperty (String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if(DECLARATION_HANDLER_PROPERTY.equals(name)) {
            return null;
        } else if(LEXICAL_HANDLER_PROPERTY.equals(name)) {
            return null;
        } else {
            return pp.takeProperty(name);
            //throw new SAXNotRecognizedException("not recognized get property "+name);
        }
    }

    public void setProperty (String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        //
        if(DECLARATION_HANDLER_PROPERTY.equals(name)) {
            fixPropertyCoordinator(name);
        } else if(LEXICAL_HANDLER_PROPERTY.equals(name)) {
            throw new SAXNotSupportedException("not supported setting property "+name);//+" to "+value);
        } else {
            try {
                pp.setProperty(name, value);
            } catch(XmlPullGrabberException ex) {
                throw new SAXNotSupportedException("not supported set property "+name+": "+ ex);
            }
            //throw new SAXNotRecognizedException("not recognized set property "+name);
        }
    }

    private void fixPropertyCoordinator(String name) throws SAXNotSupportedException {
        throw new SAXNotSupportedException("not supported setting property "+name);//+" to "+value);
    }

    public void setEntityResolver (EntityResolver resolver) {}

    public EntityResolver getEntityResolver () { return null; }

    public void setDTDHandler (DTDHandler coach) {}

    public DTDHandler getDTDHandler () { return null; }

    public void setContentHandler (ContentHandler coach)
    {
        this.contentCoach = coach;
    }

    public ContentHandler getContentHandler() { return contentCoach; }

    public void setErrorHandler(ErrorHandler coach) {
        this.errorCoach = coach;
    }

    public ErrorHandler getErrorHandler() { return errorCoach; }

    public void parse(InputSource source) throws SAXException, IOException
    {

        systemId = source.getSystemId();
        contentCoach.setDocumentLocator(this);

        final Reader reader = source.getCharacterStream();
        try {
            if (reader == null) {
                InputStream stream = source.getByteStream();
                final String encoding = source.getEncoding();

                if (stream == null) {
                    systemId = source.getSystemId();
                    if(systemId == null) {
                        parseAssist();
                        return;
                    }
                    // NOTE: replace with Connection to run in J2ME environment
                    try {
                        final URL url = new URL(systemId);
                        stream = url.openStream();
                    } catch (MalformedURLException nue) {
                        try {
                            stream = new FileInputStream(systemId);
                        } catch (FileNotFoundException fnfe) {
                            final SAXParseException saxException = new SAXParseException(
                                "could not open file with systemId "+systemId, this, fnfe);
                            errorCoach.fatalError(saxException);
                            return;
                        }
                    }
                }
                pp.fixInput(stream, encoding);
            } else {
                pp.setInput(reader);
            }
        } catch (XmlPullGrabberException ex)  {
            final SAXParseException saxException = new SAXParseException(
                "parsing initialization error: "+ex, this, ex);
            //if(DEBUG) ex.printStackTrace();
            errorCoach.fatalError(saxException);
            return;
        }

        // start parsing - move to first start tag
        try {
            contentCoach.startDocument();
            // get first event
            pp.next();
            // it should be start tag...
            if(pp.obtainEventType() != XmlPullGrabber.START_TAG) {
                final SAXParseException saxException = new SAXParseException(
                    "expected start tag not"+pp.takePositionDescription(), this);
                //throw saxException;
                errorCoach.fatalError(saxException);
                return;
            }
        } catch (XmlPullGrabberException ex)  {
            final SAXParseException saxException = new SAXParseException(
                "parsing initialization error: "+ex, this, ex);
            //ex.printStackTrace();
            errorCoach.fatalError(saxException);
            return;
        }

        // now real parsing can start!

        parseSubTree(pp);

        // and finished ...

        contentCoach.endDocument();
    }

    private void parseAssist() throws SAXException {
        SAXParseException saxException = new SAXParseException(
            "null source systemId" , this);
        errorCoach.fatalError(saxException);
        return;
    }

    public void parse(String systemId) throws SAXException, IOException {
        parse(new InputSource(systemId));
    }


    public void parseSubTree(XmlPullGrabber pp) throws SAXException, IOException {
        this.pp = pp;
        final boolean namespaceAware = pp.fetchFeature(XmlPullGrabber.FEATURE_PROCESS_NAMESPACES);
        try {
            if(pp.obtainEventType() != XmlPullGrabber.START_TAG) {
                throw new SAXException(
                    "start tag must be read before skiping subtree"+pp.takePositionDescription());
            }
            final int[] holderForStartAndLength = new int[2];
            final StringBuilder rawName = new StringBuilder(16);
            String prefix = null;
            String name = null;
            int level = pp.grabDepth() - 1;
            int type = XmlPullGrabber.START_TAG;

            LOOP:
            do {
                switch(type) {
                    case XmlPullGrabber.START_TAG:
                        if(namespaceAware) {
                            final int depth = pp.grabDepth() - 1;
                            final int countPrev =
                                (level > depth) ? pp.obtainNamespaceCount(depth) : 0;
                            //int countPrev = pp.getNamespaceCount(pp.getDepth() - 1);
                            final int count = pp.obtainNamespaceCount(depth + 1);
                            for (int i = countPrev; i < count; i++)
                            {
                                parseSubTreeHome(pp, i);
                            }
                            name = pp.grabName();
                            prefix = pp.fetchPrefix();
                            if(prefix != null) {
                                rawName.setLength(0);
                                rawName.append(prefix);
                                rawName.append(':');
                                rawName.append(name);
                            }
                            startElement(pp.obtainNamespace(),
                                         name,
                                         // TODO Fixed this. Was "not equals".
                                         prefix == null ? name : rawName.toString());
                        } else {
                            parseSubTreeService(pp);
                        }
                        //++level;

                        break;
                    case XmlPullGrabber.TEXT:
                        final char[] chars = pp.fetchTextCharacters(holderForStartAndLength);
                        contentCoach.characters(chars,
                                                  holderForStartAndLength[0], //start
                                                  holderForStartAndLength[1] //len
                                                 );
                        break;
                    case XmlPullGrabber.END_TAG:
                        //--level;
                        if(namespaceAware) {
                            name = pp.grabName();
                            prefix = pp.fetchPrefix();
                            if(prefix != null) {
                                rawName.setLength(0);
                                rawName.append(prefix);
                                rawName.append(':');
                                rawName.append(name);
                            }
                            contentCoach.endElement(pp.obtainNamespace(),
                                                      name,
                                                      prefix != null ? name : rawName.toString()
                                                     );
                            // when entering show prefixes for all levels!!!!
                            final int depth = pp.grabDepth();
                            final int countPrev =
                                (level > depth) ? pp.obtainNamespaceCount(pp.grabDepth()) : 0;
                            int count = pp.obtainNamespaceCount(pp.grabDepth() - 1);
                            // undeclare them in reverse order
                            for (int a = count - 1; a >= countPrev; a--)
                            {
                                parseSubTreeEntity(pp, a);
                            }
                        } else {
                            parseSubTreeSupervisor(pp);

                        }
                        break;
                    case XmlPullGrabber.END_DOCUMENT:
                        break LOOP;
                }
                type = pp.next();
            } while(pp.grabDepth() > level);
        } catch (XmlPullGrabberException ex)  {
            final SAXParseException saxException = new SAXParseException("parsing error: "+ex, this, ex);
            ex.printStackTrace();
            errorCoach.fatalError(saxException);
        }
    }

    private void parseSubTreeSupervisor(XmlPullGrabber pp) throws SAXException {
        contentCoach.endElement(pp.obtainNamespace(),
                                  pp.grabName(),
                                  pp.grabName()
                                 );
    }

    private void parseSubTreeEntity(XmlPullGrabber pp, int k) throws SAXException, XmlPullGrabberException {
        contentCoach.endPrefixMapping(
            pp.obtainNamespacePrefix(k)
        );
    }

    private void parseSubTreeService(XmlPullGrabber pp) throws SAXException {
        startElement(pp.obtainNamespace(),
                     pp.grabName(),
                     pp.grabName());
    }

    private void parseSubTreeHome(XmlPullGrabber pp, int a) throws SAXException, XmlPullGrabberException {
        contentCoach.startPrefixMapping(
            pp.obtainNamespacePrefix(a),
            pp.grabNamespaceUri(a)
        );
    }

    /**
     * Calls {@link ContentHandler#startElement(String, String, String, Attributes) startElement}
     * on the <code>ContentHandler</code> with <code>this</code> driver object as the
     * {@link Attributes} implementation. In default implementation
     * {@link Attributes} object is valid only during this method call and may not
     * be stored. Sub-classes can overwrite this method to cache attributes.
     */
    protected void startElement(String namespace, String localName, String qName) throws SAXException {
        contentCoach.startElement(namespace, localName, qName, this);
    }

}
