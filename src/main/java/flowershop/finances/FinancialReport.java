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
	public byte[] generatePDF(){
		try (PDDocument document = new PDDocument()) {
			// Create an A4 page
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);

			// Start a content stream to write to the page
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				PDType0Font customFont = PDType0Font.load(document, new File("src/main/resources/fonts/josefin-sans.semibold.ttf"));
				contentStream.setFont(customFont, 12);

				// Write text to the page
				contentStream.beginText();
				contentStream.setLeading(14.5f);
				contentStream.newLineAtOffset(50, 750);
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

			// Write the document to a ByteArrayOutputStream
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				document.save(outputStream);
				return outputStream.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null; // Handle the error appropriately in real scenarios
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
}
