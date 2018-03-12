This directory contains items pertaining to timings of BidComparison message creation.

The two .db files are json files containing timings of BidComparison messages.  For each bid value, 3 timings were recorded.  These are stored under the ref_times keyword
as a map from the bid amount to the list of timings.

(These were obtained by running the BidPal auction attack, where BidPal is the auction kernel used in PowerBroker with a command line user interface, which we developed
as a proof of concept, and hope to include in Engagement 3, pending approval by DARPA.)

bid_attack_SC_0_500.db is for the 0-indexed version of the vulnerable code, while bid_attack_SC_500.db is for the more obvious version of the vulnerable code.

graphBidTimes.py is a script that will plot the times in either of the files.  (Run python graphBidTimes.py <db file>.)