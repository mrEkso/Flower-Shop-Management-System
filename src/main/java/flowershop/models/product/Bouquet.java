package flowershop.models.product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import flowershop.models.embedded.Pricing;
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

	public Bouquet(String name, Pricing pricing, List<Flower> flowers, Money additionalPrice) {
		super(name, pricing.getSellPrice());
		this.pricing = pricing;
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
		super.setPrice(pricing.getSellPrice()); // Обновляем цену в Product
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
}
