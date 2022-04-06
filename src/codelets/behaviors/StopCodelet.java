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
package codelets.behaviors;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author leticia
 */
public class StopCodelet extends Codelet{
    //private List<String> actionsList;//List com as ações do MO resultantes do Learner Codelet
   
    private List actionsWinnerList = new ArrayList<ArrayList>();
    private String actionsList;
    
    private MemoryContainer behaviorMO;
    
    private int indice = -1;
    private double eval;
    
    private int tamActionMO = 0;
    
    @Override
    public void accessMemoryObjects() {
        MemoryObject MO;
        //MO = (MemoryObject) this.getInput("ACTIONS");
        //actionsList = (List) MO.getI();
        
        MO = (MemoryObject) this.getInput("ACTIONS");
        actionsWinnerList = (List) MO.getI();
        
        behaviorMO = (MemoryContainer)this.getOutput("BEHAVIOR");
    }

    @Override
    public void calculateActivation() {
    }

    @Override
    public void proc() {
        try {
            Thread.sleep(100);//Estava 100
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        eval = 0.0;
        //System.out.println("Antes Do nothing " + eval);
        if (!actionsWinnerList.isEmpty()){
            
            int sizeAct = actionsWinnerList.size();
            
            //Significa que o Learner colocou uma ação nova no MO. 
            /*Serve para dar prioridade ao learning, e também evitar comportamento indesejado se 
            esse codelet estiver mais rapido*/
            if(sizeAct != tamActionMO){
                tamActionMO = sizeAct;
                //Get the most recent action to take
                String actionToTake = (String) ((ArrayList)actionsWinnerList.get(actionsWinnerList.size()-1)).get(0); 
   
                if (actionToTake.equals("Stop")) {
                    eval = 1.0;  
                    //JSONMessageToMO(actionToTake);
                }
                JSONMessageToMO("Stop");
            }
        }
    }//fim proc
    
        void JSONMessageToMO(String actionToTake){
            //System.out.println("STOP" + eval);
            JSONObject message = new JSONObject();
            try {
                message.put("ACTION", actionToTake);
                message.put("LeftMO", 0f);
                message.put("RightMO", 0f);

                if(indice == -1)
                indice = behaviorMO.setI(message.toString());
            else
                behaviorMO.setI(message.toString(), eval, indice);

            } catch (JSONException ex) {
                Logger.getLogger(MoveForwardsCodelet.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
}