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
package codelets.sensors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import coppelia.FloatWA;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author leticia
 */
public class GetCuriosityCodelet extends Codelet{
    
    private static final int TAM_DISC_METROS = 1;
    //private static final double TAM_DISC_METROS = 0.5;
    
    //Contem o nivel atual do need. Inicialmente vamos ter 20 niveis
    private int nivelDiscrNeeds = 10;
    private int sizeX = 10;//tamanho em metros de X do ambiente
    private int sizeY = 10;//tamanho em metros de Y no ambiente
    private final double gamma = 0.9;
    private final double lambda = 0.9;
    private double curiosity;
    private List localization = Collections.synchronizedList(new ArrayList<ArrayList<FloatWA>>());
    
    private MemoryObject curiosityMO;
    
    Random value = new Random(23);
    
    Map<String, Double> visitasAreas = new HashMap<String, Double>();//armazena qntd de vezes que esteve naquela area
    
    private int timeMO;
    private int lastTimeMO = -1;
    
    public GetCuriosityCodelet(){
        //curiosity = 5;
        resetSensors();
        resetVisitsDiscMenor();
    }
    
    @Override
    public void accessMemoryObjects() {
        MemoryObject MO = (MemoryObject) this.getInput("GTMO");
        localization = (List) MO.getI();
        
        MO = (MemoryObject) this.getInput("TIME");
        timeMO = (int) MO.getI();
        
        curiosityMO = (MemoryObject) this.getOutput("CURIOSITY"); 
    }

    @Override
    public void calculateActivation() {
    }

    @Override
    public void proc() {
        //curiosity++;
        if(lastTimeMO != timeMO){
            checkIfAreaVisitedDiscMenor();
            //System.out.println("Curiosity " + curiosity);
            //Atualiza valor no MO responsavel
            curiosityMO.setI(curiosity);

           // printToFile(curiosity);
            //printToFileRegions(visitasAreas);
            lastTimeMO = timeMO;
        }    
    }
    
    public void resetSensors(){
        curiosity = value.nextInt(nivelDiscrNeeds);//gera numeros entre 0 e o nivel desejado (90, por exemplo)
        boolean negPos = value.nextBoolean();//se for 1, gera um numero negativo, senao positivo
        if(negPos && curiosity != 0)
            curiosity = -curiosity;
        printToFile(curiosity);
    }

    //Discretização a cada 1 metro -> total de 100 regiões
    public void resetVisitsDiscMenor(){
        //O ambiente tem tamanho sizeX, mas é do -sizeX ao +sizeX
        for(int i = -(sizeX/2); i < (sizeX/2); i++){
            for(int j = -(sizeY/2); j < (sizeY/2); j++){
                String r = Integer.toString(i);
                visitasAreas.put(r.concat(Integer.toString(j)), 0.0);
            }
        }
    }
    
    //Discretização a cada 0.5 metro -> total de 400 regiões
    public void resetVisits(){
        //O ambiente tem tamanho sizeX, mas é do -sizeX ao +sizeX
        for(int i = -sizeX+1; i < sizeX; i++){
            for(int j = -sizeY+1; j < sizeY; j++){
                String r = Integer.toString(i);
                visitasAreas.put(r.concat(Integer.toString(j)), 0.0);
            }
        }
        
        //Inserir as opções de -0 que não foram contabilizadas antes
        for(int i = -sizeX+1; i < sizeX; i++){
            String r = "-0";
            visitasAreas.put(r.concat(Integer.toString(i)), 0.0);
            r = Integer.toString(i);
            visitasAreas.put(r.concat("-0"), 0.0);
        }
        String r = "-0";//Inserir -0-0 que não tinha em nenhum antes
        visitasAreas.put(r.concat("-0"), 0.0);

    }

    //Discretização a cada 0.5 metro -> total de 400 regiões
    private void checkIfAreaVisited() {
        if (!localization.isEmpty()){
            float [] positionGT = ((FloatWA) localization.get(0)).getArray();

            System.out.println(positionGT[0]);
            System.out.println(positionGT[1]);
            
            double PosX = positionGT[0]/TAM_DISC_METROS; //Discretização da posição X
            double PosY = positionGT[1]/TAM_DISC_METROS; //Discretização da posição Y
        
            System.out.println(PosX);
            System.out.println(PosY);
            
            String keyToSearch;
            
            if(PosX < 0){
                //keyToSearch = "-";
                keyToSearch = Integer.toString((int)Math.ceil(PosX));
                //keyToSearch = keyToSearch.concat(Integer.toString((int)Math.ceil(PosX)));
            }
            else
                keyToSearch = Integer.toString((int)Math.floor(PosX));
        
            if(PosY < 0){
                //keyToSearch = keyToSearch.concat("-");
                keyToSearch = keyToSearch.concat(Integer.toString((int)Math.ceil(PosY)));
                //keyToSearch = keyToSearch.concat(Integer.toString((int)Math.ceil(PosY)));
            }
            else
                keyToSearch = keyToSearch.concat(Integer.toString((int)Math.floor(PosY)));

            System.out.println(keyToSearch);          
            //Atualiza o traço de elegibilidade para todas as regioes
            for (String key : visitasAreas.keySet()) {
                double value = visitasAreas.get(key);//Capturamos o valor a partir da chave
                if(key.equals(keyToSearch))//regiao em que o robo esta atualmente
                    visitasAreas.put(key, gamma*lambda*value + 0.01);//gamma*lambda*value + 1
                else
                    visitasAreas.put(key, gamma*lambda*value);
                
                //System.out.println(key + " = " + visitasAreas.get(key));
            }
            
            double sumRegionsCur = 0;
            //usa os niveis de todas as regioes para atualizar a curiosidade
            for (String key : visitasAreas.keySet()) {
                double value = visitasAreas.get(key);//Capturamos o valor a partir da chave
                //System.out.println(key + " = " + visitasAreas.get(key));
                if(key.equals(keyToSearch)){//regiao em que o robo esta atualmentes
                    sumRegionsCur -= value;
                    //System.out.println(key + " = " + visitasAreas.get(key));
                }
                else{
                    sumRegionsCur += value;
                }
            }
            
            System.out.println("sum " + sumRegionsCur);
            curiosity += sumRegionsCur;
        }
    }
    
        //Discretização a cada 1 metro -> total de 100 regiões
        private void checkIfAreaVisitedDiscMenor() {
        if (!localization.isEmpty()){
            float [] positionGT = ((FloatWA) localization.get(0)).getArray();

            int PosX = (int) positionGT[0]/1; //int PosX = (int) positionGT[0]/TAM_DISC_METROS -> Fica só com a parte inteira -- Discretização da posição X
            int PosY = (int) positionGT[1]/1; //Fica só com a parte inteira -- Discretização da posição Y
        
            String keyToSearch = Integer.toString(PosX);
            keyToSearch = keyToSearch.concat(Integer.toString(PosY));
            
                        
            //Atualiza o traço de elegibilidade para todas as regioes
            for (String key : visitasAreas.keySet()) {
                double value = visitasAreas.get(key);//Capturamos o valor a partir da chave
                if(key.equals(keyToSearch))//regiao em que o robo esta atualmente
                    visitasAreas.put(key, gamma*lambda*value + 1);//gamma*lambda*value + 1
                else
                    visitasAreas.put(key, gamma*lambda*value);
                
                //System.out.println(key + " = " + visitasAreas.get(key));
            }
            
            double sumRegionsCur = 0;
            //usa os niveis de todas as regioes para atualizar a curiosidade
            for (String key : visitasAreas.keySet()) {
                double value = visitasAreas.get(key);//Capturamos o valor a partir da chave
                if(key.equals(keyToSearch))//regiao em que o robo esta atualmentes
                    sumRegionsCur -= value;
                else
                    sumRegionsCur += value;
            }
            curiosity += sumRegionsCur;
        }
    }
    
    private void printToFile(double curiosityValue){
        try(FileWriter fw = new FileWriter("Curiosity.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(curiosityValue);
            out.close();
        } catch (IOException e) {   }
        
    }  
    //private void printToFileRegions(Object regionsCurValue)
    public void printToFileRegions(int episodio){
        try(FileWriter fw = new FileWriter("CuriosityRegions.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(episodio + " " + visitasAreas);
            out.close();
        } catch (IOException e) {   }
        
    }  
}   