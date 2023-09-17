package org.fog.placement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fog.mobilitydata.Location;
import org.fog.mobilitydata.DataParser;
import org.fog.mobilitydata.References;
import org.fog.utils.Config;

public class LocationHandler {
	
	public DataParser dataObject;
	public Map<Integer, String> instanceToDataId;
	

	public LocationHandler(DataParser dataObject) {
		// TODO Auto-generated constructor stub
		this.dataObject = dataObject;
		instanceToDataId = new HashMap<Integer, String>();
		
	}

	public LocationHandler() {
		// TODO Auto-generated constructor stub

	}
	
	public DataParser getDataObject(){
		return dataObject;
	}
	
	public static double calculateDistance(Location loc1, Location loc2) {

	    final int R = 6371; // Radius of the earth in Kilometers

	    double latDistance = Math.toRadians(loc1.latitude - loc2.latitude);
	    double lonDistance = Math.toRadians(loc1.longitude - loc2.longitude);
	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(loc1.latitude)) * Math.cos(Math.toRadians(loc2.latitude))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double distance = R * c; // kms


	    distance = Math.pow(distance, 2);

	    return Math.sqrt(distance);
	}
	

	public int determineParent(int resourceId, double time) {
		// TODO Auto-generated method stub
		String dataId = getDataIdByInstanceID(resourceId);
	//	System.out.println("************************************************************\n");
	//System.out.println(dataId);
	
		int resourceLevel=getDataObject().resourceAndUserToLevel.get(dataId);
	
	//	System.out.println(resourceLevel);
		
     int parentLevel = resourceLevel-1;

		//System.out.println(parentLevel);
	
		Location resourceLoc;
	
		if(resourceLevel!=getDataObject().levelID.get("User"))
			resourceLoc = getResourceLocationInfo(dataId); // location de fog device manipuler 
	
		else
			resourceLoc = getUserLocationInfo(dataId,time); // location de l'utilisteur manipuler 
		
		//System.out.println(resourceLoc.block);
		//System.out.println(resourceLoc.latitude);
		//System.out.println(resourceLoc.longitude);
		
	//System.out.println("************************************************************\n");
		
		int parentInstanceId = References.NOT_SET;	
		String parentDataId = "";
				
	// déterminer la topologie avant la simulation c'est à dire le parent de chaque fog device 
		if(time<References.INIT_TIME){
		//	System.out.println("************************************************************ if if if if if if if if \n");
			
			for(int i=0; i<getLevelWiseResources(parentLevel).size();i++){   // n=nombre de ressorces dans level parent 
				Location potentialParentLoc = getResourceLocationInfo(getLevelWiseResources(parentLevel).get(i)); // location de chaque ressource 
				if(potentialParentLoc.block==resourceLoc.block) { // si block de fog device manipuler égalale block de resource actuel 
					parentDataId = getLevelWiseResources(parentLevel).get(i);
					
					
					for(int parentIdIterator: instanceToDataId.keySet())
					{
						if(instanceToDataId.get(parentIdIterator).equals(parentDataId))
						{
							parentInstanceId = parentIdIterator;
							System.out.println("parent de  " +getDataIdByInstanceID(resourceId)+" est : "+getDataIdByInstanceID(parentInstanceId));
						}
					}
				}	
			}
		}
		
		
		else
		{
			double minmumDistance = Config.MAX_VALUE;
			for(int i=0; i<getLevelWiseResources(parentLevel).size();i++){ // n=nombre de ressorces dans level parent 
				Location potentialParentLoc = getResourceLocationInfo(getLevelWiseResources(parentLevel).get(i));// location de chaque ressource
				
				double distance = calculateDistance(resourceLoc, potentialParentLoc);
			
			
					if(distance<minmumDistance){
					
						parentDataId = getLevelWiseResources(parentLevel).get(i);
			
						minmumDistance = distance;
					
					}
			}
			
			for(int parentIdIterator: instanceToDataId.keySet())
			{
				if(instanceToDataId.get(parentIdIterator).equals(parentDataId))
				{
				
					System.out.println(instanceToDataId.get(parentIdIterator)+"  "+parentDataId);
					parentInstanceId = parentIdIterator;
					System.out.println("parent de  " +getDataIdByInstanceID(resourceId)+" est : "+getDataIdByInstanceID(parentInstanceId));
				}
			}
			
		}
		
		return parentInstanceId;	
	}	

	
	// retourne la location de l'ulisateur dans  chaque instant du temps de l'exécution 
	private Location getUserLocationInfo(String dataId, double time) {
		// TODO Auto-generated method stub
		return getDataObject().usersLocation.get(dataId).get(time);
	}

	
	// retourner la location de chaque resource 
	private Location getResourceLocationInfo(String dataId) {
		// TODO Auto-generated method stub
		return getDataObject().resourceLocationData.get(dataId);
	}

	
	public List<Double> getTimeSheet(int instanceId) {
		
		String dataId = getDataIdByInstanceID(instanceId);
		List<Double>timeSheet = new ArrayList<Double>(getDataObject().usersLocation.get(dataId).keySet());
		return timeSheet;
	}

	public void linkDataWithInstance(int instanceId, String dataID) {
		// TODO Auto-generated method stub
		instanceToDataId.put(instanceId, dataID);
	}

	public int getLevelID(String resourceType) {
		// TODO Auto-generated method stub
		return dataObject.levelID.get(resourceType);
	}
	
	
	
	//retourner les resources de chaque level 
	public ArrayList<String> getLevelWiseResources(int levelNo) {
		// TODO Auto-generated method stub
		return getDataObject().levelwiseResources.get(levelNo);
	}

	public void parseUserInfo(Map<Integer, Integer> userMobilityPattern, String datasetReference) throws IOException {
		// TODO Auto-generated method stub
		getDataObject().parseUserData(userMobilityPattern, datasetReference);
	}

	public void parseResourceInfo() throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		getDataObject().parseResourceData();
	}

	public List<String> getMobileUserDataId() {
		// TODO Auto-generated method stub
		List<String> userDataIds = new ArrayList<>(getDataObject().usersLocation.keySet());
		return userDataIds;
		
	}

	public Map<String, Integer> getDataIdsLevelReferences() {
		// TODO Auto-generated method stub
		return getDataObject().resourceAndUserToLevel;
	}
	
	public boolean isCloud(int instanceID) {
		// TODO Auto-generated method stub
		String dataId = getDataIdByInstanceID(instanceID);
		int instenceLevel=getDataObject().resourceAndUserToLevel.get(dataId);
		if(instenceLevel==getDataObject().levelID.get("Cloud"))
			return true;
		else
			return false;
	}
	
	public String getDataIdByInstanceID(int instanceID) {
		// TODO Auto-generated method stub
		return instanceToDataId.get(instanceID);
	}
	
	public Map<Integer, String> getInstenceDataIdReferences() {
		// TODO Auto-generated method stub
		return instanceToDataId;
	}

	public boolean isAMobileDevice(int instanceId) {
		// TODO Auto-generated method stub
		String dataId = getDataIdByInstanceID(instanceId);
		int instenceLevel=getDataObject().resourceAndUserToLevel.get(dataId);
		if(instenceLevel==getDataObject().levelID.get("User"))
			return true;
		else
			return false;
	}
}
