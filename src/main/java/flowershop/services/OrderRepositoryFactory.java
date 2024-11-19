package flowershop.services;

import flowershop.sales.SimpleOrderRepository;
import flowershop.sales.WholesalerOrderRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryFactory {
	private final EventOrderRepository eventOrderRepository;
	private final ContractOrderRepository contractOrderRepository;
	private final ReservationOrderRepository reservationOrderRepository;
	private final SimpleOrderRepository simpleOrderRepository;
	private final WholesalerOrderRepository wholesalerOrderRepository;

	public OrderRepositoryFactory(EventOrderRepository eventOrderRepository, ContractOrderRepository contractOrderRepository, ReservationOrderRepository reservationOrderRepository, SimpleOrderRepository simpleOrderRepository, WholesalerOrderRepository wholesalerOrderRepository) {
		this.eventOrderRepository = eventOrderRepository;
		this.contractOrderRepository = contractOrderRepository;
		this.reservationOrderRepository = reservationOrderRepository;
		this.simpleOrderRepository = simpleOrderRepository;
		this.wholesalerOrderRepository = wholesalerOrderRepository;
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

	public SimpleOrderRepository getSimpleOrderRepository() {
		return simpleOrderRepository;
	}

	public WholesalerOrderRepository getWholesalerOrderRepository() {
		return wholesalerOrderRepository;
	}
}
