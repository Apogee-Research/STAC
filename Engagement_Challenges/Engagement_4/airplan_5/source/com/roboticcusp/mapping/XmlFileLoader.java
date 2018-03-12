package com.roboticcusp.mapping;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads a graph from an XML file of the form:
 * <pre>
 * {@code
 * <graph>
 *   <vertex name="1"/>
 *   <vertex name="2"/>
 *   <vertex name="3">
 *     <data>
 *       <entry key="string">value</entry>
 *     </data>
 *   </vertex>
 *   <edge src="1" dst="2" weight=".5"/>
 *   <edge src="2" dst="3" weight=".25"/>
 *   <edge src="3" dst="1" weight="7.0">
 *     <data>
 *       <entry key="string">value</entry>
 *     </data>
 *   </edge>
 * </graph>
 * }
 * </pre>
 */
public class XmlFileLoader implements ChartFileLoader {
    private static final String[] EXTENSIONS = new String[]{"xml"};

    public static void register() {
        ChartLoader.registerLoader(new XmlFileLoader());
    }

    @Override
    public Chart loadChart(String filename) throws FileNotFoundException, ChartException {
        XMLReader reader;
        try {
            reader = XMLReaderFactory.createXMLReader();
            XmlChartCoach coach = new XmlChartCoach();
            reader.setContentHandler(coach);
            reader.parse(new InputSource(filename));

            return coach.takeChart();
        } catch (SAXException e) {
            throw new ChartException(e);
        } catch (IOException e) {
            throw new ChartException(e);
        }
    }

    @Override
    public List<String> obtainExtensions() {
        return new ArrayList<>(Arrays.asList(EXTENSIONS));
    }

}
