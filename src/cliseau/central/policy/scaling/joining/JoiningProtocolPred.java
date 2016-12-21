package cliseau.central.policy.scaling.joining;

import java.net.InetSocketAddress;
import java.util.Set;

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
 * executed by direct predecessor of the new node that should join the Chord
 * identifier circle (or the future direct predecessor as long as the new node
 * has not been instantiated).
 * 
 * @author Tobias Reinhard
 * 
 */
public class JoiningProtocolPred extends JoinintProtocolBase {

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

		case STABILIZATION_RESP: // step 12)
		case STABILIZATION_TURN_ON_REQ: // step 16)
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

		// step 12
		case STABILIZATION_RESP: {
			return updateSuccessor(msg);
		}
		
		// step 16
		case STABILIZATION_TURN_ON_REQ: {
			return receiveStabilTurnOnRequest(msg);
		}

		default: {
			signaleUnimplementatedCase(msg.type, "process(IcapJoiningNotification)");
			return new DelegationLocPolDummyReturn();
		}
		}

	}

	/**
	 * Step 12 in Joining Protocol.<br>
	 * <br>
	 * This node receives a "stabilization response" containing the address data
	 * (i.e. ID, domain, port) of its new successor. (Note that this address
	 * data is stored in the notification's predecessor field. The reason is,
	 * that the sender of this response sends its predecessor's address data.)<br>
	 * 
	 * In case the new successor differs from the current one, this node updates
	 * its successor information (in particular finger table and addressing).
	 * Furthermore it sends a "successor update confirmation" to its new
	 * successor (i.e. IcapJoiningNotification of type
	 * STABILIZATION_SUC_UPDATE_CONFIRMATION). Furthermore the node turns off
	 * its stabilization (i.e. does not perform any stabilization until turned
	 * on again)<br>
	 * 
	 * In case the new successor equals the old one, nothing happens.
	 * 
	 * @param stabilResp
	 * @return
	 * @author Hoang-Duong Nguyen, Tobias Reinhard
	 */
	public LocalPolicyResponse updateSuccessor(IcapJoiningNotification stabilResp)
			throws IllegalArgumentException {

		JoiningProtocolAuxiliary.checkNotificationType(stabilResp,
				Notification.STABILIZATION_RESP);
		JoiningProtocolAuxiliary.printStep(12);
		
		
		/*  @NHD TODO Debugging stuff Remove me !
		System.out.println("suc : " + Clicap.getSucID());
		System.out.println("compare pred " + stabilResp.getPredecessor()
				+ " and local node " + localNode);
		*/
		
		if (stabilResp.getPredecessor().equals(localNode)) {
			// successor is still correct, do nothing
			//System.out.println(localNode + " : succesor still up to date");
			return new DelegationLocPolDummyReturn();

		} else // successor changed => new node occurred => set new node as
				// successor
		{
			
			// @NHD TODO Debugging stuff remove me !
			
			System.out.println(localNode + " : succesor outdated => new successor "
					+ stabilResp.getPredecessor() + " old one " + Clicap.getSucID());
			
			
			
			
			Node newSuccessor = stabilResp.getPredecessor();
			updateSuccessor(newSuccessor);
			PeriodicalChecker.stopStabilization();

			/**
			 * TODO update FingerTable accordingly, and DO-NOT harm the Updating
			 * Algorithm !!!! ;)
			 * 
			 */

			// Notify the new Node
			IcapJoiningNotification newPredConfirmation = new IcapJoiningNotification(
					Notification.STABILIZATION_SUC_UPDATE_CONFIRMATION);
			newPredConfirmation.setSuccessor(localNode);

			return new DelegationLocPolDirectReturn(newSuccessor.domain,
					newSuccessor.port, newPredConfirmation);
		}

	}

	/**
	 * Step 16 in Joining Protocol.<br>
	 * <br>
	 * 
	 * @param stabilResp
	 * @return
	 * @author Tobias Reinhard
	 */
	public LocalPolicyResponse receiveStabilTurnOnRequest(
			IcapJoiningNotification stabilTurnOnReq) throws IllegalArgumentException {
		
		JoiningProtocolAuxiliary.checkNotificationType(stabilTurnOnReq,
				Notification.STABILIZATION_TURN_ON_REQ);
		JoiningProtocolAuxiliary.printStep(16);

		IcapJoiningNotification confirmation = new IcapJoiningNotification(
				Notification.STABILIZATION_TURN_ON_CONFIRMATION);

		return new DelegationLocPolReturn(Clicap.getSucID(), confirmation);
	}

	// <------------------------------------------------------------------------------>
	// Auxiliary Methods
	// <------------------------------------------------------------------------------>

	/**
	 * Sets the given node as new successor. In particular, this method also
	 * updates this node's finger table and addressing.
	 * 
	 * @param newSuccessor
	 *            The node that shall be set as new successor.
	 */
	protected void updateSuccessor(Node newSuccessor) {
		System.out.println(localNode + " : setting suc from " + Clicap.getSucID() + " to "
				+ newSuccessor);
		
		Clicap.fTable.updateSuccessor(newSuccessor.id);
		Clicap.config.put(Clicap.SUCCESSOR, Integer.toString(newSuccessor.id));
		Clicap.config.put(Clicap.SUC_DOMAIN, newSuccessor.domain);
		Clicap.config.put(Clicap.SUC_PORT, Integer.toString(newSuccessor.port));

		Clicap.addressing.setAddress(Integer.toString(newSuccessor.id),
				new InetSocketAddress(newSuccessor.domain, newSuccessor.port));

		Clicap.fTable.log();
		Clicap.fTable.log("new Successor " + Clicap.getSucID());
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
