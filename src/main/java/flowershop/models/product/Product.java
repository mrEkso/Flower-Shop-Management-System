package flowershop.models.product;

public class Product {
	private String name;
	private String type;
	private int quantity;
	private double pricePerUnit;

	public Product(String name, String type, int quantity, double pricePerUnit) {
		this.name = name;
		this.type = type;
		this.quantity = quantity;
		this.pricePerUnit = pricePerUnit;
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
