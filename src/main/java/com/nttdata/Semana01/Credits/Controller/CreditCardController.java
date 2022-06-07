package com.nttdata.Semana01.Credits.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.Credits.DTO.Bank;
import com.nttdata.Semana01.Credits.DTO.Customer;
import com.nttdata.Semana01.Credits.DTO.CustomerType;
import com.nttdata.Semana01.Credits.Entity.CreditCard;
import com.nttdata.Semana01.Credits.Entity.Credits; 
import com.nttdata.Semana01.Credits.Service.CreditCardService;
import com.nttdata.Semana01.Credits.Service.CreditsService;
import com.nttdata.Semana01.Credits.response.CreditCardResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/creditCard")
public class CreditCardController {

	@Autowired
	CreditCardService creditCardService;
	
	@Autowired
	CreditsService creditsService;

	private String codigoValidatorCredit = "";

	@PostMapping
	public Mono<CreditCard> createCreditCard(@RequestBody CreditCard creditCard) {

		boolean validationvalue = this.validationRegisterCreditCardRequest(creditCard);

		if (validationvalue) {

			try { 
				
				var credits = this.creditsService.getAllCreditsByNumberAccount(creditCard.getCredits().getNumberCredits());

				List<Credits> listCredits = new ArrayList<>();

				credits.collectList().subscribe(listCredits::addAll); 
				
				long temporizador = (5 * 1000);
				Thread.sleep(temporizador);

				codigoValidatorCredit = this.validardorCustomer(listCredits, creditCard);

				if (codigoValidatorCredit.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"No existe Cuenta de Credito, para realizar la relacion con la CreditCard"));
				} else {
					
					creditCard.setKeyCredit(listCredits.get(0).getKeyCredit());
					creditCard.setAvailableBalanceCreditCard(listCredits.get(0).getAvailableBalanceCredit());
					creditCard.setAvailableBalanceCreditCardMaximum(listCredits.get(0).getAvailableBalanceCreditMaximum());
					creditCard.setStatusAccount(true);
					creditCard.setDateCreationCreditCard(new Date());
					return this.creditCardService.createCreditCard(creditCard);
				}

			} catch (InterruptedException e) {
				log.info(e.toString());
				Thread.currentThread().interrupt();
				return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
			}

		} else {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		}

	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteCreditCardById(@PathVariable String id) {

		try {
			return this.creditCardService.deleteCreditCard(id).map(r -> ResponseEntity.ok().<Void>build())
					.defaultIfEmpty(ResponseEntity.notFound().build());

		} catch (Exception e) {
			log.info(e.toString());
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}

	}

	@GetMapping(value = "/creditCardbynumbercreditCardResponse/{numbercreditCard}")
	public Mono<ResponseEntity<CreditCardResponse>> getAllCreditCardBynumbercreditCardResponse(@PathVariable String numbercreditCard) {

		try {

			Flux<CreditCardResponse> creditcardflux = this.creditCardService.getAllCreditCardBynumbercreditCardResponse(numbercreditCard);

			List<CreditCardResponse> list1 = new ArrayList<>();

			creditcardflux.collectList().subscribe(list1::addAll);

			long temporizador = (5 * 1000);

			Thread.sleep(temporizador);

			if (list1.isEmpty()) {
				return null;

			} else {
				return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list1.get(0)))
						.defaultIfEmpty(ResponseEntity.notFound().build());
			}

		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}
	}
	
	public boolean validationRegisterCreditCardRequest(CreditCard creditCard) {

		boolean validatorCreditCard;

		if (creditCard.getId() != null) {
			validatorCreditCard = false;
		} else if (creditCard.getNumberCreditCard() == null || creditCard.getNumberCreditCard().equals("")) {
			validatorCreditCard = false;
		} else if (creditCard.getKeyCredit() != 0) {
			validatorCreditCard = false;
		} else if (creditCard.getAvailableBalanceCreditCard() != 0) {
			validatorCreditCard = false;
		} else if (creditCard.getAvailableBalanceCreditCardMaximum() != 0) {
			validatorCreditCard = false;
		} else if (creditCard.getCredits() == null || creditCard.getCredits().getNumberCredits().equals("")) {
			validatorCreditCard = false;
		} else {
			validatorCreditCard = true;
		}

		return validatorCreditCard;
	}

	public String validardorCustomer(List<Credits> credit, CreditCard creditCard) {

		if (credit.isEmpty()) {
			
			codigoValidatorCredit = "";
		
		} else {
			
			log.info("Obtener valor para validar Id --->" + credit.get(0).getNumberCredits());
			
			codigoValidatorCredit = credit.get(0).getNumberCredits();

			// Setear Valor de Customer al Request para el registro

			creditCard.setCredits(credit.get(0));

		}

		return codigoValidatorCredit;
	}

	// Metodo para Mock

		public List<Customer> comunicationWebClientObtenerCustomerMock() {

			List<Customer> customers = new ArrayList<>();

			customers.add(new Customer("6288256a24f51675daabff60", "CP1", "PRUEBACLIENTEACTUALIZAR", "APELLIDOCLIENTE4",
					"DIRECCIONCLIENTE4", "EMAIL322@PRUEBA.COM", "2132132100", new Date(), new Date(), "213210011",
					new CustomerType(1, "Personal"), new Bank("628570778f9e833491ad8ba4", "cb1", "PRUEBABANCOACTUALIZACION",
							"PRUEBADIRECCIONACTUALIZACION")));

			log.info("Vista Customer con Dni Filtrado -->" + customers);

			return customers;

	}
}
