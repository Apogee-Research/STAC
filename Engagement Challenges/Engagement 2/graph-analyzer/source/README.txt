"Billion Graphs" Space Attack

This challenge is space utilization attack on an application that
parses and exports a modified version of the GraphViz DOT graph
specification. A DOT parser was modified to create a keyword called
“container”, which allows for a node in the graph to be represented by
an arbitrary DOT graph. In our specification the container objects are
allowed to have nested containers, creating an attack that is
fundamentally equivalent to the billion laughs space utilization
attack [https://en.wikipedia.org/wiki/Billion_laughs]. When graphs are
exported to a vector graphics format (PostScript), inputs utilizing
the container keyword can have output PostScript files that are
exponentially larger than their input DOT files.

The main class is 'user.commands.CommandProcessor'.  The main class is
set as the default call in the Jar's manifest. There are no additional
external jar dependencies besides a 1.7 version (or greater) JVM.

The project is a Netbeans project that can be built using basic ant
commands The 'ant jar' command builds the jar file and places it in
the dist folder.  Simply run ‘ant jar from the root folder of the
project.

Commands that generate image files, like PNG, may generate more than
one image file per occurrence. The argument you pass as
‘outputfilename’ is the root location and file name. The application
may generate multiple images using this root by appending to it.

********* Generating PostScript files *********

To generate a postscript from the dot file use the following arguments:

    dot youfilelocation.dot xy diagram ps outputfilename

Your actual call will look similar to this:
    java -jar dist/GraphDisplay.jar dot optional_datafolderpath/yourfile.dot xy diagram ps outputfilefolder/outputfilename

********* Generating PNG files *********

To generate a png from the dot file use the following arguments:

    dot youfilelocation.dot xy diagram png outputfilefolder/outputfilename

Your actual call will look similar to this:

    java -jar dist/GraphDisplay.jar dot optional_datafolderpath/yourfile.dot xy diagram png outputfilefolder/outputfilename
