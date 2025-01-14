package flowershop.finances;

import flowershop.sales.WholesalerOrder;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
	private final CashRegisterRepository cashRegisterRepository;

	public BalanceService(CashRegisterRepository cashRegisterRepository) {
		this.cashRegisterRepository = cashRegisterRepository;
	}

	private CashRegister getCashRegister() {
		return cashRegisterRepository.findFirstByOrderById()
			.orElseThrow(() -> new IllegalStateException("CashRegister instance not found"));
	}

	/**
	 *
	 * @param wholesalerOrder the created order
	 * @return true, if we don't have enough money for this purchase
	 */
	public boolean denies(WholesalerOrder wholesalerOrder) {
		return getCashRegister().getBalance().add(wholesalerOrder.getTotal()).isNegative();
	}
}
