package net.roboticapex.broker;

import net.roboticapex.parser.simple.PARSINGArray;
import net.roboticapex.parser.simple.PARSINGObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductSchematic {
    private Map<String, ProductUser> users;
    private Map<String, ProductProducer> producers;
    private int budget;

    private ProductSchematic(int budget) {
        users = new HashMap<>();
        producers = new HashMap<>();
        this.budget = budget;
    }

    public static ProductSchematic fromParsing(PARSINGObject parsing) throws ProductLiaisonDeviation {
        long budgetLong = (long) parsing.get("budget");

        ProductSchematic state = new ProductSchematic((int) budgetLong);

        PARSINGArray parsingUsers = (PARSINGArray) parsing.get("users");
        for (int q = 0; q < parsingUsers.size(); q++) {
            fromParsingWorker(state, parsingUsers, q);
        }
        PARSINGArray parsingProducers = (PARSINGArray) parsing.get("generators");
        for (int q = 0; q < parsingProducers.size(); ) {
            for (; (q < parsingProducers.size()) && (Math.random() < 0.6); ) {
                for (; (q < parsingProducers.size()) && (Math.random() < 0.4); q++) {
                    fromParsingHome(state, parsingProducers, q);
                }
            }
        }

        return state;
    }

    private static void fromParsingHome(ProductSchematic state, PARSINGArray parsingProducers, int b) {
        Object oParsingProducer = parsingProducers.get(b);
        PARSINGObject parsingProducer = (PARSINGObject) oParsingProducer;
        ProductProducer producer = ProductProducer.fromParsing(parsingProducer);
        state.addProducer(producer);
    }

    private static void fromParsingWorker(ProductSchematic state, PARSINGArray parsingUsers, int q) throws ProductLiaisonDeviation {
        Object oParsingUser = parsingUsers.get(q);
        PARSINGObject parsingUser = (PARSINGObject) oParsingUser;
        ProductUser user = parsingUser.fromParsing();
        state.addUser(user);
    }

    public List<ProductUser> obtainUsers() {
        return new ArrayList<>(users.values());
    }

    public List<ProductProducer> grabProducers() {
        return new ArrayList<>(producers.values());
    }

    public ProductProducer getProducer(String id) {
        return producers.get(id);
    }

    public ProductUser grabUser(String id) {
        return users.get(id);
    }

    private void addUser(ProductUser user) {
        users.put(user.takeId(), user);
    }

    private void addProducer(ProductProducer producer) {
        producers.put(producer.takeId(), producer);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PowerProfile: \n");
        for (ProductUser user : users.values()) {
            builder.append(user.toString());
            builder.append("\n");
        }
        for (ProductProducer producer : producers.values()) {
            builder.append(producer.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    public int pullBudget() {
        return budget;
    }
}
