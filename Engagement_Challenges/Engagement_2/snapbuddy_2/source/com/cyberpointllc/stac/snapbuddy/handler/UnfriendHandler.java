package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.template.TemplateEngine;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnfriendHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/unfriend";

    private static final String TITLE = "Remove Friends";

    private static final String FIELD_NAME = "unfriend";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<tr>" + "    <td rowspan=\"2\">" + "        <input type=\"checkbox\" name=\"unfriend\" value=\"{{identity}}\"> " + "    </td>" + "    <td rowspan=\"2\"><img src=\"{{photoURL}}\" alt=\"{{name}} Profile Photo\" class=\"snapshot\"/></td>" + "    <td>{{name}}</td>" + "</tr>" + "<tr><td>{{location}}</td></tr>");

    public UnfriendHandler(SnapService snapService) {
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
        Person person = context.getActivePerson();
        Map<String, String> map = new  HashMap();
        StringBuilder sb = new  StringBuilder();
        // add form
        sb.append("<form action=\"");
        sb.append(PATH);
        sb.append("\" method=\"post\" enctype=\"multipart/form-data\">");
        sb.append("<table>");
        for (String identity : person.getFriends()) {
            getContentsHelper(identity, sb, map);
        }
        sb.append("</table>");
        // add Unfriend Button
        sb.append("<input type=\"submit\" value=\"Unfriend\" name=\"submit\" >");
        // close form
        sb.append("</form>");
        return sb.toString();
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        Person person = getPerson(httpExchange);
        Set<Person> friends = new  HashSet();
        List<String> identities = MultipartHelper.getMultipartFieldItems(httpExchange, FIELD_NAME);
        for (String identity : identities) {
            handlePostHelper(identity, friends);
        }
        getSnapService().removeFriends(person, friends);
        return getDefaultRedirectResponse();
    }

    private void getContentsHelper(String identity, StringBuilder sb, Map<String, String> map) {
        Person friend = getSnapService().getPerson(identity);
        if (friend != null) {
            Location location = friend.getLocation();
            String city = Location.UNKNOWN.equals(location) ? "" : location.getCity();
            map.clear();
            map.put("photoURL", getProfilePhotoUrl(friend));
            map.put("name", friend.getName());
            map.put("identity", friend.getIdentity());
            map.put("location", city);
            sb.append(TEMPLATE.replaceTags(map));
        }
    }

    private void handlePostHelper(String identity, Set<Person> friends) {
        Person friend = getSnapService().getPerson(identity);
        if (friend != null) {
            friends.add(friend);
        }
    }
}
