/* Copyright (c) 2011-2014 Richard Gay <gay@mais.informatik.tu-darmstadt.de>
 *
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
package cliseau.javacor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import cliseau.Clicap;
import cliseau.central.delegation.IcapLocalNotification;
import cliseau.javacor.CoordinatorAddressing;
import cliseau.javacor.CriticalEvent;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.EnforcementDecision;
import cliseau.javacor.LocalPolicy;

/**
 * Coordinator component of CliSeAu units.
 *
 * This class implements the coordinator component of CliSeAu units. The
 * class itself need not be changed when instantiating CliSeAu units.
 * Instead, the constructor accepts all the parameters required for configuring
 * the behavior as parameters to the constructor. This follows the "Strategy"
 * software design pattern: the coordinator runs the parametric behavior by
 * calling methods of the parameter objects.
 */
public class Coordinator implements Runnable {
	/** This is the identifier of this coordinator */
	private String identifier;

	/** The socket for listening for connections from the local interceptor */
	private ServerSocket localServer;
	/** The socket for listening for connections from remote CliSeAu units */
	private ServerSocket remoteServer;

	/** Object allowing the coordinator to address remote coordinators and the local enforcer */
	private CoordinatorAddressing addressing;

	/** The local policy object for making local decisions */
	private LocalPolicy localPolicy;

	/** Indicates whether the coordinator shall continue operating */
	private boolean canContinue;

	/** Level including and above which all messages are displayed */
	//private org.apache.log4j.Level logLevel;

	/**
	 * Create a coordinator object.
	 *
	 * @param identifier Identifier name of the coordinator
	 * @param localServer The socket for listening for connections from the local interceptor
	 * @param remoteServer The socket for listening for connections from remote CliSeAu units
	 * @param addressing Object allowing the coordinator to address remote coordinators and the local enforcer
	 * @param localPolicy The local policy object for making local decisions
	 * @param logLevel Level including and above which all messages are displayed
	 */
	// @NHD logger removed
	public Coordinator(
			String identifier,
			ServerSocket localServer,
			ServerSocket remoteServer,
			CoordinatorAddressing addressing,
			LocalPolicy localPolicy) {
		this.identifier   = identifier;
		this.localServer  = localServer;
		this.remoteServer = remoteServer;
		this.addressing   = addressing;
		this.localPolicy  = localPolicy;
		canContinue = true;
	}

	/**
	 * Runnable listener for remote connections.
	 *
	 * In order to listen to the local and the remote socket at the same time, we
	 * use multi-threading. The RemoteListener is the class for the thread which
	 * listens on the remote socket.
	 */
	private class RemoteListener implements Runnable {
		/**
		 * Run the listener for remote connections.
		 */
		public void run() {
			handleSocket(remoteServer, false);
		}
	}

	/**
	 * Handling incoming requests with the help of the local policy and the enforcer.
	 *
	 * This method listens on the given socket, reads the incoming requests,
	 * comes to a reaction to the request with the help of the local policy, and
	 * delivers the reaction either to the local enforcer or to a remote CliSeAu unit.
	 *
	 * This method does not terminate unless the {@link #canContinue canContinue}
	 * field is set to false using {@link #stop() stop}.
	 *
	 * @param socket The ServerSocket on which to listen for incoming requests.
	 * @param bLocal Whether or not "socket" is the local server socket (true) or the remote one (false).
	 * @todo The logger code should finally only produce "debug" output if
	 *       a special flag is set.
	 * @todo Think about closing the connection for the incoming request only
	 *       *after* the request to the local policy. This would allow for
	 *       sending some kind of status code back to the sender, thereby
	 *       enabling error handling.
	 * @todo Check whether it is possible to avoid using 'instanceof' on the
	 *       LocalPolicy response 'resp' by instead calling a method on 'resp'
	 *       itself. Using 'instanceof' is bad programming practice.
	 * @todo Define a meaningful subclass of RuntimeException for the case that
	 *       the LocalPolicy returned an object of the wrong type (which is not a
	 *       subclass of EnforcementDecision or DelegationLocPolReturn).
	 * @todo Increase robustness of the Coordinator by meaningfully reacting to
	 *       exceptions and (a) requesting a retransmit from the sender on
	 *       reception of an invalid message; (b) retransmitting sent messages on
	 *       transmission failures.
	 */
	protected void handleSocket(ServerSocket socket, final boolean bLocal) {
		// create a logger for logging of debug infos
		//Logger logger = Logger.getLogger("coordinator@" + identifier + (bLocal ? "/local" : "/remote"));
		//logger.setLevel(logLevel);

		// now operate in an infinite loop (until 'stop()' is called)
		while (canContinue) {
			IcapLocalNotification notif = null; 	// @NHD: for receiving local notification (not sec-relative) 
			CriticalEvent ev = null;     			// for receiving local requests
			DelegationReqResp dr = null; 			// for receiving remote requests
			Socket connection;
			try {
				// Step 2: wait for local requests
				connection = socket.accept();
				//logger.debug("got connection");
			} catch (SocketTimeoutException ste) {
				try {
					Thread.sleep(500); // wait a bit until the next try
				} catch (InterruptedException ie) {}
				continue; // ignore timeouts or connection failures
			} catch (IOException ioe) { // same handler as for SocketTimeoutException!
				try {
					Thread.sleep(500); // wait a bit until the next try
				} catch (InterruptedException ie) {}
				continue; // ignore timeouts or connection failures
			}

			try {
				// Step 3: read the incoming (local/remote) message
				//logger.debug("reading from stream");
				ObjectInputStream ois = null;
				try {
					ois = new ObjectInputStream(connection.getInputStream());
				} catch (java.io.EOFException eofe) {
					//TODO: this exception is currently caused, e.g,. by the
					// StartupBarrier, which establishes a connection but does not
					// send anything. That's why we don't do anything special here.
					// However, in the future, this hack should be avoided by a less
					// hackish StartupBarrier implementation.
					continue;
				}
				try {
					
					/** @NHD the type of the incoming object depends on blocal*/ 
					
					if (bLocal) {
						
						// @NHD: In order to use the coordinator for other purposes...
						Object obj = ois.readObject();
						if(obj instanceof CriticalEvent)
							ev = (CriticalEvent) obj;
						else if (obj instanceof IcapLocalNotification)
							notif = (IcapLocalNotification) obj;
						
							
						//logger.info("read critical event");
					} else {
						dr = (DelegationReqResp) ois.readObject();
						//logger.info("read delegation request/response");
					}
				} catch (ClassNotFoundException cnfe) {
					// Unknown class received. Could be caused by different CliSeAu
					// versions in the same system or by a message from something
					// else than a CliSeAu unit. We currently simply ignore
					// this issue here with a warning.
					//logger.warn("class not found (" + cnfe.getMessage() + ")");
					continue;
				} catch (ClassCastException cce) {
					// Unexpected class received. Could be caused by different CliSeAu
					// versions in the same system, by different versions of
					// Coordinator and Interceptor, or by a message from something
					// else than a CliSeAu component. We currently simply
					// ignore this issue here with a warning.
					//logger.warn("request of invalid type (" + cce.getMessage() + ")");
					continue;
				}
			} catch (IOException e) {
				//logger.warn("failed to 'readObject()' [" + e.getClass().getName() + ": " + e.getMessage() + "]");
				//TODO: rethrow exception? use counter and exit after 3 times?
				continue;
			} finally {
				try {
					// Step 4: close the connection.
					//logger.debug("closing connection");
					connection.close();
				} catch (IOException e) {
					// Failing to close the connection is unproblematic; well, actually only
					// if it does not occur very frequently since this might end up in OOM.
					//logger.warn("failed to close the connection.");
				}
			}

			/** @NHD Now we have received the object of either type    CriticalEvent   or   DelegationReqResp  */
			
			// Step 5: handle input with local policy
			final LocalPolicyResponse resp;
			synchronized(localPolicy) {
				if (bLocal) {
					//@NHD also handle local notifications
					if(notif == null && ev != null)
						resp = localPolicy.localRequest(ev);
					else
						resp = localPolicy.localNotify(notif);
				} else {
					resp = localPolicy.remoteRequest(dr);
				}
			}

			/** @NHD Now we have response object */
			
			// Step 6: pass on the result - locally or remotely
			/** @NHD Bad programming practice !
			 * 	 NO NEED to consider this case  ;) */
			if (resp instanceof EnforcementDecision) {
		
				//logger.info("sending local decision");

				// Step 7a: connect to local enforcer
				try {
					Socket out_connection = addressing.connectLocalEnforcer();
					try {
						// Step 8a: send serialized enforcement decision
						ObjectOutputStream oos = new ObjectOutputStream(out_connection.getOutputStream());
						oos.writeObject(resp);
					} finally {
						// Step 9a: close connection to local enforcer
						out_connection.close();
					}
				} catch (IOException e) {
					// Currently, retransmissions or other means of recovering from
					// the error are not intended but should be added later to
					// increase the rebustness of the Coordinator.
					//logger.warn("failed to send enforcemend decision to local enforcer (" + e.getMessage() + ")");
					continue;
				}	
			} else if (resp instanceof DelegationLocPolReturn) {
				DelegationLocPolReturn del = (DelegationLocPolReturn)resp;
				
				final String dest = del.getDestinationID();
				//logger.info("sending delegation request/response to CliSeAu unit \"" + dest + "\"");

				// Step 7b: connect to the remote CliSeAu unit
				try {
					Socket out_connection = addressing.connectRemote(dest);
					try {
						// Step 8b: send serialized delegation req/resp
						final DelegationReqResp drPrime = del.getDR();
						ObjectOutputStream oos = new ObjectOutputStream(out_connection.getOutputStream());
						oos.writeObject(drPrime);
						
						// TODO debug - remove me !
						sent("DEL REQ/RES", dest);
						
					} finally {
						// Step 9b: close connection to remote CliSeAu unit
						out_connection.close();
					}
				} catch (IOException e) {
					
					// Currently, retransmissions or other means of recovering from
					// the error are not intended but should be added later to
					// increase the rebustness of the Coordinator.
					//logger.warn("failed to send delegation request/response to CliSeAu unit \""+dest+"\" (" + e.getMessage() + ")");
					continue;
				}
			} else {
				
				//	<------------------------------------------------------------------------------------------>
				//					Handle the case the unit send the decision directly 
				//					 to the unit that initially requested the decision
				//	<------------------------------------------------------------------------------------------>
				if(resp instanceof DelegationLocPolDirectReturn){
					// @NHD  Direct connection => does not require addressing.
					//		   The subclass of LocalPolicyResponse must contain Domain & Port of destination ;)
					DelegationLocPolDirectReturn del = (DelegationLocPolDirectReturn)resp;
					// Step 7c: directly connect to the remote CliSeAu unit
					try {
						Socket out_connection = new Socket(del.getDestinationDomain(), del.getDestinationPort());
						try {
							// Step 8c: send serialized delegation resp (This must be a response according to the DHT Algorithm!)
							final DelegationReqResp drPrime = del.getDR();
							ObjectOutputStream oos = new ObjectOutputStream(out_connection.getOutputStream());
							oos.writeObject(drPrime);
							
							// TODO debug - remove me !
							sent("DIRECT RETURN", "...");
							
						} finally {
							// Step 9c: close connection to remote CliSeAu unit
							out_connection.close();
						}
					} catch (IOException e) {
						continue;
					}
				} else{
					if(resp instanceof DelegationLocPolDummyReturn)
						continue;
					else
						// This case can only occur if some extension to the original
						// CliSeAu implementation derived additional classes
						// immediately from LocalPolicyResponse - and forgot to handle this
						// here.
						throw new RuntimeException("Unexpected subclass of LocalPolicyResponse returned by LocalPolicy.");
				}
				// done.
			}
		}
	}

	/**
	 * Start the coordinator and handling local as well as remote requests.
	 *
	 * Note: we spawn off a separate thread for the remote socket only
	 * because I don't know how to listen on 2 sockets (the local and the
	 * remote one) at the same time within a single thread. In C (POSIX), this
	 * would be possible without multi-threading when using the 'select()'
	 * function. There seems to be related functionality in Java's NIO
	 * package, but I don't know exactly how to use it.
	 */
	public void run() {
		// Step 1: create a separate thread for listening to remote requests
		RemoteListener remoteListener = new RemoteListener();
		Thread remoteThread = new Thread(remoteListener);
		remoteThread.start();

		// Step 2: listen for local requests in the same thread.
		handleSocket(localServer, true);
	}

	/**
	 * Try to stop the coordinator gently.
	 *
	 * @todo Currently, the implementation only works properly if the local and
	 *   remote sockets have a timeout; if they wait for connections
	 *   indefinetely, then the coordinator in most cases won't terminate.
	 * @todo Generally, it should be considered whether a more sophisticated
	 *   shutdown procedure for a whole running CliSeAu framework should
	 *   be implemented - similar to the bootstrapping (which is to be
	 *   implemented elsewhere).
	 */
	public void stop() {
		canContinue = false;
	}
	
	// TODO Debugging  @NHD
	private void sent(String msg, String dest){
		//System.out.println("* canContinued = " + canContinue);
		//System.out.println("--> Server " + Clicap.config.get(Clicap.ID) + " sent: " + msg + " to " + dest);
		
		
	}
}

