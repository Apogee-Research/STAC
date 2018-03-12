The input budget for this proof is 1430 bytes and the space output exceeds 100,000KB.

You must be in the InfoTraderProof directory for this to work.
To build, Call:
ant


To run the space attack proof, Call:
run java -jar ./dist/InfoTraderProof.jar

To run the time complexity red herring, Call:
run java -jar ./dist/InfoTraderProof.jar redherring

Note: InfoTrader server should already be running! 


Description of proof folder contents:
spaceattackdata folder: contains 5 files (4 malicous link docs and 1 malicous named doc). 
The docs with the malicous (empty string) link are:
blankemerlink.ged
blankenerlink.ged
blankjedilink.ged
blankmsftlink.ged
The triggering doc with the malicous (empty string) name is:
blankdocname.ged

redherring folder: contains two docs with links that point to each other.
Microsoft+Gains
New+eyePod+Released

Example: The 'Microsoft+Gains' file contains the line:
1 TITL New eyePod Released
which refers to the 'New+eyePod+Released' doc.
Similarly, the New+eyePod+Released doc contains a link to the Microsoft doc.