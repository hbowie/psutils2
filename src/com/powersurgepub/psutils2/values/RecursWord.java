/*
 * Copyright 2017 - 2017 Herb Bowie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powersurgepub.psutils2.values;

/**
 One word parsed from the Recurs string. 

 @author Herb Bowie
 */
public class RecursWord {
  
  private StringBuilder wordBuilder = new StringBuilder();
  private int           number = 0;
  private int           digits = 0;
  private int           letters = 0;
  private int           startOfLetters = -1;
  private int           symbols = 0;
  private int           endWord = 0;
  private boolean       endOfWord = false;
  
  public RecursWord(int startWord, String str) {
    endOfWord = false;
    int i = startWord;
    while (i < str.length() && (! endOfWord)) {
      i = anotherChar(i, str);
      if (wordBuilder.toString().equalsIgnoreCase("every")
          || wordBuilder.toString().equalsIgnoreCase("semi")
          || wordBuilder.toString().equalsIgnoreCase("bi")) {
        endOfWord = true;
      } // end if we have an important word (even without white space)
      
      // See what kind of word we have
      String word = wordBuilder.toString();
      
      // See if we have some kind of number
      if (digits > 0 && (letters == 0 || startOfLetters > 0)) {
        int numberEnd = word.length();
        if (startOfLetters > 0) {
          numberEnd = startOfLetters;
        }
        try {
          number = Integer.parseInt(word.substring(0, numberEnd));
        } catch (NumberFormatException e) {
          number = 0;
        }
      }
      else
      if (word.equals("first") 
          || word.equals("one")) {
        number = 1;
      }
      else
      if (word.equals("second")
          || word.equals("two")
          || word.equals("other")) {
        number = 2;
      }
      else
      if (word.equals("third")
          || word.equals("three")) {
        number = 3;
      }
      else
      if (word.equals("fourth")
          || word.equals("four")) {
        number = 4;
      }
      else
      if (word.equals("fifth")
          || word.equals("five")) {
        number = 5;
      }
      else
      if (word.equals("sixth")
          || word.equals("six")) {
        number = 7;
      }
      else
      if (word.equals("seventh")
          || word.equals("seven")) {
        number = 7;
      }
    } // end of string
    
  } // end of constructor
  
  /**
   Process another character in the input string. 
  
   @param position The current position of the index into the string. 
   @param str The string being parsed. 
  
   @return The next position to be scanned. 
  */
  private int anotherChar(int position, String str) {
    int i = position;
    anotherChar(str.charAt(i));
    i++;
    endWord = i;
    return i;
  }
  
  /**
   Process another character in the input string. 
  
   @param c The next character to be evaluated. .
  */
  private void anotherChar(char c) {
    if (Character.isDigit(c)) {
      digits++;
      wordBuilder.append(c);
    }
    else
    if (Character.isLetter(c)) {
      if (letters == 0) {
        startOfLetters = wordBuilder.length();
      }
      letters++;
      wordBuilder.append(c);
    }
    else
    if (Character.isWhitespace(c)) {
      endOfWord = true;
    }
    else {
      symbols++;
      wordBuilder.append(c);
    }
  }
  
  public int getDigits() {
    return digits;
  }
  
  public boolean allDigitsAndSymbols() {
    return (digits > 0 && letters == 0); 
  }
  
  public int getLetters() {
    return letters;
  }
  
  public int getSymbols() {
    return symbols;
  }
  
  public int getEndOfWord() {
    return endWord;
  }
  
  public String toString() {
    return wordBuilder.toString();
  }
  
  public int length() {
    return wordBuilder.length();
  }
  
  public String get() {
    return wordBuilder.toString();
  }
  
  public String getWord() {
    return wordBuilder.toString();
  }
  
  public boolean isNumber() {
    return (number != 0);
  }
  
  public int getNumber() {
    return number;
  }

}
