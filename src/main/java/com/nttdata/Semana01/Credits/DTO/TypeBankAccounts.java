package com.nttdata.Semana01.Credits.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder; 
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TypeBankAccounts { 
	
	private Integer id;
	 
	private String description;
	
	// Manejo Por porcentaje 
	private Integer commission;
	
	// Limite maximo
	private Integer maximumLimit;
	
	
}
