/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigris.gef.base;

import graph.Edge;
import graph.Graph;
import graph.GraphException;
import graph.Node;
import graph.dot.DotParser;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import user.commands.TwoPartCommand;
import user.commands.UserInputException;

/**
 *
 * @author user
 */
public class CmdLoadDot extends Cmd implements TwoPartCommand {

    String fname;

    public CmdLoadDot(String fname) throws UserInputException {
        super("LoadDot");
        this.fname = fname;
        checkFName();
    }

    public void checkFName() throws UserInputException {

        File f = new File(fname);
        if (!f.exists()) {

            throw new UserInputException("File does not exist");
        }
    }

    @Override
    public void undoIt() {
    }

    @Override
    public void doIt() {
        try {
            Map<String, Graph> graphs = DotParser.parseDot(new DataInputStream(new FileInputStream(fname)));
            Graph graph = graphs.get("main");

            Collection<Graph> graphsc = graphs.values();

            Iterator<Graph> itgs = graphsc.iterator();
            while (itgs.hasNext()) {

                Graph itg = itgs.next();

                java.util.List<Node> nodes = new ArrayList<Node>();
                java.util.List<Edge> edges = new ArrayList<Edge>();

                Iterator it = itg.nodes.iterator();
                while (it.hasNext()) {
                    Node n = (Node) it.next();
                    nodes.add(n);
                    Iterator<Edge> ite = n.out.iterator();
                    while (ite.hasNext()) {
                        Edge e = ite.next();
                        edges.add(e);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CmdLoadDot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CmdLoadDot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GraphException ex) {
            Logger.getLogger(CmdLoadDot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Object check(List<Exception> exs, Map inputs) {
        return null;
    }
}
