package cliseau.central.event;

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
 * Class to generate Finger Tables , Predecessor and Successor pointers automatically
 * @author Hoang-Duong Nguyen
 *
 */
public class FingerTableGen {
	
	private static final int START_REMOTE_PORT = 10003;
	private static int num;
	private static int p;
	private static boolean isSingleServer = false;

	//private static int[] ids  = {2,7,8,29,33,37,48,51,60,63};
	private static int[] ids;
	//private static int[] pre  = {63,2,7,8,29,33,37,48,51,60};
	private static int[] pre;
	//private static int[] succ = {7,8,29,33,37,48,51,60,63,2};
	private static int[] succ;

	private static int[] ports;

	public static void main(String[] args){
		
		if(args == null || args.length == 0)
			num = args.length;
		else
			num = args.length;
		
		if(num == 0){
			num = 10;
			// No arguments => 10 default servers
			ids  = new int[]{2,7,8,29,33,37,48,51,60,63};
			pre  = new int[]{63,2,7,8,29,33,37,48,51,60};
			succ = new int[]{7,8,29,33,37,48,51,60,63,2};
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
				ids = new int []{Integer.parseInt(args[0]),Integer.parseInt(args[0])};
			}
			else{
				ids = new int [num];
				for (int i=0; i < num; i++){
					ids[i] = Integer.parseInt(args[i]);
				}
				
			}
			
			// else create requested servers	
	
			pre = new int [num];
			succ = new int [num];
			ports = new int[num];
			// Initialized server IDs
			
			
			pre[0] = ids[num-1];
			succ[0] = ids[1];
			
			for (int j = 1; j < num-1; j++){
				pre[j] = ids[j-1];
				succ[j]= ids[j+1];
			}
			
			pre[num-1] = ids[num-2];
			succ[num-1] = ids[0];
			
			
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
		
	
	printStart();


	}
		
	/**
	* Compute the Finger Tables and Print out the content of method start() to run 10 servers
	**/
	private static void printStart(){

		for(int i = 0; i < num; i++){

			System.out.println("» Server " + i + "\n");
		    for (int j=0; j < 6; j++){
			    
			    System.out.println(j +  "  " + suc(ids[i],j));
		    }
		    System.out.println("Successor:   " + succ[i]);
		    System.out.println("Predecessor: " + pre[i]);
		    System.out.println("\n__________________________\n");
		}
	}


	/**
	* Return the successor according to the given id and offset
	**/
	private static int suc(int id, int offset){
		
		int targ = (id + (int)Math.pow(2, (double) offset)) % ((int)Math.pow(2, (double) 6));
		for(int i = 0; i<num; i++){
			if(ids[i] >= targ)
				return ids[i];
		}
		return ids[0];
	}

	/**
	* Return the port of the given server ID
	**/
	private static int getPort(int id){
		for(int i=0; i<num; i++){
			if(ids[i]==id)
				return ports[i];
		}
		return 0;
	}
}