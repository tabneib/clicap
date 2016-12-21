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
package cliseau.central;

import cliseau.javacor.EnforcementDecision;

/**
 * Enforcement and management decisions are instances of this class.
 * 
 * @author Hoang-Duong Nguyen
 */
public class IcapEnforcementDecision implements EnforcementDecision {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 4L;

	/**
	 * An enum modeling a rejecting or a permitting decision.
	 */
	public static enum Decision {
		PERMIT, REJECT
	};

	/**
	 * Type of this decision whether it is a decision for security-relevant
	 * request, scaling request, or request for network map printing
	 * 
	 */
	public static enum Type {
		SEC, SCALE, MAP
	};

	// The enforcement decision that this decision carries

	/**
	 * The contained decision.
	 */
	public Decision decision;

	/**
	 * The type of event for which this decision is made.
	 */
	public Type type;

	/**
	 * Construct a new enforcement decision with the given decision and type.
	 * 
	 * @param d
	 *            The decision (reject or permit) contained in the contructed
	 *            enforcement decision.
	 * @param type
	 *            The type of the contructed enforcement decision.
	 */
	public IcapEnforcementDecision(Decision d, Type type) {
		this.decision = d;
		this.type = type;
	}
}