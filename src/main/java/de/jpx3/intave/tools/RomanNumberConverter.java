package de.jpx3.intave.tools;

public final class RomanNumberConverter {
  private final static int[] ROMAN_STEPS = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
  private final static String[] ROMAN_LITERALS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

  public static String toRomanLiteral(int number) {
    StringBuilder roman = new StringBuilder();
    for (int i = 0; i < ROMAN_STEPS.length; i++) {
      while (number >= ROMAN_STEPS[i]) {
        number -= ROMAN_STEPS[i];
        roman.append(ROMAN_LITERALS[i]);
      }
    }
    return roman.toString();
  }
}
