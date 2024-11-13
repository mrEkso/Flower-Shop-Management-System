package kickstart.Davyd_Lera.models.orders;

import jakarta.persistence.Entity;
import kickstart.Davyd_Lera.models.Client;
import kickstart.Davyd_Lera.models.enums.ReservationStatus;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Entity
public class ReservationOrder extends AbstractOrder {

	private LocalDateTime reservationDateTime; // Date and time for the reservation
	/* In addition to the orderStatus field built into the Order parent class,
	 which can be “Open”, “Paid”, “Completed”, “Canceled”, we will have a
	 reservationStatus field that shows the progress of the reservation process itself. */
	private ReservationStatus reservationStatus;

	public ReservationOrder(LocalDateTime reservationDateTime, UserAccount orderProcessingEmployee, Client client) {
		super(orderProcessingEmployee, client);
		this.reservationDateTime = reservationDateTime;
		this.reservationStatus = ReservationStatus.IN_PROCESS;
	}

	public ReservationOrder() {
	}

	public LocalDateTime getReservationDateTime() {
		return reservationDateTime;
	}

	public void setReservationDateTime(LocalDateTime reservationDateTime) {
		this.reservationDateTime = reservationDateTime;
	}

	public ReservationStatus getReservationStatus() {
		return reservationStatus;
	}

	ReservationOrder markInProcess() {
		this.reservationStatus = ReservationStatus.IN_PROCESS;
		return this;
	}

	ReservationOrder markReadyForPickup() {
		this.reservationStatus = ReservationStatus.READY_FOR_PICKUP;
		return this;
	}

	ReservationOrder markPickedUp() {
		Assert.isTrue(this.isPaid(), "A reservation must be paid to be picked up!");
		this.reservationStatus = ReservationStatus.PICKED_UP;
		return this;
	}
}
