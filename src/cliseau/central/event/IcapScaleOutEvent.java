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
 * Class that presents an abstract scale-out event used by the formalization
 * @author Hoang-Duong Nguyen
 */
public class IcapScaleOutEvent implements CriticalEvent {
	
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The id of the new node.
	 */
	public String newNodeID;
	
	/**
	 * The new node's domain
	 */
	public String newNodeDomain;
	
	/**
	 * The new node's port.
	 */
	public int newNodePort;
	
	public IcapScaleOutEvent(String newNodeID, String newNodeDomain, int newNodePort){
		this.newNodeID = newNodeID;
		this.newNodeDomain = newNodeDomain;
		this.newNodePort = newNodePort;
	}
	
	/**
	 * Returns the id of the new node.
	 * @return The id of the new node.
	 */
	public String getID(){
		return this.newNodeID;
	}
	
	/**
	 * Returns the new node's domain.
	 * @return The new node's domain.
	 */
	public String getDomain(){
		return this.newNodeDomain;
	}
	
	/**
	 * Returns the port of the new node.
	 * @return The port of the new node.
	 */
	public int getPort(){
		return this.newNodePort;
	}
	
	/**
	 * Return the string that denotes the respective abstract event in the formalization
	 * @return the formal name of the event
	 */
	public String toString(){
		return "SCALE_OUT("+newNodeID+", "+newNodeDomain+", "+newNodePort+")";
	}
}