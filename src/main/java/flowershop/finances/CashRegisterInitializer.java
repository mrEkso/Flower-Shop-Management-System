package flowershop.finances;


import org.javamoney.moneta.Money;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Set;

import static org.salespointframework.core.Currencies.EURO;

@Component
@Order(20)
public class CashRegisterInitializer implements DataInitializer {
	private final CashRegisterRepository cashRegisterRepository;

	public CashRegisterInitializer(CashRegisterRepository cashRegisterRepository) {
		Assert.notNull(cashRegisterRepository, "CashRegisterRepository must not be null!");
		this.cashRegisterRepository = cashRegisterRepository;
	}
	@Override
	public void initialize() {
		if (cashRegisterRepository.findAll().iterator().hasNext()) {
			return; // Skip initialization if products already exist
		}
		CashRegister cashRegister = new CashRegister(Set.of(), Money.of(5000, EURO));
		cashRegisterRepository.save(cashRegister);

	}
}
