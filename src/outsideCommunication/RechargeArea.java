/*
 * Copyright (C) 2021 leticia
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
package outsideCommunication;

import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 *
 * @author leticia
 */
public class RechargeArea {
    
    private remoteApi vrep;
    private int clientID;
    public static int nAreas = 4;
    private IntW[] rechargeArea_handle;
    private ArrayList<FloatWA> rechargeAreaPositions;
    private float sizeStation = 1;//Tamanho da area no simulador
    
    Random value;
    private int qntdPunish = 5;//Qnts vezes ai punir a cada x conjunto de acessos
    private int reset = 10;//a cada 10 visitas ao mesmo deck reinicia
    private double[] greaterPunishmentsAB = {1.5, 3.0, 2.0, 2.5, 3.5};//Deck A = {150, 300, 200, 250, 350}
    private double[] smallerPunishmentsCD = {0.25, 0.75, 0.25, 0.75, 0.50};//Deck C = {25, 75, 25, 75, 50}
    private double rewardAB = 1.0;//100--1.0
    private double rewardCD = 0.5;//50--0.5
    private ArrayList<double[]> applyPunish;
    
    public RechargeArea (int clientID, remoteApi vrep) {
        this.vrep = vrep;
        this.clientID = clientID;
        rechargeArea_handle = new IntW[nAreas];
        rechargeAreaPositions = new ArrayList<>();
        setRechargeAreasPosition();
        
        value = new Random(42);
        
        applyPunish = new ArrayList<>();
        double[] dummy = null;
        for(int i = 0; i < nAreas; i++){
            applyPunish.add(i, dummy);
            updatePunishmentsPositionOfDeck(i);
        }
        //printPunishes();  
    }

    //pega posições das areas de caregamento presentes no simulador
    private void setRechargeAreasPosition() {
        //Get the position of recharge areas
        char areaName = 'A';
        for (int i = 0; i < nAreas; i++) {
            FloatWA position = new FloatWA(3);
          
            rechargeArea_handle[i] = new IntW(-1);

            String s = "Energy" + (areaName);
            areaName++;
            vrep.simxGetObjectHandle(clientID, s, rechargeArea_handle[i], remoteApi.simx_opmode_blocking);
            //System.out.println(position);
            vrep.simxGetObjectPosition(clientID, rechargeArea_handle[i].getValue(), -1, position, vrep.simx_opmode_blocking);
            
            rechargeAreaPositions.add(position);
        }
    }
    
    public ArrayList getRechargeAreasPosition(){
        return rechargeAreaPositions;
    }
    
    public float getSizeStation() {
        return sizeStation;
    }
        
    //Punições sao aplicadas em valores menores várias vezes
    private void setEnergyPunishmentsMultipleTimesAleatorio(int pos, double[] valuesToPunish){
        double[] punishments = new double[10];
        int x, i = 0;
        
        Integer[] sorteados = new Integer[5];
        Arrays.fill(sorteados, -1);
        
        //Preenche a ordem de visita que recebe a punição de forma aleatoria
        while(i < qntdPunish){
            x = value.nextInt(reset);
            //System.out.print(x + " ");
            boolean contains = (Arrays.asList(sorteados)).contains(x);
           // System.out.println(contains);
            if(!contains){
                punishments[x] = valuesToPunish[i];
                sorteados[i] = x;
                i++;  
            }      
        }   
        applyPunish.set(pos,punishments);
    }

     //Punições sao aplicadas em valor maior uma unica vez
    private void setEnergyPunishmentsOneTimeAleatorio(int pos, double[] valuesToPunish) {
        double[] punishments = new double[10];
        int x;

        x = value.nextInt(reset);
        punishments[x] = DoubleStream.of(valuesToPunish).sum();   
        
        applyPunish.set(pos,punishments);
    }
    
    
    //Ao atingir o numero estipulado visitas (10 por exemplo) muda a ordem de aplicar os punishments para esse deck
    public void updatePunishmentsPositionOfDeck(int deck){
        switch (deck) {
            case 0://Deck A
                setEnergyPunishmentsMultipleTimesAleatorio(deck, greaterPunishmentsAB);
                break;
            case 1://Deck B
                setEnergyPunishmentsOneTimeAleatorio(deck, greaterPunishmentsAB);
                break;
            case 2://Deck C
                setEnergyPunishmentsMultipleTimesAleatorio(deck, smallerPunishmentsCD);
                break;
            case 3://Deck D
                setEnergyPunishmentsOneTimeAleatorio(deck, smallerPunishmentsCD);
            default:
                break;
        }
    }
    
    //Retorna o valor da punição do deck informado na visita informada (ex do artigo de IGT original)
    public double getEnergyPunishmentValueAtPosition(int deck, int visitaNumber){
        return applyPunish.get(deck)[visitaNumber];
    }
    
    //Retorna o valor de reward para cada deck
    public double getEnergyRewardValue(int deck){
        if(deck == 0 || deck == 1)
            return rewardAB;
        else
            return rewardCD;
    }

    public void printPunishes(){
        for(int i = 0; i < nAreas; i++){
            double[] print = applyPunish.get(i);
            for(int j = 0; j < reset; j++)
                System.out.print(print[j] + " ");
            System.out.println();
        }
    }

    public int getMaxVisitNumber() {
        return reset;
    }

    public void resetPunishesAndVisitsEnergy() {
        for(int i = 0; i < nAreas; i++)
            updatePunishmentsPositionOfDeck(i);    
    }
    
    //Punições sao aplicadas em valores menores várias vezes
    private void setEnergyPunishmentsMultipleTimes(int pos, double[] valuesToPunish){
        
        double[] punishments = new double[10];
        int[] x = {2, 4, 6, 8, 9};

        for(int i = 0; i < qntdPunish; i++)
            punishments[x[i]] = valuesToPunish[i];   
        
        applyPunish.set(pos,punishments);
    }

     //Punições sao aplicadas em valor maior uma unica vez
    private void setEnergyPunishmentsOneTime(int pos, double[] valuesToPunish) {
        double[] punishments = new double[10];
        int x = 8;

        //x = value.nextInt(reset);
        punishments[x] = DoubleStream.of(valuesToPunish).sum();   
        
        applyPunish.set(pos,punishments);
    }

}