package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.inventory.DeletedProduct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.salespointframework.time.Interval;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.settings.VerticalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import javax.money.MonetaryAmount;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Is used to generate financial PDF-reports
 */
public abstract class FinancialReport {
	protected MonetaryAmount income;
	protected MonetaryAmount expenditure;
	protected MonetaryAmount profit; //difference
	protected MonetaryAmount balance;
	protected LocalDateTime startDate;
	protected Interval interval;
	protected ClockService clockService;
	protected List<DeletedProduct> deletedProducts;

	public FinancialReport(Interval period,
						   MonetaryAmount balanceEndOfThePeriod,
						   CashRegisterService cashRegister,
						   LocalDateTime firstEverTransaction,
						   ClockService clockService) {
		this.balance = balanceEndOfThePeriod;
		this.interval = period;
		this.startDate = firstEverTransaction;
		this.clockService = clockService;
		//this.orders = orders;
		//count the fields based on orders
	}

	/**
	 *
	 * @return the ready-made file of the report
	 */
	public byte[] generatePDF() {
		try (PDDocument document = new PDDocument()) {
			InputStream inFont = getClass().getResourceAsStream("/fonts/josefin-sans.semibold.ttf");
			System.out.println(inFont.toString());
      		PDType0Font customFont = PDType0Font.load(document, inFont);

			TableDrawer.builder()
				.startX(50)
				.endY(50)
				.startY(780)
				.table(buildTheTable(customFont))
				.build()
				.draw(() -> document, () -> new PDPage(PDRectangle.A4), 50);
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				document.save(outputStream);
				return outputStream.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public abstract boolean isBeforeBeginning();

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

	/**
	 *
	 * @param day number of the weekday (1-7)
	 * @return german name of the weekday
	 */
	public static String getWeekdayNameDE(int day) {
		switch (day) {
			case 1:
				return "Montag";
			case 2:
				return "Dienstag";
			case 3:
				return "Mittwoch";
			case 4:
				return "Donnerstag";
			case 5:
				return "Freitag";
			case 6:
				return "Samstag";
			case 7:
				return "Sonntag";
			default:
				return "";
		}
	}

	/**
	 * Calculates the profit variable
	 */
	protected void countProfit() {
		this.profit = this.income.add(this.expenditure);
	}

	/**
	 *
	 * @param font to be used in the document
	 * @return ready Table for being wrapped into the document
	 */
	protected Table buildTheTable(PDFont font) {
		// Add the header and "Finanzuebersicht fuer ... here
		List<Row> rows = getNeededRows(font);
		System.out.println(rows.size());
		Table.TableBuilder builder = Table.builder()
			.addColumnsOfWidth(110, 115, 110, 50, 45, 55);
		Row shapka1 = Row.builder()
			.add(TextCell.builder()
				.text(" ").fontSize(16).colSpan(2)
				.build())
			.add(TextCell.builder()
				.text("Floris Blumenladen Dresden").fontSize(16).colSpan(4).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(shapka1);
		Row adress = Row.builder()
			.add(TextCell.builder()
				.text(" ").fontSize(16).colSpan(2)
				.build())
			.add(TextCell.builder()
				.text("Wiener Platz 4, 01069 Dresden").fontSize(16).colSpan(4).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(adress);
		LocalDateTime day = clockService.now();
		String month = (day.getMonth().getValue() < 10) ? "0" + day.getMonth().getValue() : String.valueOf(day.getMonth().getValue());
		String dateRepr = new StringBuilder().append(day.getDayOfMonth()).append(".").append(month).append(".").append(day.getYear()).toString();

		Row datum = Row.builder()
			.add(TextCell.builder()
				.text(" ").fontSize(16).colSpan(2)
				.build())
			.add(TextCell.builder()
				.text("Am " + dateRepr).fontSize(16).colSpan(4).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(datum);
		builder.addRow(emptyRow());
		//builder.addRow(emptyRow());
		Row title = Row.builder()
			.add(TextCell.builder()
				.text("Finanzübersicht für " + intervalToString()).font(font).fontSize(24).colSpan(6).horizontalAlignment(HorizontalAlignment.CENTER)
				.build())
			.build();
		builder.addRow(title);
		builder.addRow(emptyRow());
		for (Row row : rows) {
			builder.addRow(row);
		}
		return builder.build();
	}

	public List<Row> getDeletedProductRows(PDFont font){
		List<Row> neededRows = new java.util.ArrayList<>();
		if(!this.deletedProducts.isEmpty()){
			Row title = Row.builder()
				.add(TextCell.builder()
					.text(this instanceof DailyFinancialReport ? "Verwelkte Blumen an diesem Tag:" : "Verwelkte Blumen in diesem Monat:")
					.font(font).fontSize(14).colSpan(6).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.CENTER)
					.build())
				.build();
			neededRows.add(title);
			Row headerDeleted = Row.builder()
				.add(TextCell.builder()
					.text("Produkt").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
					.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
					.build())
				.add(TextCell.builder()
					.text("Preis pro Stück").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
					.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
					.build())
				.add(TextCell.builder()
					.text("Anzahl").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
					.fontSize(14).colSpan(1).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
					.build())
				.add(TextCell.builder()
					.text("Einnahmen verloren").horizontalAlignment(HorizontalAlignment.CENTER).font(font).borderWidth(1)
					.fontSize(14).colSpan(3).borderColor(Color.BLACK).horizontalAlignment(HorizontalAlignment.LEFT)
					.build())
				.build();
			neededRows.add(headerDeleted);
			for (DeletedProduct deletedProduct : this.deletedProducts) {
				Row deleted = Row.builder()
					.add(TextCell.builder()
						.text(deletedProduct.getName()).verticalAlignment(VerticalAlignment.TOP)
						.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT)
						.build())
					.add(TextCell.builder()
						.text(deletedProduct.getPricePerUnit().toString()).verticalAlignment(VerticalAlignment.TOP)
						.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT)
						.build())
					.add(TextCell.builder()
						.text(String.valueOf(deletedProduct.getQuantityDeleted())).verticalAlignment(VerticalAlignment.TOP)
						.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT)
						.build())
					.add(TextCell.builder()
						.text(String.valueOf(deletedProduct.getTotalLoss())).verticalAlignment(VerticalAlignment.TOP)
						.font(font).fontSize(10).horizontalAlignment(HorizontalAlignment.LEFT).colSpan(3)
						.build())
					.build();
				neededRows.add(deleted);
			}
			MonetaryAmount total = this.deletedProducts.stream()
				.map(DeletedProduct::getTotalLoss)
				.reduce(MonetaryAmount::add).get();
			Row totalLosses = Row.builder()
				.add(TextCell.builder()
					.text("Gesamte Verluste:").verticalAlignment(VerticalAlignment.TOP).colSpan(3)
					.font(font).fontSize(14).horizontalAlignment(HorizontalAlignment.LEFT)
					.build())
				.add(TextCell.builder()
					.text(total.toString()).verticalAlignment(VerticalAlignment.TOP).colSpan(3)
					.font(font).fontSize(14).horizontalAlignment(HorizontalAlignment.LEFT)
					.build()).borderWidth(1).borderColor(Color.BLACK).verticalAlignment(VerticalAlignment.MIDDLE)
				.build();
			neededRows.add(totalLosses);
		}
		return neededRows;
	}

	/**
	 *
	 * @return the instance of an empty row (just to add distance between rows)
	 */
	protected Row emptyRow() {
		return Row.builder().add(TextCell.builder().text("  ").colSpan(6).fontSize(10).build())
			.build();
	}

	/**
	 * Will return either 12.2024 or 13.12.2024 depending on is it month or day report
	 *
	 * @return
	 */
	protected abstract String intervalToString();

	/**
	 * Will return a list of the rows of the table that represents the period
	 *
	 * @param font
	 * @return
	 */
	protected abstract List<Row> getNeededRows(PDFont font);
}
