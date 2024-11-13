package flowershop.models.product;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import flowershop.models.embedded.Pricing;
import org.salespointframework.catalog.Product;

@Entity
public class Flower extends Product {

	@Embedded
	private Pricing pricing;

	private String color;

	public Flower(String name, Pricing pricing, String color) {
		super(name, pricing.getSellPrice());
		this.pricing = pricing;
		this.color = color;
	}

	@SuppressWarnings({"unused", "deprecation"})
	public Flower() {
	}

	public Pricing getPricing() {
		return pricing;
	}

	public void setPricing(Pricing pricing) {
		this.pricing = pricing;
		super.setPrice(pricing.getSellPrice());
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
