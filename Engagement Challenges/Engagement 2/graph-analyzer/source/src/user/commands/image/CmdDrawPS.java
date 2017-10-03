/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.image;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tigris.gef.base.CmdSavePS;
import org.tigris.gef.base.Globals;
import user.commands.Cmd;

/**
 *
 * @author user
 */
public class CmdDrawPS implements Cmd {

    @Override
    public Object runCmd(Map inputs) {

        FileOutputStream fos = null;
        try {
            String imagef = (String) inputs.get("imageout");
            CmdSavePS sps = new CmdSavePS();
            try {
                fos = new FileOutputStream(imagef + ".ps");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CmdDrawPS.class.getName()).log(Level.SEVERE, null, ex);
            }
            sps.setStream(fos);
            Globals.curEditor().executeCmd(sps, null);
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CmdDrawPS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CmdDrawPS.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(CmdDrawPS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

}
