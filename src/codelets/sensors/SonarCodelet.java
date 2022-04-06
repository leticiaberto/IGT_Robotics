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
 
package codelets.sensors;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import codelets.motor.Lock;
import outsideCommunication.SonarData;

/**
 *
 * @author leandro
 */
public class SonarCodelet extends Codelet {
    private MemoryObject sonar_read;
    private SensorI sonar;
    
    public SonarCodelet(SensorI sonar){
        this.sonar = sonar;
    }

    @Override
    public void accessMemoryObjects() {
        sonar_read = (MemoryObject) this.getOutput("SONARS");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        SonarData sonarData = (SonarData) sonar.getData();

        sonar_read.setI(sonarData);
        
//         System.out.println(sonar_read.toString());
        SonarData a = (SonarData) sonar_read.getI();
//         System.out.println("Sonar detect state = "+a.detect_state.toString());
//         System.out.println("Sonar value = "+a.sonar_readings);
    }
    
}
