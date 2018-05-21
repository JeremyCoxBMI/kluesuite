import sys

histogram = dict()

for line in open(sys.argv[1]):

    if (len(line)>0):  #protect blank lines
        count = int(line)
        if count in histogram:
            histogram[count]+=1
        else:
            histogram[count]=1

for key in histogram:
    print key,"\t", histogram[key]

