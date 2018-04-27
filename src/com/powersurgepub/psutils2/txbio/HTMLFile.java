/*
 * Copyright 1999 - 2017 Herb Bowie
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

  import com.powersurgepub.psutils2.basic.*;
  import com.powersurgepub.psutils2.logging.*;
  import com.powersurgepub.psutils2.records.*;
  import com.powersurgepub.psutils2.textio.*;

	import java.io.*;
  import java.net.*;

/**
   A source of parsed HTML. The HTML can be read as HTMLTag objects, or as
   DataRecord objects, if the input file consists of bookmarks using embedded lists
   as a hierarchical organizing mechanism, or using heading levels as a similar 
   mechanism. 
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)

 */

public class HTMLFile
    implements DataSource {
    
  public  final static String   ANCHOR = "Description";
  public  final static String		EMAIL  = "E-mail";
  public  final static String   URL    = "Web Site";
  public  final static int			MAX_CATEGORY_LEVEL = 6;
  public	final static String[] CATEGORY_FIELD_NAME 
      = {"Category 0", "Category 1", "Category 2", 
          "Category 3", "Category 4", "Category 5", "Category 6"};

	private String[]              category = new String[MAX_CATEGORY_LEVEL + 1];
  
  /** Use headings as categories rather than list levels? */
          boolean               headingsAsCategories = false;
          boolean               headingsAndLists  = false;
          
          int[]                 headings          = {0, 0, 0, 0, 0, 0, 0};
          int                   headingLevelsUsed = 0;
          
          StringBuffer					line = new StringBuffer();
	        int										lineLength;
	        int										lineIndex;
          int                   textIndex = 0;
          int[]                 ix = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
          String[]              html =
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

  private    boolean            metadataAsMarkdown = true;

          StringBuffer          listChars;
          boolean               linkAlias = false;
          boolean               leftParenPartOfURL = false;
          int                   attributeType = 0;
          StringBuffer          klass = new StringBuffer();
          StringBuffer          id = new StringBuffer();
          StringBuffer          style = new StringBuffer();
          StringBuffer          language = new StringBuffer();
  private char                  lastDefChar = ' ';
	
	        HTMLContext						context;
	
	        HTMLCharacter					htmlChar;
	
	        HTMLTag								tag;
	
	        HTMLAttribute					attribute;

	        StringBuffer					precedingText;
  
  /** Data Record currently being built. */
          DataRecord						dataRec;
  
  /** Data dictionary to be used for the file. */
          DataDictionary    		dict;
  
  /** Record definition to be used for the file. */
          RecordDefinition   		recDef;
          
  /** Path to the original source file (if any). */
  private		 String							dataParent;
  
  /** Sequential number identifying last record read or written. */
          int                		recordNumber = 0;
  
  // The following fields are used for logging
  
  /** Log to record events. */
  private  Logger       log;
  
  /** Do we want to log all data, or only data preceding significant events? */
  private  boolean      dataLogging = false;
  
  /** Data to be sent to the log. */
  private  LogData      logData;
  
  /** Events to be logged. */
  private  LogEvent     logEvent;
  
  /** Identifier for this file (to be printed in the log as a source ID). */
  private  String       fileId;

  // Input sources
  private  File               inFile = null;
  private  URL                inURL  = null;
  private  TextLineReader     textLineReader = null;
  
  private  String             encoding = "UTF-8";
  private  URLConnection      inConnect = null;
  private  String             inName = "";
  private  InputStream        inStream = null;
  private  InputStreamReader  streamReader = null;
  private  BufferedReader     reader = null;
  private  boolean            atEnd = false;
  
  private  MetaMarkdownReader mdReader = null;
	  
  /**
     A constructor that accepts a file name.
    
     @param inFileName  A file name passed as a single String.
   */
  public HTMLFile (String inFileName) {
    inFile = new File(inFileName);
    inName = inFileName;
    inURL = null;
    commonConstruction();
  }
  
  /**
     A constructor that accepts a single File object
     representing the file.
     
     @param inFile  The text file itself.
   */
  public HTMLFile (File inFile) {
    this.inFile = inFile;
    inName = inFile.toString();
    inURL = null;
    commonConstruction();
  }
  
  /**
     A constructor that accepts a URL pointing to a Web resource.
     
     @param url  The URL of a text file.
   */  
  public HTMLFile (URL url) {
    this.inFile = null;
    inURL = url;
    inName = url.toString();
    commonConstruction();
  }

  /**
     A constructor that accepts a URL pointing to a Web resource.

     @param url  The URL of a text file.
   */
  public HTMLFile (URL url, String label) {
    this.inFile = null;
    inURL = url;
    inName = url.toString();
    commonConstruction();
    context.setType (label);
  }
  
  public HTMLFile (
      TextLineReader textLineReader, 
      String name, 
      String dataParent, 
      String type) {
    this.textLineReader = textLineReader;
    inName = name;
    this.dataParent = dataParent;
    commonConstruction();
    context.setType(type);
  }
  
  /**
     Common initialization for all constructors.
   */
  void commonConstruction () {

    if (inFile != null) {
      dataParent = inFile.getParent();
    }
    if (inURL != null) {
      dataParent = "www";
    }
    context = new HTMLContext();
    context.setFile(this);
    context.entityTranslation = false;
		htmlChar = new HTMLCharacter (context);
  }
  
  /**
     Use headings as category levels, instead of list levels.
   */
  public void useHeadings () {
    headingsAsCategories = true;
  }
  
  /**
     Use headings as category levels, instead of list levels.
    
     @param headingsAsCategories On or off?
   */
  public void useHeadings (boolean headingsAsCategories) {
    this.headingsAsCategories = headingsAsCategories;
  }
  
  /**
     Use a flexible combination of headings and lists as category levels.
   */
  public void useHeadingsAndLists () {
    useHeadingsAndLists (true);
  }
  
  /**
     Use a flexible combination of headings and lists as category levels.
    
     @param headingsAndLists On or off?
   */
  public void useHeadingsAndLists (boolean headingsAndLists) {
    this.headingsAndLists = headingsAndLists;
  }

  /**
   Sets encoding to be using when opening an input stream. Defaults to "UTF-8".
   If called, must be called before openForInput.

   @param encoding MacRoman, UTF-8, etc.
   */
  public void setEncoding (String encoding) {
    this.encoding = encoding;
  }

  /**
   Returns the character encoding scheme being used.

   @return MacRoman, UTF-8 and such.
   */
  public String getEncoding () {
    return encoding;
  }
  
  /**
   Pass any metadata lines to the markdown parser as well. 
  
   @param metadataAsMarkdown True if metadata lines should appear as part
                             of output HTML, false otherwise. 
  */
  public void setMetadataAsMarkdown (boolean metadataAsMarkdown) {
    this.metadataAsMarkdown = metadataAsMarkdown;
  }
  
  /**
     Opens the HTML file for subsequent input. 
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput () 
      throws IOException {
    this.dict = new DataDictionary();
    openForInput (dict);
  }

  /**
     Opens the HTML file for subsequent input. 
    
     @param  recDef Record Definition with dictionary to be used.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput (RecordDefinition recDef) 
      throws IOException {
    openForInput (recDef.getDict());
  }
    
  /**
     Opens the HTML file for subsequent input. 
    
     @param  dict Data dictionary to be used by this file.
    
     @throws IOException If the input file is not found, or if there
                         is trouble reading it. 
   */
  public void openForInput (DataDictionary dict) 
      throws IOException {
    
    // If this is markdown, then convert it to HTML before going any farther
    if (context.isMarkdown()) {
      if (inFile == null && inURL != null) {
        mdReader = new MetaMarkdownReader(inURL, 
            MetaMarkdownReader.MARKDOWN_TYPE);
      }
      else
      if (inFile != null) {
        mdReader = new MetaMarkdownReader(inFile, 
            MetaMarkdownReader.MARKDOWN_TYPE);
      }
      else
      if (textLineReader != null) {
        mdReader = new MetaMarkdownReader(textLineReader, 
            MetaMarkdownReader.MARKDOWN_TYPE);
      }
      if (mdReader != null) {
        mdReader.setMetadataAsMarkdown(metadataAsMarkdown);
        mdReader.openForInput();
        inFile = null;
        inURL = null;
        textLineReader = new StringLineReader(mdReader.getHTML());
        mdReader.close();
        mdReader = null;
      }
    }
    
    if (inFile == null && inURL != null) {
      HttpURLConnection.setFollowRedirects(true);
      inConnect = inURL.openConnection();
      if (inConnect.getClass().getName().endsWith("HttpURLConnection")) {
        HttpURLConnection httpConnect = (HttpURLConnection)inConnect;
        httpConnect.setInstanceFollowRedirects(true);
        httpConnect.setConnectTimeout(0);
      }
      inConnect.connect();
      inStream = inConnect.getInputStream();
      streamReader = new InputStreamReader(inStream, "UTF-8");
      reader = new BufferedReader(streamReader);
      Logger.getShared().recordEvent(
          LogEvent.NORMAL,
          "HTMLFile Open for input URL " + inURL.toString()
            + " with encoding " + streamReader.getEncoding(),
          false);
    } 
    else
    if (inFile != null) {
      streamReader = new FileReader(inFile);
      reader = new BufferedReader(streamReader);
    }
    else
    if (textLineReader != null) {
      textLineReader.open();
      reader = null;
    }
    
    this.dict = dict;
    dataRec = new DataRecord();
    recDef = new RecordDefinition(this.dict);
    openWithRule();
    recordNumber = 0;
    atEnd = false;
  }
  
  /**
     Performs open operations for this particular HTML interpretation rule.
     Build record definition.  
   */
  void openWithRule () 
      throws IOException, FileNotFoundException {
    for (int i = 1; i <= MAX_CATEGORY_LEVEL; i++) {
      storeField (CATEGORY_FIELD_NAME [i], "");
      category [i] = "";
    }
    storeField (ANCHOR, "");
    storeField (EMAIL, "");
    storeField (URL, "");
    getNextLine();
		getNextCharacter();
  }
  
  /**
     Retrieve the next bookmark.
    
     @return formatted bookmark or null if end of file.
   */
  public DataRecord nextRecordIn () 
      throws IOException, FileNotFoundException {
    HTMLTag tag;
    StringBuffer text = new StringBuffer("");
    String href = "";
    dataRec = new DataRecord();
    do {
      tag = readTag();
      if (tag == null) {
        // don't do anything
      } // end if tag is null
      else { // tag not null
        text.append (tag.getPrecedingText());
        if (tag.getName().equals("a")) {
          if (tag.isEnding()) {
            // capture anchor when we fall out of the do loop
          } // end if ending tag
          else {
            href = tag.getAttribute("href").getValue();
            text.setLength(0);
          } // end beginning a tag 
        } // end a tag
        else // not an a (anchor) tag
        if ((! headingsAsCategories)
            && tag.isListItemEnd()
            && text.length() > 0) {
          int catIndex = tag.getListLevel() + headingLevelsUsed;
          if ((tag.getListLevel() > 0) 
              && ((catIndex) <= MAX_CATEGORY_LEVEL)) {
            category [catIndex] = text.toString();
            for (int i = catIndex + 1; i <= MAX_CATEGORY_LEVEL; i++) {
              category [i] = "";
            }
          }
        }
        else // not an a (anchor) tag and not a list category
        if ((headingsAsCategories || headingsAndLists)
            && tag.isHeadingTag()
            && tag.isEnding()
            && text.length() > 0) {
          if (tag.getHeadingLevel() > 0) {
            int h = tag.getHeadingLevel();
            int l = 0;
            if (headings [h] > 0) {
              l = headings [h];
            } else {
              headingLevelsUsed++;
              headings [h] = headingLevelsUsed;
              l = headingLevelsUsed;
            }
            if (l <= MAX_CATEGORY_LEVEL) {
              category [l] = text.toString();
            }
          }
        }
        if ((! headingsAsCategories)
            && tag.isListItemStart()) {
          text.setLength(0);
        }
        else
        if ((headingsAsCategories || headingsAndLists)
            && tag.isHeadingTag()
            && (! tag.isEnding())) {
          text.setLength(0);
        }
      } // end tag not null
    } while (tag != null
        && (!(tag.isEnding() 
            && tag.getName().equals("a")
            && href != null)));
    if (tag == null || href == null) {
      return null;
    }
    else {
      // populate the data record
      int dtLevelsUsed = 0;
      if (tag.isDefTermActive()) {
        dtLevelsUsed = 1;
      } 
      int listLevelsUsed = tag.getListLevel();
      if (listLevelsUsed < 0) {
        listLevelsUsed = 0;
      }
      else
      if (listLevelsUsed > 0) {
        listLevelsUsed--;
      }
      for (int i = 1; i <= MAX_CATEGORY_LEVEL; i++) {
        if ((! headingsAsCategories)
            && i <= (listLevelsUsed + headingLevelsUsed + dtLevelsUsed)) {
          storeField (CATEGORY_FIELD_NAME [i], category[i]);
        }
        else
        if (headingsAsCategories
            && i < tag.getHeadingLevel()) {
          storeField (CATEGORY_FIELD_NAME [i], category[i]);
        }
        else {
          storeField (CATEGORY_FIELD_NAME [i], "");
        }
      }
      storeField (ANCHOR, text.toString());
      text.setLength(0);
      if (href.startsWith ("mailto:")) {
        storeField (EMAIL, href.substring(7));
        storeField (URL, "");
      }
      else {
        storeField (EMAIL, "");
        storeField (URL, href);
      }
      recordNumber++;
      return dataRec;
    }
  }
  
  /**
     Adds a field to the record being built. 
    
     @return The column number assigned to the field.
     @param  fieldName The name of the field.
     @param  value     The data value assigned to the field. 
   */
  int storeField (String fieldName, String value) {
    int column = recDef.getColumnNumber (fieldName);
    if (column < 0) {
      column = recDef.addColumn (fieldName);
    }
    column = dataRec.storeField (recDef, fieldName, value);
    return column;
  }

	/**
	   Retrieve the next HTML tag with preceding text.
	  
	   @return HTML tag or null at end of file.
	 */
	public HTMLTag readTag ()
			throws IOException, FileNotFoundException {
			
		// start building new tag
		tag = new HTMLTag ();
				
		// get any text preceding the tag itself
		htmlChar.setFieldType (HTMLContext.TEXT);
		getNextField();
		tag.setPrecedingText (context.field.toString());
		if (htmlChar.character == '<') {
			getNextCharacter();
		}
		
		// see if this is an ending tag
		if (htmlChar.character == '/') {
			tag.setEnding();
			getNextCharacter();
		}
		
		// get the name of the tag
		htmlChar.setFieldType (HTMLContext.TAG_NAME);
		getNextField();
		tag.setName (context.field.toString());
		
		// check to see if the tag is a comment
		if (tag.getName().equals ("!--")) {
      attribute = new HTMLAttribute ("!==");
      htmlChar.setFieldType (HTMLContext.COMMENT);
      getNextField();
      for (int i = 0; i < 2; i++) {
        if (context.field.length() > 0
            && context.field.charAt (context.field.length() - 1) == '-') {
          context.field.setLength (context.field.length() - 1);
        }
      }
      attribute.setValue (context.field.toString());
			tag.setAttribute (attribute);
		}
    else {
		
      // Collect and store all the attributes
      while (! htmlChar.endsTag) {
        htmlChar.setFieldType (HTMLContext.ATTRIBUTE_NAME);
        getNextCharacter();
        getNextField();
        if (context.field.length() > 0) {
          attribute = new HTMLAttribute (context.field.toString());
          if (htmlChar.character == '=') {
            getNextCharacter();
            htmlChar.setFieldType (HTMLContext.ATTRIBUTE_VALUE);
            getNextField();
            attribute.setValue (context.field.toString());
          }
        tag.setAttribute (attribute);
        }
      }
    }

    // evaluate block properties of tag
    if ((! tag.isEnding())
        && (tag.isBlockTag())) {
      context.lastOpenBlock = tag.getName();
    }
    else
    if (tag.isEnding()
        && tag.getName().equals (context.lastOpenBlock)) {
      context.lastOpenBlock = "";
    }
    
    // evaluate list properties of tag
    int listTagType = 0;
    if (tag.getName().equals ("ol")
        || tag.getName().equals ("ul")
        || tag.getName().equals ("dl")) {
      listTagType = 1;
    }
    else
    if (tag.getName().equals ("li")
        || tag.getName().equals ("dt")
        || tag.getName().equals ("dd")) {
      listTagType = 2;
      tag.setListItemStart (true);
    }
    
    tag.setListLevel (context.listLevel);
    tag.setListItemTag (context.listItemTag);
    if (context.listItemTag.length() > 0) {
      if (listTagType > 0) {
        context.listItemTag = "";
        tag.setListItemEnd (true);
      }
    }
    
    if (listTagType == 1) {
      context.defTermActive = false;
      if (tag.isEnding()) {
        if (context.listLevel > 0) {
          context.listLevel--;
        }
      }
      else {
        context.listLevel++;
      }
    }
    
    tag.setDefTermActive (context.defTermActive);
    
    if (listTagType == 2) {
      context.listItemTag = tag.getName();
      if (tag.getName().equals("dt")) {
        context.defTermActive = true;
      }
    }
    
    // evaluate heading properties of tag
    if (tag.isHeadingTag()) {
      context.headingLevel = tag.getHeadingLevel();
    } else {
      tag.setHeadingLevel (context.headingLevel);
    }
    
    // Check for preformatting
    if (tag.getName().equals ("pre")) {
      context.preformatted = (! tag.isEnding());
    }
    
    // return results
		if (htmlChar.character == '>') {			
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
	   Get next field. A field is a complete block of text with associated 
     meaning. A field may consist of one or more words. 
	 */
  void getNextField () 
			throws IOException, FileNotFoundException {
    
		context.field.setLength(0);
		while ((! htmlChar.endsField) && (! atEnd)) {
			getNextWord();
			if (context.word.length() > 0) {
				context.field.append (context.word.toString());
			}
		} 
    
    // remove multiple trailing spaces from field
    // if (! context.preformatted) {
      while (context.field.length() > 1
          && context.field.charAt (context.field.length() - 1) == ' '
          && context.field.charAt (context.field.length() - 2) == ' ') {
        context.field.deleteCharAt (context.field.length() - 1);
      }
    // }
	} // end getNextField method
	
	/**
	   Get next word.
	 */
	void getNextWord () 
			throws IOException, FileNotFoundException {
    
		context.word.setLength(0);
    
    // Build the next entity
    while ((context.entityCharCount > 0)
        && (! atEnd)) {
      getNextCharacter();
    }
    
    // See if the word starts with white space
    boolean startingWhiteSpaceForWord
        = (htmlChar.whiteSpace 
        && (context.fieldType == HTMLContext.TEXT)
        && (! context.preformatted));
    
    // Capture leading whitespace if appropriate
    if (htmlChar.whiteSpace
        && context.fieldType == HTMLContext.TEXT
        && (context.field.length() > 0
          || context.preformatted)) {
      context.word.append (htmlChar.character);
    }
    
    // If we're dealing with preformatted text, 
    // then capture all leading white space
    if (context.preformatted && context.fieldType == HTMLContext.TEXT) {
      while (htmlChar.whiteSpace && (! atEnd)) {
        context.word.append (htmlChar.character);
        getNextCharacter();
      }
    }
    
    // Now skip any remaining white space
    while (((htmlChar.whiteSpace) 
          || context.entityCharCount > 0)
        && (! atEnd)) {
      getNextCharacter();
    }
    
    // See if we've got got a quoted attribute value
		if (context.fieldType == HTMLContext.ATTRIBUTE_VALUE
        && (! htmlChar.translatedEntity)) {
      if (htmlChar.character == GlobalConstants.DOUBLE_QUOTE) {
        context.quoted = true;
        context.startQuoteChar = GlobalConstants.DOUBLE_QUOTE;
      }
      else
      if (htmlChar.character == GlobalConstants.SINGLE_QUOTE) {
        context.quoted = true;
        context.startQuoteChar = GlobalConstants.SINGLE_QUOTE;
      }
		}
    
    // Now capture the word's content
		while (! htmlChar.endsWord) {
			context.word.append (htmlChar.character);
			do {
        getNextCharacter();
      } while ((context.entityCharCount > 0) && (! atEnd));
		}
    
		if (context.quoted
        && (! htmlChar.translatedEntity)
				&& htmlChar.character == context.startQuoteChar) {
			context.word.append (htmlChar.character);
      context.quoted = false;
			do {
        getNextCharacter();
      } while ((context.entityCharCount > 0) && (! atEnd));
		}
    if (startingWhiteSpaceForWord
        && context.fieldType == HTMLContext.TEXT
        && context.word.length() > 0
        && (! Character.isWhitespace (context.word.charAt (0)))) {
      context.word.insert (0, ' ');
    }
	} // end getNextWord method

	/**
	   Ready next character for processing.
	 */
	void getNextCharacter()
			throws IOException, FileNotFoundException {
		context.lastCharacter2 = context.lastCharacter1;
		context.lastCharacter1 = htmlChar.character;
    if (context.hasExtraCharacter()) {
      htmlChar.setCharacter (context.getExtraCharacter());
    }
    else
		if (lineIndex >= lineLength) {
			htmlChar.setCharacter (GlobalConstants.LINE_FEED, ' ');
			getNextLine();
		}
		else {
			htmlChar.setCharacter (line, lineIndex++);
		}
	} // end getNextCharacter method

	/**
	   Ready next input text line for processing.
	 */
	void getNextLine()
			throws IOException, FileNotFoundException {
    String nextLine = "";
    if (reader == null) {
      nextLine = textLineReader.readLine();
    } else {
      nextLine = reader.readLine();
    }
    if (nextLine == null) {
      atEnd = true;
      line = new StringBuffer();
    } else {
		  line = new StringBuffer (nextLine);
    }
    if (context.isTextile()) {
      textilize ();
    }
		lineLength = line.length();
		lineIndex = 0;
	}

  /**
     Examine most recently read line and perform textile conversions on it
   */
  private void textilize () {

    textIndex = 0;
    int textEnd = line.length() - 1;
    listChars = new StringBuffer();
    linkAlias = false;
    while (textIndex < line.length()
        && (Character.isWhitespace(textChar()))) {
      textIndex++;
    }
    while (textEnd >= 0
        && textEnd >= textIndex
        && Character.isWhitespace (line.charAt (textEnd))) {
      textEnd--;
    }
    boolean blankLine = (textIndex >= line.length());
    if (blankLine) {
      textilizeBlankLine();
    }
    else
    if ((textIndex > 0)
        || ((line.length() > 0 )
            && (line.charAt(0) == '<'))) {
      // First character is white space of the beginning of an html tag:
      // Doesn't look like textile... process it without modification.
    }
    else
    if (((textEnd - textIndex) == 3)
        && line.substring(textIndex,textEnd + 1).equals ("----")) {
        // && (line.substring(textIndex, (textEnd + 1)).equals ("----")) {
        textilizeHorizontalRule(textIndex, textEnd);
    } else {
      textilizeNonBlankLine();
    };
  }

  private void textilizeBlankLine () {
    if (! context.lastLineBlank) {
      closeOpenBlock();
      // doLists();
    }
    context.lastLineBlank = true;
  }

  private void textilizeHorizontalRule (int start, int end) {
    if (! context.lastLineBlank) {
      closeOpenBlock();
      doLists();
    }
    context.lastLineBlank = true;
    lineDelete (start, end - start + 1);
    lineInsert ("<hr>");
  }

  private void textilizeNonBlankLine () {

    // See if line begins with a block modifier
    StringBuffer blockMod = new StringBuffer();
    int startPosition = 0;
    int textPosition = 0;
    leftParenPartOfURL = false;
    attributeType = 0;
    klass = new StringBuffer();
    id = new StringBuffer();
    style = new StringBuffer();
    language = new StringBuffer();
    String imageTitle = "";
    String imageURL = "";
    int endOfImageURL = -1;
    boolean doublePeriod = false;
    int periodPosition = line.indexOf (".");
    if (periodPosition >= 0) {
      textPosition = periodPosition + 1;
      if (textPosition < line.length()) {
        if (line.charAt (textPosition) == '.') {
          doublePeriod = true;
          textPosition++;
        }
        if (textPosition < line.length()
            && line.charAt (textPosition) == ' ') {
          textPosition++;
          textIndex = 0;
          while (textIndex < periodPosition
              && (Character.isLetterOrDigit (textChar()))) {
            blockMod.append (textChar());
            textIndex++;
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
            collectAttributes (periodPosition);
          } else {
            blockMod = new StringBuffer();
            textPosition = 0;
          }
        } // end if space follows period(s)
      } // end if not end of line following first period
    } // end if period found

    // Start processing at the beginning of the line
    textIndex = 0;

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
      if (klass.length() > 0) {
        lineInsert (" class=\"" + klass.toString() + "\"");
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
      while (textIndex < line.length()
          && (textChar() == '*'
              || textChar() == '#'
              || textChar() == ';')) {
        listChars.append (textChar());
        textIndex++;
      }
      if (listChars.length() > 0
          && (textIndex >= line.length()
              || ((! Character.isWhitespace (textChar()))
                  && (textChar() != '(')))) {
        listChars = new StringBuffer();
        textIndex = 0;
      }
      int firstSpace = line.indexOf (" ", textIndex);
      if (listChars.length() > 0) {
        collectAttributes (firstSpace);
      }
    }
    int endDelete = textIndex;
    if (endDelete < line.length()
        && Character.isWhitespace (line.charAt (endDelete))) {
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
        && ((line.length() - textIndex) >= 4)
        && (textChar() == '[')) {
      int rightBracketIndex = line.indexOf ("]", textIndex);
      if (rightBracketIndex > (textIndex + 1)) {
        linkAlias = true;
        String alias = line.substring (textIndex + 1, rightBracketIndex);
        String url = line.substring (rightBracketIndex + 1);
        lineDelete (line.length() - textIndex);
        lineInsert ("<a alias=\"" + alias  + "\" href=\"" + url  + "\"> </a>");
      }
    }

    // If no other instructions, use default start for a new line
    if (blockMod.length() <= 0 
        && listChars.length() <= 0
        & (! linkAlias)) {
      // This non-blank line does not start with a block modifier or a list char
      if (context.lastLineBlank) {
        if (context.nextBlock.equals ("bq")) {
          lineInsert ("<p>");
        } else {
          closeOpenBlockQuote();
          lineInsert ("<" + context.nextBlock + ">");
        }
      } else {
        lineInsert ("<br />");
      }
    }

    // Now examine the rest of the line
    char last = ' ';
    char c = ' ';
    char next = ' ';
    leftParenPartOfURL = false;
    resetLineIndexArray();
    while (textIndex <= line.length()) {
      // Get current character, last character and next character
      last = c;
      if (textIndex < line.length()) {
        c = textChar();
      } else {
        c = ' ';
      }
      if ((textIndex + 1) < line.length()) {
        next = line.charAt (textIndex + 1);
      } else {
        next = ' ';
      }
      
      // ?? means a citation
      if (c == '?' && last == '?') {
        if (ix [CITATION] >= 0) {
          replaceWithHTML (CITATION, 2);
        } else {
          ix [CITATION] = textIndex - 1;
          textIndex++;
        }
      }
      else
      // __ means italics
      if (c == '_' && last == '_' && ix [QUOTE_COLON] < 0) {
        if (ix [ITALICS] >= 0) {
          replaceWithHTML (ITALICS, 2);
        } else {
          ix [ITALICS] = textIndex - 1;
          textIndex++;
        }
      }
      else
      // ** means bold
      if (c == '*' && last == '*') {
        if (ix [BOLD] >= 0) {
          replaceWithHTML (BOLD, 2);
        } else {
          ix [BOLD] = textIndex - 1;
          textIndex++;
        }
      }
      else
      // _ means emphasis
      if (c == '_' && next != '_' && ix [QUOTE_COLON] < 0) {
        if (ix [EMPHASIS] >= 0) {
          replaceWithHTML (EMPHASIS, 1);
        } else {
          ix [EMPHASIS] = textIndex;
          textIndex++;
        }
      }
      else
      // * means strong
      if (c == '*' && next != '*') {
        if (ix [STRONG] >= 0) {
          replaceWithHTML (STRONG, 1);
        } else {
          ix [STRONG] = textIndex;
          textIndex++;
        }
      }
      else
      // Exclamation points surround image urls
      if (c == '!' && Character.isLetter(next)
          && ix [QUOTE_COLON] < 0 && ix [EXCLAMATION] < 0) {
          // First exclamation point : store its location and move on
          ix [EXCLAMATION] = textIndex;
          textIndex++;
      }
      else
      // Second exclamation point
      if (c == '!'
          && ix [QUOTE_COLON] < 0 && ix [EXCLAMATION] >= 0) {
        // Second exclamation point
        imageTitle = "";
        endOfImageURL = textIndex;
        if (last == ')' && ix [EXCLAMATION_LEFT_PAREN] > 0) {
          ix [EXCLAMATION_RIGHT_PAREN] = textIndex - 1;
          endOfImageURL = ix [EXCLAMATION_LEFT_PAREN];
          imageTitle = line.substring
                (ix [EXCLAMATION_LEFT_PAREN] + 1, ix [EXCLAMATION_RIGHT_PAREN]);
        }
        imageURL = line.substring (ix [EXCLAMATION] + 1, endOfImageURL);
        // Delete the image url, title and parentheses,
        // but leave exclamation points for now.
        lineDelete (ix [EXCLAMATION] + 1, textIndex - ix [EXCLAMATION] - 1);
        String titleString = "";
        if (imageTitle.length() > 0) {
          titleString = " title=\"" + imageTitle + "\" alt=\"" + imageTitle + "\"";
        }
        lineInsert (ix [EXCLAMATION] + 1,
            "<img src=\"" + imageURL + "\"" + titleString + "  />");
        if (next == ':') {
          // Second exclamation followed by a colon -- look for url for link
          ix [QUOTE_COLON] = textIndex;
          ix [LAST_QUOTE] = ix [EXCLAMATION];
        } else {
          lineDelete (ix [EXCLAMATION], 1);
          lineDelete (textIndex, 1);
        }
        ix [EXCLAMATION] = -1;
        ix [EXCLAMATION_LEFT_PAREN] = -1;
        ix [EXCLAMATION_RIGHT_PAREN] = -1;
        textIndex++;
      } // end if second exclamation point
      else
      // Parentheses within exclamation points enclose the image title
      if (c == '(' && ix [EXCLAMATION] > 0 ) {
        ix [EXCLAMATION_LEFT_PAREN] = textIndex;
        textIndex++;
      }
      else
      // Double quotation marks surround linked text
      if (c == '"' && ix [QUOTE_COLON] < 0) {
        if (next == ':'
            && ix [LAST_QUOTE] >= 0
            && textIndex > (ix [LAST_QUOTE] + 1)) {
          ix [QUOTE_COLON] = textIndex;
        } else {
          ix [LAST_QUOTE] = textIndex;
        }
        textIndex++;
      }
      else
      // Flag a left paren inside of a url
      if (c == '(' && ix [QUOTE_COLON] > 0) {
        leftParenPartOfURL = true;
        textIndex++;
      }
      else
      // Space may indicate end of url
      if (Character.isWhitespace (c)
          && ix [QUOTE_COLON] > 0
          && ix [LAST_QUOTE] >= 0
          && textIndex > (ix [QUOTE_COLON] + 2)) {
        int endOfURL = textIndex - 1;
        // end of url is last character of url
        // do not include any trailing punctuation at end of url
        int backup = 0;
        while ((endOfURL > (ix [QUOTE_COLON] + 2))
            && (! Character.isLetterOrDigit (line.charAt(endOfURL)))
            && (! ((line.charAt(endOfURL) == ')') && (leftParenPartOfURL)))
            && (! (line.charAt(endOfURL) == '/'))) {
          endOfURL--;
          backup++;
        }
        String url = line.substring (ix [QUOTE_COLON] + 2, endOfURL + 1);
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
          textIndex = textIndex - backup;
        } else {
          textIndex++;
        }
      }
      else
      // Look for start of definition
      if ((c == ':' || c == ';')
          && Character.isWhitespace(last)
          && Character.isWhitespace(next)
          && listChars.length() > 0
          && (lastDefChar == ';' || lastDefChar == ':')) {
        lineDelete (textIndex - 1, 3);
        lineInsert (closeDefinitionTag (lastDefChar)
            + openDefinitionTag (c));
        lastDefChar = c;
      }
      /* else
      // -- means an em dash
      if (c == '-' && last == '-') {
        textIndex--;
        lineDelete (2);
        lineInsert ("&#8212;");
      } */ else {
        textIndex++;
      }
    }// end while more characters to examine

    context.lastLineBlank = false;

  }

  /**
   Collect any attributes following a block modifier or list item identifier.

   @param end The stopping point for the scan.
   */
  private void collectAttributes (int end) {
    attributeType = 0;
    klass = new StringBuffer();
    id = new StringBuffer();
    style = new StringBuffer();
    language = new StringBuffer();
    int start = textIndex;
    while (textIndex < end
        && ((attributeType > 0) || (textIndex == start))) {
      char c = line.charAt (textIndex);
      switch (attributeType) {
        case 0:
          // No leading character found yet
          if (c == '(') {
            attributeType = 1;
          }
          else
          if (c == '{') {
            attributeType = 3;
          }
          else
          if (c == '[') {
            attributeType = 4;
          }
          break;
        case 1:
          // Left paren indicates class or id
          if (c == '#') {
            attributeType++;
          }
          else
          if (c == ')') {
            attributeType = 0;
          } else {
            klass.append (c);
          }
          break;
        case 2:
          // ID
          if (c == ')') {
            attributeType = 0;
          } else {
            id.append (c);
          }
          break;
        case 3:
          // style
          if (c == '}') {
            attributeType = 0;
          } else {
            style.append (c);
          }
          break;
        case 4:
          // language
          if (c == ']') {
            attributeType = 0;
          } else {
            language.append (c);
          }
          break;
      } // end switch attributeType
      textIndex++;
    } // end while more characters before period
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
      if (klass.length() > 0) {
        listStart.append (" class=\"" + klass + "\"");
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

   @param last The character that started the last span of characters.
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

  private char charAt (String str, int index) {
    if (index < str.length()) {
      return str.charAt (index);
    } else {
      return ' ';
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

  private char textChar () {
    return line.charAt (textIndex);
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
      textIndex--;
    }
    lineDelete (numberOfChars);
    lineInsert ("</" + html [tagType] + ">");
    resetLineIndex (tagType);
  }

  private void lineDelete (int count) {
    line.delete (textIndex, textIndex + count);
  }

  private void lineDelete (int deletePosition, int count) {
    line.delete (deletePosition, deletePosition + count);
    if ((deletePosition + count - 1) < textIndex) {
      textIndex = textIndex - count;
    }
    else
    if (deletePosition < textIndex) {
      textIndex = deletePosition;
    }
    if (textIndex < 0) {
      textIndex = 0;
    }
    // Adjust any affected index positions we've got stored
    for (int i = 0; i < ix.length; i++) {
      if (deletePosition < ix [i]) {
        ix [i] = ix [i] - count;
      }
    }
  }

  private void lineInsert (String insert) {
    line.insert (textIndex, insert);
    textIndex = textIndex + insert.length();
  }

  private void lineInsert (int insertPoint, String insert) {
    line.insert (insertPoint, insert);
    if (insertPoint <= textIndex) {
      textIndex = textIndex + insert.length();
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
  
  /**
     Returns the record definition for the file.
    
     @return Record definition for this tab-delimited file.
   */
  public RecordDefinition getRecDef () {
    return recDef;
  }
  
  /**
     Returns the record number of the last record
     read or written.
    
     @return Number of last record read or written.
   */
  public int getRecordNumber () {
    return recordNumber;
  }
  
  /**
     Doesn't do anything for an HTML file.
   */
  public void setMaxDepth (int maxDepth) {
    
  }
  
  /**
     Retrieves the path to the original source file (if any).
    
     @return Path to the original source file (if any).
   */
  public String getDataParent () {
    if (dataParent == null) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return dataParent;
    }
  }
  
  /**
     Sets the Logger object to be used for logging. 
    
     @param log The Logger object being used for logging significant events.
   */
  public void setLog (Logger log) {
    this.log = log;
  }
  
  /**
     Gets the Logger object to be used for logging. 
    
     @return The Logger object being used for logging significant events.
   */
  public Logger getLog () {
    return log;
  } 
  
  /**
     Sets the option to log all data off or on. 
    
     @param dataLogging True to send all data read or written to the
                        log file.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
  /**
     Gets the option to log all data. 
    
     @return True to send all data read or written to the
             log file.
   */
  public boolean getDataLogging () {
    return dataLogging;
  }
  
  /**
     Sets the file ID to be passed to the Logger.
    
     @param fileId Used to identify the source of the data being logged.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }

  public URL toURL() {
    if (inFile == null) {
      return inURL;
    } else {
      try {
        return inFile.toURI().toURL();
      } catch (MalformedURLException e) {
        return null;
      }
    }
  }

  public String toString() {
    return inName;
  }

  public void setAtEnd (boolean atEnd) {
    this.atEnd = atEnd;
  }

  public boolean isAtEnd() {
    return atEnd;
  }

  public void close () {
    if (reader != null) {
      try {
        reader.close();
      } catch (java.io.IOException e) {
      }
    } else {
      textLineReader.close();
    }
  }
  
}
