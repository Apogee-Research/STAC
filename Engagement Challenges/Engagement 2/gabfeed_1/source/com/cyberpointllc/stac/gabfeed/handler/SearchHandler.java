package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.gabfeed.model.GabIndexEntry;
import com.cyberpointllc.stac.gabfeed.model.GabMessage;
import com.cyberpointllc.stac.gabfeed.model.GabThread;
import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.sort.Sorter;
import com.cyberpointllc.stac.webserver.WebSession;
import com.cyberpointllc.stac.webserver.WebSessionService;
import com.cyberpointllc.stac.webserver.WebTemplate;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.cyberpointllc.stac.webserver.handler.MultipartHelper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class SearchHandler extends GabHandler {

    protected static final String PATH = "/search";

    protected static final String TITLE = "Search";

    private static final String FIELD_NAME = "search";

    private String CONTENTS = "";

    private final WebTemplate threadTemplate;

    private final WebTemplate messageListTemplate;

    private final WebTemplate messageListTemplateWithoutTime;

    private final String dataPath;

    private List<String> dailyTerms;

    public SearchHandler(GabDatabase db, WebSessionService webSessionService, String dataPath) {
        super(db, webSessionService);
        this.threadTemplate = new  WebTemplate("ThreadTemplate.html", getClass());
        this.messageListTemplate = new  WebTemplate("MessageListSnippet.html", getClass());
        this.messageListTemplateWithoutTime = new  WebTemplate("MessageListSnippetWithoutTime.html", getClass());
        this.dataPath = dataPath;
        this.dailyTerms = getDailyTerms();
        this.CONTENTS = "<center>" + getTermsText() + "<form action=\"" + PATH + "\" method=\"post\" enctype=\"multipart/form-data\">" + "    <input type=\"text\" name=\"" + FIELD_NAME + "\" placeholder=\"search here\" /> <br/>" + "    <input type=\"submit\" value=\"search\" name=\"submit\" />" + "</form>" + "</center>";
    }

    @Override
    public String getPath() {
        return PATH;
    }

    /**
     * Get text listing the currently available educational search terms
     * @return
     */
    private String getTermsText() {
        String text = "Today's special search terms are: ";
        List<String> terms = this.dailyTerms;
        int i;
        for (i = 0; i < terms.size() - 1; i++) {
            text += terms.get(i) + ", ";
        }
        text += "and " + terms.get(i);
        text += ".  Search for these terms to learn more.  Or search for whatever interests you.";
        return text;
    }

    private String getContents(GabIndexEntry gabIndexEntry, WebSession webSession, String specialContents) {
        String suppressTimestampString = webSession.getProperty("suppressTimestamp", "false");
        boolean suppressTimestamp = Boolean.parseBoolean(suppressTimestampString);
        StringBuilder builder = new  StringBuilder();
        // get the items in the indexEntry and sort them by the number
        // of times the word appears in the item's associated message
        List<GabIndexEntry.Item> items = gabIndexEntry.getItems();
        Sorter sorter = new  Sorter(GabIndexEntry.DESCENDING_COMPARATOR);
        items = sorter.sort(items);
        for (GabIndexEntry.Item item : items) {
            getContentsHelper(suppressTimestamp, webSession, item, builder);
        }
        Map<String, String> threadMap = Collections.singletonMap("messages", builder.toString());
        return specialContents + CONTENTS + threadTemplate.getEngine().replaceTags(threadMap);
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        WebSession webSession = getWebSessionService().getSession(httpExchange);
        String userId = webSession.getUserId();
        String query = httpExchange.getRequestURI().getQuery();
        if (!StringUtils.isBlank(query) && query.equals("suppressTimestamp=true")) {
            webSession.setProperty("suppressTimestamp", "true");
        }
        return getTemplateResponse(TITLE, CONTENTS, getDb().getUser(userId));
    }

    @Override
    protected HttpHandlerResponse handlePost(HttpExchange httpExchange) {
        WebSession webSession = getWebSessionService().getSession(httpExchange);
        String searchTerm = MultipartHelper.getMultipartFieldContent(httpExchange, FIELD_NAME).trim();
        GabIndexEntry gabIndexEntry = getDb().getIndexEntry(searchTerm);
        // extra text to display for those who search for the term of the day
        String specialContents = "";
        String user = (getDb().getUser(webSession.getUserId())).getDisplayName();
        if (dailyTerms.contains(searchTerm)) {
            // check list of daily terms 
            // get info on term to display for user
            String info = getInfoText(searchTerm);
            info = PageUtils.formatLongString(searchTerm + ": " + info, webSession);
            specialContents = "<center>" + info + "</center>";
        }
        if (gabIndexEntry != null) {
            String contents = getContents(gabIndexEntry, webSession, specialContents);
            return getTemplateResponse(TITLE, contents, getDb().getUser(webSession.getUserId()));
        } else {
            return getTemplateResponse(TITLE, specialContents + CONTENTS, getDb().getUser(webSession.getUserId()));
        }
    }

    private List<String> getDailyTerms() {
        List<String> terms = new  ArrayList<String>();
        try (BufferedReader reader = new  BufferedReader(new  FileReader(dataPath + File.separator + "special_terms.txt"))) {
            String term;
            while ((term = reader.readLine()) != null) {
                terms.add(term);
            }
            return terms;
        } catch (IOException e) {
            System.out.println(e);
            return new  ArrayList<String>();
        }
    }

    // read informational text from file
    private String getInfoText(String term) {
        SearchHandlerHelper0 conditionObj0 = new  SearchHandlerHelper0(0);
        try (BufferedReader reader = new  BufferedReader(new  FileReader(dataPath + File.separator + "terms_text.txt"))) {
            // read in the file
            String line = "";
            String key = "";
            String val = "";
            Map<String, String> textInfo = new  TreeMap<String, String>();
            while ((line = reader.readLine()) != null) {
                if (line.contains("=")) {
                    if (key.length() != conditionObj0.getValue()) {
                        textInfo.put(key, val);
                    }
                    String[] parts = line.split("=");
                    try {
                        key = parts[0];
                        val = parts[1];
                    } catch (Exception e) {
                        System.out.println(line);
                    }
                } else {
                    val += line;
                }
            }
            textInfo.put(key, val);
            // now look up the term we're interested in
            return textInfo.get(term);
        } catch (IOException e) {
            System.out.println(e);
            return "Unfortunately, we are unable to provide any further info on " + term + " at this time.  Thanks for searching!";
        }
    }

    private class SearchHandlerHelper0 {

        public SearchHandlerHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private void getContentsHelper(boolean suppressTimestamp, WebSession webSession, GabIndexEntry.Item item, StringBuilder builder) {
        GabMessage message = getDb().getMessage(item.getMessageId());
        Map<String, String> messageMap = message.getTemplateMap();
        String content = messageMap.get("messageContents");
        messageMap.put("messageContents", PageUtils.formatLongString(content, webSession));
        if (!suppressTimestamp) {
            messageListTemplate.getEngine().replaceTagsBuilder(messageMap, builder);
        } else {
            messageListTemplateWithoutTime.getEngine().replaceTagsBuilder(messageMap, builder);
        }
    }
}
