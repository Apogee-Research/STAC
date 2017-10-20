/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graph.commons.logging;

import java.io.IOException;

/**
 *
 * @author Your Name <user>
 */
public class Log {

    public boolean isDebugEnabled() {
        return false;
    }

    public void debug(String string) {
    }

    public void warn(String undo_is_not_implemented) {
    }

    public void error(String string) {
    }

    public void info(String string) {
        //System.out.println(string);
    }

    public void warn(String exception_extracting_bounds_from_descript, Throwable ex) {
    }

    public void error(String error_while_exporting_Graphics, Throwable ex) {
    }

    public boolean isWarnEnabled() {
        return false;
    }

    public void debug(String string, Throwable ex) {
    }

    public void error(Throwable ex) {
    }

    public boolean isInfoEnabled() {
        return false;
    }

    public void debug(StringBuffer buf) {
    }

}
