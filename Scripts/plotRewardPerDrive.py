import numpy as np
import matplotlib.pyplot as plt
import math

i = 0

#Actions and Rewards are ploted in the same axes

#This script plots the average and standard deviation (filled) of the rewards. The window choose is 10

#Numero do experimento, numero de ações, reward do episodio, reward global

tn = 1
title = ['Long-term decisions (γ = 0.9)', 'Short-term decisions (γ = 0.1)']
nameSave = ['Long-term decisions']
fig, ax = plt.subplots(figsize=(7, 5))
color = 0
colorGraphs = ['m','b','g']
exps = ['Curiosity', 'Energy']

arq = 1

while (arq < 2):
	i = 0
	with open('rewardsPerDrive.txt', 'r') as f:
		x = f.read()
		lines = x.split()
	tam = len(lines)

	mediaRep = 0 #media das rewards dos episodios
	mediaAep = 0 #media das acoes dos episodios
	win = 10 #janela de episodios calculada

	acaoEpisodio = []
	episodioReward = []

	dpAcao = []
	dpReward = []
	mediasR = []
	mediasA = []

	k = 1
	j = 0

	while i < (tam-1):
		mediaAep = mediaAep + float(lines[i+1])
		mediaRep = mediaRep + float(lines[i+2])
		j = j + 1
		if(j == win*k):
			medRT = [j, mediaRep/win]
			medAT = [j, mediaAep/win]
			mediasA.append(mediaAep/win)
			mediasR.append(mediaRep/win)
			episodioReward.append(medRT)
			acaoEpisodio.append(medAT)
			mediaAep = 0
			mediaRep = 0
			k = k + 1
		i = i+4

	posX, posY = zip(*episodioReward)
	posXA, posYA = zip(*acaoEpisodio)

	i = 0
	dpAcao = []
	dpReward = []
	k = 1
	sumDifAc = 0
	sumDifRe = 0
	l = 0
	j = 0

	while i < (tam-1):
		sumDifAc = sumDifAc + math.pow((float(lines[i+1]) - mediasA[l]), 2)
		sumDifRe = sumDifRe + math.pow((float(lines[i+2]) - mediasR[l]), 2)
		j = j + 1
		if(j == win*k):
			dpA = [j, math.sqrt(sumDifAc/win)]
			dpR = [j, math.sqrt(sumDifRe/win)]
			dpReward.append(dpR)
			dpAcao.append(dpA)
			sumDifAc = 0
			sumDifRe = 0
			k = k + 1
			l = l + 1
		i = i+4

	posXDR, posYDR = zip(*dpReward)
	posXDA, posYDA = zip(*dpAcao)


	plt.plot(posX, posY, c=colorGraphs[color], label = exps[color] + ' Average')
	plt.fill_between(posX, [elemA + elemB for elemA, elemB in zip(posY, posYDR)], [elemA - elemB for elemA, elemB in zip(posY, posYDR)], color=colorGraphs[color], alpha=0.2)

	plt.plot(posXA, posYA, c=colorGraphs[color+1], label = exps[color+1] + ' Average')
	plt.fill_between(posXA, [elemA + elemB for elemA, elemB in zip(posYA, posYDA)], [elemA - elemB for elemA, elemB in zip(posYA, posYDA)], color=colorGraphs[color+1], alpha=0.2)

	arq = arq + 1
	color += 1

plt.legend(loc="center right")
plt.ylabel('Reward per drive', fontsize=14)
plt.xlabel('Episode', fontsize=14)
plt.title('Learning: ' + title[tn])

plt.yticks(fontsize=14)
plt.xticks(fontsize=14)

plt.show()
