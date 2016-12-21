package cliseau.central.policy.scaling;

import java.io.Serializable;

/**
 * This class offers the possibility to aggregate information identifying one
 * single node.
 * 
 * @author Tobias Reinhard
 * 
 */
public class Node implements Serializable {

	
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The id of the node.
	 */
	public int id;
	
	/**
	 * The domain of the node (e.g. localhost).
	 */
	public final String domain;
	
	/**
	 * The port of the node.
	 */
	public final int port;

	/**
	 * Constructs a node (just this object not a real node) with the given id, domain and port.
	 * @param id The id of the node.
	 * @param domain The domain of the node.
	 * @param port The port of the node.
	 */
	public Node(int id, String domain, int port) {
		this.id = id;
		this.domain = domain;
		this.port = port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + id;
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		Node other = (Node) obj;

		if (domain == null) {
			if (other.domain != null)
				return false;
		} else {
			if (!domain.equals(other.domain)) {
				System.out.println("domains do not match : " + domain + " | "
						+ other.domain);
				return false;
			}
		}

		if (id != other.id) {
			System.out.println("ids do not match : " + id + " | " + other.id);
			return false;
		}

		if (port != other.port) {
			System.out.println("ports do not match : " + port + " | "
					+ other.port);
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "(" + id + ", " + domain + ", " + port + ")";
	}

	/**
	 * Clones ths node object.
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (Exception e) {
			return null;
		}
	}
}