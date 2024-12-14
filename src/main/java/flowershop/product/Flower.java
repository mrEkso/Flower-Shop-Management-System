package flowershop.product;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import org.javamoney.moneta.Money;
import org.salespointframework.catalog.Product;

@Entity
public class Flower extends Product {

	@Embedded
	private Pricing pricing;

	private String color;
	private Integer quantity;
	private Integer deletedQuantity;

	public Flower(String name, Pricing pricing, String color, Integer quantity) {
		super(name, pricing.getSellPrice());
		this.pricing = pricing;
		this.color = color;
		this.quantity = quantity;
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

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public Integer getDeletedQuantity() {
		return deletedQuantity;
	}

	public void setDeletedQuantity(Integer deletedQuantity) {
		this.deletedQuantity = deletedQuantity;
	}
}
