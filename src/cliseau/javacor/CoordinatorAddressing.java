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
package cliseau.javacor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import cliseau.Clicap;

/**
 * Encapsulate addressing pecularities between CliSeAu units and their components.
 *
 * This helps to avoid that the Coordinator has to deal with pecularities like
 * socket creation and managing internal and external addresses of CliSeAu units.
 * Objects of this class hide
 * <ol>
 * <li>addresses and their management and</li>
 * <li>the creation of server and client sockets</li>
 * </ol>
 * from the Coordinator implementation.
 *
 * This has the idea that potentially later the integration of, e.g., SSLSockets
 * for encryption is simpler than if this would all be entangled in the
 * Coordinator implementation.
 *
 * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/net/ssl/SSLSocket.html">SSLSocket</a>
 * @see net.cliseau.runtime.javacor.Coordinator
 */
public class CoordinatorAddressing {
	
	// @NHD This has to be instantiated during the start-up phase
	
	/** Addresses for contacting the other CliSeAu units in the system (one for each identifier). */ 
	private HashMap<String, SocketAddress> addresses;
	/** Address for contacting the local enforcer. */
	private SocketAddress localEnforcerAddress;
	/** Address for being contacted by the local interceptor. */
	private SocketAddress privateAddress;

	/**
	 * Create an addressing object with an empty list of known external addresses.
	 */
	public CoordinatorAddressing() {
		addresses = new HashMap<String, SocketAddress>();
	}

	/**
	 * Obtain the set of all identifiers for which this object stores external addresses.
	 *
	 * @return Set of known identifiers.
	 * @see net.cliseau.runtime.sync.StartupBarrier
	 * @todo Check whether the implementation of this method should use HashSet
	 *       or better some other (more efficient?) instance of Set.
	 */
	public Set<String> getIdentifiers() {
		// careful: according to the Java documentation, changes to the Set returned
		//          by keySet() have an impact on the HashMap! That's why we
		//          return a copy
		return new HashSet<String>(addresses.keySet());
	}

	/**
	 * Set the external address of the CliSeAu unit with a given identifier.
	 *
	 * @param identifier The identifier whose address is to be set.
	 * @param address The new external address of the CliSeAu unit with the given identifier.
	 */
	public void setAddress(final String identifier, final SocketAddress address) {
		addresses.put(identifier, address);
	}

	/**
	 * Return the external address of the CliSeAu unit with a given identifier.
	 *
	 * @param identifier The identifier whose unit's external address to get.
	 * @exception IllegalArgumentException Is thrown if the identifier is not known.
	 */
	public SocketAddress getAddress(final String identifier)
			throws IllegalArgumentException {
		SocketAddress result = addresses.get(identifier);
		if (result == null)
			throw new IllegalArgumentException("Server"+Clicap.config.get(Clicap.ID)+": Destination identifier '"+identifier+"'unknown");
		return result;
	}

	/**
	 * Connect to remote CliSeAu unit and return connection.
	 *
	 * @param destinationID The identifier whose CliSeAu unit to connect to.
	 * @return Socket for the connection.
	 * @exception IOException Thrown if an error occurs during the connection.
	 * @exception IllegalArgumentException Is thrown if the destination identifier is not known.
	 */
	public Socket connectRemote(final String destinationID)
			throws IllegalArgumentException,IOException {
		SocketAddress destAddr = getAddress(destinationID);
		if (destAddr == null) {
			throw new IllegalArgumentException("Destination identifier unknown");
		}
		Socket result = new Socket();
		result.connect(destAddr);
		return result;
	}

	/**
	 * Establishes a connection to the local enforcer.
	 *
	 * @return The socket belonging to the connection.
	 * @exception IOException Is thrown if the connection to the local enforcer failed.
	 */
	public Socket connectLocalEnforcer() throws IOException {
		Socket result = new Socket();
		result.connect(localEnforcerAddress);
		return result;
	}

	/**
	 * Returns a server socket for the coordinator's interface to other CliSeAu units.
	 *
	 * @return The server socket for connections from other CliSeAu units.
	 * @exception IOException Is thrown if the socket could not be bound to the local external address.
	 * @exception IllegalArgumentException Is thrown if the local identifier is not known.
	 */
	public ServerSocket getPublicServer(final String identifier)
			throws IOException,IllegalArgumentException {
		SocketAddress address = addresses.get(identifier);
		if (address == null) {
			throw new IllegalArgumentException("Destination identifier unknown");
		}
		ServerSocket result = new ServerSocket();
		result.bind(address);
		return result;
	}

	/**
	 * Returns a server socket for the coordinator's interface to the interceptor.
	 *
	 * @return The server socket.
	 * @exception IOException Is thrown if the socket could not be bound to the local internal address.
	 * @exception NullPointerException Is thrown if the private/internal address is not set
	 */
	public ServerSocket getPrivateServer()
			throws IOException {
		if (privateAddress == null) {
			throw new NullPointerException();
		}
		ServerSocket result = new ServerSocket();
		result.bind(privateAddress);
		return result;
	}

	/**
	 * Get localEnforcerAddress.
	 *
	 * @return localEnforcerAddress as SocketAddress.
	 */
	public SocketAddress getLocalEnforcerAddress()
	{
	    return localEnforcerAddress;
	}

	/**
	 * Set localEnforcerAddress.
	 *
	 * @param localEnforcerAddress the value to set.
	 */
	public void setLocalEnforcerAddress(SocketAddress localEnforcerAddress)
	{
	    this.localEnforcerAddress = localEnforcerAddress;
	}

	/**
	 * Get privateAddress.
	 *
	 * @return privateAddress as SocketAddress.
	 */
	public SocketAddress getPrivateAddress()
	{
	    return privateAddress;
	}

	/**
	 * Set privateAddress.
	 *
	 * @param privateAddress the value to set.
	 */
	public void setPrivateAddress(SocketAddress privateAddress)
	{
	    this.privateAddress = privateAddress;
	}
}
