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
 * The `ContractOrderService` class provides services related to managing contract orders in the flower shop system.
 * It is annotated with `@Service` to indicate that it is a Spring service component.
 */
@Service
public class ContractOrderService {
	private final ContractOrderRepository contractOrderRepository;
	private final ProductCatalog productCatalog;
	private final OrderManagement<ContractOrder> orderManagement;

	/**
	 * Constructs a `ContractOrderService` with the specified repositories and order management.
	 *
	 * @param contractOrderRepository the repository used to manage contract orders
	 * @param productCatalog          the catalog of products available in the flower shop
	 * @param orderManagement         the order management system
	 * @throws IllegalArgumentException if any of the parameters are null
	 */
	public ContractOrderService(ContractOrderRepository contractOrderRepository, ProductCatalog productCatalog, OrderManagement<ContractOrder> orderManagement) {
		Assert.notNull(contractOrderRepository, "ContractOrderRepository must not be null!");
		Assert.notNull(productCatalog, "ProductCatalog must not be null!");
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.contractOrderRepository = contractOrderRepository;
		this.productCatalog = productCatalog;
		this.orderManagement = orderManagement;
	}

	/**
	 * Retrieves all contract orders.
	 *
	 * @return a list of all contract orders
	 */
	public List<ContractOrder> findAll() {
		return contractOrderRepository.findAll(Pageable.unpaged()).toList();
	}

	/**
	 * Retrieves a contract order by its ID.
	 *
	 * @param id the ID of the contract order
	 * @return an `Optional` containing the contract order if found, or empty if not found
	 */
	public Optional<ContractOrder> getById(UUID id) {
		return contractOrderRepository.findById(Order.OrderIdentifier.of(id.toString()));
	}

	/**
	 * Saves a new contract order with the specified products.
	 *
	 * @param order    the contract order to save
	 * @param products a map of product IDs and their quantities
	 * @return the saved contract order
	 */
	public ContractOrder save(ContractOrder order, Map<String, String> products) {
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
		return contractOrderRepository.save(order);
	}

	/**
	 * Updates an existing contract order with the specified products and status.
	 *
	 * @param order        the contract order to update
	 * @param products     a map of product IDs and their quantities
	 * @param orderStatus  the new status of the order
	 * @param cancelReason the reason for cancellation, if applicable
	 * @return the updated contract order
	 * @throws IllegalArgumentException if the order is already canceled, not paid yet, or cannot be canceled
	 */
	public ContractOrder update(ContractOrder order, Map<String, String> products, String orderStatus, String cancelReason) {
		if (order.getOrderStatus().equals(OrderStatus.CANCELED))
			throw new IllegalArgumentException("Order is already canceled!");
		if (order.getOrderStatus().equals(OrderStatus.OPEN)) {
			if (OrderStatus.COMPLETED.name().equals(orderStatus))
				throw new IllegalArgumentException("Order is not paid yet!");
			if (OrderStatus.CANCELED.name().equals(orderStatus)) {
				orderManagement.cancelOrder(order, cancelReason == null || cancelReason.isBlank() ? "Reason not provided" : cancelReason);
				return contractOrderRepository.save(order);
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
			throw new IllegalArgumentException("Cannot cancel a paid order!");
		if (order.getOrderStatus().equals(OrderStatus.PAID) &&
			OrderStatus.COMPLETED.name().equals(orderStatus)) orderManagement.completeOrder(order);
		return contractOrderRepository.save(order);
	}

	/**
	 * Deletes a contract order.
	 *
	 * @param order the contract order to delete
	 */
	public void delete(ContractOrder order) {
		contractOrderRepository.delete(order);
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
