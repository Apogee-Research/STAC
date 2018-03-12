import subprocess
import os
import json
import sys

logdir = "logs/"
try:
	os.mkdir(logdir)
except:
	True

exp_data = [] 

start = int(sys.argv[1])
stop = int(sys.argv[2])
interval = int(sys.argv[3])

for n in range(start, stop, interval):
	fw = open(logdir + str(n) + ".csv", "w+")
	proc = subprocess.Popen(['sh','send_all_tests.sh', str(n), '8080'],stdout=subprocess.PIPE)
	trial_data = [n] # [N, [SIZE, LAPLACIAN TIME], [SIZE, CPSP TIME], [SIZE, MST TIME], [SIZE, GOOD MUL TIME], [SIZE, BAD MUL TIME]]
	for line in proc.stdout:
		check = line.rstrip()
		if (check.find("Laplacian Measured") > -1):
			row = check.split(",")
			time = float(row[1])
			siz = float(row[3])
			trial_data.append([siz, time])
		if (check.find("MST Measured") > -1):
			row = check.split(",")
			time = float(row[1])
			siz = float(row[3])
			trial_data.append([siz, time])
		if (check.find("CPSP Measured") > -1):
                        row = check.split(",")
                        time = float(row[1])
                        siz = float(row[3])
                        trial_data.append([siz, time])

                if (check.find("Multiply Measured") > -1):
                        row = check.split(",")
                        time = float(row[1])
                        siz = float(row[3])
                        trial_data.append([siz, time])

		print check
		fw.write(line)
	fw.write(str(trial_data))
	fw.close()
	exp_data.append(trial_data)
	print trial_data
	


print exp_data

fw = open(logdir + "results.json", "w+")
fw.write(json.dumps(exp_data))
fw.close()

fw = open(logdir + "results.csv", "w+")
fw.write("N,AverageCase,WorstCase\n")
for item in exp_data:
	s = "%i,%f,%f\n" % (item[0], item[4][1], item[5][1])
	fw.write(s)
fw.close()



