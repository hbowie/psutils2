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

  import java.util.*;
  
/**
   A rule for formatting a telephone
   number field into a standard format. The standard format is 
   "aaa-xxx-nnnn", where "aaa" is the area code, "xxx" is the local
   exchange, and "nnnn" is the rest of the phone number. <p>
  
   This class also contains code
   to convert old 602 prefixes into one of the three new prefixes
   recently introduced for the Phoenix area. <p>
   
   This code is copyright (c) 1999-2000 by Herb Bowie of PowerSurge Publishing. 
   All rights reserved. <p>
   
   Version History: <ul><li>
      00/04/24 - Modified to be consistent with "The Elements of Java Style".
                 Also modified to remove the leading 1 from what was the 
                 standard phone number format.
                 Also modified to allow more code to be shared with USMobileRule,
                 by creating new method transformMobile.</ul>
  
   @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
           herb@powersurgepub.com</a>)<br>
           of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
           www.powersurgepub.com/software</a>)
  
   @version 00/05/23 - Fixed miscellaneous bugs in new code from 00/04/24.
 */
public class USPhoneRule
  extends DataFormatRule {

  /** ID for this DataFormatRule descendent. */
  public static final String US_PHONE_RULE_CLASS_NAME = "USPhoneRule";
  
  /** Correct length for standard format phone number. */
  public  final static int STANDARD_LENGTH = 12;
  
  /** 
     Standard number used as prefix to the area code (for long-distance 
     dialing).
   */
  private    char       areaCodePrefix = '1';
  
  /** Default area code */
  private    String     defaultAreaCode = "602";
  
  /** A dash (or hyphen) stored as a string. */
  private    String     dashString = "-";
  
  /** A dash (or hyphen) stored as a character. */
  private    char       dashChar = '-';
  
  /** 
     A list of area codes and prefixes and their conversion rules. 
     The key is a string containing a 3-digit area code, a dash, 
     a 3-digit exchange, and an optional "M" to indicate a mobile
     (cell phone or pager) number. The data is the new area code.
   */
  private    Hashtable areaCodes;
  
  /**
     The no-arg constructor builds the areaCodes conversion table, and
     fills it with the 602 conversion data from US West.
   */
  public USPhoneRule () {
    super ();
    areaCodes = new Hashtable();

    areaCodes.put("602-200", "602");
    areaCodes.put("602-201", "602");
    areaCodes.put("602-202", "602");
    areaCodes.put("602-203", "602");
    areaCodes.put("602-204", "602");
    areaCodes.put("602-205", "602");
    areaCodes.put("602-206", "602");
    areaCodes.put("602-207", "602");
    areaCodes.put("602-208", "602");
    areaCodes.put("602-209", "602");
    areaCodes.put("602-210", "602");
    areaCodes.put("602-212", "602");
    areaCodes.put("602-213", "602");
    areaCodes.put("602-215", "602");
    areaCodes.put("602-216", "602");
    areaCodes.put("602-217", "602");
    areaCodes.put("602-219", "602");
    areaCodes.put("602-220", "602");
    areaCodes.put("602-221", "602");
    areaCodes.put("602-222", "602");
    areaCodes.put("602-223", "602");
    areaCodes.put("602-224", "602");
    areaCodes.put("602-225", "602");
    areaCodes.put("602-226", "602");
    areaCodes.put("602-227", "602");
    areaCodes.put("602-228", "602");
    areaCodes.put("602-229", "602");
    areaCodes.put("602-230", "602");
    areaCodes.put("602-231", "602");
    areaCodes.put("602-232", "602");
    areaCodes.put("602-233", "602");
    areaCodes.put("602-234", "602");
    areaCodes.put("602-235", "602");
    areaCodes.put("602-236", "602");
    areaCodes.put("602-237", "602");
    areaCodes.put("602-238", "602");
    areaCodes.put("602-239", "602");
    areaCodes.put("602-240", "602");
    areaCodes.put("602-241", "602");
    areaCodes.put("602-242", "602");
    areaCodes.put("602-243", "602");
    areaCodes.put("602-244", "602");
    areaCodes.put("602-246", "602");
    areaCodes.put("602-248", "602");
    areaCodes.put("602-249", "602");
    areaCodes.put("602-250", "602");
    areaCodes.put("602-251", "602");
    areaCodes.put("602-252", "602");
    areaCodes.put("602-253", "602");
    areaCodes.put("602-254", "602");
    areaCodes.put("602-255", "602");
    areaCodes.put("602-256", "602");
    areaCodes.put("602-257", "602");
    areaCodes.put("602-258", "602");
    areaCodes.put("602-259", "602");
    areaCodes.put("602-261", "602");
    areaCodes.put("602-262", "602");
    areaCodes.put("602-263", "602");
    areaCodes.put("602-264", "602");
    areaCodes.put("602-265", "602");
    areaCodes.put("602-266", "602");
    areaCodes.put("602-267", "602");
    areaCodes.put("602-268", "602");
    areaCodes.put("602-269", "602");
    areaCodes.put("602-270", "602");
    areaCodes.put("602-271", "602");
    areaCodes.put("602-272", "602");
    areaCodes.put("602-273", "602");
    areaCodes.put("602-274", "602");
    areaCodes.put("602-275", "602");
    areaCodes.put("602-276", "602");
    areaCodes.put("602-277", "602");
    areaCodes.put("602-278", "602");
    areaCodes.put("602-279", "602");
    areaCodes.put("602-280", "602");
    areaCodes.put("602-284", "602");
    areaCodes.put("602-285", "602");
    areaCodes.put("602-286", "602");
    areaCodes.put("602-287", "602");
    areaCodes.put("602-289", "602");
    areaCodes.put("602-290", "602");
    areaCodes.put("602-291", "602");
    areaCodes.put("602-292", "602");
    areaCodes.put("602-294", "602");
    areaCodes.put("602-295", "602");
    areaCodes.put("602-296", "602");
    areaCodes.put("602-297", "602");
    areaCodes.put("602-298", "602");
    areaCodes.put("602-302", "602");
    areaCodes.put("602-304", "602");
    areaCodes.put("602-305", "602");
    areaCodes.put("602-306", "602");
    areaCodes.put("602-307", "602");
    areaCodes.put("602-309", "602");
    areaCodes.put("602-310", "602");
    areaCodes.put("602-313", "602");
    areaCodes.put("602-315", "602");
    areaCodes.put("602-316", "602");
    areaCodes.put("602-318", "602");
    areaCodes.put("602-319", "602");
    areaCodes.put("602-320", "602");
    areaCodes.put("602-321", "602");
    areaCodes.put("602-323", "602");
    areaCodes.put("602-326", "602");
    areaCodes.put("602-329", "602");
    areaCodes.put("602-330", "602");
    areaCodes.put("602-331", "602");
    areaCodes.put("602-332", "602");
    areaCodes.put("602-335", "602");
    areaCodes.put("602-336", "602");
    areaCodes.put("602-338", "602");
    areaCodes.put("602-339", "602");
    areaCodes.put("602-340", "602");
    areaCodes.put("602-341", "602");
    areaCodes.put("602-343", "602");
    areaCodes.put("602-344", "602");
    areaCodes.put("602-347", "602");
    areaCodes.put("602-351", "602");
    areaCodes.put("602-352", "602");
    areaCodes.put("602-353", "602");
    areaCodes.put("602-355", "602");
    areaCodes.put("602-356", "602");
    areaCodes.put("602-359", "602");
    areaCodes.put("602-360", "602");
    areaCodes.put("602-361", "602");
    areaCodes.put("602-363", "602");
    areaCodes.put("602-364", "602");
    areaCodes.put("602-365", "602");
    areaCodes.put("602-369", "602");
    areaCodes.put("602-370", "602");
    areaCodes.put("602-371", "602");
    areaCodes.put("602-372", "602");
    areaCodes.put("602-375", "602");
    areaCodes.put("602-376", "602");
    areaCodes.put("602-377", "602");
    areaCodes.put("602-378", "602");
    areaCodes.put("602-379", "602");
    areaCodes.put("602-381", "602");
    areaCodes.put("602-382", "602");
    areaCodes.put("602-385", "602");
    areaCodes.put("602-389", "602");
    areaCodes.put("602-390", "602");
    areaCodes.put("602-392", "602");
    areaCodes.put("602-395", "602");
    areaCodes.put("602-397", "602");
    areaCodes.put("602-398", "602");
    areaCodes.put("602-399", "602");
    areaCodes.put("602-401", "602");
    areaCodes.put("602-402", "602");
    areaCodes.put("602-404", "602");
    areaCodes.put("602-406", "602");
    areaCodes.put("602-407", "602");
    areaCodes.put("602-408", "602");
    areaCodes.put("602-409", "602");
    areaCodes.put("602-410", "602");
    areaCodes.put("602-414", "602");
    areaCodes.put("602-415", "602");
    areaCodes.put("602-416", "602");
    areaCodes.put("602-417", "602");
    areaCodes.put("602-418", "602");
    areaCodes.put("602-420", "602");
    areaCodes.put("602-422", "602");
    areaCodes.put("602-426", "602");
    areaCodes.put("602-428", "602");
    areaCodes.put("602-430", "602");
    areaCodes.put("602-431", "602");
    areaCodes.put("602-432", "602");
    areaCodes.put("602-433", "602");
    areaCodes.put("602-436", "602");
    areaCodes.put("602-437", "602");
    areaCodes.put("602-438", "602");
    areaCodes.put("602-439", "602");
    areaCodes.put("602-440", "602");
    areaCodes.put("602-442", "602");
    areaCodes.put("602-444", "602");
    areaCodes.put("602-447", "602");
    areaCodes.put("602-448", "602");
    areaCodes.put("602-450", "602");
    areaCodes.put("602-452", "602");
    areaCodes.put("602-453", "602");
    areaCodes.put("602-454", "602");
    areaCodes.put("602-455", "602");
    areaCodes.put("602-462", "602");
    areaCodes.put("602-463", "602");
    areaCodes.put("602-468", "602");
    areaCodes.put("602-469", "602");
    areaCodes.put("602-470", "602");
    areaCodes.put("602-478", "602");
    areaCodes.put("602-479", "602");
    areaCodes.put("602-482", "602");
    areaCodes.put("602-484", "602");
    areaCodes.put("602-485", "602");
    areaCodes.put("602-489", "602");
    areaCodes.put("602-493", "602");
    areaCodes.put("602-494", "602");
    areaCodes.put("602-495", "602");
    areaCodes.put("602-498", "602");
    areaCodes.put("602-499", "602");
    areaCodes.put("602-501", "602");
    areaCodes.put("602-504", "602");
    areaCodes.put("602-506", "602");
    areaCodes.put("602-508", "602");
    areaCodes.put("602-509", "602");
    areaCodes.put("602-510", "602");
    areaCodes.put("602-514", "602");
    areaCodes.put("602-519", "602");
    areaCodes.put("602-521", "602");
    areaCodes.put("602-522", "602");
    areaCodes.put("602-524", "602");
    areaCodes.put("602-525", "602");
    areaCodes.put("602-526", "602");
    areaCodes.put("602-527", "602");
    areaCodes.put("602-528", "602");
    areaCodes.put("602-530", "602");
    areaCodes.put("602-531", "602");
    areaCodes.put("602-532", "602");
    areaCodes.put("602-534", "602");
    areaCodes.put("602-540", "602");
    areaCodes.put("602-541", "602");
    areaCodes.put("602-542", "602");
    areaCodes.put("602-543", "602");
    areaCodes.put("602-547", "602");
    areaCodes.put("602-548", "602");
    areaCodes.put("602-549", "602");
    areaCodes.put("602-550", "602");
    areaCodes.put("602-551", "602");
    areaCodes.put("602-553", "602");
    areaCodes.put("602-558", "602");
    areaCodes.put("602-559", "602");
    areaCodes.put("602-560", "602");
    areaCodes.put("602-562", "602");
    areaCodes.put("602-564", "602");
    areaCodes.put("602-565", "602");
    areaCodes.put("602-568", "602");
    areaCodes.put("602-569", "602");
    areaCodes.put("602-570", "602");
    areaCodes.put("602-571", "602");
    areaCodes.put("602-573", "602");
    areaCodes.put("602-574", "602");
    areaCodes.put("602-576", "602");
    areaCodes.put("602-577", "602");
    areaCodes.put("602-578", "602");
    areaCodes.put("602-579", "602");
    areaCodes.put("602-588", "602");
    areaCodes.put("602-589", "602");
    areaCodes.put("602-590", "602");
    areaCodes.put("602-591", "602");
    areaCodes.put("602-593", "602");
    areaCodes.put("602-594", "602");
    areaCodes.put("602-597", "602");
    areaCodes.put("602-599", "602");
    areaCodes.put("602-601", "602");
    areaCodes.put("602-604", "602");
    areaCodes.put("602-605", "602");
    areaCodes.put("602-608", "602");
    areaCodes.put("602-613", "602");
    areaCodes.put("602-615", "602");
    areaCodes.put("602-616", "602");
    areaCodes.put("602-618", "602");
    areaCodes.put("602-619", "602");
    areaCodes.put("602-620", "602");
    areaCodes.put("602-621", "602");
    areaCodes.put("602-622", "602");
    areaCodes.put("602-625", "602");
    areaCodes.put("602-628", "602");
    areaCodes.put("602-629", "602");
    areaCodes.put("602-630", "602");
    areaCodes.put("602-631", "602");
    areaCodes.put("602-637", "602");
    areaCodes.put("602-639", "602");
    areaCodes.put("602-640", "602");
    areaCodes.put("602-645", "602");
    areaCodes.put("602-646", "602");
    areaCodes.put("602-647", "602");
    areaCodes.put("602-648", "602");
    areaCodes.put("602-650", "602");
    areaCodes.put("602-651", "602");
    areaCodes.put("602-652", "602");
    areaCodes.put("602-653", "602");
    areaCodes.put("602-656", "602");
    areaCodes.put("602-658", "602");
    areaCodes.put("602-660", "602");
    areaCodes.put("602-662", "602");
    areaCodes.put("602-663", "602");
    areaCodes.put("602-664", "602");
    areaCodes.put("602-665", "602");
    areaCodes.put("602-667", "602");
    areaCodes.put("602-669", "602");
    areaCodes.put("602-670", "602");
    areaCodes.put("602-672", "602");
    areaCodes.put("602-673", "602");
    areaCodes.put("602-674", "602");
    areaCodes.put("602-677", "602");
    areaCodes.put("602-678", "602");
    areaCodes.put("602-679", "602");
    areaCodes.put("602-680", "602");
    areaCodes.put("602-681", "602");
    areaCodes.put("602-683", "602");
    areaCodes.put("602-684", "602");
    areaCodes.put("602-685", "602");
    areaCodes.put("602-686", "602");
    areaCodes.put("602-688", "602");
    areaCodes.put("602-689", "602");
    areaCodes.put("602-690", "602");
    areaCodes.put("602-692", "602");
    areaCodes.put("602-694", "602");
    areaCodes.put("602-695", "602");
    areaCodes.put("602-696", "602");
    areaCodes.put("602-697", "602");
    areaCodes.put("602-698", "602");
    areaCodes.put("602-701", "602");
    areaCodes.put("602-702", "602");
    areaCodes.put("602-708", "602");
    areaCodes.put("602-709", "602");
    areaCodes.put("602-710", "602");
    areaCodes.put("602-712", "602");
    areaCodes.put("602-714", "602");
    areaCodes.put("602-716", "602");
    areaCodes.put("602-717", "602");
    areaCodes.put("602-719", "602");
    areaCodes.put("602-720", "602");
    areaCodes.put("602-721", "602");
    areaCodes.put("602-722", "602");
    areaCodes.put("602-723", "602");
    areaCodes.put("602-724", "602");
    areaCodes.put("602-725", "602");
    areaCodes.put("602-728", "602");
    areaCodes.put("602-729", "602");
    areaCodes.put("602-737", "602");
    areaCodes.put("602-738", "602");
    areaCodes.put("602-739", "602");
    areaCodes.put("602-740", "602");
    areaCodes.put("602-741", "602");
    areaCodes.put("602-743", "602");
    areaCodes.put("602-744", "602");
    areaCodes.put("602-745", "602");
    areaCodes.put("602-746", "602");
    areaCodes.put("602-747", "602");
    areaCodes.put("602-749", "602");
    areaCodes.put("602-750", "602");
    areaCodes.put("602-751", "602");
    areaCodes.put("602-757", "602");
    areaCodes.put("602-758", "602");
    areaCodes.put("602-761", "602");
    areaCodes.put("602-762", "602");
    areaCodes.put("602-763", "602");
    areaCodes.put("602-764", "602");
    areaCodes.put("602-765", "602");
    areaCodes.put("602-766", "602");
    areaCodes.put("602-768", "602");
    areaCodes.put("602-769", "602");
    areaCodes.put("602-770", "602");
    areaCodes.put("602-771", "602");
    areaCodes.put("602-779", "602");
    areaCodes.put("602-781", "602");
    areaCodes.put("602-787", "602");
    areaCodes.put("602-788", "602");
    areaCodes.put("602-789", "602");
    areaCodes.put("602-790", "602");
    areaCodes.put("602-791", "602");
    areaCodes.put("602-793", "602");
    areaCodes.put("602-795", "602");
    areaCodes.put("602-797", "602");
    areaCodes.put("602-798", "602");
    areaCodes.put("602-799", "602");
    areaCodes.put("602-801", "602");
    areaCodes.put("602-803", "602");
    areaCodes.put("602-806", "602");
    areaCodes.put("602-808", "602");
    areaCodes.put("602-809", "602");
    areaCodes.put("602-810", "602");
    areaCodes.put("602-817", "602");
    areaCodes.put("602-818", "602");
    areaCodes.put("602-819", "602");
    areaCodes.put("602-822", "602");
    areaCodes.put("602-823", "602");
    areaCodes.put("602-826", "602");
    areaCodes.put("602-828", "602");
    areaCodes.put("602-840", "602");
    areaCodes.put("602-841", "602");
    areaCodes.put("602-843", "602");
    areaCodes.put("602-850", "602");
    areaCodes.put("602-851", "602");
    areaCodes.put("602-852", "602");
    areaCodes.put("602-859", "602");
    areaCodes.put("602-861", "602");
    areaCodes.put("602-862", "602");
    areaCodes.put("602-863", "602");
    areaCodes.put("602-864", "602");
    areaCodes.put("602-865", "602");
    areaCodes.put("602-866", "602");
    areaCodes.put("602-867", "602");
    areaCodes.put("602-868", "602");
    areaCodes.put("602-870", "602");
    areaCodes.put("602-871", "602");
    areaCodes.put("602-881", "602");
    areaCodes.put("602-882", "602");
    areaCodes.put("602-885", "602");
    areaCodes.put("602-889", "602");
    areaCodes.put("602-896", "602");
    areaCodes.put("602-901", "602");
    areaCodes.put("602-903", "602");
    areaCodes.put("602-904", "602");
    areaCodes.put("602-906", "602");
    areaCodes.put("602-908", "602");
    areaCodes.put("602-909", "602");
    areaCodes.put("602-910", "602");
    areaCodes.put("602-912", "602");
    areaCodes.put("602-913", "602");
    areaCodes.put("602-914", "602");
    areaCodes.put("602-916", "602");
    areaCodes.put("602-918", "602");
    areaCodes.put("602-919", "602");
    areaCodes.put("602-920", "602");
    areaCodes.put("602-923", "602");
    areaCodes.put("602-938", "602");
    areaCodes.put("602-942", "602");
    areaCodes.put("602-943", "602");
    areaCodes.put("602-944", "602");
    areaCodes.put("602-952", "602");
    areaCodes.put("602-953", "602");
    areaCodes.put("602-954", "602");
    areaCodes.put("602-955", "602");
    areaCodes.put("602-956", "602");
    areaCodes.put("602-957", "602");
    areaCodes.put("602-971", "602");
    areaCodes.put("602-973", "602");
    areaCodes.put("602-978", "602");
    areaCodes.put("602-980", "602");
    areaCodes.put("602-989", "602");
    areaCodes.put("602-992", "602");
    areaCodes.put("602-993", "602");
    areaCodes.put("602-995", "602");
    areaCodes.put("602-996", "602");
    areaCodes.put("602-997", "602");
    areaCodes.put("602-999", "602");
    areaCodes.put("602-218", "480");
    areaCodes.put("602-283", "480");
    areaCodes.put("602-288", "480");
    areaCodes.put("602-301", "480");
    areaCodes.put("602-303", "480");
    areaCodes.put("602-312", "480");
    areaCodes.put("602-314", "480");
    areaCodes.put("602-317", "480");
    areaCodes.put("602-325", "480");
    areaCodes.put("602-342", "480");
    areaCodes.put("602-345", "480");
    areaCodes.put("602-346", "480");
    areaCodes.put("602-348", "480");
    areaCodes.put("602-350", "480");
    areaCodes.put("602-354", "480");
    areaCodes.put("602-357", "480");
    areaCodes.put("602-367", "480");
    areaCodes.put("602-368", "480");
    areaCodes.put("602-373", "480");
    areaCodes.put("602-380", "480");
    areaCodes.put("602-391", "480");
    areaCodes.put("602-394", "480");
    areaCodes.put("602-396", "480");
    areaCodes.put("602-413", "480");
    areaCodes.put("602-419", "480");
    areaCodes.put("602-421", "480");
    areaCodes.put("602-423", "480");
    areaCodes.put("602-424", "480");
    areaCodes.put("602-425", "480");
    areaCodes.put("602-429", "480");
    areaCodes.put("602-441", "480");
    areaCodes.put("602-443", "480");
    areaCodes.put("602-446", "480");
    areaCodes.put("602-449", "480");
    areaCodes.put("602-451", "480");
    areaCodes.put("602-456", "480");
    areaCodes.put("602-457", "480");
    areaCodes.put("602-460", "480");
    areaCodes.put("602-461", "480");
    areaCodes.put("602-464", "480");
    areaCodes.put("602-471", "480");
    areaCodes.put("602-473", "480");
    areaCodes.put("602-475", "480");
    areaCodes.put("602-481", "480");
    areaCodes.put("602-483", "480");
    areaCodes.put("602-488", "480");
    areaCodes.put("602-491", "480");
    areaCodes.put("602-496", "480");
    areaCodes.put("602-497", "480");
    areaCodes.put("602-502", "480");
    areaCodes.put("602-503", "480");
    areaCodes.put("602-507", "480");
    areaCodes.put("602-515", "480");
    areaCodes.put("602-517", "480");
    areaCodes.put("602-539", "480");
    areaCodes.put("602-545", "480");
    areaCodes.put("602-552", "480");
    areaCodes.put("602-554", "480");
    areaCodes.put("602-557", "480");
    areaCodes.put("602-563", "480");
    areaCodes.put("602-575", "480");
    areaCodes.put("602-585", "480");
    areaCodes.put("602-592", "480");
    areaCodes.put("602-595", "480");
    areaCodes.put("602-596", "480");
    areaCodes.put("602-598", "480");
    areaCodes.put("602-607", "480");
    areaCodes.put("602-609", "480");
    areaCodes.put("602-610", "480");
    areaCodes.put("602-614", "480");
    areaCodes.put("602-632", "480");
    areaCodes.put("602-633", "480");
    areaCodes.put("602-638", "480");
    areaCodes.put("602-641", "480");
    areaCodes.put("602-642", "480");
    areaCodes.put("602-644", "480");
    areaCodes.put("602-649", "480");
    areaCodes.put("602-654", "480");
    areaCodes.put("602-655", "480");
    areaCodes.put("602-657", "480");
    areaCodes.put("602-659", "480");
    areaCodes.put("602-661", "480");
    areaCodes.put("602-668", "480");
    areaCodes.put("602-671", "480");
    areaCodes.put("602-675", "480");
    areaCodes.put("602-682", "480");
    areaCodes.put("602-693", "480");
    areaCodes.put("602-699", "480");
    areaCodes.put("602-704", "480");
    areaCodes.put("602-705", "480");
    areaCodes.put("602-706", "480");
    areaCodes.put("602-715", "480");
    areaCodes.put("602-726", "480");
    areaCodes.put("602-727", "480");
    areaCodes.put("602-730", "480");
    areaCodes.put("602-731", "480");
    areaCodes.put("602-732", "480");
    areaCodes.put("602-733", "480");
    areaCodes.put("602-736", "480");
    areaCodes.put("602-748", "480");
    areaCodes.put("602-752", "480");
    areaCodes.put("602-753", "480");
    areaCodes.put("602-755", "480");
    areaCodes.put("602-756", "480");
    areaCodes.put("602-759", "480");
    areaCodes.put("602-767", "480");
    areaCodes.put("602-774", "480");
    areaCodes.put("602-775", "480");
    areaCodes.put("602-777", "480");
    areaCodes.put("602-782", "480");
    areaCodes.put("602-783", "480");
    areaCodes.put("602-784", "480");
    areaCodes.put("602-785", "480");
    areaCodes.put("602-786", "480");
    areaCodes.put("602-802", "480");
    areaCodes.put("602-804", "480");
    areaCodes.put("602-807", "480");
    areaCodes.put("602-812", "480");
    areaCodes.put("602-813", "480");
    areaCodes.put("602-814", "480");
    areaCodes.put("602-816", "480");
    areaCodes.put("602-820", "480");
    areaCodes.put("602-821", "480");
    areaCodes.put("602-827", "480");
    areaCodes.put("602-829", "480");
    areaCodes.put("602-830", "480");
    areaCodes.put("602-831", "480");
    areaCodes.put("602-832", "480");
    areaCodes.put("602-833", "480");
    areaCodes.put("602-834", "480");
    areaCodes.put("602-835", "480");
    areaCodes.put("602-836", "480");
    areaCodes.put("602-837", "480");
    areaCodes.put("602-838", "480");
    areaCodes.put("602-839", "480");
    areaCodes.put("602-844", "480");
    areaCodes.put("602-854", "480");
    areaCodes.put("602-855", "480");
    areaCodes.put("602-857", "480");
    areaCodes.put("602-858", "480");
    areaCodes.put("602-860", "480");
    areaCodes.put("602-874", "480");
    areaCodes.put("602-883", "480");
    areaCodes.put("602-884", "480");
    areaCodes.put("602-888", "480");
    areaCodes.put("602-890", "480");
    areaCodes.put("602-891", "480");
    areaCodes.put("602-892", "480");
    areaCodes.put("602-893", "480");
    areaCodes.put("602-894", "480");
    areaCodes.put("602-895", "480");
    areaCodes.put("602-897", "480");
    areaCodes.put("602-898", "480");
    areaCodes.put("602-899", "480");
    areaCodes.put("602-902", "480");
    areaCodes.put("602-905", "480");
    areaCodes.put("602-917", "480");
    areaCodes.put("602-921", "480");
    areaCodes.put("602-922", "480");
    areaCodes.put("602-924", "480");
    areaCodes.put("602-926", "480");
    areaCodes.put("602-927", "480");
    areaCodes.put("602-929", "480");
    areaCodes.put("602-940", "480");
    areaCodes.put("602-941", "480");
    areaCodes.put("602-945", "480");
    areaCodes.put("602-946", "480");
    areaCodes.put("602-947", "480");
    areaCodes.put("602-948", "480");
    areaCodes.put("602-949", "480");
    areaCodes.put("602-951", "480");
    areaCodes.put("602-961", "480");
    areaCodes.put("602-962", "480");
    areaCodes.put("602-963", "480");
    areaCodes.put("602-964", "480");
    areaCodes.put("602-965", "480");
    areaCodes.put("602-966", "480");
    areaCodes.put("602-967", "480");
    areaCodes.put("602-968", "480");
    areaCodes.put("602-969", "480");
    areaCodes.put("602-970", "480");
    areaCodes.put("602-981", "480");
    areaCodes.put("602-982", "480");
    areaCodes.put("602-983", "480");
    areaCodes.put("602-984", "480");
    areaCodes.put("602-985", "480");
    areaCodes.put("602-986", "480");
    areaCodes.put("602-987", "480");
    areaCodes.put("602-988", "480");
    areaCodes.put("602-990", "480");
    areaCodes.put("602-991", "480");
    areaCodes.put("602-994", "480");
    areaCodes.put("602-998", "480");
    areaCodes.put("602-201M", "602");
    areaCodes.put("602-202M", "602");
    areaCodes.put("602-203M", "602");
    areaCodes.put("602-204M", "602");
    areaCodes.put("602-205M", "602");
    areaCodes.put("602-206M", "602");
    areaCodes.put("602-208M", "602");
    areaCodes.put("602-209M", "602");
    areaCodes.put("602-210M", "602");
    areaCodes.put("602-213M", "602");
    areaCodes.put("602-215M", "602");
    areaCodes.put("602-219M", "602");
    areaCodes.put("602-226M", "602");
    areaCodes.put("602-227M", "602");
    areaCodes.put("602-228M", "602");
    areaCodes.put("602-259M", "602");
    areaCodes.put("602-270M", "602");
    areaCodes.put("602-289M", "602");
    areaCodes.put("602-290M", "602");
    areaCodes.put("602-291M", "602");
    areaCodes.put("602-292M", "602");
    areaCodes.put("602-295M", "602");
    areaCodes.put("602-309M", "602");
    areaCodes.put("602-310M", "602");
    areaCodes.put("602-315M", "602");
    areaCodes.put("602-316M", "602");
    areaCodes.put("602-319M", "602");
    areaCodes.put("602-320M", "602");
    areaCodes.put("602-321M", "602");
    areaCodes.put("602-329M", "602");
    areaCodes.put("602-330M", "602");
    areaCodes.put("602-332M", "602");
    areaCodes.put("602-339M", "602");
    areaCodes.put("602-341M", "602");
    areaCodes.put("602-356M", "602");
    areaCodes.put("602-359M", "602");
    areaCodes.put("602-360M", "602");
    areaCodes.put("602-361M", "602");
    areaCodes.put("602-369M", "602");
    areaCodes.put("602-370M", "602");
    areaCodes.put("602-376M", "602");
    areaCodes.put("602-377M", "602");
    areaCodes.put("602-390M", "602");
    areaCodes.put("602-397M", "602");
    areaCodes.put("602-398M", "602");
    areaCodes.put("602-399M", "602");
    areaCodes.put("602-401M", "602");
    areaCodes.put("602-402M", "602");
    areaCodes.put("602-408M", "602");
    areaCodes.put("602-409M", "602");
    areaCodes.put("602-410M", "602");
    areaCodes.put("602-417M", "602");
    areaCodes.put("602-418M", "602");
    areaCodes.put("602-430M", "602");
    areaCodes.put("602-432M", "602");
    areaCodes.put("602-448M", "602");
    areaCodes.put("602-450M", "602");
    areaCodes.put("602-463M", "602");
    areaCodes.put("602-469M", "602");
    areaCodes.put("602-478M", "602");
    areaCodes.put("602-489M", "602");
    areaCodes.put("602-498M", "602");
    areaCodes.put("602-499M", "602");
    areaCodes.put("602-501M", "602");
    areaCodes.put("602-509M", "602");
    areaCodes.put("602-510M", "602");
    areaCodes.put("602-519M", "602");
    areaCodes.put("602-521M", "602");
    areaCodes.put("602-524M", "602");
    areaCodes.put("602-525M", "602");
    areaCodes.put("602-526M", "602");
    areaCodes.put("602-527M", "602");
    areaCodes.put("602-531M", "602");
    areaCodes.put("602-540M", "602");
    areaCodes.put("602-541M", "602");
    areaCodes.put("602-549M", "602");
    areaCodes.put("602-550M", "602");
    areaCodes.put("602-558M", "602");
    areaCodes.put("602-559M", "602");
    areaCodes.put("602-560M", "602");
    areaCodes.put("602-565M", "602");
    areaCodes.put("602-568M", "602");
    areaCodes.put("602-570M", "602");
    areaCodes.put("602-571M", "602");
    areaCodes.put("602-573M", "602");
    areaCodes.put("602-574M", "602");
    areaCodes.put("602-576M", "602");
    areaCodes.put("602-577M", "602");
    areaCodes.put("602-578M", "602");
    areaCodes.put("602-579M", "602");
    areaCodes.put("602-590M", "602");
    areaCodes.put("602-591M", "602");
    areaCodes.put("602-593M", "602");
    areaCodes.put("602-599M", "602");
    areaCodes.put("602-601M", "602");
    areaCodes.put("602-608M", "602");
    areaCodes.put("602-613M", "602");
    areaCodes.put("602-615M", "602");
    areaCodes.put("602-616M", "602");
    areaCodes.put("602-618M", "602");
    areaCodes.put("602-619M", "602");
    areaCodes.put("602-620M", "602");
    areaCodes.put("602-621M", "602");
    areaCodes.put("602-622M", "602");
    areaCodes.put("602-625M", "602");
    areaCodes.put("602-637M", "602");
    areaCodes.put("602-639M", "602");
    areaCodes.put("602-645M", "602");
    areaCodes.put("602-646M", "602");
    areaCodes.put("602-647M", "602");
    areaCodes.put("602-652M", "602");
    areaCodes.put("602-653M", "602");
    areaCodes.put("602-656M", "602");
    areaCodes.put("602-658M", "602");
    areaCodes.put("602-660M", "602");
    areaCodes.put("602-662M", "602");
    areaCodes.put("602-669M", "602");
    areaCodes.put("602-670M", "602");
    areaCodes.put("602-672M", "602");
    areaCodes.put("602-673M", "602");
    areaCodes.put("602-677M", "602");
    areaCodes.put("602-679M", "602");
    areaCodes.put("602-680M", "602");
    areaCodes.put("602-684M", "602");
    areaCodes.put("602-686M", "602");
    areaCodes.put("602-688M", "602");
    areaCodes.put("602-689M", "602");
    areaCodes.put("602-690M", "602");
    areaCodes.put("602-692M", "602");
    areaCodes.put("602-694M", "602");
    areaCodes.put("602-695M", "602");
    areaCodes.put("602-696M", "602");
    areaCodes.put("602-697M", "602");
    areaCodes.put("602-698M", "602");
    areaCodes.put("602-701M", "602");
    areaCodes.put("602-702M", "602");
    areaCodes.put("602-708M", "602");
    areaCodes.put("602-709M", "602");
    areaCodes.put("602-710M", "602");
    areaCodes.put("602-714M", "602");
    areaCodes.put("602-717M", "602");
    areaCodes.put("602-719M", "602");
    areaCodes.put("602-720M", "602");
    areaCodes.put("602-721M", "602");
    areaCodes.put("602-722M", "602");
    areaCodes.put("602-723M", "602");
    areaCodes.put("602-725M", "602");
    areaCodes.put("602-729M", "602");
    areaCodes.put("602-737M", "602");
    areaCodes.put("602-738M", "602");
    areaCodes.put("602-739M", "602");
    areaCodes.put("602-740M", "602");
    areaCodes.put("602-741M", "602");
    areaCodes.put("602-743M", "602");
    areaCodes.put("602-746M", "602");
    areaCodes.put("602-750M", "602");
    areaCodes.put("602-751M", "602");
    areaCodes.put("602-757M", "602");
    areaCodes.put("602-758M", "602");
    areaCodes.put("602-761M", "602");
    areaCodes.put("602-762M", "602");
    areaCodes.put("602-763M", "602");
    areaCodes.put("602-764M", "602");
    areaCodes.put("602-768M", "602");
    areaCodes.put("602-769M", "602");
    areaCodes.put("602-770M", "602");
    areaCodes.put("602-771M", "602");
    areaCodes.put("602-779M", "602");
    areaCodes.put("602-781M", "602");
    areaCodes.put("602-790M", "602");
    areaCodes.put("602-791M", "602");
    areaCodes.put("602-793M", "602");
    areaCodes.put("602-797M", "602");
    areaCodes.put("602-799M", "602");
    areaCodes.put("602-803M", "602");
    areaCodes.put("602-806M", "602");
    areaCodes.put("602-809M", "602");
    areaCodes.put("602-810M", "602");
    areaCodes.put("602-818M", "602");
    areaCodes.put("602-819M", "602");
    areaCodes.put("602-823M", "602");
    areaCodes.put("602-826M", "602");
    areaCodes.put("602-828M", "602");
    areaCodes.put("602-851M", "602");
    areaCodes.put("602-859M", "602");
    areaCodes.put("602-868M", "602");
    areaCodes.put("602-871M", "602");
    areaCodes.put("602-881M", "602");
    areaCodes.put("602-882M", "602");
    areaCodes.put("602-885M", "602");
    areaCodes.put("602-901M", "602");
    areaCodes.put("602-903M", "602");
    areaCodes.put("602-904M", "602");
    areaCodes.put("602-908M", "602");
    areaCodes.put("602-909M", "602");
    areaCodes.put("602-910M", "602");
    areaCodes.put("602-913M", "602");
    areaCodes.put("602-918M", "602");
    areaCodes.put("602-919M", "602");
    areaCodes.put("602-920M", "602");
    areaCodes.put("602-980M", "602");
    areaCodes.put("602-989M", "602");
    areaCodes.put("602-999M", "602");
    areaCodes.put("602-214", "623");
    areaCodes.put("602-245", "623");
    areaCodes.put("602-247", "623");
    areaCodes.put("602-322", "623");
    areaCodes.put("602-334", "623");
    areaCodes.put("602-349", "623");
    areaCodes.put("602-362", "623");
    areaCodes.put("602-374", "623");
    areaCodes.put("602-386", "623");
    areaCodes.put("602-388", "623");
    areaCodes.put("602-393", "623");
    areaCodes.put("602-412", "623");
    areaCodes.put("602-434", "623");
    areaCodes.put("602-435", "623");
    areaCodes.put("602-465", "623");
    areaCodes.put("602-486", "623");
    areaCodes.put("602-487", "623");
    areaCodes.put("602-492", "623");
    areaCodes.put("602-516", "623");
    areaCodes.put("602-535", "623");
    areaCodes.put("602-536", "623");
    areaCodes.put("602-546", "623");
    areaCodes.put("602-561", "623");
    areaCodes.put("602-566", "623");
    areaCodes.put("602-572", "623");
    areaCodes.put("602-580", "623");
    areaCodes.put("602-581", "623");
    areaCodes.put("602-582", "623");
    areaCodes.put("602-583", "623");
    areaCodes.put("602-584", "623");
    areaCodes.put("602-587", "623");
    areaCodes.put("602-691", "623");
    areaCodes.put("602-742", "623");
    areaCodes.put("602-772", "623");
    areaCodes.put("602-773", "623");
    areaCodes.put("602-780", "623");
    areaCodes.put("602-792", "623");
    areaCodes.put("602-815", "623");
    areaCodes.put("602-825", "623");
    areaCodes.put("602-842", "623");
    areaCodes.put("602-845", "623");
    areaCodes.put("602-846", "623");
    areaCodes.put("602-847", "623");
    areaCodes.put("602-848", "623");
    areaCodes.put("602-849", "623");
    areaCodes.put("602-853", "623");
    areaCodes.put("602-856", "623");
    areaCodes.put("602-869", "623");
    areaCodes.put("602-872", "623");
    areaCodes.put("602-873", "623");
    areaCodes.put("602-875", "623");
    areaCodes.put("602-876", "623");
    areaCodes.put("602-877", "623");
    areaCodes.put("602-878", "623");
    areaCodes.put("602-879", "623");
    areaCodes.put("602-880", "623");
    areaCodes.put("602-907", "623");
    areaCodes.put("602-915", "623");
    areaCodes.put("602-925", "623");
    areaCodes.put("602-930", "623");
    areaCodes.put("602-931", "623");
    areaCodes.put("602-932", "623");
    areaCodes.put("602-933", "623");
    areaCodes.put("602-934", "623");
    areaCodes.put("602-935", "623");
    areaCodes.put("602-936", "623");
    areaCodes.put("602-937", "623");
    areaCodes.put("602-939", "623");
    areaCodes.put("602-972", "623");
    areaCodes.put("602-974", "623");
    areaCodes.put("602-975", "623");
    areaCodes.put("602-977", "623");
    areaCodes.put("602-979", "623");
  }
  
  /**
     This constructor accepts an area code to use as a default,
     and then calls the no-arg constructor.
    
     @param defaultAreaCode The default area code to use as a prefix
                            for phone numbers that don't have any prefix.
   */
  public USPhoneRule (String defaultAreaCode) {
    this();
    this.defaultAreaCode = defaultAreaCode;
  }
  
  /**
     Returns the entire table full of area codes, prefixes
     and their conversion rules.
    
     @return The table full of area code prefixes and their
             conversion rules.
   */
  public Hashtable getAreaCodes () { return areaCodes; }
  
  /**
     Converts a phone number string into a standard format
     and performs any applicable area code conversion.
    
     @return Standardized phone number.
    
     @param  inData Phone number in any of a number of different formats.
   */
  public String transform (String inData) {
    return transformMobile (inData, false);
  }
  
  /**
     Converts a phone number string into a standard format
     and performs any applicable area code conversion, with 
     consideration for whether phone is mobile or not. This
     method is also performed from the USMobileRule class.
    
     @return Standardized phone number.
    
     @param  inData Phone number in any of a number of different formats.
    
     @param  mobile Is this a mobile phone (cell phone or pager)?
   */
  public String transformMobile (String inData, boolean mobile) {
    String inString = inData.trim();
    String formatted = format (inString);
    StringBuilder work = new StringBuilder (formatted);
    if (work.length() >= STANDARD_LENGTH) {
      char[] prefixChars = new char[7];
      work.getChars (0, 7, prefixChars, 0);
      String prefixString = new String (prefixChars);
      if (mobile) {
        StringBuilder prefixStringBuilder = new StringBuilder (prefixString);
        prefixStringBuilder.append ('M');
        prefixString = prefixStringBuilder.toString();
      }
      String newAreaCode = (String)areaCodes.get (prefixString);
      if (newAreaCode == null) {
      } else {
        for (int j = 0; j < 3; j++) {
          work.setCharAt (j, newAreaCode.charAt(j));
        }
      }
    }
    return work.toString();
  } // end method transformMobile
  
  /**
     Formats a phone number into a standard format, according to the
     following rules. The desired standard output format is  
     "aaa-xxx-nnnn", where "aaa" is the area code, "xxx" is the local
     exchange, and "nnnn" is the rest of the phone number.
    
     @return The formatted phone number.
    
     @param inString The phone number to be formatted.
   */
  public String format (String inString) {
  
    StringBuilder work = new StringBuilder (inString);
    // Use a string pattern to examine the format of the input number. 
    StringPattern pattern = new StringPattern (inString);
    
    // Strip a leading 1 off if it is being used as an area code prefix.
    if ((pattern.getDigitCount() >= 11)
      && (work.charAt (0) == areaCodePrefix)) {
        work = new StringBuilder (inString.substring (1, inString.length()));
        pattern = new StringPattern (inString.substring (1, inString.length()));
    }
    
    // Replace spaces and other punctuation with dashes
    if ((pattern.getPunctCount() > 0) 
      || (pattern.getSpaceCount() > 0)) {
      char lastChar = ' ';
      char currChar = ' ';
      char patternChar;
      boolean letterChar = false;
      int i, j;
      for (i = 0, j = 0; j < work.length(); j++) {
        patternChar = pattern.charAt (j);
        lastChar = currChar;
        currChar = work.charAt (j);
        boolean skipChar = false;
        boolean replaced = false;
        if (patternChar == StringScanner.LETTER_CHAR) {
          letterChar = true;
        }
        else
        if ((i < STANDARD_LENGTH)
          && (! letterChar)
          && (currChar == '(')) {
            skipChar = true;
        }
        else 
        if ((i < STANDARD_LENGTH)
          && (! letterChar)
          && (currChar == ' ')
          && (lastChar == ')')) {
            skipChar = true;
        }
        else
        if ((i < STANDARD_LENGTH)
          && (! letterChar)
          && ((patternChar == StringScanner.PUNCT_CHAR)
            || (patternChar == StringScanner.SPACE_CHAR))) {
          work.setCharAt(i, dashChar);
          replaced = true;
        }
        if ((j > i) && (! replaced)) {
          work.setCharAt (i, currChar);
        }
        if (! skipChar) {
          i++;
        }
      } // end for each pattern character
      if (j > i) {
        work.setLength (i);
        pattern = new StringPattern (work.toString());
      }
    } // end if punctuation to fix
    
    // Add punctuation and area code if missing altogether.
    if (pattern.startsWith ("###.####")) {
      work.insert (0, defaultAreaCode + dashString);
    } else
    if (pattern.startsWith ("##########")) {
      work.insert (6, dashString);
      work.insert (3, dashString);
    } else
    if (pattern.startsWith ("#######")) {
      work.insert (3, dashString);
      work.insert (0, defaultAreaCode + dashString);
    }  
    return work.toString();
  } // end format method
  
  /**
     Return class name as a String.
    
     @return Class name as a String.
   */
  public String toString () {
    return US_PHONE_RULE_CLASS_NAME;
  }
  
  /** 
     Test the class by sending some standard test data
     through and displaying the results. This method is called
     by psdataTest.main.
   */
  public static void test() {
    
    USPhoneRule rule = new USPhoneRule();
    System.out.println ("Testing " + rule.toString());
    testRule (rule, "4519732");
    testRule (rule, "451-9732");
    testRule (rule, "220-48-5752");
    testRule (rule, "6024519732");
    testRule (rule, "602-451-9732");
    testRule (rule, "1602-451-9732");
    testRule (rule, "891-7588");
    testRule (rule, "6027703527");
    testRule (rule, "6027913527");
    testRule (rule, "(602)451-9732");
    testRule (rule, "(602) 451-9732");
    testRule (rule, "602-451-9732 ext. 1234");
    testRule (rule, "1602-451-9732ext. 1234");
    testRule (rule, "(602) 451-9732 ext. 1234");
  }
  
  /**
     Test phone conversion with one input String, displaying input
     and output.
    
     @param inRule Instance of USPhoneRule to use for conversion.
    
     @param inString Phone number to be converted to standard format. 
   */
  public static void testRule (USPhoneRule inRule, String inString) {
  
    System.out.println 
      (inString + " becomes " + (String)inRule.transform(inString));
  }

}
