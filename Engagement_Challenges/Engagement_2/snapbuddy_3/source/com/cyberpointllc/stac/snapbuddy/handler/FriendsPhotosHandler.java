package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.cyberpointllc.stac.sort.Sorter;
import com.cyberpointllc.stac.template.TemplateEngine;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendsPhotosHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/friendsphotos";

    private static final String TITLE = "My Friends' Photos";

    private static final TemplateEngine TEMPLATE = new  TemplateEngine("<li><a href=\"showphoto/{{pid}}\"><img src=\"{{photoURL}}\" alt=\"{{caption}}\" class=\"snapshot\"/></a></li>");

    public FriendsPhotosHandler(SnapService snapService) {
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
        sb.append("<ul class=\"photos\">");
        List<Person> friends = new  ArrayList();
        SnapService snapService = getSnapService();
        for (String identity : person.getFriends()) {
            friends.add(snapService.getPerson(identity));
        }
        Sorter sorter = new  Sorter(Person.ASCENDING_COMPARATOR);
        friends = sorter.sort(friends);
        for (int i = 0; i < friends.size(); i++) {
            Person friend = friends.get(i);
            if (friend != null) {
                for (String photoId : friend.getPhotos()) {
                    Photo photo = getSnapService().getPhoto(photoId);
                    if (photo != null) {
                        map.clear();
                        map.put("pid", photo.getIdentity());
                        map.put("photoURL", getThumbPhotoUrl(photo));
                        map.put("caption", StringUtils.isBlank(photo.getCaption()) ? "" : photo.getCaption());
                        sb.append(TEMPLATE.replaceTags(map));
                    }
                }
            }
        }
        sb.append("</ul>");
        return sb.toString();
    }
}
