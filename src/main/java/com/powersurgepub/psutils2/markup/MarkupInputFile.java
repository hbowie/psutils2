/*
 * MarkupInputFile.java
 *
 * Created on March 29, 2007, 4:56 AM
 *
 * To change this template, choose Tools | Template Manager
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

package com.powersurgepub.psutils2.markup;
  
  import com.powersurgepub.psutils2.basic.*;

	import java.io.*;
  import java.net.*;

/**
   A source of parsed markup. The XML/HTML can be read as MarkupTag objects. 
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)

 */

public class MarkupInputFile {
          
  private boolean               atEnd = false;
          String								line;
	        int										lineLength;
	        int										lineIndex;
	
	        MarkupContext					context = new MarkupContext();
	
	        MarkupCharacter				markupChar = new MarkupCharacter (context);
	
	private MarkupTag             tag;

	        StringBuffer					precedingText;
  
  private File           inFile;
  private BufferedReader inReader;
	  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name passed as a single String.
   */
  public MarkupInputFile (String inFileName) {
    inFile = new File (inFileName);
    commonConstruction();
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.
     
     @param inFile  The text file itself.
   */
  public MarkupInputFile (File inFile) {
    this.inFile = inFile;
    commonConstruction();
  }
  
  /**
     Common initialization for all constructors.
   */
  void commonConstruction () {
    startup();
    try {
      inReader = new BufferedReader(new FileReader (inFile));
    } catch (FileNotFoundException e) {
      setAtEnd (true);
    }
  }
  
  private void startup () {
    context = new MarkupContext();
    markupChar = new MarkupCharacter (context);
    setAtEnd (false);
  }
  
  /**
     Opens the HTML file for subsequent input. 
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput () 
      throws IOException {
    getNextLine();
		getNextCharacter();
  }

	/**
	   Retrieve the next HTML tag with preceding text.
	  
	   @return HTML tag or null at end of file.
	 */
	public MarkupTag readTag ()
			throws IOException, FileNotFoundException {
			
		// start building new tag
		tag = new MarkupTag ();
				
		// get any text preceding the tag itself
		markupChar.setFieldType (MarkupContext.TEXT);
		getNextField();
		tag.setPrecedingText (context.field.toString());
		if (markupChar.character == '<') {
			getNextCharacter();
		}
		
		// see if this is an ending tag
		if (markupChar.character == '/') {
			tag.setEnding();
			getNextCharacter();
		}
		
		// get the name of the tag
		markupChar.setFieldType (MarkupContext.TAG_NAME);
		getNextField();
		tag.setName (context.field.toString());
    
    String attribute = "";
    StringBuffer value = new StringBuffer();
		
		// check to see if the tag is a comment
		if (tag.getName().equals ("!--")) {
      attribute = "!==";
      markupChar.setFieldType (MarkupContext.COMMENT);
      getNextField();
      for (int i = 0; i < 2; i++) {
        if (context.field.charAt (context.field.length() - 1) == '-') {
          context.field.setLength (context.field.length() - 1);
        }
      }
			tag.setAttribute (attribute, context.field.toString());
		}
    else
    if (tag.getName().equals ("!DOCTYPE")) {
      markupChar.setFieldType (MarkupContext.DOCTYPE);
      while (context.fieldType == MarkupContext.DOCTYPE) {
        getNextField();
      }
    }
    else {
		
      // Collect and store all the attributes
      while ((! markupChar.endsTag) && (! atEnd)) {
        markupChar.setFieldType (MarkupContext.ATTRIBUTE_NAME);
        getNextCharacter();
        getNextField();
        if (context.field.length() > 0) {
          attribute = context.field.toString();
          value.setLength(0);
          if (markupChar.character == '=') {
            getNextCharacter();
            markupChar.setFieldType (MarkupContext.ATTRIBUTE_VALUE);
            getNextField();
            value.append (context.field.toString());
          }
        tag.setAttribute (attribute, value.toString());
        }
      }
    }
    
    context.fieldType = MarkupContext.TEXT;
    
    // return results
		if (markupChar.character == '>') {			
      getNextCharacter();
      return tag;
    }
    else 
    if (tag.getPrecedingText().length() > 0) {
      return tag;
		} 
    else {
			return null;
		}
	}

	/**
	   Get next field.
	 */
  void getNextField () 
			throws IOException, FileNotFoundException {
		context.field.setLength(0);
		while ((! markupChar.endsField) && (! atEnd)) {
			getNextWord();
			if (context.word.length() > 0) {
				context.field.append (context.word.toString());
			}
		} 
    
    // remove any extra trailing spaces from field
    while (context.field.length() > 1
        && context.field.charAt (context.field.length() - 1) == ' '
        && context.field.charAt (context.field.length() - 2) == ' ') {
      context.field.deleteCharAt (context.field.length() - 1);
    }
	} // end getNextField method
	
	/**
	   Get next word.
	 */
	void getNextWord () 
			throws IOException, FileNotFoundException {
		context.word.setLength(0);
    while ((context.entityCharCount > 0)
        && (! atEnd)) {
      getNextCharacter();
    }
    if (markupChar.whiteSpace
        && context.fieldType == MarkupContext.TEXT) {
        // && context.field.length() > 0) {
      context.word.append (markupChar.character);
    }
		while ((markupChar.whiteSpace || context.entityCharCount > 0)
        && (! atEnd)) {
			getNextCharacter();
		}
		if ((context.fieldType == MarkupContext.ATTRIBUTE_VALUE
          || context.fieldType == MarkupContext.DOCTYPE)
        && (! markupChar.translatedEntity)
				&& markupChar.character == GlobalConstants.DOUBLE_QUOTE) {
			context.quoted = true;
		}
		while ((! markupChar.endsWord) && (! atEnd)) {
			context.word.append (markupChar.character);
			do {
        getNextCharacter();
      } while ((context.entityCharCount > 0) && (! atEnd));
		}
		if (context.quoted
        && (! markupChar.translatedEntity)
				&& markupChar.character == GlobalConstants.DOUBLE_QUOTE) {
			context.word.append (markupChar.character);
      context.quoted = false;
			do {
        getNextCharacter();
      } while ((context.entityCharCount > 0) && (! atEnd));
		}
	} // end getNextWord method

	/**
	   Ready next character for processing.
	 */
	void getNextCharacter()
			throws IOException, FileNotFoundException {
		context.lastCharacter2 = context.lastCharacter1;
		context.lastCharacter1 = markupChar.character;
		if (lineIndex >= lineLength) {
			markupChar.setCharacter (GlobalConstants.LINE_FEED);
			getNextLine();
		}
		else {
			markupChar.setCharacter (line.charAt (lineIndex++));
		}
	} // end getNextCharacter method

	/**
	   Ready next input text line for processing.
	 */
	void getNextLine() {
    lineLength = 0;
    if (! atEnd) {
      try {
        line = inReader.readLine();
        if (line == null) {
          setAtEnd (true);
        } else {
          lineLength = line.length();
          // System.out.println ("MarkupInputFile.getNextLine " + line);
        }
      } catch (IOException ioe) {
        setAtEnd (true);
      } 
      lineIndex = 0;
    }
	}
  
  private void setAtEnd (boolean atEnd) {
    this.atEnd = atEnd;
    context.inputIsAtEnd = atEnd;
  }
  
  public void close () {
    // no need to do anything
  }
  
} // end Class

