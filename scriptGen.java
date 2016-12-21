import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

/**
 * Class to generate Finger Tables & print out shell script to start 10 servers automatically
 * @author Hoang-Duong Nguyen
 *
 */
public class scriptGen {
	
	private static final int START_REMOTE_PORT = 10003;
	private static int num;
	private static int p;
	private static boolean isSingleServer = false;

	/**
	 * List of Server IDs
	 */
	private static List<Integer> ids;
	/**
	 * List of Predecessors'IDs
	 */
	private static List<Integer> pre;
	/**
	 * List of Successors' IDs
	 */
	private static List<Integer> succ;

	/**
	 * List of ports used by the servers
	 */
	private static int[] ports;

	public static void main(String[] args){
		
		if(args == null || args.length == 0)
			num = 0;
		else
			num = args.length;
		
		// Instantiating all needed materials
		if(num == 0){
			num = 10;
			// No arguments => 10 default servers
			Integer[] tmpIds = new Integer[]{2,7,8,29,33,37,48,51,60,63};
			ids = new ArrayList<Integer>(10);
			ids = Arrays.asList(tmpIds);
			Integer[] tmpPre  = new Integer[]{63,2,7,8,29,33,37,48,51,60};
			pre = new ArrayList<Integer>(10);
			pre = Arrays.asList(tmpPre);
			Integer[] tmpSucc = new Integer[]{7,8,29,33,37,48,51,60,63,2};
			succ = new ArrayList<Integer>(10);
			succ = Arrays.asList(tmpSucc);
			ports = new int[10];
			p = START_REMOTE_PORT;
		
			for(int i = 0; i<10; i++){
				ports[i] = p;
				p = p + 3;
			}
			p = START_REMOTE_PORT - 2;
		}
		else{
			// In case there is only 1 server
			if(num == 1){
				isSingleServer = true;
				num = 2;
				ids = new ArrayList<Integer>(2);
				pre = new ArrayList<Integer>(2);
				succ = new ArrayList<Integer>(2);
				Integer[] tmpIds = 
						new Integer[]{Integer.parseInt(args[0]),Integer.parseInt(args[0])};
				ids = Arrays.asList(tmpIds);
			}
			else{
				ids = new ArrayList<Integer>(num);
				pre = new ArrayList<Integer>(num);
				succ = new ArrayList<Integer>(num);
				for (int i=0; i < num; i++){
					ids.add(i,  Integer.parseInt(args[i]));
				}
			
				// Removing duplication
				Set<Integer> hs = new HashSet<>();	
				hs.addAll(ids);
				ids.clear();
				ids.addAll(hs);
				num = ids.size();
				
				// Removing ids that >= 2^6
				for(int i = 0; i < num; i++){
					if(ids.get(i) >= 64){
						ids.remove(i);
						i--;
						num--;
					}	
				}
				
				// If there is only 1 element remaining
				if(num == 1){
					Integer[] tmpIds = 
							new Integer[]{ids.get(0), ids.get(0)};
					ids = Arrays.asList(tmpIds);
					num = 2;
				}
				
				// If all elements are invalid
				if(num == 0){
					System.err.println("Invalid Arguments !");
					System.exit(1);
				}
				
				// Sorting
				Collections.sort(ids);
			}
			
			// create requested servers	
			ports = new int[num];
			// Initialized server IDs
			
			pre.add(0, ids.get(num-1));
			succ.add(0, ids.get(1));
			
			for (int j = 1; j < num-1; j++){
				pre.add(j, ids.get(j-1));
				succ.add(j, ids.get(j+1));
			}
			pre.add(num-1, ids.get(num-2));
			succ.add(num-1, ids.get(0));
	
			// Initialize port list
			p = START_REMOTE_PORT;
			
			for(int i = 0; i<num; i++){
				ports[i] = p;
				p = p + 3;
			}
			p = START_REMOTE_PORT - 2;
		
			if(isSingleServer)
				num = 1;
		}
		
		System.out.println("# Permission is hereby granted, free of charge, to any person obtaining a copy of\n"
 + "# this software and associated documentation files (the \"Software\"), to deal in\n"
 + "# the Software without restriction, including without limitation the rights to\n"
 + "# use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies\n"
 + "# of the Software, and to permit persons to whom the Software is furnished to do\n"
 + "# so, subject to the following conditions:\n\n"
 + "# The above copyright notice and this permission notice shall be included in all\n"
 + "# copies or substantial portions of the Software.\n\n" 
 + "# THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n"
 + "# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n"
 + "# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n"
 + "# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n"
 + "# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n"
 + "# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n"
 + "# SOFTWARE.);\n");
		
		System.out.println(
"#######################################################################################" + "\n" + 
"#!/bin/sh" + "\n" +
"#######################################################################################" + "\n" +
"# @author Hoang-Duong Nguyen" + "\n" +
"#######################################################################################" + "\n" + 
"\n" +
"#######################################################################################" + "\n" +
"JAVA_HOME=" + System.getProperty("java.home") + "\n" +

"OPTIONS='-Xms256m -Xmx256m -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true'" + "\n" +
""+
"pidpath=/tmp/" + "\n" +
"classname='clicap'" + "\n" +
"pid=${classname}" + "\n" +
"####################################################################################### " + "\n" +
"\n" +
"#######################################################################################" + "\n" +
"start () {" + "\n" +
"       # Run "+num+" Clicap - 1 central & "+(num-1)+" minor" + "\n" +
"       cd jar	" + "\n" +
"       echo -n 'Starting CliCap...'" + "\n" +
"       echo $JAVA_HOME/bin/java" + "\n" +
"       # Arguments: IS_CENTRAL - IS_READY - ID - COR_PORT - ENF_PORT -  REMOTE_PORT - "
+ "DOMAIN - PREDECESSOR_ID - PREDECESSOR_DOMAIN - PREDECESSOR_PORT"
+ " - SUCCESSOR_ID - SUCCESSOR_DOMAIN - SUCCESSOR_PORT - BIT_LENGTH - [remoteUnitID - domain - port]* " + "\n" );
	
		
	printStart();

	System.out.println("}\nstop () {");

	printStop();	

	System.out.println(
"       echo -e \"\\nStopped CliCap\\n\"" + "\n" +
"       exit 1" + "\n" +
"}" + "\n" +
"info () {" + "\n" +
"       echo" + "\n" +
"       echo 'BUILD:            .\\clicap build'" + "\n" +
"       echo 'RUN:              .\\clicap start'" + "\n" +
"       echo 'STOP:             .\\clicap stop'" + "\n" +
"       echo 'BUILD AND RUN:    .\\clicap build start'" + "\n" +
"       echo" + "\n" +
"       return '0'" + "\n" +
"}" + "\n" +
"####################################################################################### " + "\n" +
"\n" +
"#######################################################################################" + "\n" +
"if [ $# -gt 2 ] || [ $# -eq 0 ]; then" + "\n" +
"       echo -e \"\\nWrong number of arguments!\"" + "\n" +
"       info" + "\n" +
"       exit 1" + "\n" +
"fi" + "\n" +
"####################################################################################### " + "\n" +
"\n" +
"#######################################################################################" + "\n" +
"if [ $# -eq 2 ]; then" + "\n" +
"       if [ \"$1\" = \"build\" ] && [ \"$2\" = \"start\" ]; then" + "\n" +
"               # Build CliCap		" + "\n" +
"               ant		" + "\n" +
"               # Start CliCap" + "\n" +
"               	start" + "\n" +
"       else" + "\n" +
"               echo -e \"\\nIncorrect arguments!\"" + "\n" +
"               info" + "\n" +
"               exit 1" + "\n" +
"       fi" + "\n" +
"fi" + "\n" +
"####################################################################################### " + "\n" +
"\n" +
"#######################################################################################" + "\n" +
"if [ $# -eq 1 ]; then" + "\n" +
"       if [ \"$1\" = \"start\" ]; then	" + "\n" +
"               # Run CliCap" + "\n" +
"               start" + "\n" +
"       else" + "\n" +
"               if [ \"$1\" = \"stop\" ]; then" + "\n" +
"                       # Stop CliCap" + "\n" +
"                       stop" + "\n" +
"               else" + "\n" +
"                       if [  \"$1\" = \"build\" ]; then" + "\n" +
"                               # Build CliCap				" + "\n" +
"                               ant" + "\n" +
"                       else" + "\n" +
"                               echo -e \"\\nIncorrect argument!\"" + "\n" +
"                               info" + "\n" +
"                               exit 1" + "\n" +
"                       fi" + "\n" +
"               fi" + "\n" +
"      fi" + "\n" +
"fi" + "\n" +
"####################################################################################### " + "\n" +
"\n" +
"#######################################################################################");	

	}
		
	/**
	* Compute the Finger Tables and Print out the content of method start() to run 10 servers
	**/
	private static void printStart(){

		for(int i = 0; i < num; i++){

		    if(i == 0)
			System.out.println(
"      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 1 1 "+ids.get(i)+" "+p+" "+(p+1)+" "+(p+2)+" localhost "+pre.get(i)+
			" localhost "+getPort(pre.get(i))+" "+succ.get(i)+" localhost "+getPort(succ.get(i))+" 6     \\");
		    else
			System.out.println(
"      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 1 "+ids.get(i)+" "+p+" "+(p+1)+" "+(p+2)+" localhost "+pre.get(i)+
			" localhost "+getPort(pre.get(i))+" "+succ.get(i)+" localhost "+getPort(succ.get(i))+" 6     \\");
	
		    for (int j=0; j < 6; j++){
			    
			    System.out.println(
			    		"                                                                 "
			    +suc(ids.get(i),j)+" localhost "+getPort(suc(ids.get(i),j)) + "    \\");
		    }
		    System.out.println(
		    		"                                                                 "
		    				+" & echo $!>${pidpath}${pid}-"+ids.get(i)+".pid\n\n");
		     p = p +3;
		}
	}

	/**
	* Print out the content of method stop() to stop all the servers
	**/	
	private static void printStop(){
		for(int i = 0; i <num; i++){
			System.out.println(
"       kill -15 `cat ${pidpath}${pid}-"+ids.get(i)+".pid`" + "\n" +
"       rm -r ${pidpath}${pid}-"+ids.get(i)+".pid");
		}
	}

	/**
	* Return the successor according to the given id and offset
	**/
	private static int suc(int id, int offset){
		
		int targ = (id + (int)Math.pow(2, (double) offset)) % ((int)Math.pow(2, (double) 6));
		for(int i = 0; i<num; i++){
			if(ids.get(i) >= targ)
				return ids.get(i);
		}
		return ids.get(0);
	}

	/**
	* Return the port of the given server ID
	**/
	private static int getPort(int id){
		for(int i=0; i<num; i++){
			if(ids.get(i)==id)
				return ports[i];
		}
		return 0;
	}
}
