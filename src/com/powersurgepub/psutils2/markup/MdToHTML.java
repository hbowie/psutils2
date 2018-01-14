/*
 * Copyright 2016 - 2017 Herb Bowie
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

  import com.vladsch.flexmark.ast.*;
  import com.vladsch.flexmark.ext.definition.*;
  import com.vladsch.flexmark.ext.tables.*;
  import com.vladsch.flexmark.ext.typographic.*;
  import com.vladsch.flexmark.html.*;
  import com.vladsch.flexmark.parser.*;
  import com.vladsch.flexmark.util.options.*;

  import java.util.*;

/**
 Converts markdown text to HTML. 

 @author Herb Bowie
 */
public class MdToHTML {
  
  private static MdToHTML mdToHTML = null;
  private MutableDataSet options = null;
  private Parser parser = null;
  private HtmlRenderer renderer = null;
  
  /**
   Return a standard, shared instance for converting Markdown to HTML, using 
   standard options. 
  
   @return A standard, shared instance. 
  */
  public static MdToHTML getShared() {
    if (mdToHTML == null) {
      mdToHTML = new MdToHTML();
    }
    return mdToHTML;
  }
  
  /** 
   Construct an instance with the standard options. 
  
   Options include definitions, tables, and typographic conversions. 
  */
  public MdToHTML() {
    options = new MutableDataSet();
    options.set(Parser.EXTENSIONS, Arrays.asList(
        DefinitionExtension.create(),
        TablesExtension.create(), 
        TypographicExtension.create()));
    parser = Parser.builder(options).build();
    renderer = HtmlRenderer.builder(options).build();
  }
  
  /**
   Convert Markdown source to HTML. 
  
   @param md Source written in Markdown. 
  
   @return A string containing the equivalent HTML. 
   */
  public String markdownToHtml(String md) {
    Node document = parser.parse(md);
    String html = renderer.render(document); 
    return html;
  }

}
