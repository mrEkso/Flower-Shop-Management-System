package flowershop.services.order;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.orders.ContractOrder;
import flowershop.models.orders.EventOrder;
import flowershop.repositories.orders.EventOrderRepository;
import flowershop.repositories.orders.OrderFactoryRepository;

import org.salespointframework.catalog.Product;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.quantity.Quantity;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@Service
public class EventOrderService {
	private final OrderFactoryRepository orderFactoryRepository;
	private final ProductCatalog productCatalog;
	private final OrderManagement<EventOrder> orderManagement;
	private final EventOrderRepository eventOrderRepository;

	public EventOrderService(OrderFactoryRepository orderFactoryRepository, ProductCatalog productCatalog, OrderManagement<EventOrder> orderManagement, EventOrderRepository eventOrderRepository) {
		Assert.notNull(orderFactoryRepository, "OrderFactoryRepository must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.orderFactoryRepository = orderFactoryRepository;
		this.productCatalog = productCatalog;
		this.orderManagement = orderManagement;
		this.eventOrderRepository = eventOrderRepository;
	}

	public List<EventOrder> findAll() {
		return orderFactoryRepository.getEventOrderRepository().findAll(Pageable.unpaged()).toList();
	}

	public Optional<EventOrder> getById(UUID id) {
		return orderFactoryRepository.getEventOrderRepository().findById(Order.OrderIdentifier.of(id.toString()));
	}

	public EventOrder save(EventOrder order, Map<String, String> products) {
		products.forEach((key, value) -> {
			if (key.startsWith("products[")) {
				String index = key.substring(9, key.length() - 1);
				String quantityKey = "quantities[" + index + "]";
				if (products.containsKey(quantityKey)) {
					int quantity = Integer.parseInt(products.get(quantityKey));
					productCatalog.findById(Product.ProductIdentifier.of(value))
						.ifPresent(product -> {
							Quantity qty = Quantity.of(quantity);
							order.addOrderLine(product, qty);
						});
				}
			}
		});
		return orderFactoryRepository.getEventOrderRepository().save(order);
	}

	public EventOrder update(EventOrder order, Map<String, String> products, String orderStatus, String cancelReason) {
		if (OrderStatus.PAID.name().equals(orderStatus)) orderManagement.payOrder(order);
		else if (OrderStatus.COMPLETED.name().equals(orderStatus)) orderManagement.completeOrder(order);
		else if (OrderStatus.CANCELED.name().equals(orderStatus))
			orderManagement.cancelOrder(order, cancelReason == null || cancelReason.isBlank() ? "Reason not provided" : cancelReason);
		Map<UUID, Integer> incoming = extractProducts(products);
		order.getOrderLines().toList().forEach(line -> {
			if (!incoming.containsKey(UUID.fromString(line.getProductIdentifier().toString()))) order.remove(line);
		});
		incoming.forEach((productId, quantity) -> productCatalog.findById(Product.ProductIdentifier.of(productId.toString()))
			.ifPresent(product -> {
				order.getOrderLines(product).toList().forEach(order::remove);
				order.addOrderLine(product, Quantity.of(quantity));
			}));
		return orderFactoryRepository.getEventOrderRepository().save(order);
	}

	public void delete(EventOrder order) {
		orderFactoryRepository.getEventOrderRepository().delete(order);
	}

	private Map<UUID, Integer> extractProducts(Map<String, String> products) {
		Map<UUID, Integer> productQuantities = new HashMap<>();
		products.forEach((key, value) -> {
			if (key.startsWith("products[")) {
				String index = key.substring(9, key.length() - 1);
				String quantityKey = "quantities[" + index + "]";
				if (products.containsKey(quantityKey)) {
					UUID productId = UUID.fromString(value);
					int quantity = Integer.parseInt(products.get(quantityKey));
					productQuantities.put(productId, quantity);
				}
			}
		});
		return productQuantities;
	}
}
