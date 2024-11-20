package flowershop.finances;

import jakarta.persistence.*;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.AccountancyEntry;
import org.springframework.data.util.Streamable;

import java.util.List;
import java.util.Set;

@Entity
public class CashRegister  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//private final OrderManagement<AbstractOrder> orderManagement;
	//@Transient
	@OneToMany(cascade = CascadeType.ALL)
	private Set<AccountancyEntry> accountancyEntries; //= Streamable.empty();

	private Money balance;


	public Set<AccountancyEntry> getAccountancyEntries() {
		return accountancyEntries;
	}

	public void setAccountancyEntries(Set<AccountancyEntry> accountancyEntries) {
		this.accountancyEntries = accountancyEntries;
	}

	protected CashRegister() {}


	public void setBalance(Money balance) {
		this.balance = balance;
	}

	public Money getBalance() {
		return balance;
	}

	public CashRegister(Set<AccountancyEntry> accountancyEntries,
						Money balance) {
		this.accountancyEntries = accountancyEntries;
		this.balance = balance;
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
