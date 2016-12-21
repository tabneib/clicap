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

package cliseau.central.delegation;

/**
 * Enum to name the notification messages during the joining process.
 * @author Hoang-Duong Nguyen, Tobias Reinhard
 *
 */
public enum Notification {
	
	 
	// <------------------------------------------------------------------------------>
	// 									JOINING PROTOCOL
	// <------------------------------------------------------------------------------>

	/**
	 * Send in step 2.a) of the Joining Protocol, forwarded in step 2.b), received in step 3.
	 */
	JOIN_SUC_PRED_REQ,	
	/**
	 * Send in step 3 of the Joining Protocol, received in step 4.a).
	 */
	JOIN_SUC_PRED_RESP,
	
	/**
	 * Send in step 4.b) of the Joining Protocol, forwarded in step 4.c), received in step 5).
	 */
	JOIN_FINGER_TABLE_ENTRY_REQ,
	/**
	 * Send in step 5) of the Joining Protocol, received in step 6).
	 */
	JOIN_FINGER_TABLE_ENTRY_RESP,
	
	JOIN_NEW_PRED_NOTIFICATION,
	
	/**
	 * Send in step 11 of the Joining Protocol, received in step 12.
	 */
	STABILIZATION_REQ,				// step 11
	/**
	 * Send in step 12, received in step 13.
	 */
	STABILIZATION_RESP,				// step 12
	
	/**
	 * Send in step 13 and step 14 of the Joining Protocol, received in step 14 and 15.
	 */
	STABILIZATION_NEW_PRED_NOTIFICATION, // step 13 & 14
	
	STABILIZATION_SUC_UPDATE_CONFIRMATION,
	
	/**
	 * Send in step 15 of the Joining Protocol, received in step 16.
	 */
	DATA_EXCHANGE,	// step 15
	
//	PREDECESSOR_READY,		
//	DATA_REQUEST,			
//	SUCCESSOR_READY,
	SUCCESSOR_STATUS_REQUEST,
	SUCCESSOR_READY_CONFIRMATION,
	NEW_NODE_READY_NOTIFICATION,
	
	STABILIZATION_TURN_ON_REQ,
	STABILIZATION_TURN_ON_CONFIRMATION,
	
	// <------------------------------------------------------------------------------>
	// 									LEAVING PROTOCOL
	// <------------------------------------------------------------------------------>

	LEAVE_STEP_2B_QUERY_REQ,			
	LEAVE_STEP_3_SEND_DATA_TO_SUC,			
	LEAVE_STEP_4_SUC_RESP,		

	LEAVE_STEP_5A_PRED_QUERY,
	LEAVE_STEP_5B_PRED_FOUND,
	LEAVE_STEP_5C_UPDATE_FT,
	LEAVE_STEP_5D_BRANCH_TERMINATED,
	
	LEAVE_STEP_6_NOTIFY_PRED,		
	LEAVE_STEP_7_PRED_READY,
	LEAVE_STEP_8_GOOD_BYE_SUC,
	LEAVE_STEP_9_SUC_READY,
	LEAVE_STEP_10_LEAVING_NODE_READY,
	LEAVE_STEP_11_KILL,
	
	// <------------------------------------------------------------------------------>
	// 									JOINING PROTOCOL 2
	// <------------------------------------------------------------------------------>

	JOIN_STEP_2A_FT_ENTRY_REQ,
	JOIN_STEP_2B_FT_ENTRY_RESP,
	JOIN_STEP_3_SUC_QUERY,
	JOIN_STEP_4_SUC_RESP,
	JOIN_STEP_6_SUC_NOTIFY_INSTANTIATED,
	JOIN_STEP_7_SUC_SEND_DATA,
	JOIN_STEP_8_PRED_NOTIFY,
	JOIN_STEP_9_PRED_READY,
	
	JOIN_STEP_10A_PRED_QUERY,
	JOIN_STEP_10B_PRED_FOUND,
	JOIN_STEP_10C_UPDATE_FT,
	JOIN_STEP_10D_BRANCH_TERMINATED,

	JOIN_STEP_11_N_READY	
}