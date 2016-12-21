package cliseau.central.policy.scaling.joining;

import java.io.IOException;

import cliseau.Clicap;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.policy.IcapPolicy;
import cliseau.central.policy.scaling.Node;
import cliseau.central.policy.scaling.PeriodicalChecker;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolDummyReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class implements all parts of the Joining Protocol that are definitely
 * executed by the new node that should join the Chord identifier circle.
 * 
 * @author Tobias Reinhard
 * 
 */
public class JoiningProtocolNewNode extends JoinintProtocolBase {

	/**
	 * The address data of the majoro node.
	 */
	protected Node majorNode = null;

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

		case DATA_EXCHANGE: // step 9)
		case STABILIZATION_SUC_UPDATE_CONFIRMATION: // step 13
		case SUCCESSOR_READY_CONFIRMATION: // step 15)
		case STABILIZATION_TURN_ON_CONFIRMATION: // step 17)
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
		// step 9)
		case DATA_EXCHANGE: {
			return receiveDataExchangeMsgAtNewNode(msg);
		}

		// step 13)
		case STABILIZATION_SUC_UPDATE_CONFIRMATION: {
			return receiveSuccessorUpdateConfirmation(msg);
		}

		// step 15)
		case SUCCESSOR_READY_CONFIRMATION: {
			return receiveSuccessorReadyConfirmation(msg);
		}
		
		// step 17)
		case STABILIZATION_TURN_ON_CONFIRMATION: {
			return receiveSabilizationTurnOnConfirmation(msg);
		}

		default: {
			signaleUnimplementatedCase(msg.type,
					"process(IcapJoiningNotification)");
			return new DelegationLocPolDummyReturn();
		}
		}
	}

	/**
	 * Step 9) of the Joining Protocol.<br>
	 * <br>
	 * This method is assumed to be called by the instantiated new node.<br>
	 * <br>
	 * The new node receives a "data exchange message" from its direct
	 * successor, adds the contained data items (i.e. tokens) to its own set of
	 * data items and also stores the address data of the Major Node (i.e. id,
	 * domain, port).
	 * 
	 * 
	 * @param exchangeDataMsg
	 *            The received "data exchange message".
	 * @return A dummy return without any content.
	 */
	public LocalPolicyResponse receiveDataExchangeMsgAtNewNode(
			IcapJoiningNotification exchangeDataMsg)
			throws IllegalArgumentException

	{
		JoiningProtocolAuxiliary.checkNotificationType(exchangeDataMsg,
				Notification.DATA_EXCHANGE);
		JoiningProtocolAuxiliary.printStep(9);

		policy.addData(exchangeDataMsg.getData());
		majorNode = exchangeDataMsg.getMajorNode();

		return new DelegationLocPolDummyReturn();
	}

	/**
	 * Step 13) of the Joining Protocol.<br>
	 * <br>
	 * This method is assumed to be called by the new node.<br>
	 * <br>
	 * The new node receives a "successor update confirmation" (i.e.
	 * IcapJoiningNotification object of type
	 * STABILIZATION_SUC_UPDATE_CONFIRMATION) stating that its direct
	 * predecessor set the new node as its direct successor. Furthermore the new
	 * node sends a "status request" to its direct successor, requesting if it
	 * is ready.
	 * 
	 * @param sucUpdateConfirm
	 *            The received "successor update confirmation".
	 * @return A "status request" for the new node's direct successor.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapNotification object is not of type
	 *             STABILIZATION_SUC_UPDATE_CONFIRMATION.
	 */
	public LocalPolicyResponse receiveSuccessorUpdateConfirmation(
			IcapJoiningNotification sucUpdateConfirm)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(sucUpdateConfirm,
				Notification.STABILIZATION_SUC_UPDATE_CONFIRMATION);
		JoiningProtocolAuxiliary.printStep(13);

		String sucID = Clicap.getSucID();
		IcapJoiningNotification sucStatusReq = new IcapJoiningNotification(
				Notification.SUCCESSOR_STATUS_REQUEST);
		sucStatusReq.setNewNode(localNode);

		return new DelegationLocPolReturn(sucID, sucStatusReq);

	}

	/**
	 * Step 15) of the Joining Protocol.<br>
	 * <br>
	 * 
	 * Assumed to be called by the new node.<br>
	 * <br>
	 * 
	 * The new node receives a "successor ready confirmation" (i.e.
	 * IcapJoiningNotification object of type SUCCESSOR_STATUS_REQUEST) from its
	 * direct successor and sends a "new node ready notification" (i.e.
	 * IcapJoiningNotification object of type NEW_NODE_READY_NOTIFICATION)to the
	 * Major Node. It sends a "stabilization turn-on request" to its direct
	 * predecessor.
	 * 
	 * @param receivedNewPredNotif
	 * @return
	 * @throws IllegalArgumentException
	 */
	public LocalPolicyResponse receiveSuccessorReadyConfirmation(
			IcapJoiningNotification sucStatusReq)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(sucStatusReq,
				Notification.SUCCESSOR_READY_CONFIRMATION);
		JoiningProtocolAuxiliary.printStep(15);

		PeriodicalChecker.startStabilization();

		IcapJoiningNotification stabilTurnOnReq = new IcapJoiningNotification(
				Notification.STABILIZATION_TURN_ON_REQ);

		//System.out.println(localNode
		//		+ " : send stabilization turn-on request to node "
		//		+ Clicap.getPredID());
		return new DelegationLocPolDirectReturn(Clicap.getPredDomain(),
				Clicap.getPredPort(), stabilTurnOnReq);
	}

	/**
	 * Step 17) of the Joining Protocol.<br>
	 * <br>
	 * 
	 * Assumed to be called by the new node.<br>
	 * <br>
	 * 
	 * The new node receives a "successor ready confirmation" (i.e.
	 * IcapJoiningNotification object of type SUCCESSOR_STATUS_REQUEST) from its
	 * direct successor and sends a "new node ready notification" (i.e.
	 * IcapJoiningNotification object of type NEW_NODE_READY_NOTIFICATION)to the
	 * Major Node.
	 * 
	 * @param receivedNewPredNotif
	 * @return
	 * @throws IllegalArgumentException
	 */
	public LocalPolicyResponse receiveSabilizationTurnOnConfirmation(
			IcapJoiningNotification stabilTurnOnConfirmation)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(stabilTurnOnConfirmation,
				Notification.STABILIZATION_TURN_ON_CONFIRMATION);
		JoiningProtocolAuxiliary.printStep(17);

		IcapJoiningNotification readyNotification = new IcapJoiningNotification(
				Notification.NEW_NODE_READY_NOTIFICATION);
		readyNotification.setNewNode(localNode);

		//System.out.println(localNode + " : received stabilization turn-on confirmation");
		DelegationLocPolDirectReturn ret = null;
		try {
			ret = new DelegationLocPolDirectReturn(majorNode.domain,
					majorNode.port, readyNotification);
		} catch (Exception e) {
			System.out.println(localNode + " : major node is " + majorNode);
			e.printStackTrace();
			System.exit(0);
		}

		return ret;
	}

	// <------------------------------------------------------------------------------>
	// Auxiliary Methods
	// <------------------------------------------------------------------------------>

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
