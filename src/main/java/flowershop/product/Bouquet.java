package flowershop.product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;

import java.util.List;

@Entity
public class Bouquet extends Product {

	@OneToMany(cascade = CascadeType.ALL)
	private List<Flower> flowers;

	@Embedded
	private Pricing pricing;

	private Money additionalPrice;

	public Bouquet(String name, List<Flower> flowers, Money additionalPrice) {
		super(name, calculateTotalPricing(flowers, additionalPrice).getSellPrice());
		this.flowers = flowers;
		this.additionalPrice = additionalPrice;
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


	
	private static Pricing calculateTotalPricing(List<Flower> flowers, Money additionalPrice) {
		
		Money totalBuyPrice = flowers.stream()
			.map(flower -> flower.getPricing().getBuyPrice()) 
			.reduce(Money.of(0, additionalPrice.getCurrency()), Money::add); 

		
		Money totalSellPrice = flowers.stream()
			.map(flower -> flower.getPricing().getSellPrice()) 
			.reduce(Money.of(0, additionalPrice.getCurrency()), Money::add) 
			.add(additionalPrice); 

		return new Pricing(totalBuyPrice, totalSellPrice);
	}

}
