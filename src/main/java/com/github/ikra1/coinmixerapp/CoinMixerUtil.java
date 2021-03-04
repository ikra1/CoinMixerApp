package com.github.ikra1.coinmixerapp;


import java.io.IOException;
import java.io.InputStream;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import java.util.Properties;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


import org.apache.http.NameValuePair;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * CoinMixerUtil
 *
 * Class is engine class for CoinMixer applications
 * 
 * 
 */
public class CoinMixerUtil {
    
    private Properties prop=null;
    private ObjectMapper mapper = new ObjectMapper();

    private PoolingHttpClientConnectionManager cm = null;
    private CloseableHttpClient httpClient = null;
    private Timer tm = null;
    private Set<String> setUsedAddrs=null;
 
    /**
     * CoinMixerUtil constructor
     *
     * Initializes PoolingHttpClientConnectionManager
     * CloseableHttpClient
     * Timer
     * used addresses set
     * 
     */
    public CoinMixerUtil()  {
	
	cm = new PoolingHttpClientConnectionManager(); //to close at the end
	cm.setMaxTotal(20);
	cm.setDefaultMaxPerRoute(6);
	httpClient = HttpClients.custom().setConnectionManager(cm).build();
	tm = new Timer();
	setUsedAddrs=new HashSet<String>(); 

    }
 
    /**
     * clears resources used
     * 
     */    
    public void clear() {
	if (cm!=null) cm.close();
	if (tm!=null) tm.cancel();
    }
    /**
     * gets property value
     * 
     */
    protected String getProperty(String key) throws Exception {	
	//prop initialization singleton
	if (this.prop==null) {
    	InputStream is = null;
	    try {	    
        	this.prop = new Properties();
        	is = this.getClass().getResourceAsStream("/application.properties");
        	prop.load(is);
        	} catch (Exception ex) {
        	    ex.printStackTrace();
        	    throw ex;
        	}
        	finally {
        	    if (is!=null) is.close();
        	}
	}
    
	return this.prop.getProperty(key);
    }
    /**
     * makes http GET call to Jobcoin API adresses
     * 
     * @param address JobCoin address
     * 
     * @return Account object, which contains address info and all transactions for this address
     * 
     */   
    public Account httpGetAddressInfo(String address) throws Exception {
	HttpGet httpGet = new HttpGet(getProperty("apiBaseUrl")+"/addresses/"+address);
	CloseableHttpResponse  response = null;
	Account acc = null;
	try {
	    response = httpClient.execute(httpGet);
	    if (response.getStatusLine().getStatusCode() == 200) {
		//Jackson convert to POJO
		      acc = mapper.readValue(response.getEntity().getContent(), Account.class);
		      acc.setAddress(address);
	    }
	    else {
		// No bad return codes for GET
	    }		
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
	finally {
	    if (response!=null) response.close();
	}
	return acc;
    }
    /**
     * makes http POST call to Jobcoin API transactions
     * 
     * @param fromAddress JobCoin address from which JobCoins are moving
     * @param toAddress JobCoin address to which JobCoins are moving
     * @param amount JobCoin value of transaction
     * 
     * @return <b>true</b> -transaction succeed, <b>false</b> - transaction failed
     * 
     */       
    public boolean httpPostTransaction(String fromAddress,String toAddress,String amount ) throws Exception {
	final HttpPost httpPost = new HttpPost(getProperty("apiBaseUrl")+"/transactions");
	CloseableHttpResponse response = null;
	boolean bRet=false;
	try {
	    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	    nvps.add(new BasicNameValuePair("fromAddress", fromAddress));
	    nvps.add(new BasicNameValuePair("toAddress", toAddress));
	    nvps.add(new BasicNameValuePair("amount", amount));
	    httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

	} catch (UnsupportedEncodingException ex) {
	    ex.printStackTrace();
	    }

	try {
	    response = httpClient.execute(httpPost);
	    if (response.getStatusLine().getStatusCode() == 200) {
		bRet=true;
	    }
	    else if (response.getStatusLine().getStatusCode() == 422) {
		// bad return codes for POST
		 System.out.println("Transaction failed:  Unprocessable Entity");	    
	    }		

	} catch (IOException ex) {
	    ex.printStackTrace();
	    }
	finally {
	    if (response!=null) response.close();
	}

	return bRet;
    }
    /**
     * validates destination addresses
     * 
     * @param destAccounts set of JobCoin addresses 
     * 
     * @return <b>true</b> - addresses were not used before, <b>false</b> - at least one of them were used before
     *    
     */       

    public boolean validateDestAccounts(Set<String> destAccounts) throws Exception {
	boolean bValid=true;	
	for(String address : destAccounts){
	    if (isAddressUsed(address,true)) {
		bValid=false;
		System.out.println(address + " was aleady used");
	    }
	}	
	return bValid;
    }
    /**
     * checks if address was used before
     * 
     * @param address JobCoin addresses 
     * @param checkMemory  indicates if check in memory for accumulated during session used addresses is needed 
     * 
     * @return boolean <b>true</b> - addresses were not used before, <b>false</b> - at least one of them were used before
     *    
     */
    public boolean isAddressUsed(String address,boolean checkMemory) throws Exception {
	if (checkMemory && setUsedAddrs.contains(address)) return true;
	Account acc=httpGetAddressInfo(address);
	return (acc.getNumberOfTransactions()>0);
    }
    /**
     * checks if address was used before and balance is greater than zero
     * 
     * @param address JobCoin address 
     * @param checkMemory  indicates if check in memory for accumulated during session used addresses is needed 
     * 
     * @return <b>true</b> - address were used before and balance is greater than zero, 
     * <b>false</b> - otherwise
     *    
     */
    public boolean isAddressUsedandBalanceAvailable(String address,boolean checkMemory) throws Exception {
	if (checkMemory && setUsedAddrs.contains(address)) return true;
	 Account acc=httpGetAddressInfo(address);
	 BigDecimal balance = new  BigDecimal(acc.getBalance());		 
	return ((acc.getNumberOfTransactions()>0) && !balance.equals(BigDecimal.ZERO));
    }    
    /**
     * <ul>
     * <li>takes entire available balance from deposit account and moves it to house account</li>
     * <li>calculates fee coefficient and adjust dispense balance</li>
     * <li>randomizes amount and time intervals for JobCoin dispenses</li>
     * <li>schedules  dispense tasks for all destination accounts</li>
     * </ul>
     * 
     * @param depositAddress JobCoin address the source for mixing
     * @param destAccounts  set of destination addresses for mixing 
     * 
     *    
     */   
    public void mix(String depositAddress, Set<String> destAccounts) throws Exception {
	System.out.println("Start mixing "+ depositAddress +" "+ new Date());
	//Save accounts to used set in memory
	setUsedAddrs.add(depositAddress);
	setUsedAddrs.addAll(destAccounts);
	
	 Account acc=httpGetAddressInfo(depositAddress);
	 BigDecimal balanceTot = new  BigDecimal(acc.getBalance());	 
	 // Move Total balance to house account
	if (httpPostTransaction(depositAddress,getProperty("HouseAccount"),acc.getBalance())) {
	    //take fee out of balance to dispense
	    BigDecimal feeRate = new BigDecimal(getProperty("Fee_rate"));
	    BigDecimal feeCoef = new BigDecimal("1.00").subtract(feeRate);;	    
	    BigDecimal balanceTotDispense = balanceTot.multiply(feeCoef);
	    //Now we need to randomize amount and time and schedule dispense for each target
	    int i = 0;
	    Randomizer rd = new Randomizer();
	    BigDecimal currDispenseAmount;
	    int maxInterval=Integer.parseInt(getProperty("max_interval_dispenses")) * 1000; //from seconds to miliseconds
		for(String address : destAccounts){		    
		    i++;
		    if (i==destAccounts.size()) {
			// We need to dispense the rest of balanceTotDispense
			currDispenseAmount=balanceTotDispense;
		    }
		    else {
			BigDecimal brnd=rd.getRandomBigDecimal(0.01, 0.99);
			currDispenseAmount = balanceTotDispense.multiply(brnd);
		    }
		    int delay_milisec=rd.getRandomInt(maxInterval);
		    DispenseTask dt = new DispenseTask(currDispenseAmount,getProperty("HouseAccount"),address);		    
		    tm.schedule(dt, delay_milisec);
		    balanceTotDispense=balanceTotDispense.subtract(currDispenseAmount);
		}
	}else {
	    // Transaction failed - no dispense needed
	    System.out.println("Moving balance from "+ depositAddress +" to house account failed");	    
	}		
    }
    
    private class DispenseTask extends TimerTask{
	    
	    private BigDecimal amount;
	    String fromAddress;
	    String toAddress;
	    
	    public DispenseTask(BigDecimal amount,String fromAddress,String toAddress) {
		this.amount=amount;
		this.fromAddress=fromAddress;
		this.toAddress=toAddress;	
	    }

	    public void run() {
	        try {
		    if (httpPostTransaction(this.fromAddress,this.toAddress,amount.toString())) {
		        //System.out.println("DispenseTask completed "+new Date()+" to address"+this.toAddress+" amount="+amount.toString());    
		    }
		} catch (Exception e) {		    
		    e.printStackTrace();
		}
	    }
	}

}