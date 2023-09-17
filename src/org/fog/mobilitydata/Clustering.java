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
 * @author Mohammad Goudarzi
 */
public class Clustering {
	static double distance1=0 ;
	static double distance2=0 ;
	
	
    public void createClusterMembers(int parentId, int nodeId, JSONObject locatorObject) {
    	System.out.println(nodeId);
        List<Integer> SiblingListIDs = new ArrayList<>();
        List<FogDevice> SiblingsList = new ArrayList<FogDevice>();
        List<Integer> clusterMemberList = new ArrayList<>();
        int fogId = nodeId;
        LocationHandler locatorTemp = new LocationHandler();
        locatorTemp = (LocationHandler) locatorObject.get("locationsInfo");
        
        // retourner fog device pére 
        FogDevice parentDevice = (FogDevice) CloudSim.getEntity(parentId);
        // retourner les enfants de fog device (ID) 
        SiblingListIDs = parentDevice.getChildrenIds();

        if (SiblingListIDs.size() < 1 || SiblingListIDs.isEmpty()) {
            //System.out.println("The node: " + nodeId + " with parent Id: " + parentId + " does not have any cluster members " + parentDevice.getChildrenIds());
            System.out.println("ERROR in clustering --> Pranet Node does not habe any children");
            //Log.printLine("ERROR in clustering --> Pranet Node cannot be Found");
            return;
        }
        
        // retourner les fog device enfant de chaque pére à partir de l'id 
        for (int i = 0; i < SiblingListIDs.size(); i++) {
            int tempId = SiblingListIDs.get(i);
            FogDevice tempNode = (FogDevice) CloudSim.getEntity(tempId);
            SiblingsList.add(tempNode);
        }

        // retourner la position fog device 
        double fogNodePositionX = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogId)).latitude;
        double fogNodePositionY = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogId)).longitude;
        Location L1 = new Location(fogNodePositionX, fogNodePositionY, 0);
        
        for (FogDevice fogdevice : SiblingsList) {
        
        	
            // tester si le fog device existe dans la liste des enfants de parent Id 
            if (fogId == fogdevice.getId()) {
                continue;
            }
            // To check all siblings except itself
            
            // retourner la position de chaque fog device enfant
            double tempX = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogdevice.getId())).latitude;
            double tempY = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogdevice.getId())).longitude;

            Location L2 = new Location(tempX, tempY, 0);

            boolean clusterCheck = calculateInRange(L1, L2, Config.Node_Communication_RANGE);

            //Clustering Policy
            //double x = Math.pow((fogNodePositionX - tempX), 2) + Math.pow((fogNodePositionY - tempY), 2);

            /*if (Math.sqrt(x) <= Config.Node_Communication_RANGE / 1000) {
                clusterMemberList.add(fogdevice.getId());
            */
            if (clusterCheck == true) {
          
            	
                clusterMemberList.add(fogdevice.getId());
            }
        }
        // Clustering Policy


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
        
       // System.out.println(" cluster members: " + ((FogDevice) CloudSim.getEntity(fogId)).getClusterMembers()); 
        
        return;
    }
    
    
    
    // test 
    
    public void createClusterMembers2(int parentId, int nodeId, JSONObject locatorObject) {
    	int k=10;
    	int max_iteration=20;
    	LocationHandler locatorTemp = new LocationHandler();
    	   locatorTemp = (LocationHandler) locatorObject.get("locationsInfo");
    	 // System.out.println( locatorTemp.dataObject.resourceLocationData.size());
    	Map<Integer, Location> points=new HashMap<>();
    	Map<Integer, Location> centroids=new HashMap<>();
    	Map<Integer, Integer> clusters=new HashMap<>();
    	for(int i =38 ;i<=157; i++ ) {
    		 int id=i ;
    		  double fogNodePositionX = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(id)).latitude;
    	        double fogNodePositionY = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(id)).longitude;
    	        Location L1 = new Location(fogNodePositionX, fogNodePositionY, 0);
    	   
    	        points.put(id, L1);
    	}
    	
    	
    	 // DETERMINER INTIAL CENTRES 
       Location  x1 = points.get(19);
   	
  		int r =70;
  		
  		for (int i = 0; i < k; i++) {
  			
  			x1=points.get(r++);
  			
  			centroids.put(i, x1);
  			
  		}
  		
  		// intial iteration 
  		clusters = kmeans(points, centroids, k);
  		
  		
  		Location db= new Location(0, 0, 0);
  		
		for (int i = 0; i < max_iteration; i++) {
			for (int j = 0; j < k; j++) {
				List<Location> list = new ArrayList<>();
				
				for (Integer key : clusters.keySet()) {
					
					if (clusters.get(key)==j) {
				
						Location loc = new Location(points.get(key).latitude, points.get(key).longitude, 0);
						list.add(loc);
					}
			}
				
				db = centroidCalculator(list);
				
				centroids.put(j, db);
			
			}
			clusters.clear();
			clusters = kmeans(points, centroids, k);
			
		}
		 int fogId = nodeId;
		 List<Integer> clusterMemberList = new ArrayList<>();
		   int groupe=clusters.get(fogId) ;
		for(Map.Entry<Integer, Integer> fog : clusters.entrySet()) {
			
	    	int groupeFog=fog.getValue();
	    	
	    	if(groupe==groupeFog)  clusterMemberList.add(fog.getKey()); 
	    	
	    	}
		
	
//    	
//        List<Integer> SiblingListIDs = new ArrayList<>();
//        List<FogDevice> SiblingsList = new ArrayList<FogDevice>();
//        List<Integer> clusterMemberList = new ArrayList<>();
//        int fogId = nodeId;
//        
//     
//        
//        // retourner fog device pére 
//        FogDevice parentDevice = (FogDevice) CloudSim.getEntity(parentId);
//        // retourner les enfants de fog device (ID) 
//        SiblingListIDs = parentDevice.getChildrenIds();
//
//        if (SiblingListIDs.size() < 1 || SiblingListIDs.isEmpty()) {
//            //System.out.println("The node: " + nodeId + " with parent Id: " + parentId + " does not have any cluster members " + parentDevice.getChildrenIds());
//            System.out.println("ERROR in clustering --> Pranet Node does not habe any children");
//            //Log.printLine("ERROR in clustering --> Pranet Node cannot be Found");
//            return;
//        }
//        
//        // retourner les fog device enfant de chaque pére à partir de l'id 
//        for (int i = 0; i < SiblingListIDs.size(); i++) {
//            int tempId = SiblingListIDs.get(i);
//            FogDevice tempNode = (FogDevice) CloudSim.getEntity(tempId);
//            SiblingsList.add(tempNode);
//        }
//
//        // retourner la position fog device 
//        double fogNodePositionX = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogId)).latitude;
//        double fogNodePositionY = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogId)).longitude;
//        Location L1 = new Location(fogNodePositionX, fogNodePositionY, 0);
//        
//        for (FogDevice fogdevice : SiblingsList) {
//        	
//            // tester si le fog device existe dans la liste des enfants de parent Id 
//            if (fogId == fogdevice.getId()) {
//                continue;
//            }
//            // To check all siblings except itself
//            
//            // retourner la position de chaque fog device enfant
//            double tempX = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogdevice.getId())).latitude;
//            double tempY = locatorTemp.dataObject.resourceLocationData.get(locatorTemp.instanceToDataId.get(fogdevice.getId())).longitude;
//
//            Location L2 = new Location(tempX, tempY, 0);
//
//            boolean clusterCheck = testGroupe((FogDevice) CloudSim.getEntity(fogId)  , fogdevice ,clusters );
//
//            //Clustering Policy
//            //double x = Math.pow((fogNodePositionX - tempX), 2) + Math.pow((fogNodePositionY - tempY), 2);
//
//            /*if (Math.sqrt(x) <= Config.Node_Communication_RANGE / 1000) {
//                clusterMemberList.add(fogdevice.getId());
//            */
//            if (clusterCheck == true) {
//           
//            	
//                clusterMemberList.add(fogdevice.getId());
//            }
//        }
        // Clustering Policy


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
        
       // System.out.println(" cluster members: " + ((FogDevice) CloudSim.getEntity(fogId)).getClusterMembers()); 
        
        return;
    }
    
    
    public void createClusterMembers1(int parentId, int nodeId, JSONObject locatorObject) {
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
   
    
    
    
    
    private static boolean calculateInRange(Location loc1, Location loc2, double fogRange) {
  
    	
    	//rayon de la Ter re
        final int R = 6371; // Radius of the earth in Kilometers
        
        // distance entre deux point par rapport latitude
        double latDistance = Math.toRadians(loc1.latitude - loc2.latitude);
     // distance entre deux point par rapport longitude
        double lonDistance = Math.toRadians(loc1.longitude - loc2.longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(loc1.latitude)) * Math.cos(Math.toRadians(loc2.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // kms

   //   double   sum = ((loc1.latitude - loc2.latitude) * (loc1.longitude - loc2.longitude));
     //  double distance2= Math.sqrt(sum);
        
        // distance en KM 
        distance = Math.pow(distance, 2);
       double dist=Math.sqrt(distance);
       double min= fogRange / 1000;
       

        if (dist <=min  ) {
        	distance1=dist;
            return true;
        } else {
            return false;
        }

    }
    
	
	  public static Map<Integer, Integer> kmeans(Map<Integer, Location> points,Map<Integer, Location> centroids, int k)
	  {
		  Map<Integer, Integer>clusters = new HashMap<>();
		  final int R = 6371; 
		  
		  int k1 = 0;
		  double dist=0.0;
	  for(Map.Entry<Integer, Location> point : points.entrySet() ) {
		  int fogId=point.getKey();
		  Location locFog =point.getValue();
		  double minimum = 999999.0;
		  for (int j = 0; j < k; j++) {
			  
			  Location centre= centroids.get(j);
			  double latDistance = Math.toRadians(locFog.latitude - centre.latitude); // distance entre deux point par rapport longitude
	            double lonDistance = Math.toRadians(locFog.longitude - centre.longitude);
	  
	            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	                    + Math.cos(Math.toRadians(locFog.latitude)) * Math.cos(Math.toRadians(centre.latitude))
	                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	            double distance = R * c; // kms
	            
	            distance = Math.pow(distance, 2);
	             dist=Math.sqrt(distance);
	  
	                if (dist < minimum) {
	                	minimum = dist; k1 = j; 
	                	distance2=dist;
	                   }
	  
	  } 
		  clusters.put(fogId, k1);
	  
	  
	  
	  }
	  
	  return clusters;
	  
	  }
	 
    
	
	//methode pour calculer le centre du groupe 
	public  Location centroidCalculator(List<Location> a) {
		
		int count = 0;
		//double x[] = new double[ReadDataset.numberOfFeatures];
		double sumLa=0.0;
		double sumLo=0.0;
		Location centroids = new Location(0, 0, 0) ;
		
			sumLa=0.0;
			count = 0;
			for(Location x:a){
				count++;
				sumLa = sumLa +x.latitude;
				sumLo=sumLo+x.longitude;
			}
			centroids.latitude = sumLa / count;
			centroids.longitude= sumLo / count;
	
		return centroids;

	}
    
    
    private  boolean testGroupe(FogDevice x , FogDevice y, Map<Integer, Integer> groupe ) {
    	int k1=0 ,k2=-1 ; 
    	
    	for(Map.Entry<Integer, Integer> fog : groupe.entrySet()) {
    	int k=fog.getValue();
    	int id=fog.getKey();
    	if(x.getId()==id) k1=k ;
    	if(y.getId()==id) k2=k ;
    		
    	}
    	
    	if (k1==k2) return true ; 
    	else return false ;
    }
    
}
