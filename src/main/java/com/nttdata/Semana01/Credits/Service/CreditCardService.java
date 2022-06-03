package com.nttdata.Semana01.Credits.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nttdata.Semana01.Credits.DTO.Customer;
import com.nttdata.Semana01.Credits.Entity.CreditCard;
import com.nttdata.Semana01.Credits.Repository.CreditCardRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class CreditCardService {

	private WebClient customerServiceClient = WebClient.builder().baseUrl("http://localhost:8081").build();
	
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
	
}