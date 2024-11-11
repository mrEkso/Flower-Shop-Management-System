package flowershop.services.order;

import flowershop.models.Client;
import flowershop.models.order.*;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This class is needed to create all orders of type {@link AbstractOrder}.
 * It hides the workaround of assigning a default {@link UserAccount} to every {@link org.salespointframework.order.Order} , without which the framework seems not to work.
 */
public class OrderFactory {

	private final UserAccountManagement userAccountManagement;
	private final UserAccount defaultUserAccount;

	public OrderFactory(UserAccountManagement userAccountManagement) {
		this.userAccountManagement = userAccountManagement;
		this.defaultUserAccount = userAccountManagement.findByUsername("shop_worker")
			.orElseThrow(() -> new IllegalArgumentException("Default UserAccount shop_worker not found"));
	}

	public SimpleOrder createSimpleOrder() {
		return new SimpleOrder(defaultUserAccount);
	}

	public EventOrder createEventOrder(LocalDate eventDate, String deliveryAddress, Client client) {
		return new EventOrder(defaultUserAccount, eventDate, deliveryAddress, client);
	}

	public ContractOrder createContractOrder(String frequency, LocalDate startDate, LocalDate endDate, OrderStatus orderStatus, Client client) {
		return new ContractOrder(defaultUserAccount, frequency, startDate, endDate, orderStatus, client );
	}

	public ReservationOrder createReservationOrder(OrderStatus orderStatus, LocalDateTime dateTime, Client client) {
		return new ReservationOrder(defaultUserAccount, orderStatus, dateTime, client );
	}

}