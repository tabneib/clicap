package test;

import java.util.Hashtable;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cliseau.central.IcapEventFactory;
import cliseau.central.event.IcapEvent;
import cliseau.central.event.IcapEventType;

/**
 * Check if Critical Events are correctly created
 * @author Hoang-Duong Nguyen
 *
 */
public class EventFactoryTest {
	/**
	 * A header for the test HTTP request.
	 */
	Hashtable<String, String> reqHeaders;
	
	/**
	 * A header for the test HTTP response.
	 */
	Hashtable<String, String> respHeaders;
	
	/**
	 * A body for the test HTTP request or response.
	 */
	String body;
	
	/**
	 * Stores the session id that is expected to be extracted from the current
	 * test header.
	 */
	String expectedSID;
	
	/**
	 * Stores the email address that is expected to be extracted from the
	 * current test body.
	 */
	String expectedEmail;
	
	/**
	 * Stores the order identifier that is expected to be extracted from the
	 * current test body.
	 */
	String expectedOrder;
	
	/**
	 * Stores the token that is expected to be extracted from the
	 * current test body.
	 */
	String expectedToken;
	
	/**
	 * Stores the payer id that is expected to be extracted from the
	 * current test body.
	 */
	String expectedPayerID;
	
	/**
	 * Checks if the actual created event contains the correct information
	 */
	private void assertEventEquals(IcapEvent actual, IcapEventType type){	
		
		if(expectedSID != null)
			assertTrue(expectedSID.equals(actual.sid));
		else
			assertTrue(actual.sid == null);

		if(expectedEmail != null)
			assertTrue(expectedEmail.equals(actual.email));
		else
			assertTrue(actual.email == null);
		
		if(expectedOrder != null)
			assertTrue(expectedOrder.equals(actual.order));
		else
			assertTrue(actual.order == null);
		
		if(expectedPayerID != null)
			assertTrue(expectedPayerID.equals(actual.payerID));
		else
			assertTrue(actual.payerID == null);
		
		if(expectedToken != null)
			assertTrue(expectedToken.equals(actual.token));
		else
			assertTrue(actual.token == null);
		
		assertTrue(type.equals(actual.type));
	}
	
	/**
	 * Sets the test HTTP response and request headers to the values used for
	 * the following testing.
	 */
	@Before
	public void setUp() throws Exception {
		// Instantiate a HTTP request Header
		reqHeaders = new Hashtable<String, String>();
		reqHeaders.put("user-agent", "Mozilla/5.0 "
				+ "(X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0");
		reqHeaders.put("accept", 
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		reqHeaders.put("accept-language", "en-US,en;q=0.5");
		reqHeaders.put("acept-encoding", "gzip, deflate");
		reqHeaders.put("cookie", "language=en_US; sid=ironmaidenisgreat");
		reqHeaders.put("connection", "keep-alive");
		reqHeaders.put("content-type", "application/x-www-form-urlencoded");
		// Instantiate a HTTP response Header
		respHeaders = new Hashtable<String, String>();
		respHeaders.put("date", "Fri, 05 Jun 2015 10:40:38 GMT");
		respHeaders.put("server", "Apache/2.4.7 (Ubuntu)");
		respHeaders.put("x-powered-by", "PHP/5.5.9-1ubuntu4.9");
		respHeaders.put("expires", "Thu, 19 Nov 1981 08:52:00 GMT");
		respHeaders.put("cache-control", 
				"no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
		respHeaders.put("pragma", "no-cache");
		respHeaders.put("location", 
				"https://www.sandbox.paypal.com/cgi-bin/webscr?"
				+ "cmd=_express-checkout&token=EC-12T01579N12560307&useraction=commit");
		respHeaders.put("vary", "Accept-Encoding");
		respHeaders.put("content-length", "0");
		respHeaders.put("keep-alive", "timeout=5, max=99");
		respHeaders.put("connection", "Keep-Alive");
		respHeaders.put("content-type", "text/html; charset=utf-8");
		// Instantiate HTTP body and expected values
		body = null;
		expectedSID   = "ironmaidenisgreat";
		expectedEmail = null;
		expectedOrder = null;
		expectedToken = null;
		expectedPayerID = null;	
	}

	/**
	 * Tests if "login" events from client side are created correctly from a corresponding
	 * HTML package.
	 */
	@Test
	public void loginC() {
		body = "email_address=foo%40foo.com&password=foofoo&x=22&y=5";
		expectedEmail = "foo%40foo.com";
		assertEventEquals((IcapEvent) IcapEventFactory.loginC(reqHeaders, body), 
				IcapEventType.LOG_IN_C);
	}
	
	/**
	 * Tests if "confirm order" events from client side are created correctly from a 
	 * corresponding HTML package.
	 */
	@Test
	public void confirmOrderC() {
		body = "x=63&y=13";
		expectedOrder = "x=63&y=13";
		assertEventEquals((IcapEvent) IcapEventFactory.confirmOrderC(reqHeaders, body), 
				IcapEventType.CONFIRM_ORDER_C);
	}
	
	/**
	 * Tests if login requests from client side are created correctly from a corresponding 
	 * HTML package.
	 */
	@Test
	public void tokenEstablishC() {
		assertEventEquals((IcapEvent) IcapEventFactory.tokenEstablishC(reqHeaders),
				IcapEventType.TOKEN_ESTABLISH_C);
	}
	
	/**
	 * Tests if "token establish" events from server side are created correctly from a 
	 * corresponding HTML package.
	 */
	@Test
	public void tokenEstablishS() {
		expectedSID = null;
		expectedToken = "EC-12T01579N12560307";
		assertEventEquals((IcapEvent) IcapEventFactory.tokenEstablishS(respHeaders),
				IcapEventType.TOKEN_ESTABLISH_S);
	}
	
	/**
	 * Tests if "receive payer id" events from client side are created correctly from a 
	 * corresponding HTML package.
	 */
	@Test
	public void receivePayerIdC() {
		String head = "GET http://workbox/checkout.php?callback&"
				+ "module=paypal_express&express_action=retrieve&"
				+ "token=EC-12T01579N12560307&PayerID=QF499G6WUFECQ HTTP/1.1"
				+ "\nHost: workbox\nUser-Agent: Mozilla/5.0 (X11; Ubuntu; "
				+ "Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0\nAccept: "
				+ "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;"
				+ "q=0.8\nAccept-Language: en-US,en;q=0.5\nAccept-Encoding: "
				+ "gzip, deflate\nCookie: language=en_US; sid=ironmaidenisgreat"
				+ "vn6l263n3sv6\nConnection: keep-alive";
		expectedToken="EC-12T01579N12560307";
		expectedPayerID="QF499G6WUFECQ";
		assertEventEquals((IcapEvent) IcapEventFactory.receivePayerIdC(reqHeaders, head), 
				IcapEventType.RECEIVE_PAYER_ID_C);
	}
	
	/**
	 * Tests if "process order" events from client side are created correctly from a 
	 * corresponding HTML package.
	 */
	@Test
	public void processOrderC() {
		assertEventEquals((IcapEvent) IcapEventFactory.processOrderC(reqHeaders), 
				IcapEventType.PROCESS_ORDER_C);
	}

	/**
	 * Tests if "successful order" events from client side are created correctly from
	 *  a corresponding HTML package.
	 */
	@Test
	public void successfulOrderC() {
		assertEventEquals((IcapEvent) IcapEventFactory.successfulOrderC(reqHeaders), 
				IcapEventType.SUCCESSFUL_ORDER_C);
	}
	
	/**
	 * Tests if "logout" events from client side are created correctly from a
	 * corresponding HTML package.
	 */
	@Test
	public void logoutC() {
		assertEventEquals((IcapEvent) IcapEventFactory.logoutC(reqHeaders), 
				IcapEventType.LOG_OUT_C);
	}
}