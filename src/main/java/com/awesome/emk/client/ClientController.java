package com.awesome.emk.client;


import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;

import com.awesome.emk.apiclasses.ArticleDTO;
import com.awesome.emk.apiclasses.OrderDTO;
import com.awesome.emk.apiclasses.UserDTO;

@Controller
public class ClientController {
	
	private List<ArticleDTO> order = new ArrayList<ArticleDTO>();
	private String currentUser = ""; 
	
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException() {
        return "error";
    }
	
	@RequestMapping({"/", "/index", "/login"})
	public String index(Model model) {
		RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ArticleDTO[]> list = restTemplate.getForEntity("http://localhost:8080/webservice/articles", ArticleDTO[].class);
        model.addAttribute("list", list.getBody());		
        return "index";
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/orders")
	public String getAllOrders(Model model) {
		RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OrderDTO[]> list = restTemplate.getForEntity("http://localhost:8080/webservice/orders", OrderDTO[].class);
        model.addAttribute("list", list.getBody());		
        return "bought-order-list";
	}
	
	@RequestMapping("/order-username")
	public String getOrderByUsername(Model model, @RequestParam String username) {
		RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OrderDTO[]> list = restTemplate.getForEntity("http://localhost:8080/webservice/orders/" + username, OrderDTO[].class);
        model.addAttribute("list", list.getBody());		
        return "bought-order-list";
	}
	
	@RequestMapping(value="/confirm-order", method = RequestMethod.POST)
	public String registerOrder(@RequestParam String username) {
		if(!currentUser.equals(username)) {
			currentUser = username;
			order = new ArrayList<ArticleDTO>();
			return "index";
		}
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<List<ArticleDTO>> request = new HttpEntity<>(order);
		restTemplate.postForEntity("http://localhost:8080/webservice/register/order/" + username, request, ArticleDTO[].class);
		order = new ArrayList<ArticleDTO>();
		return "redirect:/index/"; 
	}
	
	@RequestMapping("/view-cart")
	public String viewCart(Model model, @RequestParam String username) {
		if(!currentUser.equals(username)) {
			currentUser = username;
			order = new ArrayList<ArticleDTO>();
		}
        model.addAttribute("list", order);		
        return "order-list";
	}
	
	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(value="/add-cart", method = RequestMethod.POST)
	public String addToCart(@ModelAttribute ArticleDTO articleDTO, @RequestParam String username) {
		if(!currentUser.equals(username)) {
			currentUser = username;
			order = new ArrayList<ArticleDTO>();
		}
		order.add(articleDTO);
		for(ArticleDTO item : order) {
			System.out.println("Name: " + item.getName() + "\n" + "Qnt: " + item.getStock());
		}
		return "redirect:/index/";
	}
	
	@RequestMapping(value="delete/user")
	public String deleteUser(@ModelAttribute UserDTO userDTO) {
		if(userDTO == null) {
			return null;
		}
		System.out.println(userDTO.getUsername());
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete("http://localhost:8080/webservice/delete/" + userDTO.getUsername());
		return "redirect:/admin/"; 
	}
	
	@RequestMapping(value="register/user", method = RequestMethod.POST)
	public String registerUser(@ModelAttribute UserDTO userDTO) {
		if(userDTO == null) {
			return null;
		}
		
		System.out.println(userDTO.getUsername());
		System.out.println(userDTO.getLastName());
		userDTO.setEnabled(true);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<UserDTO> request = new HttpEntity<>(userDTO);
		restTemplate.postForEntity("http://localhost:8080/webservice/register/user", request, UserDTO.class);
		return "index"; 
	}
	
	@RequestMapping(value="edit/user/", method = RequestMethod.POST)
	public String editUser(@ModelAttribute UserDTO userDTO, @RequestParam String oldName, @RequestParam String oldLastName, @RequestParam String oldPassword, @RequestParam String oldRole) {
		if(userDTO == null) {
			return null;
		}	
		if(userDTO.getUsername().equals(""))
			userDTO.setUsername(oldName);
		if(userDTO.getLastName().equals(""))
			userDTO.setLastName(oldLastName);
		if(userDTO.getPassword().equals(""))
			userDTO.setPassword(oldPassword);
		if(userDTO.getRole().equals(oldRole))
			userDTO.setRole(oldRole);
		
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<UserDTO> request = new HttpEntity<>(userDTO);
		restTemplate.put("http://localhost:8080/webservice/edit/user/" + oldName, request);
		return "redirect:/admin/"; 
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value= {"/admin/users", "/admin"}, method = RequestMethod.GET)	
	public String getAllUsers(Model model) {
		RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserDTO[]> list = restTemplate.getForEntity("http://localhost:8080/webservice/users", UserDTO[].class);
        model.addAttribute("list", list.getBody());
        return "admin-index";
	}
}
