package flowershop.initializers;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.order.AbstractOrder;
import flowershop.models.payment.CardPayment;
import flowershop.models.payment.CashPayment;
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

import java.util.Objects;

@Component
@org.springframework.core.annotation.Order(20)
public class OrderCatalogInitializer implements DataInitializer {

	private final OrderManagement<AbstractOrder> orderManagement;
	private final ProductCatalog productCatalog;
	private final UserAccountManagement userAccountManagement;

	public OrderCatalogInitializer(OrderManagement<AbstractOrder> orderManagement, ProductCatalog productCatalog, UserAccountManagement userAccountManagement) {
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

		// Fetch UserAccount for Frau Floris
		UserAccount frauFloris = userAccountManagement.findByUsername("frau_floris").orElseThrow(() ->
			new IllegalArgumentException("Frau Floris account not found"));

		// Create a new order for Frau Floris
		Order orderForFrauFloris = new Order(Objects.requireNonNull(frauFloris.getId()), new CashPayment());
		Product product = productCatalog.findByName("Rose Bouquet")
			.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));
		orderForFrauFloris.addOrderLine(product, Quantity.of(2));
		orderManagement.save(orderForFrauFloris);

		// Fetch UserAccount for Floris Nichte
		UserAccount florisNichte = userAccountManagement.findByUsername("floris_nichte").orElseThrow(() ->
			new IllegalArgumentException("Floris Nichte account not found"));

		// Create a new order for Floris Nichte
		Order orderForFlorisNichte = new Order(Objects.requireNonNull(florisNichte.getId()), new CardPayment());
		orderForFlorisNichte.addOrderLine(product, Quantity.of(1));
		orderManagement.save(orderForFlorisNichte);
	}
}