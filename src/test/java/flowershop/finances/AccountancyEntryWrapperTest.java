package flowershop.finances;

import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.sales.SimpleOrder;
import flowershop.sales.WholesalerOrder;
import flowershop.services.Client;
import flowershop.services.ContractOrder;
import flowershop.services.EventOrder;
import flowershop.services.ReservationOrder;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
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
	private ProductService productService;

	@BeforeEach
	void setUp() {
		productService = mock(ProductService.class);
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
		when(orderLine1.getProductName()).thenReturn("Rose");
		when(orderLine1.getQuantity()).thenReturn(Quantity.of(1));
		when(orderLine2.getProductName()).thenReturn("Lily");
		when(orderLine2.getQuantity()).thenReturn(Quantity.of(2));

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

		Flower rose = mock(Flower.class);
		ArrayList<Flower> roses = new ArrayList<>();
		roses.add(rose);
		when(productService.findFlowersByName("Rose")).thenReturn(roses);
		Flower lily = mock(Flower.class);
		ArrayList<Flower> lilys = new ArrayList<>();
		lilys.add(lily);
		when(productService.findFlowersByName("Lily")).thenReturn(lilys);
	}

	@Test
	public void categoryToStringTest() {
		assertEquals("Einfacher Verkauf", AccountancyEntryWrapper.categoryToString(Category.Einfacher_Verkauf));
		assertEquals("Einkauf", AccountancyEntryWrapper.categoryToString(Category.Einkauf));
	}

	@Test
	void testGetCategoryForWholesalerOrder() {
    	// TODO: fix this
		// AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(wholesalerOrder,LocalDateTime.now(), productService);
		// assertEquals("Einkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForContractOrder() {
		Client client = mock(Client.class);
		when(((ContractOrder)(contractOrder)).getClient()).thenReturn(client);
		when(client.getName()).thenReturn("Habibi");
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(contractOrder,LocalDateTime.now(), productService);
		assertEquals("Vertraglicher Verkauf", wrapper.getCategory());
	}


	@Test
	public void testGetClientName_EmptyClientName() {
		Client vasya = mock(Client.class);
		when(vasya.getName()).thenReturn("");
		when(((ContractOrder)contractOrder).getClient()).thenReturn(vasya);
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(contractOrder,LocalDateTime.now(), productService);
		// Test when clientName is an empty string
		//when(wrapper.getClientName()).thenReturn(""); // Assume you have a setter for clientName
		assertEquals("", wrapper.getClientName(), "Client name should be an empty string when set to an empty value");
	}

	@Test
	public void testGetClientName_ValidClientName() {
		Client vasya = mock(Client.class);
		when(vasya.getName()).thenReturn("Floris Blumenladen");
		when(((ContractOrder)contractOrder).getClient()).thenReturn(vasya);
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(contractOrder,LocalDateTime.now(), productService);
		// Test when clientName is a valid non-empty string
		//when(wrapper.getClientName()).thenReturn("Floris Blumenladen");
		assertEquals("Floris Blumenladen", wrapper.getClientName(), "Client name should return the correct value");
	}

	@Test
	void testGetCategoryForEventOrder() {
		Client client = mock(Client.class);
		when(((EventOrder)(eventOrder)).getClient()).thenReturn(client);
		when(((EventOrder)(eventOrder)).getEventDate()).thenReturn(LocalDateTime.now());
		when(client.getName()).thenReturn("Habibi");
		// TODO: fix this
		// AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder,LocalDateTime.now(), productService);
		// assertEquals("Veranstaltung Verkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForReservationOrder() {
		Client client = mock(Client.class);
		when(((ReservationOrder)(reservationOrder)).getClient()).thenReturn(client);
		when(client.getName()).thenReturn("Habibi");
		// TODO: fix this
		// AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(reservationOrder,LocalDateTime.now(), productService);
		// assertEquals("Reservierter Verkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForSimpleOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder,LocalDateTime.now(), productService);
		assertEquals("Einfacher Verkauf", wrapper.getCategory());
	}

	@Test
	void testConstructorThrowsForUnrecognizedOrder() {
		//AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(fake);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new AccountancyEntryWrapper(fake,LocalDateTime.now(), productService);
		});
		assertEquals("Order is not recognized", exception.getMessage());
	}
	@Test
	void testGetTimestamp() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder,LocalDateTime.now(), productService);
		LocalDateTime now = LocalDateTime.now();
		wrapper.getTimestamp();
		assertNotNull(wrapper.getTimestamp(), "Timestamp should not be null");
		assertTrue(wrapper.getTimestamp().isBefore(now) || wrapper.getTimestamp().isEqual(now),
			"Timestamp should be before or equal to current time");
	}

	@Test
	void testGetItemsEmptyMap() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder,LocalDateTime.now(), productService);
		assertNotNull(wrapper.getItems(), "Items map should not be null");
		//assertTrue(wrapper.getItems().isEmpty(), "Items map should be empty by default");
	}

	@Test
	void testGetItemsModifiedMap() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder,LocalDateTime.now(), productService);
		Map<String, Quantity> mockItems = new HashMap<>();
		mockItems.put("Product 1", Quantity.of(2));
		mockItems.put("Product 2", Quantity.of(5));
		wrapper.getItems().putAll(mockItems);
		wrapper.getItems();
		assertEquals(4, wrapper.getItems().size(), "Items map should have 4 entries");
		assertEquals(Quantity.of(2), wrapper.getItems().get("Product 1"), "Quantity for Product 1 should match");
		assertEquals(Quantity.of(5), wrapper.getItems().get("Product 2"), "Quantity for Product 2 should match");
	}


}


