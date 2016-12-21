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
package cliseau.central.policy.scaling;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cliseau.Clicap;
import cliseau.central.IcapEnforcementDecision;
import cliseau.central.delegation.IcapDelegationResp;
import cliseau.central.delegation.IcapLeavingNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.policy.IcapPolicy;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.LocalPolicy;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class handles all the received notification corresponding to the leaving protocol.
 * Every time the local policy receives a leaving notification it will determine which 
 * method of this class should be called. Furthermore, which attributes of this classes
 * should be used depends on the node in which the corresponding method is invoked. This 
 * means, some attributes are used only by the central node, some only used by the 
 * successor and some only used by the leaving node.
 * 
 * @author Hoang-Duong Nguyen
 * 
 */
public class LeavingProtocol {

	/**
	 * The delay (in ms) to add in order to slow down the leaving process for testing
	 * purpose. The duration of the leaving process will be extended by this delay. This
	 * is crucial to test the transparency of the leaving protocol.<br>
	 * Set this to 0 to let the system run normally. 
	 */
	public static final int DELAY = 0000;

	/**
	 * The local policy of the CliSeau unit of the CliCap node in this server
	 */
	protected static IcapPolicy policy;
	
	/**
	 * Finger table row number to be updated. This attribute is used only by the leaving 
	 * node in order to itemize the updating process of every finger table that contains
	 * the identifier of this node.
	 */
	public static int rowNumber;
	
	/**
	 * The temporal predecessor of this node, used by the successor of the leaving node.
	 * This attribute stores the identifier of the predecessor of the leaving node.
	 * Maintaining this information allows the successor of the leaving node being able
	 * to make correct decision for the request for which the leaving node is responsible
	 * while the routing algorithm still works as the leaving node is still there (until
	 * it is indeed terminated).
	 */
	public static int tmpPred;
	
	/**
	 * The host of the temporal predecessor. This attribute is used by the successor of 
	 * the leaving node.
	 */
	public static String tmpPredDomain;
	
	/**
	 * The port of the temporal predecessor. This attribute is used by the successor of 
	 * the leaving node.
	 */
	public static int tmpPredPort;

	/**
	 * The host of the central unit. This attribute is used by the leaving node in order
	 * to be able to communicate directly with the central node.
	 */
	private static String centralDomain;
	
	/**
	 * The port of the central unit. This attribute is used by the leaving node in order
	 * to be able to communicate directly with the central node.
	 */
	private static int centralPort;

	/**
	 * This attribute is used to state the fact if this node is being moved.
	 */
	private static boolean beingMoved = false;

	/**
	 * Used by the major node to state if the leaving protocol is processing
	 */
	public static boolean isLeaving = false;

	/**
	 * Initialize the leaving protocol. By default there is no finger table row to be 
	 * updated nor temporal predecessor to be stored, so set to -1.
	 * @param pol	The local policy of the corresponding CliSeAu  unit
	 */
	public static void init(LocalPolicy pol) {
		policy = (IcapPolicy) pol;
		rowNumber = -1;
		tmpPred = -1;
	}

	// <--------------------------------------------------------------------------------->
	// 								NOTIFY THE LEAVING NODE
	// <--------------------------------------------------------------------------------->

	/**
	 * STEP 2A  <br>
	 * After the HTTP request for scaling in is captured and sent to CliSeAu, the local 
	 * policy will call this method in order to trigger the querying for the leaving node.
	 * <br><br>
	 * Occurred in : The major node <br>
	 * Triggered by: HTTP scaling request<br><br>
	 * 
	 * @param id  The identifier of the leaving node
	 * @return	  The local policy return that carries the id of the next node on the 
	 * 			  routing path and the notification for querying
	 */
	public static LocalPolicyResponse startQuery(int id) {

		// The leaving process begins
		LeavingProtocol.isLeaving = true;

		// Create query request
		IcapLeavingNotification notif = 
				new IcapLeavingNotification(Notification.LEAVE_STEP_2B_QUERY_REQ);
		setSrcInfo(notif);
		notif.setLeavingNodeID(Integer.toString(id));

		String nextUnit = Integer.toString(Clicap.fTable.lookUp(id));
		return new DelegationLocPolReturn(nextUnit, notif);
	}

	/**
	 * STEP 2B   <br>
	 * This method is called by each of the node on the routing path of the query for
	 * leaving node. If this node is the requested  one, it trigger the next step by
	 * notifying the successor, otherwise it forwards the request to the next node on the
	 * routing path.<br>
	 * <br>
	 * Occurred in : Nodes on the path from the central- to the leaving node (STEP 2B)<br> 
	 * Triggered by: LEAVE_STEP_2B_QUERY_REQ notification
	 * 
	 * @param notification
	 * 					The notification sent from the previous node on the routing path
	 * @return			The local policy return that carries the id of the next node
		*				on the routing path and the notification for querying
	 */
	public static LocalPolicyResponse query(IcapLeavingNotification notification) {
		
		if (!policy.isResponsible(Integer.parseInt(notification.getLeavingNodeID()))) {
			// Not responsible => Finger table look-up
			// then just forward the notification to the next unit
			// => STEP 2B
			int nextUnit = Clicap.fTable.lookUp(Integer.parseInt(notification
					.getLeavingNodeID()));
			return new DelegationLocPolReturn(Integer.toString(nextUnit), notification);
		} else {
			// Trigger STEP 3
			// Case this unit is responsible for the key (and therefore needs to be 
			return notifySuccessor(notification);
		}
	}
	
	/**
	 * STEP 3
	 * Finally the query routing process has reached the node that is responsible for the
	 * key carried by the leaving request which means it should be removed. This leaving
	 * node will now trigger the next step by notifying its successor. <br><br>
	 * 
	 * Occurred in  : The leaving node  <br>
	 * Triggered by : query(IcapLeavingNotification)
	 * 
	 * @param notification 
	 * 				The notification sent from the previous node on the routing path
	 * @return		The local policy direct return that carries the host and port of the 
	 * 				successor and the notification to trigger the next step
	 */
	private static LocalPolicyResponse notifySuccessor(
			IcapLeavingNotification notification){
	
		// Set state flag
		beingMoved = true;

		// So send notification to its successor (triggering STEP 4)
		IcapLeavingNotification notif = new IcapLeavingNotification(
				Notification.LEAVE_STEP_3_SEND_DATA_TO_SUC);

		// Piggyback the data
		notif.setData(policy.getData());
		// Also send information of the predecessor of the leaving node
		setPreInfo(notif);
		
		// Save the Host and Port of the central node in order to contact it at the end
		centralDomain = notification.getSourceDomain();
	
		centralPort = notification.getSourcePort();
			return new DelegationLocPolDirectReturn(Clicap.getSucDomain(),
				Clicap.getSucPort(), notif);
	}	

	/**
	 * STEP 4  <br>
	 * <br>
	 * After receiving notification about the leaving process from its predecessor, the 
	 * successor of the leaving node consider the predecessor of the leaving node as its 
	 * temporal predecessor and update the valid token set by the set carried by the
	 * notification message.<br><br>
	 * 
	 * Occurred in  : The Successor of the leaving node<br>
	 * Triggered by : LEAVE_STEP_3_SEND_DATA_TO_SUC<br><br>
	 * 
	 * @param notification 
	 * 				The notification sent from the predecessor which is the leaving node
	 * @return		The local policy direct return to notify the leaving node that its
	 * 				successor if informed about on-going the leaving process
	 */
	public static LocalPolicyResponse notifyLeavingNode(
			IcapLeavingNotification notification) {

		IcapLeavingNotification notif = new IcapLeavingNotification(
				Notification.LEAVE_STEP_4_SUC_RESP);
		// Update data
		policy.addData(notification.getData());
		// Assign temporal Predecessor
		tmpPred = Integer.parseInt(notification.getPreID());
		tmpPredDomain = notification.getPreDomain();
		tmpPredPort = notification.getPrePort();

		// Tell the leaving node that its successor is informed
		return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
				Clicap.getPredPort(), notif);
	}

	
	// <--------------------------------------------------------------------------------->
	// 				UPDATE ALL FINGER TABLES THAT CONTAIN THE LEAVING NODE
	// <--------------------------------------------------------------------------------->


	/**
	 * Starting STEP 5  <br>
	 * The new node has just received confirmation from its predecessor or a notification
	 * that an updating branch has just terminated. It now starts/continues updating 
	 * the finger tables that contains its identifier. This method triggers
	 * the process by generating the first request message for updating.<br>
	 * <br>
	 * Occurred in  : The leaving node <br>
	 * Triggered by : LEAVE_STEP_4_SUC_RESP notification or 
	 * 									receiveUpdateConfirmation()<br>
	 * <br>
	 *
	 * @return		The local policy return that carries the finger table row number to
	 * 				be updated
	 */
	public static LocalPolicyResponse triggerUpdating() {
		
		// Create query request
		IcapLeavingNotification notif = new IcapLeavingNotification(
				Notification.LEAVE_STEP_5A_PRED_QUERY);

		// Pack all the needed information required by the Finger Table updating process
		setSrcInfo(notif);
		setSucInfo(notif);	
		notif.setRowNumber(rowNumber);

		// Compute target = N - 2^(i-1) = ID - 1
		int target = Clicap.getIDnum() - (int) Math.pow(2, rowNumber - 1);
		
	
		// Make target positive
		if (target < 0)
			target = target	+ Clicap.getCapacity();
	
		notif.setTarget(target);
		
		// Determine the next destination
		String nextUnit;
		if(policy.isResponsible(target)){
			// Trigger STEP 5B
			// The leaving node is already responsible for the target
			// Note: target != ID of leaving node
			// Leaving node's predecessor is the right one!
			notif.setType(Notification.LEAVE_STEP_5B_PRED_FOUND);
			return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),	
					Clicap.getPredPort(), notif);
		}
		else
			// Look up the next unit on the routing path
			nextUnit = Integer.toString(Clicap.fTable.lookUp(target));
		
		// Send query notification to the next unit
		return new DelegationLocPolReturn(nextUnit, notif);		
	}

	/**
	 * STEP 5A and 5B <br>
	 * Successively search for the predecessor of the given target. This method handles
	 * messages sent to the nodes on the routing path from the leaving node to the 
	 * successor of the last node whose finger table possibly contains joining node'ID at
	 * the given row. <br> <br>
	 * 
	 * Occurred in  : Nodes whose finger tables possibly contain the leaving node's ID<br>
	 * Triggered by : JOIN_STEP_5A_PRED_QUERY notification<br>
	 * <br>
	 * @param notification 
	 * 				The notification sent from the successor to trigger the updating 
	 * 				process
	 * @return		The local policy return that carries the finger table row number to
	 * 				be updated
	 * @return		
	 */
	public static LocalPolicyResponse checkPredecessor(
			IcapLeavingNotification notification) {
		
		if(!policy.isResponsible(notification.getTarget())){
			// Continue STEP 5A
			// This node is not yet the requested one
			// => forward the notification to the next node
			
			// Find out the next destination 
			int nextUnit = Clicap.fTable.lookUp(notification.getTarget());
			
			// Forward the notification	
			return new DelegationLocPolReturn(Integer.toString(nextUnit), notification);
		}
		else{			
			// STEP 5B
			// This node is the direct successor of the leaving node
			
			if(notification.getTarget() == Clicap.getIDnum()){
				// and it is equal to the target
				// => also the predecessor of the target
				// => STEP 5C
				return updateCounterClockwise(notification);				
			}
				
			// Otherwise forward the query to the predecessor directly
			// Adapt the type of the notification accordingly
			notification.setType(Notification.LEAVE_STEP_5B_PRED_FOUND);
				
			return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
				Clicap.getPredPort(), notification);							
		}	
	}

	/**
	 * STEP 5C and 5D <br>
	 * This node could possibly update his finger table according to the given target. 
	 * After successful updating he will forward the updating request to his predecessor.
	 * Otherwise he notifies the leaving node that this updating branch is finished <br>
	 * <br>
	 * Occurred in  : The direct or "further" predecessor of a particular target 	<br>
	 * Triggered by : LEAVE_STEP_5B_PRED_FOUND  or  LEAVE_STEP_5C_UPDATE_FT	<br>
	 * <br>
	 * @param notification 
	 * 				The notification sent from the previous node on the routing path
	 * @return		The local policy direct return that carries the host and port of the
	 * 				predecessor of this node and the finger table row number to be updated 
	 */
	public static LocalPolicyResponse updateCounterClockwise(
			IcapLeavingNotification notification) {

		if (updateFingerTable(notification)) {
			// continue STEP 5C
			// Updated finger table successfully
			// => forward to its predecessor
			notification.setType(Notification.LEAVE_STEP_5C_UPDATE_FT); 
			return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
					Clicap.getPredPort(), notification);
		} else {
			// STEP 5D
			// No need to update finger table
			// => This branch is finished at this node
			// => direct reply to source node (leaving node)
			
			// Adapt the type of notification accordingly
			notification.setType(Notification.LEAVE_STEP_5D_BRANCH_TERMINATED);

			// Tell the leaving node that this updating "branch" has terminated
			return new DelegationLocPolDirectReturn(notification.getSourceDomain(),
					notification.getSourcePort(), notification);
		}
	}

	/**
	 * Continue STEP 5 and STEP 6  <br> 
	 * A updating "branch" has just been finished and the last node on the branch sends 
	 * this reply back to the leaving node. This leaving node has to decide whether or
	 * not to send further updating request. If not, it must trigger the next step by 
	 * notifying the predecessor<br><br>
	 * 
	 * Occurred in  : The joining node <br>
	 * Triggered by : JOIN_STEP_10D_BRANCH_TERMINATED notification <br>
	 * 
	 * @return	The local policy return that signal the major node if finished
	 */
	public static LocalPolicyResponse receiveUpdateConfirmation() {

		if (rowNumber < Clicap.getBitLength()) {
			// Continue STEP 10
			// There are still further row(s) to be updated
			rowNumber++;
			// Create query request
			return triggerUpdating();
		} 
		else {
			// STEP 6 - All rows are updated
			// => Notify the predecessor
			// => Trigger STEP 7
			IcapLeavingNotification notif = new IcapLeavingNotification(
					Notification.LEAVE_STEP_6_NOTIFY_PRED);

			// Pack all information of the successor of this leaving node into
			// the notification
			setSrcInfo(notif);
			setSucInfo(notif);

			// Notify the predecessor
			return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
					Clicap.getPredPort(), notif);
		}
	}


	// <--------------------------------------------------------------------------------->
	// 							SYNCHRONIZE DATA AND TERMINATE
	// <--------------------------------------------------------------------------------->

	/**
	 * STEP 7  <br>
	 * After receiving the confirmation from the predecessor that it is ready which means
	 * the predecessor has updated its successor pointer, the leaving node will now 
	 * notify the successor and wait for its ready confirmation.
	 * 
	 * Occurred in  : The Predecessor of the leaving node  <br>
	 * Triggered by : LEAVE_STEP_6_NOTIFY_PRED notification <br>
	 * 
	 * @param notification 
	 * 				The notification sent from leaving node
	 * @return		The local policy direct return to inform the leaving node that its
	 * 				predecessor is ready
	 */
	public static LocalPolicyResponse notifyPredecessor(
			IcapLeavingNotification notification) {
		IcapLeavingNotification notif = new IcapLeavingNotification(
				Notification.LEAVE_STEP_7_PRED_READY);

		// Update Successor Pointer

		Clicap.config.put(Clicap.SUCCESSOR, notification.getSucID());
		Clicap.config.put(Clicap.SUC_DOMAIN, notification.getSucDomain());
		Clicap.config.put(Clicap.SUC_PORT, Integer.toString(notification.getSucPort()));
		
		// Also update addressing
		Clicap.addressing.setAddress(Clicap.getSucID(), 
				new InetSocketAddress(Clicap.getSucDomain(), Clicap.getSucPort()));
		
		Clicap.fTable.log("» SUCCESSOR:      " + Clicap.getSucID());

		return new DelegationLocPolDirectReturn(notification.getSourceDomain(),
				notification.getSourcePort(), notif);
	}

	/**
	 * STEP 8  <br>
	 * After receiving the confirmation from the predecessor that it is ready, the leaving
	 * node informs its successor that it can be ready   <br><br>
	 * 
	 * Occurred in : The leaving node  <br>
	 * Triggered by : LEAVE_STEP_7_PRED_READY notification  <br>
	 * 
	 * @param notification 
	 * 				The notification sent from the predecessor of the leaving node
	 * @return		The local policy direct return to inform the successor 
	 */
	public static LocalPolicyResponse predReady(IcapLeavingNotification notification) {

		IcapLeavingNotification notif = new IcapLeavingNotification(
				Notification.LEAVE_STEP_8_GOOD_BYE_SUC);

		// Pack needed information to communicate with this leaving node
		setSrcInfo(notif);

		// Say good bye to the successor
		return new DelegationLocPolDirectReturn(Clicap.getSucDomain(),
				Clicap.getSucPort(), notif);
	}

	/**
	 * STEP 9  <br>
	 * After receiving the notification of its predecessor, the successor of the leaving
	 * node knows that the predecessor of the leaving node is already ready and consider 
	 * it as the new successor. This node will then set its temporal predecessor to the
	 * official predecessor and notify the leaving node that it is ready.  <br><br>
	 * 
	 * Occurred in  : The Successor of the leaving node  <br>
	 * Triggered by : LEAVE_STEP_8_SUC_UPDATE_DATA notification <br>
	 * 
	 * @param notification 
	 * 				The notification sent from the leaving node
	 * @return		The local policy direct return to notify the leaving node
	 */
	public static LocalPolicyResponse updateSuccessor(
			IcapLeavingNotification notification) {

		// Update predecessor
		Clicap.config.put(Clicap.PREDECESSOR, Integer.toString(tmpPred));
		Clicap.config.put(Clicap.PRE_DOMAIN, tmpPredDomain);
		Clicap.config.put(Clicap.PRE_PORT, Integer.toString(tmpPredPort));
		
		// Also update addressing (though this might be unnecessary)
		Clicap.addressing.setAddress(Clicap.getPredID(), 
				new InetSocketAddress(Clicap.getPredDomain(), Clicap.getPredPort()));
		
		Clicap.fTable.log("» PREDECESSOR:    " + Clicap.getPredID());
		
		// For testing purpose
		// set the new predecessor after a delay
		final ScheduledExecutorService setPred = Executors.newScheduledThreadPool(1);
		setPred.schedule(new Runnable() {
			@Override
			public void run() {
				// Reset temporal Predecessor
				tmpPred = -1;
			}
		}, DELAY, TimeUnit.MILLISECONDS);


		// Tell the leaving node that its successor is ready
		IcapLeavingNotification notif = new IcapLeavingNotification(
				Notification.LEAVE_STEP_9_SUC_READY);
		return new DelegationLocPolDirectReturn(notification.getSourceDomain(),
				notification.getSourcePort(), notif);
	}

	/**
	 * STEP 10  <br>
	 * The leaving node has now received the confirmations from its predecessor and 
	 * successor that they are both ready. It can hence be ready and notifies the central
	 * node. <br><br>
	 * 
	 * Occurred in : The leaving node  <br>
	 * Triggered by : LEAVE_STEP_9_SUC_READY  notification <br>
	 * 
	 * @param notification 
	 * 				The notification sent from the successor
	 * @return		The local policy direct return to inform the central node
	 */
	public static LocalPolicyResponse notifyCentralNode(
			IcapLeavingNotification notification) {

		IcapLeavingNotification notif = new IcapLeavingNotification(
				Notification.LEAVE_STEP_10_LEAVING_NODE_READY);

		// Pack information about this leaving node into the notification
		setSrcInfo(notif);

		// Notify the central node that every node is ready
		return new DelegationLocPolDirectReturn(centralDomain, centralPort, notif);
	}

	/**
	 * STEP 11  <br>
	 * The central node receives the confirmation of the leaving node that it is ready
	 * to leave, which means all other nodes are also ready for the leaving of that node.
	 * The central node will then send a command to the leaving node to confirm that
	 * it can leave and after that the central node will accept further scaling request.
	 * <br><br>
	 * 
	 * Occurred in  : Central node  <br>
	 * Triggered by : LEAVE_STEP_10_LEAVING_NODE_READY notification <br>
	 * 
	 * @param notification 
	 * 				The notification sent from the leaving node
	 * @return		The local policy direct return to notify the leaving node 
	 */
	public static LocalPolicyResponse killNode(IcapLeavingNotification notification) {

		// The leaving process terminates
		// For testing purpose
		// Execute after a delay
		final ScheduledExecutorService setIsLeaving = Executors.newScheduledThreadPool(1);
		setIsLeaving.schedule(new Runnable() {
			@Override
			public void run() {
				LeavingProtocol.isLeaving = false;
			}
		}, DELAY, TimeUnit.MILLISECONDS);

		return new DelegationLocPolDirectReturn(notification.getSourceDomain(),
				notification.getSourcePort(), new IcapLeavingNotification(
						Notification.LEAVE_STEP_11_KILL));
	}

	/**
	 * STEP 12  <br>
	 * The leaving node receives the confirmation of the central node that it can now
	 * leave. It will set up a "leaving schedule" to terminate after a short delay. This
	 * allows the leaving node still be able to send back a decision for the leaving 
	 * request back to the major node.<br><br>
	 * 
	 * Occurred in : The leaving node  <br>
	 * Triggered by : LEAVE_STEP_11_KILL notification <br>
	 * 
	 * @param notification 
	 * 				The notification sent from the central node
	 * @return		The local policy direct return to the central node that carries the 
	 * 				decision for the leaving request
	 */
	public static LocalPolicyResponse leave(IcapLeavingNotification notification) {

		final ScheduledExecutorService cancel = Executors.newScheduledThreadPool(1);

		// Kill this node after 1 seconds
		cancel.schedule(new Runnable() {
			@Override
			public void run() {
				System.out.println("\n»»» Server " + Clicap.getID()
						+ " has been successfully terminated. \n");
				// Log this leaving event
				Clicap.fTable.log(" »»» THIS SERVER HAS SUCCESSFULLY LEAVED.");
				// Terminate this node
				Clicap.kill();
			}
		}, DELAY + 1000, TimeUnit.MILLISECONDS);

		IcapEnforcementDecision ed = new IcapEnforcementDecision(
				IcapEnforcementDecision.Decision.PERMIT,
				IcapEnforcementDecision.Type.SCALE);

		// Return a decision to the central node
		return new DelegationLocPolDirectReturn(centralDomain, centralPort,
				new IcapDelegationResp(ed));
	}

	// <--------------------------------------------------------------------------------->
	// 									Auxiliary Methods
	// <--------------------------------------------------------------------------------->

	/**
	 * This method is call by a node that possibly has the leaving node in his finger 
	 * table on some particular row in order to ensure if this is really the case. <br> 
	 * If yes, it will update the respective finger table entry, otherwise do nothing 
	 * and informs the leaving node to terminate the current updating branch.
	 * 
	 * @param notification 
	 * 				The notification sent from the previous node on the routing path
	 * @return		true if finger table is updated, otherwise false
	 */
	private static boolean updateFingerTable(IcapLeavingNotification notification) {

		if (Clicap.fTable.getNode(notification.getRowNumber()) == Integer
				.parseInt(notification.getSourceID())) {
			// The leaving node is at the i-th row of the finger table
			// Update finger table and return true
			Clicap.fTable.setNode(notification.getRowNumber(),
					Integer.parseInt(notification.getSucID()));
			// Also update addressing
			Clicap.addressing.setAddress(notification.getSucID(), 
					new InetSocketAddress(notification.getSucDomain(), 
							notification.getSucPort()));
			return true;
		} else
			// No need to update
			return false;
	}
	
	/**
	 * Pack information of the predecessor of this node into the given leaving 
	 * notification. The information to be packed are the ID, host and port of the 
	 * predecessor
	 * @param notif	
	 * 				The given notification
	 */
	private static void setPreInfo(IcapLeavingNotification notif){
		notif.setPreID(Clicap.getPredID());
		notif.setPreDomain(Clicap.getPredDomain());
		notif.setPrePort(Clicap.getPredPort());
	}
	
	/**
	 * Pack information of the successor of this node into the given leaving 
	 * notification. The information to be packed are the ID, host and port of the 
	 * successor
	 * @param notif	
	 * 				The given notification
	 */
	private static void setSucInfo(IcapLeavingNotification notif){
			notif.setSucID(Clicap.getSucID());
			notif.setSucDomain(Clicap.getSucDomain());
			notif.setSucPort(Clicap.getSucPort());
	}
		
	/**
	 * Pack information of this node into the given leaving notification. The information 
	 * to be packed are the ID, host and port of this current node
	 * @param notif	
	 * 				The given notification
	 */
	private static void setSrcInfo(IcapLeavingNotification notif){
		notif.setSourceID(Clicap.getID());
		notif.setSourceDomain(Clicap.getDomain());
		notif.setSourcePort(Clicap.getRemotePort());
	}

	/**
	 * Check if this node is the successor of the leaving node.<br>
	 * 
	 * @return true if this is the successor, false otherwise
	 */
	public static boolean isSuccessor() {
		return tmpPred >= 0;
	}

	/**
	 * Check if this is the leaving node<br>
	 * 
	 * @return true if this is the leaving node, false otherwise
	 */
	public static boolean isLeavingNode() {
		return beingMoved;
	}
}