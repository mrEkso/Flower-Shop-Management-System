package flowershop.finances;

import flowershop.clock.ClockService;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;
import org.vandeseer.easytable.settings.BorderStyle;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.cell.TextCell;

import javax.money.MonetaryAmount;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

/**
 * Is used to create financial PDF-reports for a given month
 */
public class MonthlyFinancialReport extends FinancialReport {

	private LinkedList<DailyFinancialReport> dailyFinancialReports = new LinkedList<>();

	public MonthlyFinancialReport(Interval month,
								  MonetaryAmount balanceEndOfTheMonth,
								  CashRegisterService cashRegister,
								  LocalDateTime firstEverTransaction,
								  ClockService clockService) {
		super(month, balanceEndOfTheMonth, cashRegister, firstEverTransaction, clockService);
		this.income = Money.of(0, balanceEndOfTheMonth.getCurrency());
		this.expenditure = Money.of(0, balanceEndOfTheMonth.getCurrency());

		Map<Interval, Streamable<AccountancyEntry>> daysOfMonth = cashRegister.find(month, Duration.ofDays(1));
		List<Interval> sortedBackwards = new ArrayList<>(daysOfMonth.keySet().stream().toList());
		sortedBackwards.sort(new IntervalComparator());
		MonetaryAmount moneyAfterEachDay = balanceEndOfTheMonth;
		for (Interval day : sortedBackwards) {
			DailyFinancialReport currentDay = new DailyFinancialReport(day, moneyAfterEachDay, cashRegister, firstEverTransaction, clockService);
			this.dailyFinancialReports.add(currentDay);
			moneyAfterEachDay = moneyAfterEachDay.subtract(currentDay.getProfit());
			this.income = this.income.add(currentDay.getIncome());
			this.expenditure = this.expenditure.add(currentDay.getExpenditure());
		}
		this.dailyFinancialReports = this.dailyFinancialReports.reversed();
		this.deletedProducts = cashRegister.findDeletedProductsByMonth(month.getStart().toLocalDate());
		countProfit();
	}

	/**
	 *
	 * @return true if there is no data registered during the whole month because the orders started to appear after this month
	 */
	@Override
	public boolean isBeforeBeginning() {
		for (DailyFinancialReport dailyFinancialReport : dailyFinancialReports) {
			if (!dailyFinancialReport.isBeforeBeginning()) {
				return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @return for example "12.2024" for December of 2024
	 */
	@Override
	protected String intervalToString() {
		LocalDateTime day = interval.getStart();
		String month = (day.getMonth().getValue() < 10) ? "0" + day.getMonth().getValue() : String.valueOf(day.getMonth().getValue());
		String dateRepr = new StringBuilder().append(month).append(".").append(day.getYear()).toString();
		return dateRepr;
	}

	/**
	 *
	 * @param font
	 * @return a list of the rows of the table that represents the period (rows for all days that have some orders from that time)
	 */
	@Override
	protected List<Row> getNeededRows(PDFont font) {
		List<Row> neededRows = new ArrayList<>();

		for (DailyFinancialReport dailyFinancialReport : dailyFinancialReports) {
			if (dailyFinancialReport.getOrders().isEmpty() && dailyFinancialReport.deletedProducts.isEmpty()) {
				continue;
			}
			neededRows.addAll(dailyFinancialReport.getNeededRows(font));
			neededRows.add(emptyRow());
			neededRows.add(emptyRow());
		}
		MonetaryAmount profit = getProfit();
		String profitRepr = profit.toString();

		Row difference = Row.builder()
			.add(TextCell.builder()
				.text("Monatsdifferenz:").font(font).fontSize(14).colSpan(3)
				.borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text(profitRepr).font(font).fontSize(14)
				.colSpan(3).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.RIGHT)
				.build())
			.padding(10).borderWidth(1.5f).borderStyle(BorderStyle.DOTTED).build();
		neededRows.add(difference);
		neededRows.add(emptyRow());
		neededRows.addAll(this.getDeletedProductRows(font));
		return neededRows;
	}

	/**
	 * Is used to sort Intervals backwards
	 */
	private static class IntervalComparator implements Comparator<Interval> {
		//Reverses backwards [31.12, 30.12...]
		@Override
		public int compare(Interval o1, Interval o2) {
			if (o1.getEnd().isAfter(o2.getEnd())) {
				return -1;
			} else if (o1.getEnd().isBefore(o2.getEnd())) {
				return +1;
			} else {
				return 0;
			}
		}
	}

	public LinkedList<DailyFinancialReport> getDailyFinancialReports() {
		return dailyFinancialReports;
	}
}
