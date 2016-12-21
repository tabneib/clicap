/* Copyright (c) 2011-2012 Richard Gay <gay@mais.informatik.tu-darmstadt.de>
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
package cliseau.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Class for forwarding one stream to another.
 *
 * This Runnable is used to deal with one of the output streams
 * of a started process (normal output stream and error output
 * stream).
 */
class ProcessInputStreamHandler implements Runnable {
	/** Reader from which the input is obtained. */
	private BufferedReader in_reader;
	/** Stream to which to forward the input. */
	private PrintStream out_stream;

	/**
	 * Construct a new ProcessInputStreamHandler object.
	 *
	 * @param in_stream Stream from which the input is obtained
	 * @param out_stream Stream to which the output shall go
	 */
	public ProcessInputStreamHandler(final InputStream in_stream, final PrintStream out_stream) {
		this.in_reader  = new BufferedReader(new InputStreamReader(in_stream));
		this.out_stream = out_stream;
	}

	/**
	 * Execute the code of the thread.
	 *
	 * As long as it is possible, read lines from the input stream and
	 * forward it to the output stream. This way of handling the streams
	 * of a process is largely inspired by
	 * http://stackoverflow.com/questions/3343066/reading-streams-from-java-runtime-exec
	 * However, the closing of 'out_stream' has been removed because
	 * this stream might equal stdin/stderr such that closing this
	 * stream would disable output of the invoking process (most likely
	 * an undesired behavior).
	 */
	@Override public void run() {
		String line;
		try {
			while ((line = in_reader.readLine()) != null) {
				out_stream.println(line);
			}
		} catch (Exception e) {throw new Error(e);}

		out_stream.flush();
		//out_stream.close();
	}
}

/**
 * Utility class for running system commands.
 *
 * This class encapsulates the running of system commands and the forwarding of
 * command outputs to the standard output streams. Apparently, Java's approach
 * of running system commands requires that the output of the command is treated
 * by the calling Java program. That is, the command does not simply use the
 * standard output and input file descriptors of the calling program. In
 * consequence, the calling program cannot simply ignore the output of the
 * program, because then internal buffers would run full and cause the command
 * to wait indefinitely until the buffers become free again. Hence, this
 * convenience class provides variants of "exec()" which automatically forward
 * the command's output to the calling program's standard output streams.
 */
public class CommandRunner {
	/**
	 * Executes the specified command and arguments in a separate process.
	 *
	 * @param cmdarray Command to call and its arguments.
	 * @return Return value of the process
	 * @exception IOException If an I/O error occurs (that is what the Java
	 *                        documentation tells about exec()'s exception.
	 * @see java.lang.Runtime#exec(String)
	 */
	public static int exec(String[] cmdarray) throws IOException {
		Process p = Runtime.getRuntime().exec(cmdarray);
		return handleProcess(p);
	}

	/**
	 * Executes the specified string command in a separate process.
	 *
	 * @param command The system command to run.
	 * @return Return value of the process
	 * @exception IOException If an I/O error occurs (that is what the Java
	 *                        documentation tells about exec()'s exception.
	 * @see java.lang.Runtime#exec(String)
	 */
	public static int exec(String command) throws IOException {
		Process p = Runtime.getRuntime().exec(command);
		return handleProcess(p);
	}

	/**
	 * Handle output of a given process.
	 *
	 * If a process produces output during its execution then it seems like the
	 * Java API causes this output to be written to internal buffers. These
	 * buffers can run full if they are not read from. In consequence, the
	 * process waits its execution until further output can be produced. To not
	 * have the process starve, we read from the process's output and error
	 * stream here and dump the content to real standard output and,
	 * respectively, standard error.
	 *
	 * @param p The process whose output to handle.
	 * @return Return value of the process
	 */
	private static int handleProcess(Process p) {
		new Thread(new ProcessInputStreamHandler(p.getInputStream(), System.out)).start();
		new Thread(new ProcessInputStreamHandler(p.getErrorStream(), System.err)).start();
		try {
			return p.waitFor();
		} catch (InterruptedException e) {
			// We do not want exceptions here.
			return EXCEPTIONAL_RETURN_VALUE;
		}
	}

	/** Value to return in case waiting for the spawned process fails. */
	private final static int EXCEPTIONAL_RETURN_VALUE = -1;

	/** Default return value of programs that terminate without error. */
	public final static int NORMAL_TERMINATION = 0;
}
