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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import cliseau.central.delegation.IcapLocalNotification;
import cliseau.javacor.CriticalEvent;
import cliseau.javacor.EnforcementDecision;

/**
 * Interface to coordinators for interceptors and enforcers.
 *
 * This class provides an easy to use interface to the local coordinator. It is
 * meant to be used by interceptors and enforcers and abstracts away the details
 * of establishing a connection to the coordinator and sending/receiving data
 * objects (in a serialized form).
 *
 * The used communication paradigm is to not establish a single connection on
 * which the sending and receiving happens. Rather
 *  - a connection to the local coordinator is established for sending data only
 *    and closing the connection immediately afterwards;
 *  - an incoming connection from the local coordinator is awaited for receiving
 *    data.
 *
 * This follows the formal model of service automata which uses separate
 * channels for the communication between interceptor and coordinator and,
 * respectively, between enforcer and coordinator. Whether the chosen approach
 * is sufficiently generic for many target platforms and is yet efficient, this
 * still has to be evaluated. Any changes to the paradigm should, however, first
 * be performed in the Coordinator rather than the CoordinatorInterface, because
 * the latter is only concerned with Java target programs.
 *
 * @see Enforcer
 * @see net.cliseau.runtime.javacor.Coordinator
 * @todo Check whether it would make sense to merge send() and receive() to a
 *       single function since they are most likely used sequentially anyways.
 */
public class CoordinatorInterface {
	/** The address under which the local coordinator can be contacted */
	private static SocketAddress coordinatorAddress;
	/** The socket that the local enforcer listens on for enforcement decisions */
	private static ServerSocket enforcerSocket;
	/** Cached address for the local host */
	private static InetAddress localhost = null;

	/**
	 * Private constructor to disallow instances of this class.
	 *
	 * All methods of this class are static such that it does not make sense to
	 * construct an instance of this class. To initialize the static fields of
	 * this class, use init().
	 *
	 * @see #init(SocketAddress, ServerSocket)
	 */
	private CoordinatorInterface() { }

	/**
	 * Initialize the interface to the local coordinator.
	 *
	 * This method shall be invoked once at the very beginning of the monitoring
	 * of an application. It sets the parameters required to get in contact with
	 * the local coordinator.
	 *
	 * @param coordinatorAddress The address for contacting the local coordinator
	 * @param enforcerSocket The socket on which responses should be listened on
	 * @todo initialize static fields: it still has to be found out where this
	 *      "constructor"-like function is called. There should be something
	 *      that can be added to the aspect template (JavaAspect.st) such that
	 *      this function is called with the right parameters at the very
	 *      beginning of the target application's execution.
	 */
	public static void init(final SocketAddress coordinatorAddress, final ServerSocket enforcerSocket) {
		CoordinatorInterface.coordinatorAddress = coordinatorAddress;
		CoordinatorInterface.enforcerSocket = enforcerSocket;
	}

	/**
	 * Send a critical event to the local coordinator.
	 *
	 * This method is supposed to be invoked by the interceptor part of a CliSeAu unit.
	 * In an aspect-oriented programming approach, this presumably is
	 * a part of every CliSeAu aspect's advice.
	 *
	 * @param ev The critical event to be sent to the local coordinator
	 * @exception IOException Thrown in case of connection and transmission failures
	 * @see #receive()
	 * @todo This method uses Java's serialization mechanism for obtaining and
	 *       sending a transmissible representation of the critical event. This
	 *       design decision should be factored out of this method somehow (how?).
	 * @todo Find out the exact causes for IOException being thrown 
	 * @todo Check whether it makes sense to catch and handle IOException here already
	 */
	public static void send(CriticalEvent ev)
			throws IOException {
				
		// Step 1: connect to local coordinator
		Socket connection = new Socket();
		connection.connect(coordinatorAddress);

		// Step 2: send the serialized critical event
		ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
		oos.writeObject(ev);

		// Step 3: close connection
		connection.close();
	}

	/**
	 * 
	 * @param notif
	 * @throws IOException
	 * @author Hoang-Duong Nguyen
	 */
	public static void sendLocalNotification(IcapLocalNotification notif)
			throws IOException {
				
		// Step 1: connect to local coordinator
		Socket connection = new Socket();
		connection.connect(coordinatorAddress);

		// Step 2: send the serialized critical event
		ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
		oos.writeObject(notif);

		// Step 3: close connection
		connection.close();
	}

	/**
	 * Wait for and receive an enforcement decision from the local coordinator.
	 *
	 * This method is supposed to be invoked by the enforcer part of a CliSeAu unit.
	 * In an aspect-oriented programming approach, this presumably is
	 * a part of every CliSeAu aspect's advice and follows a call to
	 * send().
	 *
	 * @return The received enforcement decision
	 * @exception IOException Thrown in case of connection errors
	 * @exception ClassNotFoundException Thrown if serialized data is received which
	 *                                does not belong to a known type
	 * @see #send(CriticalEvent)
	 * @todo This method uses Java's serialization mechanism for obtaining and
	 *       sending a transmissible representation of the critical event. This
	 *       design decision should be factored out of this method somehow (how?).
	 * @todo Find out the exact causes for IOException being thrown
	 * @todo Check whether it makes sense to catch and handle IOException here already
	 * @todo Check whether the used cast can actually throw a ClassCastException
	 */
	public static EnforcementDecision receive()
			throws IOException,ClassNotFoundException {
		// Step 1: accept (wait for) connections from the local coordinator
		Socket connection = enforcerSocket.accept();

		// Step 2: read und deserialize the enforcement decision
		ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
		EnforcementDecision ed = (EnforcementDecision) ois.readObject();

		// Step 3: close connection and return the enforcement decision
		connection.close();
		return ed;
	}
}
