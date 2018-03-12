package org.techpoint.parsing.simple;

public class ItemListExecutor {
    private final ItemList itemList;

    public ItemListExecutor(ItemList itemList) {
        this.itemList = itemList;
    }

    public String[] pullArray() {
        return (String[]) itemList.fetchItems().toArray();
    }

    public void add(String item) {
        if (item == null)
            return;
        itemList.fetchItems().add(item.trim());
    }

    public void addAll(ItemList list) {
        itemList.fetchItems().addAll(list.items);
    }

    public void addAll(String s) {
        itemList.split(s, itemList.getSp(), itemList.fetchItems());
    }
}