// %1035450542467:org.tigris.gef.ocl%
// Copyright (c) 1996-99 The Regents of the University of California. All
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
package org.tigris.gef.ocl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.*;

import org.graph.commons.logging.Log;
import org.graph.commons.logging.LogFactory;

public class OCLExpander {

    // //////////////////////////////////////////////////////////////
    // constants

    public static String OCL_START = "<ocl";
    public static String OCL_END = "</ocl>";
    // //////////////////////////////////////////////////////////////
    // instance variables
    public Map _templates = new Hashtable();
    public Hashtable _bindings = new Hashtable();
    public boolean _useXMLEscapes = true;

    protected OCLEvaluator evaluator;

    private static final Log LOG = LogFactory.getLog(OCLExpander.class);

    // //////////////////////////////////////////////////////////////
    // constructor
    public OCLExpander(Map templates) {
        _templates = templates;
        createEvaluator();
    }

    protected void createEvaluator() {
        evaluator = new OCLEvaluator();
    }

    // //////////////////////////////////////////////////////////////
    // template expansion
    public void expand(OutputStream w, Object target) throws ExpansionException {
        expandContent(new PrintWriter(w), target, "", "");
    }

    private void expand(OutputStream w, Object target, String prefix,
            String suffix) throws ExpansionException {
        expandContent(new PrintWriter(w), target, prefix, suffix);
    }

    public void expand(Writer w, Object target) throws ExpansionException {
        expand(w, target, "", "");
    }

    public void expand(Writer w, Object target, String prefix)
            throws ExpansionException {
        expand(w, target, prefix, "");
    }

    private void expand(Writer w, Object target, String prefix, String suffix)
            throws ExpansionException {
        PrintWriter pw;
        if (w instanceof PrintWriter) {
            pw = (PrintWriter) w;
        } else {
            pw = new PrintWriter(w);
        }
        expandContent(pw, target, prefix, suffix);
    }

    private void expandContent(PrintWriter printWriter, Object target,
            String prefix, String suffix) throws ExpansionException {

        if (target == null) {
            return;
        }

        List exprs = findTemplatesFor(target);
        String expr = null;
        int numExpr = (exprs == null) ? 0 : exprs.size();
        for (int i = 0; i < numExpr && expr == null; i++) {
            TemplateRecord tr = (TemplateRecord) exprs.get(i);
            if (tr.getGuard() == null || tr.getGuard().equals("")) {
                expr = tr.body;
                break;
            }

            _bindings.put("self", target);
            List results = evaluate(_bindings, tr.getGuard());
            if (results.size() > 0 && !Boolean.FALSE.equals(results.get(0))) {
                expr = tr.body;
                break;
            }
        }

        if (expr == null) {
            printWriter.print(prefix);

            String s = target.toString();
            if (target instanceof MethodInfo) {
                /**
                 * If the target is a MethodInfo object then we should call the
                 * method on the object inside passing the writer as an
                 * argument.
                 */
                MethodInfo mi = (MethodInfo) target;
                Object[] params = new Object[2];
                params[0] = printWriter;
                params[1] = new Integer(prefix.length());
                try {
                    mi.getMethod().invoke(mi.getObject(), params);
                } catch (IllegalArgumentException e) {
                    throw new ExpansionException(e);
                } catch (IllegalAccessException e) {
                    throw new ExpansionException(e);
                } catch (InvocationTargetException e) {
                    throw new ExpansionException(e);
                }
            } else {
                if (_useXMLEscapes) {
                    s = replaceWithXMLEscapes(s);
                }

                printWriter.print(s);
            }
            printWriter.println(suffix);
            return;
        }

        StringTokenizer st = new StringTokenizer(expr, "\n\r");
        int lineNo = 0;
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            expandLine(printWriter, line, target, prefix, suffix, ++lineNo);
        }
    } // end of expand

    private void expandLine(PrintWriter pw, String line, Object target,
            String prefix, String suffix, int lineNo) throws ExpansionException {
        // if no embedded expression then output line else
        // then loop over all values of expr and call recursively for each result
        int startTagPos = line.indexOf(OCL_START, 0);
        int endTagPos = line.indexOf(OCL_END, 0);
        if (startTagPos == -1 || endTagPos == -1) { // no embedded expr's
            pw.println(prefix + line + suffix);
            return;
        }

        if (line.indexOf(OCL_START, endTagPos) >= 0) {
            while (startTagPos >= 0) {
                // There are multiple embedded expressions on a line.
                int expressionPos = line.indexOf('>', startTagPos) + 1;
                boolean ignoreNull = isIgnoreNull(line.substring(
                        startTagPos + 4, expressionPos));
                String before = line.substring(0, startTagPos);
                String expr = line.substring(expressionPos, endTagPos);
                String after = line.substring(endTagPos + OCL_END.length());
                _bindings.put("self", target);
                if (target == null) {
                    throw new ExpansionException(
                            "Target is null when evaluating the expression '"
                            + expr + "' at line " + lineNo);
                }
                List results = evaluate(_bindings, expr);
                Iterator iter = results.iterator();
                StringWriter sw = new StringWriter();
                if (iter.hasNext()) {
                    Object o = iter.next();
                    if (o == null && !ignoreNull) {
                        throw new ExpansionException(
                                "Evaluated the expression '" + expr
                                + "' to null on object of class "
                                + target.getClass().getName()
                                + " at line " + lineNo);
                    }
                    expand(sw, o, before, after);
                }
                if (iter.hasNext()) {
                    throw new IllegalStateException(
                            "A repeating expression cannot be on the same line as any other expression.");
                }
                line = sw.toString();
                while (line.endsWith("\n") || line.endsWith("\r")) {
                    line = line.substring(0, line.length() - 1);
                }

                startTagPos = line.indexOf(OCL_START, 0);
                endTagPos = line.indexOf(OCL_END, 0);
            }
            pw.println(prefix + line + suffix);
        } else {
            // assume one embedded expression on line
            int expressionPos = line.indexOf('>', startTagPos) + 1;
            boolean ignoreNull = isIgnoreNull(line.substring(startTagPos + 4,
                    expressionPos));
            prefix = prefix + line.substring(0, startTagPos);

            String expr = line.substring(expressionPos, endTagPos);

            suffix = line.substring(endTagPos + OCL_END.length()) + suffix;
            _bindings.put("self", target);
            if (target == null) {
                throw new ExpansionException(
                        "Target is null when evaluating the expression '"
                        + expr + "' at line " + lineNo);
            }
            List results = evaluate(_bindings, expr);
            Iterator iter = results.iterator();
            while (iter.hasNext()) {
                Object o = iter.next();
                if (o == null && !ignoreNull) {
                    throw new ExpansionException("Evaluated the expression '"
                            + expr + "' to null on object of class "
                            + target.getClass().getName() + " at line "
                            + lineNo);
                }
                expand(pw, o, prefix, suffix);
            }
        }
    }

    private boolean isIgnoreNull(String attributes) {
        boolean ignoreNull = (attributes.startsWith(" ignoreNull>"));
        return ignoreNull;
    }

    /**
     * Find the List of templates that could apply to this target object. That
     * includes the templates for its class and all superclasses.
     * Needs-More-Work: should cache.
     */
    private List findTemplatesFor(Object target) {
        List res = null;
        boolean shared = true;
        for (Class c = target.getClass(); c != null; c = c.getSuperclass()) {
            List temps = (List) _templates.get(c);
            if (temps == null) {
                continue;
            }

            if (res == null) {
                // if only one template applies, return it
                res = temps;
            } else {
                // if another template also applies, merge the two Lists,
                // but leave the original unchanged
                if (shared) {
                    shared = false;
                    List newRes = new ArrayList();
                    for (int i = 0; i < res.size(); i++) {
                        newRes.add(res.get(i));
                    }

                    res = newRes;
                }

                for (int j = 0; j < temps.size(); j++) {
                    res.add(temps.get(j));
                }
            }
        }

        return res;
    }

    private String replaceWithXMLEscapes(String s) {
        s = replaceAll(s, "&", "&amp;");
        s = replaceAll(s, "<", "&lt;");
        s = replaceAll(s, ">", "&gt;");
        s = replaceAll(s, "\"", "&quot;");
        s = replaceAll(s, "'", "&apos;");
        return s;
    }

    private String replaceAll(String s, String pat, String rep) {
        int index = s.indexOf(pat);
        int patLen = pat.length();
        int repLen = rep.length();
        while (index != -1) {
            s = s.substring(0, index) + rep + s.substring(index + patLen);
            index = s.indexOf(pat, index + repLen);
        }

        return s;
    }

    /**
     * Evaluate an expression. The expression can be in the form -
     *
     * <p>
     * <b>Style 1</b> <i>expressionpart{.expressionpart}</i>
     * <p>
     * Note - {} indicates an optional repeating expression part
     * <p>
     * The first <i>expressionpart</i> should be the keyword "self" which refers
     * to the first bound object in the bindings map. Any further
     * <i>expressionparts</i> are either attributes, methods or properties of
     * the preceding part.
     *
     * <p>
     * For example
     * <ul>
     * <li>Given a Point <code>self.x</code> would return the x instance
     * variable from Point</li>
     * <li>Given a List <code>self.size</code> would return the result of the
     * size() method on that List</li>
     * <li>Given a Color <code>self.red</code> would return the red property of
     * that Color (by calling the getRed() method)</li>
     * </ul>
     *
     * <p>
     * For example, given a Component, the expression
     * <code>self.minimumSize.width</code> will get the minimum width of that
     * Component by first calling getMinimumSize() and then retrieving the width
     * attribute from the result of that call.
     *
     * <p>
     * If an <i>expressionpart</i> returns a collection or an array then the
     * range of the items returned can be restricted by specifying a required
     * range by comma separated start and end values. These values can be
     * integer numbers with 0 representing the first item. An last item can be
     * represented by *.
     *
     * <p>
     * For example, given a Container, the expression      <code>self.getComponents[1,*] will return all components except the
     * first.
     *
     * <p>
     * <b>Style 2</b> <i>package.Class.staticMethod(style1expression)</i>
     *
     * <p>
     * The second form of an expression allows a value evaluated from the first
     * form to be passed to some static method.
     *
     * For example, given a Color, the expression
     * <code>org.tigris.gef.util.PgmlUtility.getPgmlColor(self)</code> will pass
     * the Color as an argument to the getPgmlColor method of PgmlUtility in
     * order to format the Color according to PGML style.
     *
     * @param bindings A map of expression part to object bindings. This is
     * expected to be prepopulated with "self" bound to the main target object.
     * @param expr The expression to evaluate.
     * @return A list of resulting items that satisfy the expression.
     *
     * @throws ExpansionException on any error.
     */
    private List evaluate(Map bindings, String expr) throws ExpansionException {
        if ("self".equals(expr) || expr.startsWith("self.")) {
            // If the expression refers to self then evaluate and
            // return the resulting attributes
            List values = evaluator.eval(bindings, expr);
            return values;
        } else {
            // If the expression does not refer to self then the assumption is
            // that it is an expression wrapped in a static method call.
            int bracketPosn = expr.indexOf('(');
            String classAndMethod = expr.substring(0, bracketPosn);
            int lastBracketPosn = expr.lastIndexOf(')');
            expr = expr.substring(bracketPosn + 1, lastBracketPosn);
            List values = evaluator.eval(bindings, expr);
            ArrayList newValues = new ArrayList(values.size());
            int methodSeperator = classAndMethod.lastIndexOf('.');
            String className = classAndMethod.substring(0, methodSeperator);
            String methodName = classAndMethod.substring(methodSeperator + 1);

            try {

                Class clazz = Class.forName(className);

                for (Iterator it = values.iterator(); it.hasNext();) {

                    Object o = it.next();

                    final Class<?> argClass;
                    if (o == null) {
                        // TODO: For now if we have a null value argument
                        // object then we assume it is of type Object. We need
                        // a better solution so that the template can specify
                        // a class to cast to.
                        argClass = java.lang.Object.class;
                    } else {
                        argClass = o.getClass();
                    }
                    Method m = getMethod(clazz, argClass, methodName);
                    if (!Modifier.isStatic(m.getModifiers())) {
                        throw new ExpansionException("The method "
                                + m.toString() + " was expected to be static");
                    }

                    Object[] args = new Object[1];
                    args[0] = o;
                    o = m.invoke(null, args);
                    if (o instanceof List) {
                        return (List) o;
                    }
                    newValues.add(o);
                }
            } catch (Exception e) {
                if (e instanceof ExpansionException) {
                    throw (ExpansionException) e;
                }
                throw new ExpansionException(e);
            }

            return newValues;
        }
    }

    /**
     * Get the Method object from a class which has the given name and a
     * parameter closest to matching the parameter class.
     *
     * @param targetClass
     * @param parameterClass
     * @param methodName
     * @return the Method
     * @throws ExpansionException if no such method exists
     */
    private Method getMethod(Class targetClass, Class parameterClass,
            String methodName) throws ExpansionException {

        Class parameter = parameterClass;

        Method m[] = targetClass.getMethods();
        Method method = null;

        do {
            for (int i = 0; i < m.length; ++i) {
                if (m[i].getName().equals(methodName)
                        && m[i].getParameterTypes().length == 1
                        && m[i].getParameterTypes()[0].equals(parameter)) {
                    return m[i];
                }
            }
            parameter = parameter.getSuperclass();
        } while (parameter != null);

        throw new ExpansionException("Can't find a method " + methodName
                + " on " + targetClass.getName()
                + " that takes an object compatible with "
                + parameterClass.getName() + " as the only argument");
    }

}
