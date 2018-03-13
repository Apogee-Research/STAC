import pickle
import os
import math
import matplotlib.pyplot as plt
import sys
import warnings
warnings.filterwarnings("ignore")

def findPercentile(queries, sortedDataIn):
    i = queries
    while i > 1 and sortedDataIn.count(i) == 0:
        i=i-1
    if sortedDataIn.count(i) > 0:
        lastIndex = len(sortedDataIn)-list(reversed(sortedDataIn)).index(i)-1
        percentile = (lastIndex+1)*100/len(sortedDataIn)
        return percentile
    return 0
    
def getPercentileData(maxQ, sortedDataIn):
    queriesX = list(range(0,maxQ+1,2))
    percentileY = []
    for query in queriesX:
        percentileY.append(findPercentile(query, sortedDataIn))
    return percentileY

# Read In Data
os.chdir("Results")
wcResult = pickle.load(open("Guess Tour All Methods Processed Results Encrypted.p","rb"))
sampleResult = pickle.load(open("Guess Tour All Methods Sampling Encrypted.p","rb"))
eeResult = pickle.load(open("Guess Tour All Methods Processed Results Edge by Edge Encrypted.p","rb"))
os.chdir("..")

wcList = [wcResult["Ndim"], wcResult["MahD"], wcResult["VectorD"]]
sampleList = [sampleResult["Ndim"], sampleResult["MahD"], sampleResult["VectorD"]]
edgeWCList = [eeResult["Intersection"],eeResult["Bayes"]]

# Index Representations
# B,W Ind -> 0,1
# B,W MahD 1 -> 2,3
# B,W MahD 0.1 -> 4,5
# B,W Mean Bhat -> 6,7
# B Bhat 10
# B Bhat 25
# B Bhat 50
# W BBN -> 11
caseNamesRev =  ["Ind B","Ind W",
                 "MahD 1 B", "MahD 1 W",
                 "MahD 0.1 B", "MahD 0.1 W",
                 "BhatD Mean W", "BhatD B",
                 "Bhat 10 W", "Bhat 25 W",
                 "Bhat 50 W", "BBN W"]
caseNames = ["B Ind","W Ind",
             "B MahD 1", "W MahD 1",
             "B MahD 0.1", "W MahD 0.1",
             "W Mean BhatD", "B BhatD",
             "W Bhat 10", "W Bhat 25",
             "W Bhat 50", "W BBN"]

r = 95/100
sc = 2#8
fontsize = 12
queriesX = 250
plotX = list(range(0,queriesX+1,2))
samples = 500
print("Percentage:",r*100)

redlabel = "$D_M$"
blacklabel = "2-norm"

for wcIndex in range(len(caseNames)):#[0,1,2,3,4,5,6,8,10,7,11]:
    # Number queries for rth percentile
    percentileQueries = []
    # Frequency of results requiring one oracle query
    freqOneQuery = []
    freqTwoQuery = []
    maxQuery = []
    # Percentile Data for plotting
    percentilePlotData = [[],[],[]]
    for guessMethodIndex in range(3):
        # Sorted List
        sortedData = sorted(wcList[guessMethodIndex][wcIndex])
        # Setup percentile data for plotting
        percentilePlotData[guessMethodIndex] = getPercentileData(queriesX, sortedData)
        rPercentileValue = sortedData[math.floor(samples*r)-1]
        percentileQueries.append(rPercentileValue)
        freqOneQuery.append(sortedData.count(1))
        freqTwoQuery.append(sortedData.count(1)+sortedData.count(2))
        maxQuery.append(max(sortedData))
    freqNumRightList = []
    for eeguessMethodIndex in range(2):
        numRight = sorted(edgeWCList[eeguessMethodIndex][wcIndex])
        freqNumRight = [round(numRight.count(i)*100/samples) for i in range(6)]
        freqNumRightList.append(freqNumRight)
    caseName = caseNamesRev[wcIndex]
    print(caseName,"\n\t95% Bounds:",percentileQueries)
    print("\tFreq Ones :",freqOneQuery,"\n")
    print("\tFreq Twos :",freqTwoQuery,"\n")
    print("\tMax Query :",maxQuery,"\n")
    print("\tCities Correct: 0, 1, 2, 3, 4, 5")
    print("\t-------------------------------------")
    print("\tIntersection %:",freqNumRightList[0])
    print("\tBayes        %:",freqNumRightList[1])
    fig = plt.figure()
    plt.plot([0,260],[95,95],color='black',ls='--')
    plt.scatter(plotX,percentilePlotData[0],s=sc,color='blue')
    plt.scatter(plotX,percentilePlotData[1],s=sc,color='red',label=redlabel)
    plt.scatter(plotX,percentilePlotData[2],s=sc,color='black',label=blacklabel)
    plt.xlabel("Number of Oracle Queries Required")
    plt.ylabel("Percentile of Results")
    plt.legend(bbox_to_anchor=(1.018, .2045),loc=1)
    plt.xlim([0,260])
    plt.ylim([-5,105])
    os.chdir('Figures/Best_Worst_All')
    figname = caseName+' 500 Samples.png'
#    fig.savefig(figname, bbox_inches='tight', format='png',dpi=1200)
    os.chdir('../..')
#    plt.show()
    plt.close()
    #break
#sys.exit()
print("\n-----------\nSamples")
# Number queries for rth percentile
percentileQueriesSamples = []
# Frequency of results requiring one oracle query
freqOneQuerySamples = []
freqless212 = []
percentilePlotDataSamples = [[],[],[]]
samples = 5000
for guessMethodIndex in range(3):
    # Sorted List
    sortedData = sorted(sampleList[guessMethodIndex])
    # Setup percentile data for plotting
    percentilePlotDataSamples[guessMethodIndex] = getPercentileData(queriesX, sortedData)
    rPercentileValue = sortedData[math.floor(samples*r)-1]
    percentileQueriesSamples.append(rPercentileValue)
    freqOneQuerySamples.append(sortedData.count(1))
    freqless212.append(len([z for z in sortedData if z<212 ]))
print("Samples","\n\t95% Bounds:",percentileQueriesSamples)
print("\tFreq Ones :",freqOneQuerySamples,"\n")
print("\tFreq <212 :",freqless212,"\n")
fig = plt.figure()
plt.plot([0,260],[95,95],color='black',ls='--')
plt.scatter(plotX,percentilePlotDataSamples[0],s=sc,color='blue')
plt.scatter(plotX,percentilePlotDataSamples[1],s=sc,color='red',label=redlabel)
plt.scatter(plotX,percentilePlotDataSamples[2],s=sc,color='black',label=blacklabel)
plt.xlabel("Number of Oracle Queries Required")
plt.ylabel("Percentile of Results")
plt.legend(bbox_to_anchor=(1.018, .2045),loc=1)
plt.xlim([0,260])
plt.ylim([-5,105])
os.chdir('Figures/Best_Worst')
figname = '5000 Samples of MSTs.png'
#fig.savefig(figname, bbox_inches='tight', format='png',dpi=1200)
os.chdir('../..')
#plt.show()
plt.close()





















