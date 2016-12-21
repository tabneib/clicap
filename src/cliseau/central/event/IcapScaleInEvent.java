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
package cliseau.central.event;

import cliseau.javacor.CriticalEvent;

/**
 * Class that presents an abstract scale-in event used by the formalization
 * @author Hoang-Duong Nguyen
 */
public class IcapScaleInEvent implements CriticalEvent {
	
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The id of the new node.
	 */
	private String newNodeID;
	
	/**
	 * Constructs an event containing the specified id as the id for the new node.
	 * @param newNodeID The id that shall be assigned to the new node.
	 */
	public IcapScaleInEvent(String newNodeID){
		this.newNodeID = newNodeID;
	}
	
	/**
	 * Returns the id of the new node.
	 * @return
	 */
	public String getID(){
		return this.newNodeID;
	}
	
	/**
	 * Return the string that denotes the respective abstract event in the formalization
	 * @return the formal name of the event
	 */
	public String toString(){
		return "SCALE_IN("+newNodeID+")";
	}
}