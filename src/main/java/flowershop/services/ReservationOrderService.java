package flowershop.services;

import flowershop.product.ProductCatalog;
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

/**
 * The `ReservationOrderService` class provides services related to managing reservation orders in the flower shop system.
 * It is annotated with `@Service` to indicate that it is a Spring service component.
 */
@Service
public class ReservationOrderService {
	private final ReservationOrderRepository reservationOrderRepository;
	private final ProductCatalog productCatalog;
	private final OrderManagement<ReservationOrder> orderManagement;

	/**
	 * Constructs a `ReservationOrderService` with the specified repositories and order management.
	 *
	 * @param reservationOrderRepository the repository used to manage reservation orders
	 * @param productCatalog             the catalog of products available in the flower shop
	 * @param orderManagement            the order management system
	 * @throws IllegalArgumentException if any of the parameters are null
	 */
	public ReservationOrderService(ReservationOrderRepository reservationOrderRepository, ProductCatalog productCatalog, OrderManagement<ReservationOrder> orderManagement) {
		Assert.notNull(reservationOrderRepository, "ReservationOrderRepository must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.reservationOrderRepository = reservationOrderRepository;
		this.productCatalog = productCatalog;
		this.orderManagement = orderManagement;
	}

	/**
	 * Retrieves all reservation orders.
	 *
	 * @return a list of all reservation orders
	 */
	public List<ReservationOrder> findAll() {
		List<ReservationOrder> orders = (List<ReservationOrder>) reservationOrderRepository.findAll();
		orders.sort(Comparator.comparingInt(this::getOrderStatusPriority));
		return orders;
	}

	/**
	 * Assigns a priority to each order status for sorting purposes.
	 *
	 * @param order the reservation order
	 * @return the priority of the order status
	 */
	private int getOrderStatusPriority(ReservationOrder order) {
		return switch (order.getOrderStatus()) {
			case OPEN -> 1;
			case PAID -> 2;
			case COMPLETED -> 3;
			case CANCELED -> 4;
			default -> 5;
		};
	}

	/**
	 * Retrieves a reservation order by its ID.
	 *
	 * @param id the ID of the reservation order
	 * @return an `Optional` containing the reservation order if found, or empty if not found
	 */
	public Optional<ReservationOrder> getById(UUID id) {
		return reservationOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	/**
	 * Saves a new reservation order with the specified products.
	 *
	 * @param order    the reservation order to save
	 * @param products a map of product IDs and their quantities
	 * @return the saved reservation order
	 */
	public ReservationOrder save(ReservationOrder order, Map<String, String> products) {
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
		return reservationOrderRepository.save(order);
	}

	/**
	 * Updates an existing reservation order with the specified products, status, and reservation status.
	 *
	 * @param order             the reservation order to update
	 * @param products          a map of product IDs and their quantities
	 * @param orderStatus       the new status of the order
	 * @param cancelReason      the reason for cancellation, if applicable
	 * @param reservationStatus the new status of the reservation
	 * @return the updated reservation order
	 * @throws IllegalArgumentException if the order is already canceled, not paid yet, or cannot be canceled
	 */
	public ReservationOrder update(ReservationOrder order, Map<String, String> products, String orderStatus, String cancelReason, String reservationStatus) {
		if (order.getOrderStatus().equals(OrderStatus.CANCELED))
			throw new IllegalArgumentException("Order is already canceled!");
		if (order.getOrderStatus().equals(OrderStatus.OPEN)) {
			if (OrderStatus.COMPLETED.name().equals(orderStatus)) {
				throw new IllegalArgumentException("Order is not paid yet!");
			}
			if (OrderStatus.CANCELED.name().equals(orderStatus)) {
				orderManagement.cancelOrder(order, cancelReason == null || cancelReason.isBlank() ? "Reason not provided" : cancelReason);
				return reservationOrderRepository.save(order);
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
			OrderStatus.CANCELED.name().equals(orderStatus))
			throw new IllegalArgumentException("Cannot cancel a paid order");
		if (order.getOrderStatus().equals(OrderStatus.PAID) &&
			OrderStatus.COMPLETED.name().equals(orderStatus)) orderManagement.completeOrder(order);
		if (reservationStatus != null && !reservationStatus.isBlank())
			order.setReservationStatus(ReservationStatus.valueOf(reservationStatus));
		return reservationOrderRepository.save(order);
	}

	/**
	 * Deletes a reservation order.
	 *
	 * @param order the reservation order to delete
	 */
	public void delete(ReservationOrder order) {
		reservationOrderRepository.delete(order);
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

	public void removeProductFromOrder(UUID orderId, UUID productId) {
		ReservationOrder order = getById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

		// Remove the product
		order.getOrderLines().stream()
			.filter(line -> line.getProductIdentifier().equals(productId))
			.findFirst()
			.ifPresent(order::remove);

		save(order, new HashMap<>());
	}
}