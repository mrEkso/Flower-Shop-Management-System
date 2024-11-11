package flowershop.models.accounting;

import flowershop.models.order.AbstractOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class DailyFinancialReport extends FinancialReport{
	public DailyFinancialReport(LocalDateTime day, double balancePrevDay) {
		super(day,balancePrevDay);
		//fill orders (DAY)
		ArrayList orders = new ArrayList<AbstractOrder>();
		initialize();
		//do the rest.
	}
}
