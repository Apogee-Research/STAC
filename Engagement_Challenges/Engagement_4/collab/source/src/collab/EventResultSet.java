/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 *
 * @author user
 */
public class EventResultSet {
    
    List<Integer> events;
long base; 
long min;
long max;
        
      public EventResultSet(long base,  long min, long max){
        
          this.base = base;
                  this.min = min;
                          this.max = max;
        events = new ArrayList<Integer>();
        
        
    }  
    public EventResultSet(List<Integer> eventids){
        
        events = eventids;
        
        
    }
    
    private void sort(){
        Collections.sort(events);
    
        
    }
    
    public List<Integer> get(){
    
        sort();
        return events;
    }
    
    
    public void add(long base, long key, long min, long max){
        
        if(key >= min && key<= max){
            long v = key - base;
            events.add((int)v);

        }
    }

    public void add(long mKey) {
        add(base, mKey, min, max);

    }
    
     
    
    
}
