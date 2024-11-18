package flowershop.services;

import flowershop.sales.SimpleOrderRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryFactory {
	private final EventOrderRepository eventOrderRepository;
	private final ContractOrderRepository contractOrderRepository;
	private final ReservationOrderRepository reservationOrderRepository;
	private final SimpleOrderRepository simpleOrderRepository;

	public OrderRepositoryFactory(EventOrderRepository eventOrderRepository, ContractOrderRepository contractOrderRepository, ReservationOrderRepository reservationOrderRepository, SimpleOrderRepository simpleOrderRepository) {
		this.eventOrderRepository = eventOrderRepository;
		this.contractOrderRepository = contractOrderRepository;
		this.reservationOrderRepository = reservationOrderRepository;
		this.simpleOrderRepository = simpleOrderRepository;
	}

	public EventOrderRepository getEventOrderRepository() {
		return eventOrderRepository;
	}

	public ContractOrderRepository getContractOrderRepository() {
		return contractOrderRepository;
	}

	public ReservationOrderRepository getReservationOrderRepository() {
		return reservationOrderRepository;
	}

	public SimpleOrderRepository getSimpleOrderRepository() { return simpleOrderRepository;}
}
