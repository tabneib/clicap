/* Copyright (c) 2011-2014 Richard Gay <gay@mais.informatik.tu-darmstadt.de>
 *
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
package cliseau.javacor;

import cliseau.javacor.DelegationReqResp;

/**
 * Local policy response type for delegation requests and responses.
 *
 * Objects of the DelegationLocPolReturn type are supposed to be returned by
 * instantiations of a LocalPolicy method whenever it wants to return a
 * delegation request or delegation response. Together with the actual
 * DelegationReqResp object (which comprises the respective delegation request
 * or, respectively, response) every DelegationLocPolReturn object also
 * encapsulated a destination identifier. The latter identifies the next CliSeAu
 * unit to which the delegation request/response object is supposed to be
 * sent.
 *
 * @see DelegationReqResp
 * @see LocalPolicy
 * @see LocalPolicyResponse
 */
public final class DelegationLocPolReturn implements LocalPolicyResponse {
	/** Identifier of the destination CliSeAu unit for the DelegationReqResp */
	private String destinationID;

	/** Delegation request or delegation response */
	private DelegationReqResp dr;

	/**
	 * Construct a DelegationLocPolReturn object.
	 * @param destinationID Destination identifier for the DelegationReqResp
	 * @param dr Delegation request/response object
	 */
	public DelegationLocPolReturn(String destinationID, DelegationReqResp dr) {
		this.destinationID = destinationID;
		this.dr            = dr;
	}

	/**
	 * Obtain the destination identifier for the object.
	 * @return Destination identifier
	 */
	public String getDestinationID() { return destinationID; }

	/**
	 * Set destination identifier.
	 * @param destinationID New destination identifier
	 */
	public void setDestinationID(final String destinationID) {
		this.destinationID = destinationID;
	}

	/**
	 * Obtain delegation request/response for the object.
	 * @return Delegation request/response
	 */
	public DelegationReqResp getDR() { return dr; }

	/**
	 * Set delegation request/response.
	 * @param dr Delegation request/response
	 */
	public void setDR(final DelegationReqResp dr) { this.dr = dr; }
}

