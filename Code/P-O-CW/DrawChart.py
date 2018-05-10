import matplotlib.pyplot as plt
import pylab
import numpy as np

timeList = []
valueList = []

timeList1 = []
timeList2 = []

xValueList1 = []
yValueList1 = []
zValueList1 = []

xValueList2 = []
yValueList2 = []
zValueList2 = []

# Read the data from the input file
def getInfoHP(data):
    for i in range(1, len(data)-10): #last values are mostly 'incorrect'
        items = ((data[i].rstrip()).split())
        timeList.append(float(items[0]))
        valueList.append(float(items[1]))
    return 0

def getInfoPos(data, ours = True):
    if (ours):
        for i in range(1, len(data)-1):
            items = ((data[i].rstrip()).split())
            timeList1.append(float(items[0]))
            xValueList1.append(float(items[1]))
            yValueList1.append(float(items[2]))
            zValueList1.append(float(items[3]))

    else: #provided Testbed
        for i in range(1, len(data)-1):
            items = ((data[i].rstrip()).split())
            timeList2.append(float(items[0]))
            xValueList2.append(float(items[1]))
            yValueList2.append(float(items[2]))
            zValueList2.append(float(items[3]))

    return 0


def displayPointsHP(type):
    if(len(timeList) != len(valueList)):
        print ('error in displayPointsHP')
        return
    n = int(round(max(timeList)))
    xZeroList = list(range(0,n))
    yZeroList = [0]*n

    plt.title(type)
    plt.xlabel("Time [milliseconds].")
    plt.ylabel("$\Delta$ " + type + " [radians].")
    plt.plot(timeList,valueList,'pb-',xZeroList,yZeroList, '-')

    maximum = max(valueList)
    minimum = min(valueList)
    timeMax = findTimeWithValue(maximum)
    timeMin = findTimeWithValue(minimum)

    axes = plt.gca()
    axes.set_ylim([-2*maximum, 2*maximum])

    maxLabel = 'maximum: (' + str(format(timeMax, '.2f')) + ', ' + str(format(maximum, '.2f')) + ')'
    axes.annotate(maxLabel, xy=(timeMax, maximum), xytext=(timeMax, maximum + maximum/5),
                arrowprops=dict(facecolor='black', shrink=0.05),
                )

    minLabel = 'minimum: (' + str(format(timeMin, '.2f')) + ', ' + str(format(minimum, '.2f')) + ')'
    axes.annotate(minLabel, xy=(timeMin, minimum), xytext=(timeMin, minimum - maximum/5),
                arrowprops=dict(facecolor='black', shrink=0.05),
                )


    intersection = getIntersectionBetweenThe2Curves()
    if (intersection != None):
        interTime = intersection[0]
        interValue = intersection[1]
        intersectionlabel = 'Intersection Value: (' + str(format(interTime, '.2f')) + ', ' + str(format(interValue, '.2f')) + ')'
        axes.annotate(intersectionlabel, xy=(interTime, interValue), xytext=(interTime+200, interValue + 0.01),
                    arrowprops=dict(facecolor='black', shrink=0.05),
                    )

    plt.show()

def displayPointsPos():
    global timeList1
    global timeList2
    global xValueList1
    global xValueList2
    global yValueList1
    global yValueList2
    global zValueList1
    global zValueList2

    if(len(timeList1) != len(xValueList1) or len(timeList1) != len(yValueList1) or len(timeList1) != len(zValueList1)):
        print ('error in displayPointsPos our')
        return
    if(len(timeList2) != len(xValueList2) or len(timeList2) != len(yValueList2) or len(timeList2) != len(zValueList2)):
        print ('error in displayPointsPos provided')
        return


    n = int(round(max(max(timeList1), max(timeList2))))

    plt.title("Position Of The Drone")
    plt.figure(1)

    # x-value
    plt.subplot(221)
    pylab.plot(timeList1, xValueList1, 'pb-', label = 'Our TB')
    pylab.plot(timeList2, xValueList2, 'pr-', label = 'Provided TB')
    pylab.legend(loc='upper left')
    pylab.title("X-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")

    #y-value
    plt.subplot(222)
    pylab.plot(timeList1, yValueList1, 'pb-', label='Our TB')
    pylab.plot(timeList2, yValueList2, 'pr-', label='Provided TB')
    pylab.legend(loc='upper right')
    pylab.title("Y-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")

    plt.subplot(223)
    pylab.plot(timeList1, zValueList1, 'pb-', label='Our TB')
    pylab.plot(timeList2, zValueList2, 'pr-', label='Provided TB')
    pylab.legend(loc='upper right')
    pylab.title("Z-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")

    plt.show()

#Namen kloppen niet, is copy paste van displayPointsPos, enkel labels aangepast
def displayPointsPosDiff():
    global timeList1
    global timeList2
    global xValueList1
    global xValueList2
    global yValueList1
    global yValueList2
    global zValueList1
    global zValueList2

    if(len(timeList1) != len(xValueList1) or len(timeList1) != len(yValueList1) or len(timeList1) != len(zValueList1)):
        print ('error in displayPointsPos our')
        return
    if(len(timeList2) != len(xValueList2) or len(timeList2) != len(yValueList2) or len(timeList2) != len(zValueList2)):
        print ('error in displayPointsPos provided')
        return


    n = int(round(max(max(timeList1), max(timeList2))))

    plt.title("Position Of The Drone")
    plt.figure(1)

    # x-value
    plt.subplot(221)
    pylab.plot(timeList1, xValueList1, 'pb-', label = 'Without diff Equations')
    pylab.plot(timeList2, xValueList2, 'pr-', label = 'With diff Equations')
    pylab.legend(loc='upper left')
    pylab.title("X-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")

    #y-value
    plt.subplot(222)
    pylab.plot(timeList1, yValueList1, 'pb-', label = 'Without diff Equations')
    pylab.plot(timeList2, yValueList2, 'pr-', label = 'With diff Equations')
    pylab.legend(loc='upper right')
    pylab.title("Y-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")

    #z-value
    plt.subplot(223)
    pylab.plot(timeList1, zValueList1, 'pb-', label = 'Without diff Equations')
    pylab.plot(timeList2, zValueList2, 'pr-', label = 'With diff Equations')
    pylab.legend(loc='upper right')
    pylab.title("Z-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")

    plt.show()


def findTimeWithValue(value):
    index = valueList.index(value)
    return timeList[index]

def findValueWithTime(time):
    index = timeList.index(time)
    return valueList[index]

def getIntersectionBetweenThe2Curves():
    for i in range(1,len(valueList) - 1):
        if (np.sign(valueList[i-1]) != np.sign(valueList[i+1])):
            minimum = min(abs(valueList[i]), abs(valueList[i-1]))
            if(minimum in valueList):
                return (findTimeWithValue(minimum),minimum)
    return None

def main():
    input = open("invoer.txt", "r")
    data = input.readlines()  # format input file to list
    input.close()
    type = str(data[0]).strip('\n')
    if type == "heading" or type == "pitch":
        getInfoHP(data)
        displayPointsHP(type)
    elif type == "Our":
        input2 = open("invoerProvidedTB.txt", "r")
        dataProvided = input2.readlines()  # format input file to list
        input2.close()

        getInfoPos(data)
        getInfoPos(dataProvided, False)
        displayPointsPos()
    else:
        input2 = open("invoerMetDiff.txt", "r")
        dataDiff = input2.readlines()  # format input file to list
        input2.close()

        getInfoPos(data)
        getInfoPos(dataDiff, False)
        displayPointsPosDiff()

#start the execution
main()