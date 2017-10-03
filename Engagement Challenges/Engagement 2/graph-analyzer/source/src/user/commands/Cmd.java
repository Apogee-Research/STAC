/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands;

import java.util.Map;

/**
 *
 * @author user
 */
public interface Cmd {

    public Object runCmd(Map inputs);
}
