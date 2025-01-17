package flowershop.service;

import flowershop.product.ProductCatalog;
import flowershop.services.ContractOrder;
import flowershop.services.ContractOrderRepository;
import flowershop.services.ContractOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.*;
import org.salespointframework.quantity.Quantity;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ContractOrderServiceTests {

	private ContractOrderRepository contractOrderRepository;
	private ProductCatalog productCatalog;
	private OrderManagement<ContractOrder> orderManagement;
	private ContractOrderService contractOrderService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		contractOrderRepository = mock(ContractOrderRepository.class);
		productCatalog = mock(ProductCatalog.class);
		orderManagement = mock(OrderManagement.class);

		contractOrderService = new ContractOrderService(contractOrderRepository, productCatalog, orderManagement);
	}

	@Test
	void testFindAll() {
		ContractOrder order1 = mock(ContractOrder.class);
		when(order1.getOrderStatus()).thenReturn(OrderStatus.OPEN);

		ContractOrder order2 = mock(ContractOrder.class);
		when(order2.getOrderStatus()).thenReturn(OrderStatus.PAID);

		when(contractOrderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

		List<ContractOrder> orders = contractOrderService.findAll();

		assertThat(orders).isNotEmpty();
		assertThat(orders.get(0).getOrderStatus()).isEqualTo(OrderStatus.OPEN);
		assertThat(orders.get(1).getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	void testGetById() {
		UUID orderId = UUID.randomUUID();
		ContractOrder mockOrder = mock(ContractOrder.class);
		when(contractOrderRepository.findById(Order.OrderIdentifier.of(orderId.toString()))).thenReturn(Optional.of(mockOrder));

		Optional<ContractOrder> result = contractOrderService.getById(orderId);

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(mockOrder);
		verify(contractOrderRepository, times(1)).findById(Order.OrderIdentifier.of(orderId.toString()));
	}

	@Test
	void testSave() {
		ContractOrder newOrder = mock(ContractOrder.class);
		Map<String, String> products = new HashMap<>();
		products.put("products[0]", "1");
		products.put("quantities[0]", "5");

		Product product = mock(Product.class);
		when(productCatalog.findById(Product.ProductIdentifier.of("1"))).thenReturn(Optional.of(product));
		when(contractOrderRepository.save(any(ContractOrder.class))).thenReturn(newOrder);

		ContractOrder savedOrder = contractOrderService.save(newOrder, products);

		assertThat(savedOrder).isNotNull();
		verify(newOrder, times(1)).addOrderLine(product, Quantity.of(5));
		verify(contractOrderRepository, times(1)).save(newOrder);
	}

	@Test
	void testUpdateOrderStatus_ThrowsExceptionForCanceledOrder() {
		ContractOrder order = mock(ContractOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELED);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			contractOrderService.update(order, Map.of(), 0, "PAID", null));

		assertThat(exception.getMessage()).isEqualTo("Order is already canceled!");
	}

	@Test
	void testDelete() {
		ContractOrder order = mock(ContractOrder.class);
		doNothing().when(contractOrderRepository).delete(order);

		contractOrderService.delete(order);

		verify(contractOrderRepository, times(1)).delete(order);
	}

}
