import numpy as np
import matplotlib.pyplot as plt


# centro (x,y) das estacoes A, B, C, D, respectivamente
energyStationsX =[+1.1250e+0, +3.1000e+0, -1.8000e+0, -4.3500e+0]
energyStationsY =[-2.4750e+0, +2.6250e+0, +1.3250e+0, -2.7750e+0]

sizeStation = 1.0

poweStationName = ['A', 'B', 'C', 'D']
p = 0

# Plot trajectory
fig, ax = plt.subplots(figsize=(9, 7))
color = 0
colorGraphs = ['m','b','g']

#Plota as estações de energia
for i in range(0,4):
	squareEnergyEstationX = []
	squareEnergyEstationY = []

	#canto superior esquerdo
	squareEnergyEstationX.append(energyStationsX[i] - sizeStation/2)
	squareEnergyEstationY.append(energyStationsY[i] + sizeStation/2)

	#canto inferior esquerdo
	squareEnergyEstationX.append(energyStationsX[i] - sizeStation/2)
	squareEnergyEstationY.append(energyStationsY[i] - sizeStation/2)

	#canto inferior direito
	squareEnergyEstationX.append(energyStationsX[i] + sizeStation/2)
	squareEnergyEstationY.append(energyStationsY[i] - sizeStation/2)

	#canto superior direito
	squareEnergyEstationX.append(energyStationsX[i] + sizeStation/2)
	squareEnergyEstationY.append(energyStationsY[i] + sizeStation/2)

	#canto superior esquerdo -- repete pra fechar o retangulo
	squareEnergyEstationX.append(energyStationsX[i] - sizeStation/2)
	squareEnergyEstationY.append(energyStationsY[i] + sizeStation/2)

	plt.fill_between(squareEnergyEstationX, squareEnergyEstationY, label=poweStationName[p])
	p += 1


posIniAll = []
posFimAll = []

j = 1

while (j < 4):
	with open('GroundTruthPosition0'+str(j)+'.txt', 'r') as f:
		x = f.read()
		lines = x.split()
	tam = len(lines)
	posTup = []

	i = 0

	while i < (tam-1):
		x = float(lines[i])
		y = float(lines[i+1])
		pos = [x, y]#[-y, x]
		posTup.append(pos)
		i = i+2

	posX, posY = zip(*posTup)

	x_ini = posX[0]
	y_ini = posY[0]

	x_fim = posX[len(posX)-1]
	y_fim = posY[len(posY)-1]

	posIt = [posX[0], posY[0]]
	posItF = [posX[len(posX)-1], posY[len(posY)-1]]

	posIniAll.append(posIt)
	posFimAll.append(posItF)

	with open('exploringActionsNumber0'+str(j)+'.txt', 'r') as f:
		actions = f.read()
	actions = actions.replace("\n", "")

	ax.scatter(posX, posY, label = actions + ' actions')

	j = j + 1

posXI, posYI = zip(*posIniAll)
posXF, posYF = zip(*posFimAll)

ax.scatter(posXI[2],posYI[2],c='k',marker='v', s=100, zorder=3, label = 'Start Pose')
ax.scatter(posXF,posYF,c='r',marker='D', s=100,  zorder=3, label = 'End')

ax.scatter(posXI[0],posYI[0],c='k',marker='>', s=100, zorder=3)
ax.scatter(posXI[1],posYI[1],c='k',marker='^', s=100, zorder=3)

c = 1
title = ['Trajectory: Long-term decisions','Trajectory: Short-term decisions']
nameSave = ['Trajectory_Long-term decisions','Trajectory - Short-term decisions']

#ax.grid(True)
ax.legend(loc='best', fontsize = 'large', ncol = 3)
plt.xlabel("x", fontsize=18)
plt.ylabel("y", fontsize=18)
plt.rcParams['xtick.labelsize'] = 20
plt.rcParams['ytick.labelsize'] = 20
plt.title(title[c],  fontsize=18)
plt.ylim(-4.8,4.8)
plt.xlim(-4.8,4.8)
#plt.savefig(nameSave[c] + '.png')
plt.show()
