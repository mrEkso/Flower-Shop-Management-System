package flowershop.services;

import flowershop.calendar.CalendarService;
import flowershop.calendar.Event;
import flowershop.clock.ClockService;
import flowershop.product.ProductService;
import javassist.NotFoundException;
import org.javamoney.moneta.Money;
import org.salespointframework.order.OrderStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
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
	private final ClockService clockService;

	/**
	 * Constructs a `ServiceController` with the specified services and order factory.
	 *
	 * @param eventOrderService       the service for managing event orders
	 * @param contractOrderService    the service for managing contract orders
	 * @param reservationOrderService the service for managing reservation orders
	 * @param productService          the service for managing products
	 * @param clientService           the service for managing clients
	 * @param orderFactory            the factory for creating orders
	 * @param clockService            the service for managing work hours
	 */
	public ServiceController(EventOrderService eventOrderService, ContractOrderService contractOrderService,
							 ReservationOrderService reservationOrderService, ProductService productService,
							 ClientService clientService, OrderFactory orderFactory, CalendarService calendarService,
							 ClockService clockService) {
		this.eventOrderService = eventOrderService;
		this.contractOrderService = contractOrderService;
		this.reservationOrderService = reservationOrderService;
		this.productService = productService;
		this.clientService = clientService;
		this.orderFactory = orderFactory;
		this.calendarService = calendarService;
		this.clockService = clockService;
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
	 * Handles GET requests to display the page for creating a new order.
	 *
	 * @return the view name for the new order creation page
	 */
	@GetMapping("/create")
	public String getNewOrderPage(Model model) {
		model.addAttribute("products", productService.getAllProducts());
		return "services/create_service";
	}

	/**
	 * Handles GET requests to add a new product row in the order creation form.
	 * <p>
	 * //@param index the index of the new product row
	 *
	 * @param model the model to add attributes to
	 * @return the fragment name for the product row
	 */
	@GetMapping("/add-product-row")
	public String addProductRow(Model model) {
		model.addAttribute("index", UUID.randomUUID());
		model.addAttribute("products", productService.getAllProducts());
		return "fragments/product-row :: productRow";
	}

	/**
	 * Handles GET requests to return an empty response.
	 *
	 * @return an empty string
	 */
	@GetMapping("/empty-response")
	@ResponseBody
	public String emptyResponse() {
		return "";
	}

	/**
	 * Handles GET requests to choose frequency options for a contract.
	 *
	 * @param model        the model to add attributes to
	 * @param contractType the type of the contract (One-Time or Recurring)
	 * @return the fragment name for the frequency options container
	 */
	@GetMapping("/contracts/choose-frequency-options")
	public String chooseFrequencyOptions(Model model,
										 @RequestParam(value = "contractType", required = false) String
											 contractType) {
		model.addAttribute("contractType", contractType != null ? contractType : "One-Time");
		return "Recurring".equals(contractType) ? "fragments/frequency-options :: frequencyOptionsContainer"
			: "fragments/empty-frequency-options :: empty-frequency-options";
	}

	/**
	 * Handles GET requests to choose custom frequency options for a contract.
	 *
	 * @param model           the model to add attributes to
	 * @param frequency       the frequency of the contract
	 * @param customFrequency the custom frequency of the contract
	 * @param customUnit      the custom unit of the contract
	 * @return the fragment name for the custom options container
	 */
	@GetMapping("/contracts/choose-custom-frequency-options")
	public String chooseCustomFrequencyOptions(Model model,
											   @RequestParam(value = "frequency", required = false)
											   String frequency,
											   @RequestParam(value = "customFrequency", required = false)
											   Integer customFrequency,
											   @RequestParam(value = "customUnit", required = false)
											   String customUnit
	) {
		model.addAttribute("contractType", "Recurring");
		model.addAttribute("frequency", frequency != null ? frequency : "");
		model.addAttribute("customFrequency", customFrequency);
		model.addAttribute("customUnit", customUnit);
		return "custom".equals(frequency) ? "fragments/frequency-options :: customOptionsContainer" :
			"fragments/empty-frequency-options :: empty-custom-options";
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
									  @RequestParam(value = "frequency", required = false)
									  String frequency,
									  @RequestParam(value = "customFrequency", required = false)
									  Integer customFrequency,
									  @RequestParam(value = "customUnit", required = false)
									  String customUnit,
									  @RequestParam("startDate") LocalDateTime startDate,
									  @RequestParam("endDate") LocalDateTime endDate,
									  @RequestParam("address") String address,
									  @RequestParam("phone") String phone,
									  @RequestParam Map<String, String> products,
									  @RequestParam(value = "notes", required = false) String notes,
									  @RequestParam(value = "servicePrice", defaultValue = "0")
									  int servicePrice,
									  RedirectAttributes redirectAttribute) {
		try {
			if (!clockService.isOpen()) {
				throw new IllegalArgumentException("The shop is closed");
			}
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$")) {
				throw new IllegalArgumentException("Invalid phone number format");
			}
			if (startDate.isAfter(endDate)) {
				throw new IllegalArgumentException("Start date cannot be later than end date");
			}
			if (startDate.isBefore(clockService.now())) {
				throw new IllegalArgumentException("Event date and time cannot be in the past");
			}
			ContractOrder contractOrder = orderFactory.createContractOrder(contractType, frequency,
				startDate, endDate, address, getOrCreateClient(clientName, phone), notes);
			if ("Recurring".equals(contractType)) {
				contractOrder.setFrequency(frequency);
			}
			else
			{
				contractOrder.setFrequency("One-Time");
			}
			if ("custom".equals(frequency)) {
				contractOrder.setCustomFrequency(customFrequency);
				contractOrder.setCustomUnit(customUnit);
				calendarService.removeReccuringEvent(UUID.fromString(contractOrder.getId().toString()));
				calendarService.createReccuringEvent("Contract for " + clientName, startDate, endDate, notes,
					customUnit, "contract", UUID.fromString(contractOrder.getId().toString()), customFrequency);
			}

			contractOrder.addChargeLine(Money.of(servicePrice, "EUR"), "Service Price");
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
								   @RequestParam("eventDate") LocalDateTime eventDate,
								   @RequestParam("phone") String phone,
								   @RequestParam("deliveryAddress") String deliveryAddress,
								   @RequestParam Map<String, String> products,
								   @RequestParam("notes") String notes,
								   @RequestParam(value = "deliveryPrice", defaultValue = "0")
								   int deliveryPrice,
								   RedirectAttributes redirectAttribute) {
		try {
			if (!clockService.isOpen()) {
				throw new IllegalArgumentException("The shop is closed");
			}
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$")) {
				throw new IllegalArgumentException("Invalid phone number format");
			}
			if (eventDate.isBefore(clockService.now())) {
				throw new IllegalArgumentException("Event date and time cannot be in the past");
			}
			EventOrder eventOrder = orderFactory.createEventOrder(eventDate,
				deliveryAddress, getOrCreateClient(clientName, phone), notes);
			eventOrder.addChargeLine(Money.of(deliveryPrice, "EUR"), "Delivery Price");
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
			if (!clockService.isOpen()) {
				throw new IllegalArgumentException("The shop is closed");
			}
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$")) {
				throw new IllegalArgumentException("Invalid phone number format");
			}
			if (reservationDateTime.isBefore(clockService.now())) {
				throw new IllegalArgumentException("Reservation date and time cannot be in the past");
			}
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
										   Model model,
										   RedirectAttributes redirectAttributes) {
		Optional<ContractOrder> contractOrder = contractOrderService.getById(id);
		if (contractOrder.isEmpty()) {
			redirectAttributes.addAttribute("error", "Contract order not found");
			return "redirect:/404";
		}
		model.addAttribute("contractOrder", contractOrder.get());
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
	 * @param servicePrice       the service price for the contract
	 * @param redirectAttributes the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
	@PutMapping("/contracts/edit/{id}")
	public String editContractOrder(@PathVariable UUID id,
									@RequestParam("clientName") String clientName,
									@RequestParam("contractType") String contractType,
									@RequestParam(value = "frequency", required = false) String frequency,
									@RequestParam(value = "customFrequency", required = false)
									Integer customFrequency,
									@RequestParam(value = "customUnit", required = false) String customUnit,
									@RequestParam("startDate") LocalDateTime startDate,
									@RequestParam("endDate") LocalDateTime endDate,
									@RequestParam("address") String address,
									@RequestParam("phone") String phone,
									@RequestParam Map<String, String> products,
									@RequestParam("paymentMethod") String paymentMethod,
									@RequestParam("orderStatus") String orderStatus,
									@RequestParam(value = "cancelReason", required = false) String cancelReason,
									@RequestParam(value = "notes", required = false) String notes,
									@RequestParam("servicePrice") int servicePrice,
									RedirectAttributes redirectAttributes) {
		try {
			if (!clockService.isOpen()) {
				throw new IllegalArgumentException("The shop is closed");
			}
			ContractOrder contractOrder = contractOrderService.getById(id)
				.orElseThrow(() -> new NotFoundException("Contract order not found"));
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$")) {
				throw new IllegalArgumentException("Invalid phone number format");
			}
			if (startDate.isAfter(endDate)) {
				throw new IllegalArgumentException("Start date cannot be later than end date");
			}
			contractOrder.setClient(getOrCreateClient(clientName, phone));
			if (contractOrder.getOrderStatus().equals(OrderStatus.OPEN)) {
				contractOrder.setContractType(contractType);
				contractOrder.setStartDate(startDate);
				contractOrder.setEndDate(endDate);
			}
			contractOrder.setAddress(address);
			contractOrder.setNotes(notes);
			contractOrder.setPaymentMethod(paymentMethod);
			if ("Recurring".equals(contractType)) {
				contractOrder.setFrequency(frequency);
			} else if ("custom".equals(frequency) && customFrequency != null && customUnit != null) {
				contractOrder.setCustomFrequency(customFrequency);
				contractOrder.setCustomUnit(customUnit);
			} else {
				contractOrder.setFrequency(null);
				contractOrder.setCustomFrequency(null);
				contractOrder.setCustomUnit(null);
			}
			Event event = calendarService.findEventByUUID(id);
			if (event != null) {
				if (complicatedConditionCheck(frequency)) {
					if (orderStatus.equals("CANCELED") || orderStatus.equals("COMPLETED")) {
						calendarService.removeReccuringEvent(id);
					} else {
						calendarService.removeReccuringEvent(id);
						if(customFrequency == null) {
							customFrequency = 1;
						}
						if(customUnit == null) {
							calendarService.createReccuringEvent("Contract for " +
								clientName, startDate, endDate, notes, frequency, "contract", id, customFrequency);
						}
						else{
							calendarService.createReccuringEvent("Contract for " +
								clientName, startDate, endDate, notes, customUnit, "contract", id, customFrequency);
						}
					}
				} else {
					if (orderStatus.equals("CANCELED") || orderStatus.equals("COMPLETED")) {
						calendarService.removeEvent(id);
					} else {
						event.setDate(startDate);
						calendarService.save(event);
					}
				}
			}
			contractOrderService.update(contractOrder, products, servicePrice, orderStatus, cancelReason);
			return "redirect:/services";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/contracts/edit/" + id;
		}
	}

	private boolean complicatedConditionCheck(String frequency) {
		if (frequency == null) {
			return false;
		}
		boolean out = false;
		if ((frequency.equals("weekly") || frequency.equals("monthly"))) {
			out = true;
		} else if (frequency.equals("daily") || frequency.equals("custom")) {
			out = true;
		} else {
			out = false;
		}
		return out;
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
										Model model,
										RedirectAttributes redirectAttributes) {
		Optional<EventOrder> eventOrder = eventOrderService.getById(id);
		if (eventOrder.isEmpty()) {
			redirectAttributes.addAttribute("error", "Event order not found");
			return "redirect:/404";
		}
		model.addAttribute("eventOrder", eventOrder.get());
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
	 * @param deliveryPrice      the delivery price for the event
	 * @param redirectAttributes the redirect attributes to add flash attributes to
	 * @return the redirect URL
	 */
	@PutMapping("/events/edit/{id}")
	public String editEventOrder(@PathVariable UUID id,
								 @RequestParam String clientName,
								 @RequestParam("eventDate") LocalDateTime eventDate,
								 @RequestParam("phone") String phone,
								 @RequestParam("deliveryAddress") String deliveryAddress,
								 @RequestParam Map<String, String> products,
								 @RequestParam("paymentMethod") String paymentMethod,
								 @RequestParam("orderStatus") String orderStatus,
								 @RequestParam(value = "cancelReason", required = false) String cancelReason,
								 @RequestParam("notes") String notes,
								 @RequestParam("deliveryPrice") int deliveryPrice,
								 RedirectAttributes redirectAttributes) {
		try {
			if (!clockService.isOpen()) {
				throw new IllegalArgumentException("The shop is closed");
			}
			EventOrder eventOrder = eventOrderService.getById(id)
				.orElseThrow(() -> new NotFoundException("Event order not found"));
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$")) {
				throw new IllegalArgumentException("Invalid phone number format");
			}
			eventOrder.setClient(getOrCreateClient(clientName, phone));
			eventOrder.setEventDate(eventDate);
			eventOrder.setDeliveryAddress(deliveryAddress);
			eventOrder.setNotes(notes);
			eventOrder.setPaymentMethod(paymentMethod);
			eventOrderService.update(eventOrder, products, deliveryPrice, orderStatus, cancelReason);
			Event event = calendarService.findEventByUUID(id);
			if (calendarService.findEventByUUID(id) != null) {
				if (eventOrder.getOrderStatus().name().equals("CANCELED") ||
					eventOrder.getOrderStatus().name().equals("COMPLETED")) {
					calendarService.removeEvent(id);
				} else {
					event.setDate(eventDate);
					calendarService.save(event);
				}
			}
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
											  Model model,
											  RedirectAttributes redirectAttributes) {
		Optional<ReservationOrder> reservationOrder = reservationOrderService.getById(id);
		if (reservationOrder.isEmpty()) {
			redirectAttributes.addAttribute("error", "Reservation order not found");
			return "redirect:/404";
		}
		model.addAttribute("reservationOrder", reservationOrder.get());
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
									   @RequestParam(value = "cancelReason", required = false)
									   String cancelReason,
									   @RequestParam("reservationStatus") String reservationStatus,
									   @RequestParam("notes") String notes,
									   RedirectAttributes redirectAttributes) {
		try {
			if (!clockService.isOpen()) {
				throw new IllegalArgumentException("The shop is closed");
			}
			ReservationOrder reservationOrder = reservationOrderService.getById(id)
				.orElseThrow(() -> new NotFoundException("Reservation order not found"));
			if (!phone.matches("^(\\+\\d{1,3})?\\d{9,15}$")) {
				throw new IllegalArgumentException("Invalid phone number format");
			}
			reservationOrder.setClient(getOrCreateClient(clientName, phone));
			reservationOrder.setReservationDateTime(reservationDateTime);
			reservationOrder.setNotes(notes);
			reservationOrder.setPaymentMethod(paymentMethod);
			reservationOrderService.update(reservationOrder, products, orderStatus,
				cancelReason, reservationStatus);

			Event event = calendarService.findEventByUUID(id);
			if (calendarService.findEventByUUID(id) != null) {
				if (reservationOrder.getOrderStatus().name().equals("CANCELED") ||
					reservationOrder.getOrderStatus().name().equals("COMPLETED")) {
					calendarService.removeEvent(id);
				} else {
					event.setDate(reservationDateTime);
					calendarService.save(event);
				}

			}

			return "redirect:/services";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/services/reservations/edit/" + id;
		}
	}

	/**
	 * Handles GET requests to display the view page for a contract order.
	 *
	 * @param id    the ID of the contract order
	 * @param model the model to add attributes to
	 * @return the view name for the contract order view page
	 */
	@GetMapping("/contracts/view-details/{id}")
	public String getViewContractDetails(@PathVariable UUID id,
										 Model model,
										 RedirectAttributes redirectAttributes) {
		Optional<ContractOrder> contractOrder = contractOrderService.getById(id);
		if (contractOrder.isEmpty()) {
			redirectAttributes.addAttribute("error", "Contract order not found");
			return "redirect:/404";
		}
		model.addAttribute("contractOrder", contractOrder.get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/view/viewContractDetails";
	}

	/**
	 * Handles GET requests to display the view page for an event order.
	 *
	 * @param id    the ID of the event order
	 * @param model the model to add attributes to
	 * @return the view name for the event order view page
	 */
	@GetMapping("/events/view-details/{id}")
	public String getViewEventDetails(@PathVariable UUID id,
									  Model model,
									  RedirectAttributes redirectAttributes) {
		Optional<EventOrder> eventOrder = eventOrderService.getById(id);
		if (eventOrder.isEmpty()) {
			redirectAttributes.addAttribute("error", "Event order not found");
			return "redirect:/404";
		}
		model.addAttribute("eventOrder", eventOrder.get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/view/viewEventDetails";
	}

	/**
	 * Handles GET requests to display the view page for a reservation order.
	 *
	 * @param id    the ID of the reservation order
	 * @param model the model to add attributes to
	 * @return the view name for the reservation order view page
	 */
	@GetMapping("/reservations/view-details/{id}")
	public String getViewReservationDetails(@PathVariable UUID id,
											Model model,
											RedirectAttributes redirectAttributes) {
		Optional<ReservationOrder> reservationOrder = reservationOrderService.getById(id);
		if (reservationOrder.isEmpty()) {
			redirectAttributes.addAttribute("error", "Reservation order not found");
			return "redirect:/404";
		}
		model.addAttribute("reservationOrder", reservationOrder.get());
		model.addAttribute("products", productService.getAllProducts());
		return "services/view/viewReservationDetails";
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