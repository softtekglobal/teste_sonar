package com.softtek.testesonar.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TesteController {

	
	@Autowired
	TesteRepository testeRepository;
	
	@GetMapping()
	void teste(@RequestParam String nome) {
		testeRepository.contratosUltimos30Dias(nome);
	}
	
}
