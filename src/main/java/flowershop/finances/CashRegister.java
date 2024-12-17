package flowershop.finances;

import flowershop.clock.PendingOrder;
import jakarta.persistence.*;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.AccountancyEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
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

	private LocalDate inGameDate;
	private LocalDateTime newDayStarted;

	private boolean open;

	@OneToMany(cascade = CascadeType.ALL)
	private Set<PendingOrder> pendingOrders;


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
		this.open = true;
	}


	public void setBalance(Money balance) {
		this.balance = balance;
	}

	public boolean getOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
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
	public LocalDate getInGameDate() {
		return inGameDate;
	}
	public void setInGameDate(LocalDate currentDate) {
		this.inGameDate = currentDate;
	}

	public void setPendingOrders(Set<PendingOrder> pendingOrders) {
		this.pendingOrders = pendingOrders;
	}

	public Set<PendingOrder> getPendingOrders() {
		return pendingOrders;
	}

	public CashRegister(Set<AccountancyEntry> accountancyEntries,
						Money balance) {
		this.accountancyEntries = accountancyEntries;
		this.balance = balance;
		this.firstEverDate = LocalDate.now();
		this.pendingOrders = new HashSet<>();
		this.open = true;
		this.newDayStarted = LocalDateTime.now();
		this.inGameDate = LocalDate.now();

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
