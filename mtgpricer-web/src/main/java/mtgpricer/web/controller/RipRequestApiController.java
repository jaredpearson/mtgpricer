package mtgpricer.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import mtgpricer.rip.RipRequest;
import mtgpricer.rip.RipRequestQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Controller for working with {@link RipRequest} instances
 * @author jared.pearson
 */
@Controller
public class RipRequestApiController {
	
	@Autowired
	RipRequestQueue ripRequestQueue;
	
	@Autowired
	Gson gson;
	
	@RequestMapping(value = "/api/ripRequest", method = RequestMethod.POST)
	@ResponseBody
	public String requestRip(HttpServletResponse response) throws IOException {
		final long id = ripRequestQueue.enqueue();
		response.setContentType("application/json");
		
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", id);
		return gson.toJson(jsonObject);
	}
	
	@RequestMapping(value = "/api/ripRequest/{id}", method = RequestMethod.GET) 
	@ResponseBody
	public String getRipById(HttpServletResponse response, @PathVariable long id) throws IOException {

		final RipRequest ripRequest = ripRequestQueue.getRipRequestById(id);
		if (ripRequest == null) {
			response.sendError(404);
			return null;
		}
		
		response.setContentType("application/json");
		
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", ripRequest.getId());
		jsonObject.addProperty("startDate", (ripRequest.getStartDate() != null) ? ripRequest.getStartDate().getTime() : null);
		jsonObject.addProperty("finishDate", (ripRequest.getFinishDate() != null) ? ripRequest.getFinishDate().getTime() : null);
		return gson.toJson(jsonObject);
	}
	
}
