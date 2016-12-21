package cliseau.central.policy.scaling.joining;

import cliseau.Clicap;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.policy.IcapPolicy;
import cliseau.central.policy.scaling.Node;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.LocalPolicyResponse;

/**
 * The base class for the Joining Protocol i.e., the base class for all classes
 * implementing sub protocols of the Joining Protocol.
 * 
 * @author Tobias Reinhard
 * 
 */
public abstract class JoinintProtocolBase {
	/**
	 * The address information of this node.
	 */
	public static final Node localNode = new Node(Clicap.getIDnum(),
			Clicap.getDomain(), Clicap.getRemotePort());

	/**
	 * A reference to the current security automaton.
	 */
	public static IcapPolicy policy;

	/**
	 * Checks if the implementing class is responsible for the processing of the
	 * given message.
	 * 
	 * @param msg
	 *            The message for which the responsibility is to be checked.
	 * @return True if the implementing class is responsible and false
	 *         otherwise.
	 */
	public abstract boolean isResponsible(IcapJoiningNotification msg);

	/**
	 * For the invocation of this method it is assumed that the implementing
	 * class is responsible for the given message.<br>
	 * <br>
	 * Processes the given message according to the Joining Protocol and returns
	 * the response specified in the Joining Protocol.
	 * 
	 * @param msg
	 *            The message to be processed.
	 * 
	 * @return The response specified in the Joining Protocol.
	 */
	public abstract LocalPolicyResponse process(IcapJoiningNotification msg);
}
