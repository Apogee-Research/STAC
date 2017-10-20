package com.example.subspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Config extends Properties
{
    public Config(
        String filename)
        throws Exception
    {
        super(System.getProperties());

        FileInputStream fis = null;
        InputStreamReader isr = null;
        try
        {
            File file = new File(filename);
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            load(isr);
        }
        finally
        {
            if (isr != null)
            {
                isr.close();
            }
            else if (fis != null)
            {
                fis.close();
            }
        }
    }

    /**
     * Get a property as a set.
     *
     * The resulting set is the set of the values of all properties
     * that have keys equal to {@code key} or beginning with {@code
     * key + "."}.
     */
    public Set<String> getSetProperty(
        String key)
    {
        String keyDot = key + ".";

        Set<String> result = new HashSet<String>();

        for (String subKey : stringPropertyNames())
        {
            if (subKey.startsWith(keyDot) || subKey.equals(key))
            {
                result.add(getProperty(subKey));
            }
        }

        return result;
    }

    /**
     * Get a property as a map.
     *
     * For all properties with keys beginning with {@code key + "."},
     * this maps the remainder of the key to the value of that
     * property.
     */
    public Map<String, String> getMapProperty(
        String key)
    {
        key = key + ".";

        Map<String, String> result = new HashMap<String, String>();

        for (String subKey : stringPropertyNames())
        {
            if (!subKey.startsWith(key))
            {
                continue;
            }

            result.put(
                subKey.substring(key.length()),
                getProperty(subKey));
        }

        return result;
    }

    /**
     * Get a property, or throw an exception if the property does not
     * exist.
     */
    public String getMandatoryProperty(
        String key)
        throws RuntimeException
    {
        String value = getProperty(key);
        if (value == null)
        {
            throw new RuntimeException("Please configure " + key);
        }

        return value;
    }

    /**
     * Get a mandatory property as a port number.
     */
    public int getMandatoryPortProperty(
        String key)
        throws RuntimeException
    {
        String portStr = getMandatoryProperty(key);

        int port;
        try
        {
            port = Integer.parseInt(portStr);
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException(
                "Invalid port: " + portStr);
        }

        if (port <= 0 || port >= 65536)
        {
            throw new RuntimeException(
                "Invalid port: " + port);
        }

        return port;
    }
}
