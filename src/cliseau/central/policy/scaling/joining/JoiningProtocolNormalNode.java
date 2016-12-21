package cliseau.central.policy.scaling.joining;

import cliseau.Clicap;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.policy.scaling.Node;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolDummyReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class implements all parts of the Joining Protocol for which it can not
 * be guaranteed that they are executed by a particular node (like the Major
 * Node or the new node that should join the Chord identifier).
 * 
 * @author Tobias Reinhard
 * 
 */
public class JoiningProtocolNormalNode extends JoinintProtocolBase {

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
		// check if step is step 2.b)
		case JOIN_SUC_PRED_REQ: {
			Node newNode = msg.getNewNode();
			return !(policy.isResponsible(newNode.id));
		}

		// step 4.c) or 5)
		case JOIN_FINGER_TABLE_ENTRY_REQ: {
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
		// step 2.b)
		case JOIN_SUC_PRED_REQ: {
			return forwardSucPredRequest(msg);
		}

		// step 4.c)
		case JOIN_FINGER_TABLE_ENTRY_REQ: {
			if (!policy.isResponsible(msg.getKey())) // step 4.c)
			{
				return forwardFingerTableEntryRequest(msg);
			}

			// step 5)
			return sendFingerTableEntryResponse(msg);
		}
		default: {
			signaleUnimplementatedCase(msg.type,
					"process(IcapJoiningNotification)");
			return new DelegationLocPolDummyReturn();
		}
		}
	}

	/**
	 * STEP 2.b) of the Joining Protocol.<br>
	 * <br>
	 * This method is assumed to be called by a node that is not the intended
	 * receiver of the given "successor predecessor request" (because this
	 * method is meant for forwarding the request). <br>
	 * <br>
	 * By calling this method, some node receives a request for information
	 * about the new node's direct successor and predecessor (i.e. a
	 * IcapJoiningNotification with type JOIN_SUC_PRED_REQ) send by the Major
	 * Node. As it is assumed that this node is not the future direct successor
	 * of the new node, the received request will be forwarded to a node that
	 * might be the future direct successor of the new node.
	 * 
	 * @param sucPredRequest
	 *            The request sent by the Major Node.
	 * @return A delegation forwarding delegation containing the original
	 *         request.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapScaleOutEvent is not of type
	 *             JOIN_SUC_PRED_REQ.
	 */
	public LocalPolicyResponse forwardSucPredRequest(
			IcapJoiningNotification sucPredRequest)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(sucPredRequest,
				Notification.JOIN_SUC_PRED_REQ);
		JoiningProtocolAuxiliary.printStep("2.b");

		int newNodeID = sucPredRequest.getNewNode().id;
		String forwardID = JoiningProtocolAuxiliary.lookup(newNodeID);

		return new DelegationLocPolReturn(forwardID, sucPredRequest);

	}

	/**
	 * Step 4.c) of the Joining Protocol.<br>
	 * <br>
	 * This method is assumed to be called by a node that is node the intended
	 * receiver the given "finger table entry request" i.e., received by a node
	 * whose ID shall not be the i-th entry of the new node's finger table,
	 * where i is the finger table index contained in the request.<br>
	 * <br>
	 * 
	 * This node receives a "finger table entry request" (i.e.
	 * IcapJoiningNotification object of type JOIN_FINGER_TABLE_ENTRY_REQ) from
	 * the Major Node requesting for the data for the i-th entry of the new
	 * node's finger table.<br>
	 * As this not is assumed to be not the intended receiver of the received
	 * "finger table entry request", it forwards the received request to a node
	 * that might be the intended receiver.
	 * 
	 * @param ftEntryReq
	 *            The received "finger table entry request".
	 * @return A delegation of the received request to another node that might
	 *         be the intended receiver.
	 * 
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type JOIN_FINGER_TABLE_ENTRY_REQ.
	 */
	protected LocalPolicyResponse forwardFingerTableEntryRequest(
			IcapJoiningNotification ftEntryReq) throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(ftEntryReq,
				Notification.JOIN_FINGER_TABLE_ENTRY_REQ);

		JoiningProtocolAuxiliary.printStep("4.c");

		int key = ftEntryReq.getKey();
		// System.out.println(localNode + " : looking up " + key);
		String destID = JoiningProtocolAuxiliary.lookup(key);

		return new DelegationLocPolReturn(destID, ftEntryReq);
	}

	/**
	 * Step 5) of the Joining Protocol.<br>
	 * <br>
	 * This method is assumed to be called by the intended receiver of this
	 * "finger table entry request" i.e., received by the node whose ID shall be
	 * the i-th entry of the new node's finger table, where i is the finger
	 * table index contained in the request.<br>
	 * <br>
	 * 
	 * This node receives a "finger table entry request" (i.e.
	 * IcapJoiningNotification object of type JOIN_FINGER_TABLE_ENTRY_REQ) from
	 * the Major Node requesting for the data for the i-th entry of the new
	 * node's finger table.<br>
	 * In case this node is the intended receiver (i.e. if this node's ID shall
	 * be the i-th entry of the new node's finger table), then this node sends a
	 * "finger table entry response" (i.e. IcapJoinNotification object of type
	 * JOIN_FINGER_TABLE_ENTRY_RESP) back to the Major Node containing the
	 * address data of this node (i.e. ID, domain, port).
	 * 
	 * @param ftEntryReq
	 *            The received finger table entry request.
	 * @return A "finger table entry response" containing the requested
	 *         information.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type JOIN_FINGER_TABLE_ENTRY_REQ.
	 */
	protected LocalPolicyResponse sendFingerTableEntryResponse(
			IcapJoiningNotification ftEntryReq) throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(ftEntryReq,
				Notification.JOIN_FINGER_TABLE_ENTRY_REQ);
		JoiningProtocolAuxiliary.printStep(5);

		int index = ftEntryReq.getFingerTableIndex();
		IcapJoiningNotification ftEntryResp = createNewFingerTableEntryResponse(index);
		Node majorNode = ftEntryReq.getMajorNode();

		return new DelegationLocPolDirectReturn(majorNode.domain,
				majorNode.port, ftEntryResp);

	}

	/**
	 * Step 4.c) or step 5) of the Joining Protocol.<br>
	 * <br>
	 * 
	 * This node receives a "finger table entry request" (i.e.
	 * IcapJoiningNotification object of type JOIN_FINGER_TABLE_ENTRY_REQ) from
	 * the Major Node requesting for the data for the i-th entry of the new
	 * node's finger table.<br>
	 * <br>
	 * 
	 * STEP 4.c)<br>
	 * In case this node is the intended receiver (i.e. if this node's ID shall
	 * be the i-th entry of the new node's finger table), then this node sends a
	 * "finger table entry response" (i.e. IcapJoinNotification object of type
	 * JOIN_FINGER_TABLE_ENTRY_RESP) back to the Major Node containing the
	 * address data of this node (i.e. ID, domain, port).<br>
	 * <br>
	 * 
	 * STEP 5)<br>
	 * Otherwise the request is forwarded to a node that might be the intended
	 * receiver.
	 * 
	 * @param ftEntryReq
	 *            The received "finger table entry request".
	 * @return Either a delegation message to forward the received finger table
	 *         or a "finger table entry response" containing the requested
	 *         information.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type JOIN_FINGER_TABLE_ENTRY_REQ.
	 */
	public LocalPolicyResponse processFingerTableEntryRequest(
			IcapJoiningNotification ftEntryReq) throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(ftEntryReq,
				Notification.JOIN_FINGER_TABLE_ENTRY_REQ);

		int key = ftEntryReq.getKey(); // used to check if this node is the
										// intended receiver

		// check if this node is intended receiver
		if (policy.isResponsible(key)) // step 5): send
										// "finger table entry response"
		{
			return sendFingerTableEntryResponse(ftEntryReq);

		} else // step 4.c): forward "finger table entry request"
		{
			return forwardFingerTableEntryRequest(ftEntryReq);
		}

	}

	// <------------------------------------------------------------------------------>
	// Auxiliary Methods
	// <------------------------------------------------------------------------------>

	/**
	 * Returns a "finger table entry response" containing the given index and the
	 * address data representing this node.
	 *  
	 * @param index The index contained in the returned "finger table entry response".
	 * @return A "finger table entry response" containing the given index and the
	 * address data representing this node.
	 */
	protected IcapJoiningNotification createNewFingerTableEntryResponse(
			int index) {
		IcapJoiningNotification ftEntryResp = new IcapJoiningNotification(
				Notification.JOIN_FINGER_TABLE_ENTRY_RESP);
		ftEntryResp.setFingerTableIndex(index);
		ftEntryResp.setFingerTableEntry(localNode);

		return ftEntryResp;
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
