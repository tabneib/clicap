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

import cliseau.javacor.EnforcementDecision;

/**
 * Interface for enforcer instantiations for Java target programs.
 *
 * Every enforcer instantiation for Java target programs must implement this
 * interface. An enforcer determines (based on a previously received enforcement
 * decision) the code to be performed instead of the intercepted critical event.
 *
 * Java targets are instrumented using AspectJ to include interceptor and
 * enforcer code at all join points matching one out of the given set of
 * pointcuts. For this, "around" advice are used which can make use of the
 * "proceed()" statement to run the intercepted critical event. The code of the
 * enforcer is therefore split into
 * <ul>
 * <li>code to be performed before the critical event,</li>
 * <li>code to be performed after the critical event, and</li>
 * <li>the decision whether to perform or suppress the execution of the critical
 * event itself,</li>
 * <li>code for computing a surrogate return value in case of a suppressed
 * critical event.</li>
 * </ul>
 * Obviously, if the critical event is suppressed, then the distinction of
 * "before" and "after" with respect to the critical event becomes meaningless.
 * However, for the sake of having only a single Enforcer interface, we do not
 * distinguish suppressing and permitting Enforcer classes.
 *
 * @see EnforcerFactory
 * @see net.cliseau.runtime.javacor.EnforcementDecision
 * @todo Simplify the interface to only a single method "run". See the
 *       documentation for details.
 */
public interface Enforcer {
	/**
	 * Method to be executed before the critical event.
	 */
	public void before();

	/**
	 * Determine whether the critical event should be suppressed.
	 *
	 * The critical event that caused the decision to be enforced is
	 * performed if and only if this method returns false.
	 *
	 * @return Boolean value specifying whether the critical event should be suppressed.
	 */
	public boolean suppress();

	/**
	 * Method to be executed after the critical event.
	 */
	public void after();

	/**
	 * Get the surrogate return value for a suppressed critical event.
	 *
	 * When a critical event is suppressed and this event has a return value,
	 * then a replacement value has to be produced and returned by the Enforcer.
	 *
	 * @param c Class of the expected actual return type.
	 * @return The surrogate return value.
	 * @todo Currently, the implementation of return value handling is
	 *       unsatisfactory, because type errors of Enforcer instantiations
	 *       become apparent only at runtime and not at compile time.
	 */
	public Object getReturnValue(Class c);
}

