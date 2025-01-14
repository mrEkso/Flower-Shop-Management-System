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
import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.ChargeLine;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.Totalable;
import org.salespointframework.quantity.Quantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.settings.VerticalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import javax.money.MonetaryAmount;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, Double> namePriceMap = new HashMap<>();

	@ElementCollection
	private Map<Product, Quantity> productQuantityMap = new HashMap<>();

	private Category category;
	private LocalDateTime timestamp;
	private String clientName;
	private String clientPhone;
	private String adress;
	private LocalDateTime date1;
	private LocalDateTime date2;
	private String frequency;
	private LocalDate reservationExecution;
	private String notes;
	private String paymentMethod;

	@Transient
	private ProductService productService;


	public String getClientPhone(){
		return clientPhone;
	}
	public String getAdress(){
		return adress;
	}
	public LocalDateTime getDate1(){
		return date1;
	}
	public LocalDateTime getDate2(){
		return date2;
	}
	public String getFrequency(){
		return frequency;
	}

	public String getNotes() {
		return notes;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

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
		return ClockService.getTimestampStr(this.timestamp);
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

		this.timestamp = time;
		this.productService = productService;
		this.paymentMethod = order.getPaymentMethod().toString();
		if (order instanceof WholesalerOrder) {
			this.category = Category.Einkauf;
			String notes = ((WholesalerOrder) order).getNotes();
			if(notes != null) {
				this.reservationExecution = LocalDate.parse(notes);
				this.date1 = this.reservationExecution.atTime(9,0);
			}
			else{
				this.date1 = ClockService.nextWorkingDay(this.timestamp.toLocalDate()).atTime(9, 0);
			}
		} else if (order instanceof ContractOrder) {
			this.clientName = ((ContractOrder) order).getClient().getName();
			this.clientPhone = ((ContractOrder) order).getClient().getPhone();
			this.adress = ((ContractOrder) order).getAddress();
			this.category = Category.Vertraglicher_Verkauf;
			this.date1 = ((ContractOrder) order).getStartDate();
			this.date2 = ((ContractOrder) order).getEndDate();
			this.frequency = ((ContractOrder) order).getFrequency();
			this.notes = ((ContractOrder) order).getNotes();
		} else if (order instanceof EventOrder) {
			this.clientName = ((EventOrder) order).getClient().getName();
			this.clientPhone = ((EventOrder) order).getClient().getPhone();
			this.adress = ((EventOrder) order).getDeliveryAddress();
			this.category = Category.Veranstaltung_Verkauf;
			this.date1 = ((EventOrder) order).getEventDate();
			this.reservationExecution = this.date1.toLocalDate();
			this.notes = ((EventOrder) order).getNotes();
		} else if (order instanceof ReservationOrder) {
			this.clientName = ((ReservationOrder) order).getClient().getName();
			this.clientPhone = ((ReservationOrder) order).getClient().getPhone();
			this.category = Category.Reservierter_Verkauf;
			this.date1 = ((ReservationOrder) order).getReservationDateTime();
			this.reservationExecution = this.date1.toLocalDate();
			this.notes = ((ReservationOrder) order).getNotes();
		} else if (order instanceof SimpleOrder) {
			this.category = Category.Einfacher_Verkauf;
		} else {
			throw new IllegalArgumentException("Order is not recognized");
		}
		Totalable<OrderLine> kindaItemQuantityMap = order.getOrderLines();
		for (OrderLine orderLine : kindaItemQuantityMap) {
			nameQuantityMap.put(orderLine.getProductName(), orderLine.getQuantity());
			namePriceMap.put(orderLine.getProductName(), orderLine.getPrice().getNumber().doubleValue());
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
			namePriceMap.put(chargeLine.getDescription(), chargeLine.getPrice().getNumber().doubleValue());
		}
	}

	public Map<Product, Quantity> getFlowers(){
		return productQuantityMap;
	}

	public byte[] generatePDF(LocalDateTime now){
		try (PDDocument document = new PDDocument()) {
			InputStream inFont = getClass().getResourceAsStream("/fonts/josefin-sans.semibold.ttf");
			System.out.println(inFont.toString());
			PDType0Font customFont = PDType0Font.load(document, inFont);

			TableDrawer.builder()
				.startX(50)
				.endY(50)
				.startY(780)
				.table(buildTheTable(customFont, now))
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

	private Table buildTheTable(PDFont font, LocalDateTime day) {
		Table.TableBuilder builder = Table.builder()
			.addColumnsOfWidth(121, 121, 121, 122);
		Row shapka1 = Row.builder()
			.add(TextCell.builder()
				.text(" ").fontSize(16).colSpan(2)
				.build())
			.add(TextCell.builder()
				.text("Floris Blumenladen Dresden").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(shapka1);
		Row adress = Row.builder()
			.add(TextCell.builder()
				.text(" ").fontSize(16).colSpan(2)
				.build())
			.add(TextCell.builder()
				.text("Wiener Platz 4, 01069 Dresden").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(adress);
		String month = (day.getMonth().getValue() < 10) ? "0" + day.getMonth().getValue() : String.valueOf(day.getMonth().getValue());
		String dateRepr = new StringBuilder().append(day.getDayOfMonth()).append(".").append(month).append(".").append(day.getYear()).toString();

		Row datum = Row.builder()
			.add(TextCell.builder()
				.text(" ").fontSize(16).colSpan(2)
				.build())
			.add(TextCell.builder()
				.text("Am " + dateRepr).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(datum);
		builder.addRow(emptyRow(4));
		Row title = Row.builder()
			.add(TextCell.builder()
				.text("Verkaufszettel").fontSize(16).colSpan(4).horizontalAlignment(HorizontalAlignment.CENTER).font(font)
				.build())
			.build();
		Row type = Row.builder()
			.add(TextCell.builder()
				.text("Typ:").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(this.getCategory()).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		Row transactionTime = Row.builder()
			.add(TextCell.builder()
				.text("Bezahlt am:").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(this.getTimestampStr()).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(title);
		builder.addRow(type);
		builder.addRow(transactionTime);

		if (getNotes() != null){
			Row notes = Row.builder()
				.add(TextCell.builder()
					.text("Notizen:").fontSize(16).colSpan(1).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.add(TextCell.builder()
					.text(this.getNotes()).fontSize(16).colSpan(3).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.build();
			builder.addRow(notes);
		}
		builder.addRow(emptyRow(4));
		List<Row> customerDetails = getCustomerDetailsRows(font);
		for (Row customerDetail : customerDetails) {
			builder.addRow(customerDetail);
		}
		builder.addRow(emptyRow(4));
		List<Row> timeRelatedInfo = getTimeRelatedInfoRows(font);
		for (Row timeRelatedInfoRow : timeRelatedInfo) {
			builder.addRow(timeRelatedInfoRow);
		}
		builder.addRow(emptyRow(4));
		Row productListTitle = Row.builder()
			.add(TextCell.builder()
				.text("Liste von bezahlten Produkten und Leistungen").fontSize(16).colSpan(4).horizontalAlignment(HorizontalAlignment.CENTER).font(font)
				.build())
			.build();
		builder.addRow(productListTitle);
		List<Row> productList = getProductListRows(font);
		for (Row productListRow : productList) {
			builder.addRow(productListRow);
		}
		MonetaryAmount sum = this.getValue();
		if(this.category == Category.Einkauf){
			sum = sum.multiply(-1);
		}
		Row gesamtsumme = Row.builder()
			.add(TextCell.builder()
				.text("Gesamtsumme:").fontSize(20).colSpan(3).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(sum.toString()).fontSize(20).colSpan(1).horizontalAlignment(HorizontalAlignment.RIGHT).font(font)
				.build())
			.build();
		builder.addRow(gesamtsumme);
		Row zahlungsart = Row.builder()
			.add(TextCell.builder()
				.text("Zahlungsart: "+ getPaymentMethod()).fontSize(16).colSpan(4).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		builder.addRow(zahlungsart);

		return builder.build();
	}

	private List<Row> getProductListRows(PDFont font) {
		Row head = Row.builder()
			.add(TextCell.builder()
				.text("Name").fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font).borderWidth(1)
				.build())
			.add(TextCell.builder()
				.text("Preis pro Stück").fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font).borderWidth(1)
				.build())
			.add(TextCell.builder()
				.text("Anzahl").fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font).borderWidth(1)
				.build())
			.add(TextCell.builder()
				.text("Summe").fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font).borderWidth(1)
				.build())
			.build();
		List<Row> productListRows = new ArrayList<>();
		productListRows.add(head);
		for (Map.Entry<String,Quantity> entry: this.nameQuantityMap.entrySet()){
			Row row = Row.builder()
				.add(TextCell.builder()
					.text(entry.getKey()).fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.add(TextCell.builder()
					.text(String.valueOf(Math.round(this.namePriceMap.get(entry.getKey())*100 / entry.getValue().getAmount().doubleValue())/100.0)).fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.add(TextCell.builder()
					.text(String.valueOf(entry.getValue().getAmount().intValue())).fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.add(TextCell.builder()
					.text(String.valueOf(this.namePriceMap.get(entry.getKey()))).fontSize(16).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.build();
			productListRows.add(row);
		}
		return productListRows;
	}

	private List<Row> getTimeRelatedInfoRows(PDFont font) {
		List<Row> timeRelatedInfoRows = new ArrayList<>();
		String formulation;
		switch (this.category) {
			case Einkauf -> formulation = "Zustellzeit:";
			case Reservierter_Verkauf -> formulation = "Abholszeit:";
			case Veranstaltung_Verkauf -> formulation = "Ereignisszeit:";
			case Vertraglicher_Verkauf -> formulation = "Anfangszeit:";
			default -> {
				return timeRelatedInfoRows;
			}
		}
		Row date1Row = Row.builder()
			.add(TextCell.builder()
				.text(formulation).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(ClockService.getTimestampStr(this.getDate1())).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		timeRelatedInfoRows.add(date1Row);
		if(this.category != Category.Vertraglicher_Verkauf){
			return timeRelatedInfoRows;
		}
		Row date2Row = Row.builder()
			.add(TextCell.builder()
				.text("Ablaufszeit:").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(ClockService.getTimestampStr(this.getDate2())).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		timeRelatedInfoRows.add(date2Row);
		Row freq = Row.builder()
			.add(TextCell.builder()
				.text("Häufigkeit:").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(this.getFrequency()).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		timeRelatedInfoRows.add(freq);
		return timeRelatedInfoRows;
	}

	private List<Row> getCustomerDetailsRows(PDFont font) {
		List<Row> customerDetails = new ArrayList<>();
		if(this.category == Category.Einfacher_Verkauf || this.category == Category.Einkauf){
			return customerDetails;
		}
		Row name = Row.builder()
			.add(TextCell.builder()
				.text("Kunde:").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(this.getClientName()).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		customerDetails.add(name);
		Row telephone = Row.builder()
			.add(TextCell.builder()
				.text("Telephonnummer:").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.add(TextCell.builder()
				.text(this.getClientPhone()).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
				.build())
			.build();
		customerDetails.add(telephone);
		if(this.getAdress() != null && !this.getAdress().equals("")){
			Row adress = Row.builder()
				.add(TextCell.builder()
					.text("Zustelladress:").fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.add(TextCell.builder()
					.text(this.getAdress()).fontSize(16).colSpan(2).horizontalAlignment(HorizontalAlignment.LEFT).font(font)
					.build())
				.build();
			customerDetails.add(adress);
		}
		return customerDetails;
	}

}
