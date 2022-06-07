package com.nttdata.Semana01.Credits.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nttdata.Semana01.Credits.Entity.Credits;
import com.nttdata.Semana01.Credits.Repository.CreditsRepository;
import com.nttdata.Semana01.Credits.config.BankAccountApiProperties;
import com.nttdata.Semana01.Credits.config.CustomerApiProperties;
import com.nttdata.Semana01.Credits.response.BankAccountResponse;
import com.nttdata.Semana01.Credits.response.CustomerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class CreditsService {

	private final CustomerApiProperties customerApiProperties;
	
	private final BankAccountApiProperties bankAccountApiProperties;
	
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
	
	@SuppressWarnings("unchecked")
	public CustomerResponse comunicationWebClientCustomerObtenerCustomerbyDniResponse(String dni) { 
		String uri = customerApiProperties.getBaseUrl() + "/customer/customerbydniResponse/".concat(dni);
		RestTemplate restTemplate = new RestTemplate();
		CustomerResponse result = restTemplate.getForObject(uri, CustomerResponse.class); 
		log.info("Ver lista --->" + result);
		return result;

	}
	
	@SuppressWarnings("unchecked")
	public BankAccountResponse comunicationWebClientBankAccountObtenerBankAccountbyNumberAccountResponse(String numberAccount) { 
		String uri = bankAccountApiProperties.getBaseUrl() + "/bankAccounts/bankAccountbyNumberAccountResponse/".concat(numberAccount);
		RestTemplate restTemplate = new RestTemplate();
		BankAccountResponse result = restTemplate.getForObject(uri, BankAccountResponse.class); 
		log.info("Ver lista --->" + result);
		return result;

	}
	
}
