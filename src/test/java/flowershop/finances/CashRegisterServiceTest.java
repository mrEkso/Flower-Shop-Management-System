package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.product.ProductService;
import flowershop.sales.InsufficientFundsException;
import flowershop.sales.SimpleOrder;
import flowershop.services.AbstractOrder;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.order.*;
import org.salespointframework.payment.PaymentMethod;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Streamable;

import javax.money.MonetaryAmount;
import java.time.Duration;
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
		CashRegister mockCashRegister = mock(CashRegister.class);
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

		when(mockCashRegister.getAccountancyEntries()).thenReturn(new HashSet<>(entries));

		when(mockCashRegister.getBalance()).thenReturn(mockBalance);
		when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.of(mockCashRegister));
	//when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.empty());
		when(orderManagement.findBy(OrderStatus.PAID)).thenReturn(mockPreviousOrders);
		when(clockService.now()).thenReturn(LocalDateTime.now());

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
		Category category = mock(Category.class);
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
}

