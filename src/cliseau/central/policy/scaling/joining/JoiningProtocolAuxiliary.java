package cliseau.central.policy.scaling.joining;

import cliseau.Clicap;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.policy.scaling.Node;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class implements all parts of the Joining Protocol for which it can not
 * be guaranteed that they are executed by a particular node (like the Major
 * Node or the new node that should join the Chord identifier).
 * 
 * @author Tobias Reinhard
 * 
 */
public class JoiningProtocolAuxiliary extends JoinintProtocolBase {
	/**
	 * Stores if this class should print out debugging information.
	 */
	static boolean debugging = false;

	/**
	 * Returns the id of the node to which a data item with the given key should
	 * be delegated.
	 * 
	 * @param key
	 *            The key for which the intended receiver is looked up.
	 * @return The id of the node responsible for a data item with the given
	 *         key.
	 */
	public static String lookup(int key) {
		if (policy.isResponsible(key)) {
			// return Clicap.getSucID();
			return Clicap.getID();
			// return Integer.toString(Clicap.fTable.lookUp(key));
		} else {
			return Integer.toString(Clicap.fTable.lookUp(key));
		}
	}

	/**
	 * Checks if the received notification's type equals the expected one and
	 * throws an IllegalArgumentException if they don't.
	 * 
	 * @param notification
	 *            The notification whose type shall be checked.
	 * 
	 * @param expectedType
	 *            The expected notification type.
	 * 
	 * @throws An
	 *             IllegalArgumentException if the expected notification type
	 *             does not equal the received one.
	 */
	public static void checkNotificationType(
			IcapJoiningNotification notification, Notification expectedType)
			throws IllegalArgumentException {

		if (notification.getType() != expectedType) {
			throw new IllegalArgumentException("Expected notification of type "
					+ expectedType.toString() + " but received"
					+ notification.getType().toString());
		}
	}

	/**
	 * Prints the specified step of the Joining Protocol if this class runs in
	 * debug mode.
	 * 
	 * @param step
	 *            The step to be printed.
	 */
	public static void printStep(String step) {
		if (!debugging) {
			return;
		}

		String localID = Clicap.config.get(Clicap.ID);
		String offset = "\t\t";
		System.out.println(Clicap.getID() + " : " + offset + "node " + localID
				+ " : step " + step);
	}

	/**
	 * Prints the specified step of the Joining Protocol if this class runs in
	 * debug mode.
	 * 
	 * @param step
	 *            The step to be printed.
	 */
	public static void printStep(int step) {
		if (!debugging) {
			return;
		}

		printStep(Integer.toString(step));
	}

	/**
	 * Sets the source field of the given notification to a value representing
	 * this node.
	 * 
	 * @param notification
	 *            The notification object for which the fields sourceDomain,
	 *            sourcePort and sourceID shall be set.
	 */
	protected static void setNotificationSource(
			IcapJoiningNotification notification) {

		Node localNode = new Node(Clicap.getIDnum(), Clicap.getDomain(),
				Clicap.getRemotePort());

		notification.setSource(localNode);
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
	protected static void signaleUnimplementatedCase(Object caseObj,
			String className, String methodName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unimplemented case  ").append(caseObj.toString())
				.append("  in  ").append(className).append(".")
				.append(methodName);

		String errorMessage = sb.toString();

		System.err.println(errorMessage);
	}

	/**
	 * A dummy implementation of the isResponsible method declared by the
	 * Joining Protocol's base class.
	 */
	@Override
	public boolean isResponsible(IcapJoiningNotification msg) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * A dummy implementation of the isResponsible method declared by the
	 * Joining Protocol's base class.
	 */
	@Override
	public LocalPolicyResponse process(IcapJoiningNotification msg) {
		// TODO Auto-generated method stub
		return null;
	}

}
