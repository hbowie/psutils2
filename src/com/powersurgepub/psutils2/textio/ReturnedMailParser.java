/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Copyright 1999 - 2013 Herb Bowie
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

package com.powersurgepub.psutils2.textio;

  import java.io.*;

/**
 *
 * @author hbowie
 */
public class ReturnedMailParser {

  private     String         allowedChars = ".-_";
  private     TextLineReader reader = null;
  private     String         line   = "";
  private     int            index  = -1;
  private     char           c = ' ';
  private     StringBuffer   address = new StringBuffer();
  private     boolean        validAddress = false;
  private     boolean        badAddress = false;
  private     int            atIndex = 0;
  private     int            dotIndex = 0;
  private     String         lastBadAddress = "";
  private     String         skipDomain1 = "umseattle.com";
  private     String         skipDomain2 = "spring.joyent.us";

  public ReturnedMailParser (TextLineReader reader) {
    this.reader = reader;
  }

  public ReturnedMailParser (File file) {
    reader = new FileLineReader (file);
  }

  public boolean open () {
    return reader.open();
  }

  public String readBadEmailAddress () {
    
    while ((! isAtEnd())
        && ((! validAddress)
            || (! badAddress)
            || address.toString().equalsIgnoreCase (lastBadAddress))) {
      readEmailAddress();

    } // end while looking for a valid e-mail address
    if (isAtEnd()) {
      return "";
    } else {
      lastBadAddress = address.toString();
      return address.toString();
    }
  }
  
  private void readEmailAddress () {
    address = new StringBuffer();
    atIndex = 0;
    dotIndex = -1;
    validAddress = false;
    badAddress = false;
    while ((! isAtEnd()) && (c != '@')) {
      readChar();
    }
    if (! isAtEnd()) {
      lookAroundAtSign();
    }
    if (atIndex > 0
        && atIndex <= 64
        && dotIndex > 3
        && address.length() > dotIndex
        && dotIndex > (atIndex + 1)) {
      validAddress = true;
    } // end if conditions are met for a valid e-mail address
    if (validAddress) {
      if (address.substring (0, atIndex).equalsIgnoreCase("MAILER-DAEMON")
          || address.substring (0, atIndex).equalsIgnoreCase("postmaster")
          || address.substring (atIndex + 1, address.length()).equalsIgnoreCase (skipDomain1)
          || address.substring (atIndex + 1, address.length()).equalsIgnoreCase (skipDomain2)) {
        badAddress = false;
      } else {
        badAddress = true;
      }
    }
    if (validAddress) {
      index = index + address.length() - atIndex;
      readChar();
    } else {
      readChar();
    }
  }
  
  private void lookAroundAtSign () {
    
    // Get the @ sign
    address.append(c);
    
    // Get the local part of the e-mail address
    int j = index - 1;
    while (j >= 0 && j < line.length()
        && (! Character.isWhitespace (line.charAt(j)))
        && (Character.isLetterOrDigit (line.charAt(j))
            || allowedChars.indexOf (line.charAt(j)) >= 0)) {
      address.insert (0, line.charAt (j));
      atIndex++;
      j--;
    }
    
    // Get the domain name
    j = index + 1;
    while (j < line.length() && j >= 0
        && (! Character.isWhitespace (line.charAt(j)))
        && (Character.isLetterOrDigit (line.charAt(j))
            || allowedChars.indexOf (line.charAt(j)) >= 0)
        && (! (line.charAt(j) == '.'
            && line.length() > (j + 1)
            && line.charAt(j + 1) == '.')))  {
      address.append (line.charAt (j));
      if (line.charAt(j) == '.') {
        dotIndex = address.length() - 1;
      }
      j++;
    }
  }

  private void readChar () {
    index++;
    if (index >= line.length()) {
      c = ' ';
      readLine();
    } else {
      c = line.charAt (index);
    } // end if we got a character
  } // end method

  private void readLine () {
    line = reader.readLine();
    index = -1;
  }

  public boolean close() {
    return reader.close();
  }

  public boolean isOK () {
    return reader.isOK();
  }

  public boolean isAtEnd() {
    return reader.isAtEnd();
  }

}
