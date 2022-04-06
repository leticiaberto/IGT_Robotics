package igt_robotics;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import br.unicamp.cst.sensorial.SensorBufferCodelet;
import codelets.motor.MotorCodelet;
import codelets.sensors.SonarCodelet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import outsideCommunication.OutsideCommunication;
import outsideCommunication.SonarData;
import codelets.learner.LearnerCodelet;
import codelets.perceptuals.DriveExploreCodelet;
import codelets.perceptuals.DriveSurviveCodelet;
import codelets.sensors.GetCuriosityCodelet;
import codelets.sensors.GetEnergyCodelet;
import codelets.sensors.GetGroundTruthCodelet;
import codelets.behaviors.*;
import coppelia.FloatWA;


/**
 *
 * @author leticia
 */
public class AgentMind extends Mind {
    
    public static final int buffersize = 50;
    public static final int sonardimension = 8;
    private static final double NON_VALUE_NEEDS = 10000;
    
    public AgentMind(OutsideCommunication oc, String mode){
        
        super();
        
        //////////////////////////////////////////////
        //Declare Memory Objects
        //////////////////////////////////////////////
        
        /***********************Sensory MOs******************************/
        
        //Sonars
        SonarData sonarData = new SonarData();
        MemoryObject sonar_read = createMemoryObject("SONARS", sonarData);      
        
        double energy_data = NON_VALUE_NEEDS;//mesmo valor do NON_VALUE do Learner
        MemoryObject energyMO = createMemoryObject("ENERGY", energy_data);
        
        double curiosity_data = NON_VALUE_NEEDS;
        MemoryObject curiosityMO = createMemoryObject("CURIOSITY", curiosity_data);
        
        
        List groundT = Collections.synchronizedList(new ArrayList<ArrayList<FloatWA>>());
        MemoryObject groundTMO = createMemoryObject("GTMO",groundT); 
        
                
        //Sensor Buffers
        //Sonars
        List sonar_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(buffersize));
        MemoryObject sonar_bufferMO = createMemoryObject("SONAR_BUFFER",sonar_buffer_list);
        
        
        /***********************Perceptual MOs******************************/
        double surviveLevel_data = NON_VALUE_NEEDS;
        MemoryObject surviveLevelMO = createMemoryObject("SURVIVE", surviveLevel_data);
        
        double exploreLevel_data = NON_VALUE_NEEDS;
        MemoryObject exploreLevelMO = createMemoryObject("EXPLORE", exploreLevel_data);
        
        /***********************Learner MOs******************************/
        List actionsList = Collections.synchronizedList(new ArrayList<ArrayList>());
        MemoryObject actionsMO = createMemoryObject("ACTIONS",actionsList); 

        List statesList = Collections.synchronizedList(new ArrayList<String>());
        MemoryObject statesMO = createMemoryObject("STATES", statesList);
        
        
        /***********************Motor MOs******************************/ 
        //Container para Motor - Pois possuem mais de um comportamento
        MemoryContainer behaviorMO;//trocar nome depois
        behaviorMO = createMemoryContainer("BEHAVIOR");
        
        int time = 0;//mesmo valor do NON_VALUE do Learner
        MemoryObject timeMO = createMemoryObject("TIME", time);
//        
//        
//        ////////////////////////////////////////////
//        //Codelets
//        ////////////////////////////////////////////
////        
        
        /***********************Sensorial Codelets******************************/
        Codelet sonars = new SonarCodelet(oc.sonar);
        sonars.addOutput(sonar_read);
        insertCodelet(sonars);
        
        Codelet groundTruth = new GetGroundTruthCodelet(oc.pioneer_orientation, oc.pioneer_position, oc.sonar_orientations);
        groundTruth.addOutput(groundTMO);
        insertCodelet(groundTruth);
        
        GetCuriosityCodelet gc = new GetCuriosityCodelet();
        Codelet getCuriosity = gc;
        getCuriosity.addOutput(curiosityMO);
        getCuriosity.addInput(groundTMO);
        getCuriosity.addInput(timeMO);
        insertCodelet(getCuriosity);
        
        GetEnergyCodelet gen  = new GetEnergyCodelet(oc.rechargePositions);
        Codelet getEnergy = gen;
        getEnergy.addOutput(energyMO);
        getEnergy.addInput(groundTMO);
        getEnergy.addInput(timeMO);
        insertCodelet(getEnergy);
        
        //Sensor Buffers
        //Sonar
        Codelet sonar_buffer = new SensorBufferCodelet("SONARS", "SONAR_BUFFER", buffersize);
        sonar_buffer.addInput(sonar_read);
        sonar_buffer.addOutput(sonar_bufferMO);
        insertCodelet(sonar_buffer);
        
        
        /***********************Perceptual Codelets******************************/
        Codelet needSurvive = new DriveSurviveCodelet();
        needSurvive.addInput(energyMO);
        needSurvive.addOutput(surviveLevelMO);
        insertCodelet(needSurvive);
        
        Codelet needExplore = new DriveExploreCodelet();
        needExplore.addInput(curiosityMO);
        needExplore.addOutput(exploreLevelMO);
        insertCodelet(needExplore);
        
        
        /***********************Learner Codelets******************************/  
        Codelet newLearner_cod = new LearnerCodelet(oc, buffersize, sonardimension, mode, gen, gc, oc.rechargePositions);
        newLearner_cod.addInput(sonar_bufferMO);
        newLearner_cod.addInput(groundTMO);
        newLearner_cod.addInput(surviveLevelMO);
        newLearner_cod.addInput(exploreLevelMO);
        newLearner_cod.addInput(energyMO);
        newLearner_cod.addOutput(actionsMO);
        newLearner_cod.addOutput(statesMO);
        newLearner_cod.addOutput(behaviorMO);
        insertCodelet(newLearner_cod);
           
        /***********************Behavioral Codelets******************************/
        Codelet moveFs = new MoveForwardsCodelet();
        moveFs.addInput(actionsMO);
        moveFs.addOutput(behaviorMO);
        insertCodelet(moveFs);
        
        Codelet moveB = new MoveBackwardsCodelet();
        moveB.addInput(actionsMO);
        moveB.addOutput(behaviorMO);
        insertCodelet(moveB);
        
        Codelet turnRight = new TurnRightCodelet();
        turnRight.addInput(actionsMO);
        turnRight.addOutput(behaviorMO);
        insertCodelet(turnRight);
        
        Codelet turnLeft = new TurnLeftCodelet();
        turnLeft.addInput(actionsMO);
        turnLeft.addOutput(behaviorMO);
        insertCodelet(turnLeft);
        
        Codelet diagonalUpLeft = new MoveDiagonalUpLeftCodelet();
        diagonalUpLeft.addInput(actionsMO);
        diagonalUpLeft.addOutput(behaviorMO);
        insertCodelet(diagonalUpLeft);
        
        Codelet diagonalUpRight = new MoveDiagonalUpRightCodelet();
        diagonalUpRight.addInput(actionsMO);
        diagonalUpRight.addOutput(behaviorMO);
        insertCodelet(diagonalUpRight);
        
        Codelet stop = new StopCodelet();
        stop.addInput(actionsMO);
        stop.addOutput(behaviorMO);
        insertCodelet(stop);
        
        /***********************Motor Codelets******************************/
        Codelet motors = new MotorCodelet(oc.right_motor, oc.left_motor);
        motors.addInput(behaviorMO);
        motors.addOutput(timeMO);
        insertCodelet(motors);

        ///////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////
        
        // NOTE Sets the time interval between the readings
        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(200);
	
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
     
	// Start Cognitive Cycle
	start(); 
	
    }
}
