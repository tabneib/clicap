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

package cliseau.central;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cliseau.Clicap;

/**
 * This class maintains the finger table of the corresponding CliCap node and provides
 * all relevant functionalities. 
 * @author Hoang-Duong Nguyen
 *
 */
public class FingerTable {
	
	/**
	 * The list of the real finger table entries
	 */
	private ArrayList<Integer> entries;
	/**
	 * The list of the extended finer table entries which is the original list plus this
	 * node itself. Further more, the identifier of this node is made to be the smallest 
	 * one and the list is always sorted in ascending order
	 */
	private ArrayList<Integer> extendedEntries;
	/**
	 * The bit length of the identifier of the nodes in network
	 */
	private int bitLength;
	/**
	 * The identifier of the node maintaining this finger table
	 */
	private int id;
	
	/**
	 * Construct a finger table for the server with the given ID, the 
	 * given capacity and the given list of server known to this server.
	 * @param id
	 * 				id of this server
	 * @param capacity
	 * 				The number of entries stored in this finger table
	 * @param extendedEntries
	 * 				The given list of entries
	 */
		public FingerTable(int id, int capacity, ArrayList<Integer> entr){
		this.id = id;
		this.bitLength = capacity;
		try {
			entries = new ArrayList<Integer>(capacity);
			entries.addAll(entr);
	
			// Add the node itself as an entry to simplify the computation
			extendedEntries = new ArrayList<Integer>(capacity + 1);
			extendedEntries.addAll(entr);
			
			// Make the id of this unit be the smallest one
			for(int i = 0; i < extendedEntries.size(); i++){
				if(extendedEntries.get(i) <= id){
					extendedEntries.set(i, 
							extendedEntries.get(i) + (int)Math.pow(2, (double)capacity));
				}
			}
			extendedEntries.add(id);
			Collections.sort(this.extendedEntries);
	
		log();
		log(">> Initial Predecessor: " + Clicap.getPredID() +
					"\n>> Initial Successor: "+Clicap.getSucID());	
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Server " + this.id + " :: " 
						+ "Can not initialize finger table. Server stopped");
			System.exit(0);
		}
	}
	
	/**
	 * Determine the next unit in the routing path. The given key must
	 * already be checked not to be in the responsible key range of this unit.
	 * @param key	
	 * 				the key of the data to be searched for
	 * @return		
	 * 				the identifier of the closest predecessor of the given key
	 * 				
	 */	
	public int lookUp(int k){
		
		System.out.println(">>> Server " + Clicap.getID() + ": Looking up " + k);

		int size = Clicap.getCapacity();
		int key = k % size;
		key = key < this.id ? key + size : key;
		int nextUnit = 0;
		
		for (int i=0; i < extendedEntries.size(); i++){
			if (extendedEntries.get(i) >= key){
				nextUnit = extendedEntries.get(i-1) % size;
				break;
			}
		}
		if(nextUnit == this.id)
			// The successor is the responsible one, so return the successor
			return getSuccessor() % size;   
		else{
			if(nextUnit == 0)
				return extendedEntries.get(extendedEntries.size()-1) % size;
			else
				return nextUnit % size;
		}
	}
	
	/**
	 * Replace the value of all entries that store ID of the current successor 
	 * with the given value. The other entries may no more maintain correct values.
	 * @param newSuc
	 * 				 ID of the new successor of the node maintains this table
	 */
	public void updateSuccessor(int newSuc){
		
		int oldSuc = getSuccessor();
		int eOldSuc = oldSuc;
		if(eOldSuc < this.id)
			eOldSuc = eOldSuc + Clicap.getCapacity();

		for (int i=0; i < entries.size(); i++){
			if(entries.get(i) == oldSuc
					&& newSuc >= this.id + (int)Math.pow(2, i)){
				entries.add(i, newSuc);
				entries.remove(i+1);
			}		
		}
		
		//Also update extended entries 
		extendedEntries = new ArrayList<Integer>(Clicap.getCapacity() + 1);
		extendedEntries.addAll(entries);
		extendedEntries.add(id);
		// Make the id of this unit be the smallest one
		for(int i = 0; i < extendedEntries.size(); i++){
			if(extendedEntries.get(i) < id){
				extendedEntries.set(i, 
						extendedEntries.get(i) + Clicap.getCapacity());
			}
		}
		Collections.sort(this.extendedEntries);
		log();
	}

	/**
	 * Obtain all communication link from this server to other servers stored in this
	 * finger table.
	 * @return the list containing ids of the linked servers
	 */
	public List<String> getOtherLinks(){
		
		List<String> result = new ArrayList<>();
		List<Integer> tmp = new ArrayList<>();
		tmp.addAll(entries);
		Set<Integer> hs = new HashSet<>();
		
		hs.addAll(tmp);
		tmp.clear();
		tmp.addAll(hs);
		Collections.sort(tmp);
		for(int i=0; i < tmp.size(); i++ ){
			if(tmp.get(i) != Integer.parseInt(Clicap.getSucID()))
				result.add(Clicap.getID()+" -> " + tmp.get(i) + ";");
		}
		return result;
	}
	
	/**
	 * Retrieve ID of the successor of the unit maintains this table
	 * @return	ID of the successor 
	 */
	public int getSuccessor(){
		for (int i=0; i < extendedEntries.size(); i++){
			if(extendedEntries.get(i) > id)
				return extendedEntries.get(i) % (int)Math.pow(2, (double)bitLength);
		}
		return id;
	}
	
	/**
	 * Retrieve the i-th node stored in this finger table
	 * @param  i 
	 * 				the given index
	 * @return identifier of the node at the given index
	 */
	public void setNode(int i, int newValue){
		this.entries.add(i-1, newValue);
		this.entries.remove(i);
		
		// Also update the extended entries
		extendedEntries = new ArrayList<Integer>(bitLength + 1);
		extendedEntries.addAll(entries);
		extendedEntries.add(id);
		// Make the id of this unit be the smallest one
		for(int j = 0; j < extendedEntries.size(); j++){
			if(extendedEntries.get(j) < id){
				extendedEntries.set(
						j, extendedEntries.get(j) + (int)Math.pow(2, (double)bitLength));
			}
		}
		Collections.sort(this.extendedEntries);
		log();
	}
	
	/**
	 * Retrieve the i-th node stored in this finger table
	 * @param  i 
	 * 				the given index
	 * @return identifier of the node at the given index
	 */
	public int getNode(int i){
		return entries.get(i-1);
	}
	
	/**
	 * Retrieve the extended version of th it-th node of this finger table
	 * @param  i 
	 * 				the given index
	 * @return extended identifier of the node at the given index 
	 */
	public int getExtendedNode(int i){
		
		int result = entries.get(i-1);
		if (result <= id )
			result += Clicap.getCapacity();
		return result;
	}
	
	/**
	 * For debugging purpose only
	 * log the finger table content to the corresponding log file
	 */
	public void log(){
		String file = "../log/FingerTable_" + Clicap.config.get(Clicap.ID) + ".log";
		try {
		    PrintWriter out = new PrintWriter(
		    		new BufferedWriter(new FileWriter(file, true)));
		    out.println("Finger Table: ");
		    for (int i = 0; i < entries.size(); i++)
		    	out.println((i+1) + "    " + entries.get(i));
		    
		    out.println("\n Extended Finger Table:");
		    for (int j = 0; j < extendedEntries.size(); j++)
		    	out.println((j+1) + "    " + extendedEntries.get(j));
		    
		    out.println("\n________________________\n");
		    out.close();
		    
		   // log("Predecessor is " + Clicap.getPredID());
		   // log("Successor is " + Clicap.getSucID());
		} catch (IOException e) {
			System.err.println(e);
		}
	}	
	
	/**
	 * For debugging purpose only
	 * log the given message to the corresponding log file
	 */
	public void log(String msg){
		String file = "../log/FingerTable_" + Clicap.config.get(Clicap.ID) + ".log";
		try {
		    PrintWriter out = 
		    		new PrintWriter(new BufferedWriter(new FileWriter(file, true)));    
		    out.println("\n" + msg + "\n________________________\n");    
		    out.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}