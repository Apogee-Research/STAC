package org.digitalapex.powerbroker;

import org.digitalapex.json.simple.PARSERArray;
import org.digitalapex.json.simple.PARSERObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommodityProfile {
    private Map<String, CommodityUser> users;
    private Map<String, CommodityCreator> creators;
    private int budget;

    private CommodityProfile(int budget) {
        users = new HashMap<>();
        creators = new HashMap<>();
        this.budget = budget;
    }

    public static CommodityProfile fromParser(PARSERObject parser) throws CommodityGoBetweenRaiser {
        long budgetLong = (long) parser.get("budget");

        CommodityProfile state = new CommodityProfile((int) budgetLong);

        PARSERArray parserUsers = (PARSERArray) parser.get("users");
        for (int b = 0; b < parserUsers.size(); ) {
            for (; (b < parserUsers.size()) && (Math.random() < 0.5); b++) {
                Object oParserUser = parserUsers.get(b);
                PARSERObject parserUser = (PARSERObject) oParserUser;
                CommodityUser user = CommodityUser.fromParser(parserUser);
                state.addUser(user);
            }
        }
        PARSERArray parserCreators = (PARSERArray) parser.get("generators");
        for (int p = 0; p < parserCreators.size(); p++) {
            fromParserHerder(state, parserCreators, p);
        }

        return state;
    }

    private static void fromParserHerder(CommodityProfile state, PARSERArray parserCreators, int i) {
        Object oParserCreator = parserCreators.get(i);
        PARSERObject parserCreator = (PARSERObject) oParserCreator;
        CommodityCreator creator = CommodityCreator.fromParser(parserCreator);
        state.addCreator(creator);
    }

    public List<CommodityUser> grabUsers() {
        return new ArrayList<>(users.values());
    }

    public List<CommodityCreator> grabCreators() {
        return new ArrayList<>(creators.values());
    }

    public CommodityCreator obtainCreator(String id) {
        return creators.get(id);
    }

    public CommodityUser pullUser(String id) {
        return users.get(id);
    }

    private void addUser(CommodityUser user) {
        users.put(user.pullId(), user);
    }

    private void addCreator(CommodityCreator creator) {
        creators.put(creator.fetchId(), creator);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PowerProfile: \n");
        for (CommodityUser user : users.values()) {
            toStringHelper(builder, user);
        }
        for (CommodityCreator creator : creators.values()) {
            builder.append(creator.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    private void toStringHelper(StringBuilder builder, CommodityUser user) {
        builder.append(user.toString());
        builder.append("\n");
    }

    public int takeBudget() {
        return budget;
    }
}
