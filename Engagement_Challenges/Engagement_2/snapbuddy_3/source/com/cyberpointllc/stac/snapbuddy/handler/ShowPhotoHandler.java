package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.SnapContext;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.cyberpointllc.stac.webserver.WebTemplate;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;

public class ShowPhotoHandler extends AbstractTemplateSnapBuddyHandler {

    private static final String PATH = "/showphoto/";

    private static final String TITLE = "View Photo";

    private static final String SHOW_PHOTO_RESOURCE = "showphoto.snippet";

    private final WebTemplate TEMPLATE = new  WebTemplate(SHOW_PHOTO_RESOURCE, getClass());

    public ShowPhotoHandler(SnapService snapService) {
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
        String path = context.getPath();
        if (path.startsWith(getPath())) {
            path = path.substring(getPath().length());
        }
        Photo photo = getSnapService().getPhoto(path);
        if (photo == null) {
            return "Photo does not exist: " + path;
        }
        // Is the person allowed to see this photo?
        Person person = context.getActivePerson();
        if (getSnapService().isPhotoVisible(person, photo)) {
            // good to go
            Map<String, String> map = new  HashMap();
            map.clear();
            map.put("pid", photo.getIdentity());
            map.put("photoURL", getPhotoUrl(photo));
            map.put("caption", StringUtils.isBlank(photo.getCaption()) ? "" : photo.getCaption());
            return TEMPLATE.getEngine().replaceTags(map);
        }
        return "You're not allowed to see this photo: " + path;
    }
}
