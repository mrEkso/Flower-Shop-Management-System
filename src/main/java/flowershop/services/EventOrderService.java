package flowershop.services;

import flowershop.product.ProductCatalog;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.ChargeLine;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;

/**
 * The `EventOrderService` class provides services related to managing event orders in the flower shop system.
 * It is annotated with `@Service` to indicate that it is a Spring service component.
 */
@Service
public class EventOrderService {
	private final EventOrderRepository eventOrderRepository;
	private final ProductCatalog productCatalog;
	private final OrderManagement<EventOrder> orderManagement;

	/**
	 * Constructs an `EventOrderService` with the specified repositories and order management.
	 *
	 * @param eventOrderRepository the repository used to manage event orders
	 * @param productCatalog       the catalog of products available in the flower shop
	 * @param orderManagement      the order management system
	 * @throws IllegalArgumentException if any of the parameters are null
	 */
	public EventOrderService(EventOrderRepository eventOrderRepository, ProductCatalog productCatalog,
							 OrderManagement<EventOrder> orderManagement) {
		Assert.notNull(eventOrderRepository, "EventOrderRepository must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.eventOrderRepository = eventOrderRepository;
		this.productCatalog = productCatalog;
		this.orderManagement = orderManagement;
	}

	/**
	 * Retrieves all event orders.
	 *
	 * @return a list of all event orders
	 */
	public List<EventOrder> findAll() {
		List<EventOrder> orders = (List<EventOrder>) eventOrderRepository.findAll();
		orders.sort(Comparator.comparingInt(this::getOrderStatusPriority));
		return orders;
	}

	/**
	 * Assigns a priority to each order status for sorting purposes.
	 *
	 * @param order the event order
	 * @return the priority of the order status
	 */
	private int getOrderStatusPriority(EventOrder order) {
		return switch (order.getOrderStatus()) {
			case OPEN -> 1;
			case PAID -> 2;
			case COMPLETED -> 3;
			case CANCELED -> 4;
		};
	}

	/**
	 * Retrieves an event order by its ID.
	 *
	 * @param id the ID of the event order
	 * @return an `Optional` containing the event order if found, or empty if not found
	 */
	public Optional<EventOrder> getById(UUID id) {
		return eventOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	/**
	 * Saves a new event order with the specified products.
	 *
	 * @param order    the event order to save
	 * @param products a map of product IDs and their quantities
	 * @return the saved event order
	 */
	public EventOrder save(EventOrder order, Map<String, String> products) {
		order.setPaymentMethod(Cash.CASH);
		products.forEach((key, value) -> {
			if (key.startsWith("products[")) {
				String index = key.substring(9, key.length() - 1);
				String quantityKey = "quantities[" + index + "]";
				if (products.containsKey(quantityKey)) {
					int quantity = Integer.parseInt(products.get(quantityKey));
					productCatalog.findById(Product.ProductIdentifier.of(value)).ifPresent(product -> {
						Quantity qty = Quantity.of(quantity);
						order.addOrderLine(product, qty);
					});
				}
			}
		});


		return eventOrderRepository.save(order);
	}

	/**
	 * Updates an existing event order with the specified products and status.
	 *
	 * @param order         the event order to update
	 * @param products      a map of product IDs and their quantities
	 * @param deliveryPrice the new delivery price for the order
	 * @param orderStatus   the new status of the order
	 * @param cancelReason  the reason for cancellation, if applicable
	 * @return the updated event order
	 * @throws IllegalArgumentException if the order is already canceled, not paid yet, or cannot be canceled
	 */
	public EventOrder update(EventOrder order, Map<String, String> products, int deliveryPrice,
							 String orderStatus, String cancelReason) {
		switch (order.getOrderStatus()) {
			case CANCELED -> throw new IllegalArgumentException("Order is already canceled!");
			case OPEN -> handleOpenOrder(order, products, deliveryPrice, orderStatus, cancelReason);
			case PAID -> {
				if (OrderStatus.OPEN.name().equals(orderStatus)) {
					throw new IllegalArgumentException("Order is already paid!");
				}
				if (OrderStatus.CANCELED.name().equals(orderStatus)) {
					throw new IllegalArgumentException("Cannot cancel a paid order!");
				}
				if (OrderStatus.COMPLETED.name().equals(orderStatus)) {
					orderManagement.completeOrder(order);
				}
			}
			case COMPLETED -> {
				if (!OrderStatus.COMPLETED.name().equals(orderStatus)) {
					throw new IllegalArgumentException("Order is already completed!");
				}
			}
			default -> throw new IllegalArgumentException("Unsupported order status: " + order.getOrderStatus());
		}
		return eventOrderRepository.save(order);
	}

	/**
	 * Handles logic for orders with OPEN status.
	 *
	 * @param order         the event order to update
	 * @param products      a map of product IDs and their quantities
	 * @param deliveryPrice the new delivery price for the order
	 * @param orderStatus   the new status of the order
	 * @param cancelReason  the reason for cancellation, if applicable
	 * @throws IllegalArgumentException if the order is not paid yet or cannot be canceled
	 */
	private void handleOpenOrder(EventOrder order, Map<String, String> products, int deliveryPrice,
								 String orderStatus, String cancelReason) {
		if (OrderStatus.COMPLETED.name().equals(orderStatus)) {
			throw new IllegalArgumentException("Order is not paid yet!");
		}
		if (OrderStatus.CANCELED.name().equals(orderStatus)) {
			orderManagement.cancelOrder(order, cancelReason == null || cancelReason.isBlank() ?
				"Reason not provided" : cancelReason);
			eventOrderRepository.save(order);
			return;
		}
		List<ChargeLine> chargeLinesToRemove = order.getChargeLines().stream()
			.filter(chargeLine -> chargeLine.getDescription().equals("Delivery Price"))
			.toList();
		chargeLinesToRemove.forEach(order::remove);
		order.addChargeLine(Money.of(deliveryPrice, "EUR"), "Delivery Price");
		Map<UUID, Integer> incoming = extractProducts(products);
		order.getOrderLines().toList().forEach(line -> {
			if (!incoming.containsKey(UUID.fromString(line.getProductIdentifier().toString()))) {
				order.remove(line);
			}
		});
		incoming.forEach((productId, quantity) -> productCatalog.findById(
				Product.ProductIdentifier.of(productId.toString()))
			.ifPresent(product -> {
				order.getOrderLines(product).toList().forEach(order::remove);
				order.addOrderLine(product, Quantity.of(quantity));
			}));
		if (OrderStatus.PAID.name().equals(orderStatus)) {
			orderManagement.payOrder(order);
		}
	}

	/**
	 * Deletes an event order.
	 *
	 * @param order the event order to delete
	 */
	public void delete(EventOrder order) {
		eventOrderRepository.delete(order);
	}

	/**
	 * Extracts product quantities from the provided map.
	 *
	 * @param products a map of product IDs and their quantities
	 * @return a map of product IDs and their quantities
	 */
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