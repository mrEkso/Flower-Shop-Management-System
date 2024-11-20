//package flowershop.product;
//
//public class Product {
//	private String name;
//	private String type; // "Flower" or "Bouquet"
//	private int quantity;
//	private double pricePerUnit;
//
//	public Product(String name, String type, int quantity, double pricePerUnit) {
//		this.name = name;
//		this.type = type;
//		this.quantity = quantity;
//		this.pricePerUnit = pricePerUnit;
//	}
//
//	public Product(Flower flower) {
//		this.name = flower.getName();
//		this.type = "Flower";
//		this.quantity = flower.getQuantity();
//		this.pricePerUnit = (flower.getPricing() != null && flower.getPricing().getSellPrice() != null)
//			? flower.getPricing().getSellPrice().getNumber().doubleValueExact()
//			: 0.0;
//	}
//
//	public Product(Bouquet bouquet) {
//		this.name = bouquet.getName();
//		this.type = "Bouquet";
//		this.quantity = 1;
//		this.pricePerUnit = (bouquet.getPricing() != null && bouquet.getPricing().getSellPrice() != null)
//			? bouquet.getPricing().getSellPrice().getNumber().doubleValueExact()
//			: 0.0;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public String getType() {
//		return type;
//	}
//
//	public int getQuantity() {
//		return quantity;
//	}
//
//	public double getPricePerUnit() {
//		return pricePerUnit;
//	}
//
//	public void setQuantity(int quantity) {
//		this.quantity = quantity;
//	}
//}
