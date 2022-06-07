package com.nttdata.Semana01.Credits.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.nttdata.Semana01.Credits.DTO.Customer;
import com.nttdata.Semana01.Credits.Entity.CreditCard;
import com.nttdata.Semana01.Credits.Repository.CreditCardRepository;
import com.nttdata.Semana01.Credits.config.CustomerApiProperties;
import com.nttdata.Semana01.Credits.response.CreditCardResponse;
import com.nttdata.Semana01.Credits.response.CustomerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service  
public class CreditCardService {

	private WebClient customerServiceClient = WebClient.builder().baseUrl("http://localhost:8081").build();
	
	private final CustomerApiProperties customerApiProperties;
	
	@Autowired
	CreditCardRepository creditCardRepository;
	
	public Mono<CreditCard> createCreditCard(CreditCard debitCard) {
		return creditCardRepository.save(debitCard);
	}
	
	public Mono<CreditCard> getCreditCardbyId(String id) {
		return creditCardRepository.findById(id);
	}
	
	public Flux<CreditCard> getAllDebitCard() {
		return creditCardRepository.findAll();
	}
	
	public Mono<CreditCard> deleteCreditCard(String id) {
			return creditCardRepository.findById(id).flatMap(existsCreditCard -> creditCardRepository
					.delete(existsCreditCard).then(Mono.just(existsCreditCard)));
	}
	
	public Flux<Customer> comunicationWebClientObtenerCustomerbyDni(String dni) throws InterruptedException {

		Flux<Customer> customerServiceClientResponse = customerServiceClient.get()
				.uri("/customer/customerbydni/".concat(dni)).accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToFlux(Customer.class).log().doOnError(ex -> {
					throw new RuntimeException("the exception message is - " + ex.getMessage());
				});
		long temporizador = (5 * 1000);
		Thread.sleep(temporizador);
		
		return customerServiceClientResponse;

	}

	@SuppressWarnings("unchecked")
	public CustomerResponse comunicationWebClientObtenerCustomerbyDniResponse(String dni) throws InterruptedException {

		String uri = customerApiProperties.getBaseUrl() + "/customer/customerbydniResponse/".concat(dni);
		RestTemplate restTemplate = new RestTemplate();
		CustomerResponse result = restTemplate.getForObject(uri, CustomerResponse.class); 
		log.info("Ver lista --->" + result);
		return result;

	}
	
	public Flux<CreditCardResponse> getAllCreditCardBynumbercreditCardResponse(String numbercreditCard) {
		return creditCardRepository.findAll().filter(x -> x.getNumberCreditCard().equals(numbercreditCard))
			   .map(creditCard -> CreditCardResponse.builder()
					.id(creditCard.getId())
					.numberCreditCard(creditCard.getNumberCreditCard())
					.keyCredit(creditCard.getKeyCredit())
					.availableBalanceCreditCard(creditCard.getAvailableBalanceCreditCard())
					.availableBalanceCreditCardMaximum(creditCard.getAvailableBalanceCreditCardMaximum())
					.statusAccount(creditCard.isStatusAccount())
					.credits(creditCard.getCredits())
					.build());
	}
}