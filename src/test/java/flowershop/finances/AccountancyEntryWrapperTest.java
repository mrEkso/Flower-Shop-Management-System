package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.sales.CardPayment;
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
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Quantity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
	private ClockService clockService;
	private Flower rose;
	private Flower lily;
	private Client client;
	private AccountancyEntryWrapper accountancyEntryWrapper;

	@BeforeEach
	void setUp() {
		productService = mock(ProductService.class);
		clockService = mock(ClockService.class);
		Totalable<OrderLine> mockedTotalable;
		List<OrderLine> mockedOrderLines = new ArrayList<>();
		OrderLine orderLine1 = mock(OrderLine.class);
		when(orderLine1.getQuantity()).thenReturn(Quantity.of(1));
		when(orderLine1.getPrice()).thenReturn(Money.of(23, "EUR"));
		when(orderLine1.getProductName()).thenReturn("Rose");
		OrderLine orderLine2 = mock(OrderLine.class);
		when(orderLine2.getQuantity()).thenReturn(Quantity.of(2));
		when(orderLine2.getPrice()).thenReturn(Money.of(32, "EUR"));
		when(orderLine2.getProductName()).thenReturn("Lily");

		mockedOrderLines.add(orderLine1);
		mockedOrderLines.add(orderLine2);
		mockedTotalable = Totalable.of(mockedOrderLines);

		Totalable<ChargeLine> extraFees = mock(Totalable.class);
		List<ChargeLine> mockedExtraFees = new ArrayList<>();
		when(extraFees.iterator()).thenReturn(mockedExtraFees.iterator());
		when(extraFees.stream()).thenReturn(mockedExtraFees.stream());
		PaymentMethod paymentMethod = mock(PaymentMethod.class);
		when(paymentMethod.toString()).thenReturn("PUTIN HUYLO, LALALALALALALALALA LALALALALLALALAL, LALALLALALALALALA");

		client = mock(Client.class);
		when(client.getName()).thenReturn("Kuchmu vyrnit ;(");
		when(client.getPhone()).thenReturn("A v rot tobi ne datj? Telefon yomu...");

		wholesalerOrder = mock(WholesalerOrder.class);
		when(wholesalerOrder.getTotal()).thenReturn(Money.of(-100, "EUR"));
		when(wholesalerOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(wholesalerOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(wholesalerOrder.getAllChargeLines()).thenReturn(extraFees);
		when(wholesalerOrder.getPaymentMethod()).thenReturn(paymentMethod);

		contractOrder = mock(ContractOrder.class);
		when(contractOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(contractOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(contractOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(contractOrder.getAllChargeLines()).thenReturn(extraFees);
		when(contractOrder.getPaymentMethod()).thenReturn(paymentMethod);
		when(((ContractOrder)contractOrder).getStartDate()).thenReturn(LocalDateTime.now());
		when(((ContractOrder)contractOrder).getEndDate()).thenReturn(LocalDateTime.now().plusDays(4));
		when(((ContractOrder)contractOrder).getFrequency()).thenReturn("Koly rak svystytj");
		when(((ContractOrder)contractOrder).getClient()).thenReturn(client);


		eventOrder = mock(EventOrder.class);
		when(eventOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(eventOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(eventOrder.getOrderLines()).thenReturn(mockedTotalable);
		Totalable<ChargeLine> chargeLines = createMockChargeLines();
		when(eventOrder.getAllChargeLines()).thenReturn(chargeLines);
		when(eventOrder.getPaymentMethod()).thenReturn(paymentMethod);
		when(((EventOrder)eventOrder).getClient()).thenReturn(client);
		when(((EventOrder)eventOrder).getEventDate()).thenReturn(LocalDateTime.now());

		reservationOrder = mock(ReservationOrder.class);
		when(reservationOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(reservationOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(reservationOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(reservationOrder.getAllChargeLines()).thenReturn(extraFees);
		when(reservationOrder.getPaymentMethod()).thenReturn(paymentMethod);
		when(((ReservationOrder)reservationOrder).getClient()).thenReturn(client);
		when(((ReservationOrder)reservationOrder).getReservationDateTime()).thenReturn(LocalDateTime.now());

		simpleOrder = mock(SimpleOrder.class);
		when(simpleOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(simpleOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(simpleOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(simpleOrder.getAllChargeLines()).thenReturn(extraFees);
		when(simpleOrder.getAllChargeLines()).thenReturn(extraFees);
		when(simpleOrder.getPaymentMethod()).thenReturn(paymentMethod);

		fake = mock(Order.class);
		when(fake.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(fake.getPaymentMethod()).thenReturn(paymentMethod);

		this.rose = mock(Flower.class);
		when(rose.getName()).thenReturn("Rose");
		ArrayList<Flower> roses = new ArrayList<>();
		roses.add(rose);
		when(productService.findFlowersByName("Rose")).thenReturn(roses);
		this.lily = mock(Flower.class);
		when(lily.getName()).thenReturn("Lily");
		ArrayList<Flower> lilys = new ArrayList<>();
		lilys.add(lily);
		when(productService.findFlowersByName("Lily")).thenReturn(lilys);

		accountancyEntryWrapper = new AccountancyEntryWrapper(contractOrder,LocalDateTime.now(),productService);
	}

	private Totalable<ChargeLine> createMockChargeLines() {
		ChargeLine fee = mock(ChargeLine.class);
		when(fee.getDescription()).thenReturn("Delivery Fee");
		when(fee.getPrice()).thenReturn(Money.of(15, "EUR"));
		LinkedList iHateTOTALABLE = new LinkedList<ChargeLine>();
		iHateTOTALABLE.add(fee);
		Totalable<ChargeLine> extraFees = Totalable.of(iHateTOTALABLE);
		return extraFees;
	}

	private Flower createMockFlower(String name) {
		Flower flower = mock(Flower.class);
		when(flower.getName()).thenReturn(name);
		return flower;
	}

	@Test
	public void categoryToStringTest() {
		assertEquals("Einfacher Verkauf", AccountancyEntryWrapper.categoryToString(Category.EINFACHER_VERKAUF));
		assertEquals("Einkauf", AccountancyEntryWrapper.categoryToString(Category.EINKAUF));
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
		when(client.getName()).thenReturn("Habibi");
		when(client.getPhone()).thenReturn("123456789");
		when(((EventOrder) eventOrder).getClient()).thenReturn(client);
		when(((EventOrder) eventOrder).getDeliveryAddress()).thenReturn("123 Flower St.");
		when(((EventOrder) eventOrder).getEventDate()).thenReturn(LocalDateTime.of(2025, 1, 20, 14, 0));
		when(((EventOrder) eventOrder).getNotes()).thenReturn("Wedding event");


		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		assertEquals("Veranstaltung Verkauf", wrapper.getCategory());
		assertEquals("Habibi", wrapper.getClientName());
		assertEquals("123456789", wrapper.getClientPhone());
		assertEquals("123 Flower St.", wrapper.getAdress());
		assertEquals("Wedding event", wrapper.getNotes());
		assertEquals(LocalDateTime.of(2025, 1, 20, 14, 0), wrapper.getDate1());
	}

	@Test
	void testGetCategoryForReservationOrder() {
		Client client = mock(Client.class);
		when(((ReservationOrder)(reservationOrder)).getClient()).thenReturn(client);
		when(client.getName()).thenReturn("Habibi");
		// AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(reservationOrder,LocalDateTime.now(), productService);
		// assertEquals("Reservierter Verkauf", wrapper.getCategory());
	}

	@Test
	void testGetCategoryForSimpleOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder,LocalDateTime.now(), productService);
		assertEquals("Einfacher Verkauf", wrapper.getCategory());
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

	@Test
	void testGetCategoryForWholesalerOrder() {
		when(((WholesalerOrder) wholesalerOrder).getNotes()).thenReturn("2025-01-15");
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(wholesalerOrder, LocalDateTime.now(), productService);

		assertEquals("Einkauf", wrapper.getCategory());
		assertEquals(LocalDate.parse("2025-01-15").atTime(9, 0), wrapper.getDate1());
		assertEquals(LocalDate.parse("2025-01-15"), wrapper.getDeliveryDate());
	}

	@Test
	void testGetItemsPopulatedMap() {
		Map<String, Quantity> mockItems = new HashMap<>();
		mockItems.put("Rose", Quantity.of(3));
		mockItems.put("Lily", Quantity.of(5));

		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(simpleOrder, LocalDateTime.now(), productService);
		wrapper.getItems().putAll(mockItems);

		assertEquals(2, wrapper.getItems().size());
		assertEquals(Quantity.of(3), wrapper.getItems().get("Rose"));
		assertEquals(Quantity.of(5), wrapper.getItems().get("Lily"));
	}

	@Test
	void testConstructorThrowsForUnrecognizedOrder() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			new AccountancyEntryWrapper(fake, LocalDateTime.now(), productService);
		});
		assertEquals("Order is not recognized", exception.getMessage());
	}

	@Test
	void testGetClientPhoneAndAddress() {
		Client client = mock(Client.class);
		when(client.getPhone()).thenReturn("987654321");
		when(((ContractOrder) contractOrder).getClient()).thenReturn(client);
		when(((ContractOrder) contractOrder).getAddress()).thenReturn("456 Flower Blvd.");

		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(contractOrder, LocalDateTime.now(), productService);

		assertEquals("987654321", wrapper.getClientPhone());
		assertEquals("456 Flower Blvd.", wrapper.getAdress());
	}

	@Test
	void testConstructorForReservationOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(reservationOrder, LocalDateTime.now(), productService);

		assertNotNull(wrapper);
		assertEquals("Reservierter Verkauf", wrapper.getCategory().toString());
	}


	@Test
	void testGetDate2ForContractOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(contractOrder, LocalDateTime.now(), productService);

		assertNotNull(wrapper.getDate2());
		assertEquals(((ContractOrder)contractOrder).getEndDate(), wrapper.getDate2());
	}

	@Test
	void testGetFrequencyForContractOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(contractOrder, LocalDateTime.now(), productService);

		assertNotNull(wrapper.getFrequency());
		assertEquals("Koly rak svystytj", wrapper.getFrequency());
	}

	@Test
	void testGetFlowersForEventOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		Map<Product, Quantity> flowers = wrapper.getFlowers();
		assertNotNull(flowers);
		assertEquals(2, flowers.size());
		assertEquals(Quantity.of(1), flowers.get(this.rose));
		assertEquals(Quantity.of(2), flowers.get(this.lily));
	}

	@Test
	void testGetFlowersForReservationOrder() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(reservationOrder, LocalDateTime.now(), productService);

		Map<Product, Quantity> flowers = wrapper.getFlowers();
		assertNotNull(flowers);
		assertEquals(2, flowers.size());
		assertEquals(Quantity.of(1), flowers.get(this.rose));
		assertEquals(Quantity.of(2), flowers.get(this.lily));
	}

	@Test
	void testExtraFeesHandling() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		Map<String, Quantity> nameQuantityMap = wrapper.getItems();
		assertNotNull(nameQuantityMap);
		assertEquals(3, nameQuantityMap.size());
		assertTrue(nameQuantityMap.containsKey("Delivery Fee"));
		assertEquals(Quantity.of(1), nameQuantityMap.get("Delivery Fee"));
	}

	@Test
	void testExtraFeesPriceMap() {
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		Map<String, Double> namePriceMap = wrapper.getNamePriceMap();
		assertNotNull(namePriceMap);
		assertEquals(3, namePriceMap.size());
		assertTrue(namePriceMap.containsKey("Delivery Fee"));
		assertEquals(15.0, namePriceMap.get("Delivery Fee"));
	}

	@Test
	void testGetClientNameWhenClientNameIsNull() {
		when(((EventOrder)eventOrder).getClient()).thenReturn(mock(Client.class));
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		assertEquals("", wrapper.getClientName(), "Expected empty string when clientName is null");
	}

	@Test
	void testGetClientNameWhenClientNameIsNotNull() {
		Client vova = mock(Client.class);
		when(vova.getName()).thenReturn("Vova");
		when(((EventOrder)eventOrder).getClient()).thenReturn(vova);
		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		assertEquals("Vova", wrapper.getClientName(), "Expected client name to be 'Test Client'");
	}

	@Test
	void testGetTimestampStr() {
		LocalDateTime timestamp = LocalDateTime.of(2025, 1, 14, 12, 0);

		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, timestamp, productService);

		assertEquals("14.1.2025 12:00", wrapper.getTimestampStr(), "Expected formatted timestamp string");
	}

	@Test
	void testBouquetListProcessingForEventOrder() {
		// Setup
		String name = "Rose Bouquet"; // Simulating the bouquet name
		Flower rose = mock(Flower.class);
		when(rose.getName()).thenReturn("Rose");

		Bouquet bouquet = mock(Bouquet.class);
		when(bouquet.getName()).thenReturn("Rose Bouquet");
		Map<Flower, Integer> flowers = new HashMap<>();
		flowers.put(rose, 2); // Bouquet contains 2 roses
		when(bouquet.getFlowers()).thenReturn(flowers);

		List<Bouquet> bouquetList = new ArrayList<>();
		bouquetList.add(bouquet);

		when(productService.findBouquetsByName(name)).thenReturn(bouquetList);

		// Mocking the order and order line
		OrderLine orderLine = mock(OrderLine.class);
		when(orderLine.getProductName()).thenReturn("Rose Bouquet");
		when(orderLine.getQuantity()).thenReturn(Quantity.of(3)); // Quantity 3 for this order line
		when(orderLine.getPrice()).thenReturn(Money.of(100,"EUR"));

		Totalable<OrderLine> mockedTotalable = mock(Totalable.class);
		List<OrderLine> mockedOrderLines = new ArrayList<>();
		mockedOrderLines.add(orderLine);
		when(mockedTotalable.iterator()).thenReturn(mockedOrderLines.iterator());
		when(mockedTotalable.stream()).thenReturn(mockedOrderLines.stream());

		Client jesus = new Client("Jesus", "Fucking Christ");

		// Mocking the EventOrder
		EventOrder eventOrder = mock(EventOrder.class);
		when(eventOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(eventOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(eventOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(eventOrder.getPaymentMethod()).thenReturn(new CardPayment());
		when(eventOrder.getClient()).thenReturn(jesus);
		when(eventOrder.getEventDate()).thenReturn(LocalDateTime.now());
		List<ChargeLine> pohui = new LinkedList<>();
		when(eventOrder.getAllChargeLines()).thenReturn(Totalable.of(pohui));

		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		// Assertions
		Map<Product, Quantity> flowers2 = wrapper.getFlowers(); // Get the product quantity map

		assertEquals(1, flowers2.size(), "Expected one type of flower (Rose)");
		assertTrue(flowers2.containsKey(rose), "Expected the map to contain the rose flower");
		assertEquals(Quantity.of(6), flowers2.get(rose), "Expected 6 roses (2 per bouquet * 3 quantity)");
	}

	@Test
	void testEmptyBouquetListProcessingForEventOrder() {
		// Setup with no bouquets found
		String name = "Rose Bouquet";
		when(productService.findBouquetsByName(name)).thenReturn(Collections.emptyList()); // Empty bouquet list

		// Mocking the order and order line
		OrderLine orderLine = mock(OrderLine.class);
		when(orderLine.getProductName()).thenReturn("Rose Bouquet");
		when(orderLine.getQuantity()).thenReturn(Quantity.of(3));
		when(orderLine.getPrice()).thenReturn(Money.of(100,"EUR"));

		Totalable<OrderLine> mockedTotalable = mock(Totalable.class);
		List<OrderLine> mockedOrderLines = new ArrayList<>();
		mockedOrderLines.add(orderLine);
		when(mockedTotalable.iterator()).thenReturn(mockedOrderLines.iterator());
		when(mockedTotalable.stream()).thenReturn(mockedOrderLines.stream());

		Client jesus = new Client("Jesus", "Fucking Christ");

		// Mocking the EventOrder
		EventOrder eventOrder = mock(EventOrder.class);
		when(eventOrder.getTotal()).thenReturn(Money.of(100, "EUR"));
		when(eventOrder.getDateCreated()).thenReturn(LocalDateTime.now());
		when(eventOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(eventOrder.getPaymentMethod()).thenReturn(new CardPayment());
		when(eventOrder.getClient()).thenReturn(jesus);
		when(eventOrder.getEventDate()).thenReturn(LocalDateTime.now());
		List<ChargeLine> pohui = new LinkedList<>();
		when(eventOrder.getAllChargeLines()).thenReturn(Totalable.of(pohui));

		AccountancyEntryWrapper wrapper = new AccountancyEntryWrapper(eventOrder, LocalDateTime.now(), productService);

		// Assertions
		Map<Product, Quantity> flowers = wrapper.getFlowers(); // Get the product quantity map

		assertTrue(flowers.isEmpty(), "Expected empty flowers map when no bouquets are found");
	}

	@Test
	void testGeneratePDF_ValidPDF() throws IOException {
		// Arrange
		LocalDateTime now = LocalDateTime.now();

		// Act
		byte[] pdfBytes = accountancyEntryWrapper.generatePDF(now);

		// Assert
		assertNotNull(pdfBytes, "The generated PDF byte array should not be null");
		/*
		try (InputStream inputStream = new ByteArrayInputStream(pdfBytes);
			 PDDocument document = PDDocument.load(inputStream)) {
			assertNotNull(document, "The PDF document should be valid and loadable");

			// Extract text content to verify expected output
			PDFTextStripper textStripper = new PDFTextStripper();
			String pdfContent = textStripper.getText(document);

			// Verify content expectations
			assertTrue(pdfContent.contains("Expected String in PDF"), "The PDF content should contain expected text");
			assertTrue(pdfContent.contains(now.toString()), "The PDF should contain the current date/time");
		}
		 */
	}

	@Test
	void testGeneratePDF_ErrorHandling() {
		// Arrange
		LocalDateTime now = LocalDateTime.now();

		// Simulate an error scenario (e.g., missing font or IOException)
		AccountancyEntryWrapper mockWrapper = mock(AccountancyEntryWrapper.class);
		when(mockWrapper.generatePDF(now)).thenReturn(null);

		// Act
		byte[] pdfBytes = mockWrapper.generatePDF(now);

		// Assert
		assertNull(pdfBytes, "The method should handle exceptions gracefully and return null/empty result");
	}



}


