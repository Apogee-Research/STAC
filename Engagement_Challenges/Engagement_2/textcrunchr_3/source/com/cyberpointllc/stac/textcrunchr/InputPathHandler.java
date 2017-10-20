package com.cyberpointllc.stac.textcrunchr;

import com.cyberpointllc.stac.zipdecompression.ZipDecompressor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class InputPathHandler {

    private static final class FileVisitor extends SimpleFileVisitor<Path> {

        List<String> filepaths;

        FileVisitor() {
            filepaths = new  ArrayList<String>();
        }

        public List<String> getFilepaths() {
            return filepaths;
        }

        @Override
        public FileVisitResult visitFile(Path aFile, BasicFileAttributes aAttrs) throws IOException {
            filepaths.add(aFile.toString());
            return FileVisitResult.CONTINUE;
        }
    }

    public List<String> handleInputPath(String path) throws Exception {
        ZipDecompressor zd = new  ZipDecompressor();
        // assuming path is a file
        try {
            FileInputStream filestream = new  FileInputStream(path);
            if (filestream.available() <= 0) {
                handleInputPathHelper(path);
            }
            // make temp directory to put files in
            Path directory_name = Files.createTempDirectory("");
            directory_name.toFile().deleteOnExit();
            boolean decompressedFully = zd.decompress(path, directory_name.toString());
            if (!decompressedFully) {
                handleInputPathHelper1();
            }
            // walk through directory and return list of filenames
            FileVisitor visitor = new  FileVisitor();
            Files.walkFileTree(directory_name, visitor);
            return visitor.getFilepaths();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return new  ArrayList<String>();
        }
    }

    private void handleInputPathHelper(String path) throws Exception, IOException {
        System.out.println("No content loaded in file:" + path);
    }

    private void handleInputPathHelper1() throws Exception, IOException {
        System.out.println("Warning: results may be incomplete due to zip decompression depth limit");
    }
}
