package flowershop.finances;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.salespointframework.time.Interval;

import javax.money.MonetaryAmount;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class FinancialReport {
	protected MonetaryAmount income;
	protected MonetaryAmount expenditure;
	protected MonetaryAmount profit; //difference
	protected MonetaryAmount balance;
	protected LocalDateTime startDate;
	protected Interval interval;



	public FinancialReport(Interval period,
						   MonetaryAmount balanceEndOfThePeriod,
						   CashRegisterService cashRegister,
						   LocalDateTime firstEverTransaction) {
		this.balance = balanceEndOfThePeriod;
		this.interval = period;
		this.startDate = firstEverTransaction;
		//this.orders = orders;
		//count the fields based on orders
	}
	public abstract boolean isBeforeBeginning();
	protected void countProfit() {
		this.profit = this.income.add(this.expenditure);
	}
	public byte[] generatePDF(){
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				PDType0Font customFont = PDType0Font.load(document, new File("src/main/resources/fonts/josefin-sans.semibold.ttf"));
				contentStream.setFont(customFont, 12);
				contentStream.beginText();
				contentStream.setLeading(14.5f);
				contentStream.newLineAtOffset(50, 750); //50,750
				contentStream.showText("Sample Table on A4 Document");
				contentStream.newLine();
				contentStream.showText("ID    Name          Age");
				contentStream.newLine();
				contentStream.showText("-----------------------");
				contentStream.newLine();
				contentStream.showText("1     John Doe      28");
				contentStream.newLine();
				contentStream.showText("2     Jane Smith    32");
				contentStream.endText();
			}
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				document.save(outputStream);
				return outputStream.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
	public static String getWeekdayNameDE(int day)
	{
		switch (day)
		{
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
}
