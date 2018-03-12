import matplotlib.pyplot as plt
import sys
import json
import numpy as np
 
db_filename = sys.argv[1]
print db_filename
with open(db_filename, 'r') as reader:
    tempDict = json.load(reader)
    # Note: json has turned our ints into strings, so we have to convert them back

    ref_bids = tempDict["ref_bids"]
    avg_delta = float(tempDict["avg_delta"])
    ref_times = tempDict["ref_times"]
    strbids = [bid for bid in ref_times]
    bids = [int(bid) for bid in strbids]
    cpbids = [int(bid) for bid in strbids]
    bids.extend(cpbids)
    bids.extend(cpbids)
    times = [float(ref_times[bid][0]) for bid in strbids]
    times.extend([float(ref_times[bid][1]) for bid in strbids])
    times.extend([float(ref_times[bid][2]) for bid in strbids])

  
    # plot points
    plt.plot(bids, times, 'bo', markersize=1)
    plt.xlabel("Bid value")
    plt.ylabel("Time in seconds")
    plt.title("BidComparison Construction Times")
    plt.show()
            
