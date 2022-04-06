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
package codelets.sensors;

import CommunicationInterface.SensorI;
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

/**
 *
 * @author leticia
 */

//Essa classe vai substituir o uso da OrientationVrep e PositionVrep
//Nela estam implementados todos os dados de GroundTruth necessários na aplicação

public class GetGroundTruthCodelet extends Codelet{
    private SensorI pioneer_orientation;
    private SensorI pioneer_position;
    private int n = 3;//Qntd de features de GroundTruth(gd) que serão armazenadas no MO
    
    public ArrayList<SensorI> sonar_orientations;
        
    private MemoryObject GroundTMO;
    
    private List gt = Collections.synchronizedList(new ArrayList<ArrayList<FloatWA>>());
   // private List gt2 = Collections.synchronizedList(new ArrayList<ArrayList<FloatWA>>());
    //private List<Float> laser_data; // exemplo
    
    public GetGroundTruthCodelet(SensorI orien, SensorI pos, ArrayList<SensorI> sonar_orient){
        pioneer_orientation = orien;
	pioneer_position = pos;
        sonar_orientations = sonar_orient;

        //Itializing
        for(int i = 0; i < n; i++)
            gt.add(0);
        
    }

    @Override
    public void accessMemoryObjects() {
        GroundTMO = (MemoryObject) this.getOutput("GTMO");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
       
      
       /*
        gt(0) = GroundTruth de posição do Pioneer (x, y and z)
        gt(1) = GroundTruth de orientação do Pioneer (Euler angles (alpha, beta and gamma))
        gt(2) = GroundTruth de orientação dos Sonares do Pioneer (8 sonares com 3 valores de orientação cada)
       */
       
        //Orientação de cada sonar (composta por um array de FloatWA(3)) é colocada no array de GT
        
        ArrayList<float[]> sonarOrientationData =  new ArrayList<>();
        
        for(int i = 0; i < sonar_orientations.size(); i++){
            sonarOrientationData.add(i,  ((FloatWA) (sonar_orientations.get(i)).getData()).getArray());
        
        }
        
       gt.set(0, pioneer_position.getData());
       gt.set(1, pioneer_orientation.getData());
       gt.set(2, sonarOrientationData);      
        
        //Validação de corretude dos dados que vão no MO de GroundTruth
        /*System.out.println("gt direto pelo OutsideCommunication - position");
        float [] position2 = ((FloatWA) pioneer_position.getData()).getArray();
        for(int i = 0; i < position2.length; i++)
        System.out.println(position2[i]);
        System.out.println("gt List - position");
        float [] position = ((FloatWA) gt.get(0)).getArray();
        for(int i = 0; i < position.length; i++)
        System.out.println(position[i]);
        */
        //System.out.println("gt direto pelo OutsideCommunication - orientation");
        //float[] orientation = ((FloatWA) pioneer_orientation.getData()).getArray();
        //for(int i = 0; i < orientation.length; i++)
        //System.out.println(orientation[2]);
        
        //System.out.println("gt List - orientation");
        //float [] orientation2 = ((FloatWA) gt.get(1)).getArray();
        //for(int i = 0; i < orientation2.length; i++)
        //System.out.println(orientation2[2]);
         

        /*
        System.out.println("Sonar OutsideCommunication");//Pegando a leitura do sonar 0
        float [] sonar = ((FloatWA) (sonar_orientations.get(0)).getData()).getArray();
        for(int i = 0; i < sonar.length; i++)
            System.out.println(sonar[i]);
        
        
        System.out.println("Sonar GT");//Pegando a leitura do sonar 0
        float[] sonarOrientation = ((ArrayList<float[]>) gt.get(2)).get(0);
        for(int i = 0; i < sonarOrientation.length; i++)
            System.out.println(sonarOrientation[i]);
       */
        
        GroundTMO.setI(gt);
       
        //printToFilePosition(((FloatWA) gt.get(0)));

    }
    
        private void printToFilePosition(FloatWA position) {
        try(FileWriter fw = new FileWriter("GroundTruthPosition.txt",true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(position.getArray()[0] + " " + position.getArray()[1]);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
