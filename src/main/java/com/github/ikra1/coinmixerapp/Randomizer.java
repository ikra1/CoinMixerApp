package com.github.ikra1.coinmixerapp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.OptionalDouble;
import java.util.Random;

public class Randomizer {

   private Random rand = new SecureRandom(); 
 
   public BigDecimal getRandomBigDecimal(double fromRannge, double toRange) {
	OptionalDouble randdouble = rand.doubles(1, 0.01, 0.99).findFirst();
	BigDecimal brnd = BigDecimal.valueOf(randdouble.getAsDouble());
	brnd=brnd.setScale(8,RoundingMode.HALF_UP);
	return brnd;
    }
   public int getRandomInt(int maxInt) {
	return rand.nextInt(maxInt);
    }

}
