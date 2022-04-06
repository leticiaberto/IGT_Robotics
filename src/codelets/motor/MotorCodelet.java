/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
    
package codelets.motor;

/**
 *
 * @author leticia
 */

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import CommunicationInterface.MotorI;
import br.unicamp.cst.core.entities.MemoryContainer;
import org.json.JSONException;
import org.json.JSONObject;

public class MotorCodelet extends Codelet {
    
    private MemoryObject motorActionMO;
    private MemoryObject rm_speed_MO, lm_speed_MO; 
    private MotorI rm, lm;
   
    private MemoryContainer actionMO;
    
    private int MOVEMENT_TIME = 2000; // 2 seconds
    
    private MemoryObject timeMO;
    private int time;
    
    public MotorCodelet(MotorI rmo, MotorI lmo){
    	super();
        rm = rmo;
        lm = lmo;
        time = 0;
    }

    @Override
    public void accessMemoryObjects() {
        //motorActionMO = (MemoryObject) this.getInput("MOTOR");
        //rm_speed_MO = (MemoryObject) this.getInput("R_M_SPEED");
        //lm_speed_MO = (MemoryObject) this.getInput("L_M_SPEED");
        
        actionMO = (MemoryContainer)this.getInput("BEHAVIOR");
        
        timeMO = (MemoryObject) this.getOutput("TIME");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
    	try {
            Thread.sleep(110);//Estava 100. O tempo esta em milisegundos (2000 = 2s)
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    	/* Versão da Caroline - utilizando LeftMO e RightMO que eram setados nos LearnerCodelet
    	String action = (String) motorActionMO.getI();
    	rm.setSpeed((float) rm_speed_MO.getI());
        lm.setSpeed((float) lm_speed_MO.getI());
    	*/
        
        //Utlizando a mensagem em JSON lida do Container para realizar a ação
        String comm = (String) actionMO.getI();
        if (comm == null) comm = "";
		
        if(!comm.equals("") ){

            try {
                JSONObject command = new JSONObject(comm);
                if (command.has("ACTION")) {
                    
                    String action = command.getString("ACTION");
                    
                    double leftSpeed = command.getDouble("LeftMO");
                    double rightSpeed = command.getDouble("RightMO");
                    //System.out.println("Action MOTOR: " + action); 
                    //leftSpeed = 4;
                    //rightSpeed = 4;
                    rm.setSpeed((float) rightSpeed);
                    lm.setSpeed((float) leftSpeed);
                    /*try {
                        Thread.sleep(110);//Estava 100. O tempo esta em milisegundos (2000 = 2s)
                    } catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }*/
                    timeMO.setI(time);
                    time++;
                    //System.out.println("Motor: " + time);
                }	
            } catch (JSONException e) {e.printStackTrace();}
        }
        
    }//fim proc
}
