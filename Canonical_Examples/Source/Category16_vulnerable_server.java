/*MIT License

Copyright (c) 2017 Apogee Research

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashMap;

public class Category16_vulnerable_server {
    private static final int port = 8443;
    private static final int maxNumBits = 32;
    private static final int maxBitStringLength = 56;
    private static final String initiator = "!";
    private static final String terminator = "~";
    private static HashMap<String, BigInteger> receivedInts;
    private static SSLServerSocket server;

    private static BigInteger fromBitString(String receivedBitString) {
        BigInteger errorEncodedInt = BigInteger.valueOf(0);
        int numBits = receivedBitString.length();
        for (int i = 0; i < numBits; i++) {
            if (receivedBitString.charAt(i) == '0') {
                errorEncodedInt = errorEncodedInt.clearBit(i);
            } else {
                errorEncodedInt = errorEncodedInt.setBit(i);
            }
        }
        return errorEncodedInt;
    }

    private static int[] correctErrorDecodeBlock(int[] encodedBlock) {
        //Determine if single bit error occurred
        int[][] parityCheckMatrix = {
                {1, 0, 1, 0, 1, 0, 1},
                {0, 1, 1, 0, 0, 1, 1},
                {0, 0, 0, 1, 1, 1, 1}};
        int errorPosition = 0;
        int sum;
        for (int i = 0; i < 3; i++) {
            sum = 0;
            for (int j = 0; j < encodedBlock.length; j++) {
                sum += encodedBlock[j] * parityCheckMatrix[i][j];
            }
            while (sum > 1) {
                sum = sum % 2;
            }
            errorPosition += sum * Math.pow(2, i);
        }

        //If single bit error occurred then fix
        if (errorPosition != 0) {
            encodedBlock[errorPosition - 1] = encodedBlock[errorPosition - 1] ^= 1;
        }

        //Decode corrected block
        int[][] decoderMatrix = {
                {0, 0, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1}};
        int[] rawBlock = {0, 0, 0, 1};
        for (int i = 0; i < rawBlock.length; i++) {
            sum = 0;
            for (int j = 0; j < encodedBlock.length; j++) {
                sum += encodedBlock[j] * decoderMatrix[i][j];
            }
            while (sum > 1) {
                sum = sum % 2;
            }
            rawBlock[i] = sum;
        }
        return rawBlock;
    }

    private static BigInteger addBlock(BigInteger current, int[] toAdd, int startingSize, int dataSize) {
        for (int i = 0; i < toAdd.length; i++) {
            if (toAdd[i] == 1) {
                current = current.setBit(startingSize + i);
            } else {
                current = current.clearBit(startingSize + i);
            }
            if (startingSize + i + 1 == dataSize) {
                return current;
            }
        }
        return current;
    }

    private static BigInteger correctErrorDecode(BigInteger errorEncodedInt, int dataSize) {
        //Break received data into blocks of 7
        //Extract 4 blocks of data after correcting for error
        int[] encodedBlock = {0, 0, 0, 0, 0, 0, 1};
        int[] rawBlock;
        int j;
        int numDecoded = 0;
        BigInteger secretInt = BigInteger.valueOf(0);
        for (int i = 0; i < errorEncodedInt.bitLength(); i++) {
            j = i % 7;
            if (j == 0 && i != 0) {
                rawBlock = correctErrorDecodeBlock(encodedBlock);
                secretInt = addBlock(secretInt, rawBlock, numDecoded, dataSize);
                numDecoded += rawBlock.length;
            }
            encodedBlock[j] = (errorEncodedInt.testBit(i)) ? 1 : 0;
        }
        rawBlock = correctErrorDecodeBlock(encodedBlock);
        secretInt = addBlock(secretInt, rawBlock, numDecoded, dataSize);
        return secretInt;
    }

    private static void startServer() {
        System.setProperty("javax.net.ssl.keyStore", "data/keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "serverkey");
        SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        try {
            server = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
            System.out.println("Server Started Port: " + port);
            SSLSocket client;
            PrintWriter out;
            BufferedReader in;
            String remoteKey;
            String bitChar;
            String receivedBitString;
            String recievedDataSizeBitString;
            int bitStringLength;
            int dataSize;
            while (true) {
                client = (SSLSocket) server.accept();

                client.startHandshake();
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                try{
                    //Read in encoded + error bits
                    bitChar = in.readLine();
                    if (!bitChar.contains(initiator)) {
                        throw new IllegalArgumentException();
                    }
                    bitChar = in.readLine();
                    receivedBitString = "";
                    bitStringLength = 0;
                    while (!bitChar.contains(terminator)) {
                        if(bitStringLength > maxBitStringLength){
                            throw new IllegalArgumentException();
                        }
                        receivedBitString += bitChar;
                        bitChar = in.readLine();
                        bitStringLength++;
                    }

                    //Read in dataSize bits
                    bitChar = in.readLine();
                    if (!bitChar.contains(initiator)) {
                        throw new IllegalArgumentException();
                    }
                    bitChar = in.readLine();
                    recievedDataSizeBitString = "";
                    bitStringLength = 0;
                    while (!bitChar.contains(terminator)) {
                        if(bitStringLength > maxBitStringLength){
                            throw new IllegalArgumentException();
                        }
                        recievedDataSizeBitString += bitChar;
                        bitChar = in.readLine();
                        bitStringLength++;
                    }

                    dataSize = fromBitString(recievedDataSizeBitString).intValue();

                    if (receivedBitString.length() * 4 / 7 > maxNumBits) {
                        throw new IllegalArgumentException();
                    }
                    remoteKey = client.getRemoteSocketAddress().toString();

                    BigInteger errorEncodedReceived = fromBitString(receivedBitString);
                    BigInteger decodedReceived = correctErrorDecode(errorEncodedReceived, dataSize);
                    receivedInts.put(remoteKey, decodedReceived);
                    System.out.println("Request Received From: " + remoteKey);
                    System.out.println("\tBits In:" + receivedBitString);
                    System.out.println("\tStored Int: " + decodedReceived);
                    out.println("OK");
                } catch (IllegalArgumentException e) {
                    out.println("Invalid Input");
                }
                client.close();
            }
        } catch (IOException e) {
            System.out.println("Server IO Exception");
            e.printStackTrace();
            System.exit(-1);
        }

    }

    public static void main(String[] args) {
        receivedInts = new HashMap<>();
        startServer();
    }
}
