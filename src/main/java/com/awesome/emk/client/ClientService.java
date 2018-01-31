package com.awesome.emk.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.awesome.emk.apiclasses.UserDTO;

@Service
public class ClientService {

	public UserDTO getUser(String username) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<UserDTO> user = restTemplate.getForEntity("http://localhost:8080/webservice/user/" + username, UserDTO.class);
        return user.getBody();
	}
}
