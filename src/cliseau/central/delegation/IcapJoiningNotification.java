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

import cliseau.central.policy.scaling.Node;

/**
 * Class represent notification objects for security-irrelevant communications
 * between nodes during the joining process. It maintains all relevant
 * information that the involved nodes require for the joining protocol.
 * 
 * @author Hoang-Duong Nguyen, Tobias Reinhard
 */
public class IcapJoiningNotification extends IcapDelegationReqResp {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * The type of this notification.
	 */
	public Notification type;

	/**
	 * The transported source information.
	 */
	protected Node source;

	/**
	 * The transported predecessor information.
	 */
	protected Node predecessor;

	/**
	 * 
	 * The transported successor information.
	 */
	protected Node successor;

	/**
	 * The transported information about the new node.
	 */
	protected Node newNode;

	/**
	 * 
	 * The transported information about the Major Node.
	 */
	protected Node majorNode;

	/**
	 * A index of a finger table.
	 */
	protected int fingerTableIndex;

	/**
	 * A finger table entry
	 */
	protected Node fingerTableEntry;

	/**
	 * A key used to determine which node is responsible for this message.
	 */
	protected int key;

	/**
	 * A row number.
	 */
	protected int rowNumber;

	/**
	 * The target of this message.
	 */
	protected int target;

	/**
	 * A set of exchanged data items (i.e. tokens).
	 */
	protected Set<String> data;

	/**
	 * Constructor of a notification of the given type. The type of a
	 * notification depends on which step of the joining protocol this
	 * notification belongs to.
	 * 
	 * @param type
	 *            The type of this notification
	 */
	public IcapJoiningNotification(Notification type) {
		this.type = type;
	}

	/**
	 * Returns the source node.
	 * 
	 * @return The source node.
	 */
	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	/**
	 * Returns the predecessor node.
	 * 
	 * @return The predecessor node.
	 */
	public Node getPredecessor() {
		return predecessor;
	}

	/**
	 * Sets the predecessor.
	 * @param predecessor The new predecessor.
	 */
	public void setPredecessor(Node predecessor) {
		this.predecessor = predecessor;
	}

	/**
	 * Returns the successor node.
	 * 
	 * @return The successor node.
	 */
	public Node getSuccessor() {
		return successor;
	}

	/**
	 * Sets the successor.
	 * @param successor The new successor.
	 */
	public void setSuccessor(Node successor) {
		this.successor = successor;
	}

	/**
	 * Returns the new node.
	 * 
	 * @return The new node.
	 */
	public Node getNewNode() {
		return newNode;
	}

	/**
	 * Sets the new node.
	 * @param newNode The new node.
	 */
	public void setNewNode(Node newNode) {
		this.newNode = newNode;
	}

	/**
	 * Returns the Major Node.
	 * 
	 * @return The Major Node.
	 */
	public Node getMajorNode() {
		return majorNode;
	}

	/**
	 * Sets the Major Node.
	 * @param najorNode The Major Node.
	 */
	public void setMajorNode(Node najorNode) {
		this.majorNode = najorNode;
	}

	/**
	 * Returns the stored finger table index.
	 * 
	 * @return The stored finger table index.
	 */
	public int getFingerTableIndex() {
		return fingerTableIndex;
	}

	/**
	 * Sets the new finger table index.
	 * @param fingerTableIndex The new finger table index.
	 */
	public void setFingerTableIndex(int fingerTableIndex) {
		this.fingerTableIndex = fingerTableIndex;
	}

	/**
	 * Returns the stored finger table entry.
	 * 
	 * @return The stored finger table entry.
	 */
	public Node getFingerTableEntry() {
		return fingerTableEntry;
	}

	/**
	 * Sets the new finger table entry.
	 * @param fingerTableEntry The new finger table entry.
	 */
	public void setFingerTableEntry(Node fingerTableEntry) {
		this.fingerTableEntry = fingerTableEntry;
	}

	/**
	 * Returns the stored key that determines which node is responsible for this
	 * message.
	 * 
	 * @return The stored key that determines which node is responsible for this
	 *         message.
	 */
	public int getKey() {
		return key;
	}

	/**
	 * Sets the key that determines which node is responsible for this message.
	 * @param key The new key that determines which node is responsible for this message.
	 */
	public void setKey(int key) {
		this.key = key;
	}

	/**
	 * The stored set of data items that shall be exchanged.
	 * @return The exchanged data items.
	 */
	public Set<String> getData() {
		return data;
	}

	/**
	 * Sets the set of data items that shall be exchanged.
	 * @param data The new set of exchanged data items. 
	 */
	public void setData(Set<String> data) {
		this.data = data;
	}

	/**
	 * Returns the row number.
	 * @return The row number.
	 */
	public int getRowNumber() {
		return rowNumber;
	}

	/**
	 * Sets the row number.
	 * @param num The new row number.
	 */
	public void setRowNumber(int num) {
		this.rowNumber = num;
	}

	/**
	 * Returns the target.
	 * @return The target.
	 */
	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	/**
	 * Returns the serial version id
	 * @return The serial version id.
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
	
	/**
	 * Returns the type of this message.
	 * @return The type of this message.
	 */
	public Notification getType() {
		return type;
	}

	/**
	 * Sets the type of this message.
	 * @param type The new type of this message.
	 */
	public void setType(Notification type) {
		this.type = type;
	}

	@Override
	/**
	 * Returns a String representing this Object.
	 * @return A String representing this Object.
	 */
	public String toString() {
		return "IcapJoiningNotification [type=" + type + ", source=" + source
				+ ", predecessor=" + predecessor + ", successor=" + successor
				+ ", newNode=" + newNode + ", majorNode=" + majorNode
				+ ", fingerTableIndex=" + fingerTableIndex
				+ ", fingerTableEntry=" + fingerTableEntry + ", key=" + key
				+ ", data=" + data + "]";
	}
}