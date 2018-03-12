package net.techpoint.flightrouter.manager;

import java.util.HashMap;
import java.util.Map;

public class ParameterManager{

    private Map<String, Object> parameters;

    public ParameterManager(){
        parameters = new HashMap<String, Object>();

    }

    public void fix(String parameter, Object value){
        parameters.put(parameter, value);
    }

    public Object get(String parameter){
        if (parameters.containsKey(parameter)){
            Object val = parameters.get(parameter);
            return val;
        }
        else {
            return false;
            }
    }
}