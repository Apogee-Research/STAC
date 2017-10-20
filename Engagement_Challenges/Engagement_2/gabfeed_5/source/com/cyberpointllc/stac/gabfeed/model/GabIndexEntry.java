package com.cyberpointllc.stac.gabfeed.model;

import com.cyberpointllc.stac.gabfeed.persist.GabDatabase;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GabIndexEntry {

    public static final GabIndexEntryComparator DESCENDING_COMPARATOR = new  GabIndexEntryComparator();

    private final String word;

    private final List<Item> items;

    public GabIndexEntry(GabDatabase db, String word) {
        this(db, word, new  LinkedList<Item>());
    }

    public GabIndexEntry(GabDatabase db, String word, List<Item> items) {
        this.word = word;
        this.items = items;
    }

    public String getWord() {
        return word;
    }

    public void addItem(String word, String messageId, int count, Date date) {
        items.add(new  Item(word, messageId, count, date));
    }

    public List<Item> getItems() {
        // should we protect this more?
        return items;
    }

    public static class GabIndexEntryComparator implements Comparator<Item> {

        @Override
        public int compare(Item item1, Item item2) {
            int count1 = item1.count;
            int count2 = item2.count;
            if (count1 < count2) {
                return 1;
            } else if (count2 < count1) {
                return -1;
            } else {
                return item2.getDate().compareTo(item1.getDate());
            }
        }
    }

    public class Item {

        private int count;

        private String messageId;

        private String word;

        private Date date;

        public Item(String word, String messageId, int count, Date date) {
            this.word = word;
            this.messageId = messageId;
            this.count = count;
            this.date = date;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getWord() {
            return word;
        }

        public int getCount() {
            return count;
        }

        public Date getDate() {
            return date;
        }
    }
}
