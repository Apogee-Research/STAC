// Copyright (c) 1996-2009 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
// File: Mode.java
// Classes: Mode
// Original Author: thorsten Jun 2000
// $Id: Mode.java 1265 2009-08-19 05:57:56Z mvw $
package org.tigris.gef.base;

import java.util.Hashtable;

/**
 * This is the base interface for all modes in GEF. A Mode is responsible for
 * handling most of the events that come to the Editor. A Mode defines a context
 * for interpreting those events. Systems using GEF can define their own Modes
 * by subclassing from FigModifyingMode.
 *
 * @see ModeImpl
 * @see Cmd
 * @see FigModifyingMode
 */
public interface Mode {

    public void start();

    public void done();

    public void setArgs(Hashtable args);

    public void setArg(String key, Object value);

    public Hashtable getArgs();

    public Object getArg(String key);

    public boolean canExit();

    public void init(Hashtable parameters);

} /* end interface Mode */
