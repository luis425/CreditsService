package com.nttdata.Semana01.Credits.Repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.nttdata.Semana01.Credits.Entity.CreditCard;

@Repository
public interface CreditCardRepository extends ReactiveCrudRepository<CreditCard, String>{
}

