package com.cyberpointllc.stac.gabfeed.handler;

import com.cyberpointllc.stac.linebreak.LineBreak;
import com.cyberpointllc.stac.webserver.WebSession;

public class PageUtils {

    public static String formatLongString(String content, WebSession webSession) {
        String widthString = webSession.getProperty(WidthHandler.PROPERTY_NAME, "80");
        int width = Integer.parseInt(widthString);
        LineBreak lineBreak = new  LineBreak(width);
        StringBuilder builder = new  StringBuilder();
        for (String paragraph : lineBreak.breakParagraphs(content, "<br/>")) {
            builder.append("<p>");
            builder.append(paragraph);
            builder.append("</p>");
        }
        return builder.toString();
    }
}
