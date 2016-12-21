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

import cliseau.central.event.IcapEvent;

/**
 * Class represent standard delegation request objects. A standard delegation request 
 * carries a critical event for which enforcement decision is requested. It also contains
 * the host and port of the source server such that  receiver of this request is able
 * to reply directly.
 * @author Hoang-Duong Nguyen
 */
public class IcapDelegationReq extends IcapDelegationReqResp {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The source domain.
	 */
	private final String sourceDomain;
	
	/**
	 * The source port.
	 */
	private final int sourcePort;
	
	/**
	 * The critical event.
	 */
	private final IcapEvent ce;
	
	/**
	 * Create a standard delegation request object that carries the critical event e sent
	 * from the server with host sourceDomain and port sourcePort
	 * @param s the source node of the request
	 * @param e the critical event that this request carries
	 */
	public IcapDelegationReq(String sourceDomain, int sourcePort, IcapEvent e){
		this.sourceDomain = sourceDomain;
		this.sourcePort = sourcePort;
		this.ce = e;
	}
	
	/**
	 * Returns the source domain.
	 * @return The source domain.
	 */
	public String getSourceDomain(){
		return this.sourceDomain;
	}
	
	/**
	 * Returns the source port.
	 * @return The source port.
	 */
	public int getSourcePort(){
		return this.sourcePort;
	}
	
	/**
	 * Returns the event stored by this delegation request.
	 * @return The event stored by this delegation request.
	 */
	public IcapEvent getEvent(){
		return this.ce;
	}
}