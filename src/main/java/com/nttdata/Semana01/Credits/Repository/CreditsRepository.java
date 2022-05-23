package com.nttdata.Semana01.Credits.Repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.nttdata.Semana01.Credits.Entity.Credits;

@Repository
public interface CreditsRepository extends ReactiveCrudRepository<Credits, String>{

}