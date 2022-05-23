package com.nttdata.Semana01.Credits.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.Credits.DTO.BankAccounts;
import com.nttdata.Semana01.Credits.DTO.Customer;
import com.nttdata.Semana01.Credits.Entity.Credits;
import com.nttdata.Semana01.Credits.Entity.TypeCredits;
import com.nttdata.Semana01.Credits.Service.CreditsService;
import com.nttdata.Semana01.Credits.Service.TypeCreditsService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/credits")
public class CreditsController {

	@Autowired
	TypeCreditsService typeCreditsService;

	@Autowired
	CreditsService creditsService;

	private String codigoValidatorCustomer;

	private Integer codigoValidatorTypeCredits;

	private String codigoValidatorNumberBankAccount;

	private WebClient bankAccountsServiceClient = WebClient.builder().baseUrl("http://localhost:8082").build();

	private WebClient customerServiceClient = WebClient.builder().baseUrl("http://localhost:8081").build();

	private static final String CREDITS_CONTACT_TO_SERVICES = "creditsContactToServices";
	
	
	@PostMapping
	@CircuitBreaker(name = CREDITS_CONTACT_TO_SERVICES, fallbackMethod = "creditsContacttoService")
	public Mono<Credits> create(@RequestBody Credits credits) throws InterruptedException {

		boolean validationvalue = this.validationRegisterRequest(credits);

		List<BankAccounts> listBankAccounts = new ArrayList<>();

		if (validationvalue) {

			List<Customer> listCustomer = new ArrayList<>();

			Mono<Customer> endpointCustomerResponse = customerServiceClient.get()
					.uri("/customer/".concat(credits.getCustomer().getCodeCustomer()))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Customer.class).log()
					.doOnError(ex -> {
						throw new RuntimeException("the exception message is - " + ex.getMessage());
					});

			endpointCustomerResponse.flux().collectList().subscribe(listCustomer::addAll);

			var typeCredits = this.typeCreditsService.getTypeCreditsbyId(credits.getTypeCredits().getId());

			List<TypeCredits> listTypeCredits = new ArrayList<>();

			typeCredits.flux().collectList().subscribe(listTypeCredits::addAll);

			long temporizador = (8 * 1000);
			Thread.sleep(temporizador);

			if (credits.isStatusRelationAccount()) {

				Mono<BankAccounts> endpointResponse = bankAccountsServiceClient.get()
						.uri("/bankAccounts/".concat(credits.getBankAccounts().getNumberAccount()))
						.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(BankAccounts.class).log()
						.doOnError(ex -> {
							throw new RuntimeException("the exception message is - " + ex.getMessage());
						});

				endpointResponse.flux().collectList().subscribe(listBankAccounts::addAll);
			}

			try {

				Thread.sleep(temporizador);

				codigoValidatorCustomer = this.validardorCustomer(listCustomer, credits);

				log.info("Validar Codigo Repetido --->" + codigoValidatorCustomer);

				codigoValidatorTypeCredits = this.validardorTypeCredits(listTypeCredits, credits);

				log.info("Obtener valor para validar Id --->" + codigoValidatorTypeCredits);

				if (codigoValidatorCustomer.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Codigo de Customer no existe"));
				}

				if (codigoValidatorTypeCredits == 0) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Id de Tipo Cuenta Bancario no existe"));
				}

				if (credits.isStatusRelationAccount()) {

					codigoValidatorNumberBankAccount = this.validardorBankAccount(listBankAccounts, credits);

					log.info("Obtener valor para validar Id --->" + codigoValidatorNumberBankAccount);

					if (codigoValidatorCustomer.equals("")) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"El Codigo que se intenta relacion a una Cuenta Bancaria no existe"));
					}

				}

				// Validar dependiendo el Tipo de Cliente

				if (credits.getTypeCredits().getId().equals(1)) {

					// Credito Personal

					if (listCustomer.get(0).getCustomertype().getId().equals(1)) {

						if (credits.isStatusRelationAccount()) {

							Flux<Credits> listFluxCredits = this.creditsService
									.getAllCreditsByCodeCustomerAndIdTypeCredits(
											credits.getCustomer().getCodeCustomer(), credits.getTypeCredits().getId());

							List<Credits> listCreditsFluxVacio = new ArrayList<>();

							listFluxCredits.collectList().subscribe(listCreditsFluxVacio::addAll);

							Thread.sleep(temporizador);

							log.info("Obtener valor para si hay registro --->" + listCreditsFluxVacio.size());

							if (listCreditsFluxVacio.isEmpty()) {

								credits.setDateCreationCredit(new Date());
								credits.setStatusAccount(true);
								credits.setStatusRelationAccount(true);
								credits.setAvailableBalanceCreditMaximum(
										listBankAccounts.get(0).getAvailableBalanceAccount());
								credits.setAvailableBalanceCredit(listBankAccounts.get(0).getAvailableBalanceAccount());

								return this.creditsService.createCredits(credits);

							} else {
								return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
										"Solo se permite un solo Credito por persona"));
							}

						} else {

							Flux<Credits> listFluxCredits = this.creditsService
									.getAllCreditsByCodeCustomerAndIdTypeCredits(
											credits.getCustomer().getCodeCustomer(), credits.getTypeCredits().getId());

							List<Credits> listCreditsFluxVacio = new ArrayList<>();

							listFluxCredits.collectList().subscribe(listCreditsFluxVacio::addAll);

							Thread.sleep(temporizador);

							log.info("Obtener valor para si hay registro --->" + listCreditsFluxVacio.size());

							if (listCreditsFluxVacio.isEmpty()) {

								credits.setDateCreationCredit(new Date());
								credits.setStatusAccount(true);
								credits.setStatusRelationAccount(false);
								credits.setBankAccounts(null);
								credits.setAvailableBalanceCredit(credits.getAvailableBalanceCreditMaximum());

								return this.creditsService.createCredits(credits);

							} else {
								return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
										"Solo se permite un solo Credito por persona"));
							}

						}
					} else {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"El cliente que se quiere asociar no es un tipo de Cliente Personal"));
					}

				} else if (listCustomer.get(0).getCustomertype().getId().equals(2)) {

					if (listCustomer.get(0).getCustomertype().getId().equals(2)) {

						if (credits.isStatusRelationAccount()) {

							credits.setDateCreationCredit(new Date());
							credits.setStatusAccount(true);
							credits.setStatusRelationAccount(true);
							credits.setAvailableBalanceCreditMaximum(
									listBankAccounts.get(0).getAvailableBalanceAccount());
							credits.setAvailableBalanceCredit(listBankAccounts.get(0).getAvailableBalanceAccount());

							return this.creditsService.createCredits(credits);

						} else {

							// Puede registrar cuantas veces quiera

							credits.setDateCreationCredit(new Date());
							credits.setStatusAccount(true);
							credits.setStatusRelationAccount(false);
							credits.setBankAccounts(null);
							credits.setAvailableBalanceCredit(credits.getAvailableBalanceCreditMaximum());

							return this.creditsService.createCredits(credits);

						}

					} else {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"El cliente que se quiere asociar no es un tipo de Cliente Empresarial"));
					}

				} else {

					if (credits.isStatusRelationAccount()) {

						credits.setDateCreationCredit(new Date());
						credits.setStatusAccount(true);
						credits.setStatusRelationAccount(true);
						credits.setAvailableBalanceCreditMaximum(listBankAccounts.get(0).getAvailableBalanceAccount());
						credits.setAvailableBalanceCredit(listBankAccounts.get(0).getAvailableBalanceAccount());

						return this.creditsService.createCredits(credits);

					} else {

						// Puede registrar cuantas veces quiera

						credits.setDateCreationCredit(new Date());
						credits.setStatusAccount(true);
						credits.setStatusRelationAccount(false);
						credits.setBankAccounts(null);
						credits.setAvailableBalanceCredit(credits.getAvailableBalanceCreditMaximum());

						return this.creditsService.createCredits(credits);

					}
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

	@GetMapping(value = "/{numberAccount}")
	public Mono<ResponseEntity<Flux<Credits>>> getBankAccountsByNumberAccount(@PathVariable String numberAccount) {
		Flux<Credits> list = this.creditsService.getAllCreditsByNumberAccount(numberAccount);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(value = "/CreditsbyCodeCustomer/{codeCustomer}")
	public Mono<ResponseEntity<Flux<Credits>>> getBankAccountsByCodeCustomer(@PathVariable String codeCustomer) {
		Flux<Credits> list = this.creditsService.getAllCreditsByCodeCustomer(codeCustomer);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	public boolean validationRegisterRequest(Credits credits) {

		boolean validatorbankAccounts;

		if (credits.isStatusRelationAccount()) {

			if (credits.getBankAccounts().getNumberAccount() == null
					|| credits.getBankAccounts().getNumberAccount().equals("")) {
				validatorbankAccounts = false;

			} else if (credits.getId() != null) {
				validatorbankAccounts = false;
			} else if (credits.getTypeCredits().getId() == null || credits.getTypeCredits().getId() == 0) {
				validatorbankAccounts = false;
			} else if (credits.getNumberCredits() == null || credits.getNumberCredits().equals("")) {
				validatorbankAccounts = false;
			} else if (credits.getKeyCredit() == 0) {
				validatorbankAccounts = false;
			} else if (credits.getAvailableBalanceCreditMaximum() != 0.00) {
				// Se agrego la validacion por el motivo que se tomara el saldo de la cuenta
				// asociada por eso no se debe enviar el saldo maximo
				validatorbankAccounts = false;
			} else if (credits.getAvailableBalanceCredit() != 0.00) {
				// Se agrego la validacion por el motivo que se tomara el saldo maximo del
				// Credito
				validatorbankAccounts = false;
			} else if (credits.getCustomer().getCodeCustomer() == null
					|| credits.getCustomer().getCodeCustomer().equals("")) {
				validatorbankAccounts = false;
			} else {
				validatorbankAccounts = true;

			}

		} else {

			if (credits.getId() != null) {
				validatorbankAccounts = false;
			} else if (credits.getTypeCredits().getId() == null || credits.getTypeCredits().getId() == 0) {
				validatorbankAccounts = false;
			} else if (credits.getNumberCredits() == null || credits.getNumberCredits().equals("")) {
				validatorbankAccounts = false;
			} else if (credits.getKeyCredit() == 0) {
				validatorbankAccounts = false;
			} else if (credits.getAvailableBalanceCreditMaximum() == 0.00) {
				validatorbankAccounts = false;
			} else if (credits.getAvailableBalanceCredit() != 0.00) {
				// Se agrego la validacion por el motivo que se tomara el saldo maximo del
				// Credito
				validatorbankAccounts = false;
			} else if (credits.getCustomer().getCodeCustomer() == null
					|| credits.getCustomer().getCodeCustomer().equals("")) {
				validatorbankAccounts = false;
			} else {
				validatorbankAccounts = true;
			}

		}

		return validatorbankAccounts;
	}

	public String validardorCustomer(List<Customer> list1, Credits credits) {

		if (list1.isEmpty()) {
			codigoValidatorCustomer = "";
		} else {
			codigoValidatorCustomer = list1.get(0).getCodeCustomer();

			credits.getCustomer().setId(list1.get(0).getId());
			credits.getCustomer().setCodeCustomer(codigoValidatorCustomer);
			credits.getCustomer().setNameCustomer(list1.get(0).getNameCustomer());
			credits.getCustomer().setLastNameCustomer(list1.get(0).getLastNameCustomer());
			credits.getCustomer().setDirectionCustomer(list1.get(0).getDirectionCustomer());
			credits.getCustomer().setEmailCustomer(list1.get(0).getEmailCustomer());
			credits.getCustomer().setPhoneNumberCustomer(list1.get(0).getPhoneNumberCustomer());
			credits.getCustomer().setDniCustomer(list1.get(0).getDniCustomer());
			credits.getCustomer().setCustomertype(list1.get(0).getCustomertype());
			credits.getCustomer().setBank(list1.get(0).getBank());
			credits.getCustomer().setBirthDateCustomer(list1.get(0).getBirthDateCustomer());
			credits.getCustomer().setRegisterDateCustomer(list1.get(0).getRegisterDateCustomer());

		}

		return codigoValidatorCustomer;
	}

	public Integer validardorTypeCredits(List<TypeCredits> list1, Credits credits) {

		if (list1.isEmpty()) {
			codigoValidatorTypeCredits = 0;
		} else {
			codigoValidatorTypeCredits = list1.get(0).getId();
			credits.getTypeCredits().setId(codigoValidatorTypeCredits);
			credits.getTypeCredits().setDescription(list1.get(0).getDescription());
		}

		return codigoValidatorTypeCredits;
	}

	public String validardorBankAccount(List<BankAccounts> list1, Credits credits) {

		if (list1.isEmpty()) {
			codigoValidatorNumberBankAccount = "";
		} else {
			codigoValidatorNumberBankAccount = list1.get(0).getNumberAccount();

			credits.getBankAccounts().setId(list1.get(0).getId());
			credits.getBankAccounts().setTypeBankAccounts(list1.get(0).getTypeBankAccounts());
			credits.getBankAccounts().setNumberAccount(codigoValidatorNumberBankAccount);
			credits.getBankAccounts().setKeyAccount(list1.get(0).getKeyAccount());
			credits.getBankAccounts().setAvailableBalanceAccount(list1.get(0).getAvailableBalanceAccount());
			credits.getBankAccounts().setDateCreationBankAccount(list1.get(0).getDateCreationBankAccount());
			credits.getBankAccounts().setDateLastBankAccount(list1.get(0).getDateLastBankAccount());
			credits.getBankAccounts().setStatusAccount(list1.get(0).isStatusAccount());
			credits.getBankAccounts().setCustomer(list1.get(0).getCustomer());

		}

		return codigoValidatorCustomer;
	}
	
	public Mono<Credits> creditsContacttoService(Throwable ex) { 
		log.info("Message ---->" + ex.getMessage());
		Credits mockServiceResponse = null;
		return Mono.just(mockServiceResponse);
	}
}
