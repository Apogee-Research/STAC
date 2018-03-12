package stac.parser;

import stac.codecs.DecInputStream;
import stac.util.Hex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;

/**
 *
 */
public class OpenSSLRSAPEM {

    public enum DER_TYPE {
        PRIVATE_KEY,
        PUBLIC_KEY;
        private static final String privateHeader = "-----BEGIN RSA PRIVATE KEY-----";
        private static final String publicHeader = "-----BEGIN PUBLIC KEY-----";
        private static final String privateFooter = "-----END RSA PRIVATE KEY-----";
        private static final String publicFooter = "-----END PUBLIC KEY-----";

        public String getHeader() {
            if (this == PRIVATE_KEY) {
                return privateHeader;
            } else {
                return publicHeader;
            }
        }

        public String getFooter() {
            if (this == PRIVATE_KEY) {
                return privateFooter;
            } else {
                return publicFooter;
            }
        }

        public static DER_TYPE typeOf(String s) {
            switch (s) {
                case privateHeader:
                case privateFooter:
                    return PRIVATE_KEY;
                case publicHeader:
                case publicFooter:
                    return PUBLIC_KEY;
            }
            return null;
        }
    }

    private DER_TYPE type;
    private INTEGER version;
    private INTEGER modulus;
    private INTEGER publicExponent;
    private INTEGER privateExponent;
    private INTEGER prime1;
    private INTEGER prime2;
    private INTEGER exponent1;
    private INTEGER exponent2;
    private INTEGER coefficient;

    /**
     * Using this constructor will help build a public key from the provided information.
     *
     * @param publicExponent The public exponent to use at the exponent in the public key.
     * @param modulus        The modulus for the public key.
     */
    public OpenSSLRSAPEM(INTEGER publicExponent, INTEGER modulus) {
        this.type = DER_TYPE.PUBLIC_KEY;
        this.publicExponent = publicExponent;
        this.modulus = modulus;
    }

    /**
     * Using this constructor will help build a private key from the following information.
     * <p>
     * The information in this key is not the complete key and the PEM object shouldn't be used to create PEM files.
     *
     * @param publicExponent  The public exponent to use at the exponent in the public key.
     * @param modulus         The modulus for the public key.
     * @param privateExponent The private exponent of the key.
     */
    public OpenSSLRSAPEM(INTEGER publicExponent, INTEGER modulus, INTEGER privateExponent) {
        this.type = DER_TYPE.PRIVATE_KEY;
        this.publicExponent = publicExponent;
        this.privateExponent = privateExponent;
        this.modulus = modulus;
    }

    /**
     * Using this contstructor will help build a private key from all of the information available.
     * <p>
     * This information can be used to produce valid PEM files.
     *
     * @param publicExponent  The public exponent to use at the exponent in the public key.
     * @param modulus         The modulus for the public key.
     * @param privateExponent The private exponent of the key.
     * @param prime1          The value of prime p during the creation of the key.
     * @param prime2          The value of prime q during the creation of the key.
     * @param exponent1       The value of d mod(p - 1)
     * @param exponent2       The value of e mod(q - 1)
     * @param coefficient     The value of q^-1 (mod p)
     */
    public OpenSSLRSAPEM(INTEGER publicExponent, INTEGER modulus, INTEGER privateExponent, INTEGER prime1, INTEGER prime2, INTEGER exponent1, INTEGER exponent2, INTEGER coefficient) {
        this.type = DER_TYPE.PRIVATE_KEY;
        this.publicExponent = publicExponent;
        this.privateExponent = privateExponent;
        this.modulus = modulus;
        this.prime1 = prime1;
        this.prime2 = prime2;
        this.exponent1 = exponent1;
        this.exponent2 = exponent2;
        this.coefficient = coefficient;
    }

    public OpenSSLRSAPEM(InputStream is) throws IOException {
        final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8.toString());
        BufferedReader reader = new BufferedReader(isr);

        final String s = reader.readLine();
        if (s == null) throw new IOException("Failed to read first line of input stream");
        type = DER_TYPE.typeOf(s);
        // There is probably a nicer stream method of doing this
        final String armoredContent = readUntilFooter(reader);
        final InputStream decoded = new DecInputStream(new ByteArrayInputStream(armoredContent.getBytes(StandardCharsets.UTF_8)));
        final DER.DERTLV dertlv = DER.readDER(decoded);

        if (type == DER_TYPE.PRIVATE_KEY) {
            if (dertlv.getTag() == DER.SEQUENCE) {
                final ArrayList<DER.DERTLV> retrieve = (ArrayList<DER.DERTLV>) dertlv.retrieve();
                if (retrieve.size() < 9) throw new IOException("Bad Private Key: Missing required params");
                if (retrieve.get(0).getTag() != DER.INTEGER) throw new IOException("Bad Private Key: Invalid version");
                else version = (INTEGER) retrieve.get(0).retrieve();
                if (retrieve.get(1).getTag() != DER.INTEGER) throw new IOException("Bad Private Key: Invalid modulus");
                else modulus = (INTEGER) retrieve.get(1).retrieve();
                if (retrieve.get(2).getTag() != DER.INTEGER)
                    throw new IOException("Bad Private Key: Invalid publicExponent");
                else publicExponent = (INTEGER) retrieve.get(2).retrieve();
                if (retrieve.get(3).getTag() != DER.INTEGER)
                    throw new IOException("Bad Private Key: Invalid privateExponent");
                else privateExponent = (INTEGER) retrieve.get(3).retrieve();
                if (retrieve.get(4).getTag() != DER.INTEGER) throw new IOException("Bad Private Key: Invalid prime1");
                else prime1 = (INTEGER) retrieve.get(4).retrieve();
                if (retrieve.get(5).getTag() != DER.INTEGER) throw new IOException("Bad Private Key: Invalid prime2");
                else prime2 = (INTEGER) retrieve.get(5).retrieve();
                if (retrieve.get(6).getTag() != DER.INTEGER)
                    throw new IOException("Bad Private Key: Invalid exponent1");
                else exponent1 = (INTEGER) retrieve.get(6).retrieve();
                if (retrieve.get(7).getTag() != DER.INTEGER)
                    throw new IOException("Bad Private Key: Invalid exponent2");
                else exponent2 = (INTEGER) retrieve.get(7).retrieve();
                if (retrieve.get(8).getTag() != DER.INTEGER)
                    throw new IOException("Bad Private Key: Invalid coefficient");
                else coefficient = (INTEGER) retrieve.get(8).retrieve();
            } else {
                throw new IOException("Bad Private Key");
            }
        } else if (type == DER_TYPE.PUBLIC_KEY) {
            if (dertlv.getTag() == DER.SEQUENCE) {
                ArrayList<DER.DERTLV> sequence = (ArrayList<DER.DERTLV>) dertlv.retrieve();
                if (sequence.get(0).getTag() != DER.SEQUENCE)
                    throw new IOException("Bad Public Key: Invalid Public Key");
                final ArrayList<DER.DERTLV> OIDSeq = (ArrayList<DER.DERTLV>) sequence.get(0).retrieve();
                if (OIDSeq.get(0).getTag() == DER.OBJECT_IDENTIFIER) {
                    try {
                        // We throw if we enounter strange OIDs
                        OIDSeq.get(0).retrieve();
                    } catch (RuntimeException e) {
                        throw new IOException("Bad Public Key: Invalid OID");
                    }
                } else {
                    throw new IOException("Bad Public Key: Invalid OID");
                }
                if (sequence.get(1).getTag() == DER.BIT_STRING) {
                    BITSTRING bs = (BITSTRING) sequence.get(1).retrieve();
                    ByteArrayInputStream bais = new ByteArrayInputStream(bs.toByteArray());
                    final DER.DERTLV publicKeyDoc = DER.readDER(bais);
                    if (publicKeyDoc.getTag() != DER.SEQUENCE) {
                        throw new IOException("Bad Public Key: Invalid Public Key '" + Hex.bytesToHex(publicKeyDoc.value) + "'");
                    }
                    final ArrayList<DER.DERTLV> retrieve = (ArrayList<DER.DERTLV>) publicKeyDoc.retrieve();
                    if (retrieve.size() != 2) throw new IOException("Bad Public Key: Missing required params");
                    if (retrieve.get(0).getTag() != DER.INTEGER)
                        throw new IOException("Bad Private Key: Invalid modulus");
                    else modulus = (INTEGER) retrieve.get(0).retrieve();
                    if (retrieve.get(1).getTag() != DER.INTEGER)
                        throw new IOException("Bad Private Key: Invalid publicExponent");
                    else publicExponent = (INTEGER) retrieve.get(1).retrieve();
                }
            } else {
                throw new IOException("Bad Public Key");
            }
        } else {
            throw new IOException("Unknown Key Type");
        }
    }

    private String readUntilFooter(BufferedReader reader) throws IOException {
        String footer = type != null ? type.getFooter() : null;
        StringBuilder b = new StringBuilder();
        String t;
        while (!Objects.equals(t = reader.readLine(), footer)) {
            b.append(t);
        }
        return b.toString();
    }

    public OpenSSLRSAPEM(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public INTEGER getModulus() {
        return modulus;
    }

    public INTEGER getPublicExponent() {
        return publicExponent;
    }

    public INTEGER getPrivateExponent() {
        return privateExponent;
    }

    public DER_TYPE getType() {
        return type;
    }

    public INTEGER getVersion() {
        return version;
    }

    public INTEGER getPrime1() {
        return prime1;
    }

    public INTEGER getPrime2() {
        return prime2;
    }

    public INTEGER getExponent1() {
        return exponent1;
    }

    public INTEGER getExponent2() {
        return exponent2;
    }

    public INTEGER getCoefficient() {
        return coefficient;
    }

    public static class DER {
        final public static byte BOOLEAN = 0x01;
        final public static byte INTEGER = 0x02;
        final public static byte BIT_STRING = 0x03;
        final public static byte OCTET_STRING = 0x04;
        final public static byte NULL = 0x05;
        final public static byte OBJECT_IDENTIFIER = 0x06;
        final public static byte UTF8String = 0x0C;
        final public static byte PrintableString = 0x13;
        final public static byte IA5String = 0x16;
        final public static byte BMPString = 0x1E;
        final public static byte SEQUENCE = 0x30;
        final public static byte SET = 0x31;

        /**
         * Parses the DER document provided by is.
         *
         * @param is Must be a RAW DER document. This means un-armored.
         * @return The initial TLV triplet in its decoded form.
         * @throws EOFException
         */
        public static DERTLV readDER(InputStream is) throws EOFException {
            return new DERTLV(is);
        }

        public static class DERTLV {
            private final InputStream iis;
            private final byte tag;
            private final INTEGER len = new INTEGER();
            private byte[] value;
            private Object retrieved = null;

            public DERTLV(byte[] bytes) throws EOFException {
                this(new ByteArrayInputStream(bytes));
            }

            public DERTLV(InputStream data) throws EOFException {
                iis = data;
                try {
                    if (data.available() > 0) {
                        byte[] readTag = new byte[1];
                        if (data.read(readTag) != 1) {
                            throw new MalformedDERException("Tag read failed");
                        }
                        tag = readTag[0];
                        if (data.read(readTag) != 1) {
                            throw new MalformedDERException("Length read failed");
                        }

                        if ((readTag[0] & 0x80) == 0x80) {
                            final int extraLenBytes = (readTag[0] ^ (byte) 0x80);
                            byte[] readExtraLen = new byte[extraLenBytes];
                            if (data.read(readExtraLen) != extraLenBytes) {
                                throw new MalformedDERException("Extra length read failed");
                            }
                            len.setUnsigned(readExtraLen);
                        } else {
                            len.set(readTag[0]);
                        }
                    } else {
                        throw new EOFException("No more bytes");
                    }
                } catch (EOFException e) {
                    throw e;
                } catch (IOException e) {
                    throw new MalformedDERException("IOException occurred", e);
                }

                try {
                    if (len.intExactValue() > 0) {
                        value = new byte[len.intExactValue()];
                        final int read = iis.read(value);
                        if (read != len.intExactValue()) {
                            throw new MalformedDERException("Value read failed " + read);
                        }
                    }
                } catch (IOException e) {
                    throw new MalformedDERException("IOException occurred", e);
                }
            }

            public byte getTag() {
                return tag;
            }

            public OpenSSLRSAPEM.INTEGER getLen() {
                return len;
            }

            public byte[] getValue() {
                return value;
            }

            public Object retrieve() {
                if (retrieved == null) {
                    ByteArrayInputStream valueAsStream;
                    DERTLV local;
                    switch (tag) {
                        case BOOLEAN:
                            retrieved = OpenSSLRSAPEM.BOOLEAN.valueOf(value[0]);
                            break;
                        case INTEGER:
                            retrieved = OpenSSLRSAPEM.INTEGER.valueOf(value);
                            break;
                        case BIT_STRING:
                            retrieved = OpenSSLRSAPEM.BITSTRING.valueOf(value);
                            break;
                        case OCTET_STRING:
                            retrieved = OpenSSLRSAPEM.OCTETSTRING.valueOf(value);
                            break;
                        case NULL:
                            retrieved = new OpenSSLRSAPEM.NULL();
                            break;
                        case OBJECT_IDENTIFIER:
                            retrieved = OpenSSLRSAPEM.OBJECT_IDENTIFIER.valueOf(value);
                            break;
                        case UTF8String:
                            retrieved = new String(value, StandardCharsets.UTF_8);
                            break;
                        case PrintableString:
                            // PrintableString is the printable subset of iso646 (US_ASCII)
                            retrieved = new String(value, StandardCharsets.US_ASCII);
                            break;
                        case IA5String:
                            // Full iso646 (US_ASCII) charset of 128 7-bit chars
                            retrieved = new String(value, StandardCharsets.US_ASCII);
                            break;
                        case BMPString:
                            System.out.println("Warning: decoding ISO 10646-1 as UTF_16; NO LE/BE detection performed");
                            retrieved = new String(value, StandardCharsets.UTF_16);
                            break;
                        case SEQUENCE:
                            // SEQUENCE order is important
                            valueAsStream = new ByteArrayInputStream(value);
                            ArrayList<DERTLV> sequence = new ArrayList<>();
                            try {
                                while ((local = new DERTLV(valueAsStream)) != null) {
                                    sequence.add(local);
                                }
                            } catch (EOFException ignored) {
                            }
                            retrieved = sequence;
                            break;
                        case SET:
                            // SETS are unordered but don't seem to require unique items... I do
                            valueAsStream = new ByteArrayInputStream(value);
                            TreeSet<DERTLV> set = new TreeSet<>();
                            try {
                                while ((local = new DERTLV(valueAsStream)) != null) {
                                    set.add(local);
                                }
                            } catch (EOFException ignored) {
                            }
                            retrieved = set;
                            break;
                        default:
                            throw new MalformedDERException("Invalid tag type");
                    }
                }
                return retrieved;
            }
        }

        public static class MalformedDERException extends RuntimeException {
            private static final long serialVersionUID = -8452231334791152338L;

            public MalformedDERException(String message, Throwable throwable) {
                super("Malformed DER: " + message, throwable);
            }

            public MalformedDERException(String message) {
                super("Malformed DER: " + message);
            }
        }
    }

    public static class OBJECT_IDENTIFIER {
        String string;
        private final byte[] rsaPKCS1 = {(byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xF7, (byte) 0x0D, (byte) 0x01, (byte) 0x01, (byte) 0x01};

        public OBJECT_IDENTIFIER(byte[] value) {
            if (Arrays.equals(rsaPKCS1, value)) {
                string = "1.2.840.113549.1.1.1";
            } else {
                throw new RuntimeException("Not implemented");
            }
        }

        public static OBJECT_IDENTIFIER valueOf(byte[] value) {
            return new OBJECT_IDENTIFIER(value);
        }
    }

    public static class OCTETSTRING {
        byte[] string;

        public OCTETSTRING(byte[] value) {
            string = value.clone();
        }

        public static OCTETSTRING valueOf(byte[] value) {
            return new OCTETSTRING(value);
        }
    }

    public static class BOOLEAN {
        boolean v = false;

        public BOOLEAN(byte value) {
            // This is BER and not DER, but I don't care
            v = (value != 0x00);
        }

        public static BOOLEAN valueOf(byte value) {
            return new BOOLEAN(value);
        }
    }

    public static class BITSTRING {
        int unused;
        byte[] values;

        public BITSTRING(byte[] value) {
            values = new byte[value.length - 1];
            unused = value[0];

            for (int i = 1; i < value.length; i++) {
                values[i - 1] = value[i];
            }
        }

        public byte[] toByteArray() {
            return values;
        }

        public int getUnused() {
            return unused;
        }

        public static BITSTRING valueOf(byte[] value) {
            return new BITSTRING(value);
        }
    }

    public static class NULL {

    }

    public static class INTEGER implements Comparable {
        private Integer internal = null;
        private BigInteger internalBig = null;
        public static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
        public static final BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
        private static final SecureRandom SECURE_PRNG = new SecureRandom();

        public INTEGER() {
            set(0);
        }

        public INTEGER(BigInteger bigInteger) {
            set(bigInteger.toByteArray());
        }

        public INTEGER(INTEGER other) {
            set(other.getBytes());
        }

        public INTEGER(Integer internal) {
            set(internal);
        }

        public static INTEGER valueOf(String in) {
            INTEGER i = new INTEGER();
            i.set(in);
            return i;
        }

        public static INTEGER valueOf(byte[] bytes, int offset, int length) {
            if (offset == 0 && length == bytes.length) return INTEGER.valueOf(bytes);
            byte[] read = new byte[length];
            System.arraycopy(bytes, offset, read, 0, length);
            INTEGER i = new INTEGER();
            i.set(read);
            return i;
        }

        public static INTEGER valueOf(byte[] bytes) {
            INTEGER integer = new INTEGER();
            integer.set(bytes);
            return integer;
        }

        public static INTEGER valueOfUnsigned(byte[] bytes) {
            INTEGER integer = new INTEGER();
            integer.setUnsigned(bytes);
            return integer;
        }

        public static INTEGER valueOfUnsigned(byte[] bytes, int off, int length) {
            INTEGER integer = new INTEGER();
            byte[] intern = new byte[length];
            System.arraycopy(bytes, off, intern, 0, length);
            integer.setUnsigned(intern);
            return integer;
        }

        public static INTEGER valueOf(long in) {
            INTEGER i = new INTEGER();
            i.set(in);
            return i;
        }

        public void set(String in) {
            boolean b16 = false;
            if (in.contains(":")) {
                b16 = true;
                in = in.replaceAll("[:\n ]", "");
            }
            if (b16) {
                if (in.length() > 4) {
                    internalBig = new BigInteger(in, 16);
                } else {
                    final byte[] bytes = in.getBytes(StandardCharsets.UTF_8);
                    internal = 0;
                    for (int i = 0; i < bytes.length; i++) {
                        internal &= bytes[0] << i;
                    }
                }
            } else {
                try {
                    this.internal = Integer.parseInt(in);
                } catch (NumberFormatException e) {
                    this.internalBig = new BigInteger(in);
                }
            }
        }

        public void setUnsigned(byte[] bytes) {
            byte[] these_bytes;
            if ((bytes[0] & 0x80) == 0x80) {
                these_bytes = new byte[bytes.length + 1];
                for (int i = 0; i < bytes.length; i++) {
                    these_bytes[i + 1] = bytes[i];
                }
            } else {
                these_bytes = bytes;
            }
            set(these_bytes);
        }

        public void set(byte[] bytes) {
            final BigInteger bigInteger = new BigInteger(bytes);
            if (bigInteger.compareTo(INT_MAX) > 0 || bigInteger.compareTo(INT_MIN) < 0) {
                internalBig = bigInteger;
                internal = null;
            } else {
                internalBig = null;
                internal = bigInteger.intValue();
            }
        }

        public void setUnsigned(byte b) {
            set(((int) b) & 0xFF);
        }

        public void setUnsigned(int i) {
            set((long) ((long) i) & ((long) 0xFFFFFFFF));
        }

        public void set(long i) {
            internalBig = BigInteger.valueOf(i);
            internal = null;
        }

        public void set(int i) {
            internalBig = null;
            internal = i;
        }

        public void set(BigInteger i) {
            internalBig = i;
            internal = null;
        }

        public boolean isBig() {
            return internalBig != null;
        }

        public Integer getInternal() {
            if (!isBig())
                return internal;
            else
                return internalBig.intValue();
        }

        public BigInteger getInternalBig() {
            if (isBig())
                return internalBig;
            else
                return internalBig = BigInteger.valueOf(internal);
        }

        public Object get() {
            return isBig() ? internalBig : internal;
        }

        public int intExactValue() {
            if (isBig())
                return internalBig.intValue();
            else
                return internal;
        }

        public INTEGER modPow(INTEGER pow, INTEGER modulus) {
            if (pow.isBig()) {
                return new INTEGER(this.getInternalBig().modPow(pow.getInternalBig(), modulus.getInternalBig()));
            } else {
                return new INTEGER(this.getInternalBig().pow(pow.getInternal()).mod(modulus.getInternalBig()));
            }
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         * <p/>
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         * <p/>
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * <p/>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * <p/>
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         * <p/>
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(Object o) {
            INTEGER oo;
            if (o instanceof BigInteger) {
                oo = new INTEGER((BigInteger) o);
            }  else if (o instanceof Integer) {
                oo = INTEGER.valueOf((int) o);
            } else if (o instanceof Number) {
                oo = INTEGER.valueOf(o.toString());
            } else {
                oo = (INTEGER) o;
            }

            if (isBig() || oo.isBig()) {
                BigInteger loc;
                if (!isBig()) {
                    loc = BigInteger.valueOf(internal);
                } else {
                    loc = internalBig;
                }
                if (oo.isBig()) {
                    return loc.compareTo(oo.getInternalBig());
                } else {
                    return loc.compareTo(BigInteger.valueOf(oo.getInternal()));
                }
            } else {
                if (internal.longValue() > oo.internal.longValue()) return 1;
                if (internal.longValue() == oo.internal.longValue()) return 0;
                if (internal.longValue() < oo.internal.longValue()) return -1;
            }
            throw new RuntimeException("Something isn't implemented");
        }

        @Override
        public String toString() {
            if (isBig()) {
                return internalBig.toString();
            } else {
                return internal.toString();
            }
        }

        public static INTEGER randomShort() {
            return INTEGER.valueOf(SECURE_PRNG.nextInt(Short.MAX_VALUE));
        }

        public static INTEGER randomLong() {
            return INTEGER.valueOf(SECURE_PRNG.nextLong());
        }

        public static INTEGER randomInt() {
            return INTEGER.valueOf(SECURE_PRNG.nextInt());
        }

        public static INTEGER randomINTEGER(int size) {
            final byte[] bytes = new byte[size];
            SECURE_PRNG.nextBytes(bytes);
            return INTEGER.valueOf(bytes);
        }

        public static INTEGER randomINTEGER() {
            return randomINTEGER(32);
        }

        public byte[] getBytes() {
            if (isBig())
                return internalBig.toByteArray();
            else
                return BigInteger.valueOf(internal).toByteArray();
        }

        public int signum() {
            if (isBig()) return getInternalBig().signum();

            if (getInternal() == 0) {
                return 0;
            } else if (getInternal() > 0) {
                return 1;
            } else {
                return -1;
            }
        }

        public byte[] getBytes(int minSize) {
            byte[] arr;
            if (signum() >= 0) {
                if (isBig())
                    arr = internalBig.toByteArray();
                else
                    arr = BigInteger.valueOf(internal).toByteArray();

                if (arr.length < minSize) {
                    byte[] bytes = new byte[minSize];

                    System.arraycopy(arr, 0, bytes, arr.length < minSize ? minSize - arr.length : 0, arr.length);
                    arr = bytes;
                }

                return arr;
            } else {
                if (isBig())
                    arr = internalBig.negate().toByteArray();
                else
                    arr = BigInteger.valueOf(internal).negate().toByteArray();

                if (arr.length < minSize) {
                    byte[] bytes = new byte[minSize];

                    System.arraycopy(arr, 0, bytes, arr.length < minSize ? minSize - arr.length : 0, arr.length);
                    arr = bytes;
                }

                for (int i = 0; i < arr.length; i++) {
                    arr[i] = (byte) (~arr[i] & 0xff);
                }
                byte tmp = 0;
                for (int i = arr.length - 1; i >= 0 && tmp == 0; i--) {
                    arr[i] = tmp = (byte) ((arr[i] + 1) & 0xff);
                }
                return arr;
            }
        }

        public INTEGER add(int i) {
            final BigInteger bigI = BigInteger.valueOf(i);
            if (isBig()) {
                set(internalBig.add(bigI));
            } else {
                if (internal + i < 0) {
                    set(getInternalBig().add(bigI));
                } else {
                    this.internal += i;
                }
            }
            return this;
        }

        public INTEGER sub(int i) {
            final BigInteger bigI = BigInteger.valueOf(i);
            if (isBig()) {
                set(internalBig.subtract(bigI));
            } else {
                if (internal - i > 0) {
                    set(getInternalBig().subtract(bigI));
                } else {
                    this.internal -= i;
                }
            }
            return this;
        }

        public INTEGER add(INTEGER integer) {
            if (isBig() || integer.isBig()) {
                set(getInternalBig().add(integer.getInternalBig()));
            } else {
                if (getInternal() + integer.getInternal() < 0) {
                    set(getInternalBig().add(integer.getInternalBig()));
                } else {
                    this.internal += integer.getInternal();
                }
            }
            return this;
        }

        public INTEGER sub(INTEGER integer) {
            if (isBig() || integer.isBig()) {
                set(getInternalBig().subtract(integer.getInternalBig()));
            } else {
                if (internal - integer.internal > 0) {
                    set(getInternalBig().subtract(integer.getInternalBig()));
                } else {
                    this.internal -= integer.getInternal();
                }
            }
            return this;
        }

        public INTEGER abs() {
            if (isBig()) {
                internalBig = internalBig.abs();
            } else {
                internal = Math.abs(internal);
            }
            return this;
        }

        public INTEGER duplicate() {
            return new INTEGER(this);
        }
    }
}

