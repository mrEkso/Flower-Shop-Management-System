package kickstart.Davyd_Lera.initializers;

import kickstart.Davyd_Lera.repositories.ClientRepository;
import kickstart.Davyd_Lera.repositories.ProductCatalog;
import kickstart.Davyd_Lera.models.Client;
import kickstart.Davyd_Lera.models.orders.AbstractOrder;
import kickstart.Davyd_Lera.models.orders.EventOrder;
import org.salespointframework.catalog.Product;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDate;

@Component
@org.springframework.core.annotation.Order(20)
public class OrderCatalogInitializer implements DataInitializer {

	private final OrderManagement<AbstractOrder> orderManagement;
	private final ProductCatalog productCatalog;
	private final UserAccountManagement userAccountManagement;
	private final ClientRepository clientRepository;

	public OrderCatalogInitializer(OrderManagement<AbstractOrder> orderManagement, ProductCatalog productCatalog, UserAccountManagement userAccountManagement, ClientRepository clientRepository) {
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(userAccountManagement, "UserAccountManagement must not be null!");
		Assert.notNull(clientRepository, "ClientRepository must not be null!");
		this.orderManagement = orderManagement;
		this.productCatalog = productCatalog;
		this.userAccountManagement = userAccountManagement;
		this.clientRepository = clientRepository;
	}

	@Override
	public void initialize() {
		if (orderManagement.findAll(Pageable.unpaged()).iterator().hasNext())
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

		// Create orders

		EventOrder order1 = new EventOrder(LocalDate.now(), "Nöthnitzer Str. 46, 01187 Dresden", frauFloris, client1);
		Product product = productCatalog.findByName("Rose Bouquet")
			.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));
		order1.addOrderLine(product, Quantity.of(2));
		orderManagement.save(order1);

		EventOrder order2 = new EventOrder(LocalDate.now(), "Nöthnitzer Str. 46, 01187 Dresden", florisNichte, client2);
		order2.addOrderLine(product, Quantity.of(1));
		orderManagement.save(order2);
	}
}