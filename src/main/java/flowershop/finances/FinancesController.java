package flowershop.finances;

import flowershop.clock.ClockService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Controller
//@RequestMapping("/finances")
public class FinancesController {


	private final CashRegisterService cashRegisterService;
	private final ClockService clockService;
	private List<AccountancyEntryWrapper> filteredOrdersList = new ArrayList<>();
	private List<AccountancyEntryWrapper> filteredAndCutOrdersList;
	private HashSet<AccountancyEntryWrapper> filteredByDates = new HashSet<>();
	private HashSet<AccountancyEntryWrapper> filteredByCategory = new HashSet<>();
	private LocalDate date1;
	private LocalDate date2;
	private boolean isFilteredByDates;
	private boolean isFilteredByCategory;
	private String category;

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
		if(date1.isAfter(date2)) {
			return "finances";
		}
		this.date1 = date1;
		this.date2 = date2;
		Interval interval = Interval.from(LocalDateTime.of(
			date1.getYear(),
			date1.getMonth(),
			date1.getDayOfMonth(),0,0)
			).to(LocalDateTime.of(
				date2.getYear(),
				date2.getMonth(),
				date2.getDayOfMonth(),0,0
		));
		HashSet<AccountancyEntryWrapper> filteredList = new HashSet<>();
		for(AccountancyEntry i: cashRegisterService.find(interval).toList()){
			filteredList.add((AccountancyEntryWrapper)i);
		}
		this.filteredByDates = filteredList;
		if(!this.filteredByCategory.isEmpty()) {
			setFilteredOrdersList(intersection(filteredList,this.filteredByCategory).stream().toList(),100);
		}
		else {
			setFilteredOrdersList(filteredList.stream().toList(), 100);
		}
		this.isFilteredByDates=true;
		prepareFinancesModel(model,filteredAndCutOrdersList);
		return "finances";
	}


	/**
	 * Drops the date filter and adds all the other entries back to the table
	 * @param model
	 * @return
	 */
	@GetMapping("/resetDates")
	@PreAuthorize("hasRole('BOSS')")
	public String resetDates(Model model) {
		this.filteredByDates = new HashSet<>();
		this.isFilteredByDates = false;
		this.date1=LocalDate.of(1970,1,1);
		this.date2=LocalDate.now();
		getTransactionPage(model);
		if(this.isFilteredByCategory) {
			setFilteredOrdersList(intersection(new HashSet<>(this.filteredOrdersList), this.filteredByCategory).stream().toList(),100);
		}
		prepareFinancesModel(model,filteredAndCutOrdersList);
		return "finances";
	}

	/**
	 * Drops the category filter and returns all entries back to the table
	 * @param model
	 * @return
	 */
	@GetMapping("/resetCategory")
	@PreAuthorize("hasRole('BOSS')")
	public String resetCategory(Model model) {
		this.filteredByCategory = new HashSet<>();
		this.isFilteredByCategory = false;
		this.category="all";
		getTransactionPage(model);
		if(this.isFilteredByDates) {
			setFilteredOrdersList(intersection(new HashSet<>(this.filteredOrdersList), this.filteredByDates).stream().toList(),100);
		}
		/*
		else{
			getTransactionPage(model);
		}

		 */
		prepareFinancesModel(model,filteredAndCutOrdersList);
		return "finances";
	}

	/**
	 * Connects needed data to HTML
	 * @param model
	 * @param transactions
	 */
	private void prepareFinancesModel(Model model, List<AccountancyEntryWrapper> transactions) {
		model.addAttribute("transactions", transactions);
		model.addAttribute("currentBalance", cashRegisterService.getBalance());
		model.addAttribute("date1", date1);
		model.addAttribute("date2", date2);
		model.addAttribute("category", category);
		model.addAttribute("todayDate",clockService.getCurrentDate());
		System.out.println(clockService.isOpen());
		model.addAttribute("shopOpened", clockService.isOpen());
	}

	/**
	 * Will sort this list and show a cut version of it in the table
	 * @param filteredOrdersList name speaks for itself
	 * @param size max number of entries which are shown in the table at a time
	 */
	private void setFilteredOrdersList(List<AccountancyEntryWrapper> filteredOrdersList, int size) {
		List<AccountancyEntryWrapper> tempList = new ArrayList<>(filteredOrdersList);
		Collections.sort(tempList, new Comparator<AccountancyEntry>() {
			@Override
			public int compare(AccountancyEntry first, AccountancyEntry second) {
				LocalDateTime ldt1 = ((AccountancyEntryWrapper)first).getTimestamp();
				LocalDateTime ldt2 = ((AccountancyEntryWrapper)second).getTimestamp();
				if (ldt1 != null && ldt2 != null) {
					return ldt2.compareTo(ldt1);
				}
				else{
					throw new IllegalStateException("Some entries dont have date assigned");
				}
			}
		});
		this.filteredOrdersList = tempList;
		limitListSize(size);
	}

	/**
	 *
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

		setFilteredOrdersList(filteredOrdersList, 100);
		model.addAttribute("transactions", filteredAndCutOrdersList);
		model.addAttribute("currentBalance", cashRegisterService.getBalance());
		model.addAttribute("todayDate",clockService.getCurrentDate());
		System.out.println(clockService.isOpen());
		model.addAttribute("shopOpened", clockService.isOpen());
		return "finances";
	}


	/**
	 * Will open the page, where the day for the report will be asked
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
	 * @param date
	 * @param model
	 * @return PDF-File
	 */
	@GetMapping("/dayReport")
	@PreAuthorize("hasRole('BOSS')")
	public ResponseEntity<byte[]> dayReport(@RequestParam("day") LocalDate date, Model model) {
		if(date.isAfter(LocalDate.now())){
			return ResponseEntity.badRequest()
				.body("The given date cannot be in the future.".getBytes(StandardCharsets.UTF_8));
		}
		DailyFinancialReport report = cashRegisterService.createFinancialReportDay(date.atStartOfDay());
		if(report == null)
		{
			return ResponseEntity.badRequest()
				.body("No Transactions saved in the system.".getBytes(StandardCharsets.UTF_8));

		}
		if(report.isBeforeBeginning())
		{
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
	 * @param year_month String of the form YYYY-MM.
	 * @param model
	 * @return PDF-File
	 */
	@GetMapping("/monthReport")
	@PreAuthorize("hasRole('BOSS')")
	public ResponseEntity<byte[]> monthReport(@RequestParam("month") String year_month, Model model) {
		String[] date = year_month.split("-");
		if(date.length != 2) {
			return ResponseEntity.badRequest()
				.body("Please just use the widget. Don't Write text there. But if you do, use format YYYY-MM".getBytes(StandardCharsets.UTF_8));
		}
		if(date[0].length() != 4 ||
			!date[1].matches("0[1-9]|1[1-2]") || !date[0].matches("19[0-9][0-9]|2[0-9][0-9][0-9]"))
		{
			return ResponseEntity.badRequest()
				.body("Please just use the widget. Don't Write text there. But if you do, use format YYYY-MM".getBytes(StandardCharsets.UTF_8));
		}
		YearMonth monthParsed = YearMonth.parse(year_month);
		LocalDate firstOfMonth = monthParsed.atDay(1);
		if (firstOfMonth.isAfter(LocalDate.now())) {
			return ResponseEntity.badRequest()
				.body("The given date cannot be in the future.".getBytes(StandardCharsets.UTF_8));
		}
		MonthlyFinancialReport report = cashRegisterService.createFinancialReportMonth(firstOfMonth.atStartOfDay());
		if(report == null)
		{
			return ResponseEntity.badRequest()
				.body("No Transactions saved in the system.".getBytes(StandardCharsets.UTF_8));

		}
		if(report.isBeforeBeginning())
		{
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
		return "finances";
	}

	/**
	 *
	 * @param category chosen category
	 * @param model
	 * @return The page, where only chosen category of orders is shown
	 */
	@GetMapping("/filterCategories")
	@PreAuthorize("hasRole('BOSS')")
	public String filterCategories(@RequestParam("filter") String category, Model model) {
		//this.categorySet = category;
		if(category.equals("all")){
			return this.resetCategory(model);
		}
		else {
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
			if (!this.filteredByDates.isEmpty()) {
				setFilteredOrdersList(this.intersection(this.filteredByCategory,this.filteredByDates).stream().toList(), 100);
			}
			else{
				setFilteredOrdersList(this.filteredByCategory.stream().toList(),100);
			}
		}
		prepareFinancesModel(model,filteredAndCutOrdersList);
		this.isFilteredByCategory=true;
		return "finances";
	}

	/**
	 *
	 * @param size maximal number of entries to be shown in the table
	 */
	private void limitListSize(int size){
		if(this.filteredOrdersList.size() > size){
			this.filteredAndCutOrdersList = this.filteredOrdersList.subList(0, size);
		}
		else{
			this.filteredAndCutOrdersList = this.filteredOrdersList;
		}
	}

	/**
	 *
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


