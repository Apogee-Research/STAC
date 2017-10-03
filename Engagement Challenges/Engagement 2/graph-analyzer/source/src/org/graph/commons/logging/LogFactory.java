/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graph.commons.logging;

import org.tigris.gef.base.CmdCreateNode;

/**
 *
 * @author Your Name <user>
 */
public class LogFactory {

    public static Log log = null;

    public static Log getLog(Class cl) {
        if (log == null) {
            log = new Log();
        }

        return log;
    }

}
