package com.cyberpointllc.stac.gabfeed.model;

import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.template.Templated;
import org.apache.commons.lang3.StringEscapeUtils;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GabRoom implements Templated {

    public static final GabRoomAscendingComparator ASCENDING_COMPARATOR = new  GabRoomAscendingComparator();

    private final GabDatabase db;

    private final String id;

    private final String name;

    private final String description;

    private final List<String> threadIds;

    public GabRoom(GabDatabase db, String id, String name, String description) {
        this(db, id, name, description, new  LinkedList<String>());
    }

    public GabRoom(GabDatabase db, String id, String name, String description, List<String> threadIds) {
        this.db = db;
        this.id = id;
        this.name = name;
        this.description = description;
        this.threadIds = threadIds;
    }

    public GabThread addThread(String name, String authorId) {
        String threadId = getId() + "_" + this.threadIds.size();
        GabThread thread = new  GabThread(db, threadId, name, authorId, new  Date());
        threadIds.add(threadId);
        db.addThread(thread);
        db.addRoom(this);
        return thread;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getThreadIds() {
        return threadIds;
    }

    public List<GabThread> getThreads() {
        LinkedList<GabThread> threads = new  LinkedList();
        for (String threadId : getThreadIds()) {
            getThreadsHelper(threadId, threads);
        }
        return threads;
    }

    @Override
    public Map<String, String> getTemplateMap() {
        Map<String, String> templateMap = new  HashMap();
        templateMap.put("roomId", id);
        templateMap.put("roomName", StringEscapeUtils.escapeHtml4(name));
        templateMap.put("roomDescription", StringEscapeUtils.escapeHtml4(description));
        return templateMap;
    }

    public static class GabRoomAscendingComparator implements Comparator<GabRoom> {

        @Override
        public int compare(GabRoom gabRoom1, GabRoom gabRoom2) {
            return gabRoom1.id.compareTo(gabRoom2.id);
        }
    }

    private void getThreadsHelper(String threadId, LinkedList<GabThread> threads) {
        threads.add(db.getThread(threadId));
    }
}
