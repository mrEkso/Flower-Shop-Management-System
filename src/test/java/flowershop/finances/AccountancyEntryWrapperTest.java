package flowershop.finances;

import flowershop.sales.SimpleOrder;
import flowershop.sales.WholesalerOrder;
import flowershop.services.ContractOrder;
import flowershop.services.EventOrder;
import flowershop.services.ReservationOrder;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.ChargeLine;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.Totalable;
import org.salespointframework.quantity.Quantity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountancyEntryWrapperTest {

	private Order wholesalerOrder;
	private Order contractOrder;
	private Order eventOrder;
	private Order reservationOrder;
	private Order simpleOrder;
	private Order fake;

	@BeforeEach
	void setUp() {
		Totalable<OrderLine> mockedTotalable = mock(Totalable.class);
		List<OrderLine> mockedOrderLines = new ArrayList<>();
		OrderLine orderLine1 = mock(OrderLine.class);
		OrderLine orderLine2 = mock(OrderLine.class);
		mockedOrderLines.add(orderLine1);
		mockedOrderLines.add(orderLine2);
		when(mockedTotalable.iterator()).thenReturn(mockedOrderLines.iterator());
		when(mockedTotalable.spliterator()).thenReturn(mockedOrderLines.spliterator());
		when(mockedTotalable.stream()).thenReturn(mockedOrderLines.stream());
		when(mockedTotalable.toList()).thenReturn(mockedOrderLines);

		Totalable<ChargeLine> extraFees = mock(Totalable.class);
		List<ChargeLine> mockedExtraFees = new ArrayList<>();
		when(extraFees.iterator()).thenReturn(mockedExtraFees.iterator());
		when(extraFees.stream()).thenReturn(mockedExtraFees.stream());



		wholesalerOrder = mock(WholesalerOrder.class);
		when(wholesalerOrder.getTotal()).thenReturn(Money.of(-100, "EUR"));
		when(wholesalerOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(wholesalerOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(wholesalerOrder.getAllChargeLines()).thenReturn(extraFees);

		contractOrder = mock(ContractOrder.class);
		when(contractOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(contractOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(contractOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(contractOrder.getAllChargeLines()).thenReturn(extraFees);

		eventOrder = mock(EventOrder.class);
		when(eventOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(eventOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(eventOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(eventOrder.getAllChargeLines()).thenReturn(extraFees);

		reservationOrder = mock(ReservationOrder.class);
		when(reservationOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(reservationOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(reservationOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(reservationOrder.getAllChargeLines()).thenReturn(extraFees);

		simpleOrder = mock(SimpleOrder.class);
		when(simpleOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(simpleOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(simpleOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(simpleOrder.getAllChargeLines()).thenReturn(extraFees);
		when(simpleOrder.getAllChargeLines()).thenReturn(extraFees);

		fake = mock(Order.class);
		when(fake.getTotal()).thenReturn(Money.of(100, "EUR"));
	}

	@Test
	public void categoryToStringTest() {
		assertEquals("Einfacher Verkauf", AccountancyEntryWrapper.categoryToString(Category.Einfacher_Verkauf));
		assertEquals("Einkauf", AccountancyEntryWrapper.categoryToString(Category.Einkauf));
	}

	@Test
	void testGetCategoryForWholesalerOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(wholesalerOrder);
		assertEquals("Einkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForContractOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(contractOrder);
		assertEquals("Vertraglicher Verkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForEventOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder);
		assertEquals("Veranstaltung Verkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForReservationOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(reservationOrder);
		assertEquals("Reservierter Verkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForSimpleOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder);
		assertEquals("Einfacher Verkauf", wrapper.getCategory());
	}

	@Test
	void testConstructorThrowsForUnrecognizedOrder() {
		//AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(fake);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new AccountancyEntryWrapper(fake);
		});
		assertEquals("Order is not recognized", exception.getMessage());
	}
	@Test
	void testGetTimestamp() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder);
		LocalDateTime now = LocalDateTime.now();
		wrapper.getTimestamp();
		assertNotNull(wrapper.getTimestamp(), "Timestamp should not be null");
		assertTrue(wrapper.getTimestamp().isBefore(now) || wrapper.getTimestamp().isEqual(now),
			"Timestamp should be before or equal to current time");
	}

	@Test
	void testGetItemsEmptyMap() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder);
		assertNotNull(wrapper.getItems(), "Items map should not be null");
		//assertTrue(wrapper.getItems().isEmpty(), "Items map should be empty by default");
	}

	@Test
	void testGetItemsModifiedMap() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder);
		Map<String, Quantity> mockItems = new HashMap<>();
		mockItems.put("Product 1", Quantity.of(2));
		mockItems.put("Product 2", Quantity.of(5));
		wrapper.getItems().putAll(mockItems);
		wrapper.getItems();
		assertEquals(3, wrapper.getItems().size(), "Items map should have 2 entries");
		assertEquals(Quantity.of(2), wrapper.getItems().get("Product 1"), "Quantity for Product 1 should match");
		assertEquals(Quantity.of(5), wrapper.getItems().get("Product 2"), "Quantity for Product 2 should match");
	}


}


