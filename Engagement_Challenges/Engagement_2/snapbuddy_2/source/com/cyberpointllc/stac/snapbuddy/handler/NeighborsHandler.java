package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.sort.Sorter;
import com.cyberpointllc.stac.template.TemplateEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeighborsHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/neighbors";

    private static final String TITLE = "Neighbors in ";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<tr>" + "    <td><img src=\"{{photoURL}}\" alt=\"{{name}} Profile Photo\" class=\"snapshot\"/></td>" + "    <td>{{name}}</td>" + "</tr>");

    public NeighborsHandler(SnapService snapService) {
        super(snapService);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE + context.getActivePerson().getLocation().getCity();
    }

    @Override
    protected String getContents(SnapContext context) {
        assert (context != null) : "Context may not be null";
        Person person = context.getActivePerson();
        Map<String, String> map = new  HashMap();
        StringBuilder sb = new  StringBuilder();
        sb.append("<table>");
        Sorter sorter = new  Sorter(Person.ASCENDING_COMPARATOR);
        List<Person> neighbors = new  ArrayList(getSnapService().getNeighbors(person));
        neighbors = sorter.sort(neighbors);
        for (int i = 0; i < neighbors.size(); i++) {
            Person neighbor = neighbors.get(i);
            map.clear();
            map.put("photoURL", getProfilePhotoUrl(neighbor));
            map.put("name", neighbor.getName());
            sb.append(TEMPLATE.replaceTags(map));
        }
        sb.append("</table>");
        return sb.toString();
    }
}
