package flowershop.inventory;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeletedProduct {
	private String name;
	private MonetaryAmount pricePerUnit;
	private int quantityDeleted;
	private MonetaryAmount totalLoss;
	private LocalDate dateWhenDeleted;

	public DeletedProduct(String name, MonetaryAmount pricePerUnit, int quantityDeleted, MonetaryAmount totalLoss, LocalDate dateWhenDeleted) {
		this.name = name;
		this.pricePerUnit = pricePerUnit;
		this.quantityDeleted = quantityDeleted;
		this.totalLoss = totalLoss;
		this.dateWhenDeleted = dateWhenDeleted;
	}
	public String getName() {
		return name;
	}

	public MonetaryAmount getPricePerUnit() {
		return pricePerUnit;
	}

	public int getQuantityDeleted() {
		return quantityDeleted;
	}

	public MonetaryAmount getTotalLoss() {
		return totalLoss;
	}

	public int getQuantity() {
		return quantityDeleted;
	}

	public LocalDate getDateWhenDeleted() { return dateWhenDeleted; }
}
