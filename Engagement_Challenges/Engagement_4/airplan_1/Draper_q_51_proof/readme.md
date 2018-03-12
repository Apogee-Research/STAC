# Airplan 1

### Running the tool

### Running AProVE

library for `javax.servlet` has to be in the lib folder:  
`# cp $libs/javax.servlet-api-3.1.0.jar $APP_HOME/lib/`

AProVE only gave MAYBE as result.

### General
Start class: `edu.cyberapex.home.StacMain`


### Interesting methods
The most interesting method is `edu.cyberapex.chart.OptimalPath.calculateOptimalPath`.

The routemap is not checked for negative values. Non-termination can be caused by
using negative values for the cost (for example).  ==> see the example script
