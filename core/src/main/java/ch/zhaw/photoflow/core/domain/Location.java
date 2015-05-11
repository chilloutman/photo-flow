package ch.zhaw.photoflow.core.domain;

/**
 * Implementation of a model for locations.
 */
public class Location {
	
	private float lat;	//latitude
	private float lon;	//longitude
	
	public Location() {
		
	}
	
	
	/************ GETTERS AND SETTERS ************/
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	public float getLon() {
		return lon;
	}
	public void setLon(float lon) {
		this.lon = lon;
	}
	
}
