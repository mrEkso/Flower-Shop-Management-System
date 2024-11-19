package flowershop.models.products;

public class Product {
	private String name;
	private String type; // "Flower" or "Bouquet"
	private int quantity;
	private double pricePerUnit;

	public Product(String name, String type, int quantity, double pricePerUnit) {
		this.name = name;
		this.type = type;
		this.quantity = quantity;
		this.pricePerUnit = pricePerUnit;
	}

	public Product(Flower flower) {
		this.name = flower.getName();
		this.type = "Flower";
		this.quantity = flower.getQuantity();
		this.pricePerUnit = flower.getPricing().getSellPrice().getNumber().doubleValueExact();
	}

	public Product(Bouquet bouquet) {
		this.name = bouquet.getName();
		this.type = "Bouquet";
		this.quantity = 1; // Bouquets are usually treated as single units
		this.pricePerUnit = bouquet.getPricing().getSellPrice().getNumber().doubleValueExact();
	}

	// Getters and setters
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPricePerUnit() {
		return pricePerUnit;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}


