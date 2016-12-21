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
package cliseau.central.policy.nwmap;
import java.io.File;
import java.util.List;
import cliseau.Clicap;

/**
 * Generates a png file containing a Chord identifier circle as graph with edges 
 * representing the finger table entries.
 * @author Hoang-Duong Nguyen
 *
 */
public class MapGenerator{
	
	/**
	 * The radius of the Chord identifier circle's graphic representation.
	 */
	private static final float radius = 8;
	
	/**
	 * The size of the picture.
	 */
	private static final int size = 80;
	
	
   /**
    * Construct a DOT graph in memory, convert it
    * to image and store the image in the file system.
    */
	public static void generate(List<String> mainCircle, List<String> otherLinks){
		
      GraphViz gv = new GraphViz();
      gv.addln(gv.start_graph());
      gv.addln("size =\""+size+","+size+"\";");
      gv.addln("layout=neato");
      
      int key;
      key = Integer.parseInt(mainCircle.get(0));
      
      gv.addln(mainCircle.get(0)+"[style=\"invisible\", margin =\"0\", shape=\"square\","
      		+ " fontsize=\"25\", label=< <table height=\"100\" width=\"100\" "
      		+ "border=\"0\"><tr><td fixedsize=\"true\" width=\"100\" height=\"100\">"
      		+ "<img src=\"./serverMajor.png\" /></td></tr><tr><td>"+mainCircle.get(0)
      		+"</td></tr></table>  > pos=\""+computeX(key)+","+computeY(key)+"!\"];");
      
      for (int i=1; i < mainCircle.size() -1; i++){
    	  key = Integer.parseInt(mainCircle.get(i));
    	  gv.addln(mainCircle.get(i)+"[style=\"invisible\", margin =\"0\", shape="
    	  		+ "\"square\", fontsize=\"25\", label=< <table border=\"0\"><tr>"
    	  		+ "<td fixedsize=\"true\" width=\"100\" height=\"100\"><img src="
    	  		+ "\"./server.png\" /></td></tr><tr><td>"+mainCircle.get(i)+"</td>"
    	  		+ "</tr></table>  > pos=\""+computeX(key)+","+computeY(key)+"!\"];");
      }
      
      String circle = mainCircle.get(0);
      for (int i=1; i < mainCircle.size(); i++){
    	  circle += " -> " + mainCircle.get(i);
      }
      circle += ";";
      gv.addln(circle);
    
      for (int j=0; j < otherLinks.size(); j++)
    	  gv.addln(otherLinks.get(j));
      
      gv.addln(gv.end_graph());
      
      // Types of the output files 
      String type = "png";
      File out = new File("../log/networkMap." + type);   
      
      // Generate image files
      gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );    
   }
	
	/**
	 * Compute the x coordinate for the given key.
	 * @param key The key for which to compute x coordinate.
	 * @return The x coordinate.
	 */
	private static float computeX(int key){
		
		float offset = (float) (2 * Math.PI /
				(((float)Clicap.getCapacity())/ 
						((float)Clicap.getCapacity()-(float) key)));
		float x = (float)(0 + radius * Math.cos(offset));
		return x;
	}
	
	/**
	 * Compute the y coordinate for the given key.
	 * @param key The key for which to compute y coordinate.
	 * @return The y coordinate.
	 */
	private static float computeY(int key){
	
		float offset = (float) (2 * Math.PI /
				(((float)Clicap.getCapacity())/
						((float)Clicap.getCapacity()-(float) key)));
		float y = (float)(0 + radius * Math.sin(offset));
		return y;
	}
}