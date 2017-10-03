package com.stac.image.algorithms.generics;

import com.stac.image.utilities.ShowImage;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.nio.file.FileSystems;

/**
 *
 */
public class CannyEdgeDetectTest {

    private final FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.exists() && pathname.isFile() && (pathname.getName().endsWith(".jpg") || pathname.getName().endsWith(".gif"));
        }
    };

    @Test
    @Ignore
    public void testDetect() throws Exception {
        URL systemResource = ClassLoader.getSystemResource("3kitties.jpg");
        System.out.println(systemResource);
        String file = systemResource.getFile();
        String resdirpath = file.substring(0, file.lastIndexOf(FileSystems.getDefault().getSeparator()));
        File resDir = new File(resdirpath);
        File[] files = resDir.listFiles(filter);

        for (File img : files) {
            new ShowImage(img.getName(), CannyEdgeDetect.detect(ImageIO.read(img), 130, 175));
        }


        Thread.sleep(5000);
    }
}