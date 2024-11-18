package flowershop.services;

import flowershop.product.ProductService;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/services")
public class ServiceController {

	private final EventOrderService eventOrderService;
	private final ContractOrderService contractOrderService;
	private final ReservationOrderService reservationOrderService;
	private final ProductService productService;
	private final ClientService clientService;
	private final UserAccountManagement userAccountManagement;

	public ServiceController(EventOrderService eventOrderService, ContractOrderService contractOrderService, ReservationOrderService reservationOrderService, ProductService productService, ClientService clientService, UserAccountManagement userAccountManagement) {
		this.eventOrderService = eventOrderService;
		this.contractOrderService = contractOrderService;
		this.reservationOrderService = reservationOrderService;
		this.productService = productService;
		this.clientService = clientService;
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
			case "contracts" -> {
				return contractOrderService.getById(id)
					.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
					.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			}
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

	@GetMapping("/add-product-row")
	public String addProductRow(@RequestParam("index") int index, Model model) {
		model.addAttribute("index", index);
		model.addAttribute("products", productService.getAllProducts());
		return "fragments/product-row :: productRow";
	}

	@PostMapping("/contracts/create")
	public String createContractOrder(@RequestParam("clientName") String clientName,
									  @RequestParam("contractType") String contractType,
									  @RequestParam(value = "frequency", required = false) String frequency,
									  @RequestParam(value = "customFrequency", required = false) Integer customFrequency,
									  @RequestParam(value = "customUnit", required = false) String customUnit,
									  @RequestParam("startDate") LocalDate startDate,
									  @RequestParam("endDate") LocalDate endDate,
									  @RequestParam("address") String address,
									  @RequestParam("phone") String phone,
									  @RequestParam Map<String, String> products,
									  @RequestParam("notes") String notes) {
//		ContractOrder contractOrder = new ContractOrder(contractType, startDate, endDate, address,
//			getFrauFlorisAccount(), getOrCreateClient(clientName, phone), notes);
//		if ("recurring".equals(frequency)) {
//			contractOrder.setFrequency(frequency);
//		} else if ("custom".equals(frequency)) {
//			contractOrder.setCustomFrequency(customFrequency);
//			contractOrder.setCustomUnit(customUnit);
//		}
//		contractOrderService.save(contractOrder, products);
		return "redirect:/services";
	}

	@PostMapping("/events/create")
	public String createEventOrder(
		@RequestParam String clientName,
		@RequestParam("eventDate") LocalDate eventDate,
		@RequestParam("phone") String phone,
		@RequestParam("deliveryAddress") String deliveryAddress,
		@RequestParam Map<String, String> products,
		@RequestParam("notes") String notes) {
		EventOrder eventOrder = new EventOrder(eventDate, deliveryAddress,
			getFrauFlorisAccount(), getOrCreateClient(clientName, phone), notes);
		eventOrderService.save(eventOrder, products);
		return "redirect:/services";
	}

	@PostMapping("/reservations/create")
	public String createReservationOrder(@RequestParam String clientName,
										 @RequestParam("reservationDateTime") LocalDateTime reservationDateTime,
										 @RequestParam("phone") String phone,
										 @RequestParam Map<String, String> products,
										 @RequestParam("notes") String notes) {
//		ReservationOrder reservationOrder = new ReservationOrder(reservationDateTime,
//			getFrauFlorisAccount(), getOrCreateClient(clientName, phone), notes);
		//reservationOrderService.save(reservationOrder, products);
		return "redirect:/services";
	}

	/* EDIT ENDPOINTS */
	@GetMapping("/contracts/edit/{id}")
	public String getContractOrderEditPage(@PathVariable UUID id,
										   Model model) {
		model.addAttribute("contractOrder", contractOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "edit/contractOrderEditForm";
	}

	@PutMapping("/contracts/edit/{id}")
	public String editContractOrder(@PathVariable UUID id,
									@RequestParam("clientName") String clientName,
									@RequestParam("contractType") String contractType,
									@RequestParam(value = "frequency", required = false) String frequency,
									@RequestParam(value = "customFrequency", required = false) Integer customFrequency,
									@RequestParam(value = "customUnit", required = false) String customUnit,
									@RequestParam("startDate") LocalDate startDate,
									@RequestParam("endDate") LocalDate endDate,
									@RequestParam("address") String address,
									@RequestParam("phone") String phone,
									@RequestParam Map<String, String> products,
									@RequestParam("paymentMethod") String paymentMethod,
									@RequestParam("orderStatus") String orderStatus,
									@RequestParam(value = "cancelReason", required = false) String cancelReason,
									@RequestParam("notes") String notes) {
		ContractOrder contractOrder = contractOrderService.getById(id)
			.orElseThrow(() -> new IllegalArgumentException("Contract order not found"));
		contractOrder.setClient(getOrCreateClient(clientName, phone));
		contractOrder.setContractType(contractType);
		contractOrder.setStartDate(startDate);
		contractOrder.setEndDate(endDate);
		contractOrder.setAddress(address);
		contractOrder.setNotes(notes);
		contractOrder.setPaymentMethod(paymentMethod);
		System.out.println(paymentMethod);
		System.out.println(contractOrder.getPaymentMethod());
		if ("recurring".equals(frequency)) {
			contractOrder.setFrequency(frequency);
		} else if ("custom".equals(frequency)) {
			contractOrder.setCustomFrequency(customFrequency);
			contractOrder.setCustomUnit(customUnit);
		}
		contractOrderService.update(contractOrder, products, orderStatus, cancelReason);
		return "redirect:/services";
	}

	@GetMapping("/events/edit/{id}")
	public String getEventOrderEditPage(@PathVariable UUID id,
										Model model) {
		model.addAttribute("eventOrder", eventOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "edit/eventOrderEditForm";
	}

	@PutMapping("/events/edit/{id}")
	public String editEventOrder(@PathVariable UUID id,
								 @RequestParam String clientName,
								 @RequestParam("eventDate") LocalDate eventDate,
								 @RequestParam("phone") String phone,
								 @RequestParam("deliveryAddress") String deliveryAddress,
								 @RequestParam Map<String, String> products,
								 @RequestParam("paymentMethod") String paymentMethod,
								 @RequestParam("orderStatus") String orderStatus,
								 @RequestParam(value = "cancelReason", required = false) String cancelReason,
								 @RequestParam("notes") String notes) {
		EventOrder eventOrder = eventOrderService.getById(id)
			.orElseThrow(() -> new IllegalArgumentException("Event order not found"));
		eventOrder.setClient(getOrCreateClient(clientName, phone));
		eventOrder.setEventDate(eventDate);
		eventOrder.setDeliveryAddress(deliveryAddress);
		eventOrder.setNotes(notes);
		eventOrder.setPaymentMethod(paymentMethod);
		eventOrderService.update(eventOrder, products, orderStatus, cancelReason);
		return "redirect:/services";
	}

	@GetMapping("/reservations/edit/{id}")
	public String getReservationOrderEditPage(@PathVariable UUID id,
											  Model model) {
		model.addAttribute("reservationOrder", reservationOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "edit/reservationOrderEditForm";
	}

	@PutMapping("/reservations/edit/{id}")
	public String editReservationOrder(@PathVariable UUID id,
									   @RequestParam String clientName,
									   @RequestParam("reservationDateTime") LocalDateTime reservationDateTime,
									   @RequestParam("phone") String phone,
									   @RequestParam Map<String, String> products,
									   @RequestParam("paymentMethod") String paymentMethod,
									   @RequestParam("orderStatus") String orderStatus,
									   @RequestParam(value = "cancelReason", required = false) String cancelReason,
									   @RequestParam("notes") String notes) {
		ReservationOrder reservationOrder = reservationOrderService.getById(id)
			.orElseThrow(() -> new IllegalArgumentException("Reservation order not found"));
		reservationOrder.setClient(getOrCreateClient(clientName, phone));
		reservationOrder.setReservationDateTime(reservationDateTime);
		reservationOrder.setNotes(notes);
		reservationOrder.setPaymentMethod(paymentMethod);
		reservationOrderService.update(reservationOrder, products, orderStatus, cancelReason);
		return "redirect:/services";
	}

	private UserAccount getFrauFlorisAccount() {
		return userAccountManagement.findByUsername("frau_floris")
			.orElseThrow(() -> new IllegalArgumentException("Frau Floris account not found"));
	}

	private Client getOrCreateClient(String name, String phone) {
		return clientService.getOrCreateClient(name, phone);
	}
}
