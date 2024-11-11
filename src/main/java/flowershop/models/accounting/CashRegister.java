package flowershop.models.accounting;

import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.Optional;

public class CashRegister implements Accountancy {
	public CashRegister() {

	}
	@Override
	public <T extends AccountancyEntry> T add(T entry){
		return null;
	}

	@Override
	public Streamable<AccountancyEntry> findAll() {
		return null;
	}

	@Override
	public <T extends AccountancyEntry> Streamable<T> findAll(Class<T> type) {
		return null;
	}

	@Override
	public Optional<AccountancyEntry> get(AccountancyEntry.AccountancyEntryIdentifier identifier) {
		return Optional.empty();
	}

	@Override
	public <T extends AccountancyEntry> Optional<T> get(AccountancyEntry.AccountancyEntryIdentifier identifier, Class<T> type) {
		return Optional.empty();
	}

	@Override
	public Streamable<AccountancyEntry> find(Interval interval) {
		return null;
	}

	@Override
	public <T extends AccountancyEntry> Streamable<T> find(Interval interval, Class<T> type) {
		return null;
	}

	@Override
	public Map<Interval, Streamable<AccountancyEntry>> find(Interval interval, TemporalAmount duration) {
		return Map.of();
	}

	@Override
	public <T extends AccountancyEntry> Map<Interval, Streamable<T>> find(Interval interval, TemporalAmount duration, Class<T> type) {
		return Map.of();
	}

	@Override
	public Map<Interval, MonetaryAmount> salesVolume(Interval interval, TemporalAmount duration) {
		return Map.of();
	}

	public DailyFinancialReport createFinancialReportDay(LocalDateTime day){
		return null;
	}

	public MonthlyFinancialReport createFinancialReportMonth(LocalDateTime day){
		return null;
	}
}
