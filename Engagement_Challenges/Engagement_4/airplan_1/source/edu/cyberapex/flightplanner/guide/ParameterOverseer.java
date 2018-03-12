package edu.cyberapex.flightplanner.guide;

import java.util.HashMap;
import java.util.Map;

public class ParameterOverseer {

    private Map<String, Object> parameters;

    public ParameterOverseer(){
        parameters = new HashMap<String, Object>();

    }

    public void set(String parameter, Object value){
        parameters.put(parameter, value);
    }

    public Object pull(String parameter){
        if (parameters.containsKey(parameter)){
            return getGuide(parameter);
        }
        else {
            return false;
            }
    }

    private Object getGuide(String parameter) {
        Object val = parameters.get(parameter);
        return val;
    }
}