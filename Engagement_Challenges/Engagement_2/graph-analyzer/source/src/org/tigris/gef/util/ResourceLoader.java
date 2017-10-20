// Copyright (c) 1996-99 The Regents of the University of California. All
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
// File: ResourceLoader.java
// Classes: ResourceLoader
// Original Author: Thorsten Sturm
package org.tigris.gef.util;

import javax.swing.*;
import java.util.*;

/**
 * This class manages the resource locations needed within the application.
 * Already loaded resources are cached. The resources can be searched in
 * different locations.
 *
 */
public class ResourceLoader {

    private static HashMap _resourceCache = new HashMap();
    private static List _resourceLocations = new ArrayList();
    private static List _resourceExtensions = new ArrayList();

    public static ImageIcon lookupIconResource(String resource) {
        return lookupIconResource(resource, resource);
    }

    public static ImageIcon lookupIconResource(String resource, String desc) {
        return lookupIconResource(resource, desc, null);
    }

    public static ImageIcon lookupIconResource(String resource,
            ClassLoader loader) {
        return lookupIconResource(resource, resource, loader);
    }

    /**
     * This method tries to find an ImageIcon for the given name in all known
     * locations. The file extension of the used image file can be any of the
     * known extensions.
     *
     * @param resource Name of the image to be looked after.
     * @param desc A description for the ImageIcon.
     * @param loader The class loader that should be used for loading the
     * resource.
     * @return ImageIcon for the given name, null if no image could be found.
     */
    public static ImageIcon lookupIconResource(String resource, String desc,
            ClassLoader loader) {
        String strippedName = Util.stripJunk(resource);
        if (isInCache(strippedName)) {
            return (ImageIcon) _resourceCache.get(strippedName);
        }

        ImageIcon res = null;
        java.net.URL imgURL = null;
        try {
            for (Iterator extensions = _resourceExtensions.iterator(); extensions
                    .hasNext();) {
                String tmpExt = (String) extensions.next();
                for (Iterator locations = _resourceLocations.iterator(); locations
                        .hasNext();) {
                    String imageName = (String) locations.next() + "/"
                            + strippedName + "." + tmpExt;
                    // org.graph.commons.logging.LogFactory.getLog(null).info("[ResourceLoader] try loading " +
                    // imageName);
                    if (loader == null) {
                        imgURL = ResourceLoader.class.getResource(imageName);
                    } else {
                        imgURL = loader.getResource(imageName);
                    }
                    if (imgURL != null) {
                        break;
                    }
                }
                if (imgURL != null) {
                    break;
                }
            }
            if (imgURL == null) {
                return null;
            }
            res = new ImageIcon(imgURL, desc);
            synchronized (_resourceCache) {
                _resourceCache.put(strippedName, res);
            }
            return res;
        } catch (Exception ex) {
            System.err.println("Exception in looking up IconResource");
            ex.printStackTrace();
            return new ImageIcon(strippedName);
        }
    }

    /**
     * This method adds a new location to the list of known locations.
     *
     * @param location String representation of the new location.
     */
    public static void addResourceLocation(String location) {
        if (!containsLocation(location)) {
            _resourceLocations.add(location);
        }
    }

    /**
     * This method adds a new extension to the list of known extensions.
     *
     * @param extension String representation of the new extension.
     */
    public static void addResourceExtension(String extension) {
        if (!containsExtension(extension)) {
            _resourceExtensions.add(extension);
        }
    }

    /**
     * This method removes a location from the list of known locations.
     *
     * @param location String representation of the location to be removed.
     */
    public static void removeResourceLocation(String location) {
        for (Iterator iter = _resourceLocations.iterator(); iter.hasNext();) {
            String loc = (String) iter.next();
            if (loc.equals(location)) {
                _resourceLocations.remove(loc);
                break;
            }
        }
    }

    /**
     * This method removes a extension from the list of known extensions.
     *
     * @param extension String representation of the extension to be removed.
     */
    public static void removeResourceExtension(String extension) {
        for (Iterator iter = _resourceExtensions.iterator(); iter.hasNext();) {
            String ext = (String) iter.next();
            if (ext.equals(extension)) {
                _resourceExtensions.remove(ext);
                break;
            }
        }
    }

    public static boolean containsExtension(String extension) {
        return _resourceExtensions.contains(extension);
    }

    public static boolean containsLocation(String location) {
        return _resourceLocations.contains(location);
    }

    public static boolean isInCache(String resource) {
        return _resourceCache.containsKey(resource);
    }
} /* end class ResourceLoader */
