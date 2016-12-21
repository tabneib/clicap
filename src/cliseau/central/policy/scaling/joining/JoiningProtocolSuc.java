package cliseau.central.policy.scaling.joining;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sun.font.CreatedFontTracker;
import cliseau.Clicap;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.policy.IcapPolicy;
import cliseau.central.policy.scaling.LeavingProtocol;
import cliseau.central.policy.scaling.Node;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolDummyReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class implements all parts of the Joining Protocol that are definitely
 * executed by direct successor of the new node that should join the Chord
 * identifier circle (or the future direct successor as long as the new node has
 * not been instantiated).
 * 
 * @author Tobias Reinhard
 * 
 */
public class JoiningProtocolSuc extends JoinintProtocolBase {

	/**
	 * The address data of the temporal predecessor.
	 */
	protected Node tmpPred = null;

	// protected Node pred = new Node(Clicap.getPredIDnum(),
	// Clicap.getPredDomain(),
	// Clicap.getPredPort());

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
		// check if step is step 3)
		case JOIN_SUC_PRED_REQ: {
			// System.out.println(localNode.id + " :\t received event\t"
			// + msg.toString());
			Node newNode = msg.getNewNode();
			return policy.isResponsible(newNode.id);
		}

		case JOIN_NEW_PRED_NOTIFICATION: // step 8)
		case STABILIZATION_REQ: // step 11)
		case SUCCESSOR_STATUS_REQUEST: // step 16)
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
		// step 3)
		case JOIN_SUC_PRED_REQ: {
			return sendSucPredResponse(msg);
		}

		// step 8)
		case JOIN_NEW_PRED_NOTIFICATION: {
			return receiveNewPredNotifAtSuc(msg);
		}

		// step 11)
		case STABILIZATION_REQ: {
			return receiveStabilizationRequest(msg);
		}

		// step 14)
		case SUCCESSOR_STATUS_REQUEST: {
			return receiveSuccessorStatusRequest(msg);
		}

		default: {
			signaleUnimplementatedCase(msg.type, "process(IcapJoiningNotification)");
			return new DelegationLocPolDummyReturn();
		}
		}
	}

	/**
	 * STEP 3) of the Joining Protocol.<br>
	 * <br>
	 * 
	 * This method is assumed to be called by the intended receiver of the given
	 * "successor predecessor request".<br>
	 * <br>
	 * 
	 * By calling this method, some node receives a request for information
	 * about the new node's direct successor and predecessor (i.e. a
	 * IcapJoiningNotification with type JOIN_SUC_PRED_REQ) send by the Major
	 * Node. As it is assumed that this node is the future direct successor of
	 * the new node, a response containing the requested data (i.e. future
	 * direct successor and predecessor of the new node) will be returned (i.e.
	 * type JOIN_SUC_PRED_RESP).
	 * 
	 * @param sucPredRequest
	 *            The request sent by the Major Node.
	 * @return A delegation containing the response with the requested data or a
	 *         forwarding delegation containing the original request.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapScaleOutEvent is not of type
	 *             JOIN_SUC_PRED_REQ.
	 */
	public LocalPolicyResponse sendSucPredResponse(IcapJoiningNotification sucPredRequest)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(sucPredRequest,
				Notification.JOIN_SUC_PRED_REQ);

		JoiningProtocolAuxiliary.printStep(3);

		// @NHD case the successor of the leaving node has been reached

		Node majorNode = sucPredRequest.getMajorNode();

		return new DelegationLocPolDirectReturn(majorNode.domain, majorNode.port,
				createSucPredResponse());
	}

	/**
	 * Step 8) of the Joining Protocol.<br>
	 * <br>
	 * This method is assumed to be called by the instantiated new node's direct
	 * successor.<br>
	 * <br>
	 * The direct successor of the new node receives a
	 * "new predecessor notification" (i.e. IcapJoinnigNotification object of
	 * type JOIN_NEW_PRED_NOTIFICATION). The successor sets the new node as its
	 * temporal predecessor and send a "data exchange" message (i.e.
	 * IcapJoiningNotification object of type DATA_EXCHANGE) to the new node.
	 * This message contains all data items that are currently stored by the
	 * successor but for which the new node is responsible (since its
	 * instantiation).
	 * 
	 * 
	 * @param newPredNotif
	 *            The received "new predecessor notification" containing the
	 *            address data of the instantiated new node.
	 * @return A "data exchange" message containing all data items currently
	 *         stored by this node but for which the new node is responsible
	 *         now.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type JOIN_NEW_PRED_NOTIFICATION.
	 */
	public LocalPolicyResponse receiveNewPredNotifAtSuc(
			IcapJoiningNotification newPredNotif) throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(newPredNotif,
				Notification.JOIN_NEW_PRED_NOTIFICATION);
		JoiningProtocolAuxiliary.printStep(8);

		tmpPred = newPredNotif.getNewNode();
		IcapJoiningNotification dataExchangeMsg = createDataExchangeMsg(newPredNotif
				.getMajorNode());

		return new DelegationLocPolDirectReturn(tmpPred.domain, tmpPred.port,
				dataExchangeMsg);
	}

	/**
	 * Step 11) in current Joining Protocol.<br>
	 * <br>
	 * This node receives a "stabilization request" from another node and sends
	 * back a "stabilization response" (i.e. IcapJoiningNotification object of
	 * type STABILIZATION_RESP). In case this node currently stores some
	 * temporal predecessor, the "stabilization response" contains the address
	 * data of this temporal predecessor (i.e. id, domain, port of the new
	 * node!). Otherwise the response contains the address data of this node.
	 * 
	 * @param stabilReq
	 *            The received "stabilization request".
	 * @return A "stabilization response" containing the address data of either
	 *         this node's the temporal predecessor (if any) or of this node
	 *         itself.
	 * @author Hoang-Duong Nguyen, Tobias Reinhard
	 */
	public LocalPolicyResponse receiveStabilizationRequest(
			IcapJoiningNotification stabilReq) throws IllegalArgumentException {

		// System.out.println(localNode
		// + " : received stabilization request from "
		// + stabilReq.getSource());
		JoiningProtocolAuxiliary.checkNotificationType(stabilReq,
				Notification.STABILIZATION_REQ);
		JoiningProtocolAuxiliary.printStep(11);

		IcapJoiningNotification stabilResp = new IcapJoiningNotification(
				Notification.STABILIZATION_RESP);

		if (tmpPred != null) {
			stabilResp.setPredecessor(tmpPred);
		} else {
			/* @NHD : debugging stuff, Remove me later !!
			if (Clicap.getID().equals("48")) {
				System.out.println("\n\n" + Clicap.getID() + " : the temp PRED is : "
						+ LeavingProtocol.tmpPred);
				System.out.println("\n\n" + Clicap.getID() + " : the  PRED is : "
						+ Clicap.getPredID());
			}
			*/
			
			if (LeavingProtocol.isSuccessor()) {

				//System.out.println("\n\n!!!!!!!!!!!!!!!!!!!!!!!" + Clicap.getID()
				//		+ " : the temp PRED is : " + LeavingProtocol.tmpPred);
				// TODO remove me !

				Node leavingTmpPred = new Node(LeavingProtocol.tmpPred,
						LeavingProtocol.tmpPredDomain, LeavingProtocol.tmpPredPort);

				stabilResp.setPredecessor(leavingTmpPred);
			} else {

				//System.out.println(); TODO remove me !
				/*if (Clicap.getID().equals("48")) {
					System.out.println("JoiningProtocol PRED: " + Clicap.getPredID());
					System.out.println("Clicap PRED: " + Clicap.getPredID());
				}*/

				// stabilResp.setPredecessor(pred);

				stabilResp.setPredecessor(new Node(Integer.parseInt(Clicap.getPredID()),
						Clicap.getPredDomain(), Clicap.getPredPort()));
			}
		}

		Node oldPred = stabilReq.getSource();

		// System.out.println(localNode +
		// " : sending stabilization response to node " + oldPred);
		// System.out.println("\t" + stabilResp);

		return new DelegationLocPolDirectReturn(oldPred.domain, oldPred.port, stabilResp);
	}

	/**
	 * Step 14) of the Joining Protocol.<br>
	 * <br>
	 * 
	 * Assumed to be called by the direct successor of the new node.<br>
	 * <br>
	 * 
	 * The direct successor of the new node receives a
	 * "successor status request" (i.e. IcapJoiningNotification object of type
	 * SUCCESSOR_STATUS_REQUEST) and sends a response back to the new node,
	 * stating that this node is ready. This response is a "ready confirmation"
	 * (i.e. IcapJoiningNotification object of type
	 * SUCCESSOR_READY_CONFIRMATION).
	 * 
	 * @param sucStatucReq
	 *            The received "successor status request".
	 * @return A "ready confirmation" for the new node.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type SUCCESSOR_STATUS_REQUEST.
	 */
	public LocalPolicyResponse receiveSuccessorStatusRequest(
			IcapJoiningNotification sucStatusReq) throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(sucStatusReq,
				Notification.SUCCESSOR_STATUS_REQUEST);
		JoiningProtocolAuxiliary.printStep(14);

		updatePredecessor(sucStatusReq);
		tmpPred = null;

		IcapJoiningNotification sucReadyConfirm = new IcapJoiningNotification(
				Notification.SUCCESSOR_READY_CONFIRMATION);
		sucReadyConfirm.setSuccessor(localNode);

		Node newNode = sucStatusReq.getNewNode();

		return new DelegationLocPolDirectReturn(newNode.domain, newNode.port,
				sucReadyConfirm);
	}

	// <------------------------------------------------------------------------------>
	// Auxiliary Methods
	// <------------------------------------------------------------------------------>

	/**
	 * Creates a "successor predecessor response" (i.e. IcapJoinnigNotification
	 * object of type JOIN_SUC_PRED_RESP) with successor field set to a value
	 * representing this node and with predecessor field set to a value
	 * representing the direct predecessor of this node.
	 * 
	 * @return A IcapJoiningNotification object with successor and predecessor
	 *         field set such that they represent this node and its direct
	 *         predecessor, respectively.
	 */
	protected IcapJoiningNotification createSucPredResponse() {
		IcapJoiningNotification sucPredResp = new IcapJoiningNotification(
				Notification.JOIN_SUC_PRED_RESP);
		sucPredResp.setSuccessor(localNode);

		Node directPred = new Node(Clicap.getPredIDnum(), Clicap.getPredDomain(),
				Clicap.getPredPort());
		sucPredResp.setPredecessor(directPred);

		return sucPredResp;
	}

	/**
	 * Returns the set of data items that should be exchanged with the temporal
	 * predecessor. This set contains exactly those currently stored data items
	 * for which this node loses responsibility after the joining of the
	 * temporal predecessor (i.e. the new node).
	 * 
	 * @return A set of exactly those data items that should be shared with the
	 *         temporal predecessor i.e., the new node.
	 */
	protected Set<String> determineExchangeData() {
		Set<String> exchangeData = new HashSet<String>();
		Iterator<String> it = policy.getData().iterator();

		while (it.hasNext()) {
			String token = it.next();
			if (shouldBeExchanged(token)) {
				exchangeData.add(token);
				it.remove();
			}
		}

		return exchangeData;
	}

	/**
	 * Checks if the given token should be exchanged with the temporal
	 * predecessor (i.e. the new node). Therefore it is assumed that the given
	 * token stems from the data set of this node.
	 * 
	 * @param token
	 *            The token for which it is to be determined if it should be
	 *            exchanged with the temporal predecessor (i.e. new node) or
	 *            not.
	 * @return True iff the given token should be exchanged with the temporal
	 *         predecessor (i.e. new node). Thus it checks if after the temporal
	 *         predecessor / new node joined, the given token does not lie in
	 *         the responsibility of this node anymore.
	 */
	protected boolean shouldBeExchanged(String token) {
		int key = policy.hashToken(token, Clicap.getBitLength());
		int oldPredID = Integer.parseInt(Clicap.getPredID());

		if (oldPredID < localNode.id) {
			return (key <= oldPredID);
		} else {
			return !(oldPredID < key || key <= localNode.id);
		}
	}

	protected IcapJoiningNotification createDataExchangeMsg(Node majorNode) {
		IcapJoiningNotification dataExchangeMsg = new IcapJoiningNotification(
				Notification.DATA_EXCHANGE);

		dataExchangeMsg.setData(determineExchangeData());
		dataExchangeMsg.setMajorNode(majorNode);

		return dataExchangeMsg;
	}

	/**
	 * Receives the given "successor status request" (i.e.
	 * IcapJoiningNotification object of type SUCCESSOR_STATUS_REQUEST) and sets
	 * this node's predecessor to the new node (addressing information is
	 * contained in the received "successor status request".
	 * 
	 * @param sucStatusReq
	 *            The received "successor status request"
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type SUCCESSOR_STATUS_REQUEST.
	 */
	protected void updatePredecessor(IcapJoiningNotification sucStatusReq)
			throws IllegalArgumentException {
		System.out.println(localNode + " : setting pred from " + Clicap.getPredID() + " to "
				+ sucStatusReq.getNewNode());

		JoiningProtocolAuxiliary.checkNotificationType(sucStatusReq,
				Notification.SUCCESSOR_STATUS_REQUEST);

		Node newPred = sucStatusReq.getNewNode();

		Clicap.config.put(Clicap.PREDECESSOR, Integer.toString(newPred.id));
		Clicap.config.put(Clicap.PRE_DOMAIN, newPred.domain);
		Clicap.config.put(Clicap.PRE_PORT, Integer.toString(newPred.port));

		Clicap.fTable.log();
		Clicap.fTable.log("new Predecessor " + Clicap.getPredID());
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
		JoiningProtocolAuxiliary.signaleUnimplementatedCase(caseObj, this.getClass()
				.getName(), methodName);
	}
}
