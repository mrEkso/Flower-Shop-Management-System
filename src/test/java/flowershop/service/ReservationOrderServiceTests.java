package flowershop.service;

import flowershop.product.ProductCatalog;
import flowershop.services.ReservationOrder;
import flowershop.services.ReservationOrderRepository;
import flowershop.services.ReservationOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.salespointframework.order.*;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReservationOrderServiceTests {

	private ReservationOrderRepository reservationOrderRepository;
	private ProductCatalog productCatalog;
	private OrderManagement<ReservationOrder> orderManagement;
	private ReservationOrderService reservationOrderService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		reservationOrderRepository = mock(ReservationOrderRepository.class);
		productCatalog = mock(ProductCatalog.class);
		orderManagement = mock(OrderManagement.class);

		reservationOrderService = new ReservationOrderService(reservationOrderRepository, productCatalog, orderManagement);
	}

	@Test
	void testFindAll() {
		ReservationOrder order1 = mock(ReservationOrder.class);
		when(order1.getOrderStatus()).thenReturn(OrderStatus.OPEN);

		ReservationOrder order2 = mock(ReservationOrder.class);
		when(order2.getOrderStatus()).thenReturn(OrderStatus.PAID);

		when(reservationOrderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

		List<ReservationOrder> orders = reservationOrderService.findAll();

		assertThat(orders).isNotEmpty();
		assertThat(orders.get(0).getOrderStatus()).isEqualTo(OrderStatus.OPEN);
		assertThat(orders.get(1).getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	void testGetById() {
		ReservationOrder order = mock(ReservationOrder.class);
		UUID orderId = UUID.randomUUID();
		when(reservationOrderRepository.findById(Order.OrderIdentifier.of(orderId.toString())))
			.thenReturn(Optional.of(order));

		Optional<ReservationOrder> result = reservationOrderService.getById(orderId);

		assertThat(result).isPresent().contains(order);
		verify(reservationOrderRepository).findById(Order.OrderIdentifier.of(orderId.toString()));
	}

	@Test
	void testGetById_NotFound() {
		UUID orderId = UUID.randomUUID();
		when(reservationOrderRepository.findById(Order.OrderIdentifier.of(orderId.toString())))
			.thenReturn(Optional.empty());

		Optional<ReservationOrder> result = reservationOrderService.getById(orderId);

		assertThat(result).isNotPresent();
		verify(reservationOrderRepository).findById(Order.OrderIdentifier.of(orderId.toString()));
	}

	@Test
	void testSave_ThrowsException_WhenProductNotFound() {
		ReservationOrder order = mock(ReservationOrder.class);
		Map<String, String> products = new HashMap<>();
		products.put("products[0]", "invalid-id");
		products.put("quantities[0]", "5");

		when(productCatalog.findById(any())).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> reservationOrderService.save(order, products));
	}

	@Test
	void testUpdate_ThrowsException_WhenOrderIsCanceled() {
		ReservationOrder order = mock(ReservationOrder.class);
		when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELED);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			reservationOrderService.update(order, Map.of(), "PAID", null, null));

		assertThat(exception.getMessage()).isEqualTo("Order is already canceled!");
	}

	@Test
	void testUpdate_PaysOrder_WhenStatusIsPaid() {
		ReservationOrder order = mock(ReservationOrder.class);
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

		reservationOrderService.update(order, products, newStatus, null, null);

		verify(orderManagement).payOrder(order);
		verify(reservationOrderRepository).save(order);
	}

	@Test
	void testDelete() {
		ReservationOrder order = mock(ReservationOrder.class);
		doNothing().when(reservationOrderRepository).delete(order);

		reservationOrderService.delete(order);

		verify(reservationOrderRepository).delete(order);
	}
}
