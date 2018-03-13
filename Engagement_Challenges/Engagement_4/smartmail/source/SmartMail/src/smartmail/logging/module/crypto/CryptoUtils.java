/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.logging.module.crypto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * A utility class that encrypts or decrypts a file.
 *
 * @author www.codejava.net
 *
 */
public class CryptoUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public static void encrypt(String key, String inputFile, File outputFile)
            throws CryptoException {
        enCrypto(key, inputFile, outputFile);
    }

    public static String decrypt(String key, File inputFile, String outputFile)
            throws CryptoException {
        return deCrypto(key, inputFile);
    }

    public static String strencrypt(String key, String inputFile)
            throws CryptoException {
        return strCrypto(Cipher.ENCRYPT_MODE, key, inputFile);
    }

    public static String strdecrypt(String key, String inputFile)
            throws CryptoException {
        return strCrypto(Cipher.DECRYPT_MODE, key, inputFile);
    }

    private static String strCrypto(int mode, String key, String inputFile) throws CryptoException {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(mode, secretKey);

            byte[] outputBytes = cipher.doFinal(inputFile.getBytes("ISO-8859-1"));

            String string = new String(outputBytes, "ISO-8859-1");
            String encode = java.net.URLEncoder.encode(string, "UTF-8");
            return encode;

        } catch (NoSuchPaddingException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (InvalidKeyException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (BadPaddingException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (IllegalBlockSizeException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (IOException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }
    }

    private static void enCrypto(String key, String inputFile,
            File outputFile) throws CryptoException {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(inputFile.getBytes("ISO-8859-1"));
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        } catch (NoSuchPaddingException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (InvalidKeyException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (BadPaddingException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (IllegalBlockSizeException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (IOException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }
    }

    private static String deCrypto(String key, File inputFile) throws CryptoException {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            String outputFile = new String(outputBytes, "ISO-8859-1");

            inputStream.close();

            return outputFile;
        } catch (NoSuchPaddingException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (InvalidKeyException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (BadPaddingException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (IllegalBlockSizeException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        } catch (IOException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }
    }

    public static String read(String fstr) throws IOException, InterruptedException {
        //fswatch -xr /dirs/mydir2
        String readLine = "0";
        File f1 = new File("dirs/mydir2/" + fstr);
        if (f1.exists()) {

            FileReader fw = new FileReader(f1.getAbsoluteFile());
            BufferedReader bw = new BufferedReader(fw);
            readLine = bw.readLine();

            bw.close();

            System.out.println(f1.getName());
            //Thread.sleep(500);
        }
        return readLine;
    }

    public static void write(String fstr, String contents) throws IOException, InterruptedException {
        //fswatch -xr /dirs/mydir2
        File f1 = new File("dirs/mydir2/" + fstr);
        if (!f1.exists()) {
            f1.createNewFile();
        }

        FileWriter fw = new FileWriter(f1.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(contents);
        bw.flush();
        bw.close();

        System.out.println(f1.getName());
        //Thread.sleep(500);
    }

    static String seclog = "addlog1.log";
    static String reglog = "addlog2.log";

    public static void write(String strencrypt, int cnt, boolean sec) {

        //STAC: The actual vulnerability -- It writes secret address logs to a different file
        String logtowr = null;
        if (sec) {
            logtowr = seclog;
        }
        if (!sec) {
            logtowr = reglog;
        }

        try {

            Files.write(Paths.get("logs/" + logtowr), strencrypt.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
        }
        //STAC: This part below should be removed -- just for testing
        //TODO: This part below should be removed -- just for testing
        /*try {
            System.out.println("fake logs-order still here");
            String l = "./logs/ MODIFY " + logtowr + "\n";
            Files.write(Paths.get("logs/order"), l.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
        }*/

    }
}
