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
package codelets.perceptuals;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author leticia
 */
public class DriveSurviveCodelet extends Codelet{
    
    //Homeostasia é level = 0. Se tiver abaixo é pq precisa carregar, se estiver acima é pq passou a capacidade da bateria
    private double needLevel;
    private double energyRead;
    private int homeostasia = 0;
    
    private MemoryObject surviveLevelMO;
    
    
    public DriveSurviveCodelet(){
        
    }

    @Override
    public void accessMemoryObjects() {             
        MemoryObject MO = (MemoryObject) this.getInput("ENERGY");
        energyRead = (double) MO.getI();
        
        surviveLevelMO = (MemoryObject) this.getOutput("SURVIVE");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        //need tem que ser o quao longe eu to da homestasia (e nao o valor direto do sensor)
        needLevel = energyRead - homeostasia;
        //System.out.println("needSurvive" + needLevel);
        surviveLevelMO.setI(needLevel);
        //printToFile(needLevel);
    }
    
    private void printToFile(double NeedSurviveLevel){
        try(FileWriter fw = new FileWriter("NeedSurviveLevel.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(NeedSurviveLevel);
            out.close();
        } catch (IOException e) {   }
        
    } 
    
}