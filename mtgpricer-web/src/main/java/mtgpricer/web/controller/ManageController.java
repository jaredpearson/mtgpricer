package mtgpricer.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import mtgpricer.rip.RipRequest;
import mtgpricer.rip.RipRequestQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ManageController {
	private static final long DAY_LENGTH_MILLIS = 86400000;
	
	@Autowired
	RipRequestQueue ripRequestQueue;
	
	@RequestMapping("/manage")
	public ModelAndView showPage() {
		
		final RipRequest ripRequest = ripRequestQueue.getLatestRipRequest();
		
		final TimeZone timeZone = TimeZone.getTimeZone("PST");
		
		final ModelAndView modelAndView = new ModelAndView("manage");
		modelAndView.addObject("canCreateNewRipRequest", ripRequest == null || ripRequest.getFinishDate() != null);
		modelAndView.addObject("ripInProgress", ripRequest != null && ripRequest.getFinishDate() == null);
		modelAndView.addObject("ripInProgressId", ripRequest != null ? ripRequest.getId() : null);
		modelAndView.addObject("latestRipStartDate", ripRequest != null && ripRequest.getStartDate() != null ? formatTimestampAsNaturalLanguage(ripRequest.getStartDate(), timeZone) : null);
		modelAndView.addObject("latestRipDate", ripRequest != null && ripRequest.getFinishDate() != null ? formatTimestampAsNaturalLanguage(ripRequest.getFinishDate(), timeZone) : null);
		return modelAndView;
	}
	
	private static String formatTimestampAsNaturalLanguage(Date value, TimeZone timeZone) {
		assert value != null;
		final long lastMidnight = getLastMidnight();
		final long valueMillis = value.getTime();
		final String timeSuffix = " at " + formatTime(value, timeZone);
		if (valueMillis > lastMidnight) {
			return "Today" + timeSuffix;
		} else if (valueMillis > lastMidnight - DAY_LENGTH_MILLIS) {
			return "Yesterday" + timeSuffix;
		} else {
			final DateFormat dateFormat = new SimpleDateFormat("M/d/y");
			return dateFormat.format(value) + timeSuffix;
		}
	}
	
	private static String formatTime(Date value, TimeZone timeZone) {
		final DateFormat dateFormat = new SimpleDateFormat("h:mm a zz");
		return dateFormat.format(value);
	}
	
	private static long getLastMidnight() {
		final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
}
