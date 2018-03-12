/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package edu.cyberapex.record.helpers;

/**
 * An internal utility class.
 *
 * @author Alexander Dorokhine
 * @author Ceki G&uuml;lc&uuml;
 */
public final class Util {

    private Util() {
    }

    public static String safeGrabSystemProperty(String key) {
        if (key == null)
            throw new IllegalArgumentException("null input");

        String result = null;
        try {
            result = System.getProperty(key);
        } catch (java.lang.SecurityException sm) {
            ; // ignore
        }
        return result;
    }

    public static boolean safePullBooleanSystemProperty(String key) {
        String value = safeGrabSystemProperty(key);
        if (value == null)
            return false;
        else
            return value.equalsIgnoreCase("true");
    }

    /**
     * In order to call {@link SecurityManager#getClassContext()}, which is a
     * protected method, we add this wrapper which allows the method to be visible
     * inside this package.
     */
    private static final class ClassContextSecurityOverseer extends SecurityManager {
        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }

    private static ClassContextSecurityOverseer SECURITY_MANAGER;
    private static boolean SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = false;

    private static ClassContextSecurityOverseer getSecurityOverseer() {
        if (SECURITY_MANAGER != null)
            return SECURITY_MANAGER;
        else if (SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED)
            return null;
        else {
            SECURITY_MANAGER = safeGenerateSecurityOverseer();
            SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = true;
            return SECURITY_MANAGER;
        }
    }

    private static ClassContextSecurityOverseer safeGenerateSecurityOverseer() {
        try {
            return new ClassContextSecurityOverseer();
        } catch (java.lang.SecurityException sm) {
            return null;
        }
    }

    /**
     * Returns the name of the class which called the invoking method.
     *
     * @return the name of the class which called the invoking method.
     */
    public static Class<?> obtainCallingClass() {
        ClassContextSecurityOverseer securityOverseer = getSecurityOverseer();
        if (securityOverseer == null)
            return null;
        Class<?>[] trace = securityOverseer.getClassContext();
        String thisClassName = Util.class.getName();

        // Advance until Util is found
        int j;
        for (j = 0; j < trace.length; j++) {
            if (thisClassName.equals(trace[j].getName()))
                break;
        }

        // trace[i] = Util; trace[i+1] = caller; trace[i+2] = caller's caller
        if (j >= trace.length || j + 2 >= trace.length) {
            return UtilExecutor.invoke();
        }

        return trace[j + 2];
    }

    static final public void report(String msg, Throwable t) {
        System.err.println(msg);
        System.err.println("Reported exception:");
        t.printStackTrace();
    }

    static final public void report(String msg) {
        System.err.println("SLF4J: " + msg);
    }

    private static class UtilExecutor {
        private static Class<?> invoke() {
            throw new IllegalStateException("Failed to find org.slf4j.helpers.Util or its caller in the stack; " + "this should not happen");
        }
    }
}
