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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import outsideCommunication.RechargeArea;
/**
 *
 * @author leticia
 */
public class GetEnergyCodelet extends Codelet{
    
    //Contem o nivel atual do need. Inicialmente vamos ter 21 niveis
    private int nivelDiscrNeeds = 10;
    private double energy;
    private List localization = Collections.synchronizedList(new ArrayList<ArrayList<FloatWA>>());
    
    private MemoryObject energyMO;
    
    //private OutsideCommunication oc;
    private RechargeArea allRechPos;//Contem o X1, Y1, X2, Y2 de cada estação de carregamento
    
    private ArrayList<FloatWA> rechargePos;
    private static int [] visitas;
    
    private static int [] numvisitsTotalEp;
        
    Random value = new Random(42);
    
    private int timeMO;
    private int lastTimeMO = -1;
    
    public GetEnergyCodelet(RechargeArea energyPositions){
        //Inicia sensor com algum valor aleatório
        //energy = 5;
        resetSensors();
        /*energy = value.nextInt(nivelDiscrNeeds);//gera numeros entre 0 e o nivel desejado (90, por exemplo)
        boolean negPos = value.nextBoolean();//se for 1, gera um numero negativo, senao positivo
        if(negPos)
            energy = -energy;*/
        
        //Pega posições das estações
        allRechPos = energyPositions;
        rechargePos = allRechPos.getRechargeAreasPosition();
        visitas = new int[rechargePos.size()];//vetor pra contar o numero de visitas de cada deck --inicia com zero em td
        numvisitsTotalEp = new int[rechargePos.size()];
    }
    

    @Override
    public void accessMemoryObjects() {      
        MemoryObject MO = (MemoryObject) this.getInput("GTMO");
        localization = (List) MO.getI();
        
        MO = (MemoryObject) this.getInput("TIME");
        timeMO = (int) MO.getI();
        
        energyMO = (MemoryObject) this.getOutput("ENERGY");
    }

    @Override
    public void calculateActivation() {
    }

    @Override
    public void proc() {
        //significa que robo atuou no ambiente, logo pode perder bateria
        if(lastTimeMO != timeMO)
        {
            //a cada ciclo perde um pouco de energia
            energy-=0.1;//se mudar aqui muda o calculo dos estados no learner (pq estamos contando de 1 em 1)

            //se tiver na posição de alguma estação de energia, recarrega
            checkIfAtStation();

            //System.out.println("Energy " + energy);
            //Atualiza valor no MO responsavel
            energyMO.setI(energy);

            //printToFile(energy);
            lastTimeMO = timeMO;
        }
        
    }
    
   
    //verifica se esta em alguma posição de carregamento
    public void checkIfAtStation(){
        if(!localization.isEmpty()){
            float [] position = ((FloatWA) localization.get(0)).getArray();
            int i = 0;

            for (FloatWA rechargePo : rechargePos) {
                float xIni, yIni, xFim, yFim;
                /*rechargePro tem as coordenadas do centro do chão. Então subtraimos a metade do cumprimento
                para encontrar o x e y iniciais*/
                //System.out.println("size: " + allRechPos.getSizeStation());
                xIni = rechargePo.getArray()[0] - (allRechPos.getSizeStation()/2);
                xFim = rechargePo.getArray()[0] + (allRechPos.getSizeStation()/2);
                yIni = rechargePo.getArray()[1] - (allRechPos.getSizeStation()/2);
                yFim = rechargePo.getArray()[1] + (allRechPos.getSizeStation()/2);

                //System.out.println("x: " + rechargePo.getArray()[0] + "y: " + rechargePo.getArray()[1]);
                //System.out.println("x1: " + xIni + "y1: " + yIni + "x2: " + xFim + "y2: " + yFim);
                if((xIni <= position[0] && position[0] <= xFim) && (yIni <= position[1] && position[1] <= yFim)) {
                    //System.out.println("ene: " + allRechPos.getEnergyRewardValue(i));
                    energy += allRechPos.getEnergyRewardValue(i) - allRechPos.getEnergyPunishmentValueAtPosition(i, visitas[i]);
                    visitas[i] += 1;
                    numvisitsTotalEp[i] +=1;//Total de visitas por episodio
                    //System.out.println("visit: " + visitas[i]);
                    if(visitas[i] == allRechPos.getMaxVisitNumber()){
                        visitas[i] = 0;
                        allRechPos.updatePunishmentsPositionOfDeck(i);
                        allRechPos.printPunishes();    
                    }
                    break;
                }
                i++;
            }
        }
    }
    
    public void resetVisits(){
        Arrays.fill(visitas, 0);
        //for(int i = 0; i < visitas.length; i++)
            //System.out.print(visitas[i] + " ");
    }
    
    public void resetSensors(){
        energy = value.nextInt(nivelDiscrNeeds);//gera numeros entre 0 e o nivel desejado (90, por exemplo)
        boolean negPos = value.nextBoolean();//se for 1, gera um numero negativo, senao positivo
        if(negPos && energy != 0)
            energy = -energy;
        printToFile(energy);
    }
    
    private void printToFile(double energyValue){
        try(FileWriter fw = new FileWriter("Energy.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(energyValue);
            out.close();
        } catch (IOException e) {   }
        
    }     

    public int[] getVisitsStation() {
        return numvisitsTotalEp;
    }

    public void resetAllVisitsDeck() {
        Arrays.fill(numvisitsTotalEp, 0);
    }
}