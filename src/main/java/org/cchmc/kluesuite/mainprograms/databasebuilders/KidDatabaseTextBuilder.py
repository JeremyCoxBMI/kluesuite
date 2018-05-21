

__author__ = 'COX1KB'

import sys

# sys.argv[1]   final file location (path & prefix)
# sys.argv[2]   fastA file

currName = ""

outF = open(sys.argv[1]+".kidDB.txt", 'w')

size = 0
for line in open(sys.argv[2]):
    if line[0] == '>':

        # dump previous; note that skip initialized
        if len(currName) > 1:
            outF.write(str(currName)+"\t"+str(size)+"\n")

        currName = line.strip()[1:]
        size =0

    else:
        size += len(line) -1


#dump last
outF.write(str(currName)+"\t"+str(size)+"\n")

