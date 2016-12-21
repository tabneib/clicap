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
package cliseau.javacor;

import cliseau.javacor.DelegationReqResp;

/**
 * Local policy response type for direct delegation responses.
 *
 * Objects of the DelegationLocPolDirectReturn type are supposed to be returned by
 * instantiations of a LocalPolicy method whenever it wants to return a
 * delegation response directly to the unit that initially request for the decision.
 * Together with the actual DelegationReqResp object (which comprises the respective 
 * delegation response) every DelegationLocPolDirectReturn object also encapsulated 
 * the domain and port of the destination. 
 * @author Hoang-Duong Nguyen
 *
 */
public final class DelegationLocPolDirectReturn implements LocalPolicyResponse {
	/**Domain of the destination CliSeAu unit for the DelegationReqResp */
	private String destDomain;
	/**Port of the destination CliSeAu unit for the DelegationReqResp */
	private int destPort;

	/** Delegation response */
	private DelegationReqResp dr;

	/**
	 * Construct a DelegationLocPolDirectReturn object.
	 * @param destDomain Domain of the destination for the DelegationReqResp
	 * @param destPort	 Port of the destination for the DelegationReqResp
	 * @param dr Delegation response object
	 */
	public DelegationLocPolDirectReturn(String destDomain, int destPort, DelegationReqResp dr) {
		this.destDomain = destDomain;
		this.destPort	= destPort;
		this.dr         = dr;
	}

	/**
	 * Obtain the destination domain for the object.
	 * @return Destination domain
	 */
	public String getDestinationDomain() { return destDomain; }

	/**
	 * Set destination domain.
	 * @param destDomain New destination domain
	 */
	public void setDestinationID(final String destDomain) {
		this.destDomain = destDomain;
	}
	
	/**
	 * Obtain the destination port for the object.
	 * @return Destination port
	 */
	public int getDestinationPort() { return destPort; }

	/**
	 * Set destination port.
	 * @param destPort New destination port
	 */
	public void setDestinationPortID(final int destPort) {
		this.destPort = destPort;
	}

	/**
	 * Obtain delegation request/response for the object.
	 * @return Delegation response
	 */
	public DelegationReqResp getDR() { return dr; }

	/**
	 * Set delegation request/response.
	 * @param dr Delegation response
	 */
	public void setDR(final DelegationReqResp dr) { this.dr = dr; }
}