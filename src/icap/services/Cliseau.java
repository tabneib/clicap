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
package icap.services;

import icap.IcapServer;
import icap.core.AbstractService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import tools.general.ExtendedByteArrayOutputStream;
import cliseau.central.IcapEnforcementDecision;
import cliseau.central.IcapEventFactory;
import cliseau.central.policy.scaling.LeavingProtocol;
import cliseau.javacor.CriticalEvent;
import cliseau.javatarget.CoordinatorInterface;

/**
 * Attempt to use another Control of policy for ICAP Server by developing a service that
 * mediates between ICAP Server and CliSeAu. Basically we just take "a half" of CliSeAu 
 * as a service for ICAP Server instead of GreasySpoon. <br>
 * Half of CliSeAu: The Policy and Coordinator (No Intercepter, Enforcer nor Encapsulated 
 * Program).
 * Intuition of the approach: ICAP acts as an Intercepter and Enforcer, while CliSeAu 
 * plays the role of a Policy and Coordinator.
 * @author Hoang-Duong Nguyen
 *
 */
public class Cliseau extends AbstractService{
	
	static final String SERVICE_NAME = "USING CLISEAU AS SERVICE"; 
	String body=null;
	
	public Cliseau (IcapServer _server, Socket clientsocket) {
		super(_server, clientsocket);
	}
	
	@Override
	public VectoringPoint getSupportedModes() {
		return VectoringPoint.REQRESPMOD;
	}
	
	@Override
	public int getResponse(ByteArrayOutputStream response) {
		
		response.reset();
		int returncode=-1;
		try{
			switch (this.getType()){
			case REQMOD: 
				returncode = getReqmodResponse(response);
				break;
			case RESPMOD: 
				returncode = getRespModResponse(response);
				break;
			default: break;
			}
			// If in either ReqMode or RespMode
			if (returncode!=-1){
				return returncode;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		// Server error
		return 500;
	}
	
	@Override
	public String getDescription() {
		return SERVICE_NAME; 
	}
	
	/**
	 * AbstractRespmodeService implementation
	 * ICAP request has been parsed: generate a response for REQMOD
	 * @param bas The stream in which response will be provided
	 * @return	ICAP response code
	 * @throws Exception 
	 */
	public synchronized int getReqmodResponse(ByteArrayOutputStream bas) 
			throws Exception {

		// ICAP response is based on the generated event
		CriticalEvent ce = getReqModCE();
		if(ce != null){
			System.out.println(
					"                                                             " 
																		+ ce.toString());
			// This is a critical event or administrator request
			// => waiting for decision
			CoordinatorInterface.send(ce);
			IcapEnforcementDecision ed = 
					(IcapEnforcementDecision) CoordinatorInterface.receive();
		
			if((ed.decision).equals(IcapEnforcementDecision.Decision.PERMIT)){

				if ((ed.type).equals(IcapEnforcementDecision.Type.SCALE)){
					
					// Add delay to the response for scaling request
					// For testing purpose only
					try {
						Thread.sleep(LeavingProtocol.DELAY);
					} catch (InterruptedException e) {
						System.err.print("\n Adding delay failed !!!! \n");
						e.printStackTrace();
					}
				}
				
				// If this is a decision for network map request
				// So return the picture
				if ((ed.type).equals(IcapEnforcementDecision.Type.MAP)){
					return displayNWMap(bas);
				}
				// Let HTTP request pass through SQUID
				return earlyResponse(bas);
			}
			else{
				// Deny HTTP request and replace by a...
				if((ed.type).equals(IcapEnforcementDecision.Type.SCALE))
					// ... Scaling Warning, or...
					return displayScalingWarning(bas);		
				else
					// ... Security Warning
					return displaySecWarning(bas);
			}
		}
		else
			// The event is not critical => so let it pass through SQUID
			return earlyResponse(bas);	
	}
	
	/**
	 * AbstractRespmodeService implementation
	 * ICAP request has been parsed: generate a response for RESP MODE
	 * @param bas The stream in which response will be provided
	 * @return	ICAP response code
	 * @throws Exception 
	 */
	public synchronized int getRespModResponse(ByteArrayOutputStream bas) 
			throws Exception {
		
		CriticalEvent ce = getRespModCE();
		if(ce != null){
			System.out.println(
					"                                                             " 
																		+ ce.toString());
			// Critical event => waiting for enforcement decision
			CoordinatorInterface.send(ce);
			IcapEnforcementDecision ed = 
					(IcapEnforcementDecision) CoordinatorInterface.receive();
			
			if((ed.decision).equals(IcapEnforcementDecision.Decision.PERMIT)){
				// Let HTTP response pass through the reverse proxy
				return earlyResponse(bas);
			}
			else{
				// Block the response
				// This case will never occur
			    return displaySecWarning(bas);
			}			
		}
		else
			// Not relevant, so let it pass through
			return earlyResponse(bas);
	}
	
	//	<-------------------------------------------------------------------------------->
	//							  Auxiliary Methods for Service Automata
	//	<-------------------------------------------------------------------------------->
	
	/**
	 * Decide if the intercepted HTTP request is security-relevant.
	 * @return	the respective Critical Event
	 * 			null if not relevant
	 */
	public CriticalEvent getReqModCE(){
		
		String headers = reqHeader.toString();
		String body =  getRequestBody();
		
		// (1) LOG_IN_C
		if(headers.contains("/account.php?login=process")){
			return IcapEventFactory.loginC(httpReqHeaders, body);
		}
		
		// (2) CONFIRM_ORDER_C
		if(headers.contains("/checkout.php?process") && body!=null){
			return IcapEventFactory.confirmOrderC(httpReqHeaders, body);
		}
		
		// (3) TOKEN_ESTABLISH_C  and  PayPal Redirection
		if(headers.contains("/checkout.php?callback&module=paypal_express") 
							&& !headers.contains("&express_action=retrieve&token=")
				 				&& body==null){
			return IcapEventFactory.tokenEstablishC(httpReqHeaders);
		}
		
		// Steps (4)(5)(6) are automatically processed after the buyer clicks on 
		// "Jetzt Zahlen"
		// (4) RECEIVE_PAYER_ID_C - Client sends PayerID to Web Application
		if(headers.contains(
				"/checkout.php?callback&module="
				+ "paypal_express&express_action=retrieve&token=") 
								&& body==null){
			return IcapEventFactory.receivePayerIdC(httpReqHeaders, headers); 
		}
		
		// (5) PROCESS_ORDER_C 
		if(headers.contains("/checkout.php?process") && body==null){
			return IcapEventFactory.processOrderC(httpReqHeaders);
		}
		
		// (6) SUCCESSFUL_ORDER_C
		if(headers.contains("/checkout.php?success") && body==null){
			return IcapEventFactory.successfulOrderC(httpReqHeaders);
		}
		
		// (7) LOG_OUT_C
		if(headers.contains("/account.php?logoff") && body==null){
			return IcapEventFactory.logoutC(httpReqHeaders);
		}		
		
		// CLICAP ADMINISTRATOR REQUEST (Scaling or Network map request
		if(headers.contains("clicap") && body!=null){
			
			return IcapEventFactory.adminEvent(body);
		}
				
		// Not a critical event
		return  null;
	}
	
	/**
	 * Decide if the intercepted HTTP response is security-relevant.
	 * @return	the respective Critical Event
	 * 			null if not relevant
	 */
	public CriticalEvent getRespModCE(){

		String headers = resHeader.toString();			
		// TOKEN_ESTABLISH_S  and  PayPal Redirection
		if(headers.contains(""
				+ "https://www.sandbox.paypal.com/"
				+ "cgi-bin/webscr?cmd=_express-checkout&token=")){
			return IcapEventFactory.tokenEstablishS(httpRespHeaders);
		}	
		// Not a critical event
		return null;
	}
	
	/**
	 * Generate directly HTTP response for client's request which display a warning page
	 * @param bas  the byte array outputstream to write 
	 * @return	server code
	 */
	private int displaySecWarning(ByteArrayOutputStream bas){
		
		// The returning warning page
		// For ICAP message format, read: https://tools.ietf.org/html/rfc3507
		String body="<!DOCTYPE HTML PUBLIC "
				+ "\"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head>"
				+ "<title>Security Violation detected </title></head>"
				+ "<body bgcolor='#DF0101'><h1><font color='yellow'>"
				+ "You are gonna violate our security"
				+ " policy. Please go back :)</font></h1>"
				+ "</body>"
				+ "</html>";
		String headers=
				"HTTP/1.1 200 OK\nContent-Type: "
				+ "text/html;charset=ISO-8859-1\nContent-Length: "
				+ body.length() +"\n\n";
		// Print out a warning message on the terminal
		System.out.println(
				"\n                                                             "
							+">>>>  SECURITY VIOLATION DETECTED  <<<<");
		System.out.println(
				"                                                             "
							+">>>>    WARNING PAGE DISPLAYED     <<<<\n");
		this.resHeader = new StringBuilder(headers);
		this.resBody = new ExtendedByteArrayOutputStream();
		try {
			resBody.write(body.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return fullResponse(bas);
		} catch (Exception e) {
			// Server error
			return 500;
		}
	}	
	
	/**
	 * Generate directly HTTP response for administrtor's scaling request which display
	 * a warning page <br>
	 * @param bas  the byte array outputstream to write 
	 * @return	server code
	 */
	private int displayScalingWarning(ByteArrayOutputStream bas){
		
		// The returning warning page
		// For ICAP message format, read: https://tools.ietf.org/html/rfc3507
		String body="<!DOCTYPE HTML PUBLIC "
				+ "\"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><head>"
				+ "<title>Security Violation detected </title></head>"
				+ "<body><h1>"
				+ "Scaling is not successful. Another scaling request is not yet "
				+ "completed. Please go back and try again :)</h1>"
				+ "</body>"
				+ "</html>";
		String headers=
				"HTTP/1.1 200 OK\nContent-Type: "
				+ "text/html;charset=ISO-8859-1\nContent-Length: "
				+ body.length() +"\n\n";
		
		// Print out a warning message on the terminal
		System.out.println(
				"\n                                                             "
							+">>>>  SCALING UNSUCESSFUL  <<<<");

		this.resHeader = new StringBuilder(headers);
		this.resBody = new ExtendedByteArrayOutputStream();
		try {
			resBody.write(body.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return fullResponse(bas);
		} catch (Exception e) {
			// Server error
			return 500;
		}
	}	
	
	/**
	 * TODO comment me !
	 * @param bas
	 * @return
	 */
	private int displayNWMap(ByteArrayOutputStream bas){
	
		// The returning HTTP response that piggyback the picture of the network map
		// For ICAP message format, read: https://tools.ietf.org/html/rfc3507
		byte[] pngByteArray;
		BufferedImage image;
		
		try {
			String cwd =  System.getProperty("user.dir");
			// Remove the suffix "/log" of the current working directory
			cwd = cwd.substring(0, cwd.length() - 4);
			image = ImageIO.read(new File(cwd+"/log/networkMap.png"));
			
			// write it to byte array in-memory (png format)
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ImageIO.write(image, "png", b) 	;
			 pngByteArray = b.toByteArray();
			
			// convert it to a String with 0s and 1s        
			StringBuilder sb = new StringBuilder();
			for (byte by : pngByteArray)
				sb.append(Integer.toBinaryString(by));
		 
		String headers=
				"HTTP/1.1 200 OK\nContent-Type: image/png\nContent-Length: "
				+ sb.capacity() 
				+"\nConnection: Keep-Alive"
				+"\n\n";
		this.resHeader = new StringBuilder(headers);
		this.resBody = new ExtendedByteArrayOutputStream();
		
			resBody.write(pngByteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return fullResponse(bas);
		} catch (Exception e) {
			// Server error
			return 500;
		}
	}
	
	//	<-------------------------------------------------------------------------------->
	//								Auxiliary Methods for HTTP message
	//	<-------------------------------------------------------------------------------->
	
	/**
	 * Retrieve the body of the HTTP message
	 * @return  the body
	 * 		 	null if HTTP message doesn't have a body
	 */
	public String getRequestBody(){
		boolean containsBody = false;
		if (i_req_body>0){
			try {
				containsBody = this.getAllBody();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Uncompress the body if compressed
		ExtendedByteArrayOutputStream reqBodi = reqBody;
		boolean initiallyGzipped = isCompressed();
		if (containsBody){ 
			if (initiallyGzipped)
				try {
					reqBodi = uncompress(reqBody);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		if (containsBody) {
			return reqBodi.toString();
		}
		else
			return null;
	}
	
	/**
	 * Retrieve the body of the HTTP message
	 * @return  the body
	 * 		 	null if HTTP message doesn't have a body
	 */
	public String getResponseBody(){
		boolean containsBody = false;
		if (i_res_body>0){
			try {
				containsBody = this.getAllBody();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Uncompress the body
		ExtendedByteArrayOutputStream resBodi = resBody;
		boolean initiallyGzipped = isCompressed();
		if (containsBody){ 
			if (initiallyGzipped)
				try {
					resBodi = uncompress(resBody);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		if (containsBody) {
			return resBodi.toString();
		}
		else
			return null;
	}
	
	/**
	 * Set the hash table which stores all HTTP headers in REQMODE
	 * @param headers	the given hash table
	 */
	public void setHttpReqHeaders(Hashtable<String, String> headers){
		this.httpReqHeaders.clear();
		this.httpReqHeaders.putAll(headers);
	}
	
	/**
	 * Set the hash table which stores all HTTP headers in RESPMODE
	 * @param headers	the given hash table
	 */
	public void setHttpRespHeaders(Hashtable<String, String> headers){
		this.httpRespHeaders.clear();
		this.httpRespHeaders.putAll(headers);
	}

	/**
	 * Set the string builder which stores all HTTP headers in REQMODE
	 * @param headers	the given string builder
	 */
	public void setReqHeader(String headers){
		this.reqHeader = new StringBuilder();
		this.reqHeader.append(headers);
	}
	
	/**
	 * Set the string builder which stores all HTTP headers in RESPMODE
	 * @param headers	the given string builder
	 */
	public void setResHeader(String headers){
		this.resHeader = new StringBuilder();
		this.resHeader.append(headers);
	}
	
	/**
	 * Set the body of the HTTP request
	 * @param body
	 */
	public void setReqBody(String body){
		this.i_req_body = body.length();
		this.reqBody.reset();
		try {
			this.reqBody.write(body.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the body of the HTTP response
	 * @param body
	 */
	public void setResBody(String body){
		this.i_res_body = body.length();
		this.resBody.reset();
		try {
			this.resBody.write(body.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}