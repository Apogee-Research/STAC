package net.cybertip.routing;

import net.cybertip.routing.framework.RouteMap;
import net.cybertip.routing.keep.AirDatabase;
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
     * @throws AirTrouble
     */
    @Override
    public RouteMap loadRouteMap(String fileName, AirDatabase database) throws FileNotFoundException, AirTrouble {
        try {
            File file = new File(fileName);
            if (file.length() > MAX_FILE_LENGTH) {
                throw new AirTrouble("This route map is too large for the system.");
            }

            XMLReader reader = XMLReaderFactory.createXMLReader();
            XmlRouteMapCoach coach = new XmlRouteMapCoach(database);
            reader.setContentHandler(coach);
            reader.parse(new InputSource(fileName));
            return coach.pullRouteMap();
        } catch (SAXException e) {
            throw new AirTrouble(e);
        } catch (IOException e) {
            throw new AirTrouble(e);
        }
    }

    @Override
    public List<String> takeExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}
