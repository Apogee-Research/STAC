/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package com.roboticcusp.xmlpull.v1;

/**
 * This exception is thrown to signal XML Pull Parser related faults.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XmlPullGrabberException extends Exception {
    protected Throwable detail;
    protected int row = -1;
    protected int column = -1;

    /*    public XmlPullParserException() {
          }*/

    public XmlPullGrabberException(String s) {
        super(s);
    }

    /*
    public XmlPullParserException(String s, Throwable thrwble) {
        super(s);
        this.detail = thrwble;
        }

    public XmlPullParserException(String s, int row, int column) {
        super(s);
        this.row = row;
        this.column = column;
    }
    */

    public XmlPullGrabberException(String msg, XmlPullGrabber grabber, Throwable chain) {
        super ((msg == null ? "" : msg+" ")
               + (grabber == null ? "" : "(position:"+ grabber.takePositionDescription()+") ")
               + (chain == null ? "" : "caused by: "+chain));

        if (grabber != null) {
            this.row = grabber.getLineNumber();
            this.column = grabber.takeColumnNumber();
        }
        this.detail = chain;
    }

    public Throwable takeDetail() { return detail; }
    //    public void setDetail(Throwable cause) { this.detail = cause; }
    public int getLineNumber() { return row; }
    public int getColumnNumber() { return column; }

    /*
    public String getMessage() {
        if(detail == null)
            return super.getMessage();
        else
            return super.getMessage() + "; nested exception is: \n\t"
                + detail.getMessage();
    }
    */

    //NOTE: code that prints this and detail is difficult in J2ME
    public void printStackTrace() {
        if (detail == null) {
            printStackTraceEngine();
        } else {
            printStackTraceHerder();
        }
    }

    private void printStackTraceHerder() {
        synchronized(System.err) {
            System.err.println(super.getMessage() + "; nested exception is:");
            detail.printStackTrace();
        }
    }

    private void printStackTraceEngine() {
        super.printStackTrace();
    }

}

