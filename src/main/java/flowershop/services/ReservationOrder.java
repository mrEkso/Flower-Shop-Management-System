package flowershop.services;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDateTime;

/**
 * Represents an order for a reservation to pick up flower arrangements from the shop at a
 * specified date and time. This type of order is designed for clients who prefer to collect
 * their flowers in person, often used for custom or pre-arranged bouquets that are ready upon arrival.
 *
 * <p>Typical use cases include walk-in clients who schedule pickup orders or clients who prefer
 * to inspect arrangements before taking them home.</p>
 */
@Entity
public class ReservationOrder extends AbstractOrder {

	private LocalDateTime reservationDateTime; // Date and time for the reservation
	/* In addition to the orderStatus field built into the Order parent class,
	 which can be “Open”, “Paid”, “Completed”, “Canceled”, we will have a
	 reservationStatus field that shows the progress of the reservation process itself. */
	private ReservationStatus reservationStatus;
	@ManyToOne
	private Client client;

	/**
	 * Sets the client associated with the reservation order.
	 *
	 * @param client the client to set
	 */
	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * Sets the date and time for the reservation.
	 *
	 * @param reservationDateTime the reservation date and time to set
	 */
	public void setReservationDateTime(LocalDateTime reservationDateTime) {
		this.reservationDateTime = reservationDateTime;
	}

	/**
	 * Sets the status of the reservation.
	 *
	 * @param reservationStatus the reservation status to set
	 */
	public void setReservationStatus(ReservationStatus reservationStatus) {
		this.reservationStatus = reservationStatus;
	}

	/**
	 * Constructs a `ReservationOrder` with the specified user account, date and time, client, and notes.
	 *
	 * @param userAccount the user account associated with the order
	 * @param dateTime    the date and time of the reservation
	 * @param client      the client associated with the order
	 * @param notes       additional notes for the order
	 */
	public ReservationOrder(UserAccount userAccount, LocalDateTime dateTime, Client client, String notes) {
		super(userAccount, notes);
		this.reservationDateTime = dateTime;
		this.client = client;
		this.reservationStatus = ReservationStatus.IN_PROCESS;
	}

	/**
	 * Default constructor for `ReservationOrder`.
	 * This constructor is primarily used by JPA and other frameworks.
	 */
	protected ReservationOrder() {
	}

	/**
	 * Returns the status of the reservation.
	 *
	 * @return the reservation status
	 */
	public ReservationStatus getReservationStatus() {
		return reservationStatus;
	}

	/**
	 * Returns the date and time of the reservation.
	 *
	 * @return the reservation date and time
	 */
	public LocalDateTime getReservationDateTime() {
		return reservationDateTime;
	}

	/**
	 * Returns the client associated with the reservation order.
	 *
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}
}