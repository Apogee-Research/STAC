package org.digitalapex.powerbroker;

import org.digitalapex.powerbroker.stage.PeriodOverseerBuilder;
import org.digitalapex.talkers.TalkersIdentity;
import org.digitalapex.talkers.TalkersNetworkAddress;
import org.digitalapex.powerbroker.stage.PeriodOverseer;
import org.digitalapex.json.simple.PARSERObject;
import org.digitalapex.json.simple.grabber.PARSERGrabber;
import org.digitalapex.json.simple.grabber.ParseRaiser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommodityGoBetween {
    public static final int MAX_BID = 500;
    public static final int MAX_PEERS = 3; // small limit so we can have and verify a bounding box

    private final TalkersIdentity identity;
    private final PeriodOverseer periodOverseer;
    private CommodityGoBetweenUser commodityGoBetweenUser;

    public CommodityGoBetween(TalkersIdentity identity) {
        this.identity = identity;
        this.periodOverseer = new PeriodOverseerBuilder().defineIdentity(identity).setCommodityGoBetween(this).generatePeriodOverseer();
    }

    public void start(File connectionFile, File commodityFile) throws CommodityGoBetweenRaiser {
        // read the power file and create a generation plan
        // we will print the generation to stdout then use the generation plan to create the bid plan
        // we store the bid plan for later use when we want to start auctions
        BidPlan bidPlan = processFromFile(commodityFile);

        // read the connection list and start connecting to the other powerbroker instances
        // read the connection list, we expect it to be in the format:
        // <hostname or ip>:<port>
        // <hostname or ip>:<port>
        TalkersNetworkAddress us = identity.obtainCallbackAddress();
        List<TalkersNetworkAddress> peers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(connectionFile))) {
            String line;
            int peerCount = 0;
            boolean amIn = false;
            while ((line = br.readLine()) != null && peerCount < MAX_PEERS) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    throw new CommodityGoBetweenRaiser("Invalid line: " + line);
                }
                TalkersNetworkAddress peer = new TalkersNetworkAddress(parts[0], Integer.valueOf(parts[1]));
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
            throw new CommodityGoBetweenRaiser(e);
        }

        // the phaseManager starts the connection phase where connect to others
        // and organize our connections
        periodOverseer.start(peers, us, bidPlan);
    }

    private BidPlan processFromFile(File file) throws CommodityGoBetweenRaiser {
        try {
            PARSERGrabber grabber = new PARSERGrabber();
            PARSERObject parser = (PARSERObject) grabber.parse(new FileReader(file));
            CommodityProfile profile = CommodityProfile.fromParser(parser);
            CommodityAnalyzer analyzer = CommodityAnalyzerFactory.generate();
            GenerationPlan generationPlan = analyzer.generateGenerationPlan(profile);
            System.out.println(generationPlan);

            BidPlan bidPlan = analyzer.generateBidPlan(generationPlan, profile.takeBudget());

            System.out.println(bidPlan);

            return bidPlan;
        } catch (IOException e) {
            throw new CommodityGoBetweenRaiser(e);
        } catch (ParseRaiser e) {
            throw new CommodityGoBetweenRaiser(e);
        }
    }

    public void stop() {
        periodOverseer.stop();
    }

    public void defineCommodityGoBetweenUser(CommodityGoBetweenUser commodityGoBetweenUser) {
        this.commodityGoBetweenUser = commodityGoBetweenUser;
    }

    public CommodityGoBetweenUser takeCommodityGoBetweenUser() {
        return commodityGoBetweenUser;
    }

    public TalkersIdentity getIdentity() {
        return identity;
    }
}
