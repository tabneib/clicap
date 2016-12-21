/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cliseau.central;

import java.util.Hashtable;

import cliseau.central.event.IcapEvent;
import cliseau.central.event.IcapEventType;
import cliseau.central.event.IcapNWMapEvent;
import cliseau.central.event.IcapScaleInEvent;
import cliseau.central.event.IcapScaleOutEvent;
import cliseau.javacor.CriticalEvent;
import cliseau.javatarget.CriticalEventFactory;

/**
 * Factory class that produces Critical Event objects. For each critical event,
 * scaling event, or network map printing event there is a static method to generate
 * the corresponding IcapEvent object. 
 * @author Hoang-Duong Nguyen
 *
 */
public class IcapEventFactory implements CriticalEventFactory {
	/**
	 * Creates a login event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A login event representing an action from client side.
	 */
	public static CriticalEvent loginC(Hashtable<String, String> headers, String body){
		
		try{
			String bodyArray[] = (body.split("&")[0]).split("="); 
			String email  = bodyArray.length >= 2 ? bodyArray[1] : "";
			String sid = getSID(headers);
			
			if(email != null && sid != null && !sid.equals("") && !email.equals(""))
				return new IcapEvent(IcapEventType.LOG_IN_C,
						sid, email, null, null, null);
			else 
				return null;
		}
		catch(Exception e){
			return null;
		}		
	}
	
	/**
	 * Creates a confirm order event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A confirm order event representing an action from client side.
	 */
	public static CriticalEvent confirmOrderC(
			Hashtable<String, String> headers, String body){
		
		return new IcapEvent(
				IcapEventType.CONFIRM_ORDER_C, getSID(headers), null, body, null, null);
	}

	/**
	 * Creates a token establish event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A token establish event representing an action from client side.
	 */
	public static CriticalEvent tokenEstablishC(Hashtable<String, String> headers){

		return new IcapEvent(
				IcapEventType.TOKEN_ESTABLISH_C, getSID(headers), null, null, null, null);
	}

	/**
	 * Creates a token establish event from server side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A token establish event representing an action from server side.
	 */
	public static CriticalEvent tokenEstablishS(Hashtable<String, String> headers){
		return new IcapEvent(IcapEventType.TOKEN_ESTABLISH_S,
				null, null, null, (
						headers.get("location").split("&")[1]).split("=")[1], null);
	}

	/**
	 * Creates a receive payer id event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A receive payer id event representing an action from client side.
	 */
	public static CriticalEvent receivePayerIdC(
			Hashtable<String, String> headers, String head){
		return new IcapEvent(
				IcapEventType.RECEIVE_PAYER_ID_C, getSID(headers), null, null, 
						((head.split(" ")[1]).split("&")[3]).split("=")[1], 
							((head.split(" ")[1]).split("&")[4]).split("=")[1]);
	}

	/**
	 * Creates a process order event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A process order event representing an action from client side.
	 */
	public static CriticalEvent processOrderC(Hashtable<String, String> headers){

		return new IcapEvent(
				IcapEventType.PROCESS_ORDER_C, getSID(headers), null, null, null, null);
	}

	/**
	 * Creates a successful order event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A successful event representing an action from client side.
	 */
	public static CriticalEvent successfulOrderC(Hashtable<String, String> headers){

		return new IcapEvent(IcapEventType.SUCCESSFUL_ORDER_C,
				getSID(headers), null, null, null, null);
	}

	/**
	 * Creates a logout event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return A logout event representing an action from client side.
	 */
	public static CriticalEvent logoutC(Hashtable<String, String> headers){

		return new IcapEvent(IcapEventType.LOG_OUT_C,
				getSID(headers), null, null, null, null);
	}
	
	/**
	 * Creates an admin event from client side out of the given HTTP header and HTTP body.
	 * 
	 * @param headers The HTTP header used for the event's creation.
	 * @param body The HTTP body used for the event's creation.
	 * @return An admin event representing an action from client side.
	 */
	public static CriticalEvent adminEvent(String body){
		try{
			if(body.contains("join"))
				return new IcapScaleOutEvent((body.split("&")[1]).split("=")[1], 
						(body.split("&")[2]).split("=")[1], 
						Integer.parseInt((body.split("&")[3]).split("=")[1]));
			else if(body.contains("leave"))
				return new IcapScaleInEvent((body.split("&")[1]).split("=")[1]);
			else
				return new IcapNWMapEvent();
		}
		catch(Exception e){
			return null;
		}	
	}
	
	/**
	 * Extract the Session ID of the given HTTP headers
	 * @param headers  the given HTTP headers
	 * @return	Session ID
	 * 			empty string if there is no session ID
	 */
	private static String getSID(Hashtable<String, String> headers){
		String[] cookieArray = headers.get("cookie").split("; ");
		for(int i=0; i<cookieArray.length; i++){
			String[] aCookie = cookieArray[i].split("=");
			if(aCookie[0].equals("sid"))
				return aCookie[1];
		}
		return "";
	}
}