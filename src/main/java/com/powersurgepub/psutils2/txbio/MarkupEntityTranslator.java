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

package com.powersurgepub.psutils2.txbio;

  import java.util.*;

/**
 Used to translate text to and from common HTML/XML entities.
 */
public class MarkupEntityTranslator {

  private static MarkupEntityTranslator translator = null;

  private ArrayList list = new ArrayList();

  /**
   Share a common instance, since no state data is saved, and all
   instances are functionally equivalent.

   @return The shared instance of this class.
   */
  public static MarkupEntityTranslator getSharedInstance() {
    if (translator == null) {
      translator = new MarkupEntityTranslator();
    }
    return translator;
  }

  /**
   Create a new instance.
   */
  public MarkupEntityTranslator () {
    list.add (new MarkupEntity ("quot",    34, "\""));
    list.add (new MarkupEntity ("amp",     38, "&"));
    list.add (new MarkupEntity ("apos",    39, "'"));
    list.add (new MarkupEntity ("",        40, "("));
    list.add (new MarkupEntity ("",        41, ")"));
    list.add (new MarkupEntity ("lt",      60, "<"));
    list.add (new MarkupEntity ("gt",      62, ">"));
    list.add (new MarkupEntity ("nbsp",   160, " "));
    list.add (new MarkupEntity ("mdash", 8212, "--"));
    list.add (new MarkupEntity ("",      8217, "'"));
  }

  /**
   Translate any entities in the passed string.

   @param markup A string potentially containing HTML/XML entities.
   @return The equivalent string with common entities translated into their
           normal equivalents.
   */
  public String translateFromMarkup (String markup) {
    StringBuffer work = new StringBuffer (markup);
    int i = 0;
    int lastAmpersand = -1;
    char c = ' ';
    while (i < work.length()) {
      c = work.charAt(i);
      if (c == '&') {
        lastAmpersand = i;
      }
      else
      if (c == ';' 
          && lastAmpersand >= 0
          && ((i - lastAmpersand) < 10)) {
        String from = work.substring (lastAmpersand, i+1);
        String to = lookupEntityReplacement (from);
        work.delete (lastAmpersand, i+1);
        work.insert (lastAmpersand, to);
        i = i - from.length() + to.length();
        lastAmpersand = -1;
      }
      i++;
    } // end while scanning string
    return work.toString();
  }

  /**
   Lookup and return the common replacement for a string containing an
   XML/HTML entity.

   @param name This may optionally contain the leading ampersand and/or the
   trailing semi-colon. Also, the string may optionally contain characters
   indicating the presence of a numeric value rather than a name ('#'),
   and the presence of a hex value (lower- or upper-case 'x').

   @return The common replacement for the entity, if one can be found;
   otherwise the passed string will be returned without change.

   */
  public String lookupEntityReplacement (String name) {
    if (name.length() > 0) {
      int start = 0;
      int end = name.length();
      if (name.charAt(end - 1) == ';') {
        end--;
      }
      if (name.charAt(0) == '&') {
        start++;
      }
      if (name.charAt(start) == '#') {
        // Lookup using a number instead of a name
        start++;
        if (name.charAt(start) == 'x' || name.charAt(start) == 'X') {
          // Lookup using a hex number
          start++;
          try {
            return lookupEntityReplacement
                (Integer.parseInt (name.substring (start, end), 16));
          } catch (NumberFormatException e) {
            return name;
          }
        } else {
          // Lookup using a decimal number
          try {
            return lookupEntityReplacement
                (Integer.parseInt (name.substring (start, end), 10));
          } catch (NumberFormatException e) {
            return name;
          }
        } // end if decimal number found
      } else {
        // A name, and not a number, is being passed
        boolean found = false;
        int i = 0;
        MarkupEntity entity = (MarkupEntity)list.get(0);
        while ((! found) && (i < list.size())) {
          entity = (MarkupEntity)list.get(i);
          found = entity.equalsName (name.substring (start, end));
          if (! found) {
            i++;
          } // end if not found
        } // end while searching list
        if (found) {
          return entity.getReplacement();
        } else {
          return (name);
        }
      }
    } else {
      return "";
    }
  } // end method

  /**
   Lookup and return the common replacement for a number identifying an
   XML/HTML entity.

   @param number A number identifying an entity.

   @return The common replacement for the entity, if one can be found;
   otherwise the passed value will be returned as a string.

   */
  public String lookupEntityReplacement (int number) {
    boolean found = false;
    int i = 0;
    MarkupEntity entity = (MarkupEntity)list.get(0);
    while ((! found) && (i < list.size())) {
      entity = (MarkupEntity)list.get(i);
      found = entity.equalsNumber (number);
      if (! found) {
        i++;
      } // end if not found
    } // end while searching list
    if (found) {
      return entity.getReplacement();
    } else {
      return ("&#" + String.valueOf (number) + ";");
    }
  } // end method

}
