package flowershop.service;

import flowershop.product.ProductCatalog;
import flowershop.services.EventOrder;
import flowershop.services.EventOrderRepository;
import flowershop.services.EventOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.*;
import org.salespointframework.quantity.Quantity;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class EventOrderServiceTests {
	private EventOrderRepository eventOrderRepository;
	private ProductCatalog productCatalog;
	private OrderManagement<EventOrder> orderManagement;
	private EventOrderService eventOrderService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		eventOrderRepository = mock(EventOrderRepository.class);
		productCatalog = mock(ProductCatalog.class);
		orderManagement = mock(OrderManagement.class);

		eventOrderService = new EventOrderService(eventOrderRepository, productCatalog, orderManagement);

		// Mock products
		Product rose = mock(Product.class);
		when(rose.getId()).thenReturn(Product.ProductIdentifier.of("1"));
		when(rose.getName()).thenReturn("Rose");

		Product lilyBouquet = mock(Product.class);
		when(lilyBouquet.getId()).thenReturn(Product.ProductIdentifier.of("2"));
		when(lilyBouquet.getName()).thenReturn("Lily Bouquet");

		when(productCatalog.findById(Product.ProductIdentifier.of("1"))).thenReturn(Optional.of(rose));
		when(productCatalog.findById(Product.ProductIdentifier.of("2"))).thenReturn(Optional.of(lilyBouquet));

		// Mock event orders
		EventOrder eventOrder1 = mock(EventOrder.class);
		when(eventOrder1.getOrderStatus()).thenReturn(OrderStatus.OPEN);
		when(eventOrder1.getId()).thenReturn(Order.OrderIdentifier.of(UUID.randomUUID().toString()));

		EventOrder eventOrder2 = mock(EventOrder.class);
		when(eventOrder2.getOrderStatus()).thenReturn(OrderStatus.PAID);
		when(eventOrder2.getId()).thenReturn(Order.OrderIdentifier.of(UUID.randomUUID().toString()));

		List<EventOrder> orders = Arrays.asList(eventOrder1, eventOrder2);
		when(eventOrderRepository.findAll()).thenReturn(orders);
	}

	@Test
	void testFindAll() {
		List<EventOrder> orders = eventOrderService.findAll();

		assertThat(orders).isNotEmpty();
		assertThat(orders.get(0).getOrderStatus()).isEqualTo(OrderStatus.OPEN);
		assertThat(orders.get(1).getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	void testGetById() {
		EventOrder mockOrder = mock(EventOrder.class);
		UUID orderId = UUID.randomUUID();
		when(eventOrderRepository.findById(Order.OrderIdentifier.of(orderId.toString())))
			.thenReturn(Optional.of(mockOrder));

		Optional<EventOrder> result = eventOrderService.getById(orderId);

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(mockOrder);
		verify(eventOrderRepository, times(1))
			.findById(Order.OrderIdentifier.of(orderId.toString()));
	}

	@Test
	void testGetById_NotFound() {
		UUID orderId = UUID.randomUUID();
		when(eventOrderRepository.findById(Order.OrderIdentifier.of(orderId.toString())))
			.thenReturn(Optional.empty());

		Optional<EventOrder> result = eventOrderService.getById(orderId);

		assertThat(result).isNotPresent();
		verify(eventOrderRepository, times(1))
			.findById(Order.OrderIdentifier.of(orderId.toString()));
	}

	@Test
	void testSave() {
		EventOrder newOrder = mock(EventOrder.class);
		Map<String, String> products = new HashMap<>();
		products.put("products[0]", "1");
		products.put("quantities[0]", "5");

		when(eventOrderRepository.save(any(EventOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EventOrder savedOrder = eventOrderService.save(newOrder, products);

		assertThat(savedOrder).isNotNull();
		verify(newOrder, times(1)).addOrderLine(any(Product.class), any(Quantity.class));
		verify(eventOrderRepository, times(1)).save(newOrder);
	}

	@Test
	void testUpdateOrderStatus() {
		EventOrder existingOrder = mock(EventOrder.class);
		when(existingOrder.getOrderStatus()).thenReturn(OrderStatus.OPEN);

		Map<String, String> products = new HashMap<>();
		String newStatus = "COMPLETED";

		assertThrows(IllegalArgumentException.class, () ->
			eventOrderService.update(existingOrder, products, 0, newStatus, null)
		);
	}

	@Test
	void update_ThrowsException_WhenOrderIsCanceled() {
		EventOrder order = mock(EventOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELED);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> eventOrderService.update(order, Map.of(), 0, "PAID", null));

		assertEquals("Order is already canceled!", exception.getMessage());
	}

	@Test
	void update_ThrowsException_WhenOrderIsNotPaidAndCompleted() {
		EventOrder order = mock(EventOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.OPEN);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> eventOrderService.update(order, Map.of(), 0, "COMPLETED", null));

		assertEquals("Order is not paid yet!", exception.getMessage());
	}

	@Test
	void update_CancelsOrder_WhenOrderIsOpenAndCanceled() {
		EventOrder order = mock(EventOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.OPEN);

		eventOrderService.update(order, Map.of(), 0, "CANCELED", "cancel reason");

		verify(orderManagement).cancelOrder(order, "cancel reason");
		verify(eventOrderRepository, times(2)).save(order);
	}


	@Test
	void update_PaysOrder_WhenStatusIsPaid() {
		EventOrder order = mock(EventOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.OPEN);

		ChargeLine mockChargeLine = mock(ChargeLine.class);
		when(mockChargeLine.getDescription()).thenReturn("Product Description");
		Totalable<ChargeLine> mockChargeLineTotalable = mock(Totalable.class);
		when(mockChargeLineTotalable.stream()).thenReturn(Stream.of(mockChargeLine));
		when(order.getChargeLines()).thenReturn(mockChargeLineTotalable);

		Totalable<OrderLine> mockOrderLineTotalable = mock(Totalable.class);
		when(order.getOrderLines()).thenReturn(mockOrderLineTotalable);
		when(mockOrderLineTotalable.toList()).thenReturn(new ArrayList<>());

		Map<String, String> products = new HashMap<>();
		String newStatus = "PAID";

		eventOrderService.update(order, products, 0, newStatus, null);

		verify(orderManagement).payOrder(order);
		verify(eventOrderRepository).save(order);
	}

	@Test
	void update_ThrowsException_WhenTryingToCancelPaidOrder() {
		EventOrder order = mock(EventOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			eventOrderService.update(order, Map.of(), 0, "CANCELED", null)
		);

		assertEquals("Cannot cancel a paid order!", exception.getMessage());
	}

	@Test
	void update_CompletesOrder_WhenStatusIsCompleted() {
		EventOrder order = mock(EventOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);

		Map<String, String> products = new HashMap<>();
		String newStatus = "COMPLETED";

		eventOrderService.update(order, products, 0, newStatus, null);

		verify(orderManagement).completeOrder(order);
		verify(eventOrderRepository).save(order);
	}


	@Test
	void testDelete() {
		EventOrder existingOrder = mock(EventOrder.class);
		doNothing().when(eventOrderRepository).delete(existingOrder);

		eventOrderService.delete(existingOrder);

		verify(eventOrderRepository, times(1)).delete(existingOrder);
	}

	@Test
	void testSaveOrderWithProducts() {
		EventOrder newOrder = mock(EventOrder.class);
		Map<String, String> products = new HashMap<>();
		products.put("products[0]", "1");
		products.put("quantities[0]", "5");

		when(eventOrderRepository.save(any(EventOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EventOrder savedOrder = eventOrderService.save(newOrder, products);

		assertThat(savedOrder).isNotNull();
		verify(newOrder, times(1)).addOrderLine(any(Product.class), any(Quantity.class));
	}


}
