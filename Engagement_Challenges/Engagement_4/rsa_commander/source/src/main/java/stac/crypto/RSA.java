package stac.crypto;

import stac.parser.OpenSSLRSAPEM;

/**
 * This one should be self-explanatory
 */
public class RSA {
    public OpenSSLRSAPEM.INTEGER encrypt(OpenSSLRSAPEM.INTEGER message, OpenSSLRSAPEM.INTEGER publicExponent, OpenSSLRSAPEM.INTEGER modulus) {
        return run(message, publicExponent, modulus);
    }

    public OpenSSLRSAPEM.INTEGER decrypt(OpenSSLRSAPEM.INTEGER message, OpenSSLRSAPEM.INTEGER privateExponent, OpenSSLRSAPEM.INTEGER modulus) {
        return run(message, privateExponent, modulus);
    }

    private OpenSSLRSAPEM.INTEGER run(OpenSSLRSAPEM.INTEGER m, OpenSSLRSAPEM.INTEGER exp, OpenSSLRSAPEM.INTEGER mod) {
        boolean withinMod = m.compareTo(mod) < 0;
        boolean aboveZero = m.compareTo(0) >= 0;
        if (withinMod && aboveZero) {
            return m.modPow(exp, mod);
        }
        throw new RSAMessageSizeException(withinMod, aboveZero);
    }

    static class RSAMessageSizeException extends RuntimeException {
        private static final long serialVersionUID = -6006531878380802990L;
        private final String message;
        private RSAMessageSizeException(boolean messageFitsInModulus, boolean messageIsGreaterThanZero) {
            message = "Message fits under mod? " + messageFitsInModulus + ", " +
                    "Message is greater than zero? " + messageIsGreaterThanZero;
        }

        /**
         * Returns the detail message string of this throwable.
         *
         * @return the detail message string of this {@code Throwable} instance
         * (which may be {@code null}).
         */
        @Override
        public String getMessage() {
            return this.message;
        }
    }
}
