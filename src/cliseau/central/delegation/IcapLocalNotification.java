package cliseau.central.delegation;

import java.io.Serializable;

/**
 * This class represents security-irrelevant local notification and is dedicated for
 * the periodically stabilization. 
 * @author Hoang-Duong Nguyen
 *
 */
public class IcapLocalNotification implements Serializable{

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The destination id.
	 */
	private String destID;
	
	/**
	 * Construct a notification with the given destination.
	 * @param destination The destination.
	 */
	public IcapLocalNotification(int destination){
		this.destID = "" + destination;
	}
	
	/**
	 * Returns the destination of this notification.
	 * @return The destination of this notification.
	 */
	public String getDestination(){
		return this.destID;
	}
}