/*
MIT License

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
SOFTWARE.
*/

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Test {
    public static void main(String[] args) {
        boolean a = Long.toBinaryString(System.nanoTime()).charAt(47) == '1';
        String filename = System.nanoTime() + ".log";
        String line = "a";

        FileWriter fwLog;
        try {
            fwLog = new FileWriter(filename, true);
            for (int i = 0; i < 10; i++) {
                if (Long.toBinaryString(System.nanoTime()).charAt(47) == '1') {
                    //log.publish(line);
                    fwLog.write(line + "\n");
                }
            }
            fwLog.close();
        } catch (IOException e) {
            System.out.println();
        }

    }
}
