package flowershop.finances;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.data.util.Streamable;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.settings.VerticalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.cell.TextCell;

import javax.money.MonetaryAmount;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * This class is used to generate PDF-reports for a given day
 */
public class DailyFinancialReport extends FinancialReport {

	private List<AccountancyEntry> orders;

	public DailyFinancialReport(Interval day,
								MonetaryAmount balanceEndOfTheDay,
								CashRegisterService cashRegister,
								LocalDateTime firstEverTransaction) {
		super(day, balanceEndOfTheDay, cashRegister, firstEverTransaction);
		Streamable<AccountancyEntry> set = cashRegister.find(day);
		this.orders = new ArrayList<>(set.stream().toList());
		Collections.sort(orders, new Comparator<AccountancyEntry>() {
			@Override
			public int compare(AccountancyEntry first, AccountancyEntry second) {
				LocalDateTime ldt1 = ((AccountancyEntryWrapper) first).getTimestamp();
				LocalDateTime ldt2 = ((AccountancyEntryWrapper) second).getTimestamp();
				if (ldt1 != null && ldt2 != null) {
					return ldt1.compareTo(ldt2);
				} else {
					throw new IllegalStateException("Some entries dont have date assigned");
				}
			}
		});
		this.income = cashRegister.getRevenue(set);
		this.expenditure = cashRegister.getExpences(set);

		countProfit();
		//do the rest.
	}

	public Streamable<AccountancyEntry> getOrders() {
		return Streamable.of(this.orders);
	}

	/**
	 *
	 * @return true if no entries are registered at the moment, and no report can be made
	 */
	@Override
	public boolean isBeforeBeginning() {
		return orders.isEmpty() && this.startDate.isAfter(interval.getEnd());
	}

	/**
	 * @return for example "13.12.2024" if the report is made for 13 of December 2024
	 */
	@Override
	protected String intervalToString() {
		LocalDateTime day = interval.getStart();
		String month = (day.getMonth().getValue() < 10) ? "0" + day.getMonth().getValue() : String.valueOf(day.getMonth().getValue());
		String dateRepr = new StringBuilder().append(day.getDayOfMonth()).append(".").append(month).append(".").append(day.getYear()).toString();
		return dateRepr;
	}

	/**
	 * @param font to be used in these rows
	 * @return a list of the rows of the table that represents the day (date, balance in the morning, header,
	 * entries, profit and balance in the evening)
	 */
	@Override
	protected List<Row> getNeededRows(PDFont font) {
		List<Row> neededRows = new ArrayList<>();
		// adding date
		LocalDateTime day = interval.getStart();
		String month = (day.getMonth().getValue() < 10) ? "0" + day.getMonth().getValue() : String.valueOf(day.getMonth().getValue());
		String dateRepr = new StringBuilder().append(day.getDayOfMonth()).append(".").append(month).append(".").append(day.getYear()).toString();
		Row date = Row.builder()
			.add(TextCell.builder().text(
				(new StringBuilder().append(FinancialReport.getWeekdayNameDE(day.getDayOfWeek().getValue()))
					.append(", ").append(dateRepr)).toString()).backgroundColor(Color.WHITE).fontSize(16).horizontalAlignment(HorizontalAlignment.CENTER).colSpan(5).font(font).build()).build();
		neededRows.add(date);


		neededRows.add(emptyRow());
		// balance in the morning of the day
		Row kontostandMorning = Row.builder()
			.add(TextCell.builder()
				.text("Kontostand am Anfang des Tages:").font(font).fontSize(20).colSpan(3)
				.borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text(getBalance().subtract(getProfit()).toString()).font(font).fontSize(20)
				.colSpan(2).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.RIGHT)
				.build())
			.padding(10).borderWidth(1).build();
		neededRows.add(kontostandMorning);
		neededRows.add(emptyRow());

		// Header
		Row header = Row.builder()
			.add(TextCell.builder()
				.text("Zeitpunkt").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
				.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text("Typ").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
				.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text("Produkte").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
				.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text("Anzahl").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
				.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text("Summe").verticalAlignment(VerticalAlignment.MIDDLE).font(font).borderWidth(1)
				.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.build();
		neededRows.add(header);

		// actual transactions
		for (AccountancyEntry entry : orders) {
			AccountancyEntryWrapper realEntry = (AccountancyEntryWrapper) entry;
			int numRows = realEntry.getItems().size();
			int currentRow = 0;
			List<String> itemList = realEntry.getItems().keySet().stream().toList();
			while (currentRow < numRows) {
				Row.RowBuilder eintrag = Row.builder();
				if (currentRow == 0) {
					eintrag.add(TextCell.builder()
							.text(realEntry.getTimestamp().toString()).rowSpan(numRows).verticalAlignment(VerticalAlignment.TOP)
							.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT)
							.build())
						.add(TextCell.builder()
							.text(realEntry.getCategory()).rowSpan(numRows).verticalAlignment(VerticalAlignment.TOP)
							.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT)
							.build());
				}
				String productName = itemList.get(currentRow);
				eintrag.add(TextCell.builder()
					.text(productName).verticalAlignment(VerticalAlignment.TOP)
					.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT)
					.build());
				eintrag.add(TextCell.builder()
					.text(String.valueOf(realEntry.getItems().get(productName))).verticalAlignment(VerticalAlignment.TOP)
					.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.CENTER)
					.build());
				if (currentRow == 0) {
					eintrag.add(
						TextCell.builder()
							.text(realEntry.getValue().toString()).rowSpan(numRows).verticalAlignment(VerticalAlignment.MIDDLE)
							.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT)
							.build()
					);
				}
				neededRows.add(eintrag.build());
				currentRow++;
			}
		}

		neededRows.add(emptyRow());
		// day difference
		MonetaryAmount profit = getProfit();
		String profitRepr = profit.toString();
		Row difference = Row.builder()
			.add(TextCell.builder()
				.text("Tagesdifferenz:").font(font).fontSize(20).colSpan(3)
				.borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text(profitRepr).font(font).fontSize(20)
				.colSpan(2).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.RIGHT)
				.build())
			.padding(10).borderWidth(1).build();
		neededRows.add(difference);

		//balance in the evening
		Row evening = Row.builder()
			.add(TextCell.builder()
				.text(new StringBuilder().append("Kontostand am Ende des Tages (").append(dateRepr).append(")").toString())
				.font(font).fontSize(16).colSpan(3).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
				.build())
			.add(TextCell.builder()
				.text(getBalance().toString()).font(font).fontSize(20)
				.colSpan(2).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.RIGHT)
				.build())
			.padding(10).borderWidth(1).build();
		neededRows.add(evening);

		return neededRows;
	}
}
