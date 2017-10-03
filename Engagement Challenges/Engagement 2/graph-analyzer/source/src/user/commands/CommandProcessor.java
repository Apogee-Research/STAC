/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands;

import graph.Graph;
import graph.layout.adv.MinimizerBarnesHut;
import java.io.File;
import user.commands.image.CmdGUIDisplay;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Globals;
import user.commands.image.CmdDrawPNG;
import user.commands.image.CmdDrawPS;
import user.commands.input.CmdParseDot;
import user.commands.layout.CmdForceLayout;
import user.commands.layout.CmdRandLayout;
import user.commands.tomanager.CmdToLayerDiagram;
import user.commands.tomanager.CmdToLayerGrid;

/**
 *
 * @author user
 */
public class CommandProcessor {

    private void runCommands() {

        while (!cmd_stack.isEmpty()) {
            Cmd cmd = cmd_stack.pollLast();
            Object runCmd = cmd.runCmd(globalvars);
        }

    }

    CmdRandLayout layoutcmd;

    Deque<Exception> exception_stack;
    Deque<Cmd> cmd_stack;
    Map<String, Object> globalvars;
    Editor editor;

    public CommandProcessor() {

        exception_stack = new ArrayDeque<Exception>();
        cmd_stack = new ArrayDeque<Cmd>();
        globalvars = new HashMap<String, Object>();
        editor = new Editor();
        Globals.displayheight = 2000;
        Globals.displaywidth = 2000;

        Globals.nodeheight = 100;
        Globals.nodewidth = 100;

    }

    public static void main(String[] args) {

        if (args.length < 5) {

            String usage = ""
                    + "************ Usage Instructions ************ \n"
                    + "To generate a postscript from the dot file, use the following arguments: \n"
                    + "dot youfilelocation.dot xy diagram ps outputfilename\n"
                    + "\n"
                    + "To generate a png from the dot file, use the following arguments: \n"
                    + "dot youfilelocation.dot xy diagram png outputfilename\n"
                    + "\n"
                    /*+ "To load the dot file in the gui, use the following arguments: \n"
                    + "dot youfilelocation.dot xy diagram gui \n"
                    + "\n"*/
                    + "Your actual call will look similar to this:\n"
                    + "java -jar dist/GraphDisplay.jar dot an_optional_folderpath/yourfil.dot xy diagram png an_optional_folderpath/youroutfile\n"
                    +"\n"
                    +"Commands that generate image files, like PNG, may generate more than one image file per occurrence.\n"
                    +"The argument you pass as ‘outputfilename’ is the root folder location and file name. \n"
                    + "The application may generate multiple images using this root by appending to it.\n";
                    
            System.out.println(usage);
            System.exit(1);

        }

        try {
            CommandProcessor cpr = new CommandProcessor();

            //Get the format name (i.e dot)
            String format = args[0];
            //Get the file name (i.e something.dot)
            String file = args[1];
            cpr.evalFormatParseCmd(format, file);

            //Get the layout (none, random, force)
            String layoutoption = args[2];
            cpr.evalLayoutCmd(layoutoption);

            //put the data from the dot parser into the layer manager that manages the display
            String gmgr = args[3];
            cpr.evalGraphcsMgr(gmgr);

            //get the image format (png,ps, or the gui)
            String imageformat = args[4];
            //the out file name (i.e. outfile.ps)
            String image = "default";
            if ( args.length>=6) {
                image = args[5];
                
                File outpath = new File(image);
                File parentFile = outpath.getParentFile();
                if (parentFile != null)
                    parentFile.mkdirs();
                
            }
            cpr.evalImageOut(imageformat, image);

            //The code above sets up the commands, this call calls each command in sequence
            cpr.runCommands();
        } catch (CmdParseException ex) {
            Logger.getLogger(CommandProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Cmd evalFormatParseCmd(String format, String file) throws CmdParseException {
        Cmd lcmd = null;

        globalvars.put("inputfile", file);
        if (format.equals("dot")) {
            cmd_stack.push(new CmdParseDot());
        } else if (format.equals("csv")) {

        } else {
            throw new CmdParseException("unknown format");
        }

        return lcmd;
    }

    public Cmd evalGraphcsMgr(String mgr) throws CmdParseException {

        Cmd lcmd = null;

        if (mgr.equals("diagram")) {
            cmd_stack.push(new CmdToLayerDiagram());
        } else if (mgr.equals("grid")) {
            cmd_stack.push(new CmdToLayerGrid());

        } else {
            throw new CmdParseException("no layout set");
        }

        return lcmd;
    }

    public Cmd evalLayoutCmd(String layout) throws CmdParseException {

        Cmd lcmd = null;

        if (layout.equals("xy")) {

        } else if (layout.equals("rand")) {

            cmd_stack.push(new CmdRandLayout());
        } else if (layout.equals("force")) {
            cmd_stack.push(new CmdForceLayout());

        } else {
            throw new CmdParseException("no layout set");
        }

        return lcmd;
    }

    public Cmd evalImageOut(String layout, String imagefout) throws CmdParseException {

        Cmd lcmd = null;

        globalvars.put("imageout", imagefout);
        if (layout.equals("ps")) {
            cmd_stack.push(new CmdDrawPS());
        } else if (layout.equals("png")) {
            cmd_stack.push(new CmdDrawPNG());
        } /*else if (layout.equals("gui")) {
            cmd_stack.push(new CmdGUIDisplay(editor));
        } */else {
            throw new CmdParseException("no layout set");
        }

        return lcmd;
    }

}
