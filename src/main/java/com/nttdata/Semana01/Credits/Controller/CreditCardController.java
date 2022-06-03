package com.nttdata.Semana01.Credits.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.nttdata.Semana01.Credits.Service.CreditCardService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/creditCard")
public class CreditCardController {

	@Autowired
	CreditCardService creditCardService;

	private String codigoValidatorCustomer = "";

	@PostMapping
	public Mono<CreditCard> createCreditCard(@RequestBody CreditCard creditCard) {

		boolean validationvalue = this.validationRegisterCreditCardRequest(creditCard);

		if (validationvalue) {

			try {

				List<Customer> customerList = new ArrayList<>();

				/*
				 //Descomentar para consumir Servicio de CustomerService
				  
				 // Sin mock
				  
				  Flux<Customer> customerServiceClientResponse = this.creditCardService
				  .comunicationWebClientObtenerCustomerbyDni(creditCard.getCustomer().getDniCustomer(
				  ));
				  
				  customerServiceClientResponse.collectList().subscribe(customerList::addAll);
				  
				*/

				// Con Mock

				customerList = this.comunicationWebClientObtenerCustomerMock();
				 

				long temporizador = (5 * 1000);
				Thread.sleep(temporizador);

				log.info("Obtener valor para validar Id --->" + customerList);

				codigoValidatorCustomer = this.validardorCustomer(customerList, creditCard);

				if (codigoValidatorCustomer.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Customer no existe, para realizar la relacion con la CreditCard"));
				} else {
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

	public boolean validationRegisterCreditCardRequest(CreditCard creditCard) {

		boolean validatorCreditCard;

		if (creditCard.getId() != null) {
			validatorCreditCard = false;
		} else if (creditCard.getNumberCreditCard() == null || creditCard.getNumberCreditCard().equals("")) {
			validatorCreditCard = false;
		} else if (creditCard.getKeyCredit() == 0) {
			validatorCreditCard = false;
		} else if (creditCard.getAvailableBalanceCreditCard() == 0) {
			validatorCreditCard = false;
		} else if (creditCard.getAvailableBalanceCreditCardMaximum() == 0) {
			validatorCreditCard = false;
		} else if (!creditCard.isStatusAccount()) {
			validatorCreditCard = false;
		} else if (creditCard.getCustomer() == null || creditCard.getCustomer().getDniCustomer().equals("")) {
			validatorCreditCard = false;
		} else {
			validatorCreditCard = true;
		}

		return validatorCreditCard;
	}

	public String validardorCustomer(List<Customer> customers, CreditCard creditCard) {

		if (customers.isEmpty()) {
			codigoValidatorCustomer = "";
		} else {
			codigoValidatorCustomer = customers.get(0).getDniCustomer();

			// Setear Valor de Customer al Request para el registro

			creditCard.setCustomer(customers.get(0));

		}

		return codigoValidatorCustomer;
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
