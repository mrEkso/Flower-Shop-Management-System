package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.clock.PendingOrder;
import flowershop.inventory.DeletedProduct;
import flowershop.product.ProductService;
import flowershop.sales.SalesService;
import flowershop.services.AbstractOrder;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.accountancy.OrderPaymentEntry;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.*;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Primary
public class CashRegisterService implements Accountancy {

	private final OrderManagement<AbstractOrder> orderManagement;

	private final CashRegisterRepository cashRegisterRepository;
	private final ClockService clockService;
	private final ProductService productService;
	private final SalesService salesService;


	@Autowired
	public CashRegisterService(OrderManagement<AbstractOrder> orderManagement,
							   CashRegisterRepository cashRegisterRepository, ClockService clockService, ProductService productService, SalesService salesService) {
		this.orderManagement = orderManagement;
		this.cashRegisterRepository = cashRegisterRepository;
		this.productService = productService;
		Streamable<AbstractOrder> previousOrders = Optional.ofNullable(orderManagement.findBy(OrderStatus.PAID))
			.orElse(Streamable.empty());
		for (Order order : previousOrders) {
			AccountancyEntry convertedOrder = new AccountancyEntryWrapper((AbstractOrder) order, clockService.now(), productService);
			this.add(convertedOrder);
		}
		this.clockService = clockService;
		this.salesService = salesService;
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

		if (((AccountancyEntryWrapper) entry).getCategory().equals("Einkauf")) {
			Set<PendingOrder> pendingOrders = cashRegister.getPendingOrders();
			PendingOrder newOrder = new PendingOrder(
				((AccountancyEntryWrapper) entry).getFlowers(),
				((AccountancyEntryWrapper) entry).getDeliveryDate() == null
					? clockService.nextWorkingDay()
					: ((AccountancyEntryWrapper) entry).getDeliveryDate()
			);
			pendingOrders.add(newOrder);
			cashRegister.setPendingOrders(pendingOrders);

		} else if (
			(((AccountancyEntryWrapper) entry).getCategory().equals("Veranstaltung Verkauf") ||
				((AccountancyEntryWrapper) entry).getCategory().equals("Reservierter Verkauf")) &&
				((AccountancyEntryWrapper) entry).getDeliveryDate().isAfter(clockService.getCurrentDate())
		) {
			Cart cart = new Cart();
			for (Map.Entry<Product, Quantity> i : ((AccountancyEntryWrapper) entry).getFlowers().entrySet()) {
				cart.addOrUpdateItem(i.getKey(), i.getValue());
			}
			if (!cart.isEmpty()) {
				salesService.buyProductsFromBasket(
					cart,
					"Card",
					((AccountancyEntryWrapper) entry).getDeliveryDate().toString()
				);
			}
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
		AccountancyEntryWrapper convertedOrder = new AccountancyEntryWrapper(order, clockService.now(), productService);
		this.add(convertedOrder);
	}

	/**
	 *
	 * @return list of all deleted products ever
	 */
	public List<DeletedProduct> getAllDeletedProducts() {
		return productService.getDeletedProducts();
	}

	public List<DeletedProduct> getAllDeletedProducts(LocalDate date1, LocalDate date2) {
		return productService.getDeletedProducts().stream()
			.filter(product -> (!date1.isAfter(product.getDateWhenDeleted()) && !date2.isBefore(product.getDateWhenDeleted())))
			.toList();
	}

	/**
	 * @param date
	 * @return list of all deleted products on a certain date
	 */
	public List<DeletedProduct> findDeletedProductsByDate(LocalDate date) {
		return normalizeDeletedProducts(productService.getDeletedProducts().stream()
			.filter(product -> date.equals(product.getDateWhenDeleted()))
			.toList());
	}

	/**
	 * @param month any day in a desired month
	 * @return list of all deleted products during a certain month
	 */
	public List<DeletedProduct> findDeletedProductsByMonth(LocalDate month) {
		return normalizeDeletedProducts(productService.getDeletedProducts().stream()
			.filter(product -> month.getMonth().equals(product.getDateWhenDeleted().getMonth())
				&& month.getYear() == product.getDateWhenDeleted().getYear())
			.toList());
	}

	private List<DeletedProduct> normalizeDeletedProducts(List<DeletedProduct> deletedProducts) {
		Map<String, List<DeletedProduct>> grouped =
			deletedProducts
				.stream()
				.collect(Collectors.groupingBy((DeletedProduct::getName)));

		List<DeletedProduct> output = new ArrayList<>();
		for (Map.Entry<String, List<DeletedProduct>> entry : grouped.entrySet()) {
			String name = entry.getKey();
			MonetaryAmount pricePerUnit = entry.getValue().getFirst().getPricePerUnit();
			int quantityDeleted = entry.getValue().stream().mapToInt(DeletedProduct::getQuantityDeleted).sum();
			MonetaryAmount totalLoss = pricePerUnit.multiply(quantityDeleted);
			LocalDate dateWhenDeleted = entry.getValue().getFirst().getDateWhenDeleted();
			output.add(new DeletedProduct(name, pricePerUnit, quantityDeleted, totalLoss, dateWhenDeleted));
		}
		return output;
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
	public <T extends AccountancyEntry> Optional<T> get(
		AccountancyEntry.AccountancyEntryIdentifier identifier, Class<T> type) {
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
	public <T extends AccountancyEntry> Map<Interval, Streamable<T>> find(
		Interval interval, TemporalAmount duration, Class<T> type) {
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
	 *
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

		if (allEntries.isEmpty() && getAllDeletedProducts().isEmpty()) {
			return null;
		}
		if (allEntries.isEmpty()) {
			return new DailyFinancialReport(
				interval,
				moneyThen,
				this,
				getAllDeletedProducts().getFirst().getDateWhenDeleted().atTime(9, 0),
				clockService);
		} else if (getAllDeletedProducts().isEmpty()) {
			return new DailyFinancialReport(
				interval,
				moneyThen,
				this,
				allEntries.getFirst().getTimestamp(),
				clockService);
		} else {
			LocalDateTime earlier = allEntries.getFirst().getTimestamp()
				.isBefore(getAllDeletedProducts().getFirst().getDateWhenDeleted().atTime(9, 0)) ?
				allEntries.getFirst().getTimestamp() : getAllDeletedProducts().getFirst().getDateWhenDeleted().atTime(9, 0);
			return new DailyFinancialReport(
				interval,
				moneyThen,
				this,
				earlier,
				clockService);
		}
	}

	/**
	 * Use this method instead of the MonthlyFinancialReport constructor
	 *
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
		if (allEntries.isEmpty() && getAllDeletedProducts().isEmpty()) {
			return null;
		}
		if (allEntries.isEmpty()) {
			return new MonthlyFinancialReport(
				interval,
				moneyThen,
				this,
				getAllDeletedProducts().getFirst().getDateWhenDeleted().atTime(9, 0),
				clockService);
		} else if (getAllDeletedProducts().isEmpty()) {
			return new MonthlyFinancialReport(
				interval,
				moneyThen,
				this,
				allEntries.getFirst().getTimestamp(),
				clockService);
		} else {
			LocalDateTime earlier = allEntries.getFirst().getTimestamp()
				.isBefore(getAllDeletedProducts().getFirst().getDateWhenDeleted().atTime(9, 0)) ?
				allEntries.getFirst().getTimestamp() : getAllDeletedProducts().getFirst().getDateWhenDeleted().atTime(9, 0);
			return new MonthlyFinancialReport(
				interval,
				moneyThen,
				this,
				earlier,
				clockService);
		}
	}

	/**
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
	 * @return the instance of CashRegister, stored in the repository
	 */
	public CashRegister getCashRegister() {
		Optional<CashRegister> cashRegister = cashRegisterRepository.findFirstByOrderById();
		return cashRegisterRepository.findFirstByOrderById()
			.orElseThrow(() -> new IllegalStateException("CashRegister instance not found"));
	}


	public List<AccountancyEntryWrapper> filterByCustomer(String customerName) {
		LinkedList<AccountancyEntryWrapper> filteredEntries = new LinkedList<>();
		for (AccountancyEntry entry : this.getCashRegister().getAccountancyEntries()) {
			if (((AccountancyEntryWrapper) entry).getClientName().contains(customerName)) {
				filteredEntries.add((AccountancyEntryWrapper) entry);
			}
		}
		return filteredEntries;
	}

	public List<AccountancyEntryWrapper> filterByPrice(double price) {
		final double EPSILON = 1e-6; // Tolerance for floating-point comparison
		LinkedList<AccountancyEntryWrapper> filteredEntries = new LinkedList<>();

		for (AccountancyEntry entry : this.getCashRegister().getAccountancyEntries()) {
			if (Math.abs(entry.getValue().getNumber().doubleValue() - price) < EPSILON) {
				filteredEntries.add((AccountancyEntryWrapper) entry);
			}
		}
		return filteredEntries;
	}

	public AccountancyEntryWrapper getEntry(Long orderId, List<AccountancyEntryWrapper> filteredAndCutOrdersList) {
		for (AccountancyEntryWrapper entry : filteredAndCutOrdersList) {
			if (entry.getId().equals(orderId)) {
				return entry;
			}
		}
		return null;
	}
}
