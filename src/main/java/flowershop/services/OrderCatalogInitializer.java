package flowershop.services;

import flowershop.product.ProductCatalog;
import flowershop.sales.CardPayment;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

/**
 * The `OrderCatalogInitializer` class initializes the order catalog with predefined data.
 * It implements the `DataInitializer` interface and is annotated with `@Component` and `@Order` to indicate
 * that it is a Spring component and to specify the order of initialization.
 */
@Component
@Order(20)
public class OrderCatalogInitializer implements DataInitializer {
	private final EventOrderRepository eventOrderRepository;
	private final ContractOrderRepository contractOrderRepository;
	private final ReservationOrderRepository reservationOrderRepository;
	private final ProductCatalog productCatalog;
	private final ClientRepository clientRepository;
	private final OrderFactory orderFactory;

	/**
	 * Constructs an `OrderCatalogInitializer` with the specified repositories, product catalog, and order factory.
	 *
	 * @param eventOrderRepository       the repository used to manage event orders
	 * @param contractOrderRepository    the repository used to manage contract orders
	 * @param reservationOrderRepository the repository used to manage reservation orders
	 * @param productCatalog             the catalog of products available in the flower shop
	 * @param clientRepository           the repository used to manage clients
	 * @param orderFactory               the factory used to create orders
	 * @throws IllegalArgumentException if any of the parameters are null
	 */
	public OrderCatalogInitializer(EventOrderRepository eventOrderRepository, 
			ContractOrderRepository contractOrderRepository,
			ReservationOrderRepository reservationOrderRepository, ProductCatalog productCatalog,
			ClientRepository clientRepository, OrderFactory orderFactory) {
		Assert.notNull(eventOrderRepository, "EventOrderRepository must not be null!");
		Assert.notNull(contractOrderRepository, "ContractOrderRepository must not be null!");
		Assert.notNull(reservationOrderRepository, "ReservationOrderRepository must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(clientRepository, "ClientRepository must not be null!");
		Assert.notNull(orderFactory, "OrderFactory must not be null!");
		this.eventOrderRepository = eventOrderRepository;
		this.contractOrderRepository = contractOrderRepository;
		this.reservationOrderRepository = reservationOrderRepository;
		this.productCatalog = productCatalog;
		this.clientRepository = clientRepository;
		this.orderFactory = orderFactory;
	}

	/**
	 * Initializes the order catalog with predefined data.
	 * If orders already exist in the repositories, the initialization is skipped.
	 */
	@Override
	public void initialize() {
		if (eventOrderRepository.findAll(Pageable.unpaged()).iterator().hasNext() &&
			contractOrderRepository.findAll(Pageable.unpaged()).iterator().hasNext() &&
			reservationOrderRepository.findAll(Pageable.unpaged()).iterator().hasNext()) {
			return; // Skip initialization if orders already exist
		}

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

		// Create and save orders using OrderFactory
		// ContractOrders
		ContractOrder contractOrder = orderFactory.createContractOrder(
			"Recurring", "weekly", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusYears(1),
			"Nöthnitzer Str. 46, 01187 Dresden", client1, "Weekly flower delivery " +
				"+ flower arrangement + watering");
		contractOrder.setPaymentMethod(Cash.CASH);
		contractOrder.addOrderLine(rose, Quantity.of(8));
		contractOrder.addOrderLine(roseLilyBouquet, Quantity.of(2));
		contractOrder.addChargeLine(Money.of(40, "EUR"), "Service Price");
		contractOrderRepository.save(contractOrder);

		// EventOrders
		EventOrder eventOrder1 = orderFactory.createEventOrder(
			LocalDateTime.of(2026, 1, 1, 12, 0),
			"Nöthnitzer Str. 46, 01187 Dresden", client1);
		eventOrder1.addOrderLine(rose, Quantity.of(2));
		eventOrder1.setPaymentMethod(Cash.CASH);
		eventOrder1.addChargeLine(Money.of(20, "EUR"), "Delivery Price");
		eventOrderRepository.save(eventOrder1);

		EventOrder eventOrder2 = orderFactory.createEventOrder(
			LocalDateTime.of(2026, 1, 1, 12, 0),
			"Nöthnitzer Str. 46, 01187 Dresden", client2);
		eventOrder2.addOrderLine(roseLilyBouquet, Quantity.of(1));
		eventOrder2.setPaymentMethod(Cash.CASH);
		eventOrder2.addChargeLine(Money.of(20, "EUR"), "Delivery Price");
		eventOrderRepository.save(eventOrder2);

		// ReservationOrders
		ReservationOrder reservationOrder = orderFactory.createReservationOrder(
			LocalDateTime.now().plusDays(1), client2);
		reservationOrder.addOrderLine(roseLilyBouquet, Quantity.of(5));
		reservationOrder.setPaymentMethod(new CardPayment());
		reservationOrderRepository.save(reservationOrder);
	}
}