package flowershop.controllers;

import flowershop.models.accounting.AccountancyEntryWrapper;
import flowershop.services.finances.CashRegisterService;
import org.hibernate.sql.ast.tree.expression.Collation;
import org.salespointframework.accountancy.AccountancyEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.*;

@Controller
//@RequestMapping("/finances")
public class FinancesController {


	private final CashRegisterService cashRegisterService;
	private List<AccountancyEntryWrapper> filteredOrdersList = new ArrayList<>();

	public FinancesController(CashRegisterService cashRegisterService) {
		this.cashRegisterService = cashRegisterService;
	}


	@GetMapping("/finances")
	public String getTransactionPage(Model model) {
		for (AccountancyEntry i: this.cashRegisterService.findAll().toList()){
			this.filteredOrdersList.add((AccountancyEntryWrapper) i);
		}

		Collections.sort(this.filteredOrdersList, new Comparator<AccountancyEntry>() {
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
		if(this.filteredOrdersList.size() > 20){
			this.filteredOrdersList = this.filteredOrdersList.subList(0, 20);
		}
		model.addAttribute("transactions", filteredOrdersList);
		return "finances";
	}

}
