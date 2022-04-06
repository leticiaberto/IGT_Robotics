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
public class TurnLeftCodelet extends Codelet{
    
    //List com as ações do MO resultantes do Learner Codelet
    private List actionsWinnerList = new ArrayList<ArrayList>();
    private String actionsList;
   
    
    private MemoryContainer behaviorMO;
    
    /*Flag para uso do container. Na primeira vez que seta o container precisa pegar seu ID de retorno.
    Nas próximas vezes o set tem que ser acompanhdo desse id*/
    private int indice = -1;
    
    //determina o nivel de ativação/importância do comportamento da classe no momento da decisão
    private double eval;
    
    private int tamActionMO = 0;
    
    private double speed = 4.0f;
        
    @Override
    public void accessMemoryObjects() {
        MemoryObject MO;
        
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
            Thread.sleep(100);//Estava 50
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
        eval = 0.0;
        
        if (!actionsWinnerList.isEmpty()){
            
            int sizeAct = actionsWinnerList.size();
            
            //Significa que o Learner colocou uma ação nova no MO. 
            /*Serve para dar prioridade ao learning, e também evitar comportamento indesejado se 
            esse codelet estiver mais rapido*/
            if(sizeAct != tamActionMO){
                tamActionMO = sizeAct;
                //Get the most recent action to take
                String actionToTake = (String) ((ArrayList)actionsWinnerList.get(actionsWinnerList.size()-1)).get(0); 

                if (actionToTake.equals("TurnLeft")){
                    eval = 1.0;  
                }
                JSONMessageToMO("TurnLeft");
            }
        }
    }//fim proc
    
     void JSONMessageToMO(String actionToTake){
        //System.out.println("Left " + eval);
        JSONObject message = new JSONObject();
        try {
            message.put("ACTION", actionToTake);
            message.put("LeftMO", -speed);
            message.put("RightMO", speed);

            if(indice == -1)
                indice = behaviorMO.setI(message.toString());
            else
                behaviorMO.setI(message.toString(), eval, indice);
        
        } catch (JSONException ex) {
            Logger.getLogger(MoveForwardsCodelet.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
}//fim classe