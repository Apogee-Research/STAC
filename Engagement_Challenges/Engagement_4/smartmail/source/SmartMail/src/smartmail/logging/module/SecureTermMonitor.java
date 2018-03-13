/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.logging.module;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author user
 */
public class SecureTermMonitor {

    public static void doubleEntryError() {
        System.out.println("ERROR");
        try {
            String emessage = "ERROR: should not appear twice.";
            Files.write(Paths.get("logs/policyerrors.txt"), emessage.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
        }
    }

}
