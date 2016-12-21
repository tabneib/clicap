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
 * Data type for delegation requests and responses.
 *
 * This interface defines the data type which represents delegated requests and
 * responses. Such objects are transmitted between the coordinators of different
 * CliSeAu units. Local policies are supposed to return objects of this type
 * (encapsulated in DelegationLocPolReturn objects) whenever the result of a
 * call to the local policy is destined to a remote CliSeAu unit rather
 * than the local one.
 *
 * Currently, the interface does not impose any requirement on implementing
 * classes, i.e., the interface does not contain any methods.
 *
 * For instantiation, one may either define one class each for delegation
 * requests and for delegation responses which directly derive from
 * DelegationReqResp. Alternatively, one can also define a common descendant
 * class (which could, e.g., comprise fields for source and destination
 * identifier) and derive from this the actual delegation request and delegation
 * response classes.
 *
 * @see Coordinator
 * @see DelegationLocPolReturn
 * @see LocalPolicy
 */
public interface DelegationReqResp extends Serializable {
}
