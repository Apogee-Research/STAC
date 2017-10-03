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
// File: Localizer.java
// Classes: Localizer
// Original Author: Thorsten Sturm, Luc Maisonobe
package org.tigris.gef.util;

import java.awt.Toolkit;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.KeyStroke;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;

/**
 * This class manages the resource bundle files needed to localize the
 * application. All registered resource files are searched in order to find the
 * localization of a given string.
 *
 */
public class Localizer {

    private static Map resourcesByLocale = new HashMap();
    private static Map resourceNames = new HashMap();
    private static Locale defaultLocale = Locale.getDefault();
    private static Map defaultResources = new HashMap();

    static {
        resourcesByLocale.put(defaultLocale, defaultResources);
    }

    private static Log log = LogFactory.getLog(Localizer.class);

    /**
     * This method tests, if a resource with the given name is registered.
     *
     * @param resource Name of the resource to be tested.
     * @return True, if a resource with the given name is registered, otherwise
     * false.
     */
    public static boolean containsResource(String resource) {
        return resourceNames.containsValue(resource);
    }

    /**
     * This method tests, if the given locale is registered.
     *
     * @param locale Locale to be tested.
     * @return True, if the given locale is registered, otherwise false.
     */
    public static boolean containsLocale(Locale locale) {
        return resourcesByLocale.containsKey(locale);
    }

    /**
     * The method addLocale adds a new locale to the set of known locales for
     * the application. For a new locale, all known ResourceBundles are added
     * when possible.
     *
     * @see java.util.ResourceBundle
     * @see java.util.Locale
     */
    public static void addLocale(Locale locale) {
        Map resources = new HashMap();
        Iterator iter = resourceNames.keySet().iterator();

        while (iter.hasNext()) {
            try {
                String binding = (String) iter.next();
                String resourceName = (String) resourceNames.get(binding);
                ResourceBundle bundle = ResourceBundle.getBundle(resourceName,
                        locale);
                if (bundle == null) {
                    continue;
                }

                if (bundle instanceof ResourceBundle) {
                    resources.put(binding, bundle);
                }
            } catch (MissingResourceException missing) {
                continue;
            }
        }
        resourcesByLocale.put(locale, resources);
    }

    /**
     * The method changes the current locale to the given one. The resources
     * bound to the given locale are also preloaded. If the given locale is not
     * already registered, it will be registered automatically.
     *
     * @see java.util.Locale
     */
    public static void switchCurrentLocale(Locale locale) {
        if (!resourcesByLocale.containsKey(locale)) {
            addLocale(locale);
        }

        if (!defaultLocale.equals(locale)) {
            defaultLocale = locale;
            defaultResources = (Map) resourcesByLocale.get(locale);
        }
    }

    /**
     * The method returns the current locale.
     *
     * @return The current locale
     */
    public static Locale getCurrentLocale() {
        return defaultLocale;
    }

    /**
     * The method returns all resources for the given locale.
     *
     * @param locale Resources are searched for this locale.
     * @return Map of all resources and their names bound to the given locale.
     */
    public static Map getResourcesFor(Locale locale) {
        if (!containsLocale(locale)) {
            return null;
        }

        return (Map) resourcesByLocale.get(locale);
    }

    /**
     * The method adds a new resource under the given name. The resource is
     * preloaded and bound to every registered locale.
     *
     * @param resourceName Name of the resource to be registered.
     * @param binding Name under which the resource should be registered.
     */
    public static synchronized void addResource(String binding,
            String resourceName) throws MissingResourceException {
        addResource(binding, resourceName, Localizer.class.getClassLoader());
    }

    public static synchronized void addResource(String binding,
            String resourceName, ClassLoader loader)
            throws MissingResourceException {
        if (containsResource(resourceName)) {
            return;
        }

        Iterator iter = resourcesByLocale.keySet().iterator();

        while (iter.hasNext()) {
            addResource(binding, resourceName, (Locale) iter.next(), loader);
        }
    }

    public static synchronized void addResource(String binding,
            String resourceName, Locale locale) throws MissingResourceException {
        addResource(binding, resourceName, locale, Localizer.class
                .getClassLoader());
    }

    public static synchronized void addResource(String binding,
            String resourceName, Locale locale, ClassLoader loader)
            throws MissingResourceException {
        ResourceBundle resource = null;
        if (containsLocale(locale)) {
            Map resources = (Map) resourcesByLocale.get(locale);
            resource = ResourceBundle.getBundle(resourceName, locale, loader);
            resources.put(binding, resource);
            if (!resourceNames.containsValue(resourceName)) {
                resourceNames.put(binding, resourceName);
            }
        } else {
            throw new MissingResourceException("Locale not found!", locale
                    .toString(), resourceName);
        }
    }

    /**
     * The method removes the given locale from the list of known locales. If
     * the locale is the current locale, the current locale is switched to the
     * systems default locale.
     *
     * @param locale Locale to be removed.
     */
    public static void removeLocale(Locale locale) {
        if (defaultLocale.equals(locale)) {
            switchCurrentLocale(Locale.getDefault());
        }

        resourcesByLocale.remove(locale);
    }

    /**
     * The method removes the given resource from the list of used resources.
     * Any binding from any locale to that resource is also removed.
     *
     * @param binding Name under which the resource to be removed is registered.
     */
    public static void removeResource(String binding) {
        Iterator iter = resourcesByLocale.keySet().iterator();

        while (iter.hasNext()) {
            Locale tmpLocale = (Locale) iter.next();
            ((Map) resourcesByLocale.get(tmpLocale)).remove(binding);
        }
        resourceNames.remove(binding);
    }

    /**
     * This function returns a localized string corresponding to the specified
     * key. Searching goes through all registered ResourceBundles
     *
     * @param binding ResourceBundles to search in.
     * @param key String to be localized.
     * @return First localization for the given string found in the registered
     * ResourceBundles, the key itself if no localization has been found.
     */
    public static String localize(String binding, String key) {
        return localize(binding, key, defaultLocale, defaultResources);
    }

    public static String localize(String binding, String key, boolean localize) {
        return localize(binding, key, defaultLocale, defaultResources, localize);
    }

    public static String localize(String binding, String key, Locale locale,
            Map resources, boolean localize) {
        if (localize) {
            return localize(binding, key, locale, resources);
        } else {
            return key;
        }
    }

    public static String localize(String binding, String key, Locale locale,
            Map resources) {
        boolean showErrors = false;

        if (locale == null || resources == null || !containsLocale(locale)) {
            if (showErrors) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    log.warn("Localization failed for key " + key
                            + " (binding: " + binding + ")", e);
                }
            }
            return key;
        }

        String localized = null;

        ResourceBundle resource = (ResourceBundle) resources.get(binding);
        if (resource == null) {
            if (showErrors) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    log.warn("Localization failed for key " + key
                            + " (binding: " + binding + ")", e);
                }
            }
            return key;
        }

        try {
            localized = resource.getString(key);
        } catch (MissingResourceException e) {
        }
        if (localized == null) {
            if (showErrors) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    log.warn("Localization failed for key " + key
                            + " (binding: " + binding + ")", e);
                }
            }
            localized = key;
        }

        return localized;
    }

    /**
     * Check if a resource contains a specific key (for the current default
     * locale)
     *
     * @param binding
     * @param key
     * @return true if the key is contained.
     */
    public static boolean containsKey(String binding, String key) {
        return containsKey(binding, key, defaultLocale, defaultResources);
    }

    public static boolean containsKey(String binding, String key,
            Locale locale, Map resources) {
        if (locale == null || resources == null || !containsLocale(locale)) {
            return false;
        }

        ResourceBundle resource = (ResourceBundle) resources.get(binding);
        if (resource == null) {
            return false;
        }

        try {
            resource.getObject(key);
            return true;
        } catch (MissingResourceException e) {
            return false;
        }
    }

    /**
     * Returns a Set that contains all keys (strings) defined in the given
     * resource (for the current default locale)
     *
     * @param binding the resource name whose keys should be returned
     * @return a Set containing all keys. Will never return null, but an empty
     * Set if no resource was found or it contains no keys.
     */
    public static Set getKeys(String binding) {
        Set keys = getkeys(binding, defaultLocale, defaultResources);
        return keys;
    }

    private static Set getkeys(String binding, Locale locale, Map resources) {
        if (locale == null || resources == null || !containsLocale(locale)) {
            return Collections.EMPTY_SET;
        }

        ResourceBundle resource = (ResourceBundle) resources.get(binding);
        if (resource == null) {
            return Collections.EMPTY_SET;
        }

        Set result = new HashSet();
        Enumeration keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            result.add(key);
        }

        return result;
    }

    /**
     * AWT has no standard way to name the platforms default menu shortcut
     * modifier for a KeyStroke, so the localizer replaces each occurence of
     * "shortcut" with Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
     */
    protected final static String SHORTCUT_MODIFIER = "shortcut";

    /**
     * This function returns a localized menu shortcut key to the specified key.
     *
     * @param binding Name of resource to be searched.
     * @param key Shortcut string to be localized.
     * @return Localized KeyStroke object.
     */
    public static KeyStroke getShortcut(String binding, String key) {
        return getShortcut(binding, key, defaultLocale, defaultResources);
    }

    public static KeyStroke getShortcut(String binding, String key,
            Locale locale, Map resources) {
        if (locale == null || resources == null || !containsLocale(locale)) {
            return null;
        }

        KeyStroke stroke = null;
        ResourceBundle resource = (ResourceBundle) resources.get(binding);
        try {
            Object obj = resource.getObject(key);
            if (obj instanceof KeyStroke) {
                stroke = (KeyStroke) obj;
            } else if (obj instanceof String) {
                boolean hasShortcutModifier = false;
                StringBuffer shortcutBuf = new StringBuffer();

                StringTokenizer tokenizer = new StringTokenizer((String) obj);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();

                    if (token.equals(SHORTCUT_MODIFIER)) {
                        hasShortcutModifier = true;
                    } else {
                        shortcutBuf.append(token);
                        shortcutBuf.append(" ");
                    }
                }
                stroke = KeyStroke.getKeyStroke(shortcutBuf.toString());
                int modifiers = stroke.getModifiers()
                        | (hasShortcutModifier ? Toolkit.getDefaultToolkit()
                                .getMenuShortcutKeyMask() : 0);
                int keyCode = stroke.getKeyCode();
                stroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            }
        } catch (MissingResourceException e) {
        } catch (ClassCastException e) {

        } catch (NullPointerException e) {
        }
        return stroke;
    }
} /* end class Localizer */
