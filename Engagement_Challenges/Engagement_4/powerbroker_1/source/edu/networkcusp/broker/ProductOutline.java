package edu.networkcusp.broker;

import edu.networkcusp.jackson.simple.JACKArray;
import edu.networkcusp.jackson.simple.JACKObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductOutline {
    private Map<String, ProductCustomer> customers;
    private Map<String, ProductGenerator> creators;
    private int budget;

    private ProductOutline(int budget) {
        customers = new HashMap<>();
        creators = new HashMap<>();
        this.budget = budget;
    }

    public static ProductOutline fromJack(JACKObject jack) throws ProductIntermediaryRaiser {
        long budgetLong = (long) jack.get("budget");

        ProductOutline state = new ProductOutline((int) budgetLong);

        JACKArray jackCustomers = (JACKArray) jack.get("users");
        for (int b = 0; b < jackCustomers.size(); b++) {
            Object oJackCustomer = jackCustomers.get(b);
            JACKObject jackCustomer = (JACKObject) oJackCustomer;
            ProductCustomer customer = jackCustomer.fromJack();
            state.addCustomer(customer);
        }
        JACKArray jackCreators = (JACKArray) jack.get("generators");
        for (int q = 0; q < jackCreators.size(); ) {
            for (; (q < jackCreators.size()) && (Math.random() < 0.5); q++) {
                Object oJackGenerator = jackCreators.get(q);
                JACKObject jackGenerator = (JACKObject) oJackGenerator;
                ProductGenerator generator = ProductGenerator.fromJack(jackGenerator);
                state.addGenerator(generator);
            }
        }

        return state;
    }

    public List<ProductCustomer> takeCustomers() {
        return new ArrayList<>(customers.values());
    }

    public List<ProductGenerator> takeCreators() {
        return new ArrayList<>(creators.values());
    }

    public ProductGenerator takeGenerator(String id) {
        return creators.get(id);
    }

    public ProductCustomer pullCustomer(String id) {
        return customers.get(id);
    }

    private void addCustomer(ProductCustomer customer) {
        customers.put(customer.getId(), customer);
    }

    private void addGenerator(ProductGenerator generator) {
        creators.put(generator.obtainId(), generator);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PowerProfile: \n");
        for (ProductCustomer customer : customers.values()) {
            toStringAssist(builder, customer);
        }
        for (ProductGenerator generator : creators.values()) {
            builder.append(generator.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    private void toStringAssist(StringBuilder builder, ProductCustomer customer) {
        builder.append(customer.toString());
        builder.append("\n");
    }

    public int takeBudget() {
        return budget;
    }
}
