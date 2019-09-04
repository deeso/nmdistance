package nmdistance;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import info.debatty.java.stringsimilarity.Cosine;
import net.dongliu.requests.Requests;
import net.dongliu.requests.Response;

public class DomainProcessor {
	public static String SUFFIX_URL = "https://publicsuffix.org/list/public_suffix_list.dat";
	public static HashSet<String> SUFFIXES = new HashSet<String>();
	public static boolean SUFFIX_INIT = false;
    
	public static int numSuffixes() {
        return SUFFIXES.size();
    }
    
    public static boolean isSuffixesInit() {
    	return SUFFIXES.size() > 0;
    }
    
    public static boolean perfromSuffixInit() throws Exception{
    	readSuffixUrl();
    	return isSuffixesInit();
    }
    
    private static void readSuffixUrl(){
    	Response<String> resp = Requests.get(SUFFIX_URL).send().toTextResponse();
    	String data = resp.body();
    	if (data != null) {
    		String[] lines = data.split("\\r?\\n");
    		for (String line: lines) {
    			if (line.length() < 2) 
    				continue;
    			if (line.charAt(0) == '/' && line.charAt(1) == '/')
    				continue;
    			SUFFIXES.add(line.trim());
    		}
    	}
    }
    
    public static boolean compare_string(String name, String target) {
    	Cosine instance = new Cosine();
    	double result = instance.similarity(name, target);
    	return result > .6;
//    	return true;
    }
    
    public static DomainInfo processFqdn(String fqdn) {
    	DomainInfo dr = new DomainInfo();
    	if (!isSuffixesInit()) {
    		try {
				perfromSuffixInit();
			} catch (Exception e) {
				dr.setError("Unable to init suffixes");
			}
    	}
    	
    	if (fqdn == null || fqdn.length() == 0 || !fqdn.contains(".")) {
    		dr.setError("Invalid FQDN");			
    		return dr;
    	}
    		
    	String [] tokens = fqdn.split("\\.");
    	Collections.reverse(Arrays.asList(tokens));
    	
    	boolean done = false;
    	if (tokens.length < 2) {
    		dr.setError(DomainInfo.ERROR_INVALID_SUFFIX);
    		return dr;
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	String cSuffix = new String();
    	int pos = 0;
    	for (; pos < tokens.length; pos++) {
    		String temp = tokens[pos] + "." + cSuffix;
    		if (cSuffix.length() == 0 ) 
    			temp = tokens[pos];
    		
    		
    		if (hasSuffix(temp)) {
    			cSuffix = temp;
    			pos += 1;
    		} 
    		break;
    	}
    	
    	if (cSuffix.length() > 0) {
    		dr.setSuffix(cSuffix);
    	} else if (cSuffix.isEmpty()) {
    		dr.setError(dr.ERROR_INVALID_SUFFIX);
    		return dr;
    	}
    	
    	// no hostname to process
    	// no rtld to process
    	if (pos == tokens.length) {
    		return dr;
    	}
    	String hostname = tokens[tokens.length-1];
    	String rtld = cSuffix;
    	for (; pos < tokens.length-1; pos++) {
    		rtld = tokens[pos] + "." + rtld;
    	}
    	
    	dr.setHostname(hostname);
    	dr.setRtld(rtld);
    	
    	return dr;
    }
    public static boolean hasSuffix(String suffix) {
    	return SUFFIXES.contains(suffix.trim());
    }
    
}
