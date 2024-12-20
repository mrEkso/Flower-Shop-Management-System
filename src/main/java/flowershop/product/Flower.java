package flowershop.product;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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

	protected void addQuantity(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("Quantity to add cannot be negative");
		}
		this.quantity += quantity;
	}

	protected void reduceQuantity(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("Quantity to reduce cannot be negative");
		}
		if (this.quantity < quantity) {
			throw new IllegalArgumentException("Insufficient quantity to reduce");
		}
		this.quantity -= quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getDeletedQuantity() {
		return deletedQuantity;
	}

	public void setDeletedQuantity(int deletedQuantity) {
		this.deletedQuantity = deletedQuantity;
	}
}
