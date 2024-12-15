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
import java.util.ArrayList;
import java.util.List;

@Controller
public class CalendarController {

	@Autowired
	private CalendarService service;
	public CalendarController(CalendarService service) {
		this.service = service;
	}

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

	@GetMapping("/calendar/next")
	public String nextMonth(Model model, @RequestParam(value = "month") int month, @RequestParam(value = "year") int year) {
		month++;
		if (month > 12) {
			month = 1;
			year++;
		}
		return "redirect:/calendar?month=" + month + "&year=" + year;
	}


	@GetMapping("/calendar/previous")
	public String previousMonth(Model model, @RequestParam(value = "month") int month, @RequestParam(value = "year") int year) {
		month--;
		if (month < 1) {
			month = 12;
			year--;
		}
		return "redirect:/calendar?month=" + month + "&year=" + year;
	}

	@GetMapping("/calendar/new")
	public String addE(Model model) {
		return "calendar/create_event";
	}

	@PostMapping("/calendar/add")
	public String addEvent(@ModelAttribute Event event) {
		service.save(event);
		return "redirect:/calendar";
	}

	@PostMapping("/calendar/delete")
	public String deleteEvent(@RequestParam Long id) {
		service.delete(id);
		return "redirect:/calendar";
	}
	@GetMapping("/calendar/edit")
	public String editEvent(Model model, @RequestParam Long id) {
		Event event = service.findById(id);
		model.addAttribute("event", event);
		return "calendar/edit_event";
	}
	@PostMapping("/calendar/update")
	public String updateEvent(@ModelAttribute Event event) {
		service.update(event);
		return "redirect:/calendar";
	}

}
