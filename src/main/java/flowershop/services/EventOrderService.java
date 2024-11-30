package flowershop.services;

import flowershop.calendar.Event;
import flowershop.product.ProductCatalog;
import flowershop.calendar.CalendarService;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

@Service
public class EventOrderService {
	private final EventOrderRepository eventOrderRepository;
	private final ProductCatalog productCatalog;
	private final OrderManagement<EventOrder> orderManagement;

	public EventOrderService(EventOrderRepository eventOrderRepository, ProductCatalog productCatalog, OrderManagement<EventOrder> orderManagement) {
		Assert.notNull(eventOrderRepository, "EventOrderRepository must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.eventOrderRepository = eventOrderRepository;
		this.productCatalog = productCatalog;
		this.orderManagement = orderManagement;
	}

	public List<EventOrder> findAll() {
		return eventOrderRepository.findAll(Pageable.unpaged()).toList();
	}

	public Optional<EventOrder> getById(UUID id) {
		return eventOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	public EventOrder save(EventOrder order, Map<String, String> products) {
		order.setPaymentMethod(Cash.CASH);
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


		return eventOrderRepository.save(order);
	}

	public EventOrder update(EventOrder order, Map<String, String> products, String orderStatus, String cancelReason) {
		if (order.getOrderStatus().equals(OrderStatus.OPEN)) {
			if (OrderStatus.CANCELED.name().equals(orderStatus)) {
				orderManagement.cancelOrder(order, cancelReason == null || cancelReason.isBlank() ? "Reason not provided" : cancelReason);
				return eventOrderRepository.save(order);
			}
			Map<UUID, Integer> incoming = extractProducts(products);
			order.getOrderLines().toList().forEach(line -> {
				if (!incoming.containsKey(UUID.fromString(line.getProductIdentifier().toString()))) order.remove(line);
			});
			incoming.forEach((productId, quantity) -> productCatalog.findById(Product.ProductIdentifier.of(productId.toString()))
				.ifPresent(product -> {
					order.getOrderLines(product).toList().forEach(order::remove);
					order.addOrderLine(product, Quantity.of(quantity));
				}));
			if (OrderStatus.PAID.name().equals(orderStatus)) orderManagement.payOrder(order);
		}
		if (order.getOrderStatus().equals(OrderStatus.PAID) &&
			OrderStatus.COMPLETED.name().equals(orderStatus)) orderManagement.completeOrder(order);
		return eventOrderRepository.save(order);
	}

	public void delete(EventOrder order) {
		eventOrderRepository.delete(order);
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