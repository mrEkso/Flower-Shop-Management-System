package flowershop.inventory;

public class DeletedProduct {
	private String name;
	private double pricePerUnit;
	private int quantityDeleted;
	private double totalLoss;

	public DeletedProduct(String name, double pricePerUnit, int quantityDeleted, double totalLoss) {
		this.name = name;
		this.pricePerUnit = pricePerUnit;
		this.quantityDeleted = quantityDeleted;
		this.totalLoss = totalLoss;
	}
	public String getName() {
		return name;
	}

	public double getPricePerUnit() {
		return pricePerUnit;
	}

	public int getQuantityDeleted() {
		return quantityDeleted;
	}

	public double getTotalLoss() {
		return totalLoss;
	}
}
