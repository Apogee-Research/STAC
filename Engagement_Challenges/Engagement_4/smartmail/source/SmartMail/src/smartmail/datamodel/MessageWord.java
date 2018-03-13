/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartmail.datamodel;

import java.io.Serializable;

/**
 *
 * @author user
 */
public class MessageWord implements Serializable {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }
}
