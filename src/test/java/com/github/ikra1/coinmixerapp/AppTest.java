package com.github.ikra1.coinmixerapp;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest {
 
    CoinMixerUtil cmUtil = new CoinMixerUtil();

    /**
     * Testing multiple mix calls with different accounts<br>
     * <ul>
     * <li>moves moneys from destination addresses back to funding account "test_funding"</li>
     * <li>funds 2 deposit accounts"test_Deposit1" and "test_Deposit2"</li>
     * <li>initiate 2 mixes
     * <li>waits until  Expected final balance for House account (fee increase) equals current balance or timeout</li>
     * <li>calculates total amount received by destinations</li>
     * <li>checks if expected dispense amount equals actual balances sum across all destinations</li>
     * <ul>
     * <br><b><em>Note</b></em>:Test relies on <b>test_funding</b> address sufficiently funded.<br>
     * Please make sure balance is above total deposit amount, before conducting test
     * 
     */
    @Test
    void  testMultiMix() {
	try {
	    String fundingaddr="test_funding";
	    
	    String deposit1="test_Deposit1";
	    String deposit2="test_Deposit2";
	    
	    BigDecimal amnt_to_mix_1= new BigDecimal("49.9");
	    BigDecimal amnt_to_mix_2= new BigDecimal("0.1");
	    BigDecimal feeRate = new BigDecimal(cmUtil.getProperty("Fee_rate"));
	    //BigDecimal feeCoef = new BigDecimal("1.00").subtract(feeRate);
	    
	    Account accHouse=cmUtil.httpGetAddressInfo(cmUtil.getProperty("HouseAccount"));
	    BigDecimal curBalHouse = new BigDecimal (accHouse.getBalance());
	    BigDecimal deltaExpected=amnt_to_mix_1.add(amnt_to_mix_2).multiply(feeRate);
	   
	    BigDecimal expectedBalHouse= curBalHouse.add(deltaExpected);
	    BigDecimal expectedDistribution = amnt_to_mix_1.add(amnt_to_mix_2).subtract(deltaExpected);
	    
            System.out.println("expectedBalHouse before mix"+expectedBalHouse+"\n");
	    
	    
	    Set<String> setDest1 = new HashSet<String>(); 
	    Set<String> setDest2 = new HashSet<String>(); 

	    setDest1.add("test_1_1"); 
	    setDest1.add("test_1_2"); 
	    setDest1.add("test_1_3"); 
	    setDest1.add("test_1_4"); 
	    setDest1.add("test_1_5"); 
	    setDest1.add("test_1_6"); 

	    setDest2.add("test_2_1"); 
	    setDest2.add("test_2_2"); 
	    setDest2.add("test_2_3"); 
	    setDest2.add("test_2_4"); 
	    setDest2.add("test_2_5"); 
	    
	    // move current balances back to funding address

            for (String dest : setDest1) {
        	     if (cmUtil.isAddressUsedandBalanceAvailable(dest,false)) {
        		 Account accDest=cmUtil.httpGetAddressInfo(dest);
                        System.out.println("dest address "+accDest.getAddress()+"curBal "+accDest.getBalance()+" moving back to funding address\n"); 
                         cmUtil.httpPostTransaction(dest,fundingaddr,accDest.getBalance());
                }
            }

             for (String dest : setDest2) {
        	     if (cmUtil.isAddressUsedandBalanceAvailable(dest,false)) {
        		 Account accDest=cmUtil.httpGetAddressInfo(dest);
                        System.out.println("dest address "+accDest.getAddress()+"curBal "+accDest.getBalance()+" moving back to funding address\n");   
                        cmUtil.httpPostTransaction(dest,fundingaddr,accDest.getBalance());
                }
             }
	    //fund deposits
	    cmUtil.httpPostTransaction(fundingaddr, deposit1, amnt_to_mix_1.toString());
	    cmUtil.httpPostTransaction(fundingaddr, deposit2, amnt_to_mix_2.toString());
	    
	    cmUtil.mix(deposit1,setDest1);
	    cmUtil.mix(deposit2,setDest2);
	    
	    
	    long mostwait = Long.parseLong(cmUtil.getProperty("max_interval_dispenses"));
	    mostwait=mostwait + 20; //planned dispenses time plus some
            Awaitility.await().atMost(mostwait,TimeUnit.SECONDS)
            .pollInterval(Long.parseLong(cmUtil.getProperty("Poll_interval")),TimeUnit.SECONDS)
            .until(() -> checkHousebalance(expectedBalHouse));
            
            //Now check the math
	    BigDecimal distributedTotal = BigDecimal.ZERO;

            
            for (String dest : setDest1) {
        	Account accDest=cmUtil.httpGetAddressInfo(dest);
                System.out.println("dest address "+accDest.getAddress()+"curBal "+accDest.getBalance()+"\n");  
                distributedTotal=distributedTotal.add(new BigDecimal(accDest.getBalance()));

            }
            
            for (String dest : setDest2) {
        	Account accDest=cmUtil.httpGetAddressInfo(dest);
                System.out.println("dest address "+accDest.getAddress()+" curBal "+accDest.getBalance()+"\n");   
                distributedTotal=distributedTotal.add(new BigDecimal(accDest.getBalance()));

            }
            System.out.println("distributedTotal "+distributedTotal+" expectedDistribution "+expectedDistribution+"\n");  
            distributedTotal=distributedTotal.setScale(8,RoundingMode.HALF_UP);
            expectedDistribution=expectedDistribution.setScale(8,RoundingMode.HALF_UP);
            System.out.println("Rounded >> distributedTotal "+distributedTotal+" expectedDistribution "+expectedDistribution+"\n");  
            assertEquals(distributedTotal,expectedDistribution); 
	     
     } 
	catch (Exception ex){
	    ex.printStackTrace();
	    assertEquals(1,0);
	} finally {
	   //uninit if there is something
	}	

    }

    private boolean checkHousebalance(BigDecimal expectedBalHouse) throws Exception {

	Account accHouse=cmUtil.httpGetAddressInfo(cmUtil.getProperty("HouseAccount"));
	BigDecimal curBalHouse = new BigDecimal (accHouse.getBalance());
	curBalHouse=curBalHouse.setScale(8,RoundingMode.HALF_UP);
	
        System.out.println(new Date()+"checkHousebalance: expectedBalHouse "+expectedBalHouse+"curBalHouse "+curBalHouse+"\n");

	return (curBalHouse.equals(expectedBalHouse.setScale(8,RoundingMode.HALF_UP)));
		
    }
}
