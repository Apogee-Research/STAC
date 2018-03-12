# read data
lines = [int(line.split("length ")[1][:-1]) for line in open("data.txt", "r").readlines()]
print("0", lines[0], "-1" ,lines[-1], "Sum:",sum(lines))

