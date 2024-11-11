package kickstart.Davyd_Lera.models.products;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import kickstart.Davyd_Lera.models.embedded.Pricing;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;

import java.util.List;

@Entity
public class Bouquet extends Product {

	@OneToMany(cascade = CascadeType.ALL)
	private List<Flower> flowers;

	private Money additionalPrice;

	public Bouquet(String name, List<Flower> flowers, Money additionalPrice) {
		super(name, calculateTotalPricing(flowers, additionalPrice).getSellPrice());
		this.flowers = flowers;
		this.additionalPrice = additionalPrice;
	}

	@SuppressWarnings({"unused", "deprecation"})
	public Bouquet() {
	}

	public List<Flower> getFlowers() {
		return flowers;
	}

	public void setFlowers(List<Flower> flowers) {
		this.flowers = flowers;
	}

	public Money getAdditionalPrice() {
		return additionalPrice;
	}

	public void setAdditionalPrice(Money additionalPrice) {
		this.additionalPrice = additionalPrice;
	}

	// Static method to calculate the total buying and selling price of the bouquet
	private static Pricing calculateTotalPricing(List<Flower> flowers, Money additionalPrice) {
		// Calculate the total buy price of all flowers
		Money totalBuyPrice = flowers.stream()
			.map(flower -> flower.getPricing().getBuyPrice()) // Get each flower's buy price
			.reduce(Money.of(0, additionalPrice.getCurrency()), Money::add); // Sum up all buy prices

		// Calculate the total sell price of all flowers and add the additional price
		Money totalSellPrice = flowers.stream()
			.map(flower -> flower.getPricing().getSellPrice()) // Get each flower's sell price
			.reduce(Money.of(0, additionalPrice.getCurrency()), Money::add) // Sum up all sell prices
			.add(additionalPrice); // Add the additional price to the total sell price

		// Return a Pricing object with the calculated total buy and sell prices
		return new Pricing(totalBuyPrice, totalSellPrice);
	}
}
