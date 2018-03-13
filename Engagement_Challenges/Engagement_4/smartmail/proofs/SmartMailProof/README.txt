First time? You need to install inotify tools by running the following 2 commands:
sudo yum install epel-release
sudo yum install inotify-tools

Every other time:
to build run:
ant
then run: 
java -jar ./dist/SmartMailProof.jar ../../source/SmartMail/dist/logs

'../../source/SmartMail/dist/logs' path is the location of the logging dir that inotofy monitors. 
The path above expects that the SmartEmailProof is run form the SmartEmailProof root folder and that
the SmartMail server is run from the SmartEmail/dist folder.

Note: SmartEmail server should already be running!