package flowershop.controllers;

import flowershop.models.Client;
import flowershop.models.orders.ContractOrder;
import flowershop.models.orders.EventOrder;
import flowershop.models.orders.ReservationOrder;
import flowershop.services.order.ContractOrderService;
import flowershop.services.order.EventOrderService;
import flowershop.services.order.ReservationOrderService;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/services")
public class ServiceController {

	private final EventOrderService eventOrderService;
	private final ReservationOrderService reservationOrderService;
	private final ContractOrderService contractOrderService;
	private final UserAccountManagement userAccountManagement;

	public ServiceController(EventOrderService eventOrderService,
							 ReservationOrderService reservationOrderService,
							 ContractOrderService contractOrderService,
							 UserAccountManagement userAccountManagement) {
		this.eventOrderService = eventOrderService;
		this.reservationOrderService = reservationOrderService;
		this.contractOrderService = contractOrderService;
		this.userAccountManagement = userAccountManagement;
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
		switch (type) {
			case "events" -> {
				return eventOrderService.getById(id)
					.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
					.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			}
			case "reservations" -> {
				return reservationOrderService.getById(id)
					.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
					.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			}
			case "contracts" -> {
				return contractOrderService.getById(id)
					.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
					.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			}
			default -> {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
	}

	/* CREATE ENDPOINTS */
	@GetMapping("/create")
	public String getNewOrderPage() {
		return "services/create_service";
	}

	@PostMapping("/events/create")
	public String createEventOrder(
		@RequestParam String companyName,
		@RequestParam("eventDate") LocalDate eventDate,
		@RequestParam("phone") String phone,
		@RequestParam("deliveryAddress") String deliveryAddress,
		@RequestParam("notes") String notes) {
//		UserAccount frauFloris = userAccountManagement.findByUsername("frau_floris").orElseThrow(() ->
//			new IllegalArgumentException("Frau Floris account not found"));
//		EventOrder eventOrder = new EventOrder(eventDate, deliveryAddress, frauFloris, new Client(companyName, phone));
//		eventOrderService.create(eventOrder);
		return "redirect:/services";
	}

	@PostMapping("/reservations/create")
	public String createReservationOrder(@RequestParam String companyName,
										 @RequestParam("reservationDateTime") LocalDateTime reservationDateTime,
										 @RequestParam("contactPhoneNumber") String phone,
										 @RequestParam("notes") String notes) {
		UserAccount frauFloris = userAccountManagement.findByUsername("frau_floris")
			.orElseThrow(() -> new IllegalArgumentException("Frau Floris account not found"));
		ReservationOrder reservationOrder = new ReservationOrder(reservationDateTime, frauFloris, new Client(companyName, phone));
		reservationOrderService.create(reservationOrder);
		return "redirect:/services";
	}

	@PostMapping("/contracts/create")
	public String createContractOrder(@RequestParam String companyName,
									  @RequestParam("contractType") String contractType,
									  @RequestParam("startDate") LocalDate startDate,
									  @RequestParam("endDate") LocalDate endDate,
									  @RequestParam("address") String address,
									  @RequestParam("phone") String phone,
									  @RequestParam("notes") String notes) {
//		UserAccount frauFloris = userAccountManagement.findByUsername("frau_floris")
//			.orElseThrow(() -> new IllegalArgumentException("Frau Floris account not found"));
//		ContractOrder contractOrder = new ContractOrder(contractType, startDate, endDate, address, frauFloris, new Client(companyName, phone));
//		contractOrderService.create(contractOrder);
		return "redirect:/services";
	}

	/* EDIT ENDPOINTS */
	@GetMapping("/events/edit/{id}")
	public String getEventOrderEditPage(@PathVariable UUID id,
										Model model) {
		EventOrder dbEventOrder = eventOrderService.getById(id).get();
		model.addAttribute("eventOrder", dbEventOrder);
		return "edit/eventOrderEditForm";
	}

	@GetMapping("/contracts/edit/{id}")
	public String getContractOrderEditPage(@PathVariable UUID id,
										   Model model) {
		ContractOrder dbContractOrder = contractOrderService.getById(id).get();
		model.addAttribute("contractOrder", dbContractOrder);
		return "edit/contractOrderEditForm";
	}

	@GetMapping("/reservations/edit/{id}")
	public String getReservationOrderEditPage(@PathVariable UUID id,
											  Model model) {
		ReservationOrder dbReservationOrder = reservationOrderService.getById(id).get();
		model.addAttribute("reservationOrder", dbReservationOrder);
		return "edit/reservationOrderEditForm";
	}

	/* DELETE ORDER ENDPOINT */
	@DeleteMapping("/{type}/{id}")
	public ResponseEntity<?> deleteOrder(@PathVariable String type, @PathVariable UUID id) {
		switch (type) {
			case "events" -> {
				return eventOrderService.getById(id).map(value -> {
					eventOrderService.delete(value);
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			}
			case "reservations" -> {
				return reservationOrderService.getById(id).map(value -> {
					reservationOrderService.delete(value);
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			}
			case "contracts" -> {
				return contractOrderService.getById(id).map(value -> {
					contractOrderService.delete(value);
					return new ResponseEntity<>(HttpStatus.NO_CONTENT);
				}).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			}
			default -> {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
	}
}
