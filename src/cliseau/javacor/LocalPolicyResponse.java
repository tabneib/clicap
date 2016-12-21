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

import java.io.Serializable;

/**
 * Base type for all types that can be returned from a request to a local policy.
 *
 * The LocalPolicyResponse is a base type for all types that can be returned
 * from a request to a local policy. Currently, this is supposed to be either an
 * EnforcementDecision or a DelegationLocPolReturn (which basically encapsulates
 * a DelegationReqResp together with a destination identifier).
 *
 * Deriving descendant classes from LocalPolicyResponse for instantiation is not
 * intended. If you want custom local policy responses for an instantiation,
 * define descendant classes from EnforcementDecision and DelegationReqResp.
 *
 * @see DelegationLocPolReturn
 * @see EnforcementDecision
 */
public interface LocalPolicyResponse {
}

