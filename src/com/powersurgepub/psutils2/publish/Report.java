/*
 * Copyright 2016 - 2016 Herb Bowie
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

package com.powersurgepub.psutils2.publish;

  import com.powersurgepub.psutils2.files.*;
  import com.powersurgepub.psutils2.strings.*;

  import java.io.*;

/**
 The necessary information and methods to produce a single report. 

 @author Herb Bowie
 */
public class Report {
  
  public final static String TDFCZAR_FILE_EXTENSION = ".tcz";
  
  private File    reportScript = null;
  private String  reportKey = null;
  private String  reportName = null;
  private String  reportHTML = null;
  
  /**
   Construct a Report object, given a script file to generate it. 
  
   @param reportScript  The PSTextMerge script file that will generate the
                        report when requested. 
  */
  public Report(File reportScript) {
    this.reportScript = reportScript;
    FileName reportFileName = new FileName(reportScript);
    reportName = reportFileName.getFileNameEnglish();
    reportKey = StringUtils.commonName(reportFileName.getBase());
    reportHTML = reportFileName.replaceExt("html");
  }
  
  /**
   Get the report script file. 
  
   @return the report script file. 
  */
  public File getFile() {
    return reportScript;
  }
  
  /**
   Get the key used to identify the report. This will be the report name,
   an all lower-case, with white space and punctuation removed. 
  
   @return the report key. 
  */
  public String getKey() {
    return reportKey;
  }
  
  /**
   Get the name of the report. 
  
   @return report name. 
  */
  public String getName() {
    return reportName;
  }
  
  /**
   Get the file name of the report script file, without the preceding path 
   info. 
  
   @return the file name of the report script file. 
  */
  public String getFileName() {
    return reportScript.getName();
  }
  
  public String getHTMLName() {
    return reportHTML;
  }
  
  /**
   Check to see if the passed file appears to be a valid report script file. 
  
   @param candidate The file to be evaluated. 
  
   @return True if it appears to be a valid report script file; false otherwise. 
  */
  public static boolean isValidFile(File candidate) {
    return (candidate != null
        && candidate.exists()
        && candidate.canRead()
        && candidate.isFile()
        && (! candidate.isHidden())
        && candidate.getName().endsWith(TDFCZAR_FILE_EXTENSION));
  }
  
}
