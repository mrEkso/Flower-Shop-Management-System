package kickstart.Davyd_Lera.repositories.orders;

import org.springframework.stereotype.Component;

@Component
public class OrderFactoryRepository {
	private final EventOrderRepository eventOrderRepository;
	private final ContractOrderRepository contractOrderRepository;
	private final ReservationOrderRepository reservationOrderRepository;

	public OrderFactoryRepository(EventOrderRepository eventOrderRepository, ContractOrderRepository contractOrderRepository, ReservationOrderRepository reservationOrderRepository) {
		this.eventOrderRepository = eventOrderRepository;
		this.contractOrderRepository = contractOrderRepository;
		this.reservationOrderRepository = reservationOrderRepository;
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
}
