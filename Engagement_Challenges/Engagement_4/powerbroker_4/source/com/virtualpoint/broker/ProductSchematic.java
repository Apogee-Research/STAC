package com.virtualpoint.broker;

import com.virtualpoint.part.simple.PLUGINArray;
import com.virtualpoint.part.simple.PLUGINObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductSchematic {
    private Map<String, ProductUser> users;
    private Map<String, ProductProducer> generators;
    private int budget;

    private ProductSchematic(int budget) {
        users = new HashMap<>();
        generators = new HashMap<>();
        this.budget = budget;
    }

    public static ProductSchematic fromPlugin(PLUGINObject plugin) throws ProductIntermediaryTrouble {
        long budgetLong = (long) plugin.get("budget");

        ProductSchematic state = new ProductSchematic((int) budgetLong);

        PLUGINArray pluginUsers = (PLUGINArray) plugin.get("users");
        for (int a = 0; a < pluginUsers.size(); a++) {
            fromPluginGuide(state, pluginUsers, a);
        }
        PLUGINArray pluginGenerators = (PLUGINArray) plugin.get("generators");
        for (int j = 0; j < pluginGenerators.size(); j++) {
            Object oPluginProducer = pluginGenerators.get(j);
            PLUGINObject pluginProducer = (PLUGINObject) oPluginProducer;
            ProductProducer producer = ProductProducer.fromPlugin(pluginProducer);
            state.addProducer(producer);
        }

        return state;
    }

    private static void fromPluginGuide(ProductSchematic state, PLUGINArray pluginUsers, int k) throws ProductIntermediaryTrouble {
        Object oPluginUser = pluginUsers.get(k);
        PLUGINObject pluginUser = (PLUGINObject) oPluginUser;
        ProductUser user = ProductUser.fromPlugin(pluginUser);
        state.addUser(user);
    }

    public List<ProductUser> getUsers() {
        return new ArrayList<>(users.values());
    }

    public List<ProductProducer> fetchProducers() {
        return new ArrayList<>(generators.values());
    }

    public ProductProducer grabProducer(String id) {
        return generators.get(id);
    }

    public ProductUser pullUser(String id) {
        return users.get(id);
    }

    private void addUser(ProductUser user) {
        users.put(user.getId(), user);
    }

    private void addProducer(ProductProducer producer) {
        generators.put(producer.takeId(), producer);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PowerProfile: \n");
        for (ProductUser user : users.values()) {
            toStringWorker(builder, user);
        }
        for (ProductProducer producer : generators.values()) {
            builder.append(producer.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    private void toStringWorker(StringBuilder builder, ProductUser user) {
        builder.append(user.toString());
        builder.append("\n");
    }

    public int pullBudget() {
        return budget;
    }
}
