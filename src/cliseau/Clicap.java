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
package cliseau;

import icap.IcapServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cliseau.central.FingerTable;
import cliseau.central.policy.IcapPolicy;
import cliseau.central.policy.scaling.LeavingProtocol;
import cliseau.central.policy.scaling.JoiningProtocol;
import cliseau.javacor.Coordinator;
import cliseau.javacor.CoordinatorAddressing;
import cliseau.javatarget.CoordinatorInterface;

/**
 * Class that instantiates all the components and starts the service. The components to
 * be instantiated include the local policy (together with the finger table), the 
 * coordinator (together with its addressing object) and the ICAP service (if this is the
 * central node). Configuration parameters are read from the admin's input and maintained 
 * such that they can be used anywhere. 
 * 
 * @author Hoang-Duong Nguyen
 *
 */
public class Clicap {
	
	/**
	 * The absolute path to the Java directory 
	 */
	public static final String JAVA_HOME = "/usr/lib/jvm/java-7-oracle";
	public static final String JAVA_EXECUTABLE_PATH = System.getProperty("java.home") + "/bin/java";

	// Keys for the configuration parameters
	public static final String DOMAIN = "DOMAIN";
	public static final String ID = "ID";
	public static final String COR_PORT = "COR_PORT";
	public static final String ENF_PORT = "ENF_PORT";
	public static final String REMOTE_PORT = "REMOTE_PORT";
	public static final String IS_CENTRAL = "IS_CENTRAL";
	public static final String IS_READY = "IS_READY";
	public static final String BIT_LENGTH = "CAPACITY";
	public static final String PREDECESSOR = "PREDECESSOR";
	public static final String SUCCESSOR = "SUCCESSOR";	
	public static final String PRE_DOMAIN = "PRE_DOMAIN";
	public static final String SUC_DOMAIN = "SUC_DOMAIN";
	public static final String PRE_PORT = "PRE_PORT";
	public static final String SUC_PORT = "SUC_PORT";

	/**
	 * The local policy of this CliCap unit
	 */
	private static IcapPolicy loc;
	/**
	 * Maintains all the addresses required by the coordinator for communications
	 */
	public static CoordinatorAddressing addressing;
	/**
	 * The coordinator component of this CliCap unit
	 */
	private static Coordinator coor;
	/**
	 * The finger table maintained by this CliCap unit
	 */
	public static FingerTable fTable;
	/**
	 * Stores the configuration parameters
	 */
	public static Map<String, String> config;

	/**
	 * Initialize the finger table and configure the CliCap unit according to the given
	 * configuration arguments then run the unit. ICAP server is also be triggered if 
	 * this is the central unit. <br>
	 * 
	 * @param args
	 *            Configuration for the CliCap unit which contains following information: 
	 *            <br>
	 *            + This is the central unit or not <br>
	 *            + This unit is ready to make enforcement decision or not  <br>
	 *            + ID of this unit  <br>
	 *            + Port that the coordinator listens to ICAP service  <br>
	 *            + Port that ICAP service listen to the coordinator  <br>
	 *            + Port that the coordinator listens to remote CliSeAu units  <br>
	 *            + Domain of this unit  <br>
	 *            + ID of the predecessor of this unit <br>
	 *            + Domain of the predecessor <br>
	 *            + Port of the predecessor <br>
	 *            + ID of the successor of this unit <br>
	 *            + Domain of the successor <br>
	 *            + Port of the successor <br>
	 *            + bitLength (number of bits of identifiers)  <br>
	 *            + Tuples of remote units' id, their domains and ports. <br>
	 */
	public static void main(String[] args) {
		
		ArrayList<Integer> ftEntries;
		ftEntries = new ArrayList<Integer>();
		addressing = new CoordinatorAddressing();

		// <----------------------------------------------------------------------------->
		//						  Read Configuration Parameters
		// <----------------------------------------------------------------------------->
	
		config = new HashMap<String, String>();
		if (args != null) {
			try {
				config.put(IS_CENTRAL, args[0]);
				config.put(IS_READY, args[1]);
				config.put(ID, args[2]);
				config.put(COR_PORT, args[3]);
				config.put(ENF_PORT, args[4]);
				config.put(REMOTE_PORT, args[5]);
				config.put(DOMAIN, args[6]);
				config.put(PREDECESSOR, args[7]);
				config.put(PRE_DOMAIN, args[8]);
				config.put(PRE_PORT, args[9]);
				config.put(SUCCESSOR, args[10]);
				config.put(SUC_DOMAIN, args[11]);
				config.put(SUC_PORT, args[12]);
				config.put(BIT_LENGTH, args[13]);
				
				// Read finger tables entries
				// Read socket addresses of remote units known to this unit
				for (int i = 14; i < args.length; i = i + 3) {
					ftEntries.add(Integer.parseInt(args[i]));
					addressing.setAddress(args[i], new InetSocketAddress(
							args[i + 1], Integer.parseInt(args[i + 2])));
				}
				
				// Node should also be able to communicate with itself
				addressing.setAddress(args[2], new InetSocketAddress(
						args[6], Integer.parseInt(args[5])));
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Wrong arguments!");
				System.exit(0);
			}
		}

		// <----------------------------------------------------------------------------->
		// 		   Instantiate Finger Table From the configuration information
		// <----------------------------------------------------------------------------->
		
		fTable = new FingerTable(Integer.parseInt(config.get(ID)),
				Integer.parseInt(config.get(BIT_LENGTH)), ftEntries);

		// Instantiate Local Policy
		loc = new IcapPolicy(config.get(ID), config.get(IS_READY).equals("1"));

		// Initialize joining and leaving protocols
			JoiningProtocol.init(loc);
			LeavingProtocol.init(loc);
		
		// Initialize Coordination Interface
		try {
			CoordinatorInterface.init(new InetSocketAddress(config.get(DOMAIN),
					Integer.parseInt(config.get(COR_PORT))), new ServerSocket(
					Integer.parseInt(config.get(ENF_PORT))));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Address for contacting the local enforcer.
		addressing.setLocalEnforcerAddress(new InetSocketAddress(config
				.get(DOMAIN), Integer.parseInt(config.get(ENF_PORT))));

		// Run ICAP with default configuration file if this is the central unit
		if (config.get(IS_CENTRAL).equals("1"))
			IcapServer.main(null);

		// Fork a thread to run a CliSeAu Unit
		new Thread(new CliseaulUnit()).start();

		// uncomment this to run mapping test
		/*MappingTest mappingTest = new MappingTest((IcapPolicy) loc);
		mappingTest.test();*/
	}

	/**
	 * Class used to create a thread for the CliSeAu unit
	 *
	 */
	private static class CliseaulUnit implements Runnable {
		public void run() {
			// Instantiate and run Coordinator
			try {
				System.out.println("Server " + config.get(ID) + " is started \n");
				coor = new Coordinator(config.get(ID), new ServerSocket(
						Integer.parseInt(config.get(COR_PORT))),
						new ServerSocket(Integer.parseInt(config
								.get(REMOTE_PORT))), addressing, loc);
				// Run the coordinator of this CliSeAu unit
				coor.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Terminate this node. This method is called at in the last step of the leaving 
	 * process of this node.
	 */
	public static void kill(){
		System.exit(1);
	}
	
	// <--------------------------------------------------------------------------------->
	// 										Getters
	// <--------------------------------------------------------------------------------->
	
	/**
	 * Obtain the bit length of the identifiers of the nodes in network 
	 * @return	the bit length 
	 */
	public static int getBitLength(){
		return Integer.parseInt(config.get(BIT_LENGTH));
	}
	
	/**
	 * Obtain the capacity of the network which is the maximal number of nodes that the 
	 * network can contain.
	 * @return	the capacity of the network
	 */
	public static int getCapacity(){
		 return (int) Math.pow(2, Clicap.getBitLength());
	}
	
	/**
	 * Obtain the identifier of this node as String
	 * @return the identifier
	 */
	public static String getID(){
		return config.get(ID);
	}
	
	/**
	 * Obtain the identifier of this node as int.
	 * @return the identifier
	 */
	public static int getIDnum() {
		return Integer.parseInt(config.get(ID));
	}
	
	/**
	 * Obtain the host of this node
	 * @return the host
	 */
	public static String getDomain(){
		return config.get(DOMAIN);
	}
	
	/**
	 * Obtain the port of this node
	 * @return the port
	 */
	public static int getRemotePort(){
		return Integer.parseInt(config.get(REMOTE_PORT));
	}
	
	/**
	 * Obtain the identifier of the predecessor of this node as String.
	 * @return the identifier
	 */
	public static String getPredID(){
		return config.get(PREDECESSOR);
	}
	
	/**
	 * Obtain the identifier of the predecessor of this node as int.
	 * @return the identifier
	 */
	public static int getPredIDnum(){
		return Integer.parseInt(config.get(PREDECESSOR));
	}
	
	/**
	 * Obtain the host of the predecessor of this node
	 * @return the host
	 */
	public static String getPredDomain(){
		return config.get(PRE_DOMAIN);
	}
	
	/**
	 * Obtain the port of the predecessor of this node
	 * @return the port
	 */
	public static int getPredPort(){
		return Integer.parseInt(config.get(PRE_PORT));
	}
	
	/**
	 * Obtain the identifier of the successor of this node as String.
	 * @return the identifier
	 */
	public static String getSucID(){
		return config.get(SUCCESSOR);
	}
	
	/**
	 * Obtain the identifier of the successor of this node as int.
	 * @return the identifier
	 */
	public static int getSucIDnum(){
		return Integer.parseInt(config.get(SUCCESSOR));
	}
	
	/**
	 * Obtain the host of the successor of this node
	 * @return the host
	 */
	public static String getSucDomain(){
		return config.get(SUC_DOMAIN);
	}
	
	/**
	 * Obtain the port of the successor of this node
	 * @return the port
	 */
	public static int getSucPort(){
		return Integer.parseInt(config.get(SUC_PORT));
	}
}