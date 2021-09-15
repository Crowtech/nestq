package au.com.crowtech.quarkus.nest.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CodeUtils {
	public static final int CARD_CODE_LEN = 16;

	/**
	 * Generate a code with only upper-case letters and numbers
	 * @param numUppers
	 * @param numTotal
	 * @return
	 */
	public static String genUppersCode(int numUppers, int numTotal) {
		int numDigits = numTotal - numUppers;
		return genCode(numUppers, 0, numDigits);
	}
	
	/**
	 * Generate a code using a random assortment of digits, lower case and upper case.
	 * Uses one of each
	 * @param numDigits
	 * @return
	 */
	public static String genCode(int length) {
		int numUppers = generateFromRange(1, length);
		int numLowers = generateFromRange(1, length);
		int numDigits = generateFromRange(1, length);
		
		return genCode(numUppers, numLowers, numDigits);
	}
	
	public static String genCode(int numUppers, int numLowers, int numDigits) {
		String code = "";
		int i;
		for(i = 0; i < numUppers; i++) {
			Character character = generateCharFromRange('A', 'Z');
			code += character;
		}
		
		for(i = 0; i < numLowers; i++) {
			Character character = generateCharFromRange('a', 'z');
			code += character;
		}
		
		for(i = 0; i < numDigits; i++) {
			Character character = generateCharFromRange('0', '9');
			code += character;
		}
		
		return shuffle(code);
	}
    
	/**
	 * Generate a Random Card Code using the Luhn Algorithm and an initial String containing at least 1 number
	 * @param initial string to generate from
	 * @return
	 */
	public static String generateCardCode(String code) {
		//d72c5dd3-c9e8-4087-bd21-d68eb5fe8895
		code = code.replaceAll("[a-zA-Z-]", "")
				.substring(0, code.length() / 2);
		// - 1 to allow for a CHECK SUM digit
		if(code.length() > CARD_CODE_LEN - 1) {
			// range: [0, PantaCard.CARD_CODE_LEN - 1]
			code = code.substring(0, CARD_CODE_LEN - 1);
		} else {
			while(code.length() < CARD_CODE_LEN - 1)
				code += Integer.toString(new Random().nextInt(10));
		}
		
		// Generate Check sum digit
		code += Integer.toString(getCheckSum(code));
		return code;
	}

	/**
	 * Generate a checksum from a code of at least {@link PantaCard#CARD_CODE_LEN} - 1 digits
	 * (default for CARD_CODE_LEN is 16)
	 * This method assumes that the code provided does not have a check sum on the end!
	 * @param code
	 * @return
	 */
    private static int getCheckSum(String code) {
    	int checksum = 0;
    	code = new StringBuilder(code).reverse().toString();
    	if(code.length() < CARD_CODE_LEN - 1)
    		return -1;
    	
    	// Moving right to left through the code
    	int currentVal;
    	for(int i = 0; i < CARD_CODE_LEN - 1; i++) {
			currentVal = Integer.parseInt(code.substring(i, i + 1));
    		// if Even
    		if(i % 2 == 0) {
    			// get the digital root of 2 * code[i] and make it the new value
    			currentVal *= 2;
    			if(currentVal > 9) {
    				currentVal = currentVal % 10 + currentVal / 10;
    			}
    		}
    		checksum += currentVal;
    	}
    	
    	return 10 - checksum % 10;
    }

    public static String shuffle(List<Character> characters) {
		StringBuilder output = new StringBuilder(characters.size());
		while (characters.size() != 0) {
			int randPicker = (int) (Math.random() * characters.size());
			output.append(characters.remove(randPicker));
		}
		return output.toString();
    }
    
	public static String shuffle(String input) {
		List<Character> characters = new ArrayList<Character>();
		for (char c : input.toCharArray()) {
			characters.add(c);
		}
		
		return shuffle(characters);
	}

	public static Character generateCharFromRange(Character min, Character max) {
		return (char)(new Random().nextInt((int)max - (int)min) + (int)min);
	}
	
	public static Integer generateFromRange(int minVal, int maxVal) {
		return new Random().nextInt(maxVal - minVal) + minVal;
	}
}
