package com.github.ikra1.coinmixerapp;

import java.util.Collection;


public class Account {
    private String address;
    private String balance;
    private Collection<Transaction> transactions;            //  List

//    public Account(String address,String balance,Collection<Transaction> transactions ) {
//	this.address=address;
//	this.balance=balance;
//	this.transactions=transactions;	
//    }
    
    public String getAddress()
    {
	return address;
    }
    public String getBalance()
    {
	return balance;
    }
    public Collection<Transaction> getTransactions()
    {
	return transactions;
    }
    
    public int getNumberOfTransactions()
    {
	return transactions.size();
    }
    
    public void setAddress(String address) {
	this.address=address;
    }
}
