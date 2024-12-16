package flowershop.finances;

import flowershop.clock.PendingOrder;
import jakarta.persistence.*;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.AccountancyEntry;
import org.springframework.data.util.Streamable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Is used to store all AccountancyEntries in the database, as well as balance
 */
@Entity
public class CashRegister {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//private final OrderManagement<AbstractOrder> orderManagement;
	//@Transient
	@OneToMany(cascade = CascadeType.ALL)
	private Set<AccountancyEntry> accountancyEntries; //= Streamable.empty();

	private Money balance;

	private final LocalDate firstEverDate;

	private LocalDate currentDate;
	private LocalDateTime newDayStarted;

	private boolean isOpen;

	@OneToMany(mappedBy = "yourEntity", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<PendingOrder> entryDates = new HashSet<>();


	public Set<AccountancyEntry> getAccountancyEntries() {
		return accountancyEntries;
	}

	public void setAccountancyEntries(Set<AccountancyEntry> accountancyEntries) {
		this.accountancyEntries = accountancyEntries;
	}
	public LocalDate getFirstEverDate() {
		return firstEverDate;
	}


	protected CashRegister() {
		this.firstEverDate = LocalDate.now();
	}


	public void setBalance(Money balance) {
		this.balance = balance;
	}

	public boolean isOpen() {
		return isOpen;
	}
	public void setOpen(boolean open) {
		isOpen = open;
	}
	public LocalDateTime getNewDayStarted() {
		return newDayStarted;
	}
	public void setNewDayStarted(LocalDateTime newDayStarted) {
		this.newDayStarted = newDayStarted;
	}

	public Money getBalance() {
		return balance;
	}
	public LocalDate getCurrentDate() {
		return currentDate;
	}
	public void setCurrentDate(LocalDate currentDate) {
		this.currentDate = currentDate;
	}

	public CashRegister(Set<AccountancyEntry> accountancyEntries,
						Money balance) {
		this.accountancyEntries = accountancyEntries;
		this.balance = balance;
		this.firstEverDate = LocalDate.now();
		/*
		Streamable<AbstractOrder> previousOrders = orderManagement.findBy(OrderStatus.PAID);
		for (Order order : previousOrders) {
			AccountancyEntry convertedOrder = new AccountancyEntryWrapper((AbstractOrder) order);
			this.add(convertedOrder);
		}
		 */
	}

	public Long getId() {
		return id;
	}
}
