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

/**
   An object representing one attribute of an HTML tag. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
 */

public class HTMLAttribute {

  /** 
	   The name of the attribute.
	 */
  private String		name;

  /**
	   The value associated with the attribute.
	 */
  private String		value = "";

  /**
	   Was the value enclosed in quotation marks?
	 */
  private boolean		quoted = false;

  /**
	   The constructor to use when the attribute has no value.
	  
	   @param name the name of the attribute.
	 */
  public HTMLAttribute (String name) {
    this.setName (name);
  }

  /**
	   The constructor to use when the attribute has a value. 
	   If the passed value includes surrounding
	   quotation marks, then they will be stripped and the quoted flag will be
	   set on.
	  
	   @param name the name of the attribute.
	   @param value the value of the attribute, possibly including surrounding quotation marks.
	 */
  public HTMLAttribute (String name, String value) {
    setName (name);
		setValue (value);
	}

  /**
	   The constructor to use when the attribute has a value, and when the presence of surrounding
	   quotation marks has already been determined.
	  
	   @param name the name of the attribute.
	   @param value the value of the attribute, possibly including surrounding quotation marks.
	   @param quoted whether the value was enclosed in quotation marks.
	 */
  public HTMLAttribute (String name, String value, boolean quoted) {
    setName (name);
		setValue (value);
		setQuoted (quoted);
	}
	
  /**
	   Sets the name of the attribute.
	  
	   @param name the name of the attribute.
	 */
  public void setName (String name) {
    this.name = name.toLowerCase();
  }

  /**
	   Sets the value of the attribute. If the passed value includes surrounding
	   quotation marks, then they will be stripped and the quoted flag will be
	   set on.
	  
	   @param value the value of the attribute.
	 */
  public void setValue (String value) {
		this.value = value;
		int valueLength = value.length();
		if (valueLength >= 2) {
			if (value.startsWith ("\"") && value.endsWith ("\"")) {
				this.value = value.substring (1, valueLength -1);
				setQuoted (true);
			}
      else
			if (value.startsWith ("\'") && value.endsWith ("\'")) {
				this.value = value.substring (1, valueLength -1);
				setQuoted (true);
			}
		}
	}

  /**
	   Sets the quoted flag.
	  
	   @param quoted indicates whether the value was enclosed in quotation marks.
	 */
  public void setQuoted (boolean quoted) {
    this.quoted = quoted;
  }

  /*
	   Returns the attribute name.
	  
	   @return name of the attribute.
	 */
  public String getName() {
    return name;
  }
  
  /*
	   Checks to see if attribute has a value.
	  
	   @return true if attribute has a value.
	 */
  public boolean hasValue() {
    return ((value.length() > 0) || quoted);
  }

  /*
	   Returns the attribute value.
	  
	   @return value of the attribute, with no enclosing quotation marks.
	 */
  public String getValue() {
    return value;
  }
  
  /*
	   Returns the attribute value as an integer.
	  
	   @return value of the attribute as an integer, or 0 if not a valid 
             integer.
	 */
  public int getValueAsInt() {
    int answer = 0;
    if (value.trim().length() == 0) {
      answer = 0;
    }
    else {
      try {
        answer = Integer.parseInt(value.trim());
      } catch (NumberFormatException e) {
        answer = 0;
      }
    }
    return answer;
  }

  /*
	   Returns the attribute value, with enclosing quotation marks, if necessary.
	  
	   @return value of the attribute, with enclosing quotation marks, if necessary.
	 */
  public String getValueWithQuotesIfNeeded() {
    if (quoted || value.indexOf (" ") >= 0) {
			return ("\"" + value + "\"");
		} else {
			return value;			
		}
  }

  /**
	   Returns a flag indicating whether the value originally had enclosing
	   quotation marks.
	  
	   @return flag indicating whether enclosing quotation marks were originally present.
	 */
  public boolean getQuoted() {
    return quoted;
  }

	/**
	   Determines equality.
	  
	   @return true if lower-case names are the same.
	  
	   @param attr2 second HTMLAttribute object.
	 */
	public boolean equals (HTMLAttribute attr2) {
		return (getName().equals (attr2.getName()));
	}

  /**
	   Returns the attribute in string form.
	  
	   @return attribute formatted as a string
	 */
	public String toString() {
		if (value.length() > 0) {
			return (getName() + "=" + getValueWithQuotesIfNeeded());
		} else {
			return getName(); 		
		}
	}

}

	
