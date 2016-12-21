package cliseau.central.policy.scaling;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import cliseau.Clicap;
import cliseau.central.delegation.IcapLocalNotification;
import cliseau.javatarget.CoordinatorInterface;
import static java.util.concurrent.TimeUnit.*;

/**
 * This class maintains a scheduled service for the stabilization which
 * periodically checks for possible update of the successor pointer.
 * 
 * @author Hoang-Duong Nguyen
 * 
 */
public class PeriodicalChecker {

	/**
	 * Delay between two checks
	 */
	private static final int STABILIZATION_PERIOD = 2;// + Clicap.getIDnum() ;

	/**
	 * The thread used to invoke the periodical stabilization process.
	 */
	private static final ScheduledExecutorService stabilization = Executors
			.newScheduledThreadPool(1);

	/**
	 * The handler for the periodical successor check.
	 */
	private static ScheduledFuture<?> sucCheckerHandle;

	/**
	 * Runnable service to be run periodically
	 */
	static final Runnable sucChecker = new Runnable() {

		@Override
		public void run() {
			try {

				CoordinatorInterface
						.sendLocalNotification(new IcapLocalNotification(
								Clicap.fTable.getSuccessor()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Start checking successor pointer periodically
	 */
	public static void startStabilization() {

		sucCheckerHandle = stabilization.scheduleAtFixedRate(sucChecker,
				STABILIZATION_PERIOD, STABILIZATION_PERIOD, SECONDS);
	}

	/**
	 * Stop checking successor pointer periodically
	 */
	public static void stopStabilization() {
		sucCheckerHandle.cancel(true);

	}
}
