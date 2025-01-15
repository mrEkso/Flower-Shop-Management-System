package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.clock.PendingOrder;
import flowershop.inventory.DeletedProduct;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.sales.InsufficientFundsException;
import flowershop.sales.SalesService;
import flowershop.sales.SimpleOrder;
import flowershop.services.AbstractOrder;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.accountancy.OrderPaymentEntry;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.*;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Streamable;

import javax.money.MonetaryAmount;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class CashRegisterServiceTest {

	private static final Logger log = LoggerFactory.getLogger(CashRegisterServiceTest.class);
	@Mock
	private OrderManagement<AbstractOrder> orderManagement;
	@Mock
	private CashRegisterRepository cashRegisterRepository;
	@Mock
	private ClockService clockService;
	@Mock
	private ProductService productService;
	@Mock
	private SalesService salesService;
	private CashRegister cashRegister;
	@Mock
	private Cart cart;
	/*
	@Mock
	private Streamable<AbstractOrder> previousOrders;

	 */

	@InjectMocks
	private CashRegisterService cashRegisterService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		Streamable<AbstractOrder> mockPreviousOrders = Streamable.empty();
		// Mock a CashRegister instance
		cashRegister = mock(CashRegister.class);
		Money mockBalance = Money.of(100, "EUR");

		LinkedList<AccountancyEntryWrapper> entries = new LinkedList<>();
		AccountancyEntryWrapper entry1 = mock(AccountancyEntryWrapper.class);
		when(entry1.getValue()).thenReturn(Money.of(100, "EUR"));
		when(entry1.getTimestamp()).thenReturn(LocalDateTime.now());
		when(entry1.getCategory()).thenReturn("Einfacher Verkauf");
		when(entry1.isRevenue()).thenReturn(true);
		when(entry1.getId()).thenReturn(AccountancyEntry.AccountancyEntryIdentifier.of("1"));
		entries.add(entry1);

		AccountancyEntryWrapper entry2 = mock(AccountancyEntryWrapper.class);
		when(entry2.getValue()).thenReturn(Money.of(-100, "EUR"));
		when(entry2.getTimestamp()).thenReturn(LocalDateTime.now());
		when(entry2.getCategory()).thenReturn("Einkauf");
		when(entry2.isRevenue()).thenReturn(false);
		entries.add(entry2);

		AccountancyEntryWrapper entry3 = mock(AccountancyEntryWrapper.class);
		when(entry3.getValue()).thenReturn(Money.of(0.5, "EUR"));
		when(entry3.getTimestamp()).thenReturn(LocalDateTime.now());
		when(entry3.getCategory()).thenReturn("Einfacher Verkauf");
		when(entry3.isRevenue()).thenReturn(true);
		entries.add(entry3);

		when(cashRegister.getAccountancyEntries()).thenReturn(new HashSet<>(entries));

		when(cashRegister.getBalance()).thenReturn(mockBalance);
		when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.of(cashRegister));
	//when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.empty());
		when(orderManagement.findBy(OrderStatus.PAID)).thenReturn(mockPreviousOrders);
		when(clockService.now()).thenReturn(LocalDateTime.now());
		when(clockService.getCurrentDate()).thenReturn(LocalDate.now());

	}


	@Test
	void testConstructorInitializesSafely() {
		assertNotNull(cashRegisterService, "CashRegisterService should be initialized correctly.");
	}

	@Test
	void testFindAllReturnsEmptyStreamable() {
		// Act
		Streamable<AccountancyEntry> result = cashRegisterService.findAll(AccountancyEntry.class);

		// Assert
		assertNotNull(result, "The result should not be null");
		assertTrue(result.isEmpty(), "The result Streamable should be empty");
	}

	@Test
	void testGetCashRegisterById() {
		Long id = 1L;
		CashRegister mockCashRegister = mock(CashRegister.class);
		when(cashRegisterRepository.findById(id)).thenReturn(Optional.of(mockCashRegister));

		CashRegister result = cashRegisterService.getCashRegisterById(id);
		assertNotNull(result, "Cash register should not be null");
		assertEquals(mockCashRegister, result, "Expected CashRegister not returned");
	}

	@Test
	void testGetCashRegisterWhenNotFound() {
		when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.empty());
		IllegalStateException exception = assertThrows(
			IllegalStateException.class,
			() -> {
				// Call the method that uses `.orElseThrow` internally
				cashRegisterService.getCashRegister();
			},
			"Expected an IllegalStateException to be thrown"
		);
		assertEquals("CashRegister instance not found", exception.getMessage());
	}

	@Test
	void testGetBalance() {
		CashRegister mockCashRegister = mock(CashRegister.class);
		Money mockBalance = Money.of(100, "EUR");
		when(mockCashRegister.getBalance()).thenReturn(mockBalance);
		when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.of(mockCashRegister));

		MonetaryAmount result = cashRegisterService.getBalance();
		assertNotNull(result, "Balance shouldn't be null");
		assertEquals(mockBalance, result, "Wrong balance");
	}

	@Test
	void testAddEntry() {
		AccountancyEntryWrapper entry = mock(AccountancyEntryWrapper.class);
		when(cashRegisterRepository.save(any(CashRegister.class))).thenReturn(null);
		when(entry.getValue()).thenReturn(Money.of(100, "EUR"));
		when(entry.getCategory()).thenReturn("Einfacher Verkauf");
		AccountancyEntry result = cashRegisterService.add(entry);
		assertNotNull(result, "AccountancyEntry should not be null");
		verify(cashRegisterRepository, times(1)).save(any(CashRegister.class));
	}

	@Test
	void testOnOrderPaid() throws InsufficientFundsException {
		Totalable<OrderLine> mockedTotalable = mock(Totalable.class);
		List<OrderLine> mockedOrderLines = new ArrayList<>();
		OrderLine orderLine1 = mock(OrderLine.class);
		when(orderLine1.getQuantity()).thenReturn(Quantity.of(2));
		when(orderLine1.getPrice()).thenReturn(Money.of(35, "EUR"));
		OrderLine orderLine2 = mock(OrderLine.class);
		when(orderLine2.getQuantity()).thenReturn(Quantity.of(4));
		when(orderLine2.getPrice()).thenReturn(Money.of(352, "EUR"));
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


		OrderEvents.OrderPaid event = mock(OrderEvents.OrderPaid.class);
		SimpleOrder mockOrder = mock(SimpleOrder.class);
		when(event.getOrder()).thenReturn(mockOrder);
		when(mockOrder.getTotal()).thenReturn(Money.of(50, "EUR"));
		when(mockOrder.getOrderLines()).thenReturn(mockedTotalable);
		when(mockOrder.getAllChargeLines()).thenReturn(extraFees);
		when(mockOrder.getPaymentMethod()).thenReturn(mock(PaymentMethod.class));
		cashRegisterService.onOrderPaid(event);

		verify(cashRegisterRepository, times(1)).save(any(CashRegister.class));
	}

	@Test
	void testFindAllEntries() {
		/*
		CashRegister mockCashRegister = mock(CashRegister.class);
		LinkedList<AccountancyEntry> entries = new LinkedList<>();
		entries.add(mock(AccountancyEntry.class));
		when(mockCashRegister.getAccountancyEntries()).thenReturn(new HashSet<>(entries));
*/
		Streamable<AccountancyEntry> result = cashRegisterService.findAll();
		assertNotNull(result, "Streamable of entries should not be null");
		assertEquals(3, result.toList().size(), "Expected one entry in the list");
	}

	@Test
	void testFilterEntriesByCategory() {
		CashRegister mockCashRegister = mock(CashRegister.class);
		Category category = Category.EINKAUF;
		LinkedList<AccountancyEntry> entries = new LinkedList<>();
		entries.add(mock(AccountancyEntry.class));
		when(mockCashRegister.getAccountancyEntries()).thenReturn(new HashSet<>(entries));


		Streamable<AccountancyEntry> result = cashRegisterService.filterEntries(category);
		assertNotNull(result, "Filtered entries should not be null");
	}

	@Test
	void testCreateFinancialReportDay() {
		LocalDateTime day = LocalDateTime.now();
		Interval interval = Interval.from(day.minusDays(1)).to(day); // Assuming a custom Interval implementation

		/*
		CashRegister mockCashRegister = mock(CashRegister.class);
		when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.of(mockCashRegister));
*/
		when(productService.getDeletedProducts()).thenReturn(new ArrayList<>());
		DailyFinancialReport report = cashRegisterService.createFinancialReportDay(day);
		assertNotNull(report, "Financial report for the day cannot be null.");
	}

	@Test
	void testFilterIncome() {
		LinkedList<AccountancyEntry> filteredEntries = cashRegisterService.filterIncomeOrSpending(true);

		assertEquals(2, filteredEntries.size(), "Should return 2 income entries");
		filteredEntries.forEach(entry -> assertEquals(true, entry.isRevenue(), "Each entry should be income"));
	}

	@Test
	void testFilterSpending() {
		LinkedList<AccountancyEntry> filteredEntries = cashRegisterService.filterIncomeOrSpending(false);

		assertEquals(1, filteredEntries.size(), "Should return 2 spending entries");
		filteredEntries.forEach(entry -> assertEquals(false, entry.isRevenue(), "Each entry should be spending"));
	}

	@Test
	void testFindReturnsNull() {
		Interval mockInterval = Interval.from(LocalDateTime.MIN).to(LocalDateTime.now());
		Streamable<AccountancyEntry> result = cashRegisterService.find(mockInterval, AccountancyEntry.class);

		assertNull(result, "The find method should return null");
	}

	@Test
	void testFindWithDifferentTypeReturnsEmptyMap() {

		Interval mockInterval = Interval.from(LocalDateTime.MIN).to(LocalDateTime.now());
		TemporalAmount duration = Duration.ofDays(1);

		Map<Interval, Streamable<AccountancyEntry>> result = cashRegisterService.find(mockInterval, duration, AccountancyEntry.class);

		assertNotNull(result, "The result should not be null for any type");
		assertTrue(result.isEmpty(), "The result map should be empty for any type");
	}

	@Test
	void testGetReturnsEmptyOptional() {
		Set<AccountancyEntry> entries = cashRegisterRepository.findFirstByOrderById().get().getAccountancyEntries();
		AccountancyEntry.AccountancyEntryIdentifier identifier = entries.stream().toList().get(0).getId();

		Optional<AccountancyEntry> result = cashRegisterService.get(identifier);
		assertNotNull(result, "The result should not be null");
		assertTrue(result.isEmpty(), "The result Optional should be empty");
	}

	@Test
	void testGetWithTypeReturnsEmptyOptional() {
		Set<AccountancyEntry> entries = cashRegisterRepository.findFirstByOrderById().get().getAccountancyEntries();
		AccountancyEntry.AccountancyEntryIdentifier identifier = entries.stream().toList().get(0).getId();

		Optional<AccountancyEntryWrapper> result = cashRegisterService.get(identifier, AccountancyEntryWrapper.class);

		assertNotNull(result, "The result should not be null for any type");
		assertTrue(result.isEmpty(), "The result Optional should be empty for any type");
	}

	@Test
	void testAddNullEntry() {
		// Test when entry is null
		AccountancyEntry result = cashRegisterService.add(null);
		assertNull(result, "Expected null return for null entry");
	}

	@Test
	void testAddOrderPaymentEntry() {
		// Test when entry is OrderPaymentEntry
		AccountancyEntry orderPaymentEntry = mock(OrderPaymentEntry.class);
		AccountancyEntry result = cashRegisterService.add(orderPaymentEntry);
		assertNull(result, "Expected null return for OrderPaymentEntry");
	}

	@Test
	void testAddEntryWithCategoryEinkauf() {
		// Test when entry has category "Einkauf"
		AccountancyEntryWrapper entry = mock(AccountancyEntryWrapper.class);
		when(entry.getCategory()).thenReturn("Einkauf");
		when(entry.getFlowers()).thenReturn(new HashMap<>());
		when(entry.getValue()).thenReturn(Money.of(-50, "EUR"));
		when(entry.getDeliveryDate()).thenReturn(LocalDate.from(LocalDateTime.now().plusDays(1)));
		when(clockService.nextWorkingDay()).thenReturn(LocalDate.from(LocalDateTime.now().plusDays(2)));

		// Mocking PendingOrder behavior
		Set<PendingOrder> pendingOrders = new HashSet<>();
		when(cashRegister.getPendingOrders()).thenReturn(pendingOrders);

		// Call the method under test
		AccountancyEntry result = cashRegisterService.add(entry);

		// Assertions
		assertNotNull(result, "Expected a non-null return value");
		assertEquals(1, pendingOrders.size(), "Expected one pending order added");
		assertEquals(ClockService.nextWorkingDay(LocalDate.now()), pendingOrders.iterator().next().getDueDate(),
			"Expected the correct delivery date for pending order");
	}

	@Test
	void testAddEntryWithCategoryVeranstaltungVerkauf() {
		// Test when entry has category "Veranstaltung Verkauf" and future delivery date
		AccountancyEntryWrapper entry = mock(AccountancyEntryWrapper.class);
		when(entry.getCategory()).thenReturn("Veranstaltung Verkauf");
		Flower product = mock(Flower.class);
		when(product.getPrice()).thenReturn(Money.of(50, "EUR"));
		when(product.getName()).thenReturn("Rose");
		Map<Product, Quantity> flowers = new HashMap<Product, Quantity>();
		flowers.put(product, Quantity.of(2));
		when(entry.getDeliveryDate()).thenReturn(LocalDate.from(LocalDateTime.now().plusDays(3))); // Future date
		when(entry.getFlowers()).thenReturn(flowers);
		when(entry.getValue()).thenReturn(Money.of(150, "EUR"));
		Cart cart = new Cart();

		cart.addOrUpdateItem(product, Quantity.of(1));
		when(cart.getPrice()).thenReturn(Money.of(150, "EUR"));
		when(cashRegister.getBalance()).thenReturn(Money.of(100, "EUR"));
		//when(cart.isEmpty()).thenReturn(false);

		// Mocking salesService behavior
		doNothing().when(salesService).buyProductsFromBasket(eq(cart), anyString(), anyString());

		// Call the method under test
		AccountancyEntry result = cashRegisterService.add(entry);

		// Assertions
		assertNotNull(result, "Expected a non-null return value");
		verify(salesService).buyProductsFromBasket(any(Cart.class), eq("Card"), anyString());

	}

	@Test
	void testAddEntryWithNegativeCartPrice() {
		// Test when cart price is negative, should return null
		Flower product = mock(Flower.class);
		when(product.getPrice()).thenReturn(Money.of(-50, "EUR"));
		when(product.getName()).thenReturn("Rose");
		Map<Product, Quantity> flowers = new HashMap<Product, Quantity>();
		flowers.put(product, Quantity.of(3));
		AccountancyEntryWrapper entry = mock(AccountancyEntryWrapper.class);
		when(entry.getCategory()).thenReturn("Veranstaltung Verkauf");
		when(entry.getDeliveryDate()).thenReturn(LocalDate.from(LocalDateTime.now().plusDays(1))); // Future date
		when(entry.getFlowers()).thenReturn(flowers);
		when(entry.getValue()).thenReturn(Money.of(-50, "EUR"));
		//when(cart.getPrice()).thenReturn(Money.of(-50, "EUR"));

		// Call the method under test
		AccountancyEntry result = cashRegisterService.add(entry);

		// Assertions
		assertNull(result, "Expected null return value for negative cart price");
		verify(salesService, never()).buyProductsFromBasket(any(), any(), any());
	}

	@Test
	void testFindDeletedProductsByMonth() {
		// Mocking deleted products with different dates and properties
		DeletedProduct product1 = new DeletedProduct("ProductA", Money.of(10, "EUR"), 5, Money.of(50, "EUR"), LocalDate.of(2025, 1, 5));
		DeletedProduct product2 = new DeletedProduct("ProductB", Money.of(20, "EUR"), 3, Money.of(60, "EUR"), LocalDate.of(2025, 1, 10));
		DeletedProduct product3 = new DeletedProduct("ProductC", Money.of(15, "EUR"), 2, Money.of(30, "EUR"), LocalDate.of(2025, 2, 1));
		DeletedProduct product4 = new DeletedProduct("ProductA", Money.of(10, "EUR"), 1, Money.of(10, "EUR"), LocalDate.of(2025, 1, 20));

		// Mocking ProductService to return these products
		when(productService.getDeletedProducts()).thenReturn(List.of(product1, product2, product3, product4));

		// Call the method under test
		List<DeletedProduct> result = cashRegisterService.findDeletedProductsByMonth(LocalDate.of(2025, 1, 1));

		// Assertions
		assertNotNull(result, "Expected a non-null result");
		assertEquals(2, result.size(), "Expected 2 distinct products grouped by name for January 2025");

		DeletedProduct groupedProductA = result.stream().filter(p -> p.getName().equals("ProductA")).findFirst().orElse(null);
		assertNotNull(groupedProductA, "Expected grouped product A");
		assertEquals(6, groupedProductA.getQuantityDeleted(), "Expected quantity for ProductA to be summed");
		assertEquals(Money.of(60, "EUR"), groupedProductA.getTotalLoss(), "Expected total loss for ProductA to be correct");

		DeletedProduct groupedProductB = result.stream().filter(p -> p.getName().equals("ProductB")).findFirst().orElse(null);
		assertNotNull(groupedProductB, "Expected grouped product B");
		assertEquals(3, groupedProductB.getQuantityDeleted(), "Expected quantity for ProductB to match the input");
		assertEquals(Money.of(60, "EUR"), groupedProductB.getTotalLoss(), "Expected total loss for ProductB to be correct");

		// Verify interactions with ProductService
		verify(productService, times(1)).getDeletedProducts();
	}

	@Test
	void testNormalizeDeletedProducts() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		// Mocking a list of deleted products
		DeletedProduct product1 = new DeletedProduct("ProductA", Money.of(10, "EUR"), 5, Money.of(50, "EUR"), LocalDate.of(2025, 1, 5));
		DeletedProduct product2 = new DeletedProduct("ProductA", Money.of(10, "EUR"), 3, Money.of(30, "EUR"), LocalDate.of(2025, 1, 10));
		DeletedProduct product3 = new DeletedProduct("ProductB", Money.of(20, "EUR"), 2, Money.of(40, "EUR"), LocalDate.of(2025, 1, 15));

		// Creating input list
		List<DeletedProduct> input = List.of(product1, product2, product3);

		// Call the private method via reflection
		Method normalizeMethod = CashRegisterService.class.getDeclaredMethod("normalizeDeletedProducts", List.class);
		normalizeMethod.setAccessible(true);

		@SuppressWarnings("unchecked")
		List<DeletedProduct> result = (List<DeletedProduct>) normalizeMethod.invoke(cashRegisterService, input);

		// Assertions
		assertNotNull(result, "Expected a non-null result");
		assertEquals(2, result.size(), "Expected 2 distinct products grouped by name");

		DeletedProduct groupedProductA = result.stream().filter(p -> p.getName().equals("ProductA")).findFirst().orElse(null);
		assertNotNull(groupedProductA, "Expected grouped product A");
		assertEquals(8, groupedProductA.getQuantityDeleted(), "Expected quantity for ProductA to be summed");
		assertEquals(Money.of(80, "EUR"), groupedProductA.getTotalLoss(), "Expected total loss for ProductA to be correct");

		DeletedProduct groupedProductB = result.stream().filter(p -> p.getName().equals("ProductB")).findFirst().orElse(null);
		assertNotNull(groupedProductB, "Expected grouped product B");
		assertEquals(2, groupedProductB.getQuantityDeleted(), "Expected quantity for ProductB to match the input");
		assertEquals(Money.of(40, "EUR"), groupedProductB.getTotalLoss(), "Expected total loss for ProductB to be correct");
	}

	@Test
	void testFilterByCustomer() {
		// Mock accountancy entries
		AccountancyEntryWrapper entry1 = mock(AccountancyEntryWrapper.class);
		when(entry1.getClientName()).thenReturn("John Doe");
		AccountancyEntryWrapper entry2 = mock(AccountancyEntryWrapper.class);
		when(entry2.getClientName()).thenReturn("Jane Smith");
		AccountancyEntryWrapper entry3 = mock(AccountancyEntryWrapper.class);
		when(entry3.getClientName()).thenReturn("Johnathan Doe");

		Set<AccountancyEntry> mockEntries = new HashSet<>(List.of(entry1, entry2, entry3));
		when(cashRegisterRepository.findFirstByOrderById().get().getAccountancyEntries()).thenReturn(mockEntries);

		// Call the method under test
		List<AccountancyEntryWrapper> result = cashRegisterService.filterByCustomer("John");

		// Assertions
		assertNotNull(result, "Expected a non-null result");
		assertEquals(2, result.size(), "Expected 2 entries containing 'John'");
		assertTrue(result.contains(entry1), "Result should contain entry1");
		assertTrue(result.contains(entry3), "Result should contain entry3");
	}

	@Test
	void testFilterByPrice() {
		// Mock accountancy entries with specific prices
		AccountancyEntryWrapper entry1 = mock(AccountancyEntryWrapper.class);
		when(entry1.getValue()).thenReturn(Money.of(100.00, "EUR"));
		AccountancyEntryWrapper entry2 = mock(AccountancyEntryWrapper.class);
		when(entry2.getValue()).thenReturn(Money.of(50.00, "EUR"));
		AccountancyEntryWrapper entry3 = mock(AccountancyEntryWrapper.class);
		when(entry3.getValue()).thenReturn(Money.of(100.00, "EUR"));

		Set<AccountancyEntry> mockEntries = new HashSet<>(List.of(entry1, entry2, entry3));
		when(cashRegisterRepository.findFirstByOrderById().get().getAccountancyEntries()).thenReturn(mockEntries);

		// Call the method under test with a specific price
		List<AccountancyEntryWrapper> result = cashRegisterService.filterByPrice(100.00);

		// Assertions
		assertNotNull(result, "Expected a non-null result");
		assertEquals(2, result.size(), "Expected 2 entries with the price 100.00 EUR");
		assertTrue(result.contains(entry1), "Result should contain entry1");
		assertTrue(result.contains(entry3), "Result should contain entry3");
	}

	@Test
	void testGetEntry() {
		// Mock accountancy entries
		AccountancyEntryWrapper entry1 = mock(AccountancyEntryWrapper.class);
		when(entry1.getId()).thenReturn(AccountancyEntry.AccountancyEntryIdentifier.of("order1"));
		AccountancyEntryWrapper entry2 = mock(AccountancyEntryWrapper.class);
		when(entry2.getId()).thenReturn(AccountancyEntry.AccountancyEntryIdentifier.of("order2"));

		List<AccountancyEntryWrapper> filteredAndCutOrdersList = List.of(entry1, entry2);

		// Call the method under test with a specific orderId
		AccountancyEntryWrapper result = cashRegisterService.getEntry("order1", filteredAndCutOrdersList);

		// Assertions
		assertNotNull(result, "Expected a non-null result");
		assertEquals(entry1, result, "Expected the result to match entry1");

		// Test with an invalid orderId
		AccountancyEntryWrapper invalidResult = cashRegisterService.getEntry("order3", filteredAndCutOrdersList);
		assertNull(invalidResult, "Expected null for a non-existent orderId");
	}


}

