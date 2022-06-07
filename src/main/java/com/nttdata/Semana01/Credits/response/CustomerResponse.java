package com.nttdata.Semana01.Credits.response;

import com.nttdata.Semana01.Credits.DTO.Bank;
import com.nttdata.Semana01.Credits.DTO.CustomerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
	 
	private String id;
	
	private String codeCustomer;
	
	private String nameCustomer;
	
	private String lastNameCustomer;
	
	private String dniCustomer;
	
	private CustomerType customertype;
	
	private Bank bank; 
	
}

