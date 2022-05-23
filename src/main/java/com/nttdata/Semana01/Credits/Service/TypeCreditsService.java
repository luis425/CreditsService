package com.nttdata.Semana01.Credits.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.Credits.Entity.TypeCredits;
import com.nttdata.Semana01.Credits.Repository.TypeCreditsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class TypeCreditsService {

	@Autowired
	TypeCreditsRepository typeCreditsRepository;
	
	public Mono<TypeCredits> createTypeCredits(TypeCredits typeCredits) {
		return typeCreditsRepository.save(typeCredits);
	}
	
	public Mono<TypeCredits> getTypeCreditsbyId(Integer id) {
		return typeCreditsRepository.findById(id);
	}
	
	public Flux<TypeCredits> getAllTypeCredits() {
		return typeCreditsRepository.findAll();
	}
	
}
