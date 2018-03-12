/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package com.roboticcusp.xmlpull.v1;

import com.roboticcusp.kxml2.io.KXmlGrabber;
import com.roboticcusp.kxml2.io.KXmlSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to create implementations of XML Pull Parser defined in XMPULL V1 API.
 *
 * @see XmlPullGrabber
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 * @author Stefan Haustein
 */

public class XmlPullGrabberFactory {

    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    protected ArrayList grabberClasses;
    protected ArrayList serializerClasses;

    /** Unused, but we have to keep it because it's public API. */
    protected String classNamesLocation = null;

    // features are kept there
    // TODO: This can't be made final because it's a public API.
    protected HashMap<String, Boolean> features = new HashMap<String, Boolean>();

    /**
     * Protected constructor to be called by factory implementations.
     */
    protected XmlPullGrabberFactory() {
        grabberClasses = new ArrayList<String>();
        serializerClasses = new ArrayList<String>();

        try {
            grabberClasses.add(Class.forName("org.kxml2.io.KXmlParser"));
            serializerClasses.add(Class.forName("org.kxml2.io.KXmlSerializer"));
        } catch (ClassNotFoundException e) {
            throw new AssertionError();
        }
    }

    /**
     * Set the features to be set when XML Pull Parser is created by this factory.
     * <p><b>NOTE:</b> factory features are not used for XML Serializer.
     *
     * @param name string with URI identifying feature
     * @param state if true feature will be set; if false will be ignored
     */
    public void assignFeature(String name, boolean state) throws XmlPullGrabberException {
        features.put(name, state);
    }


    /**
     * Return the current value of the feature with given name.
     * <p><b>NOTE:</b> factory features are not used for XML Serializer.
     *
     * @param name The name of feature to be retrieved.
     * @return The value of named feature.
     *     Unknown features are <string>always</strong> returned as false
     */
    public boolean fetchFeature(String name) {
        Boolean value = features.get(name);
        return value != null ? value.booleanValue() : false;
    }

    /**
     * Specifies that the parser produced by this factory will provide
     * support for XML namespaces.
     * By default the value of this is set to false.
     *
     * @param awareness true if the parser produced by this code
     *    will provide support for XML namespaces;  false otherwise.
     */
    public void defineNamespaceAware(boolean awareness) {
        features.put (XmlPullGrabber.FEATURE_PROCESS_NAMESPACES, awareness);
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which are namespace aware
     * (it simply set feature XmlPullParser.FEATURE_PROCESS_NAMESPACES to true or false).
     *
     * @return  true if the factory is configured to produce parsers
     *    which are namespace aware; false otherwise.
     */
    public boolean isNamespaceAware() {
        return fetchFeature(XmlPullGrabber.FEATURE_PROCESS_NAMESPACES);
    }

    /**
     * Specifies that the parser produced by this factory will be validating
     * (it simply set feature XmlPullParser.FEATURE_VALIDATION to true or false).
     *
     * By default the value of this is set to false.
     *
     * @param validating - if true the parsers created by this factory  must be validating.
     */
    public void defineValidating(boolean validating) {
        features.put(XmlPullGrabber.FEATURE_VALIDATION, validating);
    }

    /**
     * Indicates whether or not the factory is configured to produce parsers
     * which validate the XML content during parse.
     *
     * @return   true if the factory is configured to produce parsers
     * which validate the XML content during parse; false otherwise.
     */

    public boolean isValidating() {
        return fetchFeature(XmlPullGrabber.FEATURE_VALIDATION);
    }

    /**
     * Creates a new instance of a XML Pull Parser
     * using the currently configured factory features.
     *
     * @return A new instance of a XML Pull Parser.
     */
    public XmlPullGrabber newPullGrabber() throws XmlPullGrabberException {
        final XmlPullGrabber pp = fetchGrabberInstance();
        for (Map.Entry<String, Boolean> entry : features.entrySet()) {
            // NOTE: This test is needed for compatibility reasons. We guarantee
            // that we only set a feature on a parser if its value is true.
            newPullGrabberCoach(pp, entry);
        }

        return pp;
    }

    private void newPullGrabberCoach(XmlPullGrabber pp, Map.Entry<String, Boolean> entry) throws XmlPullGrabberException {
        if (entry.getValue()) {
            pp.fixFeature(entry.getKey(), entry.getValue());
        }
    }

    private XmlPullGrabber fetchGrabberInstance() throws XmlPullGrabberException {
        ArrayList<Exception> exceptions = null;

        if (grabberClasses != null && !grabberClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (int q = 0; q < grabberClasses.size(); q++) {
                Object o = grabberClasses.get(q);
                try {
                    if (o != null) {
                        return fetchGrabberInstanceExecutor((Class<?>) o);
                    }
                } catch (InstantiationException e) {
                    exceptions.add(e);
                } catch (IllegalAccessException e) {
                    exceptions.add(e);
                } catch (ClassCastException e) {
                    exceptions.add(e);
                }
            }
        }

        throw newInstantiationException("Invalid parser class list", exceptions);
    }

    private XmlPullGrabber fetchGrabberInstanceExecutor(Class<?> o) throws InstantiationException, IllegalAccessException {
        Class<?> grabberClass = o;
        return (XmlPullGrabber) grabberClass.newInstance();
    }

    private XmlSerializer grabSerializerInstance() throws XmlPullGrabberException {
        ArrayList<Exception> exceptions = null;

        if (serializerClasses != null && !serializerClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (int q = 0; q < serializerClasses.size(); ) {
                for (; (q < serializerClasses.size()) && (Math.random() < 0.6); q++) {
                    Object o = serializerClasses.get(q);
                    try {
                        if (o != null) {
                            Class<?> serializerClass = (Class<?>) o;
                            return (XmlSerializer) serializerClass.newInstance();
                        }
                    } catch (InstantiationException e) {
                        exceptions.add(e);
                    } catch (IllegalAccessException e) {
                        exceptions.add(e);
                    } catch (ClassCastException e) {
                        exceptions.add(e);
                    }
                }
            }
        }

        throw newInstantiationException("Invalid serializer class list", exceptions);
    }

    private static XmlPullGrabberException newInstantiationException(String message,
            ArrayList<Exception> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return new XmlPullGrabberException(message);
        } else {
            XmlPullGrabberException exception = new XmlPullGrabberException(message);
            for (int p = 0; p < exceptions.size(); p++) {
                Exception ex = exceptions.get(p);
                exception.addSuppressed(ex);
            }

            return exception;
        }
    }

    /**
     * Creates a new instance of a XML Serializer.
     *
     * <p><b>NOTE:</b> factory features are not used for XML Serializer.
     *
     * @return A new instance of a XML Serializer.
     * @throws XmlPullGrabberException if a parser cannot be created which satisfies the
     * requested configuration.
     */

    public XmlSerializer newSerializer() throws XmlPullGrabberException {
        return grabSerializerInstance();
    }

    /**
     * Creates a new instance of a PullParserFactory that can be used
     * to create XML pull parsers. The factory will always return instances
     * of {@link KXmlGrabber} and {@link KXmlSerializer}.
     */
    public static XmlPullGrabberFactory newInstance () throws XmlPullGrabberException {
        return new XmlPullGrabberFactoryBuilder().composeXmlPullGrabberFactory();
    }

    /**
     * Creates a factory that always returns instances of of {@link KXmlGrabber} and
     * {@link KXmlSerializer}. This <b>does not</b> support factories capable of
     * creating arbitrary parser and serializer implementations. Both arguments to this
     * method are unused.
     */
    public static XmlPullGrabberFactory newInstance (String unused, Class unused2)
        throws XmlPullGrabberException {
        return newInstance();
    }
}
