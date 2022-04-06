import csv
import numpy as np

file = open('/home/leticia/Development/igt_robotics_experiment/Experiments/actionsTook.csv', 'a', newline='')
write = csv.writer(file, delimiter = ",")

experimentType = ['Normal', 'Damage']
actions = ['MoveBackwards', 'MoveFowards', 'TurnLeft', 'TurnRight', 'Stop', 'DiagonalUpLeft', 'DiagonalUpRight']

write.writerow(actions)

valuesPose = []
MoveBackwards = 0
MoveFowards = 0
TurnLeft = 0
TurnRight = 0
Stop = 0
DiagonalUpLeft = 0
DiagonalUpRight = 0

arquivo = open('actionTook.txt', 'r')
for linha in arquivo:
	if (linha == 'MoveBackwards\n'):
		MoveBackwards += 1
	elif (linha == 'MoveFowards\n'):
		MoveFowards += 1
	elif (linha == 'TurnLeft\n'):
		TurnLeft += 1
	elif (linha == 'TurnRight\n'):
		TurnRight += 1
	elif (linha == 'Stop\n'):
		Stop += 1
	elif (linha == 'DiagonalUpLeft\n'):
		DiagonalUpLeft += 1
	elif (linha == 'DiagonalUpRight\n'):
		DiagonalUpRight += 1

valuesPose.append(MoveBackwards)
valuesPose.append(MoveFowards)
valuesPose.append(TurnLeft)
valuesPose.append(TurnRight)
valuesPose.append(Stop)
valuesPose.append(DiagonalUpLeft)
valuesPose.append(DiagonalUpRight)

write.writerow(valuesPose)

arquivo.close()

write.writerow('')
file.close()
