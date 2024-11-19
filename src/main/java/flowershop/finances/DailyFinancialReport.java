package flowershop.finances;

import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;

import javax.money.MonetaryAmount;

public class DailyFinancialReport extends FinancialReport{

	private Streamable<AccountancyEntry> orders;

	public DailyFinancialReport(Interval day,
								MonetaryAmount balanceEndOfTheDay,
								CashRegisterService cashRegister) {
		super(day, balanceEndOfTheDay, cashRegister);
		Streamable<AccountancyEntry> set = cashRegister.find(day);
		this.orders = set;
		this.income = cashRegister.getRevenue(set);
		this.expenditure = cashRegister.getExpences(set);

		countProfit();
		//do the rest.
	}

	public Streamable<AccountancyEntry> getOrders() {
		return this.orders;
	}


}
