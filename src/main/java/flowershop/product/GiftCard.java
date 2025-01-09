package flowershop.product;

import jakarta.persistence.Entity;
import org.salespointframework.catalog.Product;

import javax.money.MonetaryAmount;

@Entity
public class GiftCard extends Product {
	private MonetaryAmount balance;

	public GiftCard(MonetaryAmount startBalance) {
		super( startBalance.toString() + " Gift Card", startBalance);
		balance = startBalance;
	}

	@SuppressWarnings({"unused", "deprecation"})
	public GiftCard() {

	}
}
