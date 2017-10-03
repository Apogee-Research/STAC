// Copyright (c) 1996-06 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
// File: ShowURLAction.java
// Classes: ShowURLAction
// Original Author: andrea.nironi@gmail.com
package org.tigris.gef.base;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.tigris.gef.util.Localizer;

/**
 * Action to display the contents of the given URL in the browser.
 * Needs-More-Work: This Action can only be used from an applet.
 */
public class ShowURLAction extends AbstractAction {

    private static final long serialVersionUID = -6226407614825075684L;

    protected URL _url;

    /**
     * Creates a new ShowURLAction
     */
    public ShowURLAction() {
        super();
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param url The url to be shown
     * @throws MalformedURLException When the url is malformed
     */
    public ShowURLAction(String name, String url) throws MalformedURLException {
        this(name, url, false);
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param url The url to be shown
     */
    public ShowURLAction(String name, URL url) {
        this(name, url, false);
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param url The url to be shown
     * @throws MalformedURLException When the url is malformed
     */
    public ShowURLAction(String name, Icon icon, String url)
            throws MalformedURLException {
        this(name, icon, url, false);
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param url The url to be shown
     */
    public ShowURLAction(String name, Icon icon, URL url) {
        this(name, icon, url, false);
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param url The url to be shown
     * @param localize Whether to localize the name or not
     * @throws MalformedURLException When the url is malformed
     */
    public ShowURLAction(String name, String url, boolean localize)
            throws MalformedURLException {
        super(localize ? Localizer.localize("GefBase", name) : name);
        setUrl(url);
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param url The url to be shown
     * @param localize Whether to localize the name or not
     */
    public ShowURLAction(String name, URL url, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
        setUrl(url);
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param url The url to be shown
     * @param localize Whether to localize the name or not
     * @throws MalformedURLException When the url is malformed
     */
    public ShowURLAction(String name, Icon icon, String url, boolean localize)
            throws MalformedURLException {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        setUrl(url);
    }

    /**
     * Creates a new ShowURLAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param url The url to be shown
     * @param localize Whether to localize the name or not
     */
    public ShowURLAction(String name, Icon icon, URL url, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
        setUrl(url);
    }

    public void setUrl(URL u) {
        _url = u;
    }

    public void setUrl(String u) throws MalformedURLException {
        _url = new URL(u);
    }

    public URL getUrl() {
        return _url;
    }

    /**
     * Translate all selected Fig's in the current editor.
     */
    public void actionPerformed(ActionEvent e) {
        Globals.showDocument(_url);
    }
}
