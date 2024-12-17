package flowershop.clock;

import flowershop.finances.CashRegister;
import flowershop.finances.CashRegisterRepository;
import flowershop.inventory.InventoryController;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.services.MonthlyBillingService;
import org.salespointframework.time.Interval;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


/**
 * Is used to control the time
 */
@Service
public class ClockService {
	private final CashRegisterRepository cashRegisterRepository;

	private final MonthlyBillingService monthlyBillingService;
	private final InventoryController inventoryController;
	private final ProductService productService;

	public ClockService(CashRegisterRepository cashRegisterRepository,
						MonthlyBillingService monthlyBillingService,
						InventoryController inventoryController,
						ProductService productService) {
		this.cashRegisterRepository = cashRegisterRepository;
		this.monthlyBillingService = monthlyBillingService;
		this.inventoryController = inventoryController;
		this.productService = productService;
	}

	/**
	 *
	 * @return the instance of CashRegister, stored in the repository
	 */
	public CashRegister getCashRegister() {
		Optional<CashRegister> cashRegister = cashRegisterRepository.findFirstByOrderById();
		return cashRegisterRepository.findFirstByOrderById()
			.orElseThrow(() -> new IllegalStateException("CashRegister instance not found"));
	}

	/**
	 *
	 * @return the date the is currently in the world of Frau Floris
	 */
	public LocalDate getCurrentDate(){
		CashRegister cashRegister = getCashRegister();
		if (cashRegister == null) {
			throw new IllegalStateException("CashRegister is not initialized");
		}
		return cashRegister.getInGameDate();
	}

	/**
	 *
	 * @return current "in-game" time (current in-game date and some time after 09:00)
	 */
	public LocalDateTime now(){
		CashRegister cashRegister = getCashRegister();
		if(cashRegister.getInGameDate() == null)
		{
			return LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0));
		}
		return cashRegister
			.getInGameDate()
			.atStartOfDay()
			.plusHours(9)
			.plus(Interval.from(cashRegister.getNewDayStarted()).to(LocalDateTime.now()).toDuration());
	}

	public boolean isOpen(){
		CashRegister cashRegister = getCashRegister();
		if (cashRegister == null) {
			throw new IllegalStateException("CashRegister is not initialized");
		}
		return cashRegister.getOpen();
	}

	/**
	 * Toggles the state of the shop (opened/closed)
	 *
	 */
	public void openOrClose(){
		CashRegister cashRegister = getCashRegister();
		cashRegister.setOpen(!cashRegister.getOpen());
		if (cashRegister.getOpen()) {
			if(!cashRegister.getInGameDate().getMonth().equals(this.nextWorkingDay().getMonth()))
			{
				monthlyBillingService.addMonthlyCharges();
			}
			cashRegister.setInGameDate(this.nextWorkingDay());
			cashRegister.setNewDayStarted(LocalDateTime.now());
			Set<PendingOrder> newPendingOrdersSet = new HashSet<>();
			for (PendingOrder i : cashRegister.getPendingOrders()) {
				if(i.getDueDate().equals(getCurrentDate())){
					Map<Flower,Integer> todaysGoods = new HashMap<>();
					for(String flowerName: i.getItemQuantityMap().keySet())
					{
						todaysGoods.put(productService.findFlowersByName(flowerName).getFirst(),
									i.getItemQuantityMap().get(flowerName).getAmount().intValue());
					}
				}
				else{
					newPendingOrdersSet.add(i);
				}
			}
			cashRegister.setPendingOrders(newPendingOrdersSet);
		}
		cashRegisterRepository.save(cashRegister);
	}

	/**
	 *
	 * @return next day, when the shop can work
	 * Currently it works Mo-Fr. To change this, refactor the while-clause a little
	 */
	public LocalDate nextWorkingDay() {
		LocalDate currentDate = getCurrentDate();
		LocalDate nextWorkingDay = currentDate.plusDays(1);
		while(nextWorkingDay.getDayOfWeek().getValue()>5)
		{
			nextWorkingDay = nextWorkingDay.plusDays(1);
		}
		return nextWorkingDay;
	}


}
