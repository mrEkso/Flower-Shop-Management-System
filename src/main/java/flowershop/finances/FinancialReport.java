package flowershop.finances;

import org.salespointframework.time.Interval;

import javax.money.MonetaryAmount;

public abstract class FinancialReport {
	protected MonetaryAmount income;
	protected MonetaryAmount expenditure;
	protected MonetaryAmount profit; //difference
	protected MonetaryAmount balance;


	public FinancialReport(Interval period,
						   MonetaryAmount balanceEndOfThePeriod,
						   CashRegisterService cashRegister) {
		this.balance = balanceEndOfThePeriod;
		//this.orders = orders;
		//count the fields based on orders
	}
	protected void countProfit() {
		this.profit = this.income.add(this.expenditure);
	}
	public void generatePDF(){

	}

	public MonetaryAmount getBalance() {
		return balance;
	}

	public MonetaryAmount getExpenditure() {
		return expenditure;
	}

	public MonetaryAmount getIncome() {
		return income;
	}

	public MonetaryAmount getProfit() {
		return profit;
	}
}
