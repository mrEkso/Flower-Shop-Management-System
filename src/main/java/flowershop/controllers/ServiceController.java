package flowershop.controllers;

import flowershop.models.orders.ContractOrder;
import flowershop.models.orders.EventOrder;
import flowershop.models.orders.ReservationOrder;
import flowershop.services.order.ContractOrderService;
import flowershop.services.order.EventOrderService;
import flowershop.services.order.ReservationOrderService;
import org.salespointframework.order.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
	public ResponseEntity<?> getOrderById(@PathVariable String type, @PathVariable UUID id) {
		return switch (type) {
			case "events" -> handleGetOrder(eventOrderService.getById(id));
			case "reservations" -> handleGetOrder(reservationOrderService.getById(id));
			case "contracts" -> handleGetOrder(contractOrderService.getById(id));
			default -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		};
	}

	@GetMapping("/events/edit/{id}")
	public String getEventOrderEditPage(@PathVariable UUID id,
										Model model){
		EventOrder dbEventOrder = eventOrderService.getById(id).get();
		model.addAttribute("eventOrder", dbEventOrder);
		return "edit/eventOrderEditForm";
	}

	/* CREATE ENDPOINTS */
	@PostMapping("/events")
	public ResponseEntity<EventOrder> createEventOrder(@RequestBody EventOrder eventOrder) {
		return new ResponseEntity<>(eventOrderService.create(eventOrder), HttpStatus.CREATED);
	}

	@PostMapping("/reservations")
	public ResponseEntity<ReservationOrder> createReservationOrder(@RequestBody ReservationOrder reservationOrder) {
		return new ResponseEntity<>(reservationOrderService.create(reservationOrder), HttpStatus.CREATED);
	}

	@PostMapping("/contracts")
	public ResponseEntity<ContractOrder> createContractOrder(@RequestBody ContractOrder contractOrder) {
		return new ResponseEntity<>(contractOrderService.create(contractOrder), HttpStatus.CREATED);
	}

	/* DELETE ORDER ENDPOINT */
	@DeleteMapping("/{type}/{id}")
	public ResponseEntity<?> deleteOrder(@PathVariable String type, @PathVariable UUID id) {
		return switch (type) {
			case "events" -> handleDeleteOrder(eventOrderService.getById(id), eventOrderService::delete);
			case "reservations" ->
				handleDeleteOrder(reservationOrderService.getById(id), reservationOrderService::delete);
			case "contracts" -> handleDeleteOrder(contractOrderService.getById(id), contractOrderService::delete);
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
