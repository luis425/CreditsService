package com.nttdata.Semana01.Credits.Entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nttdata.Semana01.Credits.DTO.BankAccounts;
import com.nttdata.Semana01.Credits.DTO.Customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Document
@Data
@Builder
public class Credits {

	@Id
	private String id;

	private TypeCredits typeCredits;

	private String numberCredits;

	// Clave de cuenta - Deberia incriptarse
	private int keyCredit;

	// Monto Maximo del Credito
	private double availableBalanceCreditMaximum;

	// Monto Dispoible del Credito
	private double availableBalanceCredit;

	@JsonFormat(pattern = "dd-MM-yyyy", timezone = "GMT-05:00")
	private Date dateCreationCredit;

	// Estado
	private boolean statusAccount;

	private Customer customer;

	// Relacionar con Cuenta
	private boolean statusRelationAccount;

	// Dependiendo del Flag statusRelationAccount Validamos si se desea tener
	// relacion con una cuenta Bancaria
	private BankAccounts bankAccounts;

}
