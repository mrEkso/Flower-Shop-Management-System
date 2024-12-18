package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.clock.PendingOrder;
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

import static flowershop.finances.Category.Einkauf;

@Service
@Primary
public class CashRegisterService implements Accountancy {

	private final OrderManagement<AbstractOrder> orderManagement;

	private final CashRegisterRepository cashRegisterRepository;
	private final ClockService clockService;


	@Autowired
	public CashRegisterService(OrderManagement<AbstractOrder> orderManagement,
							   CashRegisterRepository cashRegisterRepository, ClockService clockService) {
		this.orderManagement = orderManagement;
		this.cashRegisterRepository = cashRegisterRepository;
		Streamable<AbstractOrder> previousOrders = Optional.ofNullable(orderManagement.findBy(OrderStatus.PAID))
			.orElse(Streamable.empty());
		for (Order order : previousOrders) {
			AccountancyEntry convertedOrder = new AccountancyEntryWrapper((AbstractOrder) order, clockService.now());
			this.add(convertedOrder);
		}
		this.clockService = clockService;
	}


	public CashRegister getCashRegisterById(Long id) {
		return cashRegisterRepository.findById(id)
			.orElseThrow(() -> new IllegalStateException("Product not found with id " + id));
	}

	public MonetaryAmount getBalance() {
		return this.getCashRegister().getBalance();
	}

	/**
	 * Is used to add an AccountancyEntry instance to the register
	 * @param entry
	 * @return entry, if everything went well. Otherwise - null
	 * @param <T> type of the entry (T extends AccountancyEntry)
	 */
	@Override
	public <T extends AccountancyEntry> T add(T entry) {
		if (entry == null || entry instanceof OrderPaymentEntry) { //because salespoint is also doing it apparently
			return null;
		}

		CashRegister cashRegister = getCashRegister();
		Set<AccountancyEntry> existing = cashRegister.getAccountancyEntries();
		existing.add(entry);
		cashRegister.setAccountancyEntries(existing);
		cashRegister.setBalance((Money) entry.getValue().add(cashRegister.getBalance()));
		if(((AccountancyEntryWrapper)entry).getCategory().equals("Einkauf"))
		{
			Set<PendingOrder> pendingOrders = cashRegister.getPendingOrders();
			PendingOrder newOrder = new PendingOrder(((AccountancyEntryWrapper) entry).getItems(), clockService.nextWorkingDay());
			pendingOrders.add(newOrder);
			cashRegister.setPendingOrders(pendingOrders);
		}
		cashRegisterRepository.save(cashRegister);
		return entry;
	}

	/**
	 * Will wrap the order into AccountancyEntryWrapper and add it to the register
	 * @param event of type OrderPaid that carries an order
	 */
	@EventListener
	public void onOrderPaid(OrderEvents.OrderPaid event) {
		AbstractOrder order = (AbstractOrder) event.getOrder();
		//convert order to AccountancyEntry
		AccountancyEntryWrapper convertedOrder = new AccountancyEntryWrapper(order,clockService.now());
		this.add(convertedOrder);
	}

	/**
	 *
	 * @return all registered AccountancyEntries
	 */
	@Override
	public Streamable<AccountancyEntry> findAll() {
		return Streamable.of(this.getCashRegister().getAccountancyEntries());
	}

	@Override
	public <T extends AccountancyEntry> Streamable<T> findAll(Class<T> type) {
		return Streamable.of();
	}

	/**
	 *
	 * @param category
	 * @return all registered AccountancyEntries of the given Category
	 */
	public Streamable<AccountancyEntry> filterEntries(Category category) {
		Streamable<AccountancyEntry> filteredEntries = Streamable.empty();
		for (AccountancyEntry entry : this.getCashRegister().getAccountancyEntries()) {
			if (((AccountancyEntryWrapper) entry).getCategory().equals(AccountancyEntryWrapper.categoryToString(category))) {
				filteredEntries = filteredEntries.and(entry);
			}
		}
		return filteredEntries;
	}

	/**
	 *
	 * @param isIncome
	 * @return a list of all AccountancyEntries that are either incomes or spendings
	 */
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

	/**
	 *
	 * @param interval
	 * @return all AccountancyEntries form this interval of time
	 */
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

	/**
	 *
	 * @param interval overall period
	 * @param duration periodity of how to split the data
	 * @return the map of smaller periods of duration to AccountancyEntries from that interval
	 */
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

	/**
	 *
	 * @param interval overall period
	 * @param duration periodity of how to split the data
	 * @return a map of smaller periods of duration to the overall profit from that interval
	 */
	@Override
	public Map<Interval, MonetaryAmount> salesVolume(Interval interval, TemporalAmount duration) {
		Map<Interval, Streamable<AccountancyEntry>> splits = find(interval, duration);
		Map<Interval, MonetaryAmount> output = new HashMap<>();
		for (Map.Entry<Interval, Streamable<AccountancyEntry>> entry : splits.entrySet()) {
			output.put(entry.getKey(), getProfit(entry.getValue()));
		}
		return output;
	}

	/**
	 * Use this method instead of the DailyFinancialReport constructor
	 * @param day any timestamp during the needed day
	 * @return an instance of DailyFinancialReport
	 */
	public DailyFinancialReport createFinancialReportDay(LocalDateTime day) {
		LocalDateTime start = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), 0, 0);
		LocalDateTime end = start.plusDays(1);
		Interval interval = Interval.from(start).to(end);
		Interval endToNow = Interval.from(end).to(clockService.now().plusDays(1));
		TemporalAmount endToNowDuration = endToNow.toDuration();
		MonetaryAmount moneyDifference = salesVolume(endToNow, endToNowDuration).get(endToNow);
		CashRegister cashRegister = getCashRegister();
		MonetaryAmount moneyThen = cashRegister.getBalance().subtract(moneyDifference);
		//List<AccountancyEntry> allEntries = getCashRegister().getAccountancyEntries().stream().toList();
		List<AccountancyEntryWrapper> allEntries = getCashRegister().getAccountancyEntries().stream()
			.map(entry -> (AccountancyEntryWrapper) entry)
			.sorted(Comparator.comparing(AccountancyEntryWrapper::getTimestamp))
			.toList();

		if (allEntries.isEmpty()) {
			return null;
		}
		return new DailyFinancialReport(
			interval,
			moneyThen,
			this,
			allEntries.getFirst().getTimestamp(),
			clockService);
	}

	/**
	 * Use this method instead of the MonthlyFinancialReport constructor
	 * @param day any timestamp during the needed month
	 * @return an instance of MonthlyFinancialReport
	 */
	public MonthlyFinancialReport createFinancialReportMonth(LocalDateTime day) {
		LocalDateTime start = LocalDateTime.of(day.getYear(), day.getMonth(), 1, 0, 0);
		LocalDateTime end = start.plusMonths(1);
		if (end.isAfter(clockService.now())) {
			end = clockService.now();
		}
		Interval interval = Interval.from(start).to(end);

		Interval endToNow = Interval.from(end).to(clockService.now().plusDays(1));
		TemporalAmount endToNowDuration = endToNow.toDuration();
		MonetaryAmount moneyDifference = salesVolume(endToNow, endToNowDuration).get(endToNow);
		CashRegister cashRegister = getCashRegister();
		MonetaryAmount moneyThen = cashRegister.getBalance().subtract(moneyDifference);
		List<AccountancyEntryWrapper> allEntries = getCashRegister().getAccountancyEntries().stream()
			.map(entry -> (AccountancyEntryWrapper) entry)
			.sorted(Comparator.comparing(AccountancyEntryWrapper::getTimestamp))
			.toList();
		if (allEntries.isEmpty()) {
			return null;
		}
		return new MonthlyFinancialReport(
			interval,
			moneyThen,
			this,
			allEntries.getFirst().getTimestamp(),
			clockService);
	}

	/**
	 *
	 * @param set AccountancyEntries, for which profit has to be calculated
	 * @return profit
	 */
	public MonetaryAmount getProfit(Streamable<AccountancyEntry> set) {
		Money output = Money.of(0, getCashRegister().getBalance().getCurrency());
		for (AccountancyEntry entry : set) {
			output = output.add(entry.getValue());
		}
		return output;
	}

	/**
	 *
	 * @param set AccountancyEntries, for which income has to be calculated
	 * @return income (all positive values added up and negative - ignored)
	 */
	public MonetaryAmount getRevenue(Streamable<AccountancyEntry> set) {
		Money output = Money.of(0, getCashRegister().getBalance().getCurrency());
		for (AccountancyEntry entry : set) {
			if (entry.isRevenue()) {
				output = output.add(entry.getValue());
			}
		}
		return output;
	}

	/**
	 *
	 * @param set AccountancyEntries, for which spendings have to be calculated
	 * @return expences (all negative values added up and positive - ignored)
	 */
	public MonetaryAmount getExpences(Streamable<AccountancyEntry> set) {
		Money output = Money.of(0, getCashRegister().getBalance().getCurrency());
		for (AccountancyEntry entry : set) {
			if (entry.isExpense()) {
				output = output.add(entry.getValue());
			}
		}
		return output;
	}

	/**
	 *
	 * @return the instance of CashRegister, stored in the repository
	 */
	public CashRegister getCashRegister() {
		Optional<CashRegister> cashRegister = cashRegisterRepository.findFirstByOrderById();
		return cashRegisterRepository.findFirstByOrderById()
			.orElseThrow(() -> new IllegalStateException("CashRegister instance not found"));
	}


}
