package net.roboticapex.broker;

import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import net.roboticapex.senderReceivers.SenderReceiversNetworkAddress;
import net.roboticapex.broker.period.StepOverseer;
import net.roboticapex.parser.simple.PARSINGObject;
import net.roboticapex.parser.simple.grabber.PARSINGParser;
import net.roboticapex.parser.simple.grabber.ParseDeviation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductLiaison {
    public static final int MAX_BID = 500;
    public static final int MAX_PEERS = 3; // small limit so we can have and verify a bounding box

    private final SenderReceiversIdentity identity;
    private final StepOverseer stepOverseer;
    private ProductLiaisonUser productLiaisonUser;

    public ProductLiaison(SenderReceiversIdentity identity) {
        this.identity = identity;
        this.stepOverseer = new StepOverseer(identity, this);
    }

    public void start(File connectionFile, File productFile) throws ProductLiaisonDeviation {
        // read the power file and create a generation plan
        // we will print the generation to stdout then use the generation plan to create the bid plan
        // we store the bid plan for later use when we want to start auctions
        BidPlan promisePlan = processFromFile(productFile);

        // read the connection list and start connecting to the other powerbroker instances
        // read the connection list, we expect it to be in the format:
        // <hostname or ip>:<port>
        // <hostname or ip>:<port>
        SenderReceiversNetworkAddress us = identity.getCallbackAddress();
        List<SenderReceiversNetworkAddress> peers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(connectionFile))) {
            String line;
            int peerCount = 0;
            boolean amIn = false;
            while ((line = br.readLine()) != null && peerCount < MAX_PEERS) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    startGateKeeper(line);
                }
                SenderReceiversNetworkAddress peer = new SenderReceiversNetworkAddress(parts[0], Integer.valueOf(parts[1]));
                peers.add(peer);
                peerCount++;
                if (peer.equals(us)) {
                    amIn = true;
                }
            }
            if (!amIn) {
                System.err.println("Connection list contained too many peers.  I am dropping out.");
                System.exit(0);
            }
        } catch (IOException e) {
            throw new ProductLiaisonDeviation(e);
        }

        // the phaseManager starts the connection phase where connect to others
        // and organize our connections
        stepOverseer.start(peers, us, promisePlan);
    }

    private void startGateKeeper(String line) throws ProductLiaisonDeviation {
        new ProductLiaisonHandler(line).invoke();
    }

    private BidPlan processFromFile(File file) throws ProductLiaisonDeviation {
        try {
            PARSINGParser parser = new PARSINGParser();
            PARSINGObject parsing = (PARSINGObject) parser.parse(new FileReader(file));
            ProductSchematic schematic = ProductSchematic.fromParsing(parsing);
            ProductEvaluator evaluator = ProductEvaluatorFactory.make();
            GenerationPlan generationPlan = evaluator.makeGenerationPlan(schematic);
            System.out.println(generationPlan);

            BidPlan promisePlan = evaluator.generatePromisePlan(generationPlan, schematic.pullBudget());

            System.out.println(promisePlan);

            return promisePlan;
        } catch (IOException e) {
            throw new ProductLiaisonDeviation(e);
        } catch (ParseDeviation e) {
            throw new ProductLiaisonDeviation(e);
        }
    }

    public void stop() {
        stepOverseer.stop();
    }

    public void fixProductLiaisonUser(ProductLiaisonUser productLiaisonUser) {
        this.productLiaisonUser = productLiaisonUser;
    }

    public ProductLiaisonUser obtainProductLiaisonUser() {
        return productLiaisonUser;
    }

    public SenderReceiversIdentity fetchIdentity() {
        return identity;
    }

    private class ProductLiaisonHandler {
        private String line;

        public ProductLiaisonHandler(String line) {
            this.line = line;
        }

        public void invoke() throws ProductLiaisonDeviation {
            throw new ProductLiaisonDeviation("Invalid line: " + line);
        }
    }
}
