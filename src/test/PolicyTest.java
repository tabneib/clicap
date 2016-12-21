package test;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cliseau.central.IcapEnforcementDecision;
import cliseau.central.event.IcapEvent;
import cliseau.central.event.IcapEventType;
import cliseau.central.policy.IcapPolicy;
import cliseau.javacor.LocalPolicyResponse;

/**
 * A unit test that checks if the IcapPolicy enforces the policy correctly.
 * 
 * @author Tobias Reinhard
 *
 */
public class PolicyTest {
	IcapPolicy policy;


	@Before
	public void setUp() throws Exception {
		policy = new IcapPolicy("0", true);
	}

	/**
	 * Checks if events irrelevant for the security automaton are allowed
	 * independent of their content.
	 */
	@Test
	public void allowIrrelevantEvents() {
		policy = new IcapPolicy("0", true);
		IcapEvent login = new IcapEvent(IcapEventType.LOG_IN_C, null, null,
				null, null, null);
		IcapEvent logout = new IcapEvent(IcapEventType.LOG_OUT_C, null, null,
				null, null, null);
		IcapEvent process = new IcapEvent(IcapEventType.PROCESS_ORDER_C, null,
				null, null, null, null);
		IcapEvent success = new IcapEvent(IcapEventType.SUCCESSFUL_ORDER_C,
				null, null, null, null, null);

		boolean ret1 = policy.isLegalEvent(login);
		boolean ret2 = policy.isLegalEvent(logout);
		boolean ret3 = policy.isLegalEvent(process);
		boolean ret4 = policy.isLegalEvent(success);

		boolean retVal = ret1 && ret2 && ret3 && ret4;
		assertTrue(retVal);
	}
	
	/**
	 * Checks if no token may be used more than once
	 */
	@Test
	public void rejectTokenReuse() {
		policy = new IcapPolicy("0", true);
		String token = "1234";
		IcapEvent establishToken = new IcapEvent(IcapEventType.TOKEN_ESTABLISH_S, null, null,
				null, token, null);
		IcapEvent useToken1 = new IcapEvent(IcapEventType.RECEIVE_PAYER_ID_C, null, null,
				null, token, null);
		IcapEvent useToken2 = new IcapEvent(IcapEventType.RECEIVE_PAYER_ID_C, null, null,
				null, token, null);

		boolean ret1 = policy.acceptEvent(establishToken);
		boolean ret2 = policy.acceptEvent(useToken1);
		boolean ret3 = policy.acceptEvent(useToken2);
		boolean isReuseDenied = !ret3;

		assertTrue(isReuseDenied);
	}
	
	/**
	 * Checks if normal token use is allowed.
	 */
	@Test
	public void allowTokenUse() {
		policy = new IcapPolicy("0", true);
		String token = "1234";
		IcapEvent establishToken = new IcapEvent(IcapEventType.TOKEN_ESTABLISH_S, null, null,
				null, token, null);
		IcapEvent useToken1 = new IcapEvent(IcapEventType.RECEIVE_PAYER_ID_C, null, null,
				null, token, null);

		boolean ret1 = policy.acceptEvent(establishToken);
		boolean ret2 = policy.acceptEvent(useToken1);

		assertTrue(ret2);
	}

	
}