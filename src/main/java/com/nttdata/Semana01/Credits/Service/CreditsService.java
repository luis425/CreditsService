package com.nttdata.Semana01.Credits.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.Credits.Entity.Credits;
import com.nttdata.Semana01.Credits.Repository.CreditsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class CreditsService {

	@Autowired
	CreditsRepository creditsRepository;
	
	public Mono<Credits> createCredits(Credits credits) {
		return creditsRepository.save(credits);
	}
	
	
	public Flux<Credits> getAllCreditsByCodeCustomerAndIdTypeCredits(String codeCustomer, Integer typeCredits) {
		return creditsRepository.findAll().filter(x -> x.getCustomer().getCodeCustomer().equals(codeCustomer) && x.getTypeCredits().getId().equals(typeCredits));
	}
	
	public Flux<Credits> getAllCreditsByNumberAccount(String numberAccount) {
		return creditsRepository.findAll().filter(x -> x.getNumberCredits().equals(numberAccount));
	}
	
	public Flux<Credits> getAllCreditsByCodeCustomer(String codeCustomer) {
		return creditsRepository.findAll().filter(x -> x.getCustomer().getCodeCustomer().equals(codeCustomer));
	}
	
}
