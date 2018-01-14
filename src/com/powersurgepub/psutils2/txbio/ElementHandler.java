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

  import java.io.*;

/**
 An interface for handling elements within documents. Such an interface would
 typically be used by a parser to pass data to an application. In fact, 
 this interface is a simplified version an XML content handler. Elements may
 be nested. 

 @author Herb Bowie
 */
public interface ElementHandler {
  
  public void setSource(File file);
  
  /**
   Indicate the start of a new document. 
   */
  public void startDocument();
  
  /**
   Indicate the start of a new element (aka field) within a document.
  
   @param name The name of the element. 
  
   @param isAttribute Attributes are treated as a special type of attribute,
                      so attributes are identified with this flag. 
   */
  public void startElement (String name, boolean isAttribute);
  
  /**
   Pass a data value contained within the last element started. This may be a 
   complete data value, or it may be incomplete, and only a part of the entire 
   data value. It is assumed that the handler will concatenate multiple data 
   occurrences within an element as needed. 
  
   @param str A string of data contained within the last element started. 
   */
  public void data (String str);
  
  /**
   Pass a data value contained within the last element started. This may be a 
   complete data value, or it may be incomplete, and only a part of the entire 
   data value. It is assumed that the handler will concatenate multiple data 
   occurrences within an element as needed. 
  
   @param str A string of data contained within the last element started.
   @param useMarkdown Use a Markdown parser to format the data being passed. 
   */
  public void data (String str, boolean useMarkdown);
  
  public void characters (char [] ch, int start, int length);
  
  /**
   Indicate the end of the named element. It is assumed that the handler will
   close lower-level elements as needed.
  
   @param name The name of the previously started element to be closed. 
   */
  public void endElement (String name);
  
  /**
   Indicates the end of a document.
   */
  public void endDocument();
  
}
