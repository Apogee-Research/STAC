package com.stac.learning;

import com.stac.image.ImageProcessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class ClusterController {
    private final String trainingSetFilename = "trainingSet.csv";
    private final String configFilename = "config.cfg";
    private VectorMap trainingMap = new VectorMap();
    public final Path clusteringDir;
    public final Path trainingImages = Paths.get("/", "var", "lib", "trainer", "images");
    public final Path trainingSet;
    public final Properties configuration;

    /**
     * Sets up the cluster controller with the clustering directory.
     *
     * @param clusteringDir The directory that the clustering tree should reside in.
     */
    public ClusterController(Path clusteringDir) {
        this.clusteringDir = clusteringDir;
        if (!clusteringDir.toFile().exists() || !clusteringDir.toFile().isDirectory()) {
            if (clusteringDir.toFile().exists()) {
                clusteringDir.toFile().delete();
            }
            clusteringDir.toFile().mkdirs();
        }
        trainingSet = Paths.get(clusteringDir.toString(), trainingSetFilename);
        if (!trainingSet.toFile().exists()) {
            try {
                createNewTrainingSetFile();
            } catch (IOException e) {
                throw new RuntimeException("Could not create clustering training set csv file.");
            }
        }
        final Path configurationPath = Paths.get(clusteringDir.toString(), configFilename);
        configuration = new Properties();
        try {
            if (!configurationPath.toFile().exists()) {
                createNewConfiguration(configurationPath);
            } else {
                try (InputStream configurationInputStream = new FileInputStream(configurationPath.toFile())) {
                    configuration.loadFromXML(configurationInputStream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create configuration file.");
        }
        if (!trainingImages.toFile().exists()) {
            trainingImages.toFile().mkdirs();
        }
    }

    private void createNewConfiguration(Path configurationPath) throws IOException {
        configurationPath.toFile().createNewFile();

        configuration.setProperty("Algorithms", "WhiteDetector, BlackDetector, BlueDetector, RedDetector, GreenDetector, Intensify, EdgingDetector");
        try (OutputStream os = new FileOutputStream(configurationPath.toFile())) {
            configuration.storeToXML(os, "Default algorithm order");
        }
    }

    /**
     * Read the training set from the clustering tree and run the training.
     */
    private boolean readTrainingSet() {
        HashSet<String> files = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(this.trainingSet.toFile()))) {
            int lineNo = 1;
            reader.readLine(); // Discard csv header
            for (String line; (line = reader.readLine()) != null; lineNo++) {
                String[] split = line.split(", ");
                if (split.length == 2 && !files.contains(split[0])) {
                    files.add(split[0]);
                    try {
                        File imageFromClusteringDir = getImageFromClusteringDir(split[0]);
                        System.out.println("Loading image '" + imageFromClusteringDir);
                        Vector attributeVector = ImageProcessing.getAttributeVector(imageFromClusteringDir, configuration.getProperty("Algorithms"));
                        trainingMap.put(attributeVector, split[1]);
                    } catch (InvalidObjectException e) {
                        System.err.println("Failed to add " + split[0] + " to the training set. Feature detection failed.");
                    } catch (IOException e) {
                        System.err.println("Failed to add " + split[0] + " to the training set. IO Error occurred.");
                    }
                } else if (line.length() > 0) {
                    System.err.println("Skiping line " + lineNo + " due to invalid line formation.");
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("The trainingSet file was erased before successful loading.");
            return false;
        } catch (IOException e) {
            System.err.println("Could not read the trainingSet file.");
            return false;
        }
        return true;
    }

    /**
     * Tags an image in the training set.
     *
     * @param filename The image to tag.
     * @param tag     The tag.
     */
    public boolean setTag(String filename, String tag) {
        final HashMap<String, String> trainingSet = new HashMap<>();
        try (final BufferedReader br = new BufferedReader(new FileReader(this.trainingSet.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] split = line.split("\\s*,\\s*");
                if (split.length == 2 && !split[0].equals("Filename")) {
                    trainingSet.put(split[0], split[1]);
                } else {
                    // ignore malformed lines
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot tag this image in the training set. Training set could not be opened?");
            return false;
        }

        trainingSet.put(filename, tag);

        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(this.trainingSet.toFile()))) {
            bw.write("Filename, Type\n");
            for (Map.Entry<String, String> imageTag : trainingSet.entrySet()) {
                bw.write(imageTag.getKey() + ", " + imageTag.getValue() + '\n');
            }
        } catch (IOException e) {
            System.err.println("Cannot tag this image in the training set. Training set could not be written");
            return false;
        }

        return true;
    }



    /**
     * Retrieve an image from the clustering images tree.
     *
     * @param filename The image to load to extract.
     * @return The path to the image.
     */
    private File getImageFromClusteringDir(String filename) throws IOException {
        final Path path = Paths.get(trainingImages.toFile().getCanonicalPath(), filename);
        if (path.toFile().getCanonicalPath().equals(path.toAbsolutePath().toString())) {
            return Paths.get(trainingImages.toString(), filename).toFile();
        }
        throw new FileNotFoundException(path.toString());
    }

    /**
     * Creates the training set index card.
     *
     * @throws IOException If the index card could not be created.
     */
    private void createNewTrainingSetFile() throws IOException {
        this.trainingSet.toFile().createNewFile();
        try (PrintWriter bw = new PrintWriter(new FileWriter(this.trainingSet.toFile()))) {
            bw.println("Filename, Type");
        }
    }

    /**
     * cluster with a default size of 5 for the k-nearest types.
     *
     * @param file The file to cluster.
     * @return The 5-nearest types;
     */
    public String[] cluster(File file) {
        return cluster(file, 5);
    }

    /**
     * cluster with a provided size (k) for the k-nearest types.
     *
     * @param file The file to cluster.
     * @param k    The value to use as k.
     * @return The k-nearest types;
     */
    public String[] cluster(File file, int k) {
        System.out.println("Loading clustering set...");
        if (!readTrainingSet()) return null;
        System.out.println("Finished loading");
        String[] nearestStrings = new String[k];
        float[] nearestVals = new float[k];
        //Vector[] knearest = new Vector[k];
        try {
            Vector attributeVector = ImageProcessing.getAttributeVector(file, configuration.getProperty("Algorithms"));
            for (Map.Entry<Vector, String> e : trainingMap.entrySet()) {
                float comp = e.getKey().compareTo(attributeVector);
                for (int i = 0; i < nearestVals.length; i++) {
                    // Nearest means closest to 1 in our case.
                    if (comp > nearestVals[i]) {
                        //knearest[i] = e.getKey();
                        nearestVals[i] = comp;
                        nearestStrings[i] = e.getValue();
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return nearestStrings;
    }
}
