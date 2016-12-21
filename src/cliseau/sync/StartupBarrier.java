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
package cliseau.sync;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.Logger; // for debugging

import cliseau.javacor.CoordinatorAddressing;

/**
 * Barrier synchronization for the startup of CliSeAu units.
 *
 * At the start of a CliSeAu instance, some synchronization is useful.
 * The barrier synchronization is used in order to start the coordinators of all
 * the CliSeAu units in the distributed system before the actual programs
 * belonging to them are executed. Under the assumption that CliSeAu units
 * never terminate, this ensures that whenever a program causes a critical event
 * that has to be delegated, then the delegate CliSeAu unit is already
 * running.
 */
public class StartupBarrier implements Runnable {
	/** Information object for addressing the other coordinators to check their status. */
	private CoordinatorAddressing addressing;
	/** The identifier of the CliSeAu unit. */
	private String localIdentifier;

	/** Level including and above which all messages are displayed */
	//private org.apache.log4j.Level logLevel;

	/**
	 * Create a new StartupBarrier object.
	 *
	 * @param addressing Addressing object for contacting the other coordinators.
	 * @param identifier Identifier of the "local" CliSeAu unit (self).
	 * @param logLevel Level including and above which all messages are displayed
	 */
	public StartupBarrier(final CoordinatorAddressing addressing, final String identifier,
			final org.apache.log4j.Level logLevel) {
		this.addressing = addressing;
		this.localIdentifier = identifier;
		//this.logLevel = logLevel;
	}

	/**
	 * Perform a barrier synchronization over all (known) CliSeAu units.
	 *
	 * This is a very simple barrier. It collects a list of all CliSeAu units
	 * belonging to the same framework via the addressing field. For all these
	 * CliSeAu units, a connection to the respective coordinator is
	 * established to check whether the CliSeAu unit is up. This procedure
	 * is repeated until a connection could be established to all CliSeAu units.
	 *
	 * @todo To avoid endless loops in case of a failure, some
	 *       limitation on the number of trials or their duration
	 *       should be possible.
	 */
	public void run() {
		//Logger logger = Logger.getLogger("barrier@" + localIdentifier);
		//logger.setLevel(logLevel);

		Set<String> remainingIDs = addressing.getIdentifiers();

		while (remainingIDs.size() > 0) {
			// repeat until no identifiers remain to be waited for
			Iterator<String> i = remainingIDs.iterator();
			while (i.hasNext()) {
				String id = i.next();
				if (id.equals(localIdentifier)) {
					// no need to query the local socket
					i.remove();
				} else {
					// try to establish a connection to the remote CliSeAu units
					// with identifier 'id'; if this succeeds, then remove the identifier
					// from the list of remaining identifiers
					try {
						Socket sock = addressing.connectRemote(id);
						// NOTE: no data transfer here
						// FIXME: here should finally also go an authentication of the remote party
						sock.close();
						// no exception, so connection established successfully
						//logger.info("confirmed alive: " + id);
						i.remove();
					} catch (ConnectException e) {
						// According to the Oracle Java API documentation, the
						// ConnectException is typically thrown in case of a refused
						// connection. That's why we silently ignore this exception
						// and particularly leave the identifier in the list.
					} catch (IOException e) {
						// Unexpected exception to be displayed; do not remove
						// identifier from list.
						//logger.warn(e.getClass().getName() + ":  " + e.getMessage());
					}
				}
			}
			if (remainingIDs.size() > 0) {
				// wait a bit until the next round of checks
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {}
			}
		}
	}
}
