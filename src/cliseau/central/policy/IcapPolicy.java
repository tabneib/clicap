/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of	
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cliseau.central.policy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cliseau.Clicap;
import cliseau.central.IcapEnforcementDecision;
import cliseau.central.delegation.IcapDelegationReq;
import cliseau.central.delegation.IcapDelegationResp;
import cliseau.central.delegation.IcapJoiningNotification;
import cliseau.central.delegation.IcapLeavingNotification;
import cliseau.central.delegation.IcapLocalNotification;
import cliseau.central.delegation.IcapNWMNotification;
import cliseau.central.delegation.Notification;
import cliseau.central.event.IcapEvent;
import cliseau.central.event.IcapEventType;
import cliseau.central.event.IcapNWMapEvent;
import cliseau.central.event.IcapScaleInEvent;
import cliseau.central.event.IcapScaleOutEvent;
import cliseau.central.policy.nwmap.MapGenerator;
import cliseau.central.policy.scaling.LeavingProtocol;
import cliseau.central.policy.scaling.Node;
import cliseau.central.policy.scaling.JoiningProtocol;
import cliseau.javacor.CriticalEvent;
import cliseau.javacor.DelegationLocPolDirectReturn;
import cliseau.javacor.DelegationLocPolDummyReturn;
import cliseau.javacor.DelegationLocPolReturn;
import cliseau.javacor.DelegationReqResp;
import cliseau.javacor.LocalPolicy;
import cliseau.javacor.LocalPolicyResponse;

/**
 * This class implements a CliSeAu policy forbidding pay-for-less attacks that
 * are based on the concept of exchanging a checkout's token (its unique
 * identifier) by the token of an already completed checkout during the payment
 * confirmation.
 * 
 * @author Tobias Reinhard, Hoang-Duong Nguyen
 * 
 */
public class IcapPolicy extends LocalPolicy implements PayForLessAutomaton {

	/**
	 * Node object that represents this CliCap node
	 */
	public final Node localNode = new Node(Clicap.getIDnum(),
			Clicap.getDomain(), Clicap.getRemotePort());

	/**
	 * Stores all queued joining requests i.e., joining requests that could not
	 * be handled, yet, because another joining request is currently proceeded.
	 */
	protected List<IcapEvent> queuedJoiningRequests;

	/**
	 * Stores exactly those tokens that have been generated by the web shop so
	 * far but which have not been used for the confirmation of some client's
	 * order, yet.
	 */
	protected Set<String> validTokens;

	/**
	 * Creates an IcapPolicy with the given identifier. The identifier should be
	 * unique.
	 * 
	 * @param identifier
	 *            The identifier to be used.
	 * @param isReady
	 *            Specify if this node is ready to make enforcement decision or
	 *            not
	 */
	public IcapPolicy(String identifier, boolean isReady) {
		super(identifier);

		this.queuedJoiningRequests = new LinkedList<IcapEvent>();
		this.validTokens = new HashSet<String>();
	}

	// <--------------------------------------------------------------------------------->
	// Handle Local Requests
	// <--------------------------------------------------------------------------------->

	@Override
	/**
	 * @author Tobias Reinhard, Hoang-Duong Nguyen
	 */
	public LocalPolicyResponse localRequest(CriticalEvent ev)
			throws IllegalArgumentException {

		if (!(ev instanceof IcapEvent || ev instanceof IcapScaleOutEvent
				|| ev instanceof IcapScaleInEvent || ev instanceof IcapNWMapEvent))
			throw new IllegalArgumentException(
					"Expected argument of type IcapEvent.");

		if (ev instanceof IcapEvent)
			// Security-relevant event
			return localSecRequest((IcapEvent) ev);
		else {
			if (ev instanceof IcapNWMapEvent)
				return localNWMapRequest();
			else
				// Scaling event
				return localScalingRequest(ev);
		}
	}

	/**
	 * Processes a local security relevant request.
	 * 
	 * @param iEv
	 *            The security relevant event that occurred locally.
	 * @return A response for this event, either a delegation or a decision.
	 * @author Tobias Reinhard, Hoang-Duong Nguyen
	 */
	private LocalPolicyResponse localSecRequest(IcapEvent iEv) {

		int hashValue = hash(iEv, Clicap.getBitLength());
		if (hashValue >= 0)
			System.out.println("» Server " + this.getIdentifier()
					+ " :: Hash value = " + hashValue);

		if (isResponsible(iEv)) {
			System.out.println("» Server " + this.getIdentifier()
					+ " :: Decision made locally");
			// Case this unit is responsible for the given event
			boolean isLegalEvent = acceptEvent(iEv);
			return makeDecision(isLegalEvent);
		} else {
			System.out.println("» Server " + this.getIdentifier()
					+ " :: Request sent.");
			// Finger table look-up
			int nextUnit = Clicap.fTable
					.lookUp(hash(iEv, Clicap.getBitLength()));

			return new DelegationLocPolReturn(Integer.toString(nextUnit),
					new IcapDelegationReq(Clicap.getDomain(),
							Clicap.getRemotePort(), iEv));
		}
	}

	/**
	 * This method identifies the type of the received scaling request and
	 * trigger the corresponding handling.
	 * 
	 * @param iEv
	 *            The given CliCap event
	 * @return the local policy response for the given scaling event
	 * 
	 * @author Hoang-Duong Nguyen
	 */
	private LocalPolicyResponse localScalingRequest(CriticalEvent ev) {

		if (ev instanceof IcapScaleOutEvent) {
			IcapScaleOutEvent sEv = (IcapScaleOutEvent) ev;

			if (JoiningProtocol.isJoining) {
				// Reject the joining request
				System.out
						.println("\n» ERROR: Joining process is still running! \n");
				IcapEnforcementDecision ed = new IcapEnforcementDecision(
						IcapEnforcementDecision.Decision.REJECT,
						IcapEnforcementDecision.Type.SCALE);
				return ed;
			} else {
				JoiningProtocol.isJoining = true;
				// Trigger the leaving protocol
				return JoiningProtocol.startGeneratingFT(
						Integer.parseInt(sEv.getID()), sEv.getDomain(),
						sEv.getPort());
			}
		} else {
			// Leaving - STEP 1
			IcapScaleInEvent sEv = (IcapScaleInEvent) ev;

			if (LeavingProtocol.isLeaving) {
				// Reject the leaving request
				System.out
						.println("\n» ERROR: Leaving process is still running! \n");
				IcapEnforcementDecision ed = new IcapEnforcementDecision(
						IcapEnforcementDecision.Decision.REJECT,
						IcapEnforcementDecision.Type.SCALE);
				return ed;
			} else {
				// Trigger the leaving protocol
				return LeavingProtocol
						.startQuery(Integer.parseInt(sEv.getID()));
			}
		}
	}

	/**
	 * This method prepares a network map notification that can collects all
	 * needed information to print out the network map. The notification will then be 
	 * forwarded around the network back to the major node. During the forwarding process 
	 * all needed network data will be stored in this notification.
	 * 
	 * @return the local policy response that encapsulate the generated
	 *         notification
	 * 
	 * @author Hoang-Duong Nguyen
	 */
	private LocalPolicyResponse localNWMapRequest() {

		// Generate the notification for network map request
		IcapNWMNotification nwMapReq = new IcapNWMNotification(Clicap.getID());
		nwMapReq.addToOtherLinks(Clicap.fTable.getOtherLinks());
		nwMapReq.addToMainCircle(Clicap.getID());

		return new DelegationLocPolDirectReturn(Clicap.getSucDomain(),
				Clicap.getSucPort(), nwMapReq);
	}

	// <--------------------------------------------------------------------------------->
	// Handle Local Notifications
	// <--------------------------------------------------------------------------------->

	@Override
	/**
	 * (This is dedicated for step 4 - stabilize) - not used in the current setting
	 * @author Hoang-Duong Nguyen
	 */
	public LocalPolicyResponse localNotify(IcapLocalNotification notif) {

		IcapJoiningNotification stabilReq = new IcapJoiningNotification(
				Notification.STABILIZATION_REQ);
		stabilReq.setSource(localNode);

		return new DelegationLocPolReturn(notif.getDestination(), stabilReq);
	}

	// <--------------------------------------------------------------------------------->
	// Handle Remote Requests
	// <--------------------------------------------------------------------------------->

	@Override
	/**
	 * Upon receiving a delegation request from a remote server, this node determines how
	 * it should reply to this by calling this method.
	 * 
	 * @see cliseau.javacor.Coordinator#handleSocket(ServerSocket,boolean)  <br>
	 * 		step 7c,8c,9c 
	 * @param dr the received delegation request
	 * @return the corresponding local policy response
	 * @author Hoang-Duong Nguyen
	 */
	public LocalPolicyResponse remoteRequest(DelegationReqResp dr)
			throws IllegalArgumentException {

		if (dr instanceof IcapDelegationReq) {
			if (LeavingProtocol.isLeavingNode()) {
				// If this is the leaving node,
				// so forward every delegation request to the successor
				return new DelegationLocPolReturn(Clicap.getSucID(), dr);
			} else
				// Note: the case that this is the successor of the leaving
				// node is handled in the method isResponsible()
				return remoteDelRequest((IcapDelegationReq) dr);
		} else if (dr instanceof IcapDelegationResp)
			return remoteDelResponse((IcapDelegationResp) dr);
		else if (dr instanceof IcapJoiningNotification)
			return joiningNotification((IcapJoiningNotification) dr);
		else if (dr instanceof IcapLeavingNotification)
			return leavingNotification((IcapLeavingNotification) dr);
		else if (dr instanceof IcapNWMNotification)
			return nwMapNotification((IcapNWMNotification) dr);
		else
			throw new IllegalArgumentException(
					"Expected argument must be of type IcapDelegationReqResp.");
	}

	/**
	 * Handling remote delegation requests that are security-relevant.
	 * 
	 * @param dr
	 *            the received
	 * @return
	 * @author Hoang-Duong Nguyen, Tobias Reinhard
	 */

	private LocalPolicyResponse remoteDelRequest(IcapDelegationReq dr) {

		IcapEvent ev = (IcapEvent) (dr).getEvent();

		if (isResponsible(ev)) {
			System.out.println("» Server " + this.getIdentifier()
					+ " :: Decision made.");
			// Case this unit is responsible for the event carried by the
			// given delegation request
			boolean isLegalEvent = acceptEvent(ev);
			return new DelegationLocPolDirectReturn(dr.getSourceDomain(),
					dr.getSourcePort(), new IcapDelegationResp(
							makeDecision(isLegalEvent)));
		} else {

			// Not responsible => Finger table look-up
			// then forward to the next unit
			int nextUnit = Clicap.fTable
					.lookUp(hash(ev, Clicap.getBitLength()));
			System.out.println("» Server " + this.getIdentifier()
					+ " :: Request forwarded to SERVER " + nextUnit);
			return new DelegationLocPolReturn(Integer.toString(nextUnit), dr);
		}
	}

	/**
	 * Extracts the decision from a delegation response
	 * 
	 * @param dr
	 *            The delegation response from which the decision shall be
	 *            extracted.
	 * @return The decision contained in the given delegation response.
	 * @author Tobias Reinhard
	 */
	private LocalPolicyResponse remoteDelResponse(IcapDelegationResp dr) {

		System.out.println("» Server " + this.getIdentifier()
				+ " :: Decision received.");
		// Response is sent directly from the responsible unit to the
		// source unit => Just extract and return the decision
		return dr.getED();
	}

	// <------------------------------------------>
	// SCALING PROTOCOL
	// <------------------------------------------>

	/**
	 * Processes a joining notification and calls the executes step from the
	 * Joining Protocol.
	 * 
	 * @param dr
	 *            The joining notification that shall be processed by the
	 *            Joinnig Protocol.
	 * @return The message returned by the Joining Protocol.
	 * @author Hoang-Duong Nguyen, Tobias Reinhard
	 */
	private LocalPolicyResponse joiningNotification(
			IcapJoiningNotification notification) {

		switch (notification.getType()) {

		case JOIN_STEP_2A_FT_ENTRY_REQ:
			return JoiningProtocol.checkFTRequest(notification);

		case JOIN_STEP_2B_FT_ENTRY_RESP:
			return JoiningProtocol.checkFTEntries(notification);

		case JOIN_STEP_3_SUC_QUERY:
			return JoiningProtocol.querySuccessor(notification);

		case JOIN_STEP_4_SUC_RESP:
			return JoiningProtocol.instantiateNode(notification);

		case JOIN_STEP_6_SUC_NOTIFY_INSTANTIATED:
			return JoiningProtocol.deliverData(notification);

		case JOIN_STEP_7_SUC_SEND_DATA:
			return JoiningProtocol.notifyPredecessor(notification);
		case JOIN_STEP_8_PRED_NOTIFY:
			return JoiningProtocol.predecessorReady(notification);

		case JOIN_STEP_9_PRED_READY:
			// Reset row number
			JoiningProtocol.rowNumber = 1;
			return JoiningProtocol.triggerUpdating();

		case JOIN_STEP_10A_PRED_QUERY:
			return JoiningProtocol.checkPredecessor(notification);

		case JOIN_STEP_10B_PRED_FOUND:
			return JoiningProtocol.updateCounterClockwise(notification);

		case JOIN_STEP_10C_UPDATE_FT:
			return JoiningProtocol.updateCounterClockwise(notification);

		case JOIN_STEP_10D_BRANCH_TERMINATED:
			return JoiningProtocol.receiveUpdateConfirmation();

		case JOIN_STEP_11_N_READY:
			return JoiningProtocol.finishJoining(notification);

		default:
			System.err.print("Ivalid notification type!");
			return new DelegationLocPolDummyReturn();
		}

	}

	/**
	 * Processes a leaving notification by executing the corresponding step of
	 * the leaving protocol.
	 * 
	 * @param dr
	 *            The leaving notification that shall be processed.
	 * @return The message returned by the leaving protocol.
	 * @author Hoang-Duong Nguyen
	 */
	private LocalPolicyResponse leavingNotification(
			IcapLeavingNotification notification) {

		switch (notification.getType()) {

		case LEAVE_STEP_2B_QUERY_REQ:
			return LeavingProtocol.query(notification);

		case LEAVE_STEP_3_SEND_DATA_TO_SUC:
			return LeavingProtocol.notifyLeavingNode(notification);

		case LEAVE_STEP_4_SUC_RESP:
			// Reset row number
			LeavingProtocol.rowNumber = 1;
			return LeavingProtocol.triggerUpdating();

		case LEAVE_STEP_5A_PRED_QUERY:
			return LeavingProtocol.checkPredecessor(notification);

		case LEAVE_STEP_5B_PRED_FOUND:
			return LeavingProtocol.updateCounterClockwise(notification);

		case LEAVE_STEP_5C_UPDATE_FT:
			return LeavingProtocol.updateCounterClockwise(notification);

		case LEAVE_STEP_5D_BRANCH_TERMINATED:
			return LeavingProtocol.receiveUpdateConfirmation();

		case LEAVE_STEP_6_NOTIFY_PRED:
			return LeavingProtocol.notifyPredecessor(notification);

		case LEAVE_STEP_7_PRED_READY:
			return LeavingProtocol.predReady(notification);

		case LEAVE_STEP_8_GOOD_BYE_SUC:
			return LeavingProtocol.updateSuccessor(notification);

		case LEAVE_STEP_9_SUC_READY:
			return LeavingProtocol.notifyCentralNode(notification);

		case LEAVE_STEP_10_LEAVING_NODE_READY:
			return LeavingProtocol.killNode(notification);

		case LEAVE_STEP_11_KILL:
			return LeavingProtocol.leave(notification);

		default:
			System.err.print("Ivalid notification type!");
			return new DelegationLocPolDummyReturn();
		}
	}

	/**
	 * Processes a new map notification that is part of the creation process of
	 * the current network layout.
	 * 
	 * @param notification
	 *            The new map notification that shall be processed.
	 * @return The next message for the creation of the network layout
	 * @author Hoang-Duong Nguyen
	 */
	private LocalPolicyResponse nwMapNotification(
			IcapNWMNotification notification) {

		if (notification.wentOneCircle(Clicap.getID())) {
			// If the net work map printing request has run one circle
			// => print out the obtained data and return PERMIT decision
			notification.addToMainCircle(Clicap.getID());
			MapGenerator.generate(notification.getMainCircle(),
					notification.getOtherLinks());

			return new IcapEnforcementDecision(
					IcapEnforcementDecision.Decision.PERMIT,
					IcapEnforcementDecision.Type.MAP);
		} else {
			// The query is not yet finished
			// => add data to the notification and forward it to the successor
			notification.addToMainCircle(Clicap.getID());
			notification.addToOtherLinks(Clicap.fTable.getOtherLinks());
			return new DelegationLocPolDirectReturn(Clicap.getSucDomain(),
					Clicap.getSucPort(), notification);
		}

	}

	// <--------------------------------------------------------------------------------->
	// Auxiliary Methods
	// <--------------------------------------------------------------------------------->

	/**
	 * Checks if the given event is legal i.e. checks if the service automaton
	 * is able to make a transition in its current state by receiving this
	 * event.
	 * 
	 * @param ce
	 *            The received event.
	 * @return True if event is legal and false if not i.e. true is returned iff
	 *         the service automaton is able to make a transition in its current
	 *         state by receiving the given event.
	 * 
	 * @author Tobias Reinhard
	 */
	@Override
	public boolean isLegalEvent(IcapEvent ce) {
		if (ce.type != IcapEventType.TOKEN_ESTABLISH_S
				&& ce.type != IcapEventType.RECEIVE_PAYER_ID_C) {
			return true;
		}

		switch (ce.type) {
		case TOKEN_ESTABLISH_S: {
			return true;
			/*
			 * We assume that this token has been generated by the web store.
			 * Thus if it is not currently used for some checkout, it may not be
			 * a reused token.
			 */
		}

		case RECEIVE_PAYER_ID_C: {
			boolean check = isValidToken(ce.token);
			System.out.println("----->  Valid Token : " + check);
			return check;
			/*
			 * Checks if the token has been generated by the web store and if
			 * the web store is currently waiting for some payment confirmation
			 * involving this token.
			 */
		}
		default: {
			// signaleUnimplementatedCase(ce.type, "isLegalEvent(IcapEvent");
			return true;
		}
		}
	}

	/**
	 * 
	 * Accepts the given event and makes a transition if possible. In case the
	 * service automaton is able to accept the event and to make a transition in
	 * its current state by receiving the this event (i.e. if isLegalEvent(ce)
	 * returns true), then true is returned and false otherwise.
	 * 
	 * @param ce
	 *            The event that shall be accepted.
	 * @return True if the service automaton can accept the given event in its
	 *         current state and make a transition i.e., true is returned iff
	 *         isLegalEvent(ce) returns true.
	 * 
	 * @author Tobias Reinhard
	 */
	@Override
	public boolean acceptEvent(IcapEvent ce) {
		if (!isLegalEvent(ce)) {
			return false;
		}

		// We know that ce is of type TOKEN_ESTABLISH_S or RECEIVE_PAYER_ID_C.

		switch (ce.type) {
		case TOKEN_ESTABLISH_S: {
			markTokenAsValid(ce.token);
			return true;
		}
		case RECEIVE_PAYER_ID_C: {
			markTokenAsInvalid(ce.token);
			return true;
		}

		default: {
			// signaleUnimplementatedCase(ce.type, "acceptEvent(IcapEvent)");
			return true;
		}
		}
	}

	@Override
	/**
	 * Check if this CliSeAu unit is responsible for the given event 
	 * Specified by: isResponsible(...) in IcapServiceAutomaton
	 * @param ce 
	 * 			 The given critical event
	 * @return 
	 * 			 True if responsible, otherwise False
	 * @author Hoang-Duong Nguyen
	 */
	public boolean isResponsible(IcapEvent ce) {

		int hashVal = hash(ce, Clicap.getBitLength());

		if (hashVal < 0)
			return true;
		else {
			// If this node is the predecessor of the leaving node
			// and the leaving process is not yet finished,
			// so this node has to consider tmpPred instead of
			// the leaving node when checking for responsibility.
			int pred = LeavingProtocol.isSuccessor() ? LeavingProtocol.tmpPred
					: Integer.parseInt(Clicap.getPredID());

			if (pred < Integer.parseInt(getIdentifier())) {
				if (hashVal <= Integer.parseInt(getIdentifier())
						&& hashVal > pred)
					return true;
				else
					return false;
			} else {
				if (hashVal <= Integer.parseInt(getIdentifier())
						|| hashVal > pred)
					return true;
				else
					return false;
			}
		}
	}

	/**
	 * Check if this Node is responsible for the given key
	 * 
	 * @param key
	 *            The given data key
	 * @return True if responsible, otherwise False
	 * @author Hoang-Duong Nguyen
	 */
	public boolean isResponsible(int key) {
		int localID = Integer.parseInt(getIdentifier());
		int predID = Integer.parseInt(Clicap.getPredID());

		if (predID < localID) {
			if (predID < key && key <= localID)
				return true;
			else
				return false;
		} else {
			if (predID < key || key <= localID)
				return true;
			else
				return false;
		}
	}

	/**
	 * Checks if the given token is valid i.e., checks if this token has been
	 * established by the web shop but has not been used for some client's
	 * payment confirmation, yet.
	 * 
	 * @param token
	 *            The token to be checked.
	 * @return True if the given token is valid and false otherwise.
	 */
	@Override
	public boolean isValidToken(String token) {
		return this.validTokens.contains(token);
	}

	/**
	 * Marks the given token as valid such that isValidToken will return true
	 * for this token in future. True will be returned if the given token has
	 * not been valid before and false otherwise.
	 * 
	 * <br>
	 * <br>
	 * Note: <br>
	 * According to the definition of 'valid' in the description of this
	 * interface PayForLessAutomaton, exactly those tokens should be marked as
	 * valid that have been generated by the web shop but have not been used to
	 * confirm the payment of some client's order, yet.
	 * 
	 * @param token
	 *            The token to be marked as valid
	 * @return True if the given token has not been valid at the call of this
	 *         method and false otherwise.
	 */
	@Override
	public boolean markTokenAsValid(String token) {
		System.out.println("----->  " + token + " added");
		boolean tmp = validTokens.add(token);
		System.out.println("----->  Valid Tokens :  " + validTokens.toString());
		return tmp;
	}

	/**
	 * Marks the given token as invalid such that isValidToken will return false
	 * for this token in future. True will be returned if the given token has
	 * been valid before and false otherwise.
	 * 
	 * <br>
	 * <br>
	 * Note: <br>
	 * According to the definition of 'valid' in the description of this
	 * interface, exactly those tokens should be marked as valid that have been
	 * generated by the web shop but have not been used to confirm the payment
	 * of some client's order, yet.
	 * 
	 * @param token
	 *            The token to be marked as invalid
	 * @return True if the given token has been valid at the call of this method
	 *         and false otherwise.
	 */
	@Override
	public boolean markTokenAsInvalid(String token) {
		System.out.println("----->  " + token + " removed");
		boolean tmp = validTokens.remove(token);
		System.out.println("----->  Valid Tokens :  " + validTokens.toString());
		return tmp;
	}

	@Override
	public int hash(IcapEvent ev, int capacity) {
		// relevant data of IcapEvent: token
		if (ev.token == null)
			return -1;

		return hashToken(ev.token, capacity); 
	}

	/**
	 * Maps a token to the key that is used to determine the responsible unit
	 * for this token in the Chord identifier circle.
	 * 
	 * @param token
	 *            The token to be mapped to its key.
	 * @param capacity
	 *            The capacity of the Chord identifier circle.
	 * @return The key of the given token.
	 */
	public int hashToken(String token, int capacity) {

		int tokenHash = token.hashCode();
		tokenHash = Math.abs(tokenHash); // just for safety: make it positive.
		final int strictUpperBound = (int) Math.pow(2, capacity); // 2^capacity
		int hash = tokenHash % strictUpperBound;

		return hash;
	}

	/**
	 * Translates the given boolean stating if some event should be permitted or
	 * not into an IcapEnforcementDecision.
	 * 
	 * @param permit
	 *            The information whether the decision should be PERMIT or not.
	 * @return An permitting IcapEnforcementDecision if the given boolean
	 *         parameter was true and a rejecting one otherwise. otherwise.
	 */
	public IcapEnforcementDecision makeDecision(boolean permit) {
		IcapEnforcementDecision.Decision decision;
		if (permit) {
			decision = IcapEnforcementDecision.Decision.PERMIT;
		} else {
			decision = IcapEnforcementDecision.Decision.REJECT;
		}

		return new IcapEnforcementDecision(decision,
				IcapEnforcementDecision.Type.SEC);
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
		StringBuilder sb = new StringBuilder();
		sb.append("Unimplemented case  ").append(caseObj.toString())
				.append("  in  ").append(this.getClass().getName()).append(".")
				.append(methodName);

		String errorMessage = sb.toString();

		System.err.println(errorMessage);
	}

	/**
	 * Obtain the set of tokens that have been generated by the web shop so far
	 * but which have not been used for the confirmation of some client's order,
	 * yet.
	 * 
	 * @return the set of valid tokens
	 */
	public Set<String> getData() {
		return this.validTokens;
	}

	/**
	 * Obtain a sub set of the set of tokens that have been generated by the web
	 * shop so far but which have not been used for the confirmation of some
	 * client's order, yet. This subset contains all requests that are less or
	 * equal than the given key. The subset then will be removed from the token
	 * set maintained by this node.
	 * 
	 * @param key
	 *            The given key
	 * @return the set of valid tokens
	 */
	public Set<String> getPartialData(int key) {
		Set<String> result = new HashSet<String>();
		for (String token : validTokens) {
			if (hashToken(token, Clicap.getCapacity()) <= key) {
				result.add(token);
				validTokens.remove(token);
			}
		}
		return result;
	}

	/**
	 * Update the set of valid tokens
	 * 
	 * @param data
	 *            the given set of tokens for which this policy is now als
	 *            responsible for
	 */
	public void addData(Set<String> data) {
		this.validTokens.addAll(data);
	}
}