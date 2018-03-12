package com.roboticcusp.organizer.coach;

import java.util.HashMap;
import java.util.Map;

public class ParameterConductor {

    private Map<String, Object> parameters;

    public ParameterConductor(){
        parameters = new HashMap<String, Object>();

    }

    public void set(String parameter, Object value){
        parameters.put(parameter, value);
    }

    public Object obtain(String parameter){
        if (parameters.containsKey(parameter)){
            Object val = parameters.get(parameter);
            return val;
        }
        else {
            return true; // in this case we make the default val true so random padding will occur
            }
    }
}