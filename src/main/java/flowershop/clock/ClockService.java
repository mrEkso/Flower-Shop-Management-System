package flowershop.clock;

import flowershop.finances.CashRegister;
import flowershop.finances.CashRegisterRepository;
import flowershop.services.MonthlyBillingService;
import org.salespointframework.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;


/**
 * Is used to control the time
 */
@Service
public class ClockService {
	private final CashRegisterRepository cashRegisterRepository;

	private final MonthlyBillingService monthlyBillingService;

	public ClockService(CashRegisterRepository cashRegisterRepository, MonthlyBillingService monthlyBillingService) {
		this.cashRegisterRepository = cashRegisterRepository;
		this.monthlyBillingService = monthlyBillingService;
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

	public LocalDate getCurrentDate(){
		return getCashRegister().getCurrentDate();
	}

	/**
	 *
	 * @return current "in-game" time
	 */
	public LocalDateTime now(){
		CashRegister cashRegister = getCashRegister();
		return cashRegister
			.getCurrentDate()
			.atStartOfDay()
			.plusHours(9)
			.plus(Interval.from(cashRegister.getNewDayStarted()).to(LocalDateTime.now()).toDuration());
	}

	public boolean isOpen(){
		CashRegister cashRegister = getCashRegister();
		return cashRegister.isOpen();
	}

	/**
	 * Toggles the state of the shop (opened/closed)
	 *
	 */
	public void openOrClose(){
		CashRegister cashRegister = getCashRegister();
		cashRegister.setOpen(!cashRegister.isOpen());
		if (cashRegister.isOpen()) {
			if(!cashRegister.getCurrentDate().getMonth().equals(this.nextWorkingDay().getMonth()))
			{
				monthlyBillingService.addMonthlyCharges();
			}
			cashRegister.setCurrentDate(this.nextWorkingDay());
			cashRegister.setNewDayStarted(LocalDateTime.now());
			for ()
		}
		cashRegisterRepository.save(cashRegister);
		//TODO trigger the appearance of ordered goods in the inventory
	}

	private LocalDate nextWorkingDay() {
		LocalDate currentDate = getCurrentDate();
		LocalDate nextWorkingDay = currentDate.plusDays(1);
		while(nextWorkingDay.getDayOfWeek().getValue()>5)
		{
			nextWorkingDay = nextWorkingDay.plusDays(1);
		}
		return nextWorkingDay;
	}


}
