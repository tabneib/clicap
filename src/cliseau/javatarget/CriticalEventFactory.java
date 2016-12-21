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

/**
 * Factory for critical event objects.
 *
 * An instantiation (for Java targets) must provide an implementation of a
 * CriticalEventFactory. For each pointcut
 * <em>pointcut pcname(T1 p1, ..., Tn pn)</em>
 * declared for the instantiation, such an implementation must provide a method
 * <em>public static CriticalEvent pcname(T1 p1, ..., Tn pn);</em>. Note that
 * this requirement is currently not reflected in the declaration of the
 * CriticalEventFactory.
 *
 * In simple cases, the implementation of the CriticalEventFactory can at the
 * same time be an implementation of the CriticalEvent interface. However, care
 * has to be taken that the resulting derived classes do not refer to
 * target-specific datatypes which are known to the target program (and thereby
 * to the enforcer and interceptor) but not to the Coordinator. Otherwise the
 * de-serialization of received CriticalEvent streams by the Coordinator fails
 * with a NoClassDefFoundError.
 *
 * @see net.cliseau.runtime.javacor.CriticalEvent
 * @todo Later, a tool could be developed which, from a given pointcut
 *       specification file, generates a stub CriticalEventFactory for
 *       convenience of instantiators.
 */
public interface CriticalEventFactory {
}
