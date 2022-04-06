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
public class DriveExploreCodelet extends Codelet{
    //Homeostasia é level = 0. Se tiver abaixo é pq precisa explorar, se estiver acima é pq explorou mt  
    private double needLevel;
    private double curiosityRead;
    public static int homeostasia;
    
    private MemoryObject exploreLevelMO;
    
    
    public DriveExploreCodelet(){
        homeostasia = 0;
    }
    
    @Override
    public void accessMemoryObjects() {       
        MemoryObject MO = (MemoryObject) this.getInput("CURIOSITY");
        curiosityRead = (double) MO.getI();
        
        exploreLevelMO = (MemoryObject) this.getOutput("EXPLORE");
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
        
        needLevel = curiosityRead - homeostasia;
        //System.out.println("needExplore " + needLevel);
        exploreLevelMO.setI(needLevel);
        
        //printToFile(needLevel);
    }
    
    private void printToFile(double NeedExploreLevel){
        try(FileWriter fw = new FileWriter("NeedExploreLevel.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)){
            out.println(NeedExploreLevel);
            out.close();
        } catch (IOException e) {   }
        
    }  
}