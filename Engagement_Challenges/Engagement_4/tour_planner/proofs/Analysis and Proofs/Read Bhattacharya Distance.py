import pickle
import os
from operator import itemgetter


res2 = list(range(1,53130))
bhatD = []
os.chdir("Results/Worst Case Results/BhatD")
files = sorted(os.listdir())
for file in files:
    bhatD+=pickle.load(open(file,'rb'))
os.chdir("../../..")
output = bhatD

outputMean = sorted(output, key=itemgetter(1))
output10 = sorted(output, key=itemgetter(2))
output25 = sorted(output, key=itemgetter(3))
output50 = sorted(output, key=itemgetter(4))

print("-------------------\n")
print("Mean")
print("Best Case:")
print(outputMean[0][1], outputMean[0][5])

print("Worst Case:")
print(outputMean[-1][1], outputMean[-1][5])

print("10")
print("Best Case:")
print(output10[0][2], output10[0][5])

print("Worst Case:")
print(output10[-1][2], output10[-1][5])

print("25")
print("Best Case:")
print(output25[0][3], output25[0][5])

print("Worst Case:")
print(output25[-1][3], output25[-1][5])

print("50")
print("Best Case:")
print(output50[0][4], output50[0][5])

print("Worst Case:")
print(output50[-1][4], output50[-1][5])

#Store Results
os.chdir("Results/Worst Case Results")
#Write Results to File
file = open('Worst Case BhatD.txt', 'w')
file.write("Mean\n")
file.write("Best Case:\n")
file.write(str(outputMean[0][1])+" "+str(outputMean[0][5])+"\n")
file.write("\nWorst Case:\n")
file.write(str(outputMean[-1][1])+" "+str(outputMean[-1][5])+"\n")
file.write("-------------------\n")
file.write("10\n")
file.write("Best Case:\n")
file.write(str(output10[0][2])+" "+str(output10[0][5])+"\n")
file.write("\nWorst Case:\n")
file.write(str(output10[-1][2])+" "+str(output10[-1][5])+"\n")
file.write("-------------------\n")
file.write("25\n")
file.write("Best Case:\n")
file.write(str(output25[0][3])+" "+str(output25[0][5])+"\n")
file.write("\nWorst Case:\n")
file.write(str(output25[-1][3])+" "+str(output25[-1][5])+"\n")
file.write("-------------------\n")
file.write("-------------------\n")
file.write("50\n")
file.write("Best Case:\n")
file.write(str(output50[0][4])+" "+str(output50[0][5])+"\n")
file.write("\nWorst Case:\n")
file.write(str(output50[-1][4])+" "+str(output50[-1][5])+"\n")
file.write("-------------------\n")
file.close()

pickle.dump(output,open("Worst Case BhatD Redux.p", "wb"))
os.chdir("../..")






















