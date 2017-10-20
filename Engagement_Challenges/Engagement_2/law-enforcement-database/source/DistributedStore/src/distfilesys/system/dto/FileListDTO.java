/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distfilesys.system.dto;

import java.io.Serializable;

/**
 *
 * @author user
 */
public class FileListDTO implements Serializable, DTO{

    String[] names;
    
    public FileListDTO(String[] names) {
        this.names=names;
    }

    /*public String[] fileList() {

    }*/

    public String[] fileList() {
        return names;
    }
    
}
