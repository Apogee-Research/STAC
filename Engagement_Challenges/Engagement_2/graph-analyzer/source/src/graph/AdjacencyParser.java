package graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;

/**
 * Parses an ADJ file and outputs an AdjacencyMatrix. The format of the file is:
 *
 * <PRE>
 *	# This is a comment
 *         Fa.Of   Sc.Of   Sem.R
 *	Fa.Of   2                      Faculty Offices
 *	Sc.Of   2       1              Faculty Secretary Offices
 *	Sem.R   1       1       0      Seminar Room
 *
 * </PRE>
 *
 * The following parsing rules are used:
 * <UL>
 * <LI>White space can be either tabs or spaces
 * <LI>Shell-style comments are used (# denotes comment to the end of line)
 * <LI>Matrix is assumed to be symmetric; the abbreviations in the margins are
 * for readability only
 * <LI>The right-most text is the labels for the rows/columns
 * <LI>Only elements from the lower-left half of the matrix are used as input
 * data; the right-most text is assumed to be the first non-number string past
 * the diagonal.
 * <LI>The first non-comment line of the file must contain data
 * </UL>
 *
 * @see AdjacencyMatrix
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class AdjacencyParser {

    /**
     * Parse an ADJ file and output an AdjacencyMatrix
     *
     * @param s	The Input stream of the file
     * @return	The
     * @exception IOException Thrown if there is any problem with the input
     * stream
     * @exception FileFormatException Thrown if there is a problem with the
     * format of contents of the file.
     */
    public static AdjacencyMatrix parse(InputStream s)
            throws IOException, FileFormatException {
        StreamTokenizer st = new StreamTokenizer(s);
        st.commentChar('#');
        st.parseNumbers();
        st.eolIsSignificant(true);
        st.wordChars('(', '(');
        st.wordChars(')', ')');
        st.wordChars('_', '_');
        st.wordChars('\'', '\'');

        int val;

        //Get the number of rows/columns from the first line
        int numCols = 0;
        while ((val = st.nextToken()) != StreamTokenizer.TT_EOL) {
            if ((val == StreamTokenizer.TT_WORD)
                    || (val == StreamTokenizer.TT_NUMBER)) {
                String cname = ((val == StreamTokenizer.TT_WORD) ? st.sval
                        : String.valueOf(st.nval));
//DBG			org.graph.commons.logging.LogFactory.getLog(null).info("Column = " + cname);
                numCols++;
            } else {
                String err = "Parse error, line " + st.lineno()
                        + " : Expected string header for column " + numCols + ".";
                throw (new FileFormatException(err));
            }
        }
//DBG	org.graph.commons.logging.LogFactory.getLog(null).info("NumCols = " + numCols);
        AdjacencyMatrix mat = new AdjacencyMatrix(numCols);

        //Fill in the matrix line-by-line
        int y = 0;
        while (y < numCols) {
            val = st.nextToken();
            if (val != StreamTokenizer.TT_WORD) {
                String err = "Parse error, line " + st.lineno() + " : Expected row header.";
                throw (new FileFormatException(err));
            }
        	//else ignore it
//DBG      	org.graph.commons.logging.LogFactory.getLog(null).info("Row header " + st.sval);

            //Traverse the row
            int x = 0;
            while (x <= y) {
                val = st.nextToken();
                if (val != StreamTokenizer.TT_NUMBER) {
                    String err = "Parse error, line " + st.lineno()
                            + " : Expected integer edge weight, column " + x + ".";
                    throw (new FileFormatException(err));
                } else {
                    mat.values[x][y] = mat.values[y][x] = (int) st.nval;
                    x++;
                }
            }

            //Grab the title off the end of the row
            String label = new String();
            while ((val = st.nextToken()) != StreamTokenizer.TT_EOL) {
                if (val == StreamTokenizer.TT_WORD) {
                    label = label + " " + st.sval;
                } else if (val == StreamTokenizer.TT_NUMBER) {
                    label = label + " " + String.valueOf(st.nval);
                } else if (val == StreamTokenizer.TT_EOF) {
                    //the end of the line was actually EOF
                    if (y == (numCols - 1)) {
                        //we are on the last line anyway
                        mat.names[y] = label;
                        return mat;
                    } else {
                        String err = "Parse error, line " + st.lineno() + " : Premature EOF.";
                        throw (new FileFormatException(err));
                    }
                } else {
                    String err = "Parse error, line " + st.lineno() + " : Expected string label.";
                    throw (new FileFormatException(err));
                }
            }
//DBG       org.graph.commons.logging.LogFactory.getLog(null).info("Label = " + label);
            mat.names[y] = label;
            y++;
        }
        //We are done with all the rows
        if ((val = st.nextToken()) != StreamTokenizer.TT_EOF) {
            String warn = "Warning, line " + st.lineno()
                    + " : Ignoring extra information at end of file";
            org.graph.commons.logging.LogFactory.getLog(null).info(warn);
        }
        return mat;
    }
}
