package com.graphhopper.http;

import com.graphhopper.tour.Places;
import com.graphhopper.tour.TourCalculator;
import com.graphhopper.tour.TourResponse;
import com.graphhopper.tour.util.ProgressReporter;
import com.graphhopper.util.shapes.GHPlace;
import com.graphhopper.util.shapes.GHPoint;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * Servlet that provides a {@link TourCalculator} as an HTTP service.
 */
public class TourServlet extends GHBaseServlet
{
    @Inject
    private List<GHPlace> places;
    @Inject
    private TourCalculator tourCalculator;
    @Inject
    private TourSerializer tourSerializer;

    private Map<String, GHPlace> nameIndex;

    @Override
    public void init()
    {
        nameIndex = Places.nameIndex(places);
    }

    /**
     * Process an HTTP request.
     *
     * To implement the timing side channel, if a `progress=true` parameter is specified
     * in the HTTP request, this servlet will write its response using HTTP server-sent
     * events[1][1].
     *
     * [1]: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
     * [2]: http://www.w3.org/TR/eventsource/
     */
    @Override
    public void doGet( final HttpServletRequest req, final HttpServletResponse res ) throws ServletException, IOException
    {
        List<GHPoint> points;
        TourResponse<?> tourRsp = new TourResponse<>();

        // Get points from request and look them up by name.
        // Return an error if any named places are unknown.
        try
        {
            points = getPoints(req, "point");
        } catch (Exception ex)
        {
            tourRsp.addError(ex);
            Map<String, Object> map = tourSerializer.toJSON(tourRsp);
            writeJsonError(res, SC_BAD_REQUEST, new JSONObject(map));
            return;
        }

        // Begin server-sent event stream
        res.setContentType("text/event-stream");
        res.setCharacterEncoding("UTF-8");
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().flush();
        res.flushBuffer();

        ProgressReporter progressReporter = new ProgressReporter()
        {
            @Override
            public void reportProgress( int complete, int total ) throws IOException
            {
                JSONObject json = new JSONObject();
                json.put("complete", complete);
                json.put("total", total);

                Writer writer = res.getWriter();
                writer.append("event: progress\r\n");
                writer.append("data: " + json.toString() + "\r\n\r\n");
                writer.flush();
                //try { res.flushBuffer(); } catch (Exception ex) {}
            }
        };

        // Calculate tour, showing progress as server-sent events
        tourRsp = tourCalculator.calcTour(points, progressReporter);

        // Write result event (or normal result if progress=false)
        Map<String, Object> map = tourSerializer.toJSON(tourRsp);
        JSONObject json = new JSONObject(map);
        Writer writer = res.getWriter();
        writer.append("event: result\r\n");
        writer.append("data: " + json.toString() + "\r\n\r\n");
    }

    protected List<GHPoint> getPoints( HttpServletRequest req, String key )
    {
        String[] pointsAsStr = getParams(req, key);
        final List<GHPoint> points = new ArrayList<>(pointsAsStr.length);
        for (String str : pointsAsStr)
        {
            String[] fromStrs = str.split(",");
            if (fromStrs.length == 2)
            {
                GHPoint point = GHPoint.parse(str);
                if (point != null)
                    points.add(point);
            } else if (fromStrs.length == 1)
            {
                GHPoint point = nameIndex.get(str);
                if (point == null)
                    throw new IllegalArgumentException("unknown place \"" + str + "\"");
                points.add(point);
            }
        }

        return points;
    }
}
