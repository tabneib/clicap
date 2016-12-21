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
package cliseau.javatarget;

import cliseau.javacor.CriticalEvent;
import cliseau.javacor.EnforcementDecision;
import cliseau.javatarget.Enforcer;

/**
 * Factory for enforcer objects.
 *
 * @see Enforcer
 */
public interface EnforcerFactory {
	/**
	 * Create an Enforcer object from an EnforcementDecision.
	 *
	 * @param ed The EnforcementDecision for which to construct an Enforcer.
	 * @return The corresponding Enforcer.
	 * @todo Looks like Java supports static interface methods only from version 7
	 */
	//public static Enforcer fromDecision(final EnforcementDecision ed);

	/**
	 * Create a fallback Enforcer object from a CriticalEvent.
	 *
	 * This method should be called when an Enforcer object is required but an
	 * EnforcementDecision could not be obtained.
	 *
	 * @param ev The CriticalEvent for which to construct a fallback Enforcer.
	 * @return The corresponding Enforcer.
	 * @todo Check whether the EnforcerFactory is the right place to construct
	 *       fallback enforcers; one alternative would be to have a factory
	 *       method for EnforcementDecision objects from CriticalEvent in the
	 *       EnforcementDecision class - however, this would move
	 *       EnforcementDecision from a plain data structure to something
	 *       carryign code.
	 * @todo Looks like Java supports static interface methods only from version 7
	 */
	//public static Enforcer fallback(final CriticalEvent ev);
}
