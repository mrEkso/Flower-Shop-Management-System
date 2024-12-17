package flowershop.finances;

import flowershop.clock.ClockService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;

import javax.money.MonetaryAmount;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MonthlyFinancialReportTests {

	private CashRegisterService cashRegisterService;
	private MonetaryAmount balanceEndOfMonth;
	private LocalDateTime firstTransaction;
	private Interval monthInterval;
	private ClockService clockService;

	@BeforeEach
	void setUp() {
		// Mock dependencies
		cashRegisterService = mock(CashRegisterService.class);
		clockService = mock(ClockService.class);

		// Prepare test data
		balanceEndOfMonth = Money.of(1000, "EUR");
		firstTransaction = LocalDateTime.of(2024, 1, 1, 0, 0);
		monthInterval = Interval.from(LocalDateTime.of(2024, 6, 1, 0, 0))
			.to(LocalDateTime.of(2024, 6, 30, 23, 59));
	}

//	@Test
//	void constructor_ShouldInitializeCorrectly() {
//		// Arrange
//		Interval day1 = Interval.from(LocalDateTime.of(2024, 6, 30, 0, 0))
//			.to(LocalDateTime.of(2024, 6, 30, 23, 59));
//		Interval day2 = Interval.from(LocalDateTime.of(2024, 6, 29, 0, 0))
//			.to(LocalDateTime.of(2024, 6, 29, 23, 59));
//
//		Map<Interval, Streamable<AccountancyEntry>> days = new HashMap<>();
//		days.put(day1, Streamable.empty());
//		days.put(day2, Streamable.empty());
//
//
//		Map<Interval, Streamable<AccountancyEntry>> daysOfMonth = new HashMap<>();
//		daysOfMonth.put(day1, Streamable.empty()); // Use Streamable.empty() instead of null
//		daysOfMonth.put(day2, Streamable.empty()); // Use Streamable.empty() instead of null
//
//
//		when(cashRegisterService.find(monthInterval, Duration.ofDays(1))).thenReturn(days);
//
//		// Act
//		MonthlyFinancialReport report = new MonthlyFinancialReport(monthInterval, balanceEndOfMonth, cashRegisterService, firstTransaction);
//
//		// Assert
//		assertEquals(2, report.getDailyFinancialReports().size(), "Daily reports should match the days of the month.");
//		assertNotNull(report.getIncome(), "Income should not be null.");
//		assertNotNull(report.getExpenditure(), "Expenditure should not be null.");
//	}
//

	@Test
	void constructor_WithNoDays_ShouldHandleGracefully() {
		// Arrange empty days
		when(cashRegisterService.find(monthInterval, Duration.ofDays(1))).thenReturn(Collections.emptyMap());

		// Act
		MonthlyFinancialReport report = new MonthlyFinancialReport(monthInterval, balanceEndOfMonth, cashRegisterService, firstTransaction, clockService);

		// Assert
		assertEquals(0, report.getDailyFinancialReports().size(), "There should be no daily reports.");
		assertEquals(Money.of(0, "EUR"), report.getIncome(), "Income should be zero.");
		assertEquals(Money.of(0, "EUR"), report.getExpenditure(), "Expenditure should be zero.");
	}

	@Test
	void isBeforeBeginning_ShouldReturnCorrectValue() {
		// Mock daily reports
		DailyFinancialReport dailyReport1 = mock(DailyFinancialReport.class);
		DailyFinancialReport dailyReport2 = mock(DailyFinancialReport.class);

		when(dailyReport1.isBeforeBeginning()).thenReturn(true);
		when(dailyReport2.isBeforeBeginning()).thenReturn(false);

		when(clockService.now()).thenReturn(LocalDateTime.now());
		MonthlyFinancialReport report = spy(new MonthlyFinancialReport(monthInterval, balanceEndOfMonth, cashRegisterService, firstTransaction, clockService));
		report.getDailyFinancialReports().addAll(List.of(dailyReport1, dailyReport2));

		assertFalse(report.isBeforeBeginning(), "Report should return false if any daily report is not before the beginning.");
	}

	@Test
	void isBeforeBeginning_ShouldReturnTrueWhenAllDaysAreBeforeBeginning() {
		// Mock daily reports
		DailyFinancialReport dailyReport1 = mock(DailyFinancialReport.class);
		DailyFinancialReport dailyReport2 = mock(DailyFinancialReport.class);

		when(dailyReport1.isBeforeBeginning()).thenReturn(true);
		when(dailyReport2.isBeforeBeginning()).thenReturn(true);

		MonthlyFinancialReport report = spy(new MonthlyFinancialReport(monthInterval, balanceEndOfMonth, cashRegisterService, firstTransaction, clockService));
		report.getDailyFinancialReports().addAll(List.of(dailyReport1, dailyReport2));

		assertTrue(report.isBeforeBeginning(), "Report should return true if all daily reports are before the beginning.");
	}
}
