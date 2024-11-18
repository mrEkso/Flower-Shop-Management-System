package flowershop.finances;

import jakarta.persistence.*;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.AccountancyEntry;
import org.springframework.data.util.Streamable;

@Entity
public class CashRegister  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//private final OrderManagement<AbstractOrder> orderManagement;
	@Transient
	private Streamable<AccountancyEntry> accountancyEntries = Streamable.empty();

	private Money balance;


	public Streamable<AccountancyEntry> getAccountancyEntries() {
		return accountancyEntries;
	}

	public void setAccountancyEntries(Streamable<AccountancyEntry> accountancyEntries) {
		this.accountancyEntries = accountancyEntries;
	}

	protected CashRegister() {}


	public void setBalance(Money balance) {
		this.balance = balance;
	}

	public Money getBalance() {
		return balance;
	}

	public CashRegister(Streamable<AccountancyEntry> accountancyEntries,
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
