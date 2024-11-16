package flowershop.repositories.orders;

import flowershop.services.order.SimpleOrderService;
import org.springframework.stereotype.Component;

@Component
public class OrderFactoryRepository {
	private final EventOrderRepository eventOrderRepository;
	private final ContractOrderRepository contractOrderRepository;
	private final ReservationOrderRepository reservationOrderRepository;
	private final SimpleOrderRepository simpleOrderRepository;

	public OrderFactoryRepository(EventOrderRepository eventOrderRepository, ContractOrderRepository contractOrderRepository, ReservationOrderRepository reservationOrderRepository, SimpleOrderRepository simpleOrderRepository) {
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
