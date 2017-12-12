import matplotlib.pyplot as plt
import pylab
import numpy as np

timeList = []
valueList = []

timeListOur = []
timeListProvided = []

xValueListOur = []
yValueListOur = []
zValueListOur = []

xValueListProvided = []
yValueListProvided = []
zValueListProvided = []

# Read the data from the input file
def getInfoHP(data):
    for i in range(1, len(data)-10): #last values are mostly 'incorrect'
        items = ((data[i].rstrip()).split())
        #Point(float(items[0]), float(items[1])) # time, value
        timeList.append(float(items[0]))
        valueList.append(float(items[1]))
    return 0

def getInfoPos(data, ours = True):
    if (ours):
        for i in range(1, len(data)-1):
            items = ((data[i].rstrip()).split())
            timeListOur.append(float(items[0]))
            xValueListOur.append(float(items[1]))
            yValueListOur.append(float(items[2]))
            zValueListOur.append(float(items[3]))
    else:
        for i in range(1, len(data)-1):
            items = ((data[i].rstrip()).split())
            timeListProvided.append(float(items[0]))
            xValueListProvided.append(float(items[1]))
            yValueListProvided.append(float(items[2]))
            zValueListProvided.append(float(items[3]))

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
    global timeListOur
    global timeListProvided
    global xValueListOur
    global xValueListProvided
    global yValueListOur
    global yValueListProvided
    global zValueListOur
    global zValueListProvided

    if(len(timeListOur) != len(xValueListOur) or len(timeListOur) != len(yValueListOur) or len(timeListOur) != len(zValueListOur)):
        print ('error in displayPointsPos our')
        return
    if(len(timeListProvided) != len(xValueListProvided) or len(timeListProvided) != len(yValueListProvided) or len(timeListProvided) != len(zValueListProvided)):
        print ('error in displayPointsPos provided')
        return


    n = int(round(max(max(timeListOur), max(timeListProvided))))

    plt.title("Position Of The Drone")
    plt.figure(1)

    # x-value
    plt.subplot(221)
    pylab.plot(timeListOur, xValueListOur, 'pb-', label = 'Our TB')
    pylab.plot(timeListProvided, xValueListProvided, 'pr-', label = 'Provided TB')
    pylab.legend(loc='upper left')
    pylab.title("X-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")
    #plt.plot(timeListOur, xValueListOur, 'pb-', timeListProvided, xValueListProvided, 'pr-')

    #y-value
    plt.subplot(222)
    pylab.plot(timeListOur, yValueListOur, 'pb-', label='Our TB')
    pylab.plot(timeListProvided, yValueListProvided, 'pr-', label='Provided TB')
    pylab.legend(loc='upper right')
    pylab.title("Y-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")
    #plt.plot(timeListOur, yValueListOur, 'pb-', timeListProvided, yValueListProvided, 'pr-')

    plt.subplot(223)
    pylab.plot(timeListOur, zValueListOur, 'pb-', label='Our TB')
    pylab.plot(timeListProvided, zValueListProvided, 'pr-', label='Provided TB')
    pylab.legend(loc='upper right')
    pylab.title("Z-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")
    #plt.plot(timeListOur, zValueListOur, 'pb-', timeListProvided, zValueListProvided, 'pr-')

    plt.show()

#Namen kloppen niet, is copy paste van displayPointsPos, enkel labels aangepast
def displayPointsPosDiff():
    global timeListOur
    global timeListProvided
    global xValueListOur
    global xValueListProvided
    global yValueListOur
    global yValueListProvided
    global zValueListOur
    global zValueListProvided

    if(len(timeListOur) != len(xValueListOur) or len(timeListOur) != len(yValueListOur) or len(timeListOur) != len(zValueListOur)):
        print ('error in displayPointsPos our')
        return
    if(len(timeListProvided) != len(xValueListProvided) or len(timeListProvided) != len(yValueListProvided) or len(timeListProvided) != len(zValueListProvided)):
        print ('error in displayPointsPos provided')
        return


    n = int(round(max(max(timeListOur), max(timeListProvided))))

    plt.title("Position Of The Drone")
    plt.figure(1)

    # x-value
    plt.subplot(221)
    pylab.plot(timeListOur, xValueListOur, 'pb-', label = 'Without diff Equations')
    pylab.plot(timeListProvided, xValueListProvided, 'pr-', label = 'With diff Equations')
    pylab.legend(loc='upper left')
    pylab.title("X-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")
    #plt.plot(timeListOur, xValueListOur, 'pb-', timeListProvided, xValueListProvided, 'pr-')

    #y-value
    plt.subplot(222)
    pylab.plot(timeListOur, yValueListOur, 'pb-', label = 'Without diff Equations')
    pylab.plot(timeListProvided, yValueListProvided, 'pr-', label = 'With diff Equations')
    pylab.legend(loc='upper right')
    pylab.title("Y-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")
    #plt.plot(timeListOur, yValueListOur, 'pb-', timeListProvided, yValueListProvided, 'pr-')

    plt.subplot(223)
    pylab.plot(timeListOur, zValueListOur, 'pb-', label = 'Without diff Equations')
    pylab.plot(timeListProvided, zValueListProvided, 'pr-', label = 'With diff Equations')
    pylab.legend(loc='upper right')
    pylab.title("Z-Values")
    pylab.xlabel("Time [milliseconds]")
    pylab.ylabel("Position [World Coordinates]")
    #plt.plot(timeListOur, zValueListOur, 'pb-', timeListProvided, zValueListProvided, 'pr-')

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

    type = str(data[0])
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