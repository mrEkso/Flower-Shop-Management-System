package flowershop.services;

import flowershop.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/services")
public class ServiceController {

	private final EventOrderService eventOrderService;
	private final ContractOrderService contractOrderService;
	private final ReservationOrderService reservationOrderService;
	private final ProductService productService;
	private final ClientService clientService;
	private final OrderFactory orderFactory;

	public ServiceController(EventOrderService eventOrderService, ContractOrderService contractOrderService, ReservationOrderService reservationOrderService,
							 ProductService productService, ClientService clientService, OrderFactory orderFactory) {
		this.eventOrderService = eventOrderService;
		this.contractOrderService = contractOrderService;
		this.reservationOrderService = reservationOrderService;
		this.productService = productService;
		this.clientService = clientService;
		this.orderFactory = orderFactory;
	}

	@GetMapping("")
	public String getAllServices(Model model) {
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
	public String getNewOrderPage(Model model) {
		model.addAttribute("productRows", new ArrayList<>());
		model.addAttribute("products", productService.getAllProducts());
		return "services/create_service";
	}

	@GetMapping("/add-product-row")
	public String addProductRow(@RequestParam("index") int index, Model model) {
		model.addAttribute("index", index);
		model.addAttribute("products", productService.getAllProducts());
		return "fragments/product-row :: productRow";
	}

	// Adding a product during creation
	@PostMapping("/{type}/create/add-product")
	public String addProductDuringCreation(@PathVariable String type,
										   @RequestParam int index,
										   Model model) {
		model.addAttribute("index", index);
		model.addAttribute("products", productService.getAllProducts());
		model.addAttribute("type", type);
		return "fragments/product-row :: productRow";
	}

	// Removing a product during creation
	@PostMapping("/{type}/create/remove-product")
	public String removeProductDuringCreation(@PathVariable String type,
											  @RequestParam int index,
											  Model model) {
		model.addAttribute("type", type);
		return "redirect:/services/" + type + "/create";
	}

	// Adding a product during editing
	@PostMapping("/{type}/edit/{id}/add-product")
	public String addProductDuringEdit(@PathVariable String type,
									   @PathVariable UUID id,
									   @RequestParam int index,
									   Model model) {
		model.addAttribute("index", index);
		model.addAttribute("products", productService.getAllProducts());
		model.addAttribute("type", type);
		model.addAttribute("orderId", id);
		return "fragments/product-row :: productRow";
	}

	// Removing a product during editing
	@PostMapping("/{type}/edit/{id}/remove-product")
	public String removeProductDuringEdit(@PathVariable String type,
										  @PathVariable UUID id,
										  @RequestParam UUID productId,
										  Model model) {
		switch (type) {
			case "contracts" -> contractOrderService.removeProductFromOrder(id, productId);
			case "events" -> eventOrderService.removeProductFromOrder(id, productId);
			case "reservations" -> reservationOrderService.removeProductFromOrder(id, productId);
			default -> throw new IllegalArgumentException("Invalid order type: " + type);
		}
		return "redirect:/services/" + type + "/edit/" + id;
	}


	@PostMapping("/create")
	public String handleFormSubmission(
		@RequestParam Map<String, String> allParams,
		@RequestParam(name = "action", required = false) String action,
		@RequestParam(name = "removeIndex", required = false) Integer removeIndex,
		Model model) {

		List<Map<String, String>> productRows = extractProductRows(allParams);

		// Handle "Add Product" action
		if ("add".equals(action)) {
			productRows.add(new HashMap<>());
		}

		// Handle "Remove Product" action
		if ("remove".equals(action) && removeIndex != null) {
			productRows.remove((int) removeIndex);
		}

		// Re-render the form with updated rows
		model.addAttribute("productRows", productRows);
		model.addAttribute("products", productService.getAllProducts());
		return "services/create";
	}

	 private List<Map<String, String>> extractProductRows(Map<String, String> allParams) {
		List<Map<String, String>> rows = new ArrayList<>();
		return rows;
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
		ContractOrder contractOrder = orderFactory.createContractOrder(contractType,
			startDate, endDate, address, getOrCreateClient(clientName, phone), notes);
		if ("recurring".equals(frequency)) {
			contractOrder.setFrequency(frequency);
		} else if ("custom".equals(frequency)) {
			contractOrder.setCustomFrequency(customFrequency);
			contractOrder.setCustomUnit(customUnit);
		}
		contractOrderService.save(contractOrder, products);
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
		EventOrder eventOrder = orderFactory.createEventOrder(eventDate,
			deliveryAddress, getOrCreateClient(clientName, phone), notes);
		eventOrderService.save(eventOrder, products);
		return "redirect:/services";
	}

	@PostMapping("/reservations/create")
	public String createReservationOrder(@RequestParam String clientName,
										 @RequestParam("reservationDateTime") LocalDateTime reservationDateTime,
										 @RequestParam("phone") String phone,
										 @RequestParam Map<String, String> products,
										 @RequestParam("notes") String notes) {
		ReservationOrder reservationOrder = orderFactory.createReservationOrder(reservationDateTime,
			getOrCreateClient(clientName, phone), notes);
		reservationOrderService.save(reservationOrder, products);
		return "redirect:/services";
	}

	/* EDIT ENDPOINTS */
	@GetMapping("/contracts/edit/{id}")
	public String getContractOrderEditPage(@PathVariable UUID id,
										   Model model) {
		model.addAttribute("contractOrder", contractOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/edit/contractOrderEditForm";
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
									@RequestParam("notes") String notes,
									RedirectAttributes redirectAttributes) {
		try {
			ContractOrder contractOrder = contractOrderService.getById(id)
				.orElseThrow(() -> new IllegalArgumentException("Contract order not found"));
			contractOrder.setClient(getOrCreateClient(clientName, phone));
			contractOrder.setContractType(contractType);
			contractOrder.setStartDate(startDate);
			contractOrder.setEndDate(endDate);
			contractOrder.setAddress(address);
			contractOrder.setNotes(notes);
			contractOrder.setPaymentMethod(paymentMethod);
			if ("recurring".equals(frequency)) {
				contractOrder.setFrequency(frequency);
			} else if ("custom".equals(frequency)) {
				contractOrder.setCustomFrequency(customFrequency);
				contractOrder.setCustomUnit(customUnit);
			}
			contractOrderService.update(contractOrder, products, orderStatus, cancelReason);
			return "redirect:/services";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/reservations/edit/" + id;
		}
	}

	@GetMapping("/events/edit/{id}")
	public String getEventOrderEditPage(@PathVariable UUID id,
										Model model) {
		model.addAttribute("eventOrder", eventOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/edit/eventOrderEditForm";
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
								 @RequestParam("notes") String notes,
								 RedirectAttributes redirectAttributes) {
		try {
			EventOrder eventOrder = eventOrderService.getById(id)
				.orElseThrow(() -> new IllegalArgumentException("Event order not found"));
			eventOrder.setClient(getOrCreateClient(clientName, phone));
			eventOrder.setEventDate(eventDate);
			eventOrder.setDeliveryAddress(deliveryAddress);
			eventOrder.setNotes(notes);
			eventOrder.setPaymentMethod(paymentMethod);
			eventOrderService.update(eventOrder, products, orderStatus, cancelReason);
			return "redirect:/services";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/reservations/edit/" + id;
		}
	}

	@GetMapping("/reservations/edit/{id}")
	public String getReservationOrderEditPage(@PathVariable UUID id,
											  Model model) {
		model.addAttribute("reservationOrder", reservationOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/edit/reservationOrderEditForm";
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
									   @RequestParam("reservationStatus") String reservationStatus,
									   @RequestParam("notes") String notes,
									   RedirectAttributes redirectAttributes) {
		try {
			ReservationOrder reservationOrder = reservationOrderService.getById(id)
				.orElseThrow(() -> new IllegalArgumentException("Reservation order not found"));
			reservationOrder.setClient(getOrCreateClient(clientName, phone));
			reservationOrder.setReservationDateTime(reservationDateTime);
			reservationOrder.setNotes(notes);
			reservationOrder.setPaymentMethod(paymentMethod);
			reservationOrderService.update(reservationOrder, products, orderStatus, cancelReason, reservationStatus);
			return "redirect:/services";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/reservations/edit/" + id;
		}
	}

	private Client getOrCreateClient(String name, String phone) {
		return clientService.getOrCreateClient(name, phone);
	}
}