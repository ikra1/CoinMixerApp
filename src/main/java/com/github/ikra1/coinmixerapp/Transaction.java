package com.github.ikra1.coinmixerapp;


public class Transaction {
    private String amount;
    private String fromAddress;
    private String toAddress;
    private String timestamp;
    
//    public Transaction(String amount,String fromAddress,String toAddress,String timestamp ) {
//	this.amount=amount;
//	this.fromAddress=fromAddress;
//	this.toAddress=toAddress;
//	this.timestamp=timestamp;
//   }
    public String getAmount()
    {
	return this.amount;
    }
    public String getFromAddress()
    {
	return this.fromAddress;
    }
    public String getToAddress()
    {
	return this.toAddress;
    }
    public String getTimestamp()
    {
	return this.timestamp;
    }
}
