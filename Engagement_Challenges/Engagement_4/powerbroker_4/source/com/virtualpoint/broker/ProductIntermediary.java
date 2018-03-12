package com.virtualpoint.broker;

import com.virtualpoint.talkers.DialogsIdentity;
import com.virtualpoint.talkers.DialogsNetworkAddress;
import com.virtualpoint.broker.step.StepOverseer;
import com.virtualpoint.part.simple.PLUGINObject;
import com.virtualpoint.part.simple.retriever.PLUGINRetriever;
import com.virtualpoint.part.simple.retriever.ParseTrouble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductIntermediary {
    public static final int MAX_BID = 500;
    public static final int MAX_PEERS = 3; // small limit so we can have and verify a bounding box

    private final DialogsIdentity identity;
    private final StepOverseer stepOverseer;
    private ProductIntermediaryUser productIntermediaryUser;

    public ProductIntermediary(DialogsIdentity identity) {
        this.identity = identity;
        this.stepOverseer = new StepOverseer(identity, this);
    }

    public void start(File connectionFile, File productFile) throws ProductIntermediaryTrouble {
        // read the power file and create a generation plan
        // we will print the generation to stdout then use the generation plan to create the bid plan
        // we store the bid plan for later use when we want to start auctions
        PurchasePlan bidPlan = processFromFile(productFile);

        // read the connection list and start connecting to the other powerbroker instances
        // read the connection list, we expect it to be in the format:
        // <hostname or ip>:<port>
        // <hostname or ip>:<port>
        DialogsNetworkAddress us = identity.grabCallbackAddress();
        List<DialogsNetworkAddress> peers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(connectionFile))) {
            String line;
            int peerCount = 0;
            boolean amIn = false;
            while ((line = br.readLine()) != null && peerCount < MAX_PEERS) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    new ProductIntermediaryHerder(line).invoke();
                }
                DialogsNetworkAddress peer = new DialogsNetworkAddress(parts[0], Integer.valueOf(parts[1]));
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
            throw new ProductIntermediaryTrouble(e);
        }

        // the phaseManager starts the connection phase where connect to others
        // and organize our connections
        stepOverseer.start(peers, us, bidPlan);
    }

    private PurchasePlan processFromFile(File file) throws ProductIntermediaryTrouble {
        try {
            PLUGINRetriever retriever = new PLUGINRetriever();
            PLUGINObject plugin = (PLUGINObject) retriever.parse(new FileReader(file));
            ProductSchematic schematic = ProductSchematic.fromPlugin(plugin);
            ProductAuthority authority = ProductAuthorityFactory.compose();
            GenerationPlan generationPlan = authority.composeGenerationPlan(schematic);
            System.out.println(generationPlan);

            PurchasePlan bidPlan = authority.generateBidPlan(generationPlan, schematic.pullBudget());

            System.out.println(bidPlan);

            return bidPlan;
        } catch (IOException e) {
            throw new ProductIntermediaryTrouble(e);
        } catch (ParseTrouble e) {
            throw new ProductIntermediaryTrouble(e);
        }
    }

    public void stop() {
        stepOverseer.stop();
    }

    public void setProductIntermediaryUser(ProductIntermediaryUser productIntermediaryUser) {
        this.productIntermediaryUser = productIntermediaryUser;
    }

    public ProductIntermediaryUser fetchProductIntermediaryUser() {
        return productIntermediaryUser;
    }

    public DialogsIdentity getIdentity() {
        return identity;
    }

    private class ProductIntermediaryHerder {
        private String line;

        public ProductIntermediaryHerder(String line) {
            this.line = line;
        }

        public void invoke() throws ProductIntermediaryTrouble {
            throw new ProductIntermediaryTrouble("Invalid line: " + line);
        }
    }
}
