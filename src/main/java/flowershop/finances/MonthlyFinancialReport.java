package flowershop.finances;

import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;

import javax.money.MonetaryAmount;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MonthlyFinancialReport extends FinancialReport{

	private LinkedList<DailyFinancialReport> dailyFinancialReports = new LinkedList<>();

	public MonthlyFinancialReport(Interval month,
								  MonetaryAmount balanceEndOfTheMonth,
								  CashRegisterService cashRegister,
								  LocalDateTime firstEverTransaction){
		super(month, balanceEndOfTheMonth, cashRegister,firstEverTransaction);
		this.profit = Money.of(0,balanceEndOfTheMonth.getCurrency());
		this.expenditure = Money.of(0,balanceEndOfTheMonth.getCurrency());

		Map<Interval, Streamable<AccountancyEntry>> daysOfMonth = cashRegister.find(month, Duration.ofDays(1));
		List<Interval> sortedBackwards = daysOfMonth.keySet().stream().toList();
		sortedBackwards.sort(new IntervalComparator());
		MonetaryAmount moneyAfterEachDay = balanceEndOfTheMonth;
		for(Interval day : sortedBackwards) {
			DailyFinancialReport currentDay = new DailyFinancialReport(day,moneyAfterEachDay,cashRegister, firstEverTransaction);
			this.dailyFinancialReports.add(currentDay);
			moneyAfterEachDay.subtract(cashRegister.getProfit(currentDay.getOrders()));
			this.profit = this.profit.add(currentDay.getProfit());
			this.expenditure = this.expenditure.add(currentDay.getExpenditure());
		}
		countProfit();
	}

	@Override
	public boolean isBeforeBeginning() {
		for (DailyFinancialReport dailyFinancialReport : dailyFinancialReports) {
			if (!dailyFinancialReport.isBeforeBeginning()) {
				return false;
			}
		}
		return true;
	}

	private class IntervalComparator implements Comparator<Interval> {
		//Reverses backwards [31.12, 30.12...]
		@Override
		public int compare(Interval o1, Interval o2) {
			if(o1.getEnd().isAfter(o2.getEnd())) {
				return -1;
			} else if (o1.getEnd().isBefore(o2.getEnd())) {
				return +1;
			}
			else {
				return 0;
			}
		}
	}
}
