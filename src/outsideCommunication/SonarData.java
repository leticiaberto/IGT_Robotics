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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author leandro
 */
public class SonarData implements Serializable {
    public List<Float> sonar_readings;
    public List<Boolean> detect_state;
    
    public SonarData(){
        sonar_readings = Collections.synchronizedList(new ArrayList<>(8));
        detect_state = Collections.synchronizedList(new ArrayList<>(8));
        
        for (int i = 0; i < 8; i++) {
            sonar_readings.add(Float.NaN);
            detect_state.add(Boolean.FALSE);
        }
    }
    
    /*
    @Override
    public Object clone(){
        SonarData sonarData = null;
        try {
            return (SonarData) super.clone();
        } catch (CloneNotSupportedException ex) {
            sonarData = new SonarData();
            sonarData.detect_state = this.detect_state;
            sonarData.sonar_readings = this.sonar_readings;
            Logger.getLogger(SonarData.class.getName()).log(Level.SEVERE, null, ex);
            return sonarData;
        }
    }*/
}
