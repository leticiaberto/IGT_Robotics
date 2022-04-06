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
 
package igt_robotics;

import outsideCommunication.OutsideCommunication;

import java.io.File;


/**
 *
 * @author leandro
 */
public class IGT_Robotics {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	// removing previous .txt files
    	File folder = new File(".");
    	for (File f : folder.listFiles()) {
    		if(f.getName().endsWith(".txt") && !(f.getName().endsWith("QTable.txt"))) {
    			f.delete();
    		}
    	}
        OutsideCommunication oc = new OutsideCommunication();
        oc.start();
        AgentMind am = new AgentMind(oc, "learning");//exploring; learning
       
        
    }
    
}
