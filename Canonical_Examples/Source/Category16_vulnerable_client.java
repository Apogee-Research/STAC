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

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Random;

public class Category16_vulnerable_client {
    private static final int port = 8443;
    private static final int maxNumBits = 32;
    private static final String initiator = "!";
    private static final String terminator = "~";
    private static BigInteger originalSize;
    private static SSLSocket client;

    private static int[] encodeBlock(int[] rawBlock) {
        int[][] generatorMatrix = {
                {1, 1, 0, 1},
                {1, 0, 1, 1},
                {1, 0, 0, 0},
                {0, 1, 1, 1},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}};
        int[] encodedBlock = {0, 0, 0, 0, 0, 0, 1};
        int sum;
        for (int i = 0; i < encodedBlock.length; i++) {
            sum = 0;
            for (int j = 0; j < rawBlock.length; j++) {
                sum += rawBlock[j] * generatorMatrix[i][j];
            }
            while (sum > 1) {
                sum = sum % 2;
            }
            encodedBlock[i] = sum;
        }
        return encodedBlock;
    }

    private static BigInteger addBlock(BigInteger current, int[] toAdd, int startingSize) {
        for (int i = 0; i < toAdd.length; i++) {
            if (toAdd[i] == 1) {
                current = current.setBit(startingSize + i);
            } else {
                current = current.clearBit(startingSize + i);
            }
        }
        return current;
    }

    private static BigInteger encodeData(BigInteger secretInt) {
        //Add padding to ensure number of bits % 4 =0
        int numBits = secretInt.bitLength();
        originalSize = BigInteger.valueOf(numBits);
        int numBlocks = (int) Math.ceil(numBits / 4.0);
        int overflowBits = numBlocks * 4 - numBits;
        for (int i = 0; i < overflowBits; i++) {
            secretInt = secretInt.setBit(numBits + i);
        }

        //Break messages into blocks of length 4
        //Results in blocks of length 7 = 4 data bits + 3 parity bits
        int[] rawBlock = {0, 0, 0, 0};
        int[] encodedBlock;
        int j;
        int numEncoded = 0;
        BigInteger encodedInt = BigInteger.valueOf(0);
        for (int i = 0; i < secretInt.bitLength(); i++) {
            j = i % 4;
            if (j == 0 && i != 0) {
                encodedBlock = encodeBlock(rawBlock);
                encodedInt = addBlock(encodedInt, encodedBlock, numEncoded);
                numEncoded += encodedBlock.length;
            }
            rawBlock[j] = (secretInt.testBit(i)) ? 1 : 0;
        }
        encodedBlock = encodeBlock(rawBlock);
        encodedInt = addBlock(encodedInt, encodedBlock, numEncoded);
        return encodedInt;
    }

    /*As this is an example running on the reference platform, data transmission
    errors resulting in random errors won't occur. Therefore, the error is
    simulated on the client-side prior to transmission.*/
    private static BigInteger generateBitError(BigInteger encodedInt) {
        Random random = new Random();
        BigInteger errorEncodedInt = encodedInt;
        for (int i = 0; i < encodedInt.bitLength(); i += 7) {
            int errorBit = random.nextInt(7);
            if (errorBit != 7) {
                errorEncodedInt = errorEncodedInt.flipBit(errorBit + i);
            }
        }
        return errorEncodedInt;
    }

    private static String toBitString(BigInteger errorEncodedInt) {
        int numBits = errorEncodedInt.bitLength();
        StringBuilder bitStringBuilder = new StringBuilder(numBits);
        int bit;
        for (int i = 0; i < numBits; i++) {
            bit = (errorEncodedInt.testBit(i)) ? 1 : 0;
            bitStringBuilder.append(Integer.toString(bit));
        }
        return bitStringBuilder.toString();
    }

    private static void delay(int delayNum) {
        long start = System.nanoTime();
        while (System.nanoTime() - start < delayNum) {
        }
    }

    private static String sendReceive(String host, String messageBitString) {
        System.setProperty("javax.net.ssl.trustStore", "data/clientkeystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "clientkey");
        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        String response = "-1";
        try {
            client = (SSLSocket) socketFactory.createSocket(host, port);
            client.startHandshake();

            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            //indicate start
            delay(100000000);
            out.println(initiator);
            for (char bitChar : messageBitString.toCharArray()) {
                //side channel leaking encoded + block error bits
                if (bitChar == '1') {
                    delay(30000000);
                }
                delay(30000000);
                out.println(bitChar);
            }
            //indicate end of secret bits
            delay(100000000);
            out.println(terminator);

            String originalSizeBitString = toBitString(originalSize);
            //indicate start originalSize transmission
            delay(100000000);
            out.println(initiator);
            for (char bitChar : originalSizeBitString.toCharArray()) {
                //side channel leaking encoded + block error bits
                if (bitChar == '1') {
                    delay(30000000);
                }
                delay(30000000);
                out.println(bitChar);
            }
            //indicate end of secret bits
            delay(100000000);
            out.println(terminator);

            response = in.readLine();

            client.close();
        } catch (IOException e) {
            System.out.println("Server IO Exception");
            e.printStackTrace();
            System.exit(-1);
        }
        return response;
    }

    public static void main(String[] args) {
        try {
            String host = args[0];
            BigInteger secretInt = new BigInteger(args[1]);
            int numBits = secretInt.bitLength();
            if (numBits > maxNumBits) {
                throw new IllegalArgumentException();
            }
            BigInteger encodedInput = encodeData(secretInt);
            BigInteger errorEncodedInput = generateBitError(encodedInput);
            String bitString = toBitString(errorEncodedInput);

            String response = sendReceive(host, bitString);
            System.out.println("Server Response: " + response);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Required args: <host IP>  <secret integer>");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.out.println("Unable to format input secret integer");
            System.exit(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid secret integer");
            System.exit(-1);
        }
    }
}
