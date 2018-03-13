import json

# Build CSV string
def buildMatrix(n, vA, vB):
    matrix = ""
    for i in range(n):
        row = ""
        for j in range(n):
            if j ==0:
                row+=vA
            else:
                row+=","+vB
        matrix+=row+"\n"
    return {"rows": n, "cols": n, "matrix": matrix}

                
n = 650
valA = "99999999999999999"#"88888888888888888"#"0000000019088e-40"#"."+(40*"0")+"19088"
valB = "10000000000000000"#valA
matrix1 = buildMatrix(n, valA, valB)
matrix2 = matrix1

multOp = {"operation": 1, "numberOfArguments": 2, "args": [matrix1, matrix2]}
outputString = json.dumps(multOp, separators=(',', ':')) + "\n"
print(len(outputString))
with open("Slow.json","w") as file:
    file.write(outputString)



