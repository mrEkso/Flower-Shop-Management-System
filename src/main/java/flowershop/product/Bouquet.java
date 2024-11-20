package flowershop.product;

import jakarta.persistence.*;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Bouquet extends Product {

//	@OneToMany(cascade = CascadeType.ALL)
//	private List<Flower> flowers;

	@Transient
	private Map<Flower, Integer> flowers = new HashMap<>();

	@Embedded
	private Pricing pricing;

	private Money additionalPrice;

	private int quantity;

	public Bouquet(String name, Map<Flower, Integer> flowers, Money additionalPrice, int quantity) {
		super(name, calculateTotalPricing(flowers, additionalPrice).getSellPrice());
		this.flowers = flowers;
		this.additionalPrice = additionalPrice;
		this.quantity = quantity;
	}

	@SuppressWarnings({"unused", "deprecation"})
	public Bouquet() {
	}

	public Pricing getPricing() {
		return pricing;
	}

	public void setPricing(Pricing pricing) {
		this.pricing = pricing;
		super.setPrice(pricing.getSellPrice());
	}

	public void addFlower(Flower flower, int quantity) {
		flowers.put(flower, flowers.getOrDefault(flower, 0) + quantity);
	}

	public Money getAdditionalPrice() {
		return additionalPrice;
	}

	public void setAdditionalPrice(Money additionalPrice) {
		this.additionalPrice = additionalPrice;
	}

	public static Pricing calculateTotalPricing(Map<Flower, Integer> flowers, Money additionalPrice) {

		// Calculate total buy price
		Money totalBuyPrice = flowers.entrySet().stream()
			.map(entry -> entry.getKey().getPricing().getBuyPrice().multiply(entry.getValue()))
			.reduce(Money.of(0, additionalPrice.getCurrency()), Money::add);

		// Calculate total sell price
		Money totalSellPrice = flowers.entrySet().stream()
			.map(entry -> entry.getKey().getPricing().getSellPrice().multiply(entry.getValue()))
			.reduce(Money.of(0, additionalPrice.getCurrency()), Money::add)
			.add(additionalPrice);

		return new Pricing(totalBuyPrice, totalSellPrice);
	}

	public Map<Flower, Integer> getFlowers() {
		return flowers;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
