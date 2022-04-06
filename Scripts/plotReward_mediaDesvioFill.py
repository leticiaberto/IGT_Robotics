import numpy as np
import matplotlib.pyplot as plt
import math

i = 0

#This script plots the average and standard deviation (filled) of the rewards. The window choose is 10

#Numero do experimento, numero de ações, reward do episodio, reward global

tn = 0
title = ['Long-term decisions (γ = 0.9)', 'Short-term decisions (γ = 0.1)']
nameSave = ['Long-term decisions']

color = 0
colorGraphs = ['m','b','g']
exps = ['Long-term decisions']

arq = 1
fig, axs = plt.subplots(2, sharex=True, constrained_layout=True)#,gridspec_kw={'hspace': 0}
fig.suptitle('Learning: '+ title[tn], fontsize=16)
#fig, ax = plt.subplots(figsize=(12, 12))
while (arq < 2):
	i = 0
	with open('rewards0'+str(arq)+'.txt', 'r') as f:
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


	axs[0].plot(posX, posY, c=colorGraphs[color], label = exps[color] + ' Average')
	axs[0].fill_between(posX, [elemA + elemB for elemA, elemB in zip(posY, posYDR)], [elemA - elemB for elemA, elemB in zip(posY, posYDR)], color=colorGraphs[color], alpha=0.2)

	#axs[0].plot(posXDR, posYDR, label = 'Standard Deviation')


	axs[1].plot(posXA, posYA, c=colorGraphs[color+1], label = exps[color] + ' Average')
	axs[1].fill_between(posXA, [elemA + elemB for elemA, elemB in zip(posYA, posYDA)], [elemA - elemB for elemA, elemB in zip(posYA, posYDA)], color=colorGraphs[color+1], alpha=0.2)

	#axs[1].plot(posXDA, posYDA, label = 'Standard Deviation')

	arq = arq + 1
	color += 1
#leg = axs[0].legend(loc="upper left", bbox_to_anchor=[0, 1],
				# ncol=1, shadow=True, fancybox=True, fontsize = 'x-large')
#leg = axs[1].legend(loc="upper left", bbox_to_anchor=[0, 1],
                 #ncol=1, shadow=True, fancybox=True, fontsize = 'x-large')

axs[0].set_ylabel('Reward', fontsize=14)
axs[1].set_ylabel('Actions', fontsize=14)
axs[1].set_xlabel('Episode', fontsize=14)

plt.sca(axs[0])
plt.yticks(fontsize=14)

plt.sca(axs[1])
plt.yticks(fontsize=14)
plt.xticks(fontsize=14)

#plt.savefig('PLearning' + nameSave[tn] + '.png')
plt.show()
