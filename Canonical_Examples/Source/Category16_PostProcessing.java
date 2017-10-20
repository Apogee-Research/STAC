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

import java.math.BigInteger;

public class Category16_PostProcessing {
    private static final int maxNumBits = 32;

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

    public static void main(String[] args) {
        try {
            String extractedBitString = args[0];
            String extractedOriginalSizeBitString = args[1];
            int numBits = extractedBitString.length();
            if (numBits * 4 / 7 > maxNumBits) {
                throw new IllegalArgumentException();
            }
            int dataSize = fromBitString(extractedOriginalSizeBitString).intValue();
            BigInteger errorEncodedReceived = fromBitString(extractedBitString);
            BigInteger decodedReceived = correctErrorDecode(errorEncodedReceived, dataSize);
            System.out.println(decodedReceived);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Required args: <extracted bit string> <extracted original size bit string>");
            System.exit(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid bit string");
            System.exit(-1);
        }
    }
}
