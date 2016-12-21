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
package cliseau.central.delegation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents notifications for determining the network map.
 * @author Hoang-Duong Nguyen
 */
public class IcapNWMNotification extends IcapDelegationReqResp {

	private static final long serialVersionUID = 1L;

	private String source;
	private List<String> mainCircle;
	private List<String> otherLinks;

	/**
	 * Constructor 
	 * @param source
	 * 					The identifier of the source node who triggers the network 
	 * 					map determining process
	 */
	public IcapNWMNotification(String source) {
		this.mainCircle = new ArrayList<>();
		this.otherLinks = new ArrayList<>();
		this.source = source;
	}

	/**
	 * Add a new link to the main chord circle
	 * @param link
	 * 				the link between 2 neighbor servers to be added
	 */
	public void addToMainCircle(String link){
		this.mainCircle.add(link);
	}
	
	/**
	 * Add a new link to the set maintains all links other than those on the main circle
	 * @param link
	 * 				the link between 2 not neighbor servers to be added
	 */				 
	public void addToOtherLinks(List<String> link) {
		this.otherLinks.addAll(link);
	}

	/**
	 * Determine if the notification has gone one circle around the network and returns
	 * to the source node
	 * @param identifier
	 * 					The identifier of the current server, that is, the server which
	 * 					has just received this notification
	 * @return	true if has gone one circle, otherwise false
	 */
	public boolean wentOneCircle(String identifier){
		return (this.source).equals(identifier);
	}
	
	// <--------------------------------------------------------------------------------->
	// 										Getters
	// <--------------------------------------------------------------------------------->

	/**
	 * Returns the main circle.
	 * @return The main circle.
	 */
	public List<String> getMainCircle() {
		return this.mainCircle;
	}

	/**
	 * Returns the links.
	 * @return The links.
	 */
	public List<String> getOtherLinks() {
		return this.otherLinks;
	}
}