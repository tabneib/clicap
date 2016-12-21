package test;

import java.awt.Point;

import cliseau.central.IcapEnforcementDecision;
import cliseau.central.event.IcapEvent;
import cliseau.central.event.IcapEventType;
import cliseau.central.policy.IcapPolicy;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.LocalPolicyResponse;

/**
 * A unit test that checks if the IcapPolicy enforces the policy correctly.
 * 
 * @author Tobias Reinhard
 * 
 */
public class MappingTest {
	/**
	 * An instance of the security automaton enforces the policy.
	 */
	protected IcapPolicy policy;
	
	/**
	 * Stores the id of the current IcapPolicy instance
	 */
	protected String id;
	
	/**
	 * An id for testing.
	 */
	protected String id1 = "37";

	/**
	 * An id for testing. 
	 */
	protected String id2 = "51";
	
	/**
	 * An critical event which will be mapped to a value equal to the id of
	 * the current IcapPolicy instance.
	 */
	protected IcapEvent ce1;
	
	/**
	 * An critical event which will be mapped to a value equal to the key 60.
	 */
	protected IcapEvent ce2;
	
	/**
	 * An critical event which will be mapped to a value equal to the key 61.
	 */
	protected IcapEvent ce3;
	
	/**
	 * An critical event which will be mapped to a value equal to the key 10.
	 */
	protected IcapEvent ce4;

	public MappingTest(IcapPolicy policy) {
		this.policy = policy;
		this.id = policy.getIdentifier();

		ce1 = createTokenEvent(hashToString(Integer.parseInt(id)));
		ce2 = createTokenEvent(hashToString(Integer.parseInt("60")));
		ce3 = createTokenEvent(hashToString(Integer.parseInt("61")));
		ce4 = createTokenEvent(hashToString(10));

		policy.localRequest(ce1);
	}

	/**
	 * Tests if the critical event ce1 is delegated to the tested unit itself
	 * i.e., no routing.
	 * 
	 * @return The test result.
	 */
	protected boolean testRouting1() {

		LocalPolicyResponse resp = policy.localRequest(ce1);
		return (resp instanceof IcapEnforcementDecision);
		// true if no delegation is needed
	}

	/**
	 * Tests if the critical event requires routing and the mapping from
	 * critical events to integers returns the id of some CliSeAu unit.
	 * 
	 * @return The test result.
	 */
	protected boolean testRouting2() {
		LocalPolicyResponse resp = policy.localRequest(ce2);
		if (!(resp instanceof DelegationLocPolReturn)) {
			System.err.println("no delegation");
			System.err.println(resp.getClass().toString());
			System.err.flush();
			return false;
		}

		DelegationLocPolReturn locPolRet = (DelegationLocPolReturn) resp;
		String destID = locPolRet.getDestinationID();
		System.err.println("Delegated to " + destID);
		if (!isInstanceID(destID)) {
			System.err.println("not delegated to instance");
			return false;
		}

		int mapping = policy.hash(ce2, 6);
		if (!isInstanceID(mapping)) {
			System.err.println("not hashed to instance ID");
			return false;
		}

		return true;
	}

	/**
	 * Tests if the critical event requires routing and the mapping from
	 * critical events to integers does not return the id of some CliSeAu unit.
	 * 
	 * @return The test result.
	 */
	protected boolean testRouting3() {
		LocalPolicyResponse resp = policy.localRequest(ce3);
		if (!(resp instanceof DelegationLocPolReturn))
			return false;

		DelegationLocPolReturn locPolRet = (DelegationLocPolReturn) resp;
		String destID = locPolRet.getDestinationID();
		if (!isInstanceID(destID))
			return false;
		System.err.println("Delegated to " + destID);

		int mapping = policy.hash(ce3, 6);
		System.err.println("Mapped to " + mapping);
		if (isInstanceID(mapping))
			return false;

		return true;
	}

	/**
	 * Tests if the critical event requires routing and if the unit to which the
	 * event delegated has some ID smaller than the ID of this CliSeAu unit.
	 * 
	 * @return The test result.
	 */
	protected boolean testRouting4() {
		LocalPolicyResponse resp = policy.localRequest(ce4);
		if (!(resp instanceof DelegationLocPolReturn))
			return false;

		DelegationLocPolReturn locPolRet = (DelegationLocPolReturn) resp;
		int destID = Integer.parseInt(locPolRet.getDestinationID());
		System.err.println("delegated to " + destID);
		if (!isInstanceID(destID))
			return false;

		System.err.println(destID + " < " + this.id);
		System.err.println(policy.hash(ce4, 6));
		return (destID < Integer.parseInt(this.id));
	}

	/**
	 * Runs all tests and prints the results to the standart error output
	 * stream.
	 */
	public void test() {
		if (!isTestedUnit())
			return;

		System.err.println("\n=============================================");
		boolean res1 = testRouting1();
		System.err.println("unit " + id + " | test 1 succeeded : " + res1);
		boolean res2 = testRouting2();
		System.err.println("unit " + id + " | test 2 succeeded : " + res2);
		boolean res3 = testRouting3();
		System.err.println("unit " + id + " | test 3 succeeded : " + res3);
		boolean res4 = testRouting4();
		System.err.println("unit " + id + " | test 4 succeeded : " + res4);
		System.err.println("=============================================\n\n");
	}

	/**
	 * Returns a TOKEN_ESTABLISH_S event holding the given token.
	 * 
	 * @param token
	 *            The token contained in the returned event.
	 * @return The TOKEN_ESTABLISH_S event holding the given token.
	 */
	protected IcapEvent createTokenEvent(String token) {
		return new IcapEvent(IcapEventType.TOKEN_ESTABLISH_S, null, null, null,
				token, null);
	}

	/**
	 * Checks if this unit shall be tested.
	 * 
	 * @return True if this unit shall be tested, false otherwise.
	 */
	protected boolean isTestedUnit() {
		return policy.getIdentifier().equals(id1)
				|| policy.getIdentifier().equals(id2);
	}

	/**
	 * Returns a string with the given hash code as long as this hash is small.
	 * 
	 * @param hash
	 *            The hash of the returned String.
	 * @return A String with the given hash.
	 */
	protected String hashToString(int hash) {
		char hashChar = (char) hash;
		String str = Character.toString(hashChar);

		return str;
	}

	/**
	 * Checks if the given id is the ID of some clicap instance.
	 * 
	 * @param id
	 *            The id to be checked.
	 * @return True if the given id is the ID of some clicap instance, false
	 *         otherwise.
	 */
	protected boolean isInstanceID(String id) {
		return isInstanceID(Integer.parseInt(id));
	}

	/**
	 * Checks if the given id is the ID of some clicap instance.
	 * 
	 * @param id
	 *            The id to be checked.
	 * @return True if the given id is the ID of some clicap instance, false
	 *         otherwise.
	 */
	protected boolean isInstanceID(int id) {
		switch (id) {
		case 2:
		case 7:
		case 8:
		case 29:
		case 33:
		case 37:
		case 48:
		case 51:
		case 60:
		case 63:
			return true;
		default:
			return false;
		}
	}
}