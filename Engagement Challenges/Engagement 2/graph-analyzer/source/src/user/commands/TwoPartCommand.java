/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands;

import java.util.List;
import java.util.Map;

/**
 *
 * @author user
 */
public interface TwoPartCommand {

    public Object check(List<Exception> exs, Map inputs);

}
