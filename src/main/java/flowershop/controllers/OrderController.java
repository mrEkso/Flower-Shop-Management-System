package flowershop.controllers;

import flowershop.models.order.ContractOrder;
import flowershop.models.order.EventOrder;
import flowershop.models.order.ReservationOrder;
import flowershop.services.order.AbstractOrderService;
import flowershop.services.order.ContractOrderService;
import flowershop.services.order.EventOrderService;
import flowershop.services.order.ReservationOrderService;
import org.salespointframework.order.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

	private final EventOrderService eventOrderService;
	private final ReservationOrderService reservationOrderService;
	private final ContractOrderService contractOrderService;

	public OrderController(EventOrderService eventOrderService,
						   ReservationOrderService reservationOrderService,
						   ContractOrderService contractOrderService) {
		this.eventOrderService = eventOrderService;
		this.reservationOrderService = reservationOrderService;
		this.contractOrderService = contractOrderService;
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
	public ResponseEntity<Void> deleteOrder(@PathVariable String type, @PathVariable Long id) {
		return switch (type) {
			case "event" -> handleDeleteOrder(eventOrderService.getById(id), eventOrderService);
			case "reservation" -> handleDeleteOrder(reservationOrderService.getById(id), reservationOrderService);
			case "contract" -> handleDeleteOrder(contractOrderService.getById(id), contractOrderService);
			default -> new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		};
	}

	/* PRIVATE HELPER METHODS */
	private <T> ResponseEntity<T> handleGetOrder(Optional<T> order) {
		return order.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
			.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	private <T extends Order> ResponseEntity<Void> handleDeleteOrder(Optional<T> order, AbstractOrderService<T> service) {
		return order.map(value -> {
			service.delete(value);
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
