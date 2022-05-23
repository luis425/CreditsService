package com.nttdata.Semana01.Credits.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.Credits.Entity.Credits;
import com.nttdata.Semana01.Credits.Entity.MovementsCredits;
import com.nttdata.Semana01.Credits.Service.CreditsService;
import com.nttdata.Semana01.Credits.Service.MovementsCreditsService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/movementsCredits")
public class MovementsCreditsController {
	
	@Autowired
	CreditsService creditsService;

	@Autowired
	MovementsCreditsService movementsCreditService;

	private String codigoValidatorCredit;

	@PostMapping(value = "/pay")
	public Mono<MovementsCredits> Pay(@RequestBody MovementsCredits movementsCredits) {

		boolean validationvalue = this.validationRegisterRequest(movementsCredits);

		if (validationvalue) {

			try {

				Flux<Credits> creditList = this.creditsService
						.getAllCreditsByNumberAccount(movementsCredits.getCredits().getNumberCredits());

				List<Credits> list1 = new ArrayList<>();

				creditList.collectList().subscribe(list1::addAll);

				long temporizador = (8 * 1000);
				Thread.sleep(temporizador);

				codigoValidatorCredit = this.validardor(list1, movementsCredits);

				log.info("Verificar lista de Banco -->" + codigoValidatorCredit);

				if (codigoValidatorCredit.equals("")) {

					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Numero de Credito no existe"));

				} else {

					if (list1.get(0).getKeyCredit() == movementsCredits.getCredits().getKeyCredit()) {

						if (movementsCredits.getAmount() > list1.get(0).getAvailableBalanceCreditMaximum()) {

							return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
									"Excede su limite de Credito"));
						} else {

							log.info("Verificar Valor Boolean -->" + list1.get(0).isStatusAccount());

							Double descuento = list1.get(0).getAvailableBalanceCredit() - movementsCredits.getAmount();

							list1.get(list1.size() - 1).setAvailableBalanceCredit(descuento);

							Credits updateBankAccounts = this.validationUpdateCreditsRequest(list1,
									movementsCredits.getCredits());

							this.creditsService.createCredits(updateBankAccounts);

							movementsCredits.setMovementsCreditCode(UUID.randomUUID().toString());

							return this.movementsCreditService.createMovementsCredits(movementsCredits);
						}

					} else {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"Clave de Retiro es incorrecto"));
					}

				}

			}	catch (InterruptedException e) {
				log.info(e.toString());
				Thread.currentThread().interrupt();
				return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
			}


		} else {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		}

	}

	@GetMapping(value = "/MovementsCreditsbyCodeCustomer/{codeCustomer}")
	public Mono<ResponseEntity<Flux<MovementsCredits>>> getMovementsCreditsbyCodeCustomer(
			@PathVariable String codeCustomer) {
		Flux<MovementsCredits> list = this.movementsCreditService
				.getAllMovementsBankAccountsbyCodeCustomer(codeCustomer);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping
	public Mono<ResponseEntity<Flux<MovementsCredits>>> getAllMovementsCredits() {
		Flux<MovementsCredits> list = this.movementsCreditService.getAllMovementsBankAccount();
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list));
	}

	@GetMapping(value = "/MovementsCreditsbyNumberAccount/{numberAccount}")
	public Mono<ResponseEntity<Flux<MovementsCredits>>> getMovementsCreditsbyNumberAccount(
			@PathVariable String numberAccount) {
		Flux<MovementsCredits> list = this.movementsCreditService.getMovementsCreditsbyNumberAccount(numberAccount);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	public boolean validationRegisterRequest(MovementsCredits movCredits) {

		boolean validator;

		if (movCredits.getCredits().getNumberCredits() == null || movCredits.getCredits().getNumberCredits().equals("")) {
			validator = false;
		} else if (movCredits.getAmount() == 0.00) {
			validator = false;
		} else if (movCredits.getCredits().getKeyCredit() == 0) {
			validator = false;
		} else if (movCredits.getDescription() == null || movCredits.getDescription().equals("")) {
			validator = false;
		} else {
			validator = true;
		}

		return validator;
	}

	public String validardor(List<Credits> list1, MovementsCredits movementsCredits) {

		if (list1.isEmpty()) {
			codigoValidatorCredit = "";
		} else {
			codigoValidatorCredit = list1.get(list1.size() - 1).getNumberCredits();

			movementsCredits.getCredits().setId(list1.get(0).getId());
			movementsCredits.getCredits().setTypeCredits(list1.get(0).getTypeCredits());
			movementsCredits.getCredits().setNumberCredits(codigoValidatorCredit);
			movementsCredits.getCredits()
					.setAvailableBalanceCreditMaximum(list1.get(0).getAvailableBalanceCreditMaximum());
			movementsCredits.getCredits().setAvailableBalanceCredit(list1.get(0).getAvailableBalanceCredit());
			movementsCredits.getCredits().setDateCreationCredit(list1.get(0).getDateCreationCredit());
			movementsCredits.getCredits().setCustomer(list1.get(0).getCustomer());
			movementsCredits.getCredits().setBankAccounts(list1.get(0).getBankAccounts());
			movementsCredits.getCredits().setStatusAccount(list1.get(0).isStatusAccount());
			movementsCredits.getCredits().setStatusRelationAccount(list1.get(0).isStatusRelationAccount());

		}

		return codigoValidatorCredit;
	}

	public Credits validationUpdateCreditsRequest(List<Credits> list1, Credits credits) {

		credits.setId(list1.get(0).getId());
		credits.setTypeCredits(list1.get(0).getTypeCredits());
		credits.setNumberCredits(list1.get(0).getNumberCredits());
		credits.setKeyCredit(list1.get(0).getKeyCredit());
		credits.setAvailableBalanceCreditMaximum(list1.get(0).getAvailableBalanceCreditMaximum());
		credits.setAvailableBalanceCredit(list1.get(0).getAvailableBalanceCredit());
		credits.setDateCreationCredit(list1.get(0).getDateCreationCredit());
		credits.setCustomer(list1.get(0).getCustomer());
		credits.setBankAccounts(list1.get(0).getBankAccounts());

		return credits;
	}
}
