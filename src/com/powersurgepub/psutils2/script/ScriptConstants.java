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

package com.powersurgepub.psutils2.script;

/**
 Constants for use in scripting. 

 @author Herb Bowie
 */
public class ScriptConstants {
  
	// Literals used to construct and decode script commands
	public  static final String INPUT_MODULE        = "input";
	public  static final String SORT_MODULE         = "sort";
  public  static final String COMBINE_MODULE      = "combine";
	public  static final String FILTER_MODULE       = "filter";
	public  static final String OUTPUT_MODULE       = "output";
	public  static final String TEMPLATE_MODULE     = "template";
  public  static final String CALLBACK_MODULE     = "callback";
  public  static final String OPEN_ACTION         = "open";
  public  static final String WEB_ROOT_ACTION     = "webroot";
  public  static final String EPUB_IN_ACTION      = "epubin";
  public  static final String EPUB_OUT_ACTION     = "epubout";
  public  static final String SET_ACTION          = "set";
  public  static final String ADD_ACTION          = "add";
  public  static final String CLEAR_ACTION        = "clear";
  public  static final String GENERATE_ACTION     = "generate";
  public  static final String URL_MODIFIER        = "url";
  public  static final String TEXT_MODIFIER       = "text";
  public  static final String NO_MODIFIER         = "";
  public  static final String MERGE_OBJECT        = "merge";
  public  static final String MERGE_SAME_OBJECT   = "same";
  public  static final String NORMAL_OBJECT       = "normalization";
  public  static final String PARAMS_OBJECT       = "params";
  public  static final String AND_OR_OBJECT       = "andor";
  public  static final String DATA_LOSS_OBJECT    = "dataloss";
  public  static final String PRECEDENCE_OBJECT   = "precedence";
  public  static final String MIN_NO_LOSS_OBJECT  = "minnoloss";
  public  static final String USING_DICTIONARY_OBJECT = "usedict";
  public  static final String DIR_DEPTH_OBJECT		= "dirdepth";
  public  static final String EXPLODE_TAGS_OBJECT = "xpltags";
  public  static final String NO_OBJECT           = "";
  public  static final String NO_VALUE            = "";
          
  public ScriptConstants() {
    
  }

}
