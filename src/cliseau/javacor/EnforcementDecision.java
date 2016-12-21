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
 * Data type for enforcement decisions.
 *
 * This interface defines the data type which represents enforcement decisions.
 * Objects of this type are transmitted from local policies to coordinators and
 * from there sent to enforcers. For sending such objects, they must be
 * serializable to a form that is understandable by the receiving interceptor.
 *
 * For Java target programs, classes implementing this interface can (and
 * probably should) also be used from within the enforcer. For other target
 * programming languages, the data type for enforcement decisions used by the
 * enforcer should reflect the type used by the coordinator and local policy
 * (e.g., by having the same fields of the same or corresponding types).
 *
 * Currently, the interface does not impose any requirement on implementing
 * classes, i.e., the interface does not contain any methods.
 *
 * @see Coordinator
 * @see LocalPolicy
 * @see LocalPolicyResponse
 */
public interface EnforcementDecision extends Serializable,LocalPolicyResponse {
}

