package flowershop.finances;

import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.time.Interval;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
//@RequestMapping("/finances")
public class FinancesController {


	private final CashRegisterService cashRegisterService;
	private List<AccountancyEntryWrapper> filteredOrdersList = new ArrayList<>();
	private List<AccountancyEntryWrapper> filteredAndCutOrdersList;
	private HashSet<AccountancyEntryWrapper> filteredByDates = new HashSet<>();
	private HashSet<AccountancyEntryWrapper> filteredByCategory = new HashSet<>();
	private LocalDate date1;
	private LocalDate date2;
	private String category;

	public FinancesController(CashRegisterService cashRegisterService) {
		this.cashRegisterService = cashRegisterService;
	}



	@GetMapping("/filterDates")
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
			Set<AccountancyEntryWrapper> intersection = new HashSet<>(filteredList);
			intersection.retainAll(this.filteredByCategory);
		}
		setFilteredOrdersList(filteredList.stream().toList(),20);
		prepareFinancesModel(model,filteredAndCutOrdersList);
		return "finances";
	}

	@GetMapping("/resetDates")
	public String resetDates(Model model) {
		this.filteredByDates = new HashSet<>();
		this.date1=LocalDate.of(1970,1,1);
		this.date2=LocalDate.now();
		prepareFinancesModel(model,this.filteredByCategory.stream().toList());
		return "finances";
	}

	@GetMapping("/resetCategory")
	public String resetCategory(Model model) {
		this.filteredByCategory = new HashSet<>();
		this.category="all";
		prepareFinancesModel(model,this.filteredByDates.stream().toList());
		return "finances";
	}

	private void prepareFinancesModel(Model model, List<AccountancyEntryWrapper> transactions) {
		model.addAttribute("transactions", transactions);
		model.addAttribute("currentBalance", cashRegisterService.getBalance());
		model.addAttribute("date1", date1);
		model.addAttribute("date2", date2);
		model.addAttribute("category", category);
	}

	private void setFilteredOrdersList(List<AccountancyEntryWrapper> filteredOrdersList, int size) {
		List<AccountancyEntryWrapper> tempList = new ArrayList<>(filteredOrdersList);
		Collections.sort(tempList, new Comparator<AccountancyEntry>() {
			@Override
			public int compare(AccountancyEntry first, AccountancyEntry second) {
				Optional<LocalDateTime> ldt1 = first.getDate();
				Optional<LocalDateTime> ldt2 = second.getDate();
				if (ldt1.isPresent() && ldt2.isPresent()) {
					return ldt2.get().compareTo(ldt1.get());
				}
				else{
					throw new IllegalStateException("Some entries dont have date assigned");
				}
			}
		});
		this.filteredOrdersList = tempList;
		limitListSize(size);
	}

	@GetMapping("/finances")
	public String getTransactionPage(Model model) {
		for (AccountancyEntry i: this.cashRegisterService.findAll().toList()){
			this.filteredOrdersList.add((AccountancyEntryWrapper) i);
		}

		setFilteredOrdersList(filteredOrdersList, 20);
		model.addAttribute("transactions", filteredAndCutOrdersList);
		model.addAttribute("currentBalance", cashRegisterService.getBalance());
		return "finances";
	}

	@GetMapping("/filterCategories")
	public String filterCategories(@RequestParam("filter") String category, Model model) {
		//this.categorySet = category;
		if(category.equals("all")){
			this.filteredByCategory = new HashSet<>();
		}
		else {
			List<AccountancyEntry> lst;
			if (category.equals("income")) {
				lst = this.cashRegisterService.filterIncomeOrSpending(true);
			} else if (category.equals("spendings")) {
				lst = this.cashRegisterService.filterIncomeOrSpending(false);
			} else if (category.equals("simple order")) {
				lst = this.cashRegisterService.filterEntries(AccountancyEntryWrapper.Category.Einfacher_Verkauf).stream().toList();
			} else if (category.equals("reserved order")) {
				lst = this.cashRegisterService.filterEntries(AccountancyEntryWrapper.Category.Reservierter_Verkauf).stream().toList();
			} else if (category.equals("event order")) {
				lst = this.cashRegisterService.filterEntries(AccountancyEntryWrapper.Category.Veranstaltung_Verkauf).stream().toList();
			} else if (category.equals("contract order")) {
				lst = this.cashRegisterService.filterEntries(AccountancyEntryWrapper.Category.Vertraglicher_Verkauf).stream().toList();
			} else {
				lst = this.cashRegisterService.filterEntries(AccountancyEntryWrapper.Category.Einkauf).stream().toList();
			}
			this.filteredByCategory = new HashSet<>();
			for (AccountancyEntry i : lst) {
				this.filteredByCategory.add((AccountancyEntryWrapper) i);
			}
			if (!this.filteredByDates.isEmpty()) {
				Set<AccountancyEntryWrapper> intersection = new HashSet<>(this.filteredByDates);
				intersection.retainAll(this.filteredByCategory);
				setFilteredOrdersList(intersection.stream().toList(), 20);
			}
			else{
				setFilteredOrdersList(this.filteredByCategory.stream().toList(),20);
			}
		}
		prepareFinancesModel(model,filteredAndCutOrdersList);
		return "finances";
	}

	private void limitListSize(int size){
		if(this.filteredOrdersList.size() > 20){
			this.filteredAndCutOrdersList = this.filteredOrdersList.subList(0, 20);
		}
	}

}


