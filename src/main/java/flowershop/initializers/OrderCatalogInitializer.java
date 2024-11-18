 package flowershop.initializers;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.Client;
import flowershop.models.orders.ContractOrder;
import flowershop.models.orders.EventOrder;
import flowershop.models.orders.ReservationOrder;
import flowershop.models.payments.CardPayment;
import flowershop.repositories.ClientRepository;
import flowershop.repositories.orders.OrderFactoryRepository;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

 import java.time.LocalDate;
 import java.util.Objects;

 @Component
 @org.springframework.core.annotation.Order(20)
 public class OrderCatalogInitializer implements DataInitializer {

 	private final OrderManagement<Order> orderManagement;
 	private final ProductCatalog productCatalog;
 	private final UserAccountManagement userAccountManagement;

 	public OrderCatalogInitializer(OrderManagement<Order> orderManagement, ProductCatalog productCatalog, UserAccountManagement userAccountManagement) {
 		Assert.notNull(orderManagement, "OrderManagement must not be null!");
 		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
 		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
 		this.orderManagement = orderManagement;
 		this.productCatalog = productCatalog;
 		this.userAccountManagement = userAccountManagement;
 	}

	@Override
	public void initialize() {
		if (orderFactoryRepository.getEventOrderRepository()
			.findAll(Pageable.unpaged()).iterator().hasNext() &&
			orderFactoryRepository.getContractOrderRepository()
				.findAll(Pageable.unpaged()).iterator().hasNext() &&
			orderFactoryRepository.getReservationOrderRepository()
				.findAll(Pageable.unpaged()).iterator().hasNext())
			return; // Skip initialization if orders already exist

		// Fetch UserAccount for Frau Floris and Floris Nichte
		UserAccount frauFloris = userAccountManagement.findByUsername("frau_floris").orElseThrow(() ->
			new IllegalArgumentException("Frau Floris account not found"));
		UserAccount florisNichte = userAccountManagement.findByUsername("floris_nichte").orElseThrow(() ->
			new IllegalArgumentException("Floris Nichte account not found"));

		// Fetch Clients
		Client client1 = clientRepository.findByName("John Doe")
			.orElseThrow(() -> new IllegalArgumentException("Client not found"));
		Client client2 = clientRepository.findByName("Alice Johnson")
			.orElseThrow(() -> new IllegalArgumentException("Client not found"));

		// Create order lines
		Product rose = productCatalog.findByName("Rose")
			.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));
		Product roseLilyBouquet = productCatalog.findByName("Rose and Lily Bouquet")
			.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));

		// Create orders
		//	ContractOrders
		ContractOrder contractOrder = new ContractOrder("once a week", LocalDate.now(),
			LocalDate.of(2026, 1, 1),
			"Nöthnitzer Str. 46, 01187 Dresden", frauFloris, client1);
		contractOrder.addOrderLine(rose, Quantity.of(30));
		contractOrder.addOrderLine(roseLilyBouquet, Quantity.of(2));
		contractOrder.setPaymentMethod(Cash.CASH);
		orderFactoryRepository.getContractOrderRepository().save(contractOrder);

		//	EventOrders
		EventOrder eventOrder1 = new EventOrder(LocalDate.now(),
			"Nöthnitzer Str. 46, 01187 Dresden", frauFloris, client1);
		eventOrder1.addOrderLine(rose, Quantity.of(2));
		eventOrder1.setPaymentMethod(Cash.CASH);
		orderFactoryRepository.getEventOrderRepository().save(eventOrder1);

		EventOrder eventOrder2 = new EventOrder(LocalDate.now(),
			"Nöthnitzer Str. 46, 01187 Dresden", florisNichte, client2);
		eventOrder2.addOrderLine(roseLilyBouquet, Quantity.of(1));
		eventOrder2.setPaymentMethod(Cash.CASH);
		orderFactoryRepository.getEventOrderRepository().save(eventOrder2);

		//	ReservationOrders
		ReservationOrder reservationOrder = new ReservationOrder(
			LocalDate.of(2025, 1, 1).atStartOfDay(), florisNichte, client2);
		reservationOrder.addOrderLine(roseLilyBouquet, Quantity.of(5));
		reservationOrder.setPaymentMethod(new CardPayment());
		orderFactoryRepository.getReservationOrderRepository().save(reservationOrder);
	}
}