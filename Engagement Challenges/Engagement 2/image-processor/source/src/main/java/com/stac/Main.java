package com.stac;

import com.stac.learning.ClusterController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class Main {

    private enum Command {
        TRAIN, CLUSTER, NONE;
    }

    private static Path ccdir = Paths.get(System.getProperty("user.home"), ".imageClustering");

    private static ClusterController
            clusterController = new ClusterController(ccdir);


    static void printHelp() {
        System.out.append("Usage: \n")
                .append("    Arguments: \n")
                .append("       train <filename> <type>\n")
                .append("          Adds this image to the training set.\n")
                .append("       cluster <filename>\n")
                .append("          Tests this file against the training set.\n")
                .append("\n")
                .append("    See ").append(ccdir.toString()).append("/config.cfg for algorithm order configuration.\n");
    }

    public static void main(String[] args) throws IOException {
        boolean status = true;
        switch (parseArgs(args)) {
            case TRAIN:
                if (!setTag(args[1], args[2])) status = false;
                break;
            case CLUSTER:
                if (!clusterAgainstTrainingSet(args[1])) status = false;
                break;
            default:
                printHelp();
                status = false;
        }
        System.exit(status ? 0 : -1);
    }

    private static boolean clusterAgainstTrainingSet(String filename) throws IOException {
        String[] nearestTypes = clusterController.cluster(Paths.get(filename).toFile().getCanonicalFile());

        if (nearestTypes == null) {
            System.err.append("Failed to classify ").append(filename).println();
            return false;
        }

        HashMap<String, Integer> inCluster = new HashMap<>(nearestTypes.length);
        for (String type : nearestTypes) {
            if (inCluster.containsKey(type)) {
                inCluster.put(type, inCluster.get(type) + 1);
            }
            inCluster.put(type, 1);
        }

        LinkedList<Map.Entry<String, Integer>> types = new LinkedList<>(inCluster.entrySet());
        Collections.sort(types, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return Objects.requireNonNull(o2.getValue()) - Objects.requireNonNull(o1.getValue());
            }
        });

        System.out.append("The image ").append(filename).append(" classifies as: ").append(types.getFirst().getKey()).println();
        return true;
    }

    private static boolean setTag(String filename, String tag) throws IOException {
        final Path path = Paths.get(clusterController.trainingImages.toFile().getCanonicalPath(), filename);
        if (path.toFile().getCanonicalPath().equals(path.toAbsolutePath().toString())) {
            return clusterController.setTag(filename, tag);
        }
        throw new FileNotFoundException(path.toString());
    }

    private static Command parseArgs(String[] args) {
        if (args.length == 2 || args.length == 3) {
            if (args[0].equalsIgnoreCase("train") && args.length == 3) return Command.TRAIN;

            if (args[0].equalsIgnoreCase("cluster") && args.length == 2) return Command.CLUSTER;
        }
        return Command.NONE;
    }
}
