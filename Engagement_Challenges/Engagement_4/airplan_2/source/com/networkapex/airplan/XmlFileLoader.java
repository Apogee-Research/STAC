package com.networkapex.airplan;

import com.networkapex.airplan.prototype.RouteMap;
import com.networkapex.airplan.save.AirDatabase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class XmlFileLoader implements RouteMapLoader {
    private static final int MAX_FILE_LENGTH = 30 * 1024;
    private static final String[] EXTENSIONS = new String[]{"xml"};


    /**
     * Loads a route map from an XML file into memory.
     *
     * Loads a route map of the form:
     * <route>
     *     <airport name="1" />
     *     <airport name="2" />
     *     <flight origin="1" dst="2" cost="cost" distance="distance" time="travelTime" crew="numCrewMembers"
     *     weight="weightCapacity" passengers="numPassengers" />
     * </route>
     *
     *
     * @param fileName
     * @param database
     * @return
     * @throws FileNotFoundException
     * @throws AirRaiser
     */
    @Override
    public RouteMap loadRouteMap(String fileName, AirDatabase database) throws FileNotFoundException, AirRaiser {
        try {
            File file = new File(fileName);
            if (file.length() > MAX_FILE_LENGTH) {
                return loadRouteMapHome();
            }

            XMLReader reader = XMLReaderFactory.createXMLReader();
            XmlRouteMapManager manager = new XmlRouteMapManager(database);
            reader.setContentHandler(manager);
            reader.parse(new InputSource(fileName));
            return manager.getRouteMap();
        } catch (SAXException e) {
            throw new AirRaiser(e);
        } catch (IOException e) {
            throw new AirRaiser(e);
        }
    }

    private RouteMap loadRouteMapHome() throws AirRaiser {
        throw new AirRaiser("This route map is too large for the system.");
    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}
