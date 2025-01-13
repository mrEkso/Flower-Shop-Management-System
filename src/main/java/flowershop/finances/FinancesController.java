package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.inventory.DeletedProduct;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
//@RequestMapping("/finances")
public class FinancesController {

	private final int maxEntriesShown = 100;
	private final CashRegisterService cashRegisterService;
	private final ClockService clockService;
	private List<AccountancyEntryWrapper> filteredOrdersList = new ArrayList<>();
	private List<AccountancyEntryWrapper> filteredAndCutOrdersList;
	private HashSet<AccountancyEntryWrapper> filteredByDates = new HashSet<>();
	private HashSet<AccountancyEntryWrapper> filteredByCategory = new HashSet<>();
	private HashSet<AccountancyEntryWrapper> filteredByCustomerName = new HashSet<>();
	private HashSet<AccountancyEntryWrapper> filteredBySum = new HashSet<>();
	private LocalDate date1;
	private LocalDate date2;
	private boolean isFilteredByDates;
	private boolean isFilteredByCategory;
	private boolean isFilteredByCustomerName;
	private boolean isFilteredBySum;
	private String category;
	private String name = "";
	private String sum = "";
	private List<DeletedProduct> deletedProducts = new ArrayList<>();

	public FinancesController(CashRegisterService cashRegisterService, ClockService clockService) {
		this.cashRegisterService = cashRegisterService;
		this.clockService = clockService;
	}

	/**
	 * Will filter the entries shown to only those, which were registered inside of that interval
	 *
	 * @param date1 start date
	 * @param date2 end date
	 * @param model
	 * @return "finances"
	 */
	@GetMapping("/filterDates")
	@PreAuthorize("hasRole('BOSS')")
	public String filterDates(@RequestParam("date1") LocalDate date1, @RequestParam("date2") LocalDate date2, Model model) {
		if (date1.isAfter(date2)) {
			return "finances";
		}
		this.date1 = date1;
		this.date2 = date2;
		Interval interval = Interval.from(LocalDateTime.of(
			date1.getYear(),
			date1.getMonth(),
			date1.getDayOfMonth(), 0, 0)
		).to(date2.plusDays(1).atStartOfDay());
			/*
			.to(LocalDateTime.of(
			date2.getYear(),
			date2.getMonth(),
			date2.getDayOfMonth()+, 0, 0
			 */
		HashSet<AccountancyEntryWrapper> filteredList = new HashSet<>();
		for (AccountancyEntry i : cashRegisterService.find(interval).toList()) {
			filteredList.add((AccountancyEntryWrapper) i);
		}
		this.filteredByDates = filteredList;
		this.isFilteredByDates = true;
		setFilteredOrdersList(this.maxEntriesShown);
		List<DeletedProduct> tempList = new ArrayList<>(cashRegisterService.getAllDeletedProducts(date1,date2));
		tempList.sort(Comparator.comparing(DeletedProduct::getDateWhenDeleted).reversed());
		this.deletedProducts = tempList;
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}


	/**
	 * Drops the date filter and adds all the other entries back to the table
	 *
	 * @param model
	 * @return
	 */
	@GetMapping("/resetDates")
	@PreAuthorize("hasRole('BOSS')")
	public String resetDates(Model model) {
		this.filteredByDates = new HashSet<>();
		this.isFilteredByDates = false;
		this.date1 = LocalDate.of(1970, 1, 1);
		this.date2 = LocalDate.now();
		getTransactionPage(model);
		List<DeletedProduct> tempList = new ArrayList<>(cashRegisterService.getAllDeletedProducts());
		tempList.sort(Comparator.comparing(DeletedProduct::getDateWhenDeleted).reversed());
		this.deletedProducts = tempList;
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	/**
	 * Drops the category filter and returns all entries back to the table
	 *
	 * @param model
	 * @return
	 */
	@GetMapping("/resetCategory")
	@PreAuthorize("hasRole('BOSS')")
	public String resetCategory(Model model) {
		this.filteredByCategory = new HashSet<>();
		this.isFilteredByCategory = false;
		this.category = "all";
		getTransactionPage(model);
		/*
		else{
			getTransactionPage(model);
		}

		 */
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	/**
	 * Connects needed data to HTML
	 *
	 * @param model
	 * @param transactions
	 */
	private void prepareFinancesModel(Model model, List<AccountancyEntryWrapper> transactions) {
		model.addAttribute("transactions", transactions);
		model.addAttribute("currentBalance", cashRegisterService.getBalance());
		model.addAttribute("date1", date1);
		model.addAttribute("date2", date2);
		model.addAttribute("category", category);
		model.addAttribute("todayDate", clockService.getCurrentDate());
		model.addAttribute("shopOpened", clockService.isOpen());
		model.addAttribute("deletedProducts", this.deletedProducts);
		model.addAttribute("deletedProductsNotEmpty", !this.deletedProducts.isEmpty());
		LocalDateTime startOfDay = clockService.getCurrentDate().atTime(9, 0, 0);
		LocalDateTime endOfInterval = startOfDay.plusDays(1);
		model.addAttribute("dayProfit", cashRegisterService.salesVolume(Interval.from(startOfDay).to(endOfInterval), Duration.ofDays(1)).get(Interval.from(startOfDay).to(endOfInterval)));
		model.addAttribute("transactionsNotEmpty", !transactions.isEmpty());
		model.addAttribute("customerName", this.name);
		model.addAttribute("transactionValue", this.sum);

	}

	/**
	 * Will sort this list and show a cut version of it in the table
	 *
	 * @param size               max number of entries which are shown in the table at a time
	 */
	private void setFilteredOrdersList(int size) {
		List<AccountancyEntryWrapper> tempList = new ArrayList<>(findFilteredEntries());
		Collections.sort(tempList, new Comparator<AccountancyEntry>() {
			@Override
			public int compare(AccountancyEntry first, AccountancyEntry second) {
				LocalDateTime ldt1 = ((AccountancyEntryWrapper) first).getTimestamp();
				LocalDateTime ldt2 = ((AccountancyEntryWrapper) second).getTimestamp();
				if (ldt1 != null && ldt2 != null) {
					return ldt2.compareTo(ldt1);
				} else {
					throw new IllegalStateException("Some entries dont have date assigned");
				}
			}
		});
		this.filteredOrdersList = tempList;
		limitListSize(size);
	}

	private Set<AccountancyEntryWrapper> findFilteredEntries(){
		final List<Set<AccountancyEntryWrapper>> filterSets = List.of(
			filteredByDates,
			filteredByCategory,
			filteredByCustomerName,
			filteredBySum
		);
		final List<Boolean> filterSettings = List.of(
			isFilteredByDates,
			isFilteredByCategory,
			isFilteredByCustomerName,
			isFilteredBySum
		);
		Set<AccountancyEntryWrapper> filteredSet = new HashSet<>();
		boolean firstActiveFilterFound = false;
		int i = 0;
		while (i < filterSets.size()) {
			if(filterSettings.get(i) && !firstActiveFilterFound) {
				firstActiveFilterFound = true;
				filteredSet = filterSets.get(i);
			}
			else if(filterSettings.get(i)){
				filteredSet = intersection(filteredSet, filterSets.get(i));
			}
			i+=1;
		}
		if(!firstActiveFilterFound){
			for (AccountancyEntry j : this.cashRegisterService.findAll().toList()) {
				filteredSet.add((AccountancyEntryWrapper) j);
			}
		}
		return filteredSet;
	}

	/**
	 * @param model
	 * @return The main finances page
	 */
	@GetMapping("/finances")
	@PreAuthorize("hasRole('BOSS')")
	public String getTransactionPage(Model model) {
		this.filteredOrdersList = new LinkedList<>();
		for (AccountancyEntry i : this.cashRegisterService.findAll().toList()) {
			this.filteredOrdersList.add((AccountancyEntryWrapper) i);
		}
		List<DeletedProduct> tempList = new ArrayList<>(cashRegisterService.getAllDeletedProducts());
		tempList.sort(Comparator.comparing(DeletedProduct::getDateWhenDeleted).reversed());
		this.deletedProducts = tempList;

		setFilteredOrdersList(this.maxEntriesShown);
		model.addAttribute("transactions", filteredAndCutOrdersList);
		model.addAttribute("currentBalance", cashRegisterService.getBalance());
		model.addAttribute("todayDate", clockService.getCurrentDate());
		System.out.println(clockService.isOpen());
		model.addAttribute("shopOpened", clockService.isOpen());
		LocalDateTime startOfDay = clockService.getCurrentDate().atTime(9, 0, 0);
		LocalDateTime endOfInterval = startOfDay.plusDays(1);
		model.addAttribute("dayProfit", cashRegisterService.salesVolume(Interval.from(startOfDay).to(endOfInterval), Duration.ofDays(1)).get(Interval.from(startOfDay).to(endOfInterval)));
		model.addAttribute("deletedProducts", this.deletedProducts);
		model.addAttribute("deletedProductsNotEmpty", !this.deletedProducts.isEmpty());
		model.addAttribute("transactionsNotEmpty", !filteredOrdersList.isEmpty());
		model.addAttribute("customerName", this.name);
		model.addAttribute("transactionValue", this.sum);
		return "finances";
	}


	/**
	 * Will open the page, where the day for the report will be asked
	 *
	 * @param model
	 * @return
	 */
	@GetMapping("/askForDay")
	@PreAuthorize("hasRole('BOSS')")
	public String askDay(Model model) {
		return "finance/askForDay";
	}

	/**
	 * Will open the page, where the month for the report will be asked
	 *
	 * @param model
	 * @return
	 */
	@GetMapping("/askForMonth")
	@PreAuthorize("hasRole('BOSS')")
	public String askMonth(Model model) {
		return "finance/askForMonth";
	}

	/**
	 * Uploads a generated day-report
	 *
	 * @param date
	 * @param model
	 * @return PDF-File
	 */
	@GetMapping("/dayReport")
	@PreAuthorize("hasRole('BOSS')")
	public ResponseEntity<byte[]> dayReport(@RequestParam("day") String date, Model model) {
		String[] dateArray = date.split("-");
		if (dateArray.length != 3) {
			return ResponseEntity.badRequest()
				.body("Please just use the widget. Don't Write text there. But if you do, use format YYYY-MM-DD".getBytes(StandardCharsets.UTF_8));
		}
		//if (dateArray[0].length() != 4 || !dateArray[0].matches("19[0-9][0-9]|2[0-9][0-9][0-9]"))
		LocalDate actualDate;
		try{
			actualDate = LocalDate.parse(date);
		} catch (DateTimeParseException e){
			return ResponseEntity.badRequest()
				.body("Please just use the widget. Don't Write text there. But if you do, use format YYYY-MM-DD".getBytes(StandardCharsets.UTF_8));
		}
		if (actualDate.isAfter(clockService.getCurrentDate())) {
			return ResponseEntity.badRequest()
				.body("The given date cannot be in the future.".getBytes(StandardCharsets.UTF_8));
		}

		DailyFinancialReport report = cashRegisterService.createFinancialReportDay(actualDate.atStartOfDay());
		if (report == null) {
			return ResponseEntity.badRequest()
				.body("No Transactions saved in the system.".getBytes(StandardCharsets.UTF_8));

		}
		if (report.isBeforeBeginning()) {
			return ResponseEntity.badRequest()
				.body("The given date is before the accounting process started. No Data.".getBytes(StandardCharsets.UTF_8));
		}


		byte[] docu = report.generatePDF();
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_day.pdf")
			.contentType(MediaType.APPLICATION_PDF)
			.body(docu);
	}

	/**
	 * Uploads a generated month-report
	 *
	 * @param month number of the needed month (1-12)
	 * @param year the needed year
	 * @param model
	 * @return PDF-File
	 */
	@GetMapping("/monthReport")
	@PreAuthorize("hasRole('BOSS')")
	public ResponseEntity<byte[]> monthReport(@RequestParam("month") int month,
											  @RequestParam("year") int year,
											  Model model) {
		/*
		String[] date = year_month.split("-");
		if (date.length != 2) {
			return ResponseEntity.badRequest()
				.body("Please just use the widget. Don't Write text there. But if you do, use format YYYY-MM".getBytes(StandardCharsets.UTF_8));
		}
		if (date[0].length() != 4 ||
			!date[1].matches("0[1-9]|1[1-2]") || !date[0].matches("19[0-9][0-9]|2[0-9][0-9][0-9]")) {
			return ResponseEntity.badRequest()
				.body("Please just use the widget. Don't Write text there. But if you do, use format YYYY-MM".getBytes(StandardCharsets.UTF_8));
		}
		*/
		if(month < 1 || month > 12) {
			return ResponseEntity.badRequest()
				.body("No such month exists, dummy ;)".getBytes(StandardCharsets.UTF_8));
		}

		YearMonth monthParsed = YearMonth.of(year, month);
		LocalDate firstOfMonth = monthParsed.atDay(1);
		if (firstOfMonth.isAfter(clockService.getCurrentDate())) {
			return ResponseEntity.badRequest()
				.body("The given date cannot be in the future.".getBytes(StandardCharsets.UTF_8));
		}
		MonthlyFinancialReport report = cashRegisterService.createFinancialReportMonth(firstOfMonth.atStartOfDay());
		if (report == null) {
			return ResponseEntity.badRequest()
				.body("No Transactions saved in the system.".getBytes(StandardCharsets.UTF_8));

		}
		if (report.isBeforeBeginning()) {
			return ResponseEntity.badRequest()
				.body("The given month is before the accounting process started. No Data.".getBytes(StandardCharsets.UTF_8));
		}
		byte[] docu = report.generatePDF();
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_month.pdf")
			.contentType(MediaType.APPLICATION_PDF)
			.body(docu);
	}

	@PostMapping("/toggleState")
	@PreAuthorize("hasRole('BOSS')")
	public String toggleState(Model model) {
		clockService.openOrClose();
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	/**
	 * @param category chosen category
	 * @param model
	 * @return The page, where only chosen category of orders is shown
	 */
	@GetMapping("/filterCategories")
	@PreAuthorize("hasRole('BOSS')")
	public String filterCategories(@RequestParam("filter") String category, Model model) {
		//this.categorySet = category;
		this.category = category;
		if (category.equals("all")) {
			return this.resetCategory(model);
		} else {
			this.isFilteredByCategory = true;
			List<AccountancyEntry> lst;
			if (category.equals("income")) {
				lst = this.cashRegisterService.filterIncomeOrSpending(true);
			} else if (category.equals("spendings")) {
				lst = this.cashRegisterService.filterIncomeOrSpending(false);
			} else if (category.equals("simple order")) {
				lst = this.cashRegisterService.filterEntries(Category.Einfacher_Verkauf).stream().toList();
			} else if (category.equals("reserved order")) {
				lst = this.cashRegisterService.filterEntries(Category.Reservierter_Verkauf).stream().toList();
			} else if (category.equals("event order")) {
				lst = this.cashRegisterService.filterEntries(Category.Veranstaltung_Verkauf).stream().toList();
			} else if (category.equals("contract order")) {
				lst = this.cashRegisterService.filterEntries(Category.Vertraglicher_Verkauf).stream().toList();
			} else {
				lst = this.cashRegisterService.filterEntries(Category.Einkauf).stream().toList();
			}
			this.filteredByCategory = new HashSet<>();
			for (AccountancyEntry i : lst) {
				this.filteredByCategory.add((AccountancyEntryWrapper) i);
			}
			setFilteredOrdersList(this.maxEntriesShown);
		}
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	@GetMapping("/filterCustomerName")
	@PreAuthorize("hasRole('BOSS')")
	public String filterCustomerName(Model model, @RequestParam("customerName") String customerName) {
		this.name = customerName;
		List<AccountancyEntryWrapper> lst = this.cashRegisterService.filterByCustomer(customerName);
		this.filteredByCustomerName = new HashSet<>();
		for (AccountancyEntry i : lst) {
			this.filteredByCustomerName.add((AccountancyEntryWrapper) i);
		}
		this.isFilteredByCustomerName = true;
		setFilteredOrdersList(this.maxEntriesShown);
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	@GetMapping("/filterTransactionValue")
	@PreAuthorize("hasRole('BOSS')")
	public String filterPrice(Model model, @RequestParam("transactionValue") double price) {
		this.sum = String.valueOf(price);
		List<AccountancyEntryWrapper> lst = this.cashRegisterService.filterByPrice(price);
		this.filteredBySum = new HashSet<>();
		for (AccountancyEntry i : lst) {
			this.filteredBySum.add((AccountancyEntryWrapper) i);
		}
		this.isFilteredBySum = true;
		setFilteredOrdersList(this.maxEntriesShown);
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	@GetMapping("/resetCustomerName")
	@PreAuthorize("hasRole('BOSS')")
	public String resetCustomerName(Model model) {
		this.name = "";
		this.isFilteredByCustomerName = false;
		this.filteredByCustomerName.clear();
		setFilteredOrdersList(this.maxEntriesShown);
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	@GetMapping("/resetTransactionValue")
	@PreAuthorize("hasRole('BOSS')")
	public String resetTransactionValue(Model model) {
		this.sum = "";
		this.isFilteredBySum = false;
		this.filteredBySum.clear();
		setFilteredOrdersList(this.maxEntriesShown);
		prepareFinancesModel(model, filteredAndCutOrdersList);
		return "finances";
	}

	@GetMapping("/getReceipt")
	@PreAuthorize("hasRole('BOSS')")
	public ResponseEntity<byte[]> getReceipt(Model model, @RequestParam Long orderId) {
		byte[] docu = this.cashRegisterService.getEntry(orderId,this.filteredAndCutOrdersList).generatePDF();
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt.pdf")
			.contentType(MediaType.APPLICATION_PDF)
			.body(docu);

	}


	/**
	 * @param size maximal number of entries to be shown in the table
	 */
	private void limitListSize(int size) {
		if (this.filteredOrdersList.size() > size) {
			this.filteredAndCutOrdersList = this.filteredOrdersList.subList(0, size);
		} else {
			this.filteredAndCutOrdersList = this.filteredOrdersList;
		}

	}

	/**
	 * @param set1
	 * @param set2
	 * @return the intersection of these two sets (in mathematical terms)
	 */
	private Set<AccountancyEntryWrapper> intersection(Set<AccountancyEntryWrapper> set1, Set<AccountancyEntryWrapper> set2) {
		Set<AccountancyEntryWrapper> intersection = new HashSet<>(set1);
		intersection.retainAll(set2);
		return intersection;
	}

}


