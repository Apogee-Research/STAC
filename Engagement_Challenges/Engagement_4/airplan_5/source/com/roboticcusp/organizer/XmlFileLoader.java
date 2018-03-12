package com.roboticcusp.organizer;

import com.roboticcusp.organizer.framework.RouteMap;
import com.roboticcusp.organizer.save.AirDatabase;
import com.roboticcusp.kxml2.io.KXmlGrabber;
import com.roboticcusp.xmlpull.v1.XmlPullGrabber;
import com.roboticcusp.xmlpull.v1.XmlPullGrabberException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/***
 * Handles XML like this:
 *
 * <route>
 *     <airport name="1" />
 *     <airport name="2" />
 *     <flight origin="1" dst="2" cost="cost" distance="distance" time="travelTime" crew="numCrewMembers"
 *     weight="weightCapacity" passegers="numPassengers" />
 * </route>
 *
 */
public class XmlFileLoader implements RouteMapLoader {
    private static final int MAX_FILE_LENGTH = 30 * 1024;
    private static final String[] EXTENSIONS = new String[]{"xml"};

    @Override
    public RouteMap loadRouteMap(String fileName, AirDatabase database) throws FileNotFoundException, AirException {
        try {

            File file = new File(fileName);
            if (file.length() > MAX_FILE_LENGTH) {
                return loadRouteMapGuide();
            }

            XmlRouteMapCoach coach = new XmlRouteMapCoach(database);
            XmlPullGrabber xpp = new KXmlGrabber();
            xpp.fixFeature(XmlPullGrabber.FEATURE_PROCESS_DOCDECL, true);
            xpp.setInput(new FileReader(file));

            int eventType = xpp.obtainEventType();
            while (eventType != XmlPullGrabber.END_DOCUMENT) {
                if (eventType == XmlPullGrabber.START_TAG) {
                    coach.startElement(xpp.grabName(), xpp);
                }
                eventType = xpp.next();
            }
            return coach.pullRouteMap();
        } catch (XmlPullGrabberException e) {
            throw new AirException(e);
        } catch (IOException e) {
            throw new AirException(e);
        }
    }

    private RouteMap loadRouteMapGuide() throws AirException {
        throw new AirException("This route map is too large for the system.");
    }

    @Override
    public List<String> fetchExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}
