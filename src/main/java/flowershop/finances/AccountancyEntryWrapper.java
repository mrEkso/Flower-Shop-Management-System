package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.sales.SimpleOrder;
import flowershop.sales.WholesalerOrder;
import flowershop.services.ContractOrder;
import flowershop.services.EventOrder;
import flowershop.services.ReservationOrder;
import jakarta.persistence.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.ChargeLine;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.Totalable;
import org.salespointframework.quantity.Quantity;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static flowershop.finances.FinancialReport.emptyRow;


/**
 * This class is used to adapt Order to AccountancyEntry to then be used in CashRegisterService (Accountancy child class)
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AccountancyEntryWrapper extends AccountancyEntry {
	// For every entry:
	//  	* timestamp (parent class has a very stupid way of setting it. Or maybe I'm too stupid for it)
	//      - income / spending (parent class)
	//      * category
	//      * items (as separate rows)
	//      * quantity (as separate rows)
	//      - Amount (parent class)


	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, Quantity> nameQuantityMap = new HashMap<String, Quantity>();

	@ElementCollection
	private Map<Product, Quantity> productQuantityMap = new HashMap<>();

	private Category category;
	private LocalDateTime timestamp;
	private String clientName;
	private LocalDate reservationExecution;

	@Transient
	private ProductService productService;
	@Transient
	private ClockService clockService;


	/**
	 *
	 * @param category
	 * @return the names of categories of orders in German
	 */
	public static String categoryToString(Category category) {
		return category.toString().replace('_', ' ');
	}

	public String getCategory() {
		return this.categoryToString(this.category);
	}

	public String getClientName() {
		if(this.clientName == null) {
			return "";
		}
		return this.clientName;
	}

	public String getTimestampStr()
	{
		StringBuilder str = new StringBuilder(this.timestamp.getDayOfMonth() + ".");
		str.append(this.timestamp.getMonthValue() + ".")
			.append(this.timestamp.getYear() + " ")
			.append(this.timestamp.getHour() + ":");
		if(this.timestamp.getMinute() < 10)
		{
			str.append("0");
		}
		str.append(this.timestamp.getMinute());
		return str.toString();
	}

	/**
	 * USE THIS METHOD INSTEAD OF getDate()!
	 * @return the time when the order was paid.
	 */
	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public LocalDate getDeliveryDate() {
		return reservationExecution;
	}

	/**
	 *
	 * @return the map where keys are name of the products, and values - their quantity
	 */
	public Map<String, Quantity> getItems() {
		/*
		return productQuantityMap.entrySet().stream()
			.collect(Collectors.toMap(
				entry -> entry.getKey().getName(),
				Map.Entry::getValue
			));

		 */
		return nameQuantityMap;
	}

	protected AccountancyEntryWrapper() {
	}

	public AccountancyEntryWrapper(Order order, LocalDateTime time, ProductService productService) {
		super(order.getTotal());
		this.productService = productService;
		this.timestamp = time;
		if (order instanceof WholesalerOrder) {
			this.category = Category.Einkauf;
			String notes = ((WholesalerOrder) order).getNotes();
			if(notes != null) {
				this.reservationExecution = LocalDate.parse(notes);
			}
		} else if (order instanceof ContractOrder) {
			this.clientName = ((ContractOrder) order).getClient().getName();
			this.category = Category.Vertraglicher_Verkauf;
		} else if (order instanceof EventOrder) {
			this.clientName = ((EventOrder) order).getClient().getName();
			this.category = Category.Veranstaltung_Verkauf;
			this.reservationExecution = ((EventOrder) order).getEventDate().toLocalDate();
		} else if (order instanceof ReservationOrder) {
			this.clientName = ((ReservationOrder) order).getClient().getName();
			this.category = Category.Reservierter_Verkauf;
			this.reservationExecution = ((ReservationOrder) order).getReservationDateTime().toLocalDate();
		} else if (order instanceof SimpleOrder) {
			this.category = Category.Einfacher_Verkauf;
		} else {
			throw new IllegalArgumentException("Order is not recognized");
		}
		Totalable<OrderLine> kindaItemQuantityMap = order.getOrderLines();
		for (OrderLine orderLine : kindaItemQuantityMap) {
			nameQuantityMap.put(orderLine.getProductName(), orderLine.getQuantity());

			if(order instanceof WholesalerOrder || order instanceof EventOrder || order instanceof ReservationOrder) {
				String name = orderLine.getProductName();
				List<Flower> lst = productService.findFlowersByName(name);
				if(!lst.isEmpty()) {
					if(lst.getFirst().getName().equals(name)) {
						productQuantityMap.merge(lst.getFirst(), orderLine.getQuantity(), Quantity::add);
					}
					continue;
				}
				if (order instanceof EventOrder || order instanceof ReservationOrder) {
					List<Bouquet> bouquetList = productService.findBouquetsByName(name);
					if(!bouquetList.isEmpty()) {
						if(bouquetList.getFirst().getName().equals(name)) {
							Bouquet bouquet = bouquetList.getFirst();
							for (Map.Entry<Flower, Integer> eventBouquettePair : bouquet.getFlowers().entrySet()) {
								productQuantityMap.merge(eventBouquettePair.getKey(),
									Quantity.of(eventBouquettePair.getValue())
										.times(orderLine.getQuantity().getAmount().intValue()),
									Quantity::add);
							}
						}
					}
				}
			}
		}
		Totalable<ChargeLine> extraFees = order.getAllChargeLines();
		for (ChargeLine chargeLine : extraFees) {
			nameQuantityMap.put(chargeLine.getDescription(), Quantity.of(1));
		}
	}

	public Map<Product, Quantity> getFlowers(){
		return productQuantityMap;
	}

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

	private Table buildTheTable(PDFont font) {
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
		LocalDateTime day = this.clockService.now();
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
	}


}
