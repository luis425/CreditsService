package com.nttdata.Semana01.Credits.Entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat; 
import com.nttdata.Semana01.Credits.DTO.Customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@AllArgsConstructor
@Document
@Data
@Builder
public class CreditCard {

	@Id
	private String id; 

	private String numberCreditCard;

	// Clave de cuenta - Deberia incriptarse
	private int keyCredit;

	// Monto Maximo del Credito
	private double availableBalanceCreditCardMaximum;

	// Monto Dispoible del Credito
	private double availableBalanceCreditCard;

	@JsonFormat(pattern = "dd-MM-yyyy", timezone = "GMT-05:00")
	private Date dateCreationCreditCard;

	// Estado
	private boolean statusAccount;

	private Customer customer; 
	
}
