package edu.networkcusp.broker;

import edu.networkcusp.senderReceivers.ProtocolsIdentity;
import edu.networkcusp.senderReceivers.ProtocolsNetworkAddress;
import edu.networkcusp.broker.step.StageOverseer;
import edu.networkcusp.jackson.simple.JACKObject;
import edu.networkcusp.jackson.simple.parser.JACKParser;
import edu.networkcusp.jackson.simple.parser.ParseRaiser;
import edu.networkcusp.senderReceivers.ProtocolsNetworkAddressBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductIntermediary {
    public static final int MAX_BID = 500;
    public static final int MAX_PEERS = 3; // small limit so we can have and verify a bounding box

    private final ProtocolsIdentity identity;
    private final StageOverseer stageOverseer;
    private ProductIntermediaryCustomer productIntermediaryCustomer;

    public ProductIntermediary(ProtocolsIdentity identity) {
        this.identity = identity;
        this.stageOverseer = new StageOverseer(identity, this);
    }

    public void start(File connectionFile, File productFile) throws ProductIntermediaryRaiser {
        // read the power file and create a generation plan
        // we will print the generation to stdout then use the generation plan to create the bid plan
        // we store the bid plan for later use when we want to start auctions
        PurchasePlan offerPlan = processFromFile(productFile);

        // read the connection list and start connecting to the other powerbroker instances
        // read the connection list, we expect it to be in the format:
        // <hostname or ip>:<port>
        // <hostname or ip>:<port>
        ProtocolsNetworkAddress us = identity.getCallbackAddress();
        List<ProtocolsNetworkAddress> peers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(connectionFile))) {
            String line;
            int peerCount = 0;
            boolean amIn = false;
            while ((line = br.readLine()) != null && peerCount < MAX_PEERS) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    throw new ProductIntermediaryRaiser("Invalid line: " + line);
                }
                ProtocolsNetworkAddress peer = new ProtocolsNetworkAddressBuilder().setPlace(parts[0]).definePort(Integer.valueOf(parts[1])).formProtocolsNetworkAddress();
                peers.add(peer);
                peerCount++;
                if (peer.equals(us)) {
                    amIn = true;
                }
            }
            if (!amIn) {
                startEntity();
            }
        } catch (IOException e) {
            throw new ProductIntermediaryRaiser(e);
        }

        // the phaseManager starts the connection phase where connect to others
        // and organize our connections
        stageOverseer.start(peers, us, offerPlan);
    }

    private void startEntity() {
        System.err.println("Connection list contained too many peers.  I am dropping out.");
        System.exit(0);
    }

    private PurchasePlan processFromFile(File file) throws ProductIntermediaryRaiser {
        try {
            JACKParser parser = new JACKParser();
            JACKObject jack = (JACKObject) parser.parse(new FileReader(file));
            ProductOutline outline = ProductOutline.fromJack(jack);
            ProductAnalyzer analyzer = ProductAnalyzerFactory.form();
            GenerationPlan generationPlan = analyzer.formGenerationPlan(outline);
            System.out.println(generationPlan);

            PurchasePlan offerPlan = analyzer.generateOfferPlan(generationPlan, outline.takeBudget());

            System.out.println(offerPlan);

            return offerPlan;
        } catch (IOException e) {
            throw new ProductIntermediaryRaiser(e);
        } catch (ParseRaiser e) {
            throw new ProductIntermediaryRaiser(e);
        }
    }

    public void stop() {
        stageOverseer.stop();
    }

    public void assignProductIntermediaryCustomer(ProductIntermediaryCustomer productIntermediaryCustomer) {
        this.productIntermediaryCustomer = productIntermediaryCustomer;
    }

    public ProductIntermediaryCustomer getProductIntermediaryCustomer() {
        return productIntermediaryCustomer;
    }

    public ProtocolsIdentity takeIdentity() {
        return identity;
    }
}
