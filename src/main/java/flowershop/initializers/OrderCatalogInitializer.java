// package flowershop.initializers;

// import flowershop.catalogs.ProductCatalog;
// import flowershop.models.Client;
// import flowershop.models.order.AbstractOrder;
// import flowershop.models.order.EventOrder;
// import flowershop.models.payment.CardPayment;
// import flowershop.services.order.OrderFactory;
// import org.salespointframework.catalog.Product;
// import org.salespointframework.core.DataInitializer;
// import org.salespointframework.order.Order;
// import org.salespointframework.order.OrderManagement;
// import org.salespointframework.quantity.Quantity;
// import org.salespointframework.useraccount.UserAccount;
// import org.salespointframework.useraccount.UserAccountManagement;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Component;
// import org.springframework.util.Assert;

// import java.time.LocalDate;
// import java.util.Objects;

// @Component
// @org.springframework.core.annotation.Order(20)
// public class OrderCatalogInitializer implements DataInitializer {

// 	private final OrderManagement<Order> orderManagement;
// 	private final ProductCatalog productCatalog;
// 	private final UserAccountManagement userAccountManagement;

// 	public OrderCatalogInitializer(OrderManagement<Order> orderManagement, ProductCatalog productCatalog, UserAccountManagement userAccountManagement) {
// 		Assert.notNull(orderManagement, "OrderManagement must not be null!");
// 		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
// 		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
// 		this.orderManagement = orderManagement;
// 		this.productCatalog = productCatalog;
// 		this.userAccountManagement = userAccountManagement;
// 	}

// 	@Override
// 	public void initialize() {
// 		if (orderManagement.findAll(Pageable.unpaged()).iterator().hasNext()) {
// 			return; // Skip initialization if orders already exist
// 		}
// 		OrderFactory orderFactory = new OrderFactory(userAccountManagement);

// 		// Create dummy client
// 		Client dummyClient = new Client("Olaf Scholz", "Platz der Republik 1, 11011 Berlin", "+49 (0)30 227 0");

// 		// Create a new event order
// 		EventOrder orderForFrauFloris = orderFactory.createEventOrder(LocalDate.now(), "Nöthnitzer Str. 46, 01187 Dresden", dummyClient);

// 		Product product = productCatalog.findByName("Rose")
// 			.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));
// 		orderForFrauFloris.addOrderLine(product, Quantity.of(2));
// 		orderManagement.save(orderForFrauFloris);


// 		// Create a new simple order
// 		Order orderForFlorisNichte = orderFactory.createSimpleOrder();
// 		orderForFlorisNichte.addOrderLine(product, Quantity.of(1));
// 		orderManagement.save(orderForFlorisNichte);
// 	}
// }

// ----------------------------- DAVID ----------------------------
// package kickstart.Davyd_Lera.initializers;

// import kickstart.Davyd_Lera.models.Client;
// import kickstart.Davyd_Lera.models.orders.ContractOrder;
// import kickstart.Davyd_Lera.models.orders.EventOrder;
// import kickstart.Davyd_Lera.models.orders.ReservationOrder;
// import kickstart.Davyd_Lera.repositories.ClientRepository;
// import kickstart.Davyd_Lera.repositories.ProductCatalog;

// import org.salespointframework.catalog.Product;
// import org.salespointframework.core.DataInitializer;
// import org.salespointframework.quantity.Quantity;
// import org.salespointframework.useraccount.UserAccount;
// import org.salespointframework.useraccount.UserAccountManagement;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Component;
// import org.springframework.util.Assert;

// import flowershop.repositories.orders.OrderFactoryRepository;

// import java.time.LocalDate;

// @Component
// @org.springframework.core.annotation.Order(20)
// public class OrderCatalogInitializer implements DataInitializer {

// 	private final OrderFactoryRepository orderFactoryRepository;
// 	private final ProductCatalog productCatalog;
// 	private final UserAccountManagement userAccountManagement;
// 	private final ClientRepository clientRepository;

// 	public OrderCatalogInitializer(OrderFactoryRepository orderFactoryRepository, ProductCatalog productCatalog, UserAccountManagement userAccountManagement, ClientRepository clientRepository) {
// 		Assert.notNull(orderFactoryRepository, "OrderFactoryRepository must not be null!");
// 		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
// 		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
// 		Assert.notNull(clientRepository, "ClientRepository must not be null!");
// 		this.orderFactoryRepository = orderFactoryRepository;
// 		this.productCatalog = productCatalog;
// 		this.userAccountManagement = userAccountManagement;
// 		this.clientRepository = clientRepository;
// 	}

// 	@Override
// 	public void initialize() {
// 		if (orderFactoryRepository.getEventOrderRepository()
// 			.findAll(Pageable.unpaged()).iterator().hasNext() &&
// 			orderFactoryRepository.getContractOrderRepository()
// 				.findAll(Pageable.unpaged()).iterator().hasNext() &&
// 			orderFactoryRepository.getReservationOrderRepository()
// 				.findAll(Pageable.unpaged()).iterator().hasNext())
// 			return; // Skip initialization if orders already exist

// 		// Fetch UserAccount for Frau Floris and Floris Nichte
// 		UserAccount frauFloris = userAccountManagement.findByUsername("frau_floris").orElseThrow(() ->
// 			new IllegalArgumentException("Frau Floris account not found"));
// 		UserAccount florisNichte = userAccountManagement.findByUsername("floris_nichte").orElseThrow(() ->
// 			new IllegalArgumentException("Floris Nichte account not found"));

// 		// Fetch Clients
// 		Client client1 = clientRepository.findByName("John Doe")
// 			.orElseThrow(() -> new IllegalArgumentException("Client not found"));
// 		Client client2 = clientRepository.findByName("Alice Johnson")
// 			.orElseThrow(() -> new IllegalArgumentException("Client not found"));

// 		// Create orders
// 		//	EventOrders
// 		EventOrder eventOrder1 = new EventOrder(LocalDate.now(),
// 			"Nöthnitzer Str. 46, 01187 Dresden", frauFloris, client1);
// 		Product product = productCatalog.findByName("Rose Bouquet")
// 			.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));
// 		eventOrder1.addOrderLine(product, Quantity.of(2));
// 		orderFactoryRepository.getEventOrderRepository().save(eventOrder1);

// 		EventOrder eventOrder2 = new EventOrder(LocalDate.now(),
// 			"Nöthnitzer Str. 46, 01187 Dresden", florisNichte, client2);
// 		eventOrder2.addOrderLine(product, Quantity.of(1));
// 		orderFactoryRepository.getEventOrderRepository().save(eventOrder2);
// 		//	ContractOrders
// 		ContractOrder contractOrder = new ContractOrder("once a week", LocalDate.now(),
// 			LocalDate.of(2026, 1, 1),
// 			"Nöthnitzer Str. 46, 01187 Dresden", frauFloris, client1);
// 		contractOrder.addOrderLine(product, Quantity.of(30));
// 		orderFactoryRepository.getContractOrderRepository().save(contractOrder);

// 		//	ReservationOrders
// 		ReservationOrder reservationOrder = new ReservationOrder(
// 			LocalDate.of(2025, 1, 1).atStartOfDay(), florisNichte, client2);
// 		reservationOrder.addOrderLine(product, Quantity.of(5));
// 		orderFactoryRepository.getReservationOrderRepository().save(reservationOrder);
// 	}
// }