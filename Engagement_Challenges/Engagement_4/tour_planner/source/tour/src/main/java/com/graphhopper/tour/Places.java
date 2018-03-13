package com.graphhopper.tour;

import com.graphhopper.reader.OSMElement;
import com.graphhopper.reader.OSMInputFile;
import com.graphhopper.reader.OSMNode;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.GHPlace;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import javax.xml.stream.XMLStreamException;

/**
 * Static utility methods for dealing with collections of {@link GHPlace}.
 *
 * This class is mostly used to read and write CSV files of named places and their
 * geographic locations -- in particular, the list of places in the header of a
 * {@link Matrix} CSV file.
 *
 * It also contains a method `readOsm()` that reads an OpenStreetMap file and
 * extracts named places from it so they can be written back as a CSV file.
  */
public class Places
{
    private static final Logger logger = LoggerFactory.getLogger(Places.class);

    private Places()
    {
    }

    public static List<String> names( List<? extends GHPlace> places )
    {
        List<String> names = new ArrayList<>(places.size());
        for (GHPlace p : places)
        {
            names.add(p.getName());
        }

        return names;
    }

    public static <P extends GHPlace> Map<String, P> nameIndex( List<P> places )
    {
        Map<String, P> index = new HashMap<>();
        for (P p : places)
        {
            index.put(p.getName(), p);
        }

        return index;
    }

    public static <P extends GHPlace> List<P> selectByName( Map<String, P> index, List<String> names )
    {
        List<P> filtered = new ArrayList<>(names.size());
        for (String name : names)
        {
            P place = index.get(name);
            if (place == null)
                throw new IllegalArgumentException("Could not find place \"" + name + "\"");
            filtered.add(place);
        }

        return filtered;
    }

    public static <P extends GHPlace> List<P> selectByName( List<P> places, List<String> names )
    {
        return selectByName(nameIndex(places), names);
    }

    public static <P extends GHPlace> List<P> selectByName( Map<String, P> index, File namesFile )
            throws IOException
    {
        return selectByName(index, readLines(namesFile));
    }

    public static <P extends GHPlace> List<P> selectByName( List<P> places, File namesFile )
            throws IOException
    {
        return selectByName(places, readLines(namesFile));
    }

    /**
     * Initialize from a GraphHopper configuration object.
     */
    public static List<GHPlace> load( CmdArgs args ) throws IOException, XMLStreamException
    {
        String osmFile = args.get("places.osm", "");
        String csvFile = args.get("places.csv", "");

        if (Helper.isEmpty(osmFile) && Helper.isEmpty(csvFile))
            throw new IllegalArgumentException(
                "You must specify a places file (places.osm=FILE or places.csv=FILE).");

        if (!Helper.isEmpty(osmFile) && !Helper.isEmpty(csvFile))
            throw new IllegalArgumentException(
                "Either places.osm or places.csv must be specified, not both.");

        if (!Helper.isEmpty(osmFile))
            return readOsm(new File(osmFile));
        else
            return readCsv(new File(csvFile));
    }

    public static List<GHPlace> readOsm( File osmFile ) throws IOException, XMLStreamException
    {
        if (!osmFile.exists())
            throw new IllegalStateException("Places file does not exist: " + osmFile.getAbsolutePath());

        logger.info("Reading places file " + osmFile.getAbsolutePath());

        try (OSMInputFile in = new OSMInputFile(osmFile).open())
        {
            return readOsm(in);
        }
    }

    private static List<GHPlace> readOsm( OSMInputFile in ) throws XMLStreamException
    {
        List<GHPlace> places = new ArrayList<>();

        OSMElement item;
        while ((item = in.getNext()) != null)
        {
            if (item.isType(OSMElement.NODE))
            {
                final OSMNode node = (OSMNode) item;
                if (node.hasTag("name") && node.hasTag("place"))
                {
                    String name = node.getTag("name");
                    GHPlace place = new GHPlace(node.getLat(), node.getLon()).setName(name);
                    places.add(place);
                }
            }
        }

        logger.info("Read " + places.size() + " places");

        return places;
    }

    public static List<GHPlace> readCsv( File csvFile ) throws IOException
    {
        if (!csvFile.exists())
            throw new IllegalStateException("Places file does not exist: " + csvFile.getAbsolutePath());

        logger.info("Reading places file " + csvFile.getAbsolutePath());

        try (FileReader in = new FileReader(csvFile))
        {
            return readCsv(new BufferedReader(in));
        }
    }

    public static List<GHPlace> readCsv( BufferedReader in ) throws IOException
    {
        List<GHPlace> places = new ArrayList<>();

        String expected = "Name,Lat,Lon";
        String line = in.readLine();
        if (line == null || !StringUtils.strip(line).equals(expected))
            throw new IllegalArgumentException("Expected header row, got " + line);

        while ((line = in.readLine()) != null)
        {
            line = StringUtils.strip(line);
            if (line.equals(""))
                break;
            places.add(parseCsv(line));
        }

        logger.info("Read " + places.size() + " places");

        return places;
    }

    public static void writeCsv( List<? extends GHPlace> places, File csvFile ) throws IOException
    {
        try (PrintStream out = new PrintStream(csvFile))
        {
            writeCsv(places, out);
        }
    }

    public static void writeCsv( List<? extends GHPlace> places, PrintStream out ) throws IOException
    {
        out.println("Name,Lat,Lon");

        for (GHPlace p : places)
        {
            out.println(toCsv(p));
        }
    }

    private static String toCsv( GHPlace p )
    {
        return p.getName() + "," + p.getLat() + "," + p.getLon();
    }

    private static GHPlace parseCsv( String s )
    {
        String[] cols = StringUtils.split(StringUtils.strip(s), ',');
        if (cols.length != 3)
            throw new IllegalArgumentException(
                    "Expected 3 CSV elements, got " + cols.length + ": " + s);
        String name = cols[0];
        double lat = Double.parseDouble(cols[1]);
        double lon = Double.parseDouble(cols[2]);
        return new GHPlace(lat, lon).setName(name);
    }

    private static List<String> readLines( File file ) throws IOException
    {
        logger.info("Reading place names from file " + file.getPath());
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line = br.readLine();
            while (line != null)
            {
                lines.add(line);
                line = br.readLine();
            }
        }

        return lines;
    }
}
