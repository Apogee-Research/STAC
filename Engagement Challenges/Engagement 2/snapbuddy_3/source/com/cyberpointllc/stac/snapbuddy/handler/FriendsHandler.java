package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.sort.Sorter;
import com.cyberpointllc.stac.template.TemplateEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FriendsHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/friends";

    private static final String TITLE = "Friends";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<tr>" + "    <td rowspan=\"2\"><img src=\"{{photoURL}}\" alt=\"{{name}} Profile Photo\" class=\"snapshot\"/></td>" + "    <td>{{name}}</td>" + "</tr>" + "<tr><td>{{location}}</td></tr>");

    public FriendsHandler(SnapService snapService) {
        super(snapService);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected String getTitle(SnapContext context) {
        return TITLE;
    }

    @Override
    protected String getContents(SnapContext context) {
        assert (context != null) : "Context may not be null";
        Map<String, String> map = new  HashMap();
        StringBuilder sb = new  StringBuilder();
        sb.append("<table>");
        List<Person> friends = new  ArrayList();
        SnapService snapService = getSnapService();
        for (String identity : context.getActivePerson().getFriends()) {
            friends.add(snapService.getPerson(identity));
        }
        Sorter sorter = new  Sorter(Person.ASCENDING_COMPARATOR);
        friends = sorter.sort(friends);
        for (int i = 0; i < friends.size(); ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < friends.size() && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                Person friend = friends.get(i);
                if (friend != null) {
                    Location location = friend.getLocation();
                    String city = Location.UNKNOWN.equals(location) ? "" : location.getCity();
                    map.clear();
                    map.put("photoURL", getProfilePhotoUrl(friend));
                    map.put("name", friend.getName());
                    map.put("location", city);
                    sb.append(TEMPLATE.replaceTags(map));
                }
            }
        }
        sb.append("</table>");
        return sb.toString();
    }
}
