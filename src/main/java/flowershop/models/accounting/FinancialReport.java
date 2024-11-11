package flowershop.models.accounting;

import flowershop.models.order.AbstractOrder;

import java.time.LocalDateTime;
import java.util.List;

public class FinancialReport {
	private List<AbstractOrder> orders;
	private double income;
	private double expenditure;
	private double profit;
	private double balance;
	private double balanceSomeTimeBefore;
	public FinancialReport(LocalDateTime day, double balanceSomeTimeBefore) {
		//fill orders
		//count the fields based on orders
	}
	protected void initialize() {
		this.profit = this.income-this.expenditure;
		this.balance = this.balanceSomeTimeBefore + this.profit;
	}
	public void generatePDF(){

	}
}
