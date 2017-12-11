import matplotlib.pyplot as plt
import numpy as np

timeList = []
valueList = []

# Read the data from the input file
def getInfo(data):
    for i in range(1, len(data)-10): #last values are mostly 'incorrect'
        items = ((data[i].rstrip()).split())  # split the lines to get the coordinates and radius
        #Point(float(items[0]), float(items[1])) # time, value
        timeList.append(float(items[0]))
        valueList.append(float(items[1]))
    return 0

def displayPoints(type):
    if(len(timeList) != len(valueList)):
        print ('error in displayPoints')
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
    getInfo(data)  # Get the coordinates and radius of every circle

    displayPoints(type)  # show all the intersections between circles

#start the execution
main()