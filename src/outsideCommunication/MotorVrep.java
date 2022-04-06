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
 
package outsideCommunication;

import coppelia.remoteApi;
import CommunicationInterface.MotorI;

/**
 *
 * @author leandro
 */
public class MotorVrep implements MotorI {
    
    private float speed = 0;
    private remoteApi vrep;
    private int clientID;
    private int motor_handle;
    
    public MotorVrep(remoteApi rApi_, int clientid, int mot_han){
        vrep = rApi_;
        clientID = clientid;
        motor_handle = mot_han;        
    }
    
    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public int setSpeed(float speed) {
        this.speed = speed;
       if(! (vrep.simxSetJointTargetVelocity(clientID, motor_handle, speed, remoteApi.simx_opmode_oneshot) == remoteApi.simx_error_noerror)) {
    	   //System.out.println("oi");
           return 0;
       }
       else return 1;
        
    }
    
}
