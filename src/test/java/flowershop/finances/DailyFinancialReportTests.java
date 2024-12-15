package flowershop.finances;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;
import org.vandeseer.easytable.structure.Row;

import javax.money.MonetaryAmount;
import javax.money.Monetary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyFinancialReportTests {

	private CashRegisterService cashRegisterService;
	private MonetaryAmount balanceEndOfDay;
	private Interval dayInterval;
	private LocalDateTime firstTransactionTime;

	private DailyFinancialReport report;

	@BeforeEach
	void setUp() {
		cashRegisterService = mock(CashRegisterService.class);
		balanceEndOfDay = Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(500).create(); // Final balance
		dayInterval = Interval.from(LocalDateTime.of(2024, 6, 1, 0, 0))
			.to(LocalDateTime.of(2024, 6, 1, 23, 59));
		firstTransactionTime = LocalDateTime.of(2024, 1, 1, 0, 0);

		// this prevents NullPointerException:
		when(cashRegisterService.getRevenue(any())).thenReturn(Monetary.getDefaultAmountFactory()
			.setCurrency("EUR").setNumber(300).create());
		when(cashRegisterService.getExpences(any())).thenReturn(Monetary.getDefaultAmountFactory()
			.setCurrency("EUR").setNumber(100).create());
	}


	@Test
	void constructor_ShouldInitializeCorrectly() {
		// Arrange
		AccountancyEntryWrapper entry1 = mock(AccountancyEntryWrapper.class);
		AccountancyEntryWrapper entry2 = mock(AccountancyEntryWrapper.class);

		when(entry1.getTimestamp()).thenReturn(LocalDateTime.of(2024, 6, 1, 10, 0));
		when(entry2.getTimestamp()).thenReturn(LocalDateTime.of(2024, 6, 1, 15, 0));

		Streamable<AccountancyEntry> accountEntries = Streamable.of(entry1, entry2);

		when(cashRegisterService.find(dayInterval)).thenReturn(accountEntries);
		when(cashRegisterService.getRevenue(accountEntries)).thenReturn(balanceEndOfDay);
		when(cashRegisterService.getExpences(accountEntries)).thenReturn(Monetary.getDefaultAmountFactory()
			.setCurrency("EUR").setNumber(200).create());

		// Act
		report = new DailyFinancialReport(dayInterval, balanceEndOfDay, cashRegisterService, firstTransactionTime);

		// Assert
		assertNotNull(report.getOrders());
		assertEquals(2, report.getOrders().toList().size());
	}

	@Test
	void isBeforeBeginning_ShouldReturnTrueForInvalidInterval() {
		when(cashRegisterService.find(dayInterval)).thenReturn(Streamable.empty());

		report = new DailyFinancialReport(dayInterval, balanceEndOfDay, cashRegisterService, firstTransactionTime);

		assertTrue(report.isBeforeBeginning());
	}

	@Test
	void isBeforeBeginning_ShouldReturnFalseForValidInterval() {
		// Arrange
		AccountancyEntry entry = mock(AccountancyEntryWrapper.class);
		when(((AccountancyEntryWrapper) entry).getTimestamp()).thenReturn(LocalDateTime.now());
		Streamable<AccountancyEntry> entries = Streamable.of(entry);

		when(cashRegisterService.find(dayInterval)).thenReturn(entries);

		report = new DailyFinancialReport(dayInterval, balanceEndOfDay, cashRegisterService, firstTransactionTime);

		// Act & Assert
		assertFalse(report.isBeforeBeginning());
	}

	@Test
	void intervalToString_ShouldFormatCorrectly() {
		// Arrange
		when(cashRegisterService.find(dayInterval)).thenReturn(Streamable.empty());
		report = new DailyFinancialReport(dayInterval, balanceEndOfDay, cashRegisterService, firstTransactionTime);

		String formattedInterval = report.intervalToString();

		assertEquals("1.06.2024", formattedInterval);
	}

	@Test
	void getNeededRows_ShouldContainCorrectRows() {
		// Arrange
		AccountancyEntryWrapper entry = mock(AccountancyEntryWrapper.class);
		when(entry.getTimestamp()).thenReturn(LocalDateTime.of(2024, 6, 1, 10, 0));
		when(entry.getCategory()).thenReturn("Sale");
		when(entry.getItems()).thenReturn(Map.of("Flower", Quantity.of(5)));
		when(entry.getValue()).thenReturn(balanceEndOfDay);

		Streamable<AccountancyEntry> entries = Streamable.of(entry);
		when(cashRegisterService.find(dayInterval)).thenReturn(entries);

		report = new DailyFinancialReport(dayInterval, balanceEndOfDay, cashRegisterService, firstTransactionTime);

		// Act
		List<Row> rows = report.getNeededRows(null); // Assuming PDFont is optional

		// Assert
		assertFalse(rows.isEmpty());
		assertTrue(rows.size() > 5); // Rows for date, header, transactions, etc.
	}
}
