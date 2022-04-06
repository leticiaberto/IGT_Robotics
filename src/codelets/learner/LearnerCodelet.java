/*
 * Copyright (C) 2019 leticia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codelets.learner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import coppelia.FloatWA;
import br.unicamp.cst.learning.QLearning;
import outsideCommunication.OutsideCommunication;
import codelets.sensors.*;
import java.util.Random;
import outsideCommunication.RechargeArea;
/**
 *
 * @author leticia
 */
public class LearnerCodelet extends Codelet{
    
    private int time_graph;
    private static float CRASH_TRESHOLD = 0.09f;

    private static int MAX_ACTION_NUMBER = 700;

    private static int MAX_EXPERIMENTS_NUMBER = 1000;
    
    private static final int LEVEL_NO_BATTERY = -50;

    private static final int NON_VALUE_NEEDS = 10000;
    
    private static final int NIVEL_DISC_NEEDS = 10;//valor maximo que pode atingir
    private static final int TOTAL_NEED_SURVIVE = NIVEL_DISC_NEEDS * 2 + 1; //21: de -10  à + 10
    private static final int TOTAL_NEED_EXPLORE = NIVEL_DISC_NEEDS * 2 + 1; //21
    private static final int TOTAL_X = 10;//considerando ambiente de 10m discretizado de 1 em 1m
    private static final int TOTAL_Y = 10;
    private static final int TAM_DISC_METROS = 1;
    private static final int TOTAL_GRAUS = 8;//discretização com 45 graus
    private static final int TAM_DISC_GRAUS = 45;
    
    private QLearning ql;
    

    private List sonar_bufferMO;
    
    //ActionsList é um MO de Entrada e Saída, logo é necessário atualiza-lo para os demais codelets usarem
    private MemoryObject actionsListMO;
    
    /*Alterações Leticia*/
    private List gt = Collections.synchronizedList(new ArrayList<ArrayList<FloatWA>>());
    
    private List actionsWinnerList = new ArrayList<ArrayList>();
    
    private List<String> actionsList; 
    private List<String> statesList;
    
    private OutsideCommunication oc;
    private int timeWindow;
    private int sensorDimension;
   
    
    private double global_reward;
    private int action_number;
    private int experiment_number;
    
    private String mode;
    private int n = 3;//Qntd de features de GroundTruth(gd) que serão armazenadas no MO
    private double episodeReward;
    private double episodeRewardEnergy;
    private double episodeRewardCuriosity;
    
    private double needSurvive;
    private double needExplore;
    private double energyRead;
    
    private GetEnergyCodelet energyCodelet;
    private GetCuriosityCodelet curiosityCodelet;
    private RechargeArea rechargeCode;
    
    private Random value = new Random(42);
    
    public LearnerCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode, GetEnergyCodelet en, GetCuriosityCodelet cur, RechargeArea ra) {
        super();
        time_graph = 0;

        global_reward = 0;

        action_number = 0;

        experiment_number = 1;
        
        episodeReward = 0;
        episodeRewardEnergy = 0;
        episodeRewardCuriosity = 0;
        
        //Itializing
        for(int i = 0; i < n; i++)
            gt.add(0);
        
        needExplore = NON_VALUE_NEEDS;
        needSurvive = NON_VALUE_NEEDS;
        
        //Se mudar as strings de ações tem que mudar nos codelets de behavior também    
       ArrayList<String> allActionsList  = new ArrayList<>(Arrays.asList("MoveBackwards", "MoveFowards", "TurnLeft", "TurnRight", "Stop", "DiagonalUpLeft", "DiagonalUpRight")); 

       ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, TOTAL_NEED_SURVIVE * TOTAL_NEED_EXPLORE * TOTAL_X * TOTAL_Y * TOTAL_GRAUS).mapToObj(String::valueOf).toArray(String[]::new)));
       
        // QLearning initialization
        ql = new QLearning();
        ql.setActionsList(allActionsList);
        // learning mode ---> build q table from scratch
        if (mode.equals("learning")) {
        // Initialize QTable to 0
            for (int i=0; i < allStatesList.size(); i ++) {
                for (int j=0; j < allActionsList.size(); j++) {
                    ql.setQ(0, allStatesList.get(i), allActionsList.get(j));

                }
            }
        }
        // exploring mode ---> reloads q table 
        else {
            try {
                ql.recoverQ();
            }
            catch (Exception e) {
                System.out.println("ERROR LOADING QTABLE");
                System.exit(1);
            }
        }


        oc = outc;
        timeWindow = tWindow;
        sensorDimension = sensDim;
        this.mode = mode;
        
        energyCodelet = en;
        curiosityCodelet = cur;
        rechargeCode = ra;
    }

    @Override
    public void accessMemoryObjects() {

        MemoryObject MO;
                
        MO = (MemoryObject) this.getInput("GTMO");
        gt = (List) MO.getI();

        MO = (MemoryObject) this.getInput("SONAR_BUFFER");
        sonar_bufferMO = (List)MO.getI();
        
        MO = (MemoryObject) this.getInput("EXPLORE");
        needExplore = (double) MO.getI();
        
        MO = (MemoryObject) this.getInput("SURVIVE");
        needSurvive = (double) MO.getI();
        
        MO = (MemoryObject) this.getInput("ENERGY");
        energyRead = (double) MO.getI();
        
        //Verificar se a posição e orientação de GroundTruth estão vindo corretas do MO de GT
        /*System.out.println("gt List Learner - position");
        float [] position = ((FloatWA) gt.get(0)).getArray();
        for(int i = 0; i < position.length; i++)
            System.out.println(position[i]);
        
        System.out.println("gt List Learner - orientation");
        float [] orientation2 = ((FloatWA) gt.get(1)).getArray();
        for(int i = 0; i < orientation2.length; i++)
            System.out.println(orientation2[i]);*/
        

        MO = (MemoryObject) this.getOutput("STATES");
        statesList = (List) MO.getI();

        
        actionsListMO = (MemoryObject) this.getOutput("ACTIONS");


    }

    @Override
    public void calculateActivation() {
            // TODO Auto-generated method stub

    }

    public static Object getLast(List list) {
            if (!list.isEmpty()) {
                    return list.get(list.size()-1);
            }
            return null;
    }

	
	
    @Override
    public void proc() {

        try {
            Thread.sleep(90);//Estava 90
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
                
        String state = "-1";

        if (needExplore != NON_VALUE_NEEDS && needSurvive != NON_VALUE_NEEDS){             
            double needECur = checkLimitsNeeds(needSurvive);
            double needExpl = checkLimitsNeeds(needExplore);
            
            if (!actionsWinnerList.isEmpty() && mode.equals("learning")) {
                // Find reward of the current state, given previous  winner 
                check_stop_experiment(mode);
                Double rewardTotal, rewardEnergy, rewardCuriosity;
                rewardTotal = 0.0;
                rewardCuriosity = 0.0;
                rewardEnergy  = 0.0;
                
                // Gets last action taken
                String lastAction = (String) ((ArrayList)actionsWinnerList.get(actionsWinnerList.size()-1)).get(0);
                
                // Gets last state that was in
                String lastState = statesList.get(statesList.size() - 1);
                
                /**************************Need de curiosidade***************************/
                double lastNeedExplore = (double) ((ArrayList)actionsWinnerList.get(actionsWinnerList.size()-1)).get(1);
                
                double difNeedExplore = Math.abs(lastNeedExplore - needExpl);//qnt mudou no need

                if(needExpl == 0)//atingiu homeostasia, ganha reward maxima
                    rewardCuriosity += 100.0;
                else
                    rewardCuriosity += difNeedExplore/needExplore;//Proporcional ao que mudou no need / o quão perto ficou da homestasia

                /**************************Need de energia***************************/
                double lastNeedSurvive = (double) ((ArrayList)actionsWinnerList.get(actionsWinnerList.size()-1)).get(2);
                
                double difNeedSurvive = Math.abs(lastNeedSurvive - needECur);//qnt mudou no need
                
                if(needECur == 0)//atingiu homeostasia, ganha reward maxima
                    rewardEnergy += 100.0;
                else
                    rewardEnergy += difNeedSurvive/needSurvive;//Proporcional ao que mudou no need / o quão perto ficou da homestasia
                
                rewardTotal = rewardCuriosity + rewardEnergy;
                
                global_reward += rewardTotal;
                episodeReward += rewardTotal;
                
                episodeRewardEnergy += rewardEnergy;
                episodeRewardCuriosity += rewardCuriosity;
                
                //System.out.println("Global Reward: "+global_reward);
                //System.out.println("Episode Reward: "+episodeReward);
                
                // Updates QLearning table
                ql.update(lastState, lastAction, rewardTotal);
                
                //printVarsState(lastNeedExplore, needExpl, difNeedExplore, needExplore, lastNeedSurvive, needECur, difNeedSurvive, needSurvive, reward);
            }

            state = getState();
            
            statesList.add(state);


            // Selects new best action to take
            String actionToTake = ql.getAction(state);
       
            action_number++;
            
            /*
                Contem a ação ATUAL vencedora e niveis dos needs
                actionWinnerActual(0) = ação escolhida
                actionWinnerActual(1) = nivel do need Explore(curiosidade)
                actionWinnerActual(2) = nivel do need Survive(Bateria)
            */
            ArrayList actionWinnerActual = new ArrayList();
            actionWinnerActual.add(actionToTake);  
            actionWinnerActual.add(needExpl);//needExplore
            actionWinnerActual.add(needECur);//needSurvive
            
            //Ações e needs ao longo do tempo
            actionsWinnerList.add(actionWinnerActual);
            
            actionsListMO.setI(actionsWinnerList);
            
                        
            //System.out.println("Action LEARNER: "+actionToTake);
                      
            //printToFileActionExploring(actionToTake);
            
        }
        time_graph = printToFile(0, state, "states.txt", time_graph,0, true);
        
        
        if (mode.equals("exploring")) {
            check_stop_experiment(mode);           
        }

    }
	
	
    public void check_stop_experiment(String mode) {

        boolean crashed = false;
        boolean noBattery = false;
        
        //Bateria mt desacarregada é estado terminal
        if(energyRead <= LEVEL_NO_BATTERY)
                noBattery = true;
        
        /*Por enquanto nao vamos considerar bater na parede como terminal
         if (!sonar_bufferMO.isEmpty()){
            MemoryObject sonarDataMO;
            sonarDataMO = (MemoryObject) sonar_bufferMO.get(sonar_bufferMO.size() -1);
             
             SonarData sonarData1;
             sonarData1 =  (SonarData) sonarDataMO.getI();
             
             Float f1;
            
            // check if crashed anything
            for (int i=0; i < sensorDimension; i++) {
                f1 = sonarData1.sonar_readings.get(i);
                if (f1 != 0.0f & f1 < CRASH_TRESHOLD) {
                    System.out.println("Crashed something!");
                    crashed = true;
                    break;
                } 
            }
        }*/
        if (mode.equals("exploring") && (crashed || noBattery)) {
            //Test to see if prints the robots position closer to obstacle
           /* if(!gt.isEmpty()){
                FloatWA positionGT = ((FloatWA) gt.get(0));
                printToFilePosition(positionGT, "positionsGroundTruth.txt");
            }*/
            printToFileActionsExploring("exploringActionsNumber.txt");
            //oc.stopSimulation();
            oc.finishSimulation();
            System.exit(0);
        }
        else if (mode.equals("learning") && (action_number >= MAX_ACTION_NUMBER || crashed || noBattery)) {
                System.out.println("Max number of actions or crashed or no Battery");
                printenergyStationsVisit(energyCodelet.getVisitsStation());
                curiosityCodelet.printToFileRegions(experiment_number);
                experiment_number = printToFile(episodeReward, global_reward, "rewards.txt", experiment_number, action_number,false);
                printRewardPerDrive(episodeRewardCuriosity, episodeRewardEnergy);
                action_number = 0;
                episodeReward = 0;  
                episodeRewardCuriosity = 0;
                episodeRewardEnergy = 0;
                System.out.println("Experiment Number: "+experiment_number);
                if (experiment_number > MAX_EXPERIMENTS_NUMBER) {
                        ql.storeQ();
                        oc.finishSimulation();
                        System.exit(0);
                }
                
                oc.pioneer_position.resetData();//reinicia com robô em outra posição
                energyCodelet.resetVisits();//reinicia visitas de cada deck
                energyCodelet.resetSensors();//reinicia valor do sensor de energia
                energyCodelet.resetAllVisitsDeck();//reinicia as visitas de cada estação por episodio
                //Vou usar a ultima escala de punishments mesmo -- entao nao precisa resetar
                //rechargeCode.resetPunishesAndVisitsEnergy();//reinicia escala de punishments de cada deck
                
                curiosityCodelet.resetVisitsDiscMenor();//reinicia visitas de cada area
                curiosityCodelet.resetSensors();//reinicia valor do sensor de curiosidade
                
                
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
        }
    }


    public String getState() {
        if (!gt.isEmpty()){
            float [] positionGT = ((FloatWA) gt.get(0)).getArray();
            float [] orientationGT = ((FloatWA) gt.get(1)).getArray();

            int PosX = (int) positionGT[0]/TAM_DISC_METROS; //Fica só com a parte inteira -- Discretização da posição X
            int PosY = (int) positionGT[1]/TAM_DISC_METROS; //Fica só com a parte inteira -- Discretização da posição Y
            int OrientZ = (int) orientationGT[2]/TAM_DISC_GRAUS; //Discretização da orientação Z

            //Apesar do sensor poder medir além do utilizado, desconsideramos esses valores aqui
            int needExploreDisc = checkLimitsNeeds(needExplore);
            int needSurviveDisc = checkLimitsNeeds(needSurvive);
 
            long estado = needExploreDisc 
                   + ((needSurviveDisc < 0)? (long)Math.pow(needSurviveDisc, 2)*-1:(long)Math.pow(needSurviveDisc, 2))
                   + (long)Math.pow(PosX,3)  
                   + ((PosY < 0)? (long)Math.pow(PosY,4)*-1: (long)Math.pow(PosY,4))
                   + (long)Math.pow(OrientZ,5);
            //System.out.println(needExplore + " " + needSurvive + " " + PosX + " " + PosY + " " + OrientZ);
            return Long.toString(estado);
        }
        return "0";
    }
		
	
    private int printToFile(double episodeReward, Object object,String filename, int counter, int action_number, boolean check){

        if (!check || counter < MAX_EXPERIMENTS_NUMBER) {
            try(FileWriter fw = new FileWriter(filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                if(filename.equals("rewards.txt"))
                    out.println(counter+" "+ action_number + " " + episodeReward + " " +object );
                else
                    out.println(counter+" "+object );

                out.close();
                return ++counter;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return counter;

    }
    
    private void printToFilePosition(FloatWA position,String filename) {
        try(FileWriter fw = new FileWriter(filename,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(position.getArray()[0] + " " + position.getArray()[1]);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void printToFileActionsExploring(String filename) {
        try(FileWriter fw = new FileWriter(filename,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
            {
                out.println(action_number);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void printToFileActionExploring(String actionToTake) {
        try(FileWriter fw = new FileWriter("actionTook.txt",true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
            {
                out.println(actionToTake);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    
    private int checkLimitsNeeds(double need) {
        if(need > NIVEL_DISC_NEEDS)
            return NIVEL_DISC_NEEDS;
        else if (need < -NIVEL_DISC_NEEDS)
            return -NIVEL_DISC_NEEDS;
        return (int)need;
    }
    
    private void printVarsState(double lastNeedExplore, double needExpl, double difNeedExplore, double farFromHomeostasisEnergy, double lastNeedSurvive, double needECur, double difNeedSurvive, double farFromHomeostasisSurvive, double reward){
        try(FileWriter fw = new FileWriter("varsToState.txt",true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
            {
                out.println(lastNeedExplore + " " + needExpl + " " +difNeedExplore + " " +farFromHomeostasisEnergy + " " +lastNeedSurvive + " " +needECur + " " + difNeedSurvive + " " + farFromHomeostasisSurvive + " " + reward);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    
    private void printenergyStationsVisit(int [] decks){
        try(FileWriter fw = new FileWriter("energyVisitsTotal.txt",true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
            {
                out.println(experiment_number + " " + decks[0] + " " + decks[1]  + " " + decks[2] + " " + decks[3]);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void printRewardPerDrive(double episodeRewardCuriosity, double episodeRewardEnergy) {
        try(FileWriter fw = new FileWriter("rewardsPerDrive.txt",true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
            {
                out.println(episodeRewardCuriosity + " " + episodeRewardEnergy);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

}

