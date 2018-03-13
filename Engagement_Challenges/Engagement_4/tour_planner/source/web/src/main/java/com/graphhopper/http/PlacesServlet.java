/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.graphhopper.http;

import com.graphhopper.util.shapes.GHPlace;
import com.graphhopper.util.shapes.GHPoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ngoffee
 */
public class PlacesServlet extends GHBaseServlet
{
    @Inject
    protected List<GHPlace> places;

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        writeJson(req, res, getJson());
    }

    protected JSONArray getJson()
    {
        JSONArray json = new JSONArray();
        for (GHPlace p : places)
        {
            json.put(pointToJson(p));
        }

        return json;
    }

    protected JSONObject pointToJson(GHPoint p)
    {
        JSONObject json = new JSONObject();
        if (p instanceof GHPlace)
            json.put("name", ((GHPlace) p).getName());
        json.put("lat", p.getLat());
        json.put("lon", p.getLon());

        return json;
    }
}
