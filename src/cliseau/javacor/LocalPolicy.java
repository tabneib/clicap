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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cliseau.central.delegation.IcapLocalNotification;

/**
 * Base class for local policy instantiations.
 *
 * This defines the basis for all instantiations of local policies. The
 * instantiations are always implemented in Java, independent of the platform of
 * the target program.
 *
 * A local policy stores an identifier.  During the instantiation of a CliSeAu
 * unit, local policy objects have to be constructed in a prescribed way.
 * Passing the identifier to the local policy avoids reimplementing local
 * policies for every unit, but it differs from the way this is done in the
 * formal model.
 *
 * @see Coordinator
 */
public abstract class LocalPolicy {
	/** The identifier of the CliSeAu unit using the local policy */
	private final String identifier;
	/** The configuration of the policy */
	private Map<String,String> config;

	/**
	 * Construct a local policy object.
	 *
	 * @param identifier The identifier of the CliSeAu unit using the local policy
	 */
	public LocalPolicy(final String identifier) {
		this.identifier = identifier;
		this.config = null; // no configuration initially
	}

	/**
	 * Handles a request from the local interceptor component.
	 *
	 * This method is supposed to be called by the Coordinator for every local
	 * request that it receives from the local interceptor. If the policy can
	 * make a local decision, then it should return an object of type
	 * EnforcementDecision. Otherwise it should return a delegation request of
	 * some subtype of DelegationReqResp encapsulated in an object of type
	 * DelegationLocPolReturn. The method may update the state of the local
	 * policy object during the process of handling the request.
	 *
	 * @param ev The critical event for which a decision is requested.
	 * @return The response to the coordinator
	 * @exception IllegalArgumentException Can be thrown if ev is of the wrong subtype of CriticalEvent
	 */
	public abstract LocalPolicyResponse localRequest(CriticalEvent ev)
			throws IllegalArgumentException;

	/**
	 * TODO comment me
	 * @author Hoang-Duong Nguyen
	 * @param notif
	 * @return
	 * @throws IllegalArgumentException
	 */
	public abstract LocalPolicyResponse localNotify(IcapLocalNotification notif)
			throws IllegalArgumentException;
	
	
	/**
	 * Handles a request/reponse received from a remote CliSeAu unit.
	 *
	 * This method is supposed to be called by the Coordinator for every request
	 * or response received from a remote CliSeAu unit.
	 *  - If a request is received and a decision on the request can be made
	 *    locally, then a delegation response of some subtype of
	 *    DelegationReqResp (encapsulated in a DelegationLocPolReturn object)
	 *    should be returned by this method. If a local decision cannot be made,
	 *    the same or a modified request can be returned (also encapsulated in a
	 *    DelegationLocPolReturn object).
	 *  - If a response is received and is destined for the local CliSeAu unit,
	 *    then an EnforcementDecision for the local enforcer should
	 *    be returned. If the response is not for the local CliSeAu unit,
	 *    then the same or a modified response should be forwarded by this method
	 *    to another CliSeAu unit (encapsulated in a DelegationLocPolReturn
	 *    object).
	 *
	 * This method may update the state of the local policy object during the
	 * process of handling the request.
	 *
	 * @param dr The delegation request/response received from a remote party
	 * @return The response to the coordinator
	 * @exception IllegalArgumentException Can be thrown if dr is of the wrong subtype of DelegationReqResp
	 */
	public abstract LocalPolicyResponse remoteRequest(DelegationReqResp dr)
			throws IllegalArgumentException;

	/**
	 * Get identifier.
	 *
	 * @return identifier as String.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Get policy configuration entry.
	 *
	 * @param key Key of the configuration for which the value shall be retrieved.
	 * @return Configuration value for the given key or null if there is no configuration for this key.
	 */
	public String getConfig(final String key) {
		if (config == null) {
			return null;
		} else {
			return config.get(key);
		}
	}

	/**
	 * Set policy configuration map.
	 *
	 * @param config New config to be used, as Map&lt;String,String&gt;.
	 */
	public void setConfig(final Map<String,String> config) {
		this.config = config;
	}

	/**
	 * Set policy configuration map.
	 *
	 * This method assumes that under the resource identified by the parameter
	 * resourceName, a configuration file in the Java Properties format can be
	 * found.
	 *
	 * Note: one may run into annoying (null pointer) exceptions with the code
	 * if the resource is not found. A possible reason for such cases might be
	 * wrong resourceName values. Check the difference between
	 * Class.getResourceAsStream() and ClassLoader.getResourceAsStream() to
	 * understand one source of errors (leading "/").
	 *
	 * @param loader The class loader to use for loading the resource.
	 * @param resourceName Name of the resource file from which the config shall be obtained.
	 * @exception IOException Thrown when the configuration could not be loaded from the resource.
	 * @todo Check whether getResourceAsStream returned null and handle this case to do
	 *       something better than dying of a NullPointerException
	 */
	public final void setConfigResource(final ClassLoader loader, final String resourceName)
			throws IOException {
		Properties policy_param_props = new Properties();
		policy_param_props.load(loader.getResourceAsStream(resourceName));
		Map <String,String> policy_params = new HashMap<String,String>(policy_param_props.size());
		for (final String param_key: policy_param_props.stringPropertyNames()) {
			policy_params.put(param_key, policy_param_props.getProperty(param_key));
		}
		setConfig(policy_params);
	}
}
