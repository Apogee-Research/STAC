package com.cyberpointllc.stac.zipdecompression;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipDecompressor {

    private static final int FILE_LIMIT = 15;

    private static final BigInteger TOTAL_SIZE_LIMIT = BigInteger.valueOf(5 * 1024 * 1024);

    private static final long FILE_SIZE_LIMIT = 3 * 1024 * 1024;

    private Random random;

    private LinkedList<Entry> queue;

    private BigInteger totalSizeDecompressed;

    class Entry {

        Path path;

        Path target;

        Entry(Path p, Path t) {
            path = p;
            target = t;
        }
    }

    public ZipDecompressor() {
        random = new  Random();
        queue = new  LinkedList<Entry>();
    }

    private boolean withinSizeLimit() {
        boolean within = (totalSizeDecompressed.compareTo(TOTAL_SIZE_LIMIT) == -1);
        if (!within) {
            withinSizeLimitHelper();
        }
        return within;
    }

    private boolean withinIndividualFileLimit(long curFileSize) {
        boolean within = curFileSize < FILE_SIZE_LIMIT;
        if (!within) {
            System.out.println("Exceeded file size limit!");
        }
        return within;
    }

    private void adjustTotalSize(long size) {
        adjustTotalSizeHelper(size);
    }

    /**
     * @param filePath: the path of the file to decompress
     * @param outDirPath: the path of a target directory in which to put fully decompressed files
     * @param basePath: path relative to which paths within target directory should be selected
     * @return whether we completed the decompression process (as opposed to stopping early due 
     *            to too many files/recursions)
     * @throws IOException if something goes terribly wrong
     */
    public boolean decompress(String filePath, String outDirPath) throws Exception {
        int fileCount = 0;
        totalSizeDecompressed = BigInteger.valueOf(0);
        decompress_(Paths.get(filePath), Paths.get(outDirPath));
        while (//truly limit how long this can go on
        !queue.isEmpty() && fileCount < FILE_LIMIT && withinSizeLimit()) {
            Entry next = queue.removeFirst();
            decompress_(next.path, next.target);
            fileCount++;
        }
        if (!queue.isEmpty() || !withinSizeLimit()) {
            // we didn't finish decompression
            cleanUpOutput(outDirPath);
            return false;
        }
        return true;
    }

    /**
     * @param filePath: the path of the file to decompress
     * @param outDirPath: the path of a target directory in which to put fully decompressed files
     * @param basePath: path relative to which paths within target directory should be selected
     * 
     */
    protected void decompress_(Path filePath, Path outDirPath) throws Exception {
        //System.out.println("Decompressing " + filePath + " to " + outDirPath);
        File file = filePath.toFile();
        int i = filePath.toString().lastIndexOf(".");
        if (// if it doesn't have an extension
        i < 0) {
            decompress_Helper(filePath, file, outDirPath);
        } else {
            try {
                String ext = filePath.toString().substring(i);
                switch(ext) {
                    case ".tar":
                        processTarFile(filePath, outDirPath);
                        break;
                    case ".BZ2":
                    case ".bz2":
                        processBz2File(filePath, outDirPath);
                        break;
                    case ".ZIP":
                    case ".zip":
                        processZipFile(filePath, outDirPath);
                        break;
                    default:
                        processFlatFile(filePath, outDirPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * remove any still-compressed files from dir
     */
    private void cleanUpOutput(String dir) throws IOException {
        ZippedCleanupVisitor visitor = new  ZippedCleanupVisitor(dir);
        Files.walkFileTree(Paths.get(dir), visitor);
    }

    private String randString() {
        Long l = random.nextLong();
        ZipDecompressorHelper0 conditionObj0 = new  ZipDecompressorHelper0(0);
        if (l < conditionObj0.getValue())
            l = (-1) * l;
        return String.valueOf(l);
    }

    private void handleInputStream(String name, Path target, InputStream is, byte[] buf) throws Exception {
        Path loc = target.resolve(randString()).resolve(Paths.get(name).getFileName());
        File newFile = loc.toFile();
        int count = 0;
        long totalBytesWritten = 0;
        newFile.getParentFile().mkdirs();
        ZipDecompressorHelper1 conditionObj1 = new  ZipDecompressorHelper1(0);
        try (FileOutputStream collector = new  FileOutputStream(loc.toString())) {
            do {
                count = is.read(buf, 0, buf.length);
                if (count > 0)
                    collector.write(buf, 0, count);
                adjustTotalSize(count);
                totalBytesWritten += count;
            } while (count > conditionObj1.getValue() && withinIndividualFileLimit(totalBytesWritten) && withinSizeLimit());
            if (withinIndividualFileLimit(totalBytesWritten) && withinSizeLimit()) {
                handleInputStreamHelper(target, loc);
            } else {
                // too big
                throw new  RuntimeException("File is too large...");
            }
        }
    }

    private void handleCleanup(Path file, Path target) throws IOException {
        // if we are already working in the target directory, clean up zip files that have already been processed
        // (but if we're dealing with the original copy in the user's original directory, we should leave it there.)
        long size = 0;
        if (file.startsWith(target)) {
            File theFile = file.toFile();
            size = theFile.length();
            // delete zip files from the target directory once they've been processed
            Files.delete(file);
            Path parent = file.getParent();
            // if dir is empty delete it too (should fail if it's not empty
            Files.delete(parent);
        }
        adjustTotalSize(-1 * size);
    }

    public void processDirectory(Path dir, Path target) {
        File file = dir.toFile();
        for (File f : file.listFiles()) {
            queue.add(new  Entry(f.toPath(), target));
        }
    }

    public void processFlatFile(Path file, Path target) throws IOException {
        if (!file.startsWith(target)) {
            processFlatFileHelper(file, target);
        }
    }

    public void processTarFile(Path file, Path target) throws Exception {
        processTarFileHelper(file, target);
    }

    /**
     * @param file path of bz2 file to process
     * @param target path of directory to put it in
     */
    public void processBz2File(Path file, Path target) throws Exception {
        processBz2FileHelper(file, target);
    }

    /**
     * @param file path of zip file to process
     * @param target path of directory to put it in
     */
    public void processZipFile(Path file, Path target) throws Exception {
        processZipFileHelper(file, target);
    }

    private class ZipDecompressorHelper0 {

        public ZipDecompressorHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ZipDecompressorHelper1 {

        public ZipDecompressorHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private void withinSizeLimitHelper() {
        System.out.println("Exceeded total size limit!");
    }

    private void adjustTotalSizeHelper(long size) {
        totalSizeDecompressed = totalSizeDecompressed.add(BigInteger.valueOf(size));
    }

    private void decompress_Helper(Path filePath, File file, Path outDirPath) throws Exception {
        try {
            if (file.isDirectory()) {
                // if it's a directory, put its contents in the temp dir for further processing
                processDirectory(filePath, outDirPath);
            } else {
                //it's a file: just put it in the target dir
                processFlatFile(filePath, outDirPath);
            }
        } catch (IOException e) {
            System.err.println("Unable to copy file");
            e.printStackTrace();
        }
    }

    private void handleInputStreamHelper(Path target, Path loc) throws Exception {
        queue.add(new  Entry(loc, target));
    }

    private void processFlatFileHelper(Path file, Path target) throws IOException {
        Path dest = target.resolve(randString()).resolve(file.getFileName());
        dest.toFile().getParentFile().mkdirs();
        Files.copy(file, dest);
    }

    private void processTarFileHelper(Path file, Path target) throws Exception {
        try (TarArchiveInputStream tis = new  TarArchiveInputStream(new  FileInputStream(file.toString()))) {
            TarArchiveEntry entry;
            int entryCount = 0;
            byte[] buf = new byte[1024];
            while (// limit breadth
            (entry = tis.getNextTarEntry()) != null && entryCount < FILE_LIMIT) {
                entryCount++;
                String name = entry.getName();
                long len = entry.getSize();
                int count = 0;
                if (!entry.isDirectory()) {
                    // ignore directories
                    handleInputStream(name, target, tis, buf);
                }
            }
            handleCleanup(file, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processBz2FileHelper(Path file, Path target) throws Exception {
        String fileName = file.toString();
        String outFileName = fileName.replace(".bz2", "").replace(".BZ2", "");
        try (BZip2CompressorInputStream bis = new  BZip2CompressorInputStream(new  BufferedInputStream(new  FileInputStream(fileName)))) {
            byte[] buf = new byte[1024];
            handleInputStream(outFileName, target, bis, buf);
            handleCleanup(file, target);
        }
    }

    private void processZipFileHelper(Path file, Path target) throws Exception {
        try (ZipInputStream zis = new  ZipInputStream(new  FileInputStream(file.toString()))) {
            ZipEntry entry;
            int entryCount = 0;
            byte[] buf = new byte[1024];
            while (// limit breadth
            (entry = zis.getNextEntry()) != null && entryCount < FILE_LIMIT) {
                entryCount++;
                String name = entry.getName();
                long len = entry.getSize();
                if (!entry.isDirectory()) {
                    // ignore directories
                    handleInputStream(name, target, zis, buf);
                }
            }
            handleCleanup(file, target);
        }
    }
}
