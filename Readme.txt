That is java console application.

To build application open project in Eclipse,
Right Click om CoinMixerApp project, choose 'run as'and select 'Maven build'
It will build fat jar named CoinMixerApp-0.0.1-SNAPSHOT-jar-with-dependencies.jar and place it into target folder.
You can copy that jar to any folder.

Open command window go folder where jar is residing and execute command >java -jar CoinMixerApp-0.0.1-SNAPSHOT-jar-with-dependencies.jar

It will prompt you to input comma delimited not used before destination addresses.

It will generate unique mixer deposit address and will prompt you to deposit JobCoins to that address.

It will start polling that address for balance available more thn zero.

as soon as balance becomes available, it will be deposited to 'house account and mix will be initiated.
User will be notified that mix started. and will be prompted to provide more destination addresses.

Based on current application.properties setting all dispences should be scheduled within 60 seconds.

Current application.properties settings which are insided the jar are as follows:

#General
HouseAccount=AA_ikra_house_acc

#Http
apiBaseUrl=http://jobcoin.gemini.com/hazard-antacid/api

#Poll parameters in seconds
Poll_duration_max=3600
Poll_interval=10

#Dispense
Fee_rate=0.05
max_interval_dispenses=60

Please note that application currently has in-memory persistancy.

