package flowershop.services;

/**
 * The `ReservationStatus` enum represents the various statuses that a reservation order can have.
 */
public enum ReservationStatus {
	/**
	 * Indicates that the reservation is currently being processed.
	 */
	IN_PROCESS,

	/**
	 * Indicates that the reservation is ready for pickup.
	 */
	READY_FOR_PICKUP,

	/**
	 * Indicates that the reservation has been picked up by the client.
	 */
	PICKED_UP;
}
