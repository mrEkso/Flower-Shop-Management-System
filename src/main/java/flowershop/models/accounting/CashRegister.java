package flowershop.models.accounting;

import flowershop.models.order.*;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderEvents;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.time.Interval;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Streamable;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class CashRegister implements Accountancy {
	private OrderManagement<Order> orderManagement;
	private Streamable<AccountancyEntry> accountancyEntries = Streamable.empty();
	private Money balance; //has to be stored somehow always
	
	public CashRegister(OrderManagement<Order> orderManagement) {
		this.orderManagement = orderManagement;
		Streamable<Order> previousOrders = orderManagement.findBy(OrderStatus.PAID);
		for (Order order : previousOrders) {
			AccountancyEntry convertedOrder = new AccountancyEntryWrapper((AbstractOrder) order);
			this.add(convertedOrder);
		}
	}
	@Override
	public <T extends AccountancyEntry> T add(T entry){
		if(entry == null){
			return null;
		}
		this.accountancyEntries.and((AccountancyEntryWrapper)entry);
		balance = (Money) entry.getValue().add(balance);
		return entry;
	}
	@EventListener
	public void onOrderPaid(OrderEvents.OrderPaid event){
		AbstractOrder order = (AbstractOrder) event.getOrder();
		//convert order to AccountancyEntry
		AccountancyEntry convertedOrder = new AccountancyEntryWrapper(order);
		this.add(convertedOrder);
	}

	@Override
	public Streamable<AccountancyEntry> findAll() {
		return accountancyEntries;
	}

	@Override
	public <T extends AccountancyEntry> Streamable<T> findAll(Class<T> type) {
		return Streamable.of();
	}

	public Streamable<AccountancyEntry> filterEntries(AccountancyEntryWrapper.Category category) {
		Streamable<AccountancyEntry> filteredEntries = Streamable.empty();
		for (AccountancyEntry entry : accountancyEntries) {
			if (((AccountancyEntryWrapper)entry).getCategory() == category){
				filteredEntries.and(entry);
			}
		}
		return filteredEntries;
	}

	public LinkedList<AccountancyEntry> filterIncomeOrSpending(boolean isIncome){
		LinkedList<AccountancyEntry> filteredEntries = new LinkedList<>();
		for (AccountancyEntry entry : accountancyEntries){
			if (entry.isRevenue() == isIncome){
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
		for (AccountancyEntry entry : accountancyEntries) {
			if(entry.getDate().isEmpty()){
				continue;
			}
			if(interval.contains(entry.getDate().get())){
				output.and(entry);
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
		HashMap<Interval,Streamable<AccountancyEntry>> output = new HashMap<>();
		LocalDateTime start = interval.getStart();
		LocalDateTime end = start.plus(duration);
		do{
			Interval subinterval = Interval.from(start).to(end);
			Streamable<AccountancyEntry> subset = Streamable.empty();
			output.put(subinterval, subset.and(find(subinterval)));
		}while(interval.contains(end));
		return output;
	}

	@Override
	public <T extends AccountancyEntry> Map<Interval, Streamable<T>> find(Interval interval, TemporalAmount duration, Class<T> type) {
		return Map.of();
	}

	@Override
	public Map<Interval, MonetaryAmount> salesVolume(Interval interval, TemporalAmount duration) {
		Map<Interval,Streamable<AccountancyEntry>> splits = find(interval, duration);
		Map<Interval,MonetaryAmount> output = new HashMap<>();
		for(Map.Entry<Interval,Streamable<AccountancyEntry>> entry : splits.entrySet()){
			output.put(entry.getKey(),getProfit(entry.getValue()));
		}
		return output;
	}

	public DailyFinancialReport createFinancialReportDay(LocalDateTime day){
		LocalDateTime start = LocalDateTime.of(day.getYear(), day.getMonth(), day.getDayOfMonth(), 0, 0);
		LocalDateTime end = start.plusDays(1);
		Interval interval = Interval.from(start).to(end);
		Interval endToNow = Interval.from(end).to(LocalDateTime.now().plusDays(1));
		TemporalAmount endToNowDuration = endToNow.toDuration();
		MonetaryAmount moneyDifference = salesVolume(endToNow,endToNowDuration).get(endToNow);
		MonetaryAmount moneyThen = this.balance.subtract(moneyDifference);
		return new DailyFinancialReport(interval,moneyThen,this);
	}

	public MonthlyFinancialReport createFinancialReportMonth(LocalDateTime day){
		LocalDateTime start = LocalDateTime.of(day.getYear(), day.getMonth(), 1, 0, 0);
		LocalDateTime end = start.plusMonths(1);
		Interval interval = Interval.from(start).to(end);
		Interval endToNow = Interval.from(end).to(LocalDateTime.now().plusDays(1));
		TemporalAmount endToNowDuration = endToNow.toDuration();
		MonetaryAmount moneyDifference = salesVolume(endToNow,endToNowDuration).get(endToNow);
		MonetaryAmount moneyThen = this.balance.subtract(moneyDifference);
		return new MonthlyFinancialReport(interval,moneyThen,this);
	}
	public MonetaryAmount getProfit(Streamable<AccountancyEntry> set){
		Money output = Money.of(0,this.balance.getCurrency());
		for(AccountancyEntry entry : set){
			output.add(entry.getValue());
		}
		return output;
	}
	public MonetaryAmount getRevenue(Streamable<AccountancyEntry> set){
		Money output = Money.of(0,this.balance.getCurrency());
		for(AccountancyEntry entry : set){
			if(entry.isRevenue()) {
				output.add(entry.getValue());
			}
		}
		return output;
	}
	public MonetaryAmount getExpences(Streamable<AccountancyEntry> set){
		Money output = Money.of(0,this.balance.getCurrency());
		for(AccountancyEntry entry : set){
			if(entry.isExpense()) {
				output.add(entry.getValue());
			}
		}
		return output;
	}
}
