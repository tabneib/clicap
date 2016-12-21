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

import java.util.Set;

/**
 * Class represent notification objects for security-irrelevant communications
 * between nodes during the leaving process. It maintains all relevant
 * information that the involved nodes require for the leaving protocol.
 * 
 * @author Hoang-Duong Nguyen
 */
public class IcapLeavingNotification extends IcapDelegationReqResp {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The type of this notification.
	 */
	private Notification type;

	/**
	 * The source domain.
	 */
	private String sourceDomain;
	/**
	 * The source port
	 */
	private int sourcePort;
	/**
	 * The source id.
	 */
	private String sourceID;

	/**
	 * The predecessor domain.
	 */
	private String preDomain;
	/**
	 * The predecessor port.
	 */
	private int prePort;
	/**
	 * The predecessor id.
	 */
	private String preID;

	/**
	 * The successor domain.
	 */
	private String sucDomain;
	/**
	 * The successor port.
	 */
	private int sucPort;
	/**
	 * The successor id.
	 */
	private String sucID;

	/**
	 * The leaving node's domain.
	 */
	private String leavingNodeDomain;
	/**
	 * The leaving node's port.
	 */
	private int leavingNodePort;

	/**
	 * The leavin node's id.
	 */
	private String leavingNodeID;

	/**
	 * The exchanged data.
	 */
	private Set<String> data;
	/**
	 * The row number.
	 */
	private int rowNumber;
	/**
	 * The target of this notification.
	 */
	private int target;

	/**
	 * Constructor of a notification of the given type. The type of a
	 * notification depends on which step of the leaving protocol this
	 * notification belongs to.
	 * 
	 * @param type
	 *            The type of this notification
	 */
	public IcapLeavingNotification(Notification type) {
		this.type = type;
	}

	// <-------------------------------------------------------------------->
	// Setters
	// <-------------------------------------------------------------------->

	/**
	 * Sets the type of this notification.
	 * 
	 * @param newType
	 *            The new type.
	 */
	public void setType(Notification newType) {
		this.type = newType;
	}

	/**
	 * Sets the data that shall be exchanged.
	 * 
	 * @param data
	 *            The new data items.
	 */
	public void setData(Set<String> data) {
		this.data = data;
	}

	/**
	 * Sets the row number
	 * 
	 * @param row
	 *            The row number.
	 */
	public void setRowNumber(int row) {
		this.rowNumber = row;
	}

	/**
	 * Sets the target of this notification.
	 * 
	 * @param target
	 *            The new target.
	 */
	public void setTarget(int target) {
		this.target = target;
	}

	/**
	 * Sets the source domain.
	 * 
	 * @param domain
	 *            The new source domain.
	 */
	public void setSourceDomain(String domain) {
		this.sourceDomain = domain;
	}

	/**
	 * Sets the predecessor domain.
	 * 
	 * @param domain
	 *            The new predecessor domain.
	 */
	public void setPreDomain(String domain) {
		this.preDomain = domain;
	}

	/**
	 * Sets the successor domain.
	 * 
	 * @param domain
	 *            The new successor domain.
	 */
	public void setSucDomain(String domain) {
		this.sucDomain = domain;
	}

	/**
	 * Sets the leaving node's domain.
	 * 
	 * @param domain
	 *            The new domain.
	 */
	public void setLeavingNodeDomain(String domain) {
		this.leavingNodeDomain = domain;
	}

	/**
	 * Sets the predecessor's port.
	 * 
	 * @param port
	 *            the new port.
	 */
	public void setPrePort(int port) {
		this.prePort = port;
	}

	/**
	 * Sets the predecessor's id.
	 * 
	 * @param id
	 *            The new id.
	 */
	public void setPreID(String id) {
		this.preID = id;
	}

	/**
	 * Sets the source port.
	 * 
	 * @param port
	 *            The new port.
	 */
	public void setSourcePort(int port) {
		this.sourcePort = port;
	}

	/**
	 * Sets the successor port.
	 * 
	 * @param port
	 *            The new port
	 */
	public void setSucPort(int port) {
		this.sucPort = port;
	}

	/**
	 * Sets the leaving node's port.
	 * 
	 * @param port
	 *            The new port.
	 */
	public void setLeavingNodePort(int port) {
		this.leavingNodePort = port;
	}

	/**
	 * Sets the leaving node's id.
	 * 
	 * @param id
	 *            The new id.
	 */
	public void setLeavingNodeID(String id) {
		this.leavingNodeID = id;
	}

	/**
	 * Sets the source id.
	 * 
	 * @param sourceID
	 *            The new id.
	 */
	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}

	/**
	 * Sets the successors id.
	 * @param sucID The new id.
	 */
	public void setSucID(String sucID) {
		this.sucID = sucID;
	}

	// <-------------------------------------------------------------------->
	// Getters
	// <-------------------------------------------------------------------->

	/**
	 * Returns the data
	 * @return The data to be exchanged.
	 */
	public Set<String> getData() {
		return this.data;
	}

	/**
	 * Returns the type
	 * @return The type of this notification.
	 */
	public Notification getType() {
		return this.type;
	}

/**
 * Returns the row number
 * @return The row number
 */
	public int getRowNumber() {
		return this.rowNumber;
	}

	/**
	 * Returns the type of this notification.
	 * @return The typo of this notification.
	 */
	public int getTarget() {
		return this.target;
	}
	

	/**
	 * Returns the source domain
	 * @return The source domain
	 */
	public String getSourceDomain() {
		return this.sourceDomain;
	}

	/**
	 * Return The source port
	 */
	public int getSourcePort() {
		return this.sourcePort;
	}

	/**
	 * Returns the pre domain.
	 * @return The pre domain
	 */
	public String getPreDomain() {
		return this.preDomain;
	}

	/**
	 * Returns the pre port.
	 * @return The port
	 */
	public int getPrePort() {
		return this.prePort;
	}

	/**
	 * Returns the pre id
	 * @return the id.
	 */
	public String getPreID() {
		return this.preID;
	}

	/**
	 * Returns the successor domain
	 * @return the domain
	 */
	public String getSucDomain() {
		return this.sucDomain;
	}

	/**
	 * Returns the successor port
	 * @return the port
	 */
	public int getSucPort() {
		return this.sucPort;
	}

/**
 * Retruns the leaving node's domain
 * @return The domain
 */
	public String getLeavingNodeDomain() {
		return this.leavingNodeDomain;
	}

	/**
	 * Returns the leaving node's port
	 * @return The port.
	 */
	public int getLeavingNodePort() {
		return this.leavingNodePort;
	}

	/**
	 * Returns the leaving node's port 
	 * @return The port.
	 */
	public String getLeavingNodeID() {
		return this.leavingNodeID;
	}

	/**
	 * Returns the source port
	 * @return the port
	 */
	public String getSourceID() {
		return sourceID;
	}

	/**
	 * Returns the successor id
	 * @return the id
	 */
	public String getSucID() {
		return sucID;
	}
}