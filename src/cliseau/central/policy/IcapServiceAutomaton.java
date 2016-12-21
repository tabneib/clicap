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
package cliseau.central.policy;

import cliseau.central.event.IcapEvent;


/**
 * This interface should be implemented by every class implementing a service
 * automaton that enforcing some policy on an Icap server.
 * 
 * @author Tobias Reinhard
 *
 */
public interface IcapServiceAutomaton {
	/**
	 * Checks if the given event is legal i.e. checks if the service automaton
	 * is able to make a transition in its current state by receiving this
	 * event.
	 * 
	 * @param ce
	 *            The received event.
	 * @return True if event is legal and false if not i.e. true is returned iff
	 *         the service automaton is able to make a transition in its current
	 *         state by receiving the given event.
	 */
	public boolean isLegalEvent(IcapEvent ce);

	/**
	 * 
	 * Accepts the given event and makes a transition if possible. In case the
	 * service automaton is able to accept the event and to make a transition in
	 * its current state by receiving the this event (i.e. if isLegalEvent(ce)
	 * returns true), then true is returned and false otherwise.
	 * 
	 * @param ce
	 *            The event that shall be accepted.
	 * @return True if the service automaton can accept the given event in its
	 *         current state and make a transition i.e., true is returned iff
	 *         isLegalEvent(ce) returns true.
	 */
	public boolean acceptEvent(IcapEvent ce);
	
	/**
	 * Check if this CliSeAu unit is responsible for the given event
	 * @param ce
	 * 			  The given critical event
	 * @return  True if responsible, otherwise False
	 * 			
	 */
	public boolean isResponsible(IcapEvent ce);
}