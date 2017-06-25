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

package com.powersurgepub.psutils2.records;

  import com.powersurgepub.psutils2.strings.*;

/**

   Another name by which a DataFieldDefinition may be known. <p>

   

   This code is copyright (c) 1999-2000 by Herb Bowie of PowerSurge Publishing. 

   All rights reserved. <p>

   

   Version History: <ul><li>

      00/05/02 - Modified to be consistent with "The Elements of Java Style". </ul>

  

   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">

           herb@powersurgepub.com</a>)<br>

           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">

           www.powersurgepub.com/software</a>)

  

   @version 00/05/05 - Added references to CommonName class.

 */

public class DataFieldAlias {



  /** The alias (or AKA) by which the data field is known. */

  private    CommonName           alias;

  

  /** The original name for the data field. */

  private    CommonName           original;

  

  /**

     The constructor sets the two fields, using CommonName

     to store both of them in their lowest common denominator format.

    

     @param alias The AKA for the field.

    

     @param original The original name for the field.

   */

  public DataFieldAlias (String alias, String original) {

    this.alias = new CommonName (alias);

    setOriginal (original);

  }

  

  /**

     Returns the alias for the field.

    

     @return The alias for the field.

   */

  public CommonName getAlias () {

    return alias;

  }

  

  /**

     Returns the original name of the field.

    

     @return Original name of the field.

   */

  public CommonName getOriginal () {

    return original;

  }

  

  /**

     Sets the original name for the field.

    

     @param original Original name for the field.

   */

  public void setOriginal (String original) {

    this.original = new CommonName (original);

  }

  

  /**

     Returns a string identifier for the field.

    

     @return alias plus the literal " is an alias for " plus the original name.

   */

  public String toString () {

    return alias + " is an alias for " + original;

  }

  

} // end class DataFieldAlias