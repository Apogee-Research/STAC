/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.proof;

import client.support.BTReeMgrClient;
import client.support.BTReeMgrClient;
import client.support.CollabConnException;
import client.support.CollabConnException;
import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author user
 */
public class ProofClientWrapper {
    
    public static BTReeMgrClient client;
    public int ops = 0;
    
    public ProofClientWrapper(){
     client = new BTReeMgrClient();
    }

    void login(String user) throws IOException, CollabConnException {
    
        ops++;
        client.login(user);
    }

    void initSandbox() throws IOException, CollabConnException {
        ops++;
        client.initSandbox();
    }

    void add(BufferedReader object, int i) throws CollabConnException {
    
        ops++;
        client.add(object, i, "");
    }

    int[] doSearchID(BufferedReader object, int i, int i0) throws CollabConnException {

        ops++;
        return client.doSearchID(object, i, i0);
    }
}
