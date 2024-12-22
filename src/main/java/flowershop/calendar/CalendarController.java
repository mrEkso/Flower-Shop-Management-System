package flowershop.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller class for managing calendar-related functionality.
 * Handles requests for viewing, navigating, creating, editing, and deleting calendar events.
 */
@Controller
public class CalendarController {
	//Dependecies injection
	@Autowired
	private CalendarService service;
	public CalendarController(CalendarService service) {
		this.service = service;
	}

	/**
	 * Displays the main calendar page for the given month and year.
	 * If no month or year is specified, defaults to the current month and year.
	 *
	 * @param model the Model object for adding attributes to the view
	 * @param month the month to display
	 * @param year the year to display (optional)
	 * @return the name of the calendar view template
	 */
	@GetMapping("/calendar")
	public String showCalendar(Model model,
							   @RequestParam(value = "month", required = false) Integer month,
							   @RequestParam(value = "year", required = false) Integer year) {

		if (month == null || year == null) {
			LocalDate today = LocalDate.now();
			month = today.getMonthValue();
			year = today.getYear();
		}

		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

		LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
		LocalDateTime endOfMonth = lastDayOfMonth.atTime(23, 59, 59);

		List<Event> events = service.findAllByDateBetween(startOfMonth, endOfMonth);

		List<CalendarDay> calendarDays = service.generateCalendarDays(firstDayOfMonth, events);

		model.addAttribute("calendarDays", calendarDays);
		model.addAttribute("currentMonth", month);
		model.addAttribute("currentYear", year);

		return "calendar/calendar";
	}


	/**
	 * Redirects to the calendar view of the next month.
	 *
	 * @param model the Model object for adding attributes to the view
	 * @param month the current month
	 * @param year the current year
	 * @return a redirect URL to the next month's view
	 */
	@GetMapping("/calendar/next")
	public String nextMonth(Model model, @RequestParam(value = "month") int month, @RequestParam(value = "year") int year) {
		month++;
		if (month > 12) {
			month = 1;
			year++;
		}
		return "redirect:/calendar?month=" + month + "&year=" + year;
	}

	/**
	 * Redirects to the calendar view of the next month.
	 * @param model the Model object for adding attributes to the view
	 * @param month the current month
	 * @param year the current year
	 * @return a redirect URL to the previous month's view
	 */
	@GetMapping("/calendar/previous")
	public String previousMonth(Model model, @RequestParam(value = "month") int month, @RequestParam(value = "year") int year) {
		month--;
		if (month < 1) {
			month = 12;
			year--;
		}
		return "redirect:/calendar?month=" + month + "&year=" + year;
	}

	/**
	 * Handles the request to display the "create event" page for the calendar.
	 *
	 * @param model the model object used to pass data between the controller and the view
	 * @return the name of the view template for creating a new event
	 */
	@GetMapping("/calendar/new")
	public String addE(Model model) {
		return "calendar/create_event";
	}

	//Sends the request to add a new Event
	@PostMapping("/calendar/add")
	public String addEvent(@ModelAttribute Event event) {
		event.setType("regular");
		service.save(event);
		return "redirect:/calendar";
	}
	//Deletes an existing event from mvc and service
	@PostMapping("/calendar/delete")
	public String deleteEvent(@RequestParam Long id) {
		service.delete(id);
		return "redirect:/calendar";
	}

	/**
	 * Handles the HTTP GET request to edit an existing event.
	 * Retrieves the event by its ID and adds it to the model
	 * for rendering the edit event page.
	 *
	 * @param model the model object used to pass attributes to the view
	 * @param id the unique identifier of the event to be edited
	 * @return the name of the view template to render the edit event page
	 */
	@GetMapping("/calendar/edit")
	public String editEvent(Model model, @RequestParam Long id) {
		Event event = service.findById(id);
		model.addAttribute("event", event);
		return "calendar/edit_event";
	}

	@GetMapping("/calendar/view")
	public String viewEvent(Model model, @RequestParam Long id) {
		Event event = service.findById(id);
		model.addAttribute("event", event);
		return "calendar/view_event";
	}

	//Updates a given event on click
	@PostMapping("/calendar/update")
	public String updateEvent(@ModelAttribute Event event) {
		service.update(event);
		return "redirect:/calendar";
	}

}
