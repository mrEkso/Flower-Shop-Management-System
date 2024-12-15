package flowershop.finances;

import flowershop.services.AbstractOrder;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.accountancy.OrderPaymentEntry;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderEvents;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;

@Service
@Primary
public class CashRegisterService implements Accountancy {
/*
Class with simply getters and setters, completely based on CashRegistered (all fields are same)
 */

	private final OrderManagement<AbstractOrder> orderManagement;
	//private CashRegister cashRegister = new CashRegister();
	private final CashRegisterRepository cashRegisterRepository;
	//private long cashRegisterId;


	@Autowired
	public CashRegisterService(OrderManagement<AbstractOrder> orderManagement,
							   CashRegisterRepository cashRegisterRepository) {
		this.orderManagement = orderManagement;
		this.cashRegisterRepository = cashRegisterRepository;
		Streamable<AbstractOrder> previousOrders = orderManagement.findBy(OrderStatus.PAID);
		for (Order order : previousOrders) {
			AccountancyEntry convertedOrder = new AccountancyEntryWrapper((AbstractOrder) order);
			this.add(convertedOrder);
		}
		//this.cashRegister=getCashRegister();
	}


	public CashRegister getCashRegisterById(Long id) {
		return cashRegisterRepository.findById(id)
			.orElseThrow(() -> new IllegalStateException("Product not found with id " + id));
	}

	public MonetaryAmount getBalance() {
		return this.getCashRegister().getBalance();
	}

	@Override
	//@Transactional
	public <T extends AccountancyEntry> T add(T entry) {
		if (entry == null || entry instanceof OrderPaymentEntry) { //because salespoint is also doing it apparently
			return null;
		}

		CashRegister cashRegister = getCashRegister();
		Set<AccountancyEntry> existing = cashRegister.getAccountancyEntries();
		existing.add(entry);
		cashRegister.setAccountancyEntries(existing); // removed the cast to Acc..Wrapper
		cashRegister.setBalance((Money) entry.getValue().add(cashRegister.getBalance()));
		cashRegisterRepository.save(cashRegister);
		return entry;
	}

	@EventListener
	public void onOrderPaid(OrderEvents.OrderPaid event) {
		AbstractOrder order = (AbstractOrder) event.getOrder();
		//convert order to AccountancyEntry
		AccountancyEntryWrapper convertedOrder = new AccountancyEntryWrapper(order);
		this.add(convertedOrder);
	}

	@Override
	public Streamable<AccountancyEntry> findAll() {
		return Streamable.of(this.getCashRegister().getAccountancyEntries());
	}

	@Override
	public <T extends AccountancyEntry> Streamable<T> findAll(Class<T> type) {
		return Streamable.of();
	}

	public Streamable<AccountancyEntry> filterEntries(Category category) {
		Streamable<AccountancyEntry> filteredEntries = Streamable.empty();
		for (AccountancyEntry entry : this.getCashRegister().getAccountancyEntries()) {
			if (((AccountancyEntryWrapper) entry).getCategory().equals(AccountancyEntryWrapper.categoryToString(category))) {
				filteredEntries = filteredEntries.and(entry);
			}
		}
		return filteredEntries;
	}

	public LinkedList<AccountancyEntry> filterIncomeOrSpending(boolean isIncome) {
		LinkedList<AccountancyEntry> filteredEntries = new LinkedList<>();
		for (AccountancyEntry entry : this.getCashRegister().getAccountancyEntries()) {
			if (entry.isRevenue() == isIncome) {
				filteredEntries.add(entry);
			}
		}
		return filteredEntries;
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
		Streamable<AccountancyEntry> output = Streamable.of();
		for (AccountancyEntry entry : this.getCashRegister().getAccountancyEntries()) {
			if (((AccountancyEntryWrapper) entry).getTimestamp() == null) {
				continue;
			}
			if (interval.contains(((AccountancyEntryWrapper) entry).getTimestamp())) {
				output = output.and(entry);
			}
		}
		return output;
	}

	@Override
	public <T extends AccountancyEntry> Streamable<T> find(Interval interval, Class<T> type) {
		return null;
	}

	@Override
	public Map<Interval, Streamable<AccountancyEntry>> find(Interval interval, TemporalAmount duration) {
		HashMap<Interval, Streamable<AccountancyEntry>> output = new HashMap<>();
		LocalDateTime start = interval.getStart();
		LocalDateTime end = start.plus(duration);
		do {
			Interval subinterval = Interval.from(start).to(end);
			Streamable<AccountancyEntry> subset = Streamable.empty();
			output.put(subinterval, subset.and(find(subinterval)));
			end = end.plus(duration);
			start = start.plus(duration);
		} while (interval.contains(start));
		return output;
	}

	@Override
	public <T extends AccountancyEntry> Map<Interval, Streamable<T>> find(Interval interval, TemporalAmount duration, Class<T> type) {
		return Map.of();
	}

	@Override
	public Map<Interval, MonetaryAmount> salesVolume(Interval interval, TemporalAmount duration) {
		Map<Interval, Streamable<AccountancyEntry>> splits = find(interval, duration);
		Map<Interval, MonetaryAmount> output = new HashMap<>();
		for (Map.Entry<Interval, Streamable<AccountancyEntry>> entry : splits.entrySet()) {
			output.put(entry.getKey(), getProfit(entry.getValue()));
		}
		return output;
	}

	public DailyFinancialReport createFinancialReportDay(LocalDateTime day) {
		LocalDateTime start = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), 0, 0);
		LocalDateTime end = start.plusDays(1);
		Interval interval = Interval.from(start).to(end);
		Interval endToNow = Interval.from(end).to(LocalDateTime.now().plusDays(1));
		TemporalAmount endToNowDuration = endToNow.toDuration();
		MonetaryAmount moneyDifference = salesVolume(endToNow, endToNowDuration).get(endToNow);
		CashRegister cashRegister = getCashRegister();
		MonetaryAmount moneyThen = cashRegister.getBalance().subtract(moneyDifference);
		List<AccountancyEntry> allEntries = getCashRegister().getAccountancyEntries().stream().toList();
		if (allEntries.isEmpty()) {
			return null;
		}
		return new DailyFinancialReport(
			interval,
			moneyThen,
			this,
			((AccountancyEntryWrapper) allEntries.get(0)).getTimestamp());
	}

	public MonthlyFinancialReport createFinancialReportMonth(LocalDateTime day) {
		LocalDateTime start = LocalDateTime.of(day.getYear(), day.getMonth(), 1, 0, 0);
		LocalDateTime end = start.plusMonths(1);
		if (end.isAfter(LocalDateTime.now())) {
			end = LocalDateTime.now();
		}
		Interval interval = Interval.from(start).to(end);

		Interval endToNow = Interval.from(end).to(LocalDateTime.now().plusDays(1));
		TemporalAmount endToNowDuration = endToNow.toDuration();
		MonetaryAmount moneyDifference = salesVolume(endToNow, endToNowDuration).get(endToNow);
		CashRegister cashRegister = getCashRegister();
		MonetaryAmount moneyThen = cashRegister.getBalance().subtract(moneyDifference);
		List<AccountancyEntry> allEntries = getCashRegister().getAccountancyEntries().stream().toList();
		if (allEntries.isEmpty()) {
			return null;
		}
		return new MonthlyFinancialReport(
			interval,
			moneyThen,
			this,
			((AccountancyEntryWrapper) allEntries.get(0)).getTimestamp());
	}

	public MonetaryAmount getProfit(Streamable<AccountancyEntry> set) {
		Money output = Money.of(0, getCashRegister().getBalance().getCurrency());
		for (AccountancyEntry entry : set) {
			output.add(entry.getValue());
		}
		return output;
	}

	public MonetaryAmount getRevenue(Streamable<AccountancyEntry> set) {
		Money output = Money.of(0, getCashRegister().getBalance().getCurrency());
		for (AccountancyEntry entry : set) {
			if (entry.isRevenue()) {
				output = output.add(entry.getValue());
			}
		}
		return output;
	}

	public MonetaryAmount getExpences(Streamable<AccountancyEntry> set) {
		Money output = Money.of(0, getCashRegister().getBalance().getCurrency());
		for (AccountancyEntry entry : set) {
			if (entry.isExpense()) {
				output = output.add(entry.getValue());
			}
		}
		return output;
	}

	public CashRegister getCashRegister() {
		List<CashRegister> test = cashRegisterRepository.findAll();
		/*
		if(test.isEmpty()){
			throw new IllegalStateException("CashRegister instance not found");
		}

		if(this.cashRegister.getBalance() != null){
			this.cashRegister = cashRegisterRepository.findFirstByOrderById().get();
		}
		 */
		return cashRegisterRepository.findFirstByOrderById()
			.orElseThrow(() -> new IllegalStateException("CashRegister instance not found"));
	}
}
