 package flowershop.initializers;

 import flowershop.catalogs.ProductCatalog;
 import flowershop.models.Client;
 import flowershop.models.orders.EventOrder;
 import flowershop.models.payments.CardPayment;
 import flowershop.services.order.OrderFactory;
 import org.salespointframework.catalog.Product;
 import org.salespointframework.core.DataInitializer;
 import org.salespointframework.order.Order;
 import org.salespointframework.order.OrderManagement;
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
 		if (orderManagement.findAll(Pageable.unpaged()).iterator().hasNext()) {
 			return; // Skip initialization if orders already exist
 		}
 		OrderFactory orderFactory = new OrderFactory(userAccountManagement);

 		// Create dummy client
 		Client dummyClient = new Client("Olaf Scholz", "Platz der Republik 1, 11011 Berlin", "+49 (0)30 227 0");

 		// Create a new event order
 		EventOrder orderForFrauFloris = orderFactory.createEventOrder(LocalDate.now(), "Nöthnitzer Str. 46, 01187 Dresden", dummyClient);

 		Product product = productCatalog.findByName("Rose")
 			.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));
 		orderForFrauFloris.addOrderLine(product, Quantity.of(2));
 		orderManagement.save(orderForFrauFloris);


 		// Create a new simple order
 		Order orderForFlorisNichte = orderFactory.createSimpleOrder();
 		orderForFlorisNichte.addOrderLine(product, Quantity.of(1));
 		orderManagement.save(orderForFlorisNichte);
 	}
 }

