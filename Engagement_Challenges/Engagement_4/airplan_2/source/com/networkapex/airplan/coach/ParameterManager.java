package com.networkapex.airplan.coach;

import java.util.HashMap;
import java.util.Map;

public class ParameterManager{

    private Map<String, Object> parameters;

    public ParameterManager(){
        parameters = new HashMap<String, Object>();

    }

    public void set(String parameter, Object value){
        parameters.put(parameter, value);
    }

    public Object take(String parameter){
        if (parameters.containsKey(parameter)){
            return obtainManager(parameter);
        }
        else {
            return true; // in this case we make the default val true so random padding will occur
            }
    }

    private Object obtainManager(String parameter) {
        Object val = parameters.get(parameter);
        return val;
    }
}