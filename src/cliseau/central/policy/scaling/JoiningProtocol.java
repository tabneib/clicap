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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cliseau.Clicap;
import cliseau.central.IcapEnforcementDecision;
import cliseau.central.delegation.IcapDelegationResp;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.policy.IcapPolicy;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.LocalPolicy;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class provides all needed methods for the steps in the joining protocol. The 
 * implementation is sequential in the sense that a step (and the corresponding method) 
 * is trigger by the notification message sent from the node on the previous step. The 
 * first step is triggered by the local policy of the major node according to the scaling-
 * out request of the administrator. The last step sends back a scaling-decision to the
 * local policy of major node and the protocol terminates. 
 * 
 * @author Hoang-Duong Nguyen, Tobias Reinhard
 *
 */
public class JoiningProtocol {

	/**
	 * The local policy of the CliSeau unit of the CliCap node in this server
	 */
	protected static IcapPolicy policy;
	
	/**
	 * Used by the major node to store the current finger table entry number to be 
	 * generated for the new node.
	 * 
	 */
	protected static int entryNumber;
	
	
	/**
	 * Used by the major node to store the to be generated finger table for the new node
	 * 
	 */
	private static ArrayList<Node> fingerTable;
	
	
	/**
	 * Used by the major node to maintain information about the new node to be added
	 */
	protected static Node joiningNode;
	
	/**
	 * Used to maintain the fact if this node is the joining node. Set to True if this is
	 * the joining node, otherwise False.
	 */
	static boolean isJoiningNode;
	
	/**
	 * Finger table row number to be updated. This attribute is used only by the joining 
	 * node in order to itemize the updating process of every finger table that contains
	 * the identifier of this node.
	 */
	public static int rowNumber;
	
	/**
	 * The temporal predecessor of this node, used by the successor of the joining node.
	 * Maintaining this information allows the successor of the joining node being able
	 * to make correct decision for the request for which the joining node is responsible
	 * while the routing algorithm still works as the joining node has not yet fully 
	 * joined.
	 */
	public static Node tmpPred;
	
	/**
	 * Node object storing information of the major node. This attribute is used by the 
	 * successor of the joining node and forwarded to the joining node such that the 
	 * joining node can communicate with the major node.
	 */
	static Node majorNode;

	/**
	 * Used by the major node to state if the joining protocol is processing
	 */
	public static boolean isJoining = false;

	/**
	 * Initialize the joining protocol. By default there is no finger table row to be 
	 * updated nor temporal predecessor to be stored, so set to -1.
	 * @param pol	The local policy of the corresponding CliSeAu  unit
	 */
	public static void init(LocalPolicy pol) {
		fingerTable = new ArrayList<Node>();
		isJoining = false;
		policy = (IcapPolicy) pol;
		rowNumber = 1;
		tmpPred = null;
		entryNumber = -1;
		isJoiningNode = false;
	}
	
	/**
	 * TRIGGER STEP 2
	 * The major node has just received a scaling-out request from the administrator, it
	 * now start collecting all data needed in order to instantiate the new node with 
	 * the given ID, host and port. The first required data is the finger table. This 
	 * method trigger the data collecting process to generate the finger table for the 
	 * joining node.
	 * 
	 * <br><br>
	 * Occurred in : The major node <br>
	 * Triggered by: HTTP scaling request<br><br>
	 * 
	 * @param joiningID
	 * 					The ID of the joining node
	 * @param joiningDomain
	 * 					The host of the joining node
	 * @param joiningPort
	 * 					The port of the joining node
	 * @return
	 * 			a delegation return that encapsulates the id of the next unit on the 
	 * 			the routing part and a notification for the finger table generating
	 * 			request
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse startGeneratingFT(int joiningID, 
			String joiningDomain, int joiningPort){
		
		// Store information of the joining node for later use
		joiningNode = new Node(joiningID, joiningDomain, joiningPort);
		
		// reset the entry number to be generate and compute the first target
		entryNumber = 0;
		int target = (int) ((joiningID +
				Math.pow(2, entryNumber)) % Clicap.getCapacity());
		
		// Find out the next destination 
		int nextUnit;
		if(policy.isResponsible(target))
			nextUnit = Clicap.getIDnum();
		else
			nextUnit = Clicap.fTable.lookUp(target);
		
		// Pack all needed information into the notification
		IcapJoiningNotification notif = 
				new IcapJoiningNotification(Notification.JOIN_STEP_2A_FT_ENTRY_REQ);
		setSrcInfo(notif);
		System.out.println(Clicap.getID() + " : Target = " + target);
		notif.setKey(target);

		return new DelegationLocPolReturn(Integer.toString(nextUnit), notif);
		
	}
	
	/**
	 * STEP 2A and 2B
	 * This node has received a a notification for the finger table generating request 
	 * which is originally sent from the major node. It then determine if it is the one 
	 * who is responsible for the given key. If yes, it reply the major node directly, 
	 * otherwise it forwards the request to the next node on the routing path. 
	 * <br><br>
	 * Occurred in : Nodes on the querying path for finger table entries <br>
	 * Triggered by: JOIN_STEP_2A_FT_ENTRY_REQ<br><br>
	 * 
	 * @param notification
	 * 					The notification for the finger table generating request
	 * @return	a delegation to the next unit if not responsible, otherwise a direct 
	 * 			delegation to the original source node of the request
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse checkFTRequest(
			IcapJoiningNotification notification){
		
		if(!policy.isResponsible(notification.getKey())){
			// STEP 2A
			// This is not yet the requested node, so forward to the next unit
			String nextUnit = Integer.toString(
					Clicap.fTable.lookUp(notification.getKey()));
			return new DelegationLocPolReturn(nextUnit, notification);	
		}
		else{
			// Step 2B
			// This is exactly the nearest successor of the given key
			// => return its information directly to the major node
			IcapJoiningNotification notif = 
					new IcapJoiningNotification(Notification.JOIN_STEP_2B_FT_ENTRY_RESP);
			setSrcInfo(notif);
			notif.setFingerTableEntry(new Node(Clicap.getIDnum(),
					Clicap.getDomain(), Clicap.getRemotePort()));
			return new DelegationLocPolDirectReturn(notification.getSource().domain,
					notification.getSource().port, notif);}
	}
	
	/**
	 * Continue STEP 2 or Trigger STEP 3
	 * The major node has just received a response for its finger table generating 
	 * request and has to determine if the collected data is already adequate. If yes, it 
	 * trigger the next step of the joining protocol, otherwise it sends further finger
	 * table generating request.
	 * 
	 * <br><br>
	 * Occurred in : The major node  <br>
	 * Triggered by: JOIN_STEP_2B_FT_ENTRY_RESP<br><br>
	 * 
	 * @param notification
	 * 				The response for the finger table generating request of the major node
	 * @return a delegation to the next unit of the next step if collected data is 
	 * 	adequate, otherwise a delegation that carries the finger table generating request
	 * 
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse checkFTEntries(
			IcapJoiningNotification notification){
		
		if(entryNumber < (Clicap.getBitLength())){
			// Continue STEP 2
			// There are still remaining finger table entries to be generated
			// => send further finger table entry query notification
						
			// Store collected entry
			fingerTable.add(notification.getFingerTableEntry());
			
			// Increase entry number to be generate by 1
			entryNumber ++;
			// Compute the next target
			int target = (int) ((joiningNode.id + Math.pow(2, entryNumber))
					% Clicap.getCapacity());
						
			// Find out the next destination 
			int nextUnit;
			if(policy.isResponsible(target))
				nextUnit = Clicap.getIDnum();
			else
				nextUnit = Clicap.fTable.lookUp(target);
				
			// Pack all needed information into the notification
			IcapJoiningNotification notif = 
				new IcapJoiningNotification(Notification.JOIN_STEP_2A_FT_ENTRY_REQ);
			setSrcInfo(notif);
			notif.setKey(target);

			// Forward
			return new DelegationLocPolReturn(Integer.toString(nextUnit), notif);	
		}
		else{
			// Trigger STEP 3
			// All finger table entries are collected
			// => Trigger STEP 3 by sending first query notification
			
			// Find out the next destination 
			int nextUnit;
			if(policy.isResponsible(joiningNode.id))
				nextUnit = Clicap.getIDnum();
			else
				nextUnit = Clicap.fTable.lookUp(joiningNode.id);
			
			// Pack all needed information into the notification
			IcapJoiningNotification notif = 
					new IcapJoiningNotification(Notification.JOIN_STEP_3_SUC_QUERY);
			setSrcInfo(notif);
			notif.setKey(joiningNode.id);
				// Query the successor of the joining node 
			return new DelegationLocPolReturn(Integer.toString(nextUnit), notif);
		}	
	}
	
	/**
	 * STEP 3 and STEP 4
	 * This node has just received a querying request for the successor of the joining 
	 * node. It then determines if it is responsible for the ID of the joining node.
	 * If yes, it triggers the next step. Otherwise, it forward the request to next 
	 * node on the routing path.<br><br>
	 * 
	 * Occurred in :  Nodes on the routing path of the query for the joining node's 
	 * 				  successor <br>
	 * Triggered by: JOIN_STEP_3_SUC_QUERY<br><br>
	 * 
	 * @param notification
	 * 					The notification for the querying request
	 * @return	a delegation to the next unit of the next step if this is the successor 
	 * 			, otherwise a response to forward the notification to the next node on
	 * 			the routing path
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse querySuccessor(
			IcapJoiningNotification notification){
		
		if(!policy.isResponsible(notification.getKey())){
			// STEP 3
			// This node is not yet the requested one
			// => forward the notification to the next node
			
			// Find out the next destination 
			int nextUnit = Clicap.fTable.lookUp(notification.getKey());
			
			// Forward the notification
			return new DelegationLocPolReturn(Integer.toString(nextUnit), notification);
		}
		else{
			// STEP 4
			// This node is the direct successor of the joining node
			// => Trigger STEP 5 by reply the major node directly
			
			// Pack all needed information into the notification
			IcapJoiningNotification notif = 
					new IcapJoiningNotification(Notification.JOIN_STEP_4_SUC_RESP);
			setSrcInfo(notif);
			setPreInfo(notif);
			// Send the notification to the major node directly
			return new DelegationLocPolDirectReturn(notification.getSource().domain,
					notification.getSource().port, notif);
		}
	}
		
	/**
	 * STEP 5 and STEP 6
	 * The major node has received the reply from the successor of the joining node. It
	 * now has all needed data to instantiate the new node. After instantiating the new 
	 * node it will signal the successor of the new node.
	 * 
	 * <br><br>
	 * Occurred in : The major node  <br>
	 * Triggered by: JOIN_STEP_4_SUC_RESP notification <br><br>
	 * 
	 * @param notification
	 * 					Reply of the successor of the joining node from the previous step
	 * @return the delegation to the successor of the joining node which carries 
	 * 			information of the major node and the new node
	 * 
	 * @author Tobias Reinhard, Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse instantiateNode(
			IcapJoiningNotification notification){
		
		// STEP 5
		// Instantiate the new node
		
		// Correct the collected finger table
		correctFT();
		
		List<String> cmd = getNewNodeLaunchingCommand(
				joiningNode, notification.getPredecessor(), notification.getSource());

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.inheritIO();

		try {
			@SuppressWarnings("unused")
			Process p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// clear finger table for future joining
		fingerTable.clear();

		// Sleep shortly to ensure that the new node is instantiated completely
		// Bad programming practice, could be replaced by additional communications
		System.out.println("Major Node sleeping");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Major Node awake, again");

		// STEP 6
		// Notify the successor of the new node after the instantiation
		// Pack all needed information into the notification
		IcapJoiningNotification notif = 
				new IcapJoiningNotification(
						Notification.JOIN_STEP_6_SUC_NOTIFY_INSTANTIATED);
		notif.setNewNode(joiningNode);
		notif.setMajorNode(
				new Node(Clicap.getIDnum(), Clicap.getDomain(), Clicap.getRemotePort()));
		
		// Send the notification to the successor of the new node directly
		return new DelegationLocPolDirectReturn(notification.getSource().domain,
				notification.getSource().port, notif);		
	}
	
	
	/**
	 * STEP 7
	 * The successor of the joining node has just received the signal from the major node
	 * that new node is already instantiated. This node will then transfer all enforcement
	 * data, which is the set of valid tokens, to the new node.
	 * 
	 * <br><br>
	 * Occurred in : The successor of the joining node  <br>
	 * Triggered by: JOIN_STEP_6_SUC_NOTIFY_INSTANTIATED notification <br><br>
	 * 
	 * @param notification
	 * 					The notification from the major node
	 * @return the notification carrying enforcement data to the new node 
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse deliverData(
			IcapJoiningNotification notification){

		// Set temporal predecessor to the current predecessor in order to redirect 
		// requests to the new node
		tmpPred = new Node(Clicap.getPredIDnum(), 
				Clicap.getPredDomain(), Clicap.getPredPort());		
		Node newNode = notification.getNewNode();
		
		// Update predecessor pointer
		Clicap.config.put(Clicap.PREDECESSOR, Integer.toString(newNode.id));
		Clicap.config.put(Clicap.PRE_DOMAIN, newNode.domain);
		Clicap.config.put(Clicap.PRE_PORT, Integer.toString(newNode.port));						
		
		// Also update addressing
		Clicap.addressing.setAddress(Clicap.getPredID(), 
				new InetSocketAddress(Clicap.getPredDomain(), Clicap.getPredPort()));
		Clicap.fTable.log("» PREDECESSOR:      " + Clicap.getPredID());
			
		// Send Data to the new node 
		// Pack the current data into the notification
		IcapJoiningNotification notif = 
				new IcapJoiningNotification(
						Notification.JOIN_STEP_7_SUC_SEND_DATA);		
		
		// TODO   Redirect new incoming requests to new node to achieve transparency :)
		// The solution is quite simple: add 2 new communication steps such that the new
		// node after has successfully joined will notify its successor to delete tmpPred
		// Meanwhile the successor redirects all requests for which the new node is 
		// responsible (Those requests that sastify pred < req <= tmpPred) as long as
		// tmpPred is not null.
		
		notif.setData(policy.getPartialData(newNode.id));
		notif.setMajorNode(notification.getMajorNode());
		
		// Send the notification to the new node directly to inform him that this node
		// is ready!
		return new DelegationLocPolDirectReturn(notification.getNewNode().domain,
				notification.getNewNode().port, notif);
		
	}
		
	/**
	 * STEP 8
	 * The joining node has just received enforcement data from its successor. It then
	 * signals the predecessor.
	 * <br><br>
	 * Occurred in : The joining node  <br>
	 * Triggered by: JOIN_STEP_7_SUC_SEND_DATA notification <br><br>
	 * 
	 * @param notification
	 * 					The data-carrying notification from the successor
	 * @return	the encapsulated notification to the predecessor 
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse notifyPredecessor(
			IcapJoiningNotification notification){
		
		// Update the data with the one given by the successor
		policy.addData(notification.getData());
		majorNode = notification.getMajorNode();
		
		// Tell the predecessor that it should update its successor pointer 
		IcapJoiningNotification notif = 
				new IcapJoiningNotification(
						Notification.JOIN_STEP_8_PRED_NOTIFY);
		notif.setNewNode(new Node(Clicap.getIDnum(), 
				Clicap.getDomain(), Clicap.getRemotePort()));
		return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
				Clicap.getPredPort(), notif);	
	}
	
	/**
	 * STEP 9
	 * The predecessor has just been informed that it has a new successor which is the
	 * new node. It will then update its successor pointer accordingly.
	 * <br><br>
	 * Occurred in : The Predecessor of the joining node  <br>
	 * Triggered by: JOIN_STEP_8_PRED_NOTIFY notification <br><br>
	 * 
	 * @param notification
	 * 					The signal from the joining node
	 * @return	confirmation about the updating of successor pointer
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse predecessorReady(
			IcapJoiningNotification notification){
		// Update predecessor pointer
		Node newNode = notification.getNewNode();
		Clicap.config.put(Clicap.SUCCESSOR, Integer.toString(newNode.id));
		Clicap.config.put(Clicap.SUC_DOMAIN, newNode.domain);
		Clicap.config.put(Clicap.SUC_PORT, Integer.toString(newNode.port));		
		
		// Also update addressing
		Clicap.addressing.setAddress(Clicap.getSucID(), 
				new InetSocketAddress(Clicap.getSucDomain(), Clicap.getSucPort()));
		Clicap.fTable.log("» SUCCESSOR:      " + Clicap.getSucID());
		
		// Tell the joining node that it should update all the finger tables
		IcapJoiningNotification notif = 
				new IcapJoiningNotification(
						Notification.JOIN_STEP_9_PRED_READY);
		return new DelegationLocPolDirectReturn(Clicap.getSucDomain(),
				Clicap.getSucPort(), notif);
	}

	// <--------------------------------------------------------------------------------->
	// 				UPDATE ALL FINGER TABLES THAT CONTAIN THE JOINING NODE
	// <--------------------------------------------------------------------------------->

	/**
	 * Starting STEP 10  <br>
	 * The new node has just received confirmation from its predecessor or a notification
	 * that an updating branch has just terminated. It now starts/continues updating 
	 * the finger tables that contains its identifier. This method triggers
	 * the process by generating the first request message for updating.<br>
	 * <br>
	 * Occurred in  : The joining node <br>
	 * Triggered by : JOIN_STEP_9_PRED_READY notification or 
	 * 									receiveUpdateConfirmation()<br><br>
	 *
	 * @return		The local policy return that carries the finger table row number to
	 * 				be updated
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse triggerUpdating() {

		// This node now knows that it is the current joining node
		isJoiningNode = true;
		
		// Create query request
		IcapJoiningNotification notif = new IcapJoiningNotification(
				Notification.JOIN_STEP_10A_PRED_QUERY);

		// Pack all the needed information required by the Finger Table updating process
		setSrcInfo(notif);
		setSucInfo(notif);	
		notif.setNewNode(joiningNode);
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
			// Trigger STEP 10B
			// The joining node is already responsible for the target
			// Note: target != ID of joining node
			// Joining node's predecessor is the right one!
			notif.setType(Notification.JOIN_STEP_10B_PRED_FOUND);
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
	 * STEP 10A and 10B <br>
	 * Successively search for the predecessor of the given target. This method handles
	 * messages sent to the nodes on the routing path from the joining node to the 
	 * successor of the last node whose finger table possibly contains joining node'ID at
	 * the given row. <br> <br>
	 * 
	 * Occurred in  : Nodes whose finger tables possibly contain the joining node's ID<br>
	 * Triggered by : JOIN_STEP_10A_PRED_QUERY notification<br>
	 * <br>
	 * @param notification 
	 * 				The notification sent from the successor to trigger the updating 
	 * 				process
	 * @return		The local policy return that carries the finger table row number to
	 * 				be updated
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse checkPredecessor(
			IcapJoiningNotification notification) {

		if(!policy.isResponsible(notification.getTarget())){		
			// Continue STEP 10A
			// This node is not yet the requested one
			// => forward the notification to the next node
			
			// Find out the next destination 
			int nextUnit = Clicap.fTable.lookUp(notification.getTarget());
			
			// Forward the notification
			return new DelegationLocPolReturn(Integer.toString(nextUnit), notification);					
		}
		else{
			// STEP 10B
			// This node is the direct successor of the joining node
			if(notification.getTarget() == Clicap.getIDnum()){
				// and it is equal to the target
				// => also the predecessor of the target
				// => STEP 10C
				return updateCounterClockwise(notification);						
			}			
			// Otherwise forward the query to the predecessor directly
			// Adapt the type of the notification accordingly
			notification.setType(Notification.JOIN_STEP_10B_PRED_FOUND);
				
			return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
				Clicap.getPredPort(), notification);																	
		}	
	}

	/**
	 * STEP 10C and 10D <br>
	 * This node could possibly update his finger table according to the given target. 
	 * After successful updating he will forward the updating request to his predecessor.
	 * Otherwise he notify the joining node that this updating branch is finished <br>
	 * <br>
	 * Occurred in  : The direct or "further" predecessor of a particular target 	<br>
	 * Triggered by : JOIN_STEP_10B_PRED_FOUND  or  JOIN_STEP_10C_UPDATE_FT	<br>
	 * <br>
	 * @param notification 
	 * 				The notification sent from the previous node on the routing path
	 * @return		The local policy direct return that carries the host and port of the
	 * 				predecessor of this node and the finger table row number to be updated 
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse updateCounterClockwise(															
			IcapJoiningNotification notification) {
		
		if (updateFingerTable(notification)) {
			// continue STEP 10C
			// Updated finger table successfully
			// => forward to its predecessor
			notification.setType(Notification.JOIN_STEP_10C_UPDATE_FT); 
			return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
					Clicap.getPredPort(), notification);
		} else {
			// STEP 10D
			// No need to update finger table
			// => This branch is finished at this node
			// => direct reply to source node (joining node)
			
			// Adapt the type of notification accordingly
			notification.setType(Notification.JOIN_STEP_10D_BRANCH_TERMINATED);

			// Tell the joining node that this updating "branch" has terminated
			return new DelegationLocPolDirectReturn(notification.getSource().domain,
					notification.getSource().port, notification);
		}
	}

	/**
	 * Continue STEP 10 and STEP 11  <br> 
	 * A updating "branch" has just been finished and the last node on the branch sends 
	 * this reply back to the joining node. This joining node has to decide whether or
	 * not to send further updating request. If not, it must trigger the next step by 
	 * notifying the predecessor<br><br>
	 * 
	 * Occurred in  : The joining node <br>
	 * Triggered by : JOIN_STEP_10D_BRANCH_TERMINATED notification <br>
	 * 
	 * @return	The local policy return that signal the major node if finished
	 * @author Hoang-Duong Nguyen
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
			// STEP 11 - All rows are updated
			// => Notify the major node
			IcapJoiningNotification notif = new IcapJoiningNotification(
					Notification.JOIN_STEP_11_N_READY);
			// Reset row number
			rowNumber = 1;
			
			// No more a joining node
			isJoiningNode = false;
			// Send directly to the major node
			return new DelegationLocPolDirectReturn(majorNode.domain,		
					majorNode.port, notif);	
		}
	}

	
	// <--------------------------------------------------------------------------------->
	// 										Finish
	// <--------------------------------------------------------------------------------->

	/**
	 * STEP 12 <br>
	 * The major node has received the confirmation of the joining node that the joining
	 * process has been finished. It resets flags and return a decision to the local
	 * policy.<br> <br>
	 * 
	 * Occurred in  : The major node 	<br>
	 * Triggered by : JOIN_STEP_11_N_READY notification	<br>
	 * <br>
	 * 
	 * @param notification
	 * @return
	 * @author Hoang-Duong Nguyen
	 */
	public static LocalPolicyResponse finishJoining(
			IcapJoiningNotification notification) {
		
		// Reset flag
		isJoining = false;
		joiningNode = null;
		
		// Return decision
		IcapEnforcementDecision ed = new IcapEnforcementDecision(
				IcapEnforcementDecision.Decision.PERMIT,
				IcapEnforcementDecision.Type.SCALE);

		// Return a decision to itself
		// Bad programming practice !
		return new DelegationLocPolDirectReturn(
				Clicap.getDomain(), Clicap.getRemotePort(),	new IcapDelegationResp(ed));
	}
	
	
	// <--------------------------------------------------------------------------------->
	// 									Auxiliary Methods
	// <--------------------------------------------------------------------------------->

	/**
	 * This method is call by a node that possibly has the joining node in his finger 
	 * table on some particular row in order to ensure if this is really the case. <br> 
	 * If yes, it will update the respective finger table entry, otherwise do nothing 
	 * and informs the joining node to terminate the current updating branch.
	 * 	
	 * @param notification 
	 * 				The notification sent from the previous node on the routing path
	 * @return		true if finger table is updated, otherwise false
	 * @author Hoang-Duong Nguyen
	 */
	private static boolean updateFingerTable(IcapJoiningNotification notification) {
	
		int n = Clicap.getIDnum();
		int currentFinger =  Clicap.fTable.getExtendedNode(notification.getRowNumber());
		int newNode = notification.getSource().id;
		// 	Make the joining node greater than this node
		if(newNode <= n)
			newNode += Clicap.getCapacity();
		
		if (newNode < currentFinger){
			// The joining node should be at the i-th row of the finger table
			// Update finger table and return true
			Clicap.fTable.setNode(notification.getRowNumber(),
					newNode % Clicap.getCapacity());

			// Update Address of the newly added node
			Clicap.addressing.setAddress(Integer.toString(newNode % Clicap.getCapacity()), 
					new InetSocketAddress(notification.getSource().domain, 
							notification.getSource().port));
			return true;
		} else
			// No need to update
			return false;
	}

	/**
	 * Called in the major node in order to correct the collected finger table for the
	 * joining node
	 * 
	 * @author Hoang-Duong Nguyen
	 */
	private static void correctFT(){
	  
		// Traverse backwards the extended finger table and check for invalid entry
		for (int i = fingerTable.size()-1; i >= 0; i--){
			
			int entry = fingerTable.get(i).id;
			
			if(entry <= joiningNode.id)
				entry += Clicap.getCapacity();
	
			if(entry < (joiningNode.id + Math.pow(2, i))){
				// The current entry is smaller than the corresponding target 											// INVARIANT 1 !!!!!
				// => invalid 
				// => replaced with the new node itself
				fingerTable.add(i, joiningNode);
				fingerTable.remove(i+1);
			}
		}
		for(int i = 0; i < fingerTable.size(); i++){
			System.out.println(fingerTable.get(i));
		}					
	}
	
	/**
	 * Pack information of the predecessor of this node into the given joining 
	 * notification. The information to be packed are the ID, host and port of the 
	 * predecessor
	 * @param notif	
	 * 				The given notification
	 */
	private static void setPreInfo(IcapJoiningNotification notif){
		notif.setPredecessor(new Node(Clicap.getPredIDnum(), 
				Clicap.getPredDomain(), Clicap.getPredPort()));
	}
	
	/**
	 * Pack information of the successor of this node into the given joining 
	 * notification. The information to be packed are the ID, host and port of the 
	 * successor
	 * @param notif	
	 * 				The given notification
	 */
	static void setSucInfo(IcapJoiningNotification notif){
		notif.setSuccessor(new Node(Clicap.getSucIDnum(), 
				Clicap.getSucDomain(), Clicap.getSucPort()));
	}
		
	/**
	 * Pack information of this node into the given joining notification. The information 
	 * to be packed are the ID, host and port of this current node
	 * @param notif	
	 * 				The given notification
	 */
	static void setSrcInfo(IcapJoiningNotification notif){
		notif.setSource(new Node(Clicap.getIDnum(), 
				Clicap.getDomain(), Clicap.getRemotePort()));
	}

	/**
	 * Check if this node is the successor of the joining node.<br>
	 * 
	 * @return true if this is the successor, false otherwise
	 */
	public static boolean isSuccessor() {
		return tmpPred != null; 
	}

	/**
	 * Check if this is the joining node<br>
	 * 
	 * @return true if this is the leaving node, false otherwise
	 */
	public static boolean isJoiningNode() {
		
		return isJoiningNode;
	}
	
	/**
	 * Assumes that all needed information is available.
	 * Returns a list containing all parameters that have been passed to the
	 * java command in order to start the new node. Expects the variables
	 * cmd to contain all parameters independent of the concrete node e.g.,
	 * the path to the jar file.
	 * 
	 * @param newNode
	 * 				The node object that represents the joining node
	 * @param nnPred
	 * 				The node object that represents the predecessor of the joining node
	 * @param nnSuc
	 * 				The node object that represents the successor of the joining node
	 * @return
	 * 		a list of arguments for the instantiating command
	 * 
	 * @author Tobias Reinhard
	 */
	protected static List<String> getNewNodeLaunchingCommand(
			Node newNode, Node nnPred, Node nnSuc) {
		
		LinkedList<String> cmd = new LinkedList<String>();

		// absolute path to the CliCap jar file
		String clicapJarPath = Clicap.class.getProtectionDomain().getCodeSource()
				.getLocation().getFile();

		cmd.add(Clicap.JAVA_EXECUTABLE_PATH);

		cmd.add("-server");
		cmd.add("-Xms256m");
		cmd.add("-Xmx256m");
		cmd.add("-Djava.awt.headless=true");
		cmd.add("-Djava.net.preferIPv4Stack=true");
		cmd.add("-jar");
		cmd.add(clicapJarPath);

		cmd.add("0"); // is central node
		cmd.add("1"); // is ready

		String cordinatorPort = Integer.toString(newNode.port - 2);
		String enforcerPort = Integer.toString(newNode.port - 1);
		String remotePort = Integer.toString(newNode.port);

		cmd.add(Integer.toString(newNode.id));

		cmd.add(cordinatorPort);
		cmd.add(enforcerPort);
		cmd.add(remotePort); // new node's port
		cmd.add(newNode.domain);

		cmd.add(Integer.toString(nnPred.id));
		cmd.add(nnPred.domain);
		cmd.add(Integer.toString(nnPred.port));

		cmd.add(Integer.toString(nnSuc.id));
		cmd.add(nnSuc.domain);
		cmd.add(Integer.toString(nnSuc.port));

		cmd.add(Integer.toString(Clicap.getBitLength()));

		int ftSize = Clicap.getBitLength();
		for (int i = 0; i < ftSize; i++) {
			Node ftEntry = fingerTable.get(i);

			cmd.add(Integer.toString(ftEntry.id));
			cmd.add(ftEntry.domain);
			cmd.add(Integer.toString(ftEntry.port));
		}
		return cmd;
	}
}