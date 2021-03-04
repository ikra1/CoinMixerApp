CoinMixerAppt is java console application.
------------------------------------------------------------------------------------------
Build Prerequisites 
Installed Java SDK – version  from 1.8 up. (tested with version 15.02)
Installed Maven – tested with version 3.6.3
Environment variables MAVEN_HOME and JAVA_HOME set
------------------------------------------------------------------------------------------
Build
Open Command window;
 change folder to CoinMixerApp root;
execute command >mvn clean package
It will build fat jar named CoinMixerApp-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
and place it into target folder.
You can copy that jar to any folder.
Please note that CoinMixerApp-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
file is provided from my local Environment and could be found in root folder of GIT
project.
-------------------------------------------------------------------------------
Execution
Open command window and  go to folder where jar 
CoinMixerApp-0.0.1-SNAPSHOT-jar-with-dependencies is residing and execute 
command >java -jar CoinMixerApp-0.0.1-SNAPSHOT-jar-with-dependencies.jar

It will prompt you to input comma delimited not used before destination addresses.
After addresses are provided, it will generate unique mixer deposit address and 
will prompt you to deposit JobCoins to that address.

Please deposit JobCoins to address provided.

It will start polling that address immediately for balance available.

As soon as balance becomes available and detected by application,
 it will be deposited to 'house account’ and mix will be initiated.

User will be notified that mix started. and will be prompted to provide 
more destination addresses.

based on current application.properties settings, 
all deposits should be received by destination addresses around 60 seconds 
after mix start.

You can initiate as many mixes as you would like to during application session.

Please quit application by entering 'quit' command only when seeing all expected 
deposits posted or after 60 seconds from last mix initiation.

-------------------------------------------------------------------------------
Current application.properties settings which are inside the jar are as follows:

#General
HouseAccount=AA_ikra_house_acc

#Http
apiBaseUrl=http://jobcoin.gemini.com/hazard-antacid/api

#Poll parameters in seconds
Poll_duration_max=3600
Poll_interval=10

#Dispense interval in seconds
Fee_rate=0.05
max_interval_dispenses=60

-------------------------------------------------------------------------------
Disclaimers

Please note that application currently has in-memory persistence and quitting 
it before deposits received may cause permanent loss of transaction not yet
completed.

Due to time constraint, only one test case was developed,
 which will initiate two simultaneous mixes with different amounts;
 check balances in house address (to check fees collected);
and after all deposits to destination addresses were made check 
the sum of deposit addresses balances against expected sum.

