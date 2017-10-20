/*
 * CmdPrint.java
 *
 * Copyright (c) 1996-99 The Regents of the University of California. All
 * Rights Reserved. Permission to use, copy, modify, and distribute this
 * software and its documentation without fee, and without a written
 * agreement is hereby granted, provided that the above copyright notice
 * and this paragraph appear in all copies.  This software program and
 * documentation are copyrighted by The Regents of the University of
 * California. The software program and documentation are supplied "AS
 * IS", without any accompanying services from The Regents. The Regents
 * does not warrant that the operation of the program will be
 * uninterrupted or error-free. The end-user understands that the program
 * was developed for research purposes and is advised not to rely
 * exclusively on the program for any reason.  IN NO EVENT SHALL THE
 * UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
// File: CmdPrint.java
// Classes: CmdPrint
// Original Author: jrobbins@ics.uci.edu
package org.tigris.gef.base;

import org.tigris.gef.presentation.Fig;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Iterator;
import java.util.Vector;

/**
 * Cmd to Print a diagram. Only works under JDK 1.2 and above.
 *
 * in 0.12.3 use PrintAction
 *
 * @author Eugenio Alvarez
 */
public class CmdPrint extends Cmd implements Printable {

    private static final long serialVersionUID = 5930094057682454011L;

    PrinterJob printerJob;
    PageFormat pageFormat;

    int maxPageIndex = 1;
    boolean fitDiagramToPage = true;
    boolean isFirstPrintCall = true;
    double scale;
    int nCol;

    double pageX, pageY, pageWidth, pageHeight;

    double diagramX, diagramY, diagramWidth, diagramHeight;

    public CmdPrint() {
        super("Print");
    }

    public CmdPrint(String diagramName) {
        this();
        setDiagramName(diagramName);
    }

    public void setDiagramName(String name) {
        setArg("diagramName", name);
    }

    public void setPrintPageNumbers(boolean b) {
        setArg("printPageNumbers", b ? Boolean.TRUE : Boolean.FALSE);
    }

    public void doIt() {
        PrinterJob printerJob = getPrinterJob();

        printerJob.setPrintable(new CmdPrint(), getPageFormat());

        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException pe) {
                Globals.showStatus("Error got a Printer exception");
            }
        }

        Globals.showStatus("Printing finished");
    }

    public void undoIt() {
        org.graph.commons.logging.LogFactory.getLog(null).info("Undo does not make sense for CmdPrint");
    }

    private boolean isFirstPrintCall() {
        return isFirstPrintCall;
    }

    private void setFirstPrintCall(boolean b) {
        isFirstPrintCall = b;
    }

    private boolean fitDiagramToPage() {
        return fitDiagramToPage;
    }

    private void setFitDiagramToPage(boolean b) {
        fitDiagramToPage = b;
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex >= maxPageIndex) {
            return NO_SUCH_PAGE;
        }

        Editor editor = Globals.curEditor();

        Graphics2D g2d = (Graphics2D) graphics;

        Rectangle drawingArea = null;

        SelectionManager sm = editor.getSelectionManager();
        Vector selectedFigs = sm.getFigs();
        Iterator iter = null;

        if (selectedFigs.size() > 0) {
            iter = selectedFigs.iterator();
        } else {
            iter = editor.getFigs().iterator();
            drawingArea = new Rectangle();
        } // end else if

        while (iter.hasNext()) {
            Fig fig = (Fig) iter.next();
            Rectangle rect = fig.getBounds();
            if (drawingArea == null) {
                drawingArea = new Rectangle(rect);
            }
            drawingArea.add(rect);
        }

        if (drawingArea == null || drawingArea.width == 0
                || drawingArea.height == 0) {
            return NO_SUCH_PAGE;
        }

        boolean h = editor.getGridHidden();
        editor.setGridHidden(true);

        if (isFirstPrintCall()) {
            setFirstPrintCall(false);

            pageWidth = pageFormat.getImageableWidth();
            pageHeight = pageFormat.getImageableHeight();

            pageX = pageFormat.getImageableX();
            pageY = pageFormat.getImageableY();

            diagramWidth = (double) drawingArea.width;
            diagramHeight = (double) drawingArea.height;

            diagramX = (double) drawingArea.x;
            diagramY = (double) drawingArea.y;

            scale = Math.min(pageWidth / (double) (drawingArea.width + 1),
                    pageHeight / (double) (drawingArea.height + 1));

            if (scale < 1.0) {
                // the printing doesn't fit in a single page;
                // let's ask the user what to do
                if (!promptFitToPage()) {
                    // the user chose to cancel the printing job: let's restore
                    // the
                    // grid, then quit
                    editor.setGridHidden(h);
                    return NO_SUCH_PAGE;
                }
            }
            if (fitDiagramToPage()) {
                maxPageIndex = 1;
            } else {
                nCol = Math.max((int) Math.ceil(diagramWidth / pageWidth), 1);
                int nRow = Math.max(
                        (int) Math.ceil(diagramHeight / pageHeight), 1);
                maxPageIndex = nCol * nRow;
            }
        }

        if (fitDiagramToPage()) {
            if (scale < 1.0) {
                g2d.scale(scale, scale);
                g2d.translate((pageX / scale) - diagramX + 1, (pageY / scale)
                        - diagramY + 1);
            } else {
                g2d.translate(pageX - diagramX + 1, pageY - diagramY + 1);
            }
        } else {
            double iCol = pageIndex % nCol;
            double iRow = pageIndex / nCol;
            double x = iCol * pageWidth;
            double y = iRow * pageHeight;
            g2d.translate(pageX - x + 1, pageY - y + 1);
        }

        g2d.setClip(drawingArea);
        editor.print(g2d);
        editor.setGridHidden(h);

        return (PAGE_EXISTS);
    }

    PrinterJob getPrinterJob() {
        if (printerJob == null) {
            printerJob = PrinterJob.getPrinterJob();
        }
        return printerJob;
    }

    PageFormat getPageFormat() {
        if (pageFormat == null) {
            PrinterJob pj = getPrinterJob();
            if (pj != null) {
                pageFormat = pj.defaultPage();
            }
        }
        return pageFormat;
    }

    void setPageFormat(PageFormat pf) {
        pageFormat = pf;
    }

    public void doPageSetup() {
        setPageFormat(getPrinterJob().pageDialog(getPageFormat()));
    }

    /**
     * Pops up a dialog to ask the user whether to fit the exceeding diagrams to
     * page or to print multiple pages. The user can also cancel the print job.
     *
     * @return false if the user has chosen to cancel the printing, true
     * otherwise
     */
    private boolean promptFitToPage() {
        Object[] options = {"Fit to page", "Multiple Pages", "Cancel"};

        int n = JOptionPane.showOptionDialog(null,
                "The diagram exceeds the current page size. Select option?",
                "Print", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (n == JOptionPane.CANCEL_OPTION) {
            return false;
        } else {
            if (n == JOptionPane.NO_OPTION) {
                setFitDiagramToPage(false);
            } else {
                setFitDiagramToPage(true);
            }
            return true;
        }
    }

} /* end class CmdPrint */
