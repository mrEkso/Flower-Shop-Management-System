package flowershop.inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeletedProduct {
	private String name;
	private double pricePerUnit;
	private int quantityDeleted;
	private double totalLoss;
	private LocalDate dateWhenDeleted;

	public DeletedProduct(String name, double pricePerUnit, int quantityDeleted, double totalLoss, LocalDate dateWhenDeleted) {
		this.name = name;
		this.pricePerUnit = pricePerUnit;
		this.quantityDeleted = quantityDeleted;
		this.totalLoss = totalLoss;
		this.dateWhenDeleted = dateWhenDeleted;
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

	public int getQuantity() {
		return quantityDeleted;
	}

	public LocalDate getDateWhenDeleted() { return dateWhenDeleted; }
}
