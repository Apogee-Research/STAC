package com.cyberpointllc.stac.gabfeed.model;

import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.template.Templated;
import org.apache.commons.lang3.StringEscapeUtils;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

public class GabMessage implements Templated {

    public static final GabMessageAscendingComparator ASCENDING_COMPARATOR = new  GabMessageAscendingComparator();

    public static final GabMessageDescendingComparator DESCENDING_COMPARATOR = new  GabMessageDescendingComparator();

    private final GabDatabase db;

    private final String id;

    private final String contents;

    private final String authorId;

    private final Date postDate;

    private final boolean publicMessage;

    public GabMessage(GabDatabase db, String id, String contents, String authorId, Date postDate, boolean isPublicMessage) {
        this.db = db;
        this.id = id;
        this.contents = contents;
        this.authorId = authorId;
        this.postDate = postDate;
        this.publicMessage = isPublicMessage;
    }

    public String getContents() {
        return contents;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getId() {
        return id;
    }

    public Date getPostDate() {
        return postDate;
    }

    public boolean isPublicMessage() {
        return publicMessage;
    }

    @Override
    public Map<String, String> getTemplateMap() {
        Map<String, String> templateMap = new  HashMap();
        templateMap.put("messageId", id);
        templateMap.put("messageContents", StringEscapeUtils.escapeHtml4(contents));
        templateMap.put("messageAuthorId", authorId);
        templateMap.put("messagePostDate", postDate.toString());
        String displayName = authorId;
        GabUser user = db.getUser(authorId);
        if (user != null) {
            displayName = user.getDisplayName();
        }
        templateMap.put("messageAuthorDisplayName", displayName);
        templateMap.put("publicMessage", Boolean.toString(publicMessage));
        return templateMap;
    }

    public static class GabMessageAscendingComparator implements Comparator<GabMessage> {

        @Override
        public int compare(GabMessage gabMessage1, GabMessage gabMessage2) {
            Date message1Date = gabMessage1.getPostDate();
            Date message2Date = gabMessage2.getPostDate();
            return message1Date.compareTo(message2Date);
        }
    }

    public static class GabMessageDescendingComparator implements Comparator<GabMessage> {

        @Override
        public int compare(GabMessage gabMessage1, GabMessage gabMessage2) {
            Date message1Date = gabMessage1.getPostDate();
            Date message2Date = gabMessage2.getPostDate();
            return message2Date.compareTo(message1Date);
        }
    }
}
