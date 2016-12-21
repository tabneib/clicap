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
package cliseau.central.event;

/**
 * Enum that presents the type of critical events
 * @author Tobias Reinhard, Hoang-Duong Nguyen
 *
 */
public enum IcapEventType {
	// complex event types
	LOG_IN_C,
	CONFIRM_ORDER_C,
	TOKEN_ESTABLISH_C,
	TOKEN_ESTABLISH_S,
	RECEIVE_PAYER_ID_C,
	PROCESS_ORDER_C,
	SUCCESSFUL_ORDER_C,
	LOG_OUT_C,
	// simplified event types
	//CONFIRM_PAYMENT_C;
	// @NHD this enum only contains type for event of the web application, not the abstract one :D
}