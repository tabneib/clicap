package cliseau.central.policy.scaling.joining;

import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.event.IcapScaleOutEvent;
import cliseau.central.policy.IcapPolicy;
import cliseau.central.policy.scaling.Node;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class can be used as coordinator for the Joining Protocol.
 * 
 * @author Tobias Reinhard
 * 
 */
public class JoiningProtocol {
	/**
	 * The security automaton currently enforcing the policy.
	 */
	protected static IcapPolicy policy;

	/**
	 * A subprotocol processing all messages that should be received by the
	 * Major Node.
	 */
	protected static JoiningProtocolMajorNode majorNodeProtocol = new JoiningProtocolMajorNode();

	/**
	 * A subprotocol processing all messages that should be received by the new
	 * node.
	 */
	protected static JoiningProtocolNewNode newNodeProtocol = new JoiningProtocolNewNode();

	/**
	 * A subprotocol processing all messages that should be received by the node
	 * that will be the direct predecessor of the new node after joining.
	 */
	protected static JoiningProtocolPred predProtocol = new JoiningProtocolPred();

	/**
	 * A subprotocol processing all messages that should be received by the node
	 * that will be the direct successor of the new node after joining.
	 */
	protected static JoiningProtocolSuc sucProtocol = new JoiningProtocolSuc();

	/**
	 * A subprotocol processing all messages that can be received by a node
	 * without a special role (like the ones described above).
	 */
	protected static JoiningProtocolNormalNode normalNodeProtocol = new JoiningProtocolNormalNode();

	/**
	 * Initializes the reference to the current security automaton.
	 * 
	 * @param policy
	 *            The current security automaton.
	 */
	public static void init(IcapPolicy policy) {
		JoiningProtocol.policy = policy;
		JoinintProtocolBase.policy = policy;
	}

	/**
	 * STEP 1) of the Joining Protocol<br>
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
	public static boolean receiveJoiningRequest(
			IcapScaleOutEvent nodeCreationRequest) {
		return majorNodeProtocol.receiveJoiningRequest(nodeCreationRequest);
	}

	/**
	 * STEP 2.a) of the Joining Protocol<br>
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
	public static LocalPolicyResponse sendSucPredRequest(
			IcapScaleOutEvent nodeCreationRequest) {
		return majorNodeProtocol.sendSucPredRequest(nodeCreationRequest);
	}

	/**
	 * Processes the given messages according to the Joining Protocol and
	 * returns the message specified by the joining protocol. (To be concrete:
	 * For processing, the message is delegated to the the subprotocol that is
	 * responsible for the given message.)
	 * 
	 * @param msg
	 *            The message to be processed.
	 * @return An response according to the Joining Protocol.
	 */
	public static LocalPolicyResponse processMessage(IcapJoiningNotification msg) {
		// System.out.println("\n\n\n");
		if (majorNodeProtocol.isResponsible(msg)) {
			// System.out.println(policy.localNode +
			// " : major node protocol responsible for notification\n\t" +
			// msg.toString());
			return majorNodeProtocol.process(msg);
		}

		if (newNodeProtocol.isResponsible(msg)) {
			// System.out.println(policy.localNode +
			// " : new node protocol responsible for notification\n\t" +
			// msg.toString());
			return newNodeProtocol.process(msg);
		}

		if (predProtocol.isResponsible(msg)) {
			// System.out.println(policy.localNode +
			// " : predecessor protocol responsible for notification\n\t" +
			// msg.toString());
			return predProtocol.process(msg);
		}

		if (sucProtocol.isResponsible(msg)) {
			// System.out.println(policy.localNode +
			// " : successor protocol responsible for notification\n\t" +
			// msg.toString());
			return sucProtocol.process(msg);
		}

		// System.out.println(policy.localNode +
		// " : normal node protocol responsible for notification\n\t" +
		// msg.toString());
		return normalNodeProtocol.process(msg);
	}

	// <------------------------------------------------------------------------------>
	// Auxiliary Methods
	// <------------------------------------------------------------------------------>

	/**
	 * Processes a "successor predecessor request" and returns a "successor
	 * predecessor response" or forwards the request to the intended receiver.
	 * 
	 * @param sucPredRequest
	 *            The request to be processed.
	 * @return A "successor predecessor response" if this node is the intended
	 *         receiver or a delegation forwarding the request otherwise.
	 * @throws IllegalArgumentException
	 *             Thrown if the given IcapJoiningNotification object is not of
	 *             type JOIN_SUC_PRED_REQ.
	 */
	protected static LocalPolicyResponse processSucPredRequest(
			IcapJoiningNotification sucPredRequest)
			throws IllegalArgumentException {
		JoiningProtocolAuxiliary.checkNotificationType(sucPredRequest,
				Notification.JOIN_SUC_PRED_REQ);
		Node newNode = sucPredRequest.getNewNode();

		if (policy.isResponsible(newNode.id)) // step 3)
		{
			return sucProtocol.sendSucPredResponse(sucPredRequest);
		} else // step 2.b)
		{
			return normalNodeProtocol.forwardSucPredRequest(sucPredRequest);
		}
	}
}
