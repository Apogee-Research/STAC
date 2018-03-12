package org.techpoint.parsing.simple;

public class ItemListExecutorBuilder {
    private ItemList itemList;

    public ItemListExecutorBuilder setItemList(ItemList itemList) {
        this.itemList = itemList;
        return this;
    }

    public ItemListExecutor composeItemListExecutor() {
        return new ItemListExecutor(itemList);
    }
}