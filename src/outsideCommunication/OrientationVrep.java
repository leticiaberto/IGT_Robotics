package outsideCommunication;

import CommunicationInterface.SensorI;
import coppelia.CharW;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.remoteApi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class OrientationVrep implements SensorI {
    private int time_graph;
	
    private remoteApi vrep;
    private int clientID;
    private FloatWA angles;
    private IntW handle;

	
    public OrientationVrep( int clientID, IntW handle, remoteApi vrep) {
        this.time_graph = 0;
        this.handle = handle;

        this.vrep = vrep;
        this.clientID = clientID;
        this.angles = new FloatWA(3);
    }


    @Override
    public Object getData() {
        if (vrep.simxGetObjectOrientation(clientID, handle.getValue(), -1, angles, remoteApi.simx_opmode_buffer) == remoteApi.simx_return_ok) {		    
        // returns gamma angle
            //printToFile(angles.getArray()[2]);
            //return angles.getArray()[2];//-versão da Carol
            return angles;
        }
        return null;
    }

    private void printToFile(Object object){
        //if(time_graph < 50){
            try(FileWriter fw = new FileWriter("GroundTruthOrientation.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(object);
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //}
    }

    @Override
    public void resetData() {
            // TODO Auto-generated method stub

    }

}
