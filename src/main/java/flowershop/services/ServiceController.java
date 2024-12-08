package flowershop.services;

import flowershop.calendar.CalendarService;
import flowershop.calendar.Event;
import flowershop.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * The `ServiceController` class handles HTTP requests related to various types of orders in the flower shop system.
 * It provides endpoints for creating, retrieving, and editing contract, event, and reservation orders.
 */
@Controller
@RequestMapping("/services")
public class ServiceController {

	private final CalendarService calendarService;
	private final EventOrderService eventOrderService;
	private final ContractOrderService contractOrderService;
	private final ReservationOrderService reservationOrderService;
	private final ProductService productService;
	private final ClientService clientService;
	private final OrderFactory orderFactory;

	/**
	 * Constructs a `ServiceController` with the specified services and order factory.
	 *
	 * @param eventOrderService       the service for managing event orders
	 * @param contractOrderService    the service for managing contract orders
	 * @param reservationOrderService the service for managing reservation orders
	 * @param productService          the service for managing products
	 * @param clientService           the service for managing clients
	 * @param orderFactory            the factory for creating orders
	 */
	public ServiceController(EventOrderService eventOrderService, ContractOrderService contractOrderService, ReservationOrderService reservationOrderService,
							 ProductService productService, ClientService clientService, OrderFactory orderFactory, CalendarService calendarService) {
		this.eventOrderService = eventOrderService;
		this.contractOrderService = contractOrderService;
		this.reservationOrderService = reservationOrderService;
		this.productService = productService;
		this.clientService = clientService;
		this.orderFactory = orderFactory;
		this.calendarService = calendarService;
	}

	/**
	 * Handles GET requests to retrieve all services.
	 *
	 * @param model the model to add attributes to
	 * @return the view name for displaying all services
	 */
	@GetMapping("")
	public String getAllServices(Model model) {
		model.addAttribute("contracts", contractOrderService.findAll());
		model.addAttribute("events", eventOrderService.findAll());
		model.addAttribute("reservations", reservationOrderService.findAll());
		return "services/services";
	}

	/**
	 * Handles GET requests to retrieve an order by its ID and type.
	 *
	 * @param type the type of the order (contracts, events, or reservations)
	 * @param id   the ID of the order
	 * @return a `ResponseEntity` containing the order if found, or an appropriate HTTP status
	 */
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

	/**
	 * Handles GET requests to display the page for creating a new order.
	 *
	 * @return the view name for the new order creation page
	 */
	@GetMapping("/create")
	public String getNewOrderPage() {
		return "services/create_service";
	}

	/**
	 * Handles GET requests to add a new product row in the order creation form.
	 *
	 * @param index the index of the new product row
	 * @param model the model to add attributes to
	 * @return the fragment name for the product row
	 */
	@GetMapping("/add-product-row")
	public String addProductRow(@RequestParam("index") int index, Model model) {
		model.addAttribute("index", index);
		model.addAttribute("products", productService.getAllProducts());
		return "fragments/product-row :: productRow";
	}

	/**
	 * Handles POST requests to create a new contract order.
	 *
	 * @param clientName        the name of the client
	 * @param contractType      the type of the contract
	 * @param frequency         the frequency of the contract
	 * @param customFrequency   the custom frequency of the contract
	 * @param customUnit        the custom unit of the contract
	 * @param startDate         the start date of the contract
	 * @param endDate           the end date of the contract
	 * @param address           the address associated with the contract
	 * @param phone             the phone number of the client
	 * @param products          a map of product IDs and their quantities
	 * @param notes             additional notes for the order
	 * @param redirectAttribute the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
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
									  @RequestParam("notes") String notes,
									  RedirectAttributes redirectAttribute) {
		try {
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$"))
				throw new IllegalArgumentException("Invalid phone number format");
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
		} catch (Exception e) {
			redirectAttribute.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/create";
		}
	}

	/**
	 * Handles POST requests to create a new event order.
	 *
	 * @param clientName        the name of the client
	 * @param eventDate         the date of the event
	 * @param phone             the phone number of the client
	 * @param deliveryAddress   the delivery address for the event
	 * @param products          a map of product IDs and their quantities
	 * @param notes             additional notes for the order
	 * @param redirectAttribute the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
	@PostMapping("/events/create")
	public String createEventOrder(@RequestParam String clientName,
								   @RequestParam("eventDate") LocalDate eventDate,
								   @RequestParam("phone") String phone,
								   @RequestParam("deliveryAddress") String deliveryAddress,
								   @RequestParam Map<String, String> products,
								   @RequestParam("notes") String notes,
								   RedirectAttributes redirectAttribute) {
		try {
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$"))
				throw new IllegalArgumentException("Invalid phone number format");
			EventOrder eventOrder = orderFactory.createEventOrder(eventDate,
				deliveryAddress, getOrCreateClient(clientName, phone), notes);
			eventOrderService.save(eventOrder, products);
			return "redirect:/services";
		} catch (Exception e) {
			redirectAttribute.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/create";
		}
	}

	/**
	 * Handles POST requests to create a new reservation order.
	 *
	 * @param clientName          the name of the client
	 * @param reservationDateTime the date and time of the reservation
	 * @param phone               the phone number of the client
	 * @param products            a map of product IDs and their quantities
	 * @param notes               additional notes for the order
	 * @param redirectAttribute   the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
	@PostMapping("/reservations/create")
	public String createReservationOrder(@RequestParam String clientName,
										 @RequestParam("reservationDateTime") LocalDateTime reservationDateTime,
										 @RequestParam("phone") String phone,
										 @RequestParam Map<String, String> products,
										 @RequestParam("notes") String notes,
										 RedirectAttributes redirectAttribute) {
		try {
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$"))
				throw new IllegalArgumentException("Invalid phone number format");
			ReservationOrder reservationOrder = orderFactory.createReservationOrder(reservationDateTime,
				getOrCreateClient(clientName, phone), notes);
			reservationOrderService.save(reservationOrder, products);
			return "redirect:/services";
		} catch (Exception e) {
			redirectAttribute.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/create";
		}
	}

	/**
	 * Handles GET requests to display the edit page for a contract order.
	 *
	 * @param id    the ID of the contract order
	 * @param model the model to add attributes to
	 * @return the view name for the contract order edit page
	 */
	@GetMapping("/contracts/edit/{id}")
	public String getContractOrderEditPage(@PathVariable UUID id,
										   Model model) {
		model.addAttribute("contractOrder", contractOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/edit/contractOrderEditForm";
	}

	/**
	 * Handles PUT requests to edit a contract order.
	 *
	 * @param id                 the ID of the contract order
	 * @param clientName         the name of the client
	 * @param contractType       the type of the contract
	 * @param frequency          the frequency of the contract
	 * @param customFrequency    the custom frequency of the contract
	 * @param customUnit         the custom unit of the contract
	 * @param startDate          the start date of the contract
	 * @param endDate            the end date of the contract
	 * @param address            the address associated with the contract
	 * @param phone              the phone number of the client
	 * @param products           a map of product IDs and their quantities
	 * @param paymentMethod      the payment method for the order
	 * @param orderStatus        the status of the order
	 * @param cancelReason       the reason for cancellation, if applicable
	 * @param notes              additional notes for the order
	 * @param redirectAttributes the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
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
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$"))
				throw new IllegalArgumentException("Invalid phone number format");
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
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/contracts/edit/" + id;
		}
	}

	/**
	 * Handles GET requests to display the edit page for an event order.
	 *
	 * @param id    the ID of the event order
	 * @param model the model to add attributes to
	 * @return the view name for the event order edit page
	 */
	@GetMapping("/events/edit/{id}")
	public String getEventOrderEditPage(@PathVariable UUID id,
										Model model) {
		model.addAttribute("eventOrder", eventOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/edit/eventOrderEditForm";
	}

	/**
	 * Handles PUT requests to edit an event order.
	 *
	 * @param id                 the ID of the event order
	 * @param clientName         the name of the client
	 * @param eventDate          the date of the event
	 * @param phone              the phone number of the client
	 * @param deliveryAddress    the delivery address for the event
	 * @param products           a map of product IDs and their quantities
	 * @param paymentMethod      the payment method for the order
	 * @param orderStatus        the status of the order
	 * @param cancelReason       the reason for cancellation, if applicable
	 * @param notes              additional notes for the order
	 * @param redirectAttributes the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
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
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$"))
				throw new IllegalArgumentException("Invalid phone number format");
			eventOrder.setClient(getOrCreateClient(clientName, phone));
			eventOrder.setEventDate(eventDate);
			eventOrder.setDeliveryAddress(deliveryAddress);
			eventOrder.setNotes(notes);
			eventOrder.setPaymentMethod(paymentMethod);
			eventOrderService.update(eventOrder, products, orderStatus, cancelReason);
			return "redirect:/services";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/events/edit/" + id;
		}
	}

	/**
	 * Handles GET requests to display the edit page for a reservation order.
	 *
	 * @param id    the ID of the reservation order
	 * @param model the model to add attributes to
	 * @return the view name for the reservation order edit page
	 */
	@GetMapping("/reservations/edit/{id}")
	public String getReservationOrderEditPage(@PathVariable UUID id,
											  Model model) {
		model.addAttribute("reservationOrder", reservationOrderService.getById(id).get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/edit/reservationOrderEditForm";
	}

	/**
	 * Handles PUT requests to edit a reservation order.
	 *
	 * @param id                  the ID of the reservation order
	 * @param clientName          the name of the client
	 * @param reservationDateTime the date and time of the reservation
	 * @param phone               the phone number of the client
	 * @param products            a map of product IDs and their quantities
	 * @param paymentMethod       the payment method for the order
	 * @param orderStatus         the status of the order
	 * @param cancelReason        the reason for cancellation, if applicable
	 * @param reservationStatus   the status of the reservation
	 * @param notes               additional notes for the order
	 * @param redirectAttributes  the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
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
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$"))
				throw new IllegalArgumentException("Invalid phone number format");
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

	/**
	 * Retrieves an existing client or creates a new one if the client does not exist.
	 *
	 * @param name  the name of the client
	 * @param phone the phone number of the client
	 * @return the existing or newly created client
	 */
	private Client getOrCreateClient(String name, String phone) {
		return clientService.getOrCreateClient(name, phone);
	}
}