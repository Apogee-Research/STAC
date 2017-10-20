package org.tigris.gef.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Bob Tarling
 * @since 25-May-2004
 */
abstract public class ConnectionConstrainer {

    /**
     * A map of valid connections keyed by the connection type. The constructor
     * builds this from the data in the VALID_CONNECTIONS array
     */
    protected Map _validConnectionMap = new HashMap();

    public boolean isConnectionValid(Object edgeType, Object fromElement,
            Object toElement) {
        // Get the list of valid model item pairs for the given connection type
        ArrayList validItems = (ArrayList) _validConnectionMap.get(edgeType);
        if (validItems == null) {
            return false;
        }
        // See if there's a pair in this list that match the given
        // model elements
        Iterator it = validItems.iterator();
        while (it.hasNext()) {
            Class[] modeElementPair = (Class[]) it.next();
            if (modeElementPair[0].isInstance(fromElement)
                    && modeElementPair[1].isInstance(toElement)) {
                return true;
            }
        }
        return false;
    }

    protected void buildValidConnectionMap(Object[][] validConnections) {
        // A list of valid connections between elements, the
        // connection type first and then the elements to be connected

        Object connection = null;
        for (int i = 0; i < validConnections.length; ++i) {
            connection = validConnections[i][0];
            ArrayList validItems = (ArrayList) _validConnectionMap
                    .get(connection);
            if (validItems == null) {
                validItems = new ArrayList();
                _validConnectionMap.put(connection, validItems);
            }
            if (validConnections[i].length < 3) {
                // If there isn't a 3rd column then this represents a connection
                // of elements of the same type.
                Object[] modeElementPair = new Class[2];
                modeElementPair[0] = validConnections[i][1];
                modeElementPair[1] = validConnections[i][1];
                validItems.add(modeElementPair);
            } else {
                // If there is a 3rd column then this represents a connection
                // of between 2 different types of element.
                Object[] modeElementPair = new Class[2];
                modeElementPair[0] = validConnections[i][1];
                modeElementPair[1] = validConnections[i][2];
                validItems.add(modeElementPair);
                // If the array hasn't been flagged to indicate otherwise
                // swap elements the elemnts and add again.
                if (validConnections[i].length < 4) {
                    Object[] reversedModeElementPair = new Class[2];
                    reversedModeElementPair[0] = validConnections[i][2];
                    reversedModeElementPair[1] = validConnections[i][1];
                    validItems.add(reversedModeElementPair);
                }
            }
        }
    }

    protected void addValidConnection(Class edgeClass, Class portClass) {
        Object connection = edgeClass;
        ArrayList validItems = (ArrayList) _validConnectionMap.get(connection);
        if (validItems == null) {
            validItems = new ArrayList();
            _validConnectionMap.put(connection, validItems);
        }
        Object[] modeElementPair = new Class[2];
        modeElementPair[0] = portClass;
        modeElementPair[1] = portClass;
        validItems.add(modeElementPair);
    }

    protected void addValidConnection(Class edgeClass, Class portClass1,
            Class portClass2) {
        ArrayList validItems = (ArrayList) _validConnectionMap.get(edgeClass);
        if (validItems == null) {
            validItems = new ArrayList();
            _validConnectionMap.put(edgeClass, validItems);
        }
        Object[] modeElementPair = new Class[2];
        modeElementPair[0] = portClass1;
        modeElementPair[1] = portClass2;
        validItems.add(modeElementPair);
    }
}
