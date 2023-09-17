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
public class ClustringKmeans {
	static double distance1=0 ;
	static double distance2=0 ;
	
	
   
    // test 
    
    public void createClusterMembers(int parentId, int nodeId, JSONObject locatorObject) {
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
      
    
}
