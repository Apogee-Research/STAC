package net.cybertip.routing.manager;

import java.util.HashMap;
import java.util.Map;

public class ParameterOverseer {

    private Map<String, Object> parameters;

    public ParameterOverseer(){
        parameters = new HashMap<String, Object>();

    }

    public void assign(String parameter, Object value){
        parameters.put(parameter, value);
    }

    public Object obtain(String parameter){
        if (parameters.containsKey(parameter)){
            return grabUtility(parameter);
        }
        else {
            return false;
            }
    }

    private Object grabUtility(String parameter) {
        Object val = parameters.get(parameter);
        return val;
    }
}