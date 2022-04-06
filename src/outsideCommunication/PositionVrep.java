package outsideCommunication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import CommunicationInterface.SensorI;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;
import java.util.Random;

public class PositionVrep implements SensorI {
	
    private remoteApi vrep;
    private int clientID;
    private FloatWA position;
    private IntW handle;
    
    private IntW leftWheel;
    private IntW rightWheel;
    
    private FloatWA leftWheellInitialPos = new FloatWA(3);
    private FloatWA rightWheellInitialPos = new FloatWA(3);
    
    
    Random generator = new Random(42);
    
	public PositionVrep (int clientID, IntW handle, remoteApi vrep, IntW leftWheel, IntW rightWheel) {
        this.handle = handle;

        this.vrep = vrep;
        this.clientID = clientID;
        this.position = new FloatWA(3);
        
        this.leftWheel = leftWheel;
        this.rightWheel = rightWheel;
        
        
        float [] leftWheellInitialPosValues = leftWheellInitialPos.getArray();
        leftWheellInitialPosValues[0] = 1.0252e-5f;
        leftWheellInitialPosValues[1] = 8.7857e-5f;
        leftWheellInitialPosValues[2] = -2.0862e-7f;
        
        
        float [] rightWheellInitialPosValues = rightWheellInitialPos.getArray();
        rightWheellInitialPosValues[0] = +9.2983e-6f;
        rightWheellInitialPosValues[1] = +9.2983e-5f;
        rightWheellInitialPosValues[2] = +1.1325e-6f;
        
	}

	@Override
	public Object getData() {
		FloatWA position = new FloatWA(3);
		vrep.simxGetObjectPosition(clientID, handle.getValue(), -1, position,
                vrep.simx_opmode_oneshot);
		
		//printToFile(position, "positionsGroundTruth.txt");
		
		//return null; -- versão da Caroline
                return position;
	}
	
	public void resetData() {
		System.out.println("Resseting position");
		vrep.simxPauseCommunication(clientID, true);
		FloatWA position = initFloatWA(false);
		vrep.simxCallScriptFunction(clientID, "Pioneer_p3dx", vrep.sim_scripttype_childscript, "reset",  null , 
				null ,null , null , null, null , null, null, vrep.simx_opmode_blocking);
		vrep.simxSetObjectPosition(clientID, handle.getValue(), -1, position,
                vrep.simx_opmode_oneshot);
		FloatWA angles = initFloatWA(true);
		vrep.simxSetObjectOrientation(clientID, handle.getValue(), -1, angles, vrep.simx_opmode_oneshot);
		
                
                //Devido a mudança de posição forçada do robô pode quebrar a roda. 
                //Esse trecho coloca a roda na posição original
                vrep.simxSetObjectPosition(clientID, leftWheel.getValue(), vrep.sim_handle_parent, leftWheellInitialPos,
                vrep.simx_opmode_oneshot);
                vrep.simxSetObjectPosition(clientID, rightWheel.getValue(), vrep.sim_handle_parent, rightWheellInitialPos,
                vrep.simx_opmode_oneshot);
                
     
                vrep.simxPauseCommunication(clientID, false);
		vrep.simxSynchronousTrigger(clientID);

	}
	
	private FloatWA initFloatWA(boolean orient) {
		FloatWA position = new FloatWA(3);
		float[] pos = position.getArray();
		
		if (orient) {
			pos[0] = 0.0f;
			pos[1] = 0.0f;
			//pos[2] = (float) Math.random() * 360;
                        pos[2] = generator.nextFloat() * 360;
		}
		else {
                        pos[0] = generator.nextFloat() * 0.5f;
                        pos[1] = generator.nextFloat() * 0.5f;
			//pos[0] = (float) Math.random() * 0.5f;
			//pos[1] = (float) Math.random() * 0.5f;
			pos[2] = 0.138f;
		}
		return position;
	}
	
	private void printToFile(FloatWA position,String filename) {
		try(FileWriter fw = new FileWriter(filename,true);
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
