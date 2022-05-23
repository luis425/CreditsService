package com.nttdata.Semana01.Credits.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.Credits.Entity.MovementsCredits;
import com.nttdata.Semana01.Credits.Repository.MovementsCreditsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class MovementsCreditsService {

	@Autowired
	MovementsCreditsRepository movementsCreditsRepository;
	
	public Mono<MovementsCredits> createMovementsCredits(MovementsCredits movementsCredits) {
		return movementsCreditsRepository.save(movementsCredits);
	}
	
	public Flux<MovementsCredits> getAllMovementsBankAccountsbyCodeCustomer(String codeCustomer) {
		return movementsCreditsRepository.findAll().filter(x -> x.getCredits().getCustomer().getCodeCustomer().equals(codeCustomer));
	}
	
	public Flux<MovementsCredits> getAllMovementsBankAccount() {
		return movementsCreditsRepository.findAll();
	}

	public Flux<MovementsCredits> getMovementsCreditsbyNumberAccount(String numberAccount) {
		return movementsCreditsRepository.findAll().filter(x -> x.getCredits().getNumberCredits().equals(numberAccount));
	}
	
}

