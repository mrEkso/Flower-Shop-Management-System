package flowershop.services;

import org.salespointframework.order.OrderEvents;
import org.salespointframework.order.OrderManagement;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service for handling monthly billing operations.
 */
@Service
public class MonthlyBillingService {
	private final ContractOrderService contractOrderService;
	private final ApplicationEventPublisher eventPublisher;
	private final OrderManagement<ContractOrder> orderManagement;

	/**
	 * Constructs a MonthlyBillingService with the specified dependencies.
	 *
	 * @param contractOrderService the service for managing contract orders
	 * @param eventPublisher       the publisher for application events
	 * @param orderManagement      the management system for orders
	 */
	public MonthlyBillingService(ContractOrderService contractOrderService, ApplicationEventPublisher eventPublisher, OrderManagement<ContractOrder> orderManagement) {
		Assert.notNull(contractOrderService, "ContractOrderService must not be null!");
		Assert.notNull(eventPublisher, "ApplicationEventPublisher must not be null!");
		Assert.notNull(orderManagement, "OrderManagement must not be null!");
		this.contractOrderService = contractOrderService;
		this.eventPublisher = eventPublisher;
		this.orderManagement = orderManagement;
	}

	/**
	 * Adds monthly charges to active contracts and marks contracts as paid if their end date has passed.
	 * This method is scheduled to run at 9:00 on the first working day of every month.
	 */
	//@Scheduled(cron = "0 0 0 1 * ?")
	@Transactional
	public void addMonthlyCharges() {
		for (ContractOrder contract : contractOrderService.findAllActiveLastMonth()) {
			// If the contract's end date has passed, mark it as paid
			if (contract.getEndDate().isBefore(LocalDateTime.now())) orderManagement.payOrder(contract);
			else {
				// Publish an event indicating the order in last month has been paid
				var event = OrderEvents.OrderPaid.of(contract);
				eventPublisher.publishEvent(event); // Needed for Finances
			}
		}
	}
}
