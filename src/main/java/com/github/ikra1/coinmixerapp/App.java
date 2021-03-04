package com.github.ikra1.coinmixerapp;


import java.util.Arrays;

import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.awaitility.Awaitility;

/**
 * App
 * 
 * Main class for CoinMixerApp console application<br>
 * 
 *  Configuration parameters reside application.properties<br>
 *  
 *  Prompts to input comma delimited destination addresses<br>
 *  validate destination addresses<br>
 *  generates and validates unique deposit address<br>
 *  prompts user to deposit JobCoins to deposit address<br>
 *  waits for balance to become available or timeout<br>
 *  notifies user that mixing started<br>
 *  
 * The following properties in <b>application.properties</b> are supported:<br>
 * 
 * <b>HouseAccount</b> - House account through which coins flow for all mixes. It accumulates calculated fees<br>
 * <b>apiBaseUrl</b> - URL address for Jobcoin API<br>
 * 
 * <b>Poll_duration_max</b> - max duration time for waiting for deposit to be received in mixer generated deposit address (Seconds)<br>
 * <b>Poll_interval</b> - intervals between deposit address checks (Seconds)<br>
 * 
 * <b>max_interval_dispenses</b> - max interval between mix initiation and funds delivery to destination (Seconds)<br>
 * <b>Fee_rate</b> - fee rate for fee calculation (0 - no fees, 0.05 - 5 percent of deposit)<br>
 * 
 * <b><em>Note:</em></b> you can quit application by typing <b>'quit'</b>
 */

public final class App {

    /**
     * 
     * @param args program doesn't take parameters.
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        CoinMixerUtil cmUtil = new CoinMixerUtil();

	try {
    	    System.out.println("<CoinMixer>\n");
    	    while (true) {
    		System.out.println("Please enter a comma-separated list of new, unused Jobcoin addresses where your mixed Jobcoins will be sent.");
    		System.out.println("Please enter 'quit' to exit.");
                String consoleinput = scan.nextLine();
                if (consoleinput.isEmpty()) System.out.println("You must specify empty addresses to mix into!\n");
              
                if ("quit".equalsIgnoreCase(consoleinput))
    		throw new Exception("quit");
                
                Set<String> destAccounts = Arrays.stream(consoleinput.split(",")).map(String::trim).collect(Collectors.toSet());

                if (destAccounts.size() < 1) { 
            	 System.out.println("You must specify empty addresses to mix into!\n");
                }else {
                    if (cmUtil.validateDestAccounts(destAccounts)) {
                        String depositAddress = UUID.randomUUID().toString();
                        if (!cmUtil.isAddressUsed(depositAddress,true))
                        System.out.println("You may now send Jobcoins to address "+depositAddress+". They will be mixed and sent to your destination addresses.\n");
                        Awaitility.await().atMost(Long.parseLong(cmUtil.getProperty("Poll_duration_max")),TimeUnit.SECONDS)
                        .pollInterval(Long.parseLong(cmUtil.getProperty("Poll_interval")),TimeUnit.SECONDS)
                        .until(() -> cmUtil.isAddressUsedandBalanceAvailable(depositAddress,true));
                        
                        cmUtil.mix(depositAddress,destAccounts);
                    }
                }              
	    }
	} 
	catch (Exception ex){
	    if (ex.getMessage().equals("quit")) {
		System.out.println("Quitting...");
	    }else {
		ex.printStackTrace();
	    }
	} finally {
	   //uninit 
	   scan.close();
	   cmUtil.clear();
	}	
    }
}
