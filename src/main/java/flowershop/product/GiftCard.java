package flowershop.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.javamoney.moneta.Money;

import java.util.UUID;

import javax.money.MonetaryAmount;

@Entity
public class GiftCard {
	@Id
	private UUID id;

	private MonetaryAmount balance;
	private final String type;

	public GiftCard(MonetaryAmount startBalance, String type) {
		this.type = type;
		this.balance = startBalance;
		this.id = UUID.randomUUID();
	}

	public GiftCard() {
		this.type = "20";
		this.balance = Money.of(20.0, "EUR");
	}

	public MonetaryAmount getBalance() {
		return balance;
	}

	public String getType() {
		return type;
	}

	public void setBalance(MonetaryAmount balance) {
		this.balance = balance;
	}

	public void subtractBalance(MonetaryAmount amount) {
		this.balance = balance.subtract(amount);
	}

	public String getId() {
		return id.toString();
	}
}
