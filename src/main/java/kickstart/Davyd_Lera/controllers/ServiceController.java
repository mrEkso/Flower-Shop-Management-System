package kickstart.Davyd_Lera.controllers;

import kickstart.Davyd_Lera.models.orders.ContractOrder;
import kickstart.Davyd_Lera.models.orders.EventOrder;
import kickstart.Davyd_Lera.models.orders.ReservationOrder;
import kickstart.Davyd_Lera.services.order.ContractOrderService;
import kickstart.Davyd_Lera.services.order.EventOrderService;
import kickstart.Davyd_Lera.services.order.ReservationOrderService;
import org.salespointframework.order.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Controller
@RequestMapping("/services")
public class ServiceController {

	private final EventOrderService eventOrderService;
	private final ReservationOrderService reservationOrderService;
	private final ContractOrderService contractOrderService;

	public ServiceController(EventOrderService eventOrderService,
							 ReservationOrderService reservationOrderService,
							 ContractOrderService contractOrderService) {
		this.eventOrderService = eventOrderService;
		this.reservationOrderService = reservationOrderService;
		this.contractOrderService = contractOrderService;
	}

	@GetMapping("")
	public String getAllServices(Model model) {
		// Fetching all orders by type and adding to the model
		model.addAttribute("contracts", contractOrderService.findAll());
		model.addAttribute("events", eventOrderService.findAll());
		model.addAttribute("reservations", reservationOrderService.findAll());
		return "services/services";
	}

	/* GET BY ID ENDPOINT */
	@GetMapping("/{type}/{id}")
	public ResponseEntity<?> getOrderById(@PathVariable String type, @PathVariable Long id) {
		return switch (type) {
			case "event" -> handleGetOrder(eventOrderService.getById(id));
			case "reservation" -> handleGetOrder(reservationOrderService.getById(id));
			case "contract" -> handleGetOrder(contractOrderService.getById(id));
			default -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		};
	}

	/* CREATE ENDPOINTS */
	@PostMapping("/event")
	public ResponseEntity<EventOrder> createEventOrder(@RequestBody EventOrder eventOrder) {
		return new ResponseEntity<>(eventOrderService.create(eventOrder), HttpStatus.CREATED);
	}

	@PostMapping("/reservation")
	public ResponseEntity<ReservationOrder> createReservationOrder(@RequestBody ReservationOrder reservationOrder) {
		return new ResponseEntity<>(reservationOrderService.create(reservationOrder), HttpStatus.CREATED);
	}

	@PostMapping("/contract")
	public ResponseEntity<ContractOrder> createContractOrder(@RequestBody ContractOrder contractOrder) {
		return new ResponseEntity<>(contractOrderService.create(contractOrder), HttpStatus.CREATED);
	}

	/* DELETE ORDER ENDPOINT */
	@DeleteMapping("/{type}/{id}")
	public ResponseEntity<?> deleteOrder(@PathVariable String type, @PathVariable Long id) {
		return switch (type) {
			case "event" -> handleDeleteOrder(eventOrderService.getById(id), eventOrderService::delete);
			case "reservation" ->
				handleDeleteOrder(reservationOrderService.getById(id), reservationOrderService::delete);
			case "contract" -> handleDeleteOrder(contractOrderService.getById(id), contractOrderService::delete);
			default -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		};
	}

	/* PRIVATE HELPER METHODS */
	private <T> ResponseEntity<T> handleGetOrder(Optional<T> order) {
		return order.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
			.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	private <T extends Order> ResponseEntity<?> handleDeleteOrder(Optional<T> order, Consumer<T> deleteFunction) {
		return order.map(value -> {
			deleteFunction.accept(value);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
