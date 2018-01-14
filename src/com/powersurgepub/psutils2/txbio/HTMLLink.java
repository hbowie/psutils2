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
   An object representing any sort of file reference embedded
   within an HTML file. 
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing 
           (<a href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
 */

public class HTMLLink {

  public  final static String   A_HREF_TYPE 		= "ahref";
  public  final static String   LINK_HREF_TYPE 	= "linkhref";
  public  final static String   MAILTO_TYPE 		= "mailto";
  public  final static String   IMG_SRC_TYPE 		= "imgsrc";
	
	/** The name/location of the linking file. */
  private String		from					= "";
  
  /** The type of link (see literals). */
  private String		type					= "";
  
  /** The name/location of the referenced file or resource. */
  private String		to						= "";
  
	/**
	   Constructor.
     
     @param from The name/location of the linking file.
     @param type The type of link.
     @param to   The name/location of the referenced file or resource. 
	 */
	public HTMLLink (String from, String type, String to) {
		this.from = from;
    this.type = type;
    this.to = to;
	}
  
	/**
	   Sets the from field.
     
     @param from The name/location of the linking file.
	 */
	public void setFrom (String from) {
		this.from = from;
	}
  
	/**
	   Get the from field.
     
     @return The name/location of the linking file.
	 */
	public String getFrom () {
		return from;
	}
  
	/**
	   Sets the type of link. 
     
     @param type The type of link.
	 */
	public void setType (String type) {
    this.type = type;
	}
  
	/**
	   Get the type field.
     
     @return The type of link. 
	 */
	public String getType () {
    return type;
	}
  
	/**
	   Sets the to field.
     
     @param to   The name/location of the referenced file or resource. 
	 */
	public void setTo (String to) {
    this.to = to;
	}  

	/**
	   Get the to field.
     
     @return The name/location of the referenced file or resource. 
	 */
	public String getTo () {
    return to;
	}
  	
	/**
	   Returns the object in string form.
	  
	   @return object formatted as a string
	 */
	public String toString() {
    return ("HTML Link from " + from
        + " (type " + type
        + ") to " + to);
	}
  
} // end of class

