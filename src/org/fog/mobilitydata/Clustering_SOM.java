package org.fog.mobilitydata;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.FogDevice;
import org.fog.placement.LocationHandler;
import org.fog.utils.Config;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Hamza ELHAOU : hamzaelhaou.2000@gmail.com
 */
public class Clustering_SOM {
	static double distance1=0 ;
	static double distance2=0 ;
    
    public void createClusterMembers(int parentId, int nodeId, JSONObject locatorObject) {
    	 int numClusters = 10;
         int numIterations = 1000;
         double learningRate = 0.1;
         double decayRate = 0.5;
    	LocationHandler locatorTemp = new LocationHandler();
    	   locatorTemp = (LocationHandler) locatorObject.get("locationsInfo");
    	   double[][] data = new double[200][2];
    	   int[] clusters = new int[data.length];

    	for(int i =0 ;i<=117; i++ ) {
    		 int id=i+38 ;
    		  double fogNodePositionX = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(id)).latitude;
    	        double fogNodePositionY = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(id)).longitude;
    	      data[i][0]=fogNodePositionX;
    	      data[i][1]=fogNodePositionY;
    	}
    	
    	  Kohonen network = new Kohonen(data[0].length, numClusters, learningRate, decayRate);
          network.train(data, numIterations);
          for (int i = 0; i < data.length; i++) {
              clusters[i] = network.classify(data[i]);
          }
          
          
          List<Integer> clusterMemberList = new ArrayList<>();
          int fogId = nodeId;
 		   int groupe=clusters[fogId-38] ;
 		for(int j=0 ;j<clusters.length;j++) {
 			
 	    	int groupeFog=clusters[j];
 	    	
 	    	if(groupe==groupeFog)  clusterMemberList.add(j+38); 
 	    	
 	    	}
	
		
     

        if (clusterMemberList.isEmpty() || clusterMemberList.size() < 1) {
            ((FogDevice) CloudSim.getEntity(fogId)).setSelfCluster(true);
            ((FogDevice) CloudSim.getEntity(fogId)).setIsInCluster(true);
        } else {
            ((FogDevice) CloudSim.getEntity(fogId)).setIsInCluster(true);
            ((FogDevice) CloudSim.getEntity(fogId)).setSelfCluster(false);
            ((FogDevice) CloudSim.getEntity(fogId)).setClusterMembers(clusterMemberList);
            Map<Integer, Double> latencyMapL2 = new HashMap<>();
            for (int id : clusterMemberList) {
                latencyMapL2.put(id, Config.clusteringLatency);
            }
            ((FogDevice) CloudSim.getEntity(fogId)).setClusterMembersToLatencyMap(latencyMapL2);

        }
        System.out.println("The Fog Device: " + locatorTemp.instanceToDataId.get(fogId) + " with id: " + fogId + " and parent id: " + parentId +
                " has these cluster members: " + ((FogDevice) CloudSim.getEntity(fogId)).getClusterMembers()); 
        
        
        return;
    }  
}
