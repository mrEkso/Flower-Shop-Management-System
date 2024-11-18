 package flowershop.services;

 import flowershop.sales.SimpleOrder;
 import org.salespointframework.order.OrderStatus;
 import org.salespointframework.useraccount.UserAccount;
 import org.salespointframework.useraccount.UserAccountManagement;
 import org.springframework.stereotype.Service;

 import java.time.LocalDate;
 import java.time.LocalDateTime;

 /**
  * This class is needed to create all orders of type {@link AbstractOrder}.
  * It encapsulates the workaround of assigning a default {@link UserAccount}
  * to every {@link org.salespointframework.order.Order}, without which the framework seems not to work.
  */
  @Service
  // FIXME: For some reason it doesn't work in runtime. It can't find the default user in userAccountManagement, therefore the exception.
 public class OrderFactory {

 	private final UserAccount defaultUserAccount;

 	public OrderFactory(UserAccountManagement userAccountManagement) {
 		this.defaultUserAccount = userAccountManagement.findByUsername("shop_worker")
 			.orElseThrow(() -> new IllegalArgumentException("Default UserAccount shop_worker not found"));
 	}

 	public SimpleOrder createSimpleOrder() {
		 return new SimpleOrder(defaultUserAccount);
 	}

 	public EventOrder createEventOrder(LocalDate eventDate, String deliveryAddress, Client client) {
 		return new EventOrder(defaultUserAccount, eventDate, deliveryAddress, client);
 	}

 	public ContractOrder createContractOrder(String contractType, String frequency, LocalDate startDate, LocalDate endDate, Client client) {
 		return new ContractOrder(defaultUserAccount, contractType, startDate, endDate, frequency, client );
 	}

 	public ReservationOrder createReservationOrder(LocalDateTime dateTime, Client client) {
 		return new ReservationOrder(defaultUserAccount, dateTime, client );
 	}

 }