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

import cliseau.central.IcapEnforcementDecision;

/**
 * Class represent standard delegation response objects. A standard delegation response 
 * carries a enforcement decision for a critical event the node has received. 
 * @author Hoang-Duong Nguyen
 */
public class IcapDelegationResp extends IcapDelegationReqResp{

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The enforcement decision.
	 */
	private final IcapEnforcementDecision ed ;
	
	/**
	 * Constructs a new IcapDelegationResp with the given enforcement decision.
	 * @param ed the enforcement decision that this response carries
	 */
	public IcapDelegationResp(IcapEnforcementDecision ed){
		this.ed = ed;
	}
	
	/**
	 * Returns the stored enforcement decision.
	 * @return The stored enforcement decision.
	 */
	public IcapEnforcementDecision getED(){
		return this.ed;
	}
}