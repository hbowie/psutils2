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
   An object representing an HTML tag and all its attributes. <p>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
 */

  import java.util.*;

public class HTMLTag {
	
	/**
	   The name of the tag.
	 */
  private String			name			= "";

	/**
	   Starting or ending (/) tag?
	 */
  private boolean			ending	    = false;

	/**
	   Text found since last tag. 
	 */
  private String			precedingText = "";
  
  /**
     Is this a heading tag (h1 ... h6)?
   */
  private boolean			headingTag = false;

  /**
     Is this a block tag?
   */
  private boolean     blockTag = false;
  
  /**
     Heading level of current or last heading tag.
   */
  private int         headingLevel = 0;
  
  /** Is this a tag that is starting a list? */
  private boolean     listStart = false;
  
  /** Is this a tag that is ending a list? */
  private boolean     listEnd   = false;
  
  /** Is there an active dt (definition term) above us? */
  private boolean     defTermActive = false;
  
  /** 
     The list level applicable to the preceding text (before the effect of this tag).
     Zero means no list, 1 means list, 2 means a list within a list, etc.
   */
  private int					listLevel = 0;
  
  /**
     Set to name of preceding list item tag, if still open.
   */
  private String			listItemTag = "";
  
  /**
     Indicates whether this tag starts a list item.
   */
  private boolean			listItemStart = false;
  
  /**
     Indicates whether this tag closes a list item, either implicitly or explicitly.
   */
  private boolean			listItemEnd = false;
	
	/**
	   The list of attributes for the HTML tag.
	 */
  private Hashtable		attributes;
  
  /**
     The list of keys, in the order in which they were originally found in the HTML.
   */
  private Vector      attributeKeys;

	/**
	   Constructor.
	 */
	public HTMLTag () {
		attributes = new Hashtable ();
    attributeKeys = new Vector ();
	}

  /**
	   Sets the tag name and any dependent fields.
	  
	   @param tag the name of this tag.
	 */
  public void setName (String name) {
    this.name = name.toLowerCase();
    headingTag = false;
    headingLevel = 0;
    if (this.name.length() == 2
        && this.name.charAt(0) == 'h') {
      try {
        headingLevel = Integer.parseInt (this.name.substring(1));
        headingTag = true;
      } catch (NumberFormatException e) {
        // no action necessary
      }
    } // end if possible heading
    listStart = false;
    listEnd   = false;
    defTermActive = false;
    if (this.name.equals ("ol")
        || this.name.equals ("ul")
        || this.name.equals ("dl")) {
      if (ending) {
        listEnd = true;
      } else {
        listStart = true;
      }
    }
    blockTag = (name.equals ("p")
        || headingTag);
  } // end setName method

  /**
	   Sets the ending value to true.
	 */
  public void setEnding () {
    this.ending = true;
    if (listStart) {
      listEnd = true;
      listStart = false;
    } 
  }

  /**
	   Sets the preceding text.
	  
	   @param precedingText text preceding this tag, since the last tag.
	 */
  public void setPrecedingText (String precedingText) {
    this.precedingText = precedingText;
  }
  
  /**
	   Sets the heading level.
	  
	   @param heading level of preceding text.
	 */
  public void setHeadingLevel (int headingLevel) {
    this.headingLevel = headingLevel;
  }
  
  /**
	   Sets the list level.
	  
	   @param list level of preceding text.
	 */
  public void setListLevel (int listLevel) {
    this.listLevel = listLevel;
  }
  
  /**
	   Sets the tag name of an open list item (li, dt, dl).
	  
	   @param list item tag name.
	 */
  public void setListItemTag (String listItemTag) {
    this.listItemTag = listItemTag;
  }
  
  /**
	   Sets a flag indicating that a definition term (dt tag) is active.
	  
	   @param dt active flag.
	 */
  public void setDefTermActive (boolean defTermActive) {
    this.defTermActive = defTermActive;
  }
  
  /**
    Are we beneath an active dt (definition term) tag?
   
    @return True if we are beneath an active dt tag.
   */
  public boolean isDefTermActive () {
    return defTermActive;
  }
  
  /**
	   Sets a flag indicating the start of a list item.
	  
	   @param list item start flag.
	 */
  public void setListItemStart (boolean listItemStart) {
    this.listItemStart = listItemStart;
  }
  
  /**
	   Sets a flag indicating the end of an open list item.
	  
	   @param list item end flag.
	 */
  public void setListItemEnd (boolean listItemEnd) {
    this.listItemEnd = listItemEnd;
  }
	
	/**
	   Sets an attribute.
	  
	   @param attribute an attribute for this tag.
	 */
  public void setAttribute (HTMLAttribute attribute) {
    Object obj = attributes.put (attribute.getName(), attribute);
    attributeKeys.addElement (attribute.getName());
  }
  
	/*
	   Checks to see if the tag has a name (otherwise just preceding text).
	  
	   @return true if tag has a name.
	 */
	public boolean hasName () {
		return (name.length() > 0);
	}

	/*
	   Returns the name of the tag.
	  
	   @return name of tag.
	 */
	public String getName () {
		return name;
	}

	/*
	   Returns the ending value.
	  
	   @return true if this is an ending tag.
	 */
	public boolean isEnding () {
		return ending;
	}

  public boolean hasPrecedingText() {
    return (precedingText.length() > 0);
  }

	/**
	   Returns the text preceding the tag.
	  
	   @return text preceding tag.
	 */
	public String getPrecedingText () {
		return precedingText;
	}
  
  /**
	   Is this a heading tag?
	  
	   @return true if this is a heading tag.
	 */
  public boolean isHeadingTag () {
    return headingTag;
  }

  /**
     Is this a block tag?

     @return true if this is a block tag.
   */
  public boolean isBlockTag () {
    return blockTag;
  }
  
  /**
	   Gets the heading level.
	  
	   @return level of last heading (current or prior tag).
	 */
  public int getHeadingLevel () {
    return headingLevel;
  }
  
  /**
	   Gets the list level.
	  
	   @return list level of preceding text.
	 */
  public int getListLevel () {
    return listLevel;
  }
  
  /**
	   Gets the tag name of an open list item (li, dt, dl).
	  
	   @return list item tag name.
	 */
  public String getListItemTag () {
    return listItemTag;
  }

  public boolean isListTag () {
    return (isListStart()
        || isListEnd()
        || isListItemStart()
        || isListItemEnd());
  }
  
  /**
	   Gets a flag indicating the start of a list.
	  
	   @return list start flag.
	 */
  public boolean isListStart () {
    return listStart;
  }
  
  /**
	   Gets a flag indicating the end of an open list.
	  
	   @return list end flag.
	 */
  public boolean isListEnd () {
    return listEnd;
  }
  
  /**
	   Gets a flag indicating the start of a list item.
	  
	   @return list item start flag.
	 */
  public boolean isListItemStart () {
    return listItemStart;
  }
  
  /**
	   Gets a flag indicating the end of an open list item.
	  
	   @return list item end flag.
	 */
  public boolean isListItemEnd () {
    return listItemEnd;
  }

  /**
   Return the value for the specified attribute.

   @param name The name of the desired attribute.
   @return The associated value if the attribute exists, otherwise null. 
   */
  public String getAttributeValue (String name) {
    HTMLAttribute attr = getAttribute(name);
    if (attr == null) {
      return null;
    } else {
      return attr.getValue();
    }
  }
  
	/**
	   Gets an attribute based on its name.
	  
	   @return the corresponding HTML Attribute, or null, if the attribute name
             could not be found. 
     @param  the name of an attribute.
	 */
  public HTMLAttribute getAttribute (String name) {
    return (HTMLAttribute)attributes.get(name);
  }
  
  public Enumeration getAttributes () {
    return attributes.elements();
  }
  
	/**
	   Checks to see if a given attribute exists within this tag.
	  
	   @return true if the attribute is found. 
     @param  the name of an attribute.
	 */
  public boolean containsAttribute (String name) {
    return attributes.containsKey(name);
  }

	/*
	   Determines equality.
	  
	   @return true if lower-case tag names are the same and they have
	           the same ending value.
	  
	   @param tag2 second HTMLTag object.
	 */
	public boolean equals (HTMLTag tag2) {
		return ((getName().equals (tag2.getName()))
					&& (isEnding() == tag2.isEnding()));
	}
  
  /**
     Returns next attribute. StartWithFirstAttribute should be invoked first.
    
     @return HTML attribute or null, if no more.
   */
  public Enumeration enumerateAttributes () {
    return new AttributeEnum();
  }
	
	/*
	   Returns the tag in string form.
	  
	   @return tag formatted as a string
	 */
	public String toString() {
		StringBuffer work = new StringBuffer ("");
    if (getListLevel() > 0) {
      work.append ("List level: " + String.valueOf (getListLevel()) + "\n");
    }
    if (getListItemTag().length() > 0) {
      work.append ("List Item Tag: " + getListItemTag() + "\n");
    }
    if (isListItemEnd()) {
      work.append ("End of List Item \n");
    }
    work.append (getPrecedingText() + "\n");
    if (hasName()) {
      work.append (getHTMLTag());
      work.append ("\n");
    }
    return work.toString();
	}

	public String getHTMLTag() {
		StringBuffer work = new StringBuffer ("");
    if (hasName()) {
      work.append ("<");
      if (isEnding()) {
        work.append ("/");
      }
      work.append (getName());
      Enumeration attrs = enumerateAttributes();
      while (attrs.hasMoreElements()) {
        HTMLAttribute attr = (HTMLAttribute)attrs.nextElement();
        work.append (" " +   attr.getName());
        if (attr.hasValue()) {
          work.append ("=" + attr.getValueWithQuotesIfNeeded());
        }
      }
      work.append (">");
    }
    return work.toString();
	}
  
  class AttributeEnum implements Enumeration {
  
    private int index;
    
    AttributeEnum () {
      index = 0;
    }
    
    public boolean hasMoreElements() {
      return (index < attributeKeys.size());
    }
    
    public Object nextElement() {
      if (hasMoreElements()) {
        String key = (String)attributeKeys.elementAt(index++);
        return (HTMLAttribute)attributes.get(key);
      }
      else {
        throw new NoSuchElementException();
      }
    }
  }
}

