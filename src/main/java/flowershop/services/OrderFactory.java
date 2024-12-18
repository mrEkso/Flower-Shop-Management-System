package flowershop.services;

import flowershop.calendar.Event;
import flowershop.sales.SimpleOrder;
import flowershop.sales.WholesalerOrder;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import flowershop.calendar.CalendarService;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This class is needed to create all orders of type {@link AbstractOrder}.
 * Creating any type of order will also add it to the calendar
 * It encapsulates the workaround of assigning a default {@link UserAccount}
 * to every {@link org.salespointframework.order.Order}, without which the framework seems not to work.
 */
@Service
@DependsOn("userInitializer")
public class OrderFactory {

	private final CalendarService calendarService;
	private final UserAccountManagement userAccountManagement;

	/**
	 * Constructs an `OrderFactory` with the specified user account management.
	 *
	 * @param userAccountManagement the user account management system
	 */

	public OrderFactory(UserAccountManagement userAccountManagement, CalendarService calendarService) {
		this.userAccountManagement = userAccountManagement;
		this.calendarService = calendarService;
	}

	/**
	 * Creates a new `SimpleOrder` with the default user account.
	 *
	 * @return a new `SimpleOrder`
	 */
	public SimpleOrder createSimpleOrder() {
		return new SimpleOrder(getDefaultUserAccount());
	}

	/**
	 * Creates a new `EventOrder` with the specified event date, delivery address, client, and notes.
	 *
	 * @param eventDate       the date of the event
	 * @param deliveryAddress the address where the flowers will be delivered
	 * @param client          the client associated with the order
	 * @param notes           additional notes for the order
	 * @return a new `EventOrder`
	 */
	public EventOrder createEventOrder(LocalDate eventDate, String deliveryAddress, Client client, String notes) {
		Event e = new Event();
		e.setName(client.getName()+"'s Event");
		e.setDate(eventDate.atStartOfDay());
		e.setDescription(notes);
		calendarService.save(e);
		return new EventOrder(getDefaultUserAccount(), eventDate, deliveryAddress, client, notes);
	}

	/**
	 * Creates a new `EventOrder` with the specified event date, delivery address, and client.
	 *
	 * @param eventDate       the date of the event
	 * @param deliveryAddress the address where the flowers will be delivered
	 * @param client          the client associated with the order
	 * @return a new `EventOrder`
	 */
	public EventOrder createEventOrder(LocalDate eventDate, String deliveryAddress, Client client) {
		Event e = new Event();
		e.setName(client.getName()+"'s Event");
		e.setDate(eventDate.atStartOfDay());
		e.setDescription("notes");
		calendarService.save(e);
		return new EventOrder(getDefaultUserAccount(), eventDate, deliveryAddress, client, "");
	}

	/**
	 * Creates a new `ContractOrder` with the specified contract type, start date, end date, address, client, and notes.
	 *
	 * @param contractType the type of the contract
	 * @param startDate    the start date of the contract
	 * @param endDate      the end date of the contract
	 * @param address      the address associated with the contract
	 * @param client       the client associated with the order
	 * @param notes        additional notes for the order
	 * @return a new `ContractOrder`
	 */
	public ContractOrder createContractOrder(String contractType, String frequency, LocalDate startDate, LocalDate endDate, String address, Client client, String notes) {
		Event e = new Event();
		e.setName(client.getName()+"'s Contract");
		e.setDate(startDate.atStartOfDay());
		e.setDescription(notes);
		calendarService.save(e);
		return new ContractOrder(getDefaultUserAccount(), contractType, frequency, startDate, endDate, address, client, notes);
	}

	/**
	 * Creates a new `ContractOrder` with the specified contract type, frequency, start date, end date, and client.
	 *
	 * @param contractType the type of the contract
	 * @param frequency    the frequency of the contract
	 * @param startDate    the start date of the contract
	 * @param endDate      the end date of the contract
	 * @param client       the client associated with the order
	 * @return a new `ContractOrder`
	 */
	public ContractOrder createContractOrder(String contractType, String frequency, LocalDate startDate, LocalDate endDate, String address, Client client) {
		return new ContractOrder(getDefaultUserAccount(), contractType, frequency, startDate, endDate, address, client, "");
	}

	/**
	 * Creates a new `ReservationOrder` with the specified date and time, client, and notes.
	 *
	 * @param dateTime the date and time of the reservation
	 * @param client   the client associated with the order
	 * @param notes    additional notes for the order
	 * @return a new `ReservationOrder`
	 */
	public ReservationOrder createReservationOrder(LocalDateTime dateTime, Client client, String notes) {
		Event e = new Event();
		e.setName(client.getName()+"'s Reservation");
		e.setDate(dateTime);
		e.setDescription(notes);
		calendarService.save(e);
		return new ReservationOrder(getDefaultUserAccount(), dateTime, client, notes);
	}

	/**
	 * Creates a new `ReservationOrder` with the specified date and time and client.
	 *
	 * @param dateTime the date and time of the reservation
	 * @param client   the client associated with the order
	 * @return a new `ReservationOrder`
	 */
	public ReservationOrder createReservationOrder(LocalDateTime dateTime, Client client) {
		Event e = new Event();
		e.setName(client.getName()+"'s Reservation");
		e.setDate(dateTime);
		e.setDescription("notes");
		calendarService.save(e);
		return new ReservationOrder(getDefaultUserAccount(), dateTime, client, "");
	}

	/**
	 * Creates a new `WholesalerOrder` with the default user account.
	 *
	 * @return a new `WholesalerOrder`
	 */
	public WholesalerOrder createWholesalerOrder() {
		return new WholesalerOrder(getDefaultUserAccount());
	}

	/**
	 * Retrieves the default user account.
	 * This method is used internally to assign a default user account to orders.
	 *
	 * @return the default user account
	 * @throws IllegalArgumentException if the default user account is not found
	 */
	// Just believe me that this should be here in a method and not stored in private field.
	// This cannot be in the constructor because of some very complicated spring bean init logic.
	private UserAccount getDefaultUserAccount() {
		return userAccountManagement.findByUsername("shop_worker")
			.orElseThrow(() -> new IllegalArgumentException("Default UserAccount shop_worker not found"));
	}
}