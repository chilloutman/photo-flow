package ch.zhaw.photoflow.core.domain;

/**
 * Implementation of a model for locations.
 */
public class Location {

	private final float lat;
	private final float lon;
	
	/**
	 * Creates a new immutable location object.
	 * @param lat Latitude
	 * @param lon Longitude
	 */
	public Location(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	/**
	 * @return Latitude
	 */
	public float getLat() {
		return lat;
	}

	/**
	 * @return Longitude
	 */
	public float getLon() {
		return lon;
	}

}
