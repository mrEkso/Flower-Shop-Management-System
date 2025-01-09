package flowershop.services;

import flowershop.calendar.CalendarService;
import flowershop.calendar.Event;
import flowershop.sales.SimpleOrder;
import flowershop.sales.WholesalerOrder;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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
	public EventOrder createEventOrder(LocalDateTime eventDate, String deliveryAddress, Client client, String notes) {
		EventOrder order = new EventOrder(getDefaultUserAccount(), eventDate, deliveryAddress, client, notes);
		Event e = new Event(client.getName() + "'s Event", eventDate, notes, "event", UUID.fromString(order.getId().toString()));
		calendarService.save(e);
		return order;
	}
	/**
	 * Creates a new `EventOrder` with the specified event date, delivery address, and client.
	 *
	 * @param eventDate       the date of the event
	 * @param deliveryAddress the address where the flowers will be delivered
	 * @param client          the client associated with the order
	 * @return a new `EventOrder`
	 */
	public EventOrder createEventOrder(LocalDateTime eventDate, String deliveryAddress, Client client) {
		EventOrder order = new EventOrder(getDefaultUserAccount(), eventDate, deliveryAddress, client, "");
		Event e = new Event(client.getName() + "'s Event", eventDate, "", "event", UUID.fromString(order.getId().toString()));
		calendarService.save(e);
		return order;
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
	public ContractOrder createContractOrder(String contractType, String frequency, LocalDateTime startDate, LocalDateTime endDate, String address, Client client, String notes) {
		ContractOrder order = new ContractOrder(getDefaultUserAccount(), contractType, frequency, startDate, endDate, address, client, notes);
		if(order.getContractType().equalsIgnoreCase("recurring")){
			calendarService.createReccuringEvent(client.getName() + "'s Contract", startDate, endDate, notes, frequency, "contract", UUID.fromString(order.getId().toString()));
		}
		else
		{
			calendarService.save(new Event(client.getName() + "'s Contract", startDate, notes, "contract", UUID.fromString(order.getId().toString())));
		}
		return order;
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
	public ContractOrder createContractOrder(String contractType, String frequency, LocalDateTime startDate, LocalDateTime endDate, Client client) {
		return new ContractOrder(getDefaultUserAccount(), frequency, contractType, startDate, endDate, frequency, client, "");
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
		ReservationOrder order = new ReservationOrder(getDefaultUserAccount(), dateTime, client, notes);
		Event e = new Event(client.getName() + "'s Reservation", dateTime, notes, "reservation", UUID.fromString(order.getId().toString()));
		calendarService.save(e);
		return order;
	}

	/**
	 * Creates a new `ReservationOrder` with the specified date and time and client.
	 *
	 * @param dateTime the date and time of the reservation
	 * @param client   the client associated with the order
	 * @return a new `ReservationOrder`
	 */
	public ReservationOrder createReservationOrder(LocalDateTime dateTime, Client client) {
		ReservationOrder order = new ReservationOrder(getDefaultUserAccount(), dateTime, client, "");
		Event e = new Event(client.getName() + "'s Reservation", dateTime, "", "reservation", UUID.fromString(order.getId().toString()));
		calendarService.save(e);
		return order;
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