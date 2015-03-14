package mtgpricer.web.controller;

import java.security.Principal;

import mtgpricer.web.services.PasswordChangeResult;
import mtgpricer.web.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;

@Controller
public class ChangePasswordController {
	private final UserService userService;
	
	@Autowired
	public ChangePasswordController(UserService userService) {
		this.userService = userService;
	}
	
	@RequestMapping(value={"/settings", "/settings/changePassword"}, method=RequestMethod.GET)
	public ModelAndView showForm() throws Exception {
		return createChangePasswordFormModelAndView();
	}
	
	@RequestMapping(value="/settings/changePassword", method=RequestMethod.POST)
	public ModelAndView postForm(
			Principal userPrincipal,
			@RequestParam(value="currentPassword", required=false) String password, 
			@RequestParam(value="newPassword", required=false) String newPassword, 
			@RequestParam(value="confirmNewPassword", required=false) String confirmPassword) throws Exception {
		
		// TODO: validation of the form should return all messages back at once, instead of one at a time
		if (Strings.isNullOrEmpty(password)) {
			final ModelAndView modelAndView = createChangePasswordFormModelAndView();
			modelAndView.addObject("formError", "Not all required fields are specified");
			modelAndView.addObject("missingPassword", true);
			return modelAndView;
		}
		if (Strings.isNullOrEmpty(newPassword)) {
			final ModelAndView modelAndView = createChangePasswordFormModelAndView();
			modelAndView.addObject("formError", "Not all required fields are specified");
			modelAndView.addObject("missingNewPassword", true);
			return modelAndView;
		}
		if (Strings.isNullOrEmpty(confirmPassword)) {
			final ModelAndView modelAndView = createChangePasswordFormModelAndView();
			modelAndView.addObject("formError", "Not all required fields are specified");
			modelAndView.addObject("missingConfirmPassword", true);
			return modelAndView;
		}
		
		final PasswordChangeResult changeResult = userService.changePassword(userPrincipal.getName(), password, newPassword, confirmPassword);
		
		if (changeResult.isSuccessful()) {
			final ModelAndView modelAndView = createChangePasswordFormModelAndView();
			modelAndView.addObject("success", true);
			return modelAndView;
		} else {
			final ModelAndView modelAndView = createChangePasswordFormModelAndView();
			modelAndView.addObject("formError", changeResult.getMessage());
			return modelAndView;
		}
	}
	
	private ModelAndView createChangePasswordFormModelAndView() {
		final ModelAndView modelAndView = new ModelAndView("settings/changePassword");
		modelAndView.addObject("postUrl", "/settings/changePassword");
		return modelAndView;
	}
	
}
