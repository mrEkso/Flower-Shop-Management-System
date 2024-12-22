package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.inventory.DeletedProduct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Quantity;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FinancialReportTests {

	private DailyFinancialReport report;
	private ClockService mockClockService;

	@BeforeEach
	public void setUp() {
		report = mock(DailyFinancialReport.class);
		mockClockService = mock(ClockService.class);
		report.deletedProducts = new ArrayList<>();
		DeletedProduct del = mock(DeletedProduct.class);
		when(del.getName()).thenReturn("Hello");
		when(del.getPricePerUnit()).thenReturn(Money.of(20, "EUR"));
		when(del.getQuantityDeleted()).thenReturn(3);
		when(del.getTotalLoss()).thenReturn(Money.of(60, "EUR"));
		report.deletedProducts.add(del);
	}

	@Test
	public void testGetWeekdayNameDE() {
		assertEquals("Montag", FinancialReport.getWeekdayNameDE(1));
		assertEquals("Dienstag", FinancialReport.getWeekdayNameDE(2));
		assertEquals("Mittwoch", FinancialReport.getWeekdayNameDE(3));
		assertEquals("Donnerstag", FinancialReport.getWeekdayNameDE(4));
		assertEquals("Freitag", FinancialReport.getWeekdayNameDE(5));
		assertEquals("Samstag", FinancialReport.getWeekdayNameDE(6));
		assertEquals("Sonntag", FinancialReport.getWeekdayNameDE(7));

		assertEquals("", FinancialReport.getWeekdayNameDE(-1));
		assertEquals("", FinancialReport.getWeekdayNameDE(100));
	}
	/*
	@Test
	public void testGeneratePDF() {

		// Call the generatePDF method
		byte[] pdfBytes = report.generatePDF();

		// Check that the PDF bytes are not null
		assertNotNull(pdfBytes, "Generated PDF byte array should not be null");

		// Ensure the PDF byte array is not empty
		assertTrue(pdfBytes.length > 0, "Generated PDF byte array should not be empty");

		// Check that the method does not throw exceptions
		assertDoesNotThrow(() -> report.generatePDF(), "generatePDF() should not throw an exception");
	}

	 */

	/*
	@Test
	public void testBuildTheTable() {
		// Mock the current date
		LocalDateTime mockDate = LocalDateTime.of(2024, 12, 21, 0, 0);
		PDType1Font mockFont = new PDType1Font(HELVETICA);
		when(mockClockService.now()).thenReturn(mockDate);
		when(report.getNeededRows(mockFont)).thenReturn(new ArrayList<>());
		// Mock font (assuming it's already loaded in your context)

		// Call the method
		Table table = report.buildTheTable(mockFont);

		// Validate the table structure
		assertNotNull(table, "Table should not be null");
		assertEquals(6, table.getColumns().size(), "Table should have 6 columns");
		assertTrue(table.getRows().size() > 5, "Table should have more than 5 rows");

		// Validate specific rows
		Row titleRow = table.getRows().get(4); // Adjust index based on your logic
		assertNotNull(titleRow, "Title row should not be null");

		assertEquals("Finanzübersicht für " + report.intervalToString(),
			titleRow.getCells().get(0).,
			"Title row text should match");

		// Validate date row
		Row dateRow = table.getRows().get(2); // Adjust index based on your logic
		assertNotNull(dateRow, "Date row should not be null");
	}
	*/

	@Test
	public void testGetDeletedProductRows() {
		// Mock font (replace with actual font loading or a mock if needed)


		List<Row> rows = report.getDeletedProductRows(new PDType1Font(HELVETICA));

		// Validate the number of rows
		assertNotNull(rows, "Rows should not be null");
		assertEquals(0, rows.size(), "There should be exactly 4 rows (title, header, 1 product, total losses)");

	}
}



