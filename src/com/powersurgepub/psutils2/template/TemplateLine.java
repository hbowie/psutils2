/*
 * Copyright 1999 - 2014 Herb Bowie
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

package com.powersurgepub.psutils2.template;

	import com.powersurgepub.psutils2.basic.*;
	import com.powersurgepub.psutils2.logging.*;
	import com.powersurgepub.psutils2.records.*;
	import com.powersurgepub.psutils2.strings.*;

  import java.util.*;
  import java.text.*;
  
/**
   One line in a template. <p>
  
   This code is copyright (c) 1999-2003 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
      2004/06/01 -- Added logic to allow the first operand in an IF command to be recognized
                    properly even when blank and unquoted. Also added code to display template
                    line when an invalid logical operator is detected. <li>
      2003/12/19 -- Added formatting codes to control word demarcation within
                    a variable. Also modified to recognize that, when a sequence 
                    of 3 less than signs exists, the rightmost 2 should be
                    treated as the beginning of the variable. <li>
      2003/07/26 -- Added the letter 'i' (upper- or lower-case) to indicate
                    that an associated case code ('u' or 'l') should be 
                    applied only to the initial character in the variable. <li>
      2003/03/12 -- Added logic to check length variable modifier to see if
                    it is greater than current replacement value: if it is,
                    then the value will be padded on the left with zeroes. <li>
      2003/02/13 -- Added an underscore character as a special variable
                    modifier that will cause spaces to be replaced with
                    underscores. Also modified separator logic to omit  
                    spaces after forwards or backwards slashes. <li>
      2002/11/02 -- Added "today" as a special variable that will be replaced with
                    today's date. Allowed a date format to be entered as a variable
                    modifier. This follows standard Java conventions. <li>
      2002/10/05 -- Added option to include list separator as a variable
                    modifier. <li>
      2002/09/21 -- Added additional logging to show results of commands. <br>
                    Corrected bug in SET command processing. <br>
                    Added a COMMENT command. <li>
      2002/03/21 -- Added group processing for control breaks. <li>
      2000/06/18 -- Added global variables and set command. <li>
      2000/05/28 -- Modified to be consistent with "The Elements of Java Style". <li>
      1999/10/03 -- Added ability to compare two operators for equality 
                    as part of the IF command. Added DELIMS command to change 
                    delimiters from standard << and >>. <li>
      1999/09/02 -- Initial release of class. </ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com">
           www.powersurgepub.com</a>)
  
   @version 
      2004/06/01 -- Added logic to allow the first operand in an IF command to be recognized
                    properly even when blank and unquoted. Also added code to display template
                    line when an invalid logical operator is detected. 
 */
public class TemplateLine {
  
  /** Command for debugging */
  final static String DEBUG     = "debug";

  /** Command for specifying new delimiters. */
  final static String DELIMS    = "delims";
  
  /** Command for specifying output file name. */
  final static String OUTPUT    = "output";
  
  /** Command for initiating an outer loop. */
  final static String OUTER     = "outer";
  
  /** Command for specifying beginning of loop for each data record. */
  final static String NEXTREC   = "nextrec";
	
	/** Command for specifying a group field to be used to control break logic */
	final static String DEFINEGROUP = "definegroup";
	
	/** Command for indicating the beginning of lines to be output at the end of a group */
	final static String IFENDGROUP = "ifendgroup";
	
	/** Commmand for indicating the beginning of lines to be output at the beginning of a group */
	final static String IFNEWGROUP   = "ifnewgroup";
  
  final static String IFENDLIST = "ifendlist";
  
  final static String IFNEWLIST = "ifnewlist";
  
	/** Commmand for a comment line */
	final static String COMMENT   = "*";
  
  /** Command for conditional logic. */
  final static String IF        = "if";
  
  /** Command for conditional logic based on whether a value has changed. */
  final static String IFCHANGE  = "ifchange";
  
  /** Command for conditional else logic. */
  final static String ELSE      = "else";
  
  /** Command for indicating end of conditional logic. */
  final static String ENDIF     = "endif";
  
  /** Command to include text lines from a separate file. */
  final static String INCLUDE   = "include";
  
  /** Command for indicating end of NEXTREC loop. */
  final static String LOOP      = "loop";
  
  /** Command for indicating end of an outer loop. */
  final static String OUTERLOOP = "outerloop";
  
  /** Command for setting a global variable to a new value. */
  final static String SET       = "set";

  /** Command to set the epub flag on. */
  final static String EPUB      = "epub";
  
  /** Indicator to write line out without a trailing line break. */
  final static String NO_LINE_BREAK = "nobr";
  
  /** Default formatting string for dates. */
  final static String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
  
  /** Delimiters used for parsing a command line into a list of parameters. */
  final static String DELIMITERS = " =,;\t\r\n";
      
  /** The delimiter string indicating the beginning of a Template command. */
  private String       startCommand = "<<";
  
  /** The delimiter string indicating the end of a Template command. */
  private String       endCommand = ">>";
  
  /** The delimiter string indicating the beginning of a Template variable. */
  private String       startVariable = "<<";
  
  /** The delimiter string indicating the end of a Template variable. */
  private String       endVariable = ">>";
  
  /** 
     The delimiter string indicating the beginning of 
     one or more variable modifier characters. 
   */
  private String       startModifiers = "&";
  
  /** A complete NEXTREC command line. */
  private String       nextrecLine 
                         = startCommand + NEXTREC + endCommand;
                         
  /** The minimum length for a command line. */
  private int          minCommandLineLength 
                         = startCommand.length() + 1 + endCommand.length();
  
  /** 
     The collection of template utility data and methods 
     passed from the owning template. 
   */
  private  TemplateUtil      templateUtil;
  
  /**
     The collection of global variables, retrieved from templateUtil.
   */
  private  DataRecord        globals;
  
  /** The definition of the operand in a set command. */
  private  DataFieldDefinition operandDef;
  
  /** The template line itself, as a simple string. */
  private  String            lineString;
  
  /** The template line, if a command string. */
  private  String            commandString;
  
  /** The template line as a string tokenizer. */
  private  StringTokenizer   tokens;
  
  /** The next token retrieved from the template line. */
  private  String            nextToken;
  
  /** Is this a command line? */
  private  boolean           commandLine;
  
  /** 
     If this line is a command, then this contains the command without its 
     surrounding delimiters. 
   */
  private  String            command;

  private  CommonMarkup      htmlConverter     = new CommonMarkup ("txt", "html");
  
  private  boolean           lineBreak = true;

  /**
     Constructs a TemplateLine, determining the type of line
     it was handed (command or non-command) and extracting the command 
     itself, if it is a command line.
     
     @param inString   this is the text line itself
     
     @param templateUtil   this is the Template object to which this line
                        belongs.
   */
  public TemplateLine (String inString, TemplateUtil templateUtil) {
    String nonCommand = StringUtils.trimRight (inString);
    lineString = inString.trim();
    if (templateUtil.isFirstTemplateLine()
        && lineString.length() >= 2) {
      String firstTwo = lineString.substring (0, 2);
      if (firstTwo.equals ("<?")) {
        templateUtil.setNlStartCommand ("<?");
        templateUtil.setNlEndCommand ("?>");
        templateUtil.setNlStartVariable ("=$");
        templateUtil.setNlEndVariable ("$=");
        templateUtil.setNlStartModifiers ("&");
      }
    } 
    this.templateUtil = templateUtil;
    globals = templateUtil.getGlobals();
    operandDef  = new DataFieldDefinition ("operand");
    startCommand   = templateUtil.getNlStartCommand();
    endCommand     = templateUtil.getNlEndCommand();
    startVariable  = templateUtil.getNlStartVariable();
    endVariable    = templateUtil.getNlEndVariable();
    startModifiers = templateUtil.getNlStartModifiers();
    command = GlobalConstants.EMPTY_STRING;
    if ((lineString.length() >= minCommandLineLength)
      && (lineString.startsWith (startCommand))
      && (lineString.endsWith (endCommand))) {
      commandString = lineString.substring 
        (startCommand.length(), 
          (lineString.length() - endCommand.length()));
      tokens = new StringTokenizer (commandString, DELIMITERS, true);
      command = GlobalConstants.EMPTY_STRING;
      nextToken = " ";
      while (tokens.hasMoreTokens() 
        && (DELIMITERS.indexOf(nextToken) > -1)) {
        nextToken = tokens.nextToken();
      }
      nextToken = nextToken.toLowerCase ();
      if (nextToken.equals (NEXTREC)
        || nextToken.equals (LOOP)
        || nextToken.equals (OUTER)
        || nextToken.equals (OUTERLOOP)
        || nextToken.equals (OUTPUT) 
        || nextToken.equals (DELIMS)
        || nextToken.equals (INCLUDE)
        || nextToken.equals (EPUB)
        || nextToken.equals (IF) 
        || nextToken.equals (IFCHANGE)
        || nextToken.equals (ELSE)
        || nextToken.equals (ENDIF)
        || nextToken.equals (SET) 
				|| nextToken.equals (DEFINEGROUP)
				|| nextToken.equals (IFNEWGROUP)
				|| nextToken.equals (IFENDGROUP)
        || nextToken.equals (IFNEWLIST)
				|| nextToken.equals (IFENDLIST)
        || nextToken.equals (COMMENT)
        || nextToken.equals (DEBUG)) {
        commandLine = true;
        command = nextToken;
      } // end if valid command
    } // end if possible command
    if (! commandLine) {
      lineString = nonCommand;
    }
  } // end TemplateLine constructor
  
  /**
     Generates the output associated with a TemplateLine. 
     For an output command, this will consist of closing the current 
     output file (if one is already open) and opening the new one,
     using as a file name the data supplied with the output command
     (which may, in turn, have contained variable data that was replaced
     with its corresponding data values). For a non-command line, 
     appropriate output is the writing of the line itself, after variable
     substitution has been accomplished.
    
     @param dataRec  a collection of fields supplying substitution data
                     for variables in a template line.
   */  
  public void generateOutput (DataRecord dataRec) {

    String outString = lineString;
    
    // now do something to the output file
    if (this.isCommandLine()) {
      String operands = outString.substring
        ((startCommand.length() + this.command.length()), 
          (outString.length() - endCommand.length())).trim();
      templateUtil.tailorEvent (LogEvent.NORMAL,
        "Processing " + command + " Command", true);
      StringScanner operandScanner = new StringScanner (operands);
      if (templateUtil.debugging()) {
        templateUtil.sendDebugLine(lineString);
      }
      
      
      // DELIMS Command
      if (this.command.equals (DELIMS)) {
        templateUtil.recordEvent();
        int delimsCount = 1;
        while (operandScanner.moreChars()) {
          String delim = operandScanner.extractQuotedString();
          switch (delimsCount) {
            case 1:
              templateUtil.setNlStartCommand (delim);
              break;
            case 2:
              templateUtil.setNlEndCommand (delim);
              break;
            case 3:
               templateUtil.setNlStartVariable (delim);
               break;
             case 4:
               templateUtil.setNlEndVariable (delim);
               break;
             case 5:
              templateUtil.setNlStartModifiers (delim);
              break;
            default:
              templateUtil.recordEvent (LogEvent.MINOR,
                "DELIM Command has excess parameters: " + delim, true);
          } // end switch 
          delimsCount++;
        } // end while
        templateUtil.recordEvent (LogEvent.NORMAL,
            "DELIM Command Results: " 
                + templateUtil.getNlStartCommand() + " "
                + templateUtil.getNlEndCommand() + " "
                + templateUtil.getNlStartVariable() + " "
                + templateUtil.getNlEndVariable() + " "
                + templateUtil.getNlStartModifiers(), 
                    true);
      } // end delims command processing
      
      // DEBUG Command
      if (this.command.equals (DEBUG)) {
        templateUtil.setDebug(true);
        templateUtil.sendDebugLine(lineString);
      }
      else 
        
      // OUTPUT Command
      if (this.command.equals (OUTPUT)) {
        if (! templateUtil.isSkippingData()) {
          // templateUtil.recordEvent();
          templateUtil.setTextFileOutName 
              (replaceVarsInOperand (operandScanner, dataRec));
        } // end skippingData test
      } // end output command processing
      else
        
      // INCLUDE Command
      if (this.command.equals (INCLUDE)) {
        if (! templateUtil.isSkippingData()) {
          String includeFile = replaceVarsInOperand (operandScanner, dataRec);
          String includeParm = replaceVarsInOperand (operandScanner, dataRec);
          templateUtil.includeFile (includeFile, includeParm, dataRec);
        } // end skippingData test
      } // end include command processing
      else

      // EPUB Command
      if (this.command.equals (EPUB)) {
        templateUtil.setEpub (true);
        templateUtil.setEpubSite (replaceVarsInOperand (operandScanner, dataRec));
      }
      else
        
      // ELSE Command
      if (this.command.equals (ELSE)) {
        templateUtil.anElse();
        templateUtil.sendIfStateDebugLine();
      }
      else

      // ENDIF Command
      if (this.command.equals (ENDIF)) {
        templateUtil.anotherEndIf();
        templateUtil.sendIfStateDebugLine();
      } // end endif command processing
      else
      
      // IF Command
      if (this.command.equals (IF)) {
        if (templateUtil.isSkippingData()) {
          templateUtil.anotherIf();
        } else {
          String opcode = "!=";
          DataField operand1 = new DataField (operandDef, "");
          DataField operand2 = new DataField (operandDef, "");
          int opCount = 0;
          boolean ifResult = false;
          while (operandScanner.moreChars()) {
            String op 
              = replaceVarsInOperand (operandScanner, dataRec);
            opCount++;
            
            // If first operand looks like a logical operator,
            // then assume that data operand was an unquoted empty string,
            // and treat this as a logical operator.
            if (opCount == 1 
                && op.length() >= 1
                && op.length() <= 2
                && (! Character.isLetter (op.charAt (0)))
                && (! Character.isDigit  (op.charAt (0)))) {
              int opIndex = -1;
              for (opIndex = 0;
                (opIndex < DataField.NUMBER_OF_LOGICAL_OPERANDS)
                  && (! op.equals 
                      (DataField.SYMBOL_LOGICAL_OPERANDS [opIndex]));
                  opIndex++) {
                // Do nothing -- for statement does all the work
              } // end for every possible logical operand using symbols
              if (opIndex >= DataField.NUMBER_OF_LOGICAL_OPERANDS) {
                for (opIndex = 0;
                  (opIndex < DataField.NUMBER_OF_LOGICAL_OPERANDS)
                    && (! op.equals 
                        (DataField.ALT_SYMBOL_LOGICAL_OPERANDS [opIndex]));
                    opIndex++) {
                  // Do nothing -- for statement does all the work
                } // end for every possible alt symbol operands
              } // end operator not found among symbol operands
              if (opIndex < DataField.NUMBER_OF_LOGICAL_OPERANDS) {
                opCount = 2;
              }
            } // end possible operator
            
            switch (opCount) {
              case 1:
                operand1.setData (op);
                break;
              case 2:
                opcode = op;
                break;
              default:
                operand2.setData (op);
                boolean thisIf;
                try {
                  thisIf = operand1.operateLogically (opcode, operand2);
                  /* templateUtil.recordEvent (LogEvent.NORMAL,
                    "IF Command Results: " 
                    + String.valueOf (thisIf) + " ("
                    + operand1.toString() + " "
                    + opcode + " "
                    + operand2.toString() + ")", 
                    false); */
                }
                catch (IllegalArgumentException e) {
                  templateUtil.recordEvent (LogEvent.MEDIUM, 
                    "Illegal logical operator (" + opcode + ")"
                    + " for line " + outString, true);
                  thisIf = true;
                }
                if (thisIf) {
                  ifResult = true;
                }
                break;
            } // end switch
          } // end while more chars for if command
          if (opCount < 2) {
            ifResult = (operand1.length() > 0);
          } else
          if (opCount < 3) {
            boolean thisIf;
            try {
              thisIf = operand1.operateLogically (opcode, operand2);
            }
            catch (IllegalArgumentException e) {
              templateUtil.recordEvent (LogEvent.MEDIUM, 
                "Illegal logical operator (" + opcode + ")", true);
              thisIf = true;
            } // end try/catch
            ifResult = thisIf;
          }
          templateUtil.sendDebugLine
              ("Result of If evaluation = " + String.valueOf(ifResult));
          templateUtil.setSkippingData (! ifResult);
        }
        templateUtil.sendIfStateDebugLine();
      } // end if command processing
      else
      
      // IFCHANGE Command
      if (this.command.equals (IFCHANGE)) {
        if (templateUtil.isSkippingData()) {
          templateUtil.anotherIf();
        } else {
          templateUtil.setIfChangeData 
              (replaceVarsInOperand (operandScanner, dataRec));
        }
      } // end ifchange command processing
      else
      
      // SET Command
      if (this.command.equals (SET)) {
        if (! templateUtil.isSkippingData()) {
          String opcode = "";
          String global = "";
          DataField operand1 = new DataField (operandDef, "");
          int opCount = 0;
          while (operandScanner.moreChars()) {
            String op 
              = replaceVarsInOperand (operandScanner, dataRec);
            opCount++;
            switch (opCount) {
              case 1:
                global = op;
                break;
              case 2:
                opcode = op;
                break;
              case 3:
                operand1.setData (op);
                break;
              default:
                templateUtil.recordEvent (LogEvent.MINOR,
                  "SET Command has excess parameters: " + op, false);
                break;
            } // end switch
          } // end while more chars for set command
          // System.out.println("TemplateLine.GenerateOutput set command with "
          //     + String.valueOf(opCount) + " operands, "
          //     + "global variable = " + global
          //     + " op code = " + opcode
          //     + " operand 1 = " + operand1);
          if (opCount < 2) {
            templateUtil.recordEvent (LogEvent.MINOR,
                  "SET Command does not have enough parameters", false);
          } else {
            DataField globalField;
            int globalColumn = globals.getColumnNumber(global);
            // System.out.println("  global column = " + String.valueOf(globalColumn));
            if (globalColumn >= 0) {
              globalField = globals.getField (globalColumn);
            }
            else {
              DataFieldDefinition globalDef = new DataFieldDefinition (global);
              globalField = new DataField (globalDef, "0");
              globals.addField (globalField);
            }
            if (opCount == 2) {
              globalField.operate (opcode);
            } else {
              globalField.operate (opcode, operand1);
            }
            /* templateUtil.recordEvent (LogEvent.NORMAL,
            "SET Command Results: " 
                + global + " = "
                + globals.getField(global).getData(), 
                    false); */
          } // end processing SET operands
        } // end if not skipping data
      } // end SET command processing
			else
      
      // DEFINEGROUP Command
      if (this.command.equals (DEFINEGROUP)) {
        templateUtil.clearIfs();
				int groupNumber = 0;
				String groupValue = "";
				if (operandScanner.moreChars()) {
					groupNumber = operandScanner.extractInteger(templateUtil.MAX_GROUPS); 
        }
				if (operandScanner.moreChars()) {
					groupValue = replaceVarsInOperand (operandScanner, dataRec);
				}
				if ((groupNumber > 0) && (groupNumber <= templateUtil.MAX_GROUPS)) {
					templateUtil.setGroup (groupNumber - 1, groupValue);
				}
			} // end DEFINEGROUP command processing
			else
      
      // IFENDGROUP Command
      if (this.command.equals (IFENDGROUP)) {
        templateUtil.clearIfs();
        int groupNumber = 0;
        if (operandScanner.moreChars()) {
          groupNumber = operandScanner.extractInteger(templateUtil.MAX_GROUPS); 
        }
        if ((groupNumber > 0) && (groupNumber <= templateUtil.MAX_GROUPS)) {
          templateUtil.setIfEndGroup (groupNumber - 1);
        }
			} // end IFENDGROUP command processing
			else
      
      // IFNEWGROUP Command
      if (this.command.equals (IFNEWGROUP)) {
        templateUtil.clearIfs();
        int groupNumber = 0;
        if (operandScanner.moreChars()) {
          groupNumber = operandScanner.extractInteger(templateUtil.MAX_GROUPS); 
        }
        if ((groupNumber > 0) && (groupNumber <= templateUtil.MAX_GROUPS)) {
          templateUtil.setIfNewGroup (groupNumber - 1);
        }
			} 
      else 
      
      // IFENDLIST Command
      if (this.command.equals (IFENDLIST)) {
        templateUtil.clearIfs();
        int groupNumber = 0;
        if (operandScanner.moreChars()) {
          groupNumber = operandScanner.extractInteger(templateUtil.MAX_GROUPS); 
        }
        if ((groupNumber > 0) && (groupNumber <= templateUtil.MAX_GROUPS)) {
          templateUtil.setIfEndList (groupNumber - 1);
        }
			} // end IFENDLIST command processing
			else
      
      // IFNEWLIST Command
      if (this.command.equals (IFNEWLIST)) {
        templateUtil.clearIfs();
        int groupNumber = 0;
        if (operandScanner.moreChars()) {
          groupNumber = operandScanner.extractInteger(templateUtil.MAX_GROUPS); 
        }
        if ((groupNumber > 0) && (groupNumber <= templateUtil.MAX_GROUPS)) {
          templateUtil.setIfNewList (groupNumber - 1);
        }
			} // end IFNEWLIST command processing
    } // end command line processing
    else 
    
    // not a command line
    { 
      if (templateUtil.isSkippingData()) {
        // do nothing
      }
      else {
        LineWithBreak lineWithBreak = templateUtil.replaceVariables
            (new StringBuilder(outString), dataRec);
        if (lineWithBreak.getLineBreak()) {
          templateUtil.writeLine (lineWithBreak.getLine());
        } else {
          templateUtil.write (lineWithBreak.getLine());
        }
      } // end not skipping data
    } // end non-command line
  } // end generateOutput method
  
  /**
   Get next quoted string from StringScanner, then replace any variables
   found within. 
  
   @param opScanner The string to be scanned for the next operand. 
   @param dataRec   The data record containing the variable values. 
  
   @return The next operand, with variable replacements completed. 
  */
  private String replaceVarsInOperand(StringScanner opScanner, DataRecord dataRec) {
    LineWithBreak lineWithBreak = templateUtil.replaceVariables (
        new StringBuilder(opScanner.extractQuotedString()),
        dataRec);
    return lineWithBreak.getLine();
  }
  
  /**
     Returns the command extracted from a command line.
    
     @return Command without its surrounding delimiters.
   */
  public String getCommand () { return command; }
  
  /**
     Is this a command line?
    
     @return True if this is a command line. 
   */
  public boolean isCommandLine() { return commandLine; }
  
  /**
     Returns this object as some kind of string.
    
     @return The entire original line as a string.
   */
  public String toString () {
    return lineString;
  }
  
} // enc class TemplateLine
