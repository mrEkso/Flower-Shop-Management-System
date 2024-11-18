package flowershop.services;

import flowershop.sales.SimpleOrder;
import jakarta.annotation.PostConstruct;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This class is needed to create all orders of type {@link AbstractOrder}.
 * It encapsulates the workaround of assigning a default {@link UserAccount}
 * to every {@link org.salespointframework.order.Order}, without which the framework seems not to work.
 */
@Service
@DependsOn("userInitializer")
public class OrderFactory {
	private final UserAccountManagement userAccountManagement;

	public OrderFactory(UserAccountManagement userAccountManagement) {
		this.userAccountManagement = userAccountManagement;
	}

	public SimpleOrder createSimpleOrder() {
		return new SimpleOrder(getDefaultUserAccount());
	}

	public EventOrder createEventOrder(LocalDate eventDate, String deliveryAddress, Client client, String notes) {
		return new EventOrder(getDefaultUserAccount(), eventDate, deliveryAddress, client, notes);
	}

	public EventOrder createEventOrder(LocalDate eventDate, String deliveryAddress, Client client) {
		return new EventOrder(getDefaultUserAccount(), eventDate, deliveryAddress, client, "");
	}

	public ContractOrder createContractOrder(String contractType, String frequency, LocalDate startDate, LocalDate endDate, Client client, String notes) {
		return new ContractOrder(getDefaultUserAccount(), contractType, startDate, endDate, frequency, client, notes);
	}

	public ContractOrder createContractOrder(String contractType, String frequency, LocalDate startDate, LocalDate endDate, Client client) {
		return new ContractOrder(getDefaultUserAccount(), contractType, startDate, endDate, frequency, client, "");
	}

	public ReservationOrder createReservationOrder(LocalDateTime dateTime, Client client, String notes) {
		return new ReservationOrder(getDefaultUserAccount(), dateTime, client, notes);
	}

	public ReservationOrder createReservationOrder(LocalDateTime dateTime, Client client) {
		return new ReservationOrder(getDefaultUserAccount(), dateTime, client, "");
	}

	// This cannot be in the constructor because of some very complicated spring bean init logic.
	//  Just believe me that it should be here in a method and not a private field.
	private UserAccount getDefaultUserAccount() {
		return userAccountManagement.findByUsername("shop_worker")
			.orElseThrow(() -> new IllegalArgumentException("Default UserAccount shop_worker not found"));
	}
}