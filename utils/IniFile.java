/*
 * Copyright (c) 2017 CMCC. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.gnt.utils.gnt.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.netvirt.utils.mdsal.utils.MdsalUtils;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

public class IniFile {

  private static final Logger LOG = LoggerFactory.getLogger(IniFile.class);

  private final DataBroker databroker;
  GntUtils gntutil;

  public  String security_group;
  public  Map<String, String> mirror_input = new HashMap();
  public  Map<String, String> mirror_output = new HashMap();
  public  String gateway;
  	
  public IniFile(DataBroker dataBroker) {
  	     databroker = dataBroker;
		 gntutil = new GntUtils(dataBroker);
  	     init();
  }
  
  private static String removeIniComments(String source){  
    String result = source;  
      
    if(result.contains(";")){  
        result = result.substring(0, result.indexOf(";"));  
    }  
      
    if(result.contains("#")){  
        result = result.substring(0, result.indexOf("#"));  
    }  
      
    return result.trim();  
}  
  
/** 
 * 
 *  
 
 * name0=value0  # 
 * name10=value10 
 *  
 * [normal section] # 
 * name1=value1 # 
 *  
 * [list section] # 
 * value1 
 * value2 
 *  
 * @param fileName 
 * @return Map<sectionName, object> 
 */  
public static Map<String, Object> readIniFile(String fileName){  
    Map<String, List<String>> listResult = new HashMap<>();  
    Map<String, Object> result = new HashMap<>();  
      
    String globalSection = "global"; 
      
    File file = new File(fileName);  
    BufferedReader reader = null;  
    try {  
         reader = new BufferedReader(new FileReader(file));  
          
        //reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(file))));  
  
           String str = null;  
           String currentSection = globalSection; 
           List<String> currentProperties = new ArrayList<>();  
           boolean lineContinued = false;  
           String tempStr = null;  
             

           while ((str = reader.readLine()) != null) {  
            str = removeIniComments(str).trim(); 
              
            if("".equals(str)||str==null){  
                continue;  
            }  
  
            if(lineContinued == true){  
                str = tempStr + str;  
            }  
              
            if(str.endsWith("\\")){  
                lineContinued = true;  
                tempStr = str.substring(0,str.length()-1);  
                continue;  
            }else {  
                lineContinued = false;  
            }  
              
            if(str.startsWith("[") && str.endsWith("]")){  
                String newSection = str.substring(1, str.length()-1).trim();  
  
                if(!currentSection.equals(newSection)){  
                    listResult.put(currentSection, currentProperties);  
                    currentSection = newSection;  
                      
                    currentProperties=listResult.get(currentSection);  
                    if(currentProperties==null){  
                        currentProperties = new ArrayList<>();  
                    }  
                }  
            }else{  
                currentProperties.add(str);  
            }  
           }  
             
           listResult.put(currentSection, currentProperties);  
             
           reader.close();  
       } catch (IOException e) {  
           e.printStackTrace();  
       } finally {  
           if (reader != null) {  
               try {  
                   reader.close();  
               } catch (IOException e1) {  
               }  
           }  
       }  
         

       for(String key : listResult.keySet()){  
        List<String> tempList = listResult.get(key);  
          
        if(tempList==null||tempList.size()==0){  
            continue;  
        }  
          
        if(tempList.get(0).contains("=")){ 
            Map<String, String> properties = new HashMap<>();  
            for(String s : tempList){  
                int delimiterPos = s.indexOf("=");  
                properties.put(s.substring(0,delimiterPos).trim(), s.substring(delimiterPos+1, s.length()).trim());  
            }  
            result.put(key, properties);  
        }else{
            result.put(key, listResult.get(key));  
        }  
       }  
         
    return result;  
  }

  /*get SecurityGroup*/
  public String getSecurityGroup() {
        return security_group;

   }

   public String getGateway() {
        return gateway;

   }


  public Map<String, String> getMirrorInput(){
       return mirror_input; 

  }	

  

  //read ini file and write datastore....
  public void init() {

		Map<String, Object> ini = readIniFile("/root/topo.ini");		
	    Iterator<String> it = ((List<String>)ini.get("switch")).iterator();
		String dpid = "";
		String minvni = ((Map<String, String>)ini.get("l3_vni")).get("begin");
		String maxvni = ((Map<String, String>)ini.get("l3_vni")).get("end");
		security_group = ((Map<String, String>)ini.get("securitygroup")).get("enable");
		gateway = ((Map<String, String>)ini.get("gateway")).get("enable");
		LOG.info("-----initsecurity_group-----is {}--gateway is {}--------",security_group,gateway);
		gntutil.addSys(minvni,maxvni);
		while (it.hasNext()) {
		   dpid = it.next();
		   LOG.info("init dpid is {}\n",dpid);
		   String tunnelip = ((Map<String, String>)ini.get("switch_" + dpid)).get("tunnel_ip");
		   String type = ((Map<String, String>)ini.get("switch_" + dpid)).get("type");
		   String switch_type = ((Map<String, String>)ini.get("switch_" + dpid)).get("switch_type");
		   LOG.info("tunnel ip is {},type is {},switch_type is {}\n",tunnelip,type,switch_type);
		   Map<String, String> tunnel_map = (Map<String, String>)ini.get("tunnel_" + dpid);
		   
		   LOG.info("tunnel map is {},minvni is {},maxvni is {}\n",tunnel_map,minvni,maxvni);
		   gntutil.addNode(dpid,type,tunnelip,switch_type,tunnel_map);
		   
		 }

		 //add mirror....
		 mirror_input.putAll(((Map<String, String>)ini.get("mirror_input")));
		 //mirror_output.putAll(((Map<String, String>)ini.get("mirror_output")));


    }

  
}
  

