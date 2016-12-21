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
package cliseau.central.event;

import cliseau.javacor.CriticalEvent;

/**
 * Class that presents a abstract event used by the formalization
 * @author Hoang-Duong Nguyen
 */
public class IcapEvent implements CriticalEvent {
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The session id.
	 */
	public String sid     = null;
	/**
	 * The email address.
	 */
	public String email   = null;
	
	/**
	 * The identifier of the processed order.
	 */
	public String order   = null;
	
	/**
	 * The token.
	 */
	public String token   = null;
	
	/**
	 * The payer id.
	 */
	public String payerID = null;
	
	
	/**
	 * The type of this event.
	 */
	public IcapEventType type;
	
	/**
	 * Constructs a event with the given type, session id, email address, order id, token and payer id
	 * @param type The type of this event.
	 * @param sid The session id.
	 * @param email The email address.
	 * @param order The order id.
	 * @param token The token.
	 * @param payerID The payer id.
	 */
	public IcapEvent(IcapEventType type, String sid, String email,
							String order, String token, String payerID) {
		
		this.sid 	 = sid;
		this.email	 = email;
		this.order 	 = order;
		this.token   = token;
		this.payerID = payerID;
		this.type 	 = type;
	}
	
	
	/**
	 * Return the string that denotes the respective abstract event in the formalization
	 * @return the formal name of the event
	 */
	public String toString(){
		
		switch (this.type) {
		case LOG_IN_C:
			return ("LOG_IN("+ sid + ", " + email + ")");
			
		case CONFIRM_ORDER_C:
			return("CONFIRM_ORDER_C("+ sid + ", " + order + ")");
			
		case TOKEN_ESTABLISH_C:
			return("TOKEN_ESTABLISH_C("+ sid + ")");

		case TOKEN_ESTABLISH_S:
			return("TOKEN_ESTABLISH_S("+ token + ")");
		
		case RECEIVE_PAYER_ID_C:
			return("RECEIVE_PAYER_ID_C("+ sid + ", " + token + ", " + payerID + ")");
		
		case PROCESS_ORDER_C:
			return("PROCESS_ORDER_C("+ sid + ")");
		
		case SUCCESSFUL_ORDER_C:
			return("SUCCESSFUL_ORDER_C("+ sid + ")");
		
		case LOG_OUT_C:
			return("LOG_OUT_C("+ sid + ")");
			
		default:
			return("ERROR: NOT A VALID EVENT");
		}
	}
}