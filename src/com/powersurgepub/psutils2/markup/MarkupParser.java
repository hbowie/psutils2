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
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.strings.*;
  import com.powersurgepub.psutils2.ui.*;

/**
 A class that can be used to parse markup tags from an input string
 or an input file.

 @author Herb Bowie
 */
public class MarkupParser {
  
  private boolean               stringInput = true;
  private boolean               atEnd = false;
  private String                inputString = "";
  private int                   inputStringIndex = 0;
          String								line;
	        int										lineLength;
	        int										lineIndex;
	
	        MarkupContext					context = new MarkupContext();
	
	        MarkupCharacter				markupChar = new MarkupCharacter (context);
	
	private MarkupTag             tag;

	        StringBuffer					precedingText;

  private StringBuffer          workLine;
  private int                   workIndex = 0;
  private StringBuffer          listChars;
  private boolean               linkAlias = false;
  private boolean               leftParenPartOfURL = false;
  private char                  lastDefChar = ' ';
  private int[]                 ix = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
  private String[]              html =
          {"", "", "em", "i", "strong", "b", "cite", "", "", "",
           "", "", "", "", "", "", "", "", "", "" };
  public  final static          int LAST_QUOTE        = 0;
  public  final static          int QUOTE_COLON       = 1;
  public  final static          int EMPHASIS          = 2;
  public  final static          int ITALICS           = 3;
  public  final static          int STRONG            = 4;
  public  final static          int BOLD              = 5;
  public  final static          int CITATION          = 6;
  public  final static          int EXCLAMATION       = 7;
  public  final static          int EXCLAMATION_LEFT_PAREN  = 8;
  public  final static          int EXCLAMATION_RIGHT_PAREN = 9;
  public  final static          int EXCLAMATION_COLON = 10;
  public  final static          int LEFT_BRACKET      = 11;
  public  final static          int RIGHT_BRACKET     = 12;
  public  final static          int LEFT_PAREN        = 13;
  public  final static          int RIGHT_PAREN       = 14;
  
  
  /** Creates a new instance of MarkupParser */
  public MarkupParser() {
  }
  
  /**
   Pass a string to be parsed.
   */
  public void setInput (String markup, boolean useMarkdown) {

    stringInput = true;
    inputString = markup;
    inputStringIndex = 0;
    line = "";
		lineLength = line.length();
		lineIndex = 0;
    startup(useMarkdown);
    getNextCharacter();
  }
  
  private void startup (boolean useMarkdown) {
    context = new MarkupContext();
    context.setMarkdown(useMarkdown);
    markupChar = new MarkupCharacter (context);
    setAtEnd (false);
    getNextLine();
  }
  
  public void parse (MarkupElement element) {
    while (! atEnd) {
      MarkupTag tag = readTag();
      if (tag != null) {
        // build MarkupElement
        if (tag.hasPrecedingText()) {
          element.characters (tag.getPrecedingText());
        }
        if (tag.getName().equals ("!--")) {
          // no need to process comments
        } else {
          if (! tag.isEnding()) {
            element.startElement("", tag.getName(), "", tag.getAttributes());
            if (MarkupUtils.isBlockTag (tag.getName())) {
              context.lastOpenBlock = tag.getName();
            }
          }
          if (tag.isEnding() || tag.isSelfContained()) {
            element.endElement("", tag.getName(), "");
          } // end if need to end tag
          if (tag.isEnding()
              && tag.getName().equals (context.lastOpenBlock)) {
            context.lastOpenBlock = "";
          }
        } // end if not a comment
      } // end if tag not null
    } // end while more text to parse
  } // end method parse

	/**
	   Retrieve the next HTML tag with preceding text.
	  
	   @return HTML tag or null at end of file.
	 */
	public MarkupTag readTag () {
			
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
  void getNextField () {
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
	void getNextWord () {
    
		context.word.setLength(0);
    
    // Consume enough characters to complete an entity in work
    while ((context.entityCharCount > 0)
        && (! atEnd)) {
      getNextCharacter();
    }
    
    // Write one leading white space character, if present
    if (markupChar.whiteSpace
        && context.fieldType == MarkupContext.TEXT
        // && context.field.length() > 0
        ) {
      context.word.append (markupChar.character);
      getNextCharacter();
    }
    
    // Bypass other leading white space
		while ((markupChar.whiteSpace || context.entityCharCount > 0)
        && (! markupChar.lonelyAmpersand)
        && (! atEnd)) {
			getNextCharacter();
		}
    
    // See if we're starting with a double quote
		if ((context.fieldType == MarkupContext.ATTRIBUTE_VALUE
          || context.fieldType == MarkupContext.DOCTYPE)
        && (! markupChar.translatedEntity)
				&& markupChar.character == GlobalConstants.DOUBLE_QUOTE) {
			context.quoted = true;
		}
    
    // Build the word
		while ((! markupChar.endsWord) && (! atEnd)) {
			context.word.append (markupChar.character);
			do {
        getNextCharacter();
      } while ((context.entityCharCount > 0) && (! atEnd));
		}
    if (markupChar.lonelyAmpersand) {
      context.word.append ('&');
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
	void getNextCharacter() {
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
	void getNextLine()  {
    if (stringInput) {
      getNextLineFromInputString();
    } else {
      // line = readLine();
      // lineLength = line.length();
		  // lineIndex = 0;
    }
    
    // Turn smart quotes, apostrophes, etc. into their "dumb" equivalents
    line = StringUtils.stupefy(line);
    
    if (context.useMarkdown()) {
      makeMarkupFromMarkdown();
    }
    lineIndex = 0;
    lineLength = line.length();
	} // end method getNextLine

  private void getNextLineFromInputString () {
    int startOfLine = inputStringIndex;
    char nextChar = ' ';
    char charAfter = ' ';
    while (inputStringIndex < inputString.length()
        && (! endOfLine (nextChar))) {
      nextChar = inputString.charAt(inputStringIndex);
      inputStringIndex++;
    }
    if (inputStringIndex <= (startOfLine + 1)) {
      line = "";
      lineLength = 0;
    } else {
      if (endOfLine(nextChar)) {
        line = inputString.substring(startOfLine, inputStringIndex - 1);
      } else {
        line = inputString.substring(startOfLine, inputStringIndex);
      }
      lineLength = line.length();
    }
    if (inputStringIndex < inputString.length()) {
      charAfter = inputString.charAt(inputStringIndex);
      if (charAfter != nextChar
          && (endOfLine(charAfter))) {
        inputStringIndex++;
      }
    }
    if (lineLength == 0 && inputStringIndex >= inputString.length()) {
      atEnd = true;
    }
  }
  
  private boolean endOfLine (char c) {
    return (c == GlobalConstants.CARRIAGE_RETURN
        || c == GlobalConstants.LINE_FEED);
  }

  private void makeMarkupFromMarkdown() {

    workLine = new StringBuffer(line);
    workIndex = 0;
    int workEnd = workLine.length() - 1;
    listChars = new StringBuffer();
    linkAlias = false;
    while (workIndex < workLine.length()
        && (Character.isWhitespace(workChar()))) {
      workIndex++;
    }
    while (workEnd >= 0
        && workEnd >= workIndex
        && Character.isWhitespace (workLine.charAt (workEnd))) {
      workEnd--;
    }
    boolean blankLine = (workIndex >= workLine.length());
    if (blankLine) {
      context.lastLineHTML = false;
      makeMarkupFromMarkdownBlankLine();
    }
    else
    if (
        // (workIndex > 0) || 
        ((workLine.length() > 0 )
            && (workLine.charAt(workIndex) == '<'))) {
      // First character looks like beginning of an html tag:
      // Doesn't look like textile... process it without modification.
      context.lastLineHTML = true;
    } else {
      int ruleCharCount = 0;
      boolean onlyRuleChars = false;
      char ruleChar = workChar();
      if (ruleChar == '-' || ruleChar == '_' || ruleChar == '*') {
        ruleCharCount = 1;
        onlyRuleChars = true;
        int ruleCharIndex = workIndex + 1;
        while (ruleCharIndex < workEnd && onlyRuleChars) {
          if (Character.isWhitespace(workLine.charAt(ruleCharIndex))) {
            ruleCharIndex++;
          }
          else
          if (workLine.charAt(ruleCharIndex) == ruleChar) {
            ruleCharIndex++;
            ruleCharCount++;
          } else {
            onlyRuleChars = false;
          }
        } // end while still checking for horizontal rule characters
      } // end if the line starts with a potential horizontal rule character
      if (onlyRuleChars && ruleCharCount >= 3) {
        makeMarkupFromMarkdownHorizontalRule(workIndex, workEnd);
        context.lastLineHTML = false;
      } else {
        makeMarkupFromMarkdownNonBlankLine();
        context.lastLineHTML = false;
      }
    } // end if still checking for line type
    line = workLine.toString();
  }

  private void makeMarkupFromMarkdownBlankLine () {

    if (! context.lastLineBlank) {
      closeOpenBlock();
      // doLists();
    }
    context.lastLineBlank = true;
  }

  private void makeMarkupFromMarkdownHorizontalRule (int start, int end) {
    if (! context.lastLineBlank) {
      closeOpenBlock();
      doLists();
    }
    context.lastLineBlank = true;
    lineDelete (start, end - start + 1);
    lineInsert ("<hr />");
  }

  private void makeMarkupFromMarkdownNonBlankLine () {

    // See if line begins with a block modifier
    StringBuffer blockMod = new StringBuffer();
    int startPosition = 0;
    int textPosition = 0;
    leftParenPartOfURL = false;

    String imageTitle = "";
    String imageURL = "";
    int endOfImageURL = -1;
    boolean doublePeriod = false;
    int periodPosition = workLine.indexOf (".");
    if (periodPosition >= 0) {
      textPosition = periodPosition + 1;
      if (textPosition < workLine.length()) {
        if (workLine.charAt (textPosition) == '.') {
          doublePeriod = true;
          textPosition++;
        }
        if (textPosition < workLine.length()
            && workLine.charAt (textPosition) == ' ') {
          textPosition++;
          workIndex = 0;
          while (workIndex < periodPosition
              && (Character.isLetterOrDigit (workChar()))) {
            blockMod.append (workChar());
            workIndex++;
          } // end while more letters and digits
          if (blockMod.toString().equals ("p")
              || blockMod.toString().equals ("bq")
              || blockMod.toString().equals ("h1")
              || blockMod.toString().equals ("h2")
              || blockMod.toString().equals ("h3")
              || blockMod.toString().equals ("h4")
              || blockMod.toString().equals ("h5")
              || blockMod.toString().equals ("h6")) {
            // look for attributes
          } else {
            blockMod = new StringBuffer();
            textPosition = 0;
          }
        } // end if space follows period(s)
      } // end if not end of line following first period
    } // end if period found

    // Start processing at the beginning of the line
    workIndex = 0;

    // If we have a block modifier, then generate appropriate HTML
    if (blockMod.length() > 0) {
      lineDelete (textPosition);
      closeOpenBlockQuote();
      closeOpenBlock();
      doLists();
      if (blockMod.toString().equals ("bq")) {
        lineInsert ("<blockquote><p");
        context.blockQuoting = true;
      } else {
        lineInsert ("<" + blockMod.toString());
      }
      lineInsert (">");
      if (doublePeriod) {
        context.nextBlock = blockMod.toString();
      } else {
        context.nextBlock = "p";
      }
    }

    // See if line starts with one or more list characters
    if (blockMod.length() <= 0) {
      while (workIndex < workLine.length()
          && (workChar() == '*'
              || workChar() == '#'
              || workChar() == ';')) {
        listChars.append (workChar());
        workIndex++;
      }
      if (listChars.length() > 0
          && (workIndex >= workLine.length()
              || ((! Character.isWhitespace (workChar()))
                  && (workChar() != '(')))) {
        listChars = new StringBuffer();
        workIndex = 0;
      }
      int firstSpace = workLine.indexOf (" ", workIndex);
    }
    int endDelete = workIndex;
    if (endDelete < workLine.length()
        && Character.isWhitespace (workLine.charAt (endDelete))) {
      endDelete++;
    }

    if (listChars.length() > 0) {
      lineDelete (0, endDelete);
    }
    doLists();
    if (listChars.length() > 0
        && listChars.charAt(listChars.length() - 1) == ';') {
      lastDefChar = ';';
    } else {
      lastDefChar = ' ';
    }

    // See if this line contains a link alias
    if ((blockMod.length() <= 0)
        && ((workLine.length() - workIndex) >= 4)
        && (workChar() == '[')) {
      int rightBracketIndex = workLine.indexOf ("]", workIndex);
      if (rightBracketIndex > (workIndex + 1)) {
        linkAlias = true;
        String alias = workLine.substring (workIndex + 1, rightBracketIndex);
        String url = workLine.substring (rightBracketIndex + 1);
        lineDelete (workLine.length() - workIndex);
        lineInsert ("<a alias=\"" + alias  + "\" href=\"" + url  + "\"> </a>");
      }
    }

    // If no other instructions, use default start for a new line
    if (blockMod.length() <= 0
        && listChars.length() <= 0
        && (! linkAlias)) {
      // This non-blank line does not start with a block modifier or a list char
      if (context.lastLineBlank) {
        closeOpenBlockQuote();
        closeOpenBlock();
        doLists();
        if (context.nextBlock.equals ("bq")) {
          lineInsert ("<blockquote><p>");
          context.blockQuoting = true;
        } else {
          lineInsert ("<" + context.nextBlock + ">");
        }
      } 
      else
      if (context.lastLineHTML) {
        // Assume HTML is in control
      } else {
        lineInsert ("<br />");
      }
      context.nextBlock = "p";
    }

    // Now examine the rest of the line
    char last = ' ';
    char c = ' ';
    char next = ' ';
    leftParenPartOfURL = false;
    resetLineIndexArray();
    while (workIndex <= workLine.length()) {
      // Get current character, last character and next character
      last = c;
      if (workIndex < workLine.length()) {
        c = workChar();
      } else {
        c = ' ';
      }
      if ((workIndex + 1) < workLine.length()) {
        next = workLine.charAt (workIndex + 1);
      } else {
        next = ' ';
      }

      // Brackets surround linked text, and parentheses surround the link
      if (c == '[') {
        ix [LEFT_BRACKET] = workIndex;
        workIndex++;
      }
      else
      if (c == ']'
          && ix [LEFT_BRACKET] >= 0) {
        ix [RIGHT_BRACKET] = workIndex;
        workIndex++;
      }
      else
      if (c == '('
          && ix [LEFT_BRACKET] >= 0
          && ix [RIGHT_BRACKET] >= 0
          && ix [RIGHT_BRACKET] == (workIndex - 1)) {
        ix [LEFT_PAREN] = workIndex;
        workIndex++;
      }
      else
      if (c == ')'
          && ix [LEFT_BRACKET] >= 0
          && ix [RIGHT_BRACKET] >= 0
          && ix [LEFT_PAREN] >= 0) {
        String linkText 
            = workLine.substring(
              ix [LEFT_BRACKET ] + 1, 
              ix [RIGHT_BRACKET]);
        String link 
            = workLine.substring(
              ix [LEFT_PAREN] + 1, 
              workIndex);
        lineDelete (ix [LEFT_BRACKET], workIndex - ix [LEFT_BRACKET] + 1);
        lineInsert (workIndex,
            "<a href=\"" + link + "\">" + linkText + "</a>");
        ix [LEFT_BRACKET] = -1;
        ix [RIGHT_BRACKET] = -1;
        ix [LEFT_PAREN] = -1;
        workIndex++;
      }
      else
      // ?? means a citation
      if (c == '?' && last == '?') {
        if (ix [CITATION] >= 0) {
          replaceWithHTML (CITATION, 2);
        } else {
          ix [CITATION] = workIndex - 1;
          workIndex++;
        }
      }
      else
      // __ means strong
      if (c == '_' && last == '_' && ix [QUOTE_COLON] < 0) {
        if (ix [STRONG] >= 0) {
          replaceWithHTML (STRONG, 2);
        } else {
          ix [STRONG] = workIndex - 1;
          workIndex++;
        }
      }
      else
      // ** means strong
      if (c == '*' && last == '*') {
        if (ix [STRONG] >= 0) {
          replaceWithHTML (STRONG, 2);
        } else {
          ix [STRONG] = workIndex - 1;
          workIndex++;
        }
      }
      else
      // _ means emphasis
      if (c == '_' && next != '_' && ix [QUOTE_COLON] < 0) {
        if (ix [EMPHASIS] >= 0) {
          replaceWithHTML (EMPHASIS, 1);
        } else {
          ix [EMPHASIS] = workIndex;
          workIndex++;
        }
      }
      else
      // * means emphasis
      if (c == '*' && next != '*') {
        if (ix [EMPHASIS] >= 0) {
          replaceWithHTML (EMPHASIS, 1);
        } else {
          ix [EMPHASIS] = workIndex;
          workIndex++;
        }
      }
      else
      // Exclamation points surround image urls
      if (c == '!' && Character.isLetter(next)
          && ix [QUOTE_COLON] < 0 && ix [EXCLAMATION] < 0) {
          // First exclamation point : store its location and move on
          ix [EXCLAMATION] = workIndex;
          workIndex++;
      }
      else
      // Second exclamation point
      if (c == '!'
          && ix [QUOTE_COLON] < 0 && ix [EXCLAMATION] >= 0) {
        // Second exclamation point
        imageTitle = "";
        endOfImageURL = workIndex;
        if (last == ')' && ix [EXCLAMATION_LEFT_PAREN] > 0) {
          ix [EXCLAMATION_RIGHT_PAREN] = workIndex - 1;
          endOfImageURL = ix [EXCLAMATION_LEFT_PAREN];
          imageTitle = workLine.substring
                (ix [EXCLAMATION_LEFT_PAREN] + 1, ix [EXCLAMATION_RIGHT_PAREN]);
        }
        imageURL = workLine.substring (ix [EXCLAMATION] + 1, endOfImageURL);
        // Delete the image url, title and parentheses,
        // but leave exclamation points for now.
        lineDelete (ix [EXCLAMATION] + 1, workIndex - ix [EXCLAMATION] - 1);
        String titleString = "";
        if (imageTitle.length() > 0) {
          titleString = " title=\"" + imageTitle + "\" alt=\"" + imageTitle + "\"";
        }
        lineInsert (ix [EXCLAMATION] + 1,
            "<img src=\"" + imageURL + "\"" + titleString + "  />");
        if (next == ':') {
          // Second exclamation followed by a colon -- look for url for link
          ix [QUOTE_COLON] = workIndex;
          ix [LAST_QUOTE] = ix [EXCLAMATION];
        } else {
          lineDelete (ix [EXCLAMATION], 1);
          lineDelete (workIndex, 1);
        }
        ix [EXCLAMATION] = -1;
        ix [EXCLAMATION_LEFT_PAREN] = -1;
        ix [EXCLAMATION_RIGHT_PAREN] = -1;
        workIndex++;
      } // end if second exclamation point
      else
      // Parentheses within exclamation points enclose the image title
      if (c == '(' && ix [EXCLAMATION] > 0 ) {
        ix [EXCLAMATION_LEFT_PAREN] = workIndex;
        workIndex++;
      }
      else
      // Double quotation marks surround linked text
      if (c == '"' && ix [QUOTE_COLON] < 0) {
        if (next == ':'
            && ix [LAST_QUOTE] >= 0
            && workIndex > (ix [LAST_QUOTE] + 1)) {
          ix [QUOTE_COLON] = workIndex;
        } else {
          ix [LAST_QUOTE] = workIndex;
        }
        workIndex++;
      }
      else
      // Flag a left paren inside of a url
      if (c == '(' && ix [QUOTE_COLON] > 0) {
        leftParenPartOfURL = true;
        workIndex++;
      }
      else
      // Space may indicate end of url
      if (Character.isWhitespace (c)
          && ix [QUOTE_COLON] > 0
          && ix [LAST_QUOTE] >= 0
          && workIndex > (ix [QUOTE_COLON] + 2)) {
        int endOfURL = workIndex - 1;
        // end of url is last character of url
        // do not include any trailing punctuation at end of url
        int backup = 0;
        while ((endOfURL > (ix [QUOTE_COLON] + 2))
            && (! Character.isLetterOrDigit (workLine.charAt(endOfURL)))
            && (! ((workLine.charAt(endOfURL) == ')') && (leftParenPartOfURL)))
            && (! (workLine.charAt(endOfURL) == '/'))) {
          endOfURL--;
          backup++;
        }
        String url = workLine.substring (ix [QUOTE_COLON] + 2, endOfURL + 1);
        // insert the closing anchor tag
        lineInsert (endOfURL + 1, "</a>");
        // Delete the quote, colon and url from the line
        lineDelete (ix [QUOTE_COLON], endOfURL + 1 - ix [QUOTE_COLON]);
        // insert the beginning of the anchor tag
        lineDelete (ix [LAST_QUOTE], 1);
        lineInsert (ix [LAST_QUOTE], "<a href=\"" + url + "\">");
        // Reset the pointers
        ix [QUOTE_COLON] = -1;
        ix [LAST_QUOTE] = -1;
        leftParenPartOfURL = false;
        // Increment the index to the next character
        if (backup > 0) {
          workIndex = workIndex - backup;
        } else {
          workIndex++;
        }
      }
      else
      // Look for start of definition
      if ((c == ':' || c == ';')
          && Character.isWhitespace(last)
          && Character.isWhitespace(next)
          && listChars.length() > 0
          && (lastDefChar == ';' || lastDefChar == ':')) {
        lineDelete (workIndex - 1, 3);
        lineInsert (closeDefinitionTag (lastDefChar)
            + openDefinitionTag (c));
        lastDefChar = c;
      }
      /* else
      // -- means an em dash
      if (c == '-' && last == '-') {
        workIndex--;
        lineDelete (2);
        lineInsert ("&#8212;");
        // System.out.println ("Inserting em dash: " + line);
      } */ else {
        workIndex++;
      }
    }// end while more characters to examine
    context.lastLineBlank = false;
  }

  private char workChar () {
    return workLine.charAt (workIndex);
  }

  private char charAt (String str, int index) {
    if (index < str.length()) {
      return str.charAt (index);
    } else {
      return ' ';
    }
  }

  /**
   Process leading list characters (* for bulleted, # for numbered)
   when using Textile
   */
  private void doLists () {
    char lastListChar = ' ';
    char thisListChar = ' ';

    // Determine how deeply lists are nested
    int listDepth = context.lastListChars.length();
    if (listChars.length() > listDepth) {
      listDepth = listChars.length();
    }

    // Determine how deeply we are maintaining existing lists with new line
    int listCharsMatchingDepth = 0;
    for (int i = 0; i < listDepth; i++) {
      thisListChar = charAt (listChars.toString(), i);
      lastListChar = charAt (context.lastListChars, i);
      if (thisListChar == lastListChar) {
        listCharsMatchingDepth = i + 1;
      }
    }

    // Where list characters don't match, end any lists in progress
    for (int i = listDepth - 1; i >= listCharsMatchingDepth; i--) {
      lastListChar = charAt (context.lastListChars, i);
      if (lastListChar == '*') {
        lineInsert ("</li>");
        lineInsert ("</ul>");
      }
      else
      if (lastListChar == '#') {
        lineInsert ("</li>");
        lineInsert ("</ol>");
      }
      else
      if (lastListChar == ';') {
        lineInsert (closeDefinitionTag(lastDefChar));
        lastDefChar = ' ';
        lineInsert ("</dl>");
      }
    }

    // Where list characters don't match, start any new lists needed
    for (int i = listCharsMatchingDepth; i < listDepth; i++) {
      thisListChar = charAt (listChars.toString(), i);
      StringBuffer listStart = new StringBuffer();
      if (thisListChar == '*') {
        listStart.append ("ul");
      }
      else
      if (thisListChar == ';') {
        listStart.append ("dl");
      } else {
        listStart.append ("ol");
      }
      if (thisListChar == '*') {
        lineInsert ("<" + listStart.toString() + ">");
        lineInsert ("<li>");
      }
      else
      if (thisListChar == '#') {
        lineInsert ("<" + listStart.toString() + ">");
        lineInsert ("<li>");
      }
      else
      if (thisListChar == ';') {
        lineInsert ("<" + listStart.toString() + ">");
        lineInsert (openDefinitionTag(thisListChar));
      }
    }

    // If List characters all match, then continue list in process
    if (listCharsMatchingDepth == listDepth && listDepth > 0) {
      if (thisListChar == ';') {
        lineInsert (closeDefinitionTag(lastDefChar));
        lastDefChar = ' ';
        lineInsert ("<dt>");
      } else {
        lineInsert ("</li>");
        lineInsert ("<li>");
      }
    }

    // If some lists ended, but some continuing, start new list item
    if (listCharsMatchingDepth > 0
        && listCharsMatchingDepth < listDepth) {
      thisListChar = charAt (listChars.toString(), listCharsMatchingDepth - 1);
      if (thisListChar == '*') {
        lineInsert ("<li>");
      }
      else
      if (thisListChar == '#') {
        lineInsert ("<li>");
      } else
      if (thisListChar == ';') {
        lineInsert (openDefinitionTag(thisListChar));
      }
    }

    // Save latest list characters
    context.lastListChars = listChars.toString();
  } // end method doLists

  /**
   Open a new definition tag (dt or dd) while parsing Textile.
   @param thisDefChar The character starting the next span of characters.
   @return The appropriate XHTML.
   */
  private String openDefinitionTag (char thisDefChar) {
    if (thisDefChar == ';') {
      return "<dt>";
    }
    else
    if (thisDefChar == ':') {
      return "<dd>";
    } else {
      return "";
    }
  }

  /**
   Close an open definition tag (dt or dd) while parsing Textile.

   @param lastDefChar The character that started the last span of characters.
   @return The appropriate XHTML
   */
  private String closeDefinitionTag (char last) {
    if (last == ';') {
      return "</dt>";
    }
    else
    if (last == ':') {
      return "</dd>";
    } else {
      return "";
    }
  }

  private void closeOpenBlock () {
    if (context.lastOpenBlock.length() > 0) {
      lineInsert ("</" + context.lastOpenBlock + ">");
    }
  }

  private void closeOpenBlockQuote () {
    if (context.blockQuoting) {
      lineInsert ("</blockquote>");
      context.blockQuoting = false;
    }
  }

  /**
   Replace starting and ending sequences of Textile characters
   with corresponding HTML tags.

   @param tagType An index to the arrays containing the location of the
                  starting tag and the html tag itself.

   @param numberOfChars The number of characters to be replaced
   */
  private void replaceWithHTML (int tagType, int numberOfChars) {

    lineDelete (ix [tagType], numberOfChars);
    lineInsert (ix [tagType], "<" + html [tagType] + ">");
    if (numberOfChars > 1) {
      workIndex--;
    }
    lineDelete (numberOfChars);
    lineInsert ("</" + html [tagType] + ">");
    resetLineIndex (tagType);
  }

  private void lineDelete (int count) {
    workLine.delete (workIndex, workIndex + count);
  }

  /**
  Delete characters starting with the first parm, and continuing for the 
  number of characters identified in the second parm, and adjust the
  workIndex if needed.
  
  @param deletePosition The starting position for the delete.
  @param count The number of characters to be deleted.
  */
  private void lineDelete (int deletePosition, int count) {
    workLine.delete (deletePosition, deletePosition + count);
    if ((deletePosition + count - 1) < workIndex) {
      workIndex = workIndex - count;
    }
    else
    if (deletePosition < workIndex) {
      workIndex = deletePosition;
    }
    if (workIndex < 0) {
      workIndex = 0;
    }
    // Adjust any affected index positions we've got stored
    for (int i = 0; i < ix.length; i++) {
      if (deletePosition < ix [i]) {
        ix [i] = ix [i] - count;
      }
    }
  }

  private void lineInsert (String insert) {
    workLine.insert (workIndex, insert);
    workIndex = workIndex + insert.length();
  }

  private void lineInsert (int insertPoint, String insert) {
    workLine.insert (insertPoint, insert);
    if (insertPoint <= workIndex) {
      workIndex = workIndex + insert.length();
    }
    // Adjust any affected index positions we've got stored
    for (int i = 0; i < ix.length; i++) {
      if (insertPoint <= ix [i]) {
        ix [i] = ix [i] + insert.length();
      }
    }
  }

  private void resetLineIndexArray () {
    for (int i = 0; i < ix.length; i++) {
      resetLineIndex (i);
    }
  }

  private void resetLineIndex (int position) {
    ix [position] = -1;
  }
	
	private void setAtEnd (boolean atEnd) {
    this.atEnd = atEnd;
    context.inputIsAtEnd = atEnd;
  }
  
  public void close () {
    // no need to do anything
  }

  private void reportTrouble(Exception e) {
    Logger.getShared().recordEvent
        (LogEvent.MAJOR, "MarkupParser Exception " + e.toString(), false);
    Trouble.getShared().report
        ("MarkupParser Exception " + e.toString(), "I/O Error");
    atEnd = true;
  }
  
} // end Class
