package cliseau.central.policy.scaling.joining;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cliseau.Clicap;
import cliseau.central.IcapEnforcementDecision;
import cliseau.central.IcapEnforcementDecision.Decision;
import cliseau.central.IcapEnforcementDecision.Type;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.event.IcapScaleOutEvent;
import cliseau.central.policy.scaling.Node;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolDummyReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.EnforcementDecision;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class implements all parts of the Joining Protocol that are definitely
 * executed by the Major Node.
 * 
 * @author Tobias Reinhard
 * 
 */
public class JoiningProtocolMajorNode extends JoinintProtocolBase {

	/**
	 * Stores if a joining process is currently running.
	 */
	boolean isJoining = false;

	/**
	 * Stores the data of the new node that is currently joining, if any.
	 */
	Node newNode = null;
	/**
	 * Direct successor of the new node that shall join the Chord identifier
	 * circle (or future direct successor if the new node has not been
	 * instantiated, yet).
	 */
	Node nnSuc = null;

	/**
	 * Direct predecessor of the new node that shall join the Chord identifier
	 * circle (or future direct predecessor if the new node has not been
	 * instantiated, yet).
	 */
	Node nnPred = null;

	/**
	 * A list that stores the finger table entries of the new node. Note that
	 * due to indices used by the list fingerTable.get(i) will not return the
	 * i-th finger table entry of the new node but the (i-1)-th finger table
	 * entry.
	 */
	List<Node> fingerTable = new LinkedList<Node>();

	/**
	 * Checks if this class is responsible for the processing of the given
	 * message.
	 * 
	 * @param msg
	 *            The message for which the responsibility is to be checked.
	 * @return True if this class is responsible and false otherwise.
	 */
	@Override
	public boolean isResponsible(IcapJoiningNotification msg) {
		switch (msg.type) {
		case JOIN_SUC_PRED_RESP: // step 4.a)
		case JOIN_FINGER_TABLE_ENTRY_RESP: // step 6)
		case NEW_NODE_READY_NOTIFICATION: // step 18)
		{
			return true;
		}
		default:
			return false;
		}
	}

	/**
	 * For the invocation of this method it is assumed that this class is
	 * responsible for the given message.<br>
	 * <br>
	 * Processes the given message according to the Joining Protocol and returns
	 * the response specified in the Joining Protocol.
	 * 
	 * @param msg
	 *            The message to be processed.
	 * 
	 * @return The response specified in the Joining Protocol.
	 */
	@Override
	public LocalPolicyResponse process(IcapJoiningNotification msg) {
		switch (msg.type) {
		// step 4.a) and step 4.b) or step 7)
		case JOIN_SUC_PRED_RESP: {
			return handleSucPredResponse(msg);
		}

		// step 6) and in case finger table is complete step 7.) otherwise step
		// 4.b).
		case JOIN_FINGER_TABLE_ENTRY_RESP: {
			return receiveFingerTableEntryResponse(msg);
		}

		// step 16)
		case NEW_NODE_READY_NOTIFICATION: {
			return receiveNewNodeReadyConfirmation(msg);
		}

		default: {
			signaleUnimplementatedCase(msg.type,
					"process(IcapJoiningNotification)");
			return new DelegationLocPolDummyReturn();
		}
		}
	}

	/**
	 * STEP 1) of the Joining Protocol<br>
	 * <br>
	 * This method is assumed to be called by the Major Node.<br>
	 * <br>
	 * The Major Node receives a request to create a new node. If currently no
	 * node is joining, then the static new node data is set and true is
	 * returned. Otherwise the static data remains untouched and false is
	 * returned.
	 * 
	 * @param nodeCreationRequest
	 *            The request to create a new node.
	 * 
	 * @return True if currently no node is joining i.e., if the given request
	 *         can be processed.
	 * 
	 * @author Tobias Reinhard
	 */
	public boolean receiveJoiningRequest(IcapScaleOutEvent nodeCreationRequest) {
		JoiningProtocolAuxiliary.printStep(1);

		if (!isJoining) {
			setNewNode(nodeCreationRequest);
			isJoining = true;
			return true;
		}

		return false;
	}

	/**
	 * STEP 2.a) of the Joining Protocol<br>
	 * <br>
	 * This method is assumed to be called by the Major Node.<br>
	 * <br>
	 * The Major Node sends a JOIN_SUC_PRED_REQ into the Chord circle and
	 * requests information about the direct successor and predecessor of the
	 * new node.
	 * 
	 * @param nodeCreationRequest
	 *            The request to create a new node that is being processed.
	 * @return The request for the new node's direct successor and predecessor.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapScaleOutEvent is not of type
	 *             JOIN_SUC_PRED_REQ.
	 * 
	 * @author Tobias Reinhard
	 */
	public LocalPolicyResponse sendSucPredRequest(
			IcapScaleOutEvent nodeCreationRequest) {

		JoiningProtocolAuxiliary.printStep("2.a)");

		IcapJoiningNotification sucPredReq = new IcapJoiningNotification(
				Notification.JOIN_SUC_PRED_REQ);
		sucPredReq.setMajorNode(localNode);
		sucPredReq.setNewNode(newNode);

		int forwardID = Integer.parseInt(JoiningProtocolAuxiliary
				.lookup(newNode.id));
		String forwardIDStr = Integer.toString(forwardID);

		// @NHD DEBUGGING BUG?
		/**
		 * TODO What if the next node on the routing path is the major node
		 * itself ? Problem: the CoordinatorAddressing does not contain the port
		 * and the domain of the node itself => One possible solution: adapt the
		 * configuration in Clicap by also add the port and domain of this node
		 * into Addressing
		 */

		DelegationLocPolReturn msg = new DelegationLocPolReturn(forwardIDStr,
				sucPredReq);

		return msg;
	}

	/**
	 * Step 4.a) of the Joining Protocol and Step 4.b) or step 7).<br>
	 * <br>
	 * It is assumed that this method is called by the Major Node.<br>
	 * <br>
	 * Step 4.a)<br>
	 * The Major Node receives the "successor predecessor response" i.e., a
	 * response to its request for information about the new node's future
	 * direct successor and predecessor (i.e. an IcapJoinNotification of type
	 * JOIN_SUC_PRED_RESP).<br>
	 * <br>
	 * Step 4.b)<br>
	 * In case finger tables contain more that one entry in the current setting,
	 * the Major Node sends a request for the new node's second finger table
	 * entry (i.e. an IcapJoinNotification of type JOIN_FINGER_TABLE_ENTRY_REQ)
	 * into the Chord circle. (The first finger table entry has already been
	 * received inform of the current "successor predecessor response".)<br>
	 * <br>
	 * Step 7)<br>
	 * Otherwise the Major Node instantiates the new node and sends a
	 * "new predecessor notification" to the new node's direct successor (i.e.
	 * an IcapJoinNotification of type JOIN_NEW_PRED_NOTIFICATION).<br>
	 * <br>
	 * 
	 * 
	 * @param sucPredResponse
	 * @return
	 * @throws IllegalArgumentException
	 */
	public LocalPolicyResponse handleSucPredResponse(
			IcapJoiningNotification sucPredResponse)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(sucPredResponse,
				Notification.JOIN_SUC_PRED_RESP);

		JoiningProtocolAuxiliary.printStep("4.a");

		this.nnSuc = sucPredResponse.getSuccessor();
		this.nnPred = sucPredResponse.getPredecessor();

		fingerTable.add(this.nnSuc); // first finger table entry is new node's
										// direct successor

		// @NHD So it is crucial that nodes can send message to themselves

		if (isFingerTableComplete()) // step 7)
		{
			return createNewNode();
		} else // step 4.b)
		{
			return sendSingleFingerTableEntryRequest(2);
		}
	}

	/**
	 * Step 6) of the Joining Protocol and in case finger table is complete step
	 * 7.) otherwise step 4.b).<br>
	 * <br>
	 * It is assumed that this method is called by the Major Node.<br>
	 * <br>
	 * 
	 * Step 6):<br>
	 * The Major Node receives a "finger table entry response"
	 * (IcapJoiningNotification object of type JOIN_FINGER_TABLE_ENTRY_RESP). <br>
	 * <br>
	 * Step 7):<br>
	 * In case the finger table entry is complete, the Major Node instantiates
	 * the new node and sends a "new predecessor notification" (i.e.
	 * IcapJoiningNotification object of type JOIN_NEW_PRED_NOTIFICATION) to the
	 * direct successor of the newly instantiated node.<br>
	 * <br>
	 * Step 4.b):<br>
	 * Otherwise the Major Node proceeds with step 4.b) and sends a "finger
	 * table entry request" (i.e. IcapJoiningNotification object of type
	 * JOIN_FINGER_TABLE_ENTRY_RESP) requesting the data for the next finger
	 * table entry.
	 * 
	 * 
	 * @param ftEntryResp
	 *            The received "finger table entry response" containing the data
	 *            for a finger table entry of the new node.
	 * @return In case of step 7, a "new predecessor notification" (i.e.
	 *         IcapJoiningNotification object of type
	 *         NEW_PREDECESSOR_NOTIFICATION) for the direct successor of the new
	 *         node is returned. In case of step 4.b) a finger table entry
	 *         request is returned.
	 * @throws IllegalArgumentException
	 *             Thrown in case the given IcapJoiningNotification object is
	 *             not of type JOIN_FINGER_TABLE_ENTRY_RESP.
	 * 
	 * @param ftEntryResp
	 *            The received "finger table entry response".
	 * @return Either
	 * @throws IllegalArgumentException
	 */
	public LocalPolicyResponse receiveFingerTableEntryResponse(
			IcapJoiningNotification ftEntryResp)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(ftEntryResp,
				Notification.JOIN_FINGER_TABLE_ENTRY_RESP);

		JoiningProtocolAuxiliary.printStep(6);

		int index = ftEntryResp.getFingerTableIndex();

		fingerTable.add(ftEntryResp.getFingerTableEntry());

		if (isFingerTableComplete()) // step 7)
		{
			return createNewNode();
		}

		// step 4.b): request next finger table entry
		return sendSingleFingerTableEntryRequest(index + 1);
	}

	/**
	 * Step 7) of the Joining Protocol.<br>
	 * <br>
	 * It is assumed that this method is called by the Major Node.<br>
	 * <br>
	 * The Major Node instantiates a new node with the static predecessor and
	 * successor information and the static finger table (all information that
	 * has been collected in previous steps). Furthermore it sends a
	 * "new predecessor notification" (i.e. IcapJoiningNotification object of
	 * type JOIN_NEW_PRED_NOTIFICATION) to the direct successor of the new node.
	 * This notification informs the successor that it now should tread the new
	 * node as its temporal predecessor.
	 * 
	 * 
	 * @return A "new predecessor notification" for the direct successor of the
	 *         newly instantiated node.
	 */
	public LocalPolicyResponse createNewNode() {

		JoiningProtocolAuxiliary.printStep(7);

		List<String> cmd = getNewNodeLaunchingCommand();

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.inheritIO();

		try {
			Process p = pb.start();
			// System.out.println("\n\n" + Clicap.getID() + " : "
			// + "started process for new node" + newNode.id + "\n\n");

		} catch (IOException e) {
			e.printStackTrace();
		}

		// clear finger table to allow further joinings
		fingerTable.clear();

		IcapJoiningNotification newPredNotification = createNewPredNotification();

		System.out.println("Major Node sleeping");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Major Node awake, again");

		return new DelegationLocPolDirectReturn(nnSuc.domain, nnSuc.port,
				newPredNotification);
	}

	/**
	 * Step 18) of the Joining Protocol.<br>
	 * <br>
	 * 
	 * This method is assumed to be called by the Major Node.<br>
	 * <br>
	 * 
	 * The Major Node receives a "new node ready notification" (i.e.
	 * IcapJoiningNotification of type NEW_NODE_READY_NOTIFICATION) from the
	 * joining process of the new node is now completed. The Major Node unsets
	 * the isJoining flag in order to allow further joinings.
	 * 
	 * @param newNodeReadyNotif
	 *            The received "new node ready notification".
	 * @return A dummy response without content.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type NEW_NODE_READY_NOTIFICATION
	 */
	public LocalPolicyResponse receiveNewNodeReadyConfirmation(
			IcapJoiningNotification newNodeReadyNotif)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(newNodeReadyNotif,
				Notification.NEW_NODE_READY_NOTIFICATION);
		JoiningProtocolAuxiliary.printStep(16);

		isJoining = false;
		System.out.println("\n\n\n\n\n====> " + Clicap.getSucID()
				+ " : Joining of new node "
				+ newNodeReadyNotif.getNewNode().toString()
				+ " completed SUCCESSFULLY :-)");
		System.out.println("====> " + Clicap.getSucID() + " : isJoining : "
				+ isJoining);

		return new IcapEnforcementDecision(Decision.PERMIT, Type.SCALE);
	}

	// <------------------------------------------------------------------------------>
	// Auxiliary Methods
	// <------------------------------------------------------------------------------>

	/**
	 * This method returns a "finger table entry request" requesting the data
	 * for the new node's i-th finger table entry, where i is the specified
	 * index.
	 * 
	 * @param index
	 *            The index of the finger table entry for which data is
	 *            requested. This index should be in the set {1, ..., ftSize}
	 *            where 'ftSize' is the number of finger table entries that a
	 *            finger table contains in the current setting.
	 * 
	 * @return A "finger table entry request" requesting data for the new node's
	 *         i-th finger table entry. More technically, a delegation object is
	 *         returned that contains a "finger table entry request" i.e., a
	 *         IcapJoiningNotification object of type
	 *         JOIN_FINGER_TABLE_ENTRY_REQ.
	 */
	protected LocalPolicyResponse sendSingleFingerTableEntryRequest(int index) {
		IcapJoiningNotification ftEntryReq = new IcapJoiningNotification(
				Notification.JOIN_FINGER_TABLE_ENTRY_REQ);

		JoiningProtocolAuxiliary.printStep("4.b)");

		ftEntryReq.setFingerTableIndex(index);

		final int key = (newNode.id + (int) Math.pow(2, index - 1))
				% Clicap.getCapacity();
		ftEntryReq.setKey(key);
		ftEntryReq.setMajorNode(localNode);

		System.out.println("generating key :  " + newNode.id + " 2^"
				+ (index - 1) + " % " + Clicap.getCapacity() + "  ==  " + key);
		String destID = JoiningProtocolAuxiliary.lookup(key);
		System.out.println("after lookup");
		return new DelegationLocPolReturn(destID, ftEntryReq);
	}

	/**
	 * Assumes that all needed information is available and returns a command
	 * line that can be used to startup a new node.
	 * 
	 * @return A list containing the command used to startup a new node.
	 */
	protected List<String> getNewNodeLaunchingCommand() {
		LinkedList<String> cmd = new LinkedList<String>();

		// absolute path to the clicap jar file
		String clicapJarPath = Clicap.class.getProtectionDomain()
				.getCodeSource().getLocation().getFile();

		cmd.add(Clicap.JAVA_EXECUTABLE_PATH);

		cmd.add("-server");
		cmd.add("-Xms256m");
		cmd.add("-Xmx256m");
		cmd.add("-Djava.awt.headless=true");
		cmd.add("-Djava.net.preferIPv4Stack=true");
		cmd.add("-jar");
		cmd.add(clicapJarPath);

		cmd.add("0"); // is central node
		cmd.add("0"); // is ready

		extendBaseCmd(cmd);

		return cmd;
	}

	/**
	 * Returns a list containing all parameters that have been passed to the
	 * java command in order to start the new node. Expects the variables
	 * baseCmd to contain all parameters independent of the concrete node e.g.,
	 * the path to the jar file.
	 * 
	 * @param baseCmd
	 *            A command line setting all basic parameters of for the new
	 *            node's startup i.e., id ports, etc.
	 */
	protected void extendBaseCmd(List<String> baseCmd) {
		String cordinatorPort = Integer.toString(newNode.port - 2);
		String enforcerPort = Integer.toString(newNode.port - 1);
		String remotePort = Integer.toString(newNode.port);

		baseCmd.add(Integer.toString(newNode.id));

		baseCmd.add(cordinatorPort);
		baseCmd.add(enforcerPort);
		baseCmd.add(remotePort); // new node's port
		baseCmd.add(newNode.domain);

		baseCmd.add(Integer.toString(nnPred.id));
		baseCmd.add(nnPred.domain);
		baseCmd.add(Integer.toString(nnPred.port));

		baseCmd.add(Integer.toString(nnSuc.id));
		baseCmd.add(nnSuc.domain);
		baseCmd.add(Integer.toString(nnSuc.port));

		baseCmd.add(Integer.toString(Clicap.getBitLength()));

		int ftSize = Clicap.getBitLength();
		for (int i = 0; i < ftSize; i++) {
			Node ftEntry = fingerTable.get(i);

			baseCmd.add(Integer.toString(ftEntry.id));
			baseCmd.add(ftEntry.domain);
			baseCmd.add(Integer.toString(ftEntry.port));
		}
	}

	/**
	 * Sets the new node to the value contained in the scale out event.
	 * 
	 * @param newNodeCreationReq
	 *            The scale out event that contains the data for the new node.
	 */
	protected void setNewNode(IcapScaleOutEvent newNodeCreationReq) {
		int newNodID = Integer.parseInt(newNodeCreationReq.getID());
		String newNodeDomain = newNodeCreationReq.getDomain();
		int newNodePort = newNodeCreationReq.getPort();

		this.newNode = new Node(newNodID, newNodeDomain, newNodePort);
	}

	/**
	 * Sets the Major Node field of the given notification to a value
	 * representing this node (the Major Node).
	 * 
	 * @param notification
	 *            The notification whose Major Node field shall be set.
	 */
	protected void setMajorNode(IcapJoiningNotification notification) {
		Node localNode = new Node(Clicap.getIDnum(), Clicap.getDomain(),
				Clicap.getRemotePort());
		notification.setMajorNode(localNode);
	}

	/**
	 * Checks if the data needed to construct the new node's finger table is
	 * complete.
	 * 
	 * @return True if the finger table is complete and false otherwise.
	 */
	protected boolean isFingerTableComplete() {
		return (this.fingerTable.size() >= Clicap.getBitLength());
	}

	/**
	 * Creates a "new predecessor notification" (i.e. IcapJoiningNotification
	 * object of type JOIN_NEW_PRED_NOTIFICATION). This notification contains
	 * the address data of the new node (i.e. id, domain, port) and the address
	 * data of the Major Node (i.e. this node).
	 * 
	 * @return A "new predecessor notification" containing the address data of
	 *         the new node (i.e. id, domain, port) and the address data of the
	 *         Major Node (i.e. this node).
	 */
	protected IcapJoiningNotification createNewPredNotification() {
		IcapJoiningNotification newPredNotification = new IcapJoiningNotification(
				Notification.JOIN_NEW_PRED_NOTIFICATION);
		newPredNotification.setNewNode(newNode);
		newPredNotification.setMajorNode(localNode);

		return newPredNotification;
	}

	/**
	 * Prints out an error message to the standard error stream saying that a
	 * case distinction for the given value is missing in a method of this class
	 * with the specified name.
	 * 
	 * @param caseObj
	 *            The value for which a case distinction is missing.
	 * @param methodName
	 *            The name of the method in which the case distinction is
	 *            missing.
	 */
	protected void signaleUnimplementatedCase(Object caseObj, String methodName) {
		JoiningProtocolAuxiliary.signaleUnimplementatedCase(caseObj, this
				.getClass().getName(), methodName);
	}
}
