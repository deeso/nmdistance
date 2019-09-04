package nmdistance;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.JsonGenerationException;
//import com.fasterxml.jackson.map.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class DomainInfo {

	public static String ERROR_INVALID_SUFFIX = "Invalid Suffix";
	public static String ERROR_UNKNOWN_ERROR = "Unknown Error";
	
	private String rtld = new String();
	private boolean rtld_set = false;
	
	private String hostname = new String();
	private boolean hostname_set = false;
	
	private String suffix = new String();
	private boolean suffix_set = false;
	
	private String error_msg = new String();
	private boolean error = true;

	double rtld_fqdn_dilution = 0.0;
	double hostname_fqdn_dilution = 0.0;
	double hostname_rtld_dilution = 0.0;

	HashMap<String, ArrayList<SimilarityResult>> matches_fqdn;
	HashMap<String, ArrayList<SimilarityResult>> matches_rtld;
	HashMap<String, ArrayList<SimilarityResult>> matches_hostname;
	
	HashMap<String, ArrayList<SimilarityResult>> ignores_fqdn;
	HashMap<String, ArrayList<SimilarityResult>> ignores_rtld;
	HashMap<String, ArrayList<SimilarityResult>> ignores_hostname;
	
	public DomainInfo() {
		rtld = new String();
		hostname = new String();
		suffix = new String();
		error_msg = new String();
		error = false;
		
		matches_fqdn = new HashMap<String, ArrayList<SimilarityResult>>();
		matches_rtld = new HashMap<String, ArrayList<SimilarityResult>>();
		matches_hostname = new HashMap<String, ArrayList<SimilarityResult>>();
		
		ignores_fqdn = new HashMap<String, ArrayList<SimilarityResult>>();
		ignores_rtld = new HashMap<String, ArrayList<SimilarityResult>>();
		ignores_hostname = new HashMap<String, ArrayList<SimilarityResult>>();
		
		
	}
	
	public void setError() {
		error = true;
	}
	
	public void setError(String msg) {
		error_msg = msg;
		setError();
	}
	
	public void setHostname(String hostname) {
		hostname_set = true;
		this.hostname = hostname;
	}
	
	public void setRtld(String rtld) {
		rtld_set = true;
		this.rtld = rtld;
	}
	
	public void setSuffix(String suffix) {
		suffix_set = true;
		this.suffix = suffix;
	}

	public boolean isRtldSet() {
		return rtld_set;
	}

	public boolean isHostnameSet() {
		return hostname_set;
	}

	public boolean isSuffixSet() {
		return suffix_set;
	}

	public String getErrorMsg() {
		return error_msg;
	}

	public boolean isError() {
		return error;
	}

	public String getRtld() {
		return rtld;
	}

	public String getHostname() {
		return hostname;
	}

	public String getSuffix() {
		return suffix;
	}
	
	public String toJsonString(boolean pprint) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		if (pprint)
			mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writeValueAsString(this);	
	}
	public String toJson() throws JsonProcessingException {
		return toJsonString(false);
	}
	
	public double getRtldFqdnDilution() {
		String fqdn = getFqdn();
		String rtld = getRtld();
		if (rtld.length() > 0 && fqdn.length() > 0)
			this.rtld_fqdn_dilution = 1 - ((double)rtld.length() / (double)fqdn.length());
		return rtld_fqdn_dilution;
	}
	
	public double getHostnameRtldDilution() {
		if (rtld.length() > 0 && hostname.length() > 0)
			this.hostname_rtld_dilution = 1 - ((double)hostname.length() / (double)rtld.length());
		return hostname_rtld_dilution;
	}
	
	public double getHostnameFqdnDilution() {
		String fqdn = getFqdn();
		if (fqdn.length() > 0 && hostname.length() > 0)
			this.hostname_fqdn_dilution = 1 - ((double)hostname.length() / (double)fqdn.length());
		return hostname_fqdn_dilution;
	}
	
	public String getFqdn() {
		if (isHostnameSet() && isRtldSet())
			return hostname + "." + rtld;
		return new String();
	}
	
	public boolean compareString(String similar) {
		double threshold = Similarity.DEFAULT_FUZZY_CONTAINS_THRESHOLD;
		return compareString(similar, threshold);
	}
	
	public boolean compareString(String similar, double threshold) {
		String algorithm = Similarity.ALL;
		return compareString(similar, threshold, algorithm);
	}
	
	public boolean compareString(String similar, double threshold, String algorithm) {
		ArrayList<SimilarityResult> results = Similarity.compareOnlyMatches(algorithm, similar, getFqdn(), threshold); 
		matches_fqdn.put(similar, results);
		results = Similarity.compareOnlyMatches(algorithm, similar, getRtld(), threshold);
		matches_rtld.put(similar, results);
		results = Similarity.compareOnlyMatches(algorithm, similar, getHostname(), threshold);
		matches_hostname.put(similar, results);
		return hasMatchingResults();
	}
	
	public boolean measureSimilarity(String similar, double threshold, String algorithm) {
		ArrayList<SimilarityResult> results = Similarity.measureSimilarity(algorithm, similar, getFqdn(), threshold); 
		matches_fqdn.put(similar, results);
		results = Similarity.measureSimilarity(algorithm, similar, getRtld(), threshold);
		matches_rtld.put(similar, results);
		results = Similarity.measureSimilarity(algorithm, similar, getHostname(), threshold);
		matches_hostname.put(similar, results);
		return hasMatchingResults();
	}
	

	public boolean compareIgnoreString(String similar) {
		double threshold = Similarity.DEFAULT_FUZZY_CONTAINS_THRESHOLD;
		return compareIgnoreString(similar, threshold);
	}
	
	public boolean compareIgnoreString(String similar, double threshold) {
		String algorithm = Similarity.ALL;
		return compareIgnoreString(similar, threshold, algorithm);
	}
	
	public boolean compareIgnoreString(String similar, double threshold, String algorithm) {
		ArrayList<SimilarityResult> results = Similarity.compareOnlyMatches(algorithm, similar, getFqdn(), threshold); 
		ignores_fqdn.put(similar, results);
		results = Similarity.compareOnlyMatches(algorithm, similar, getRtld(), threshold);
		ignores_rtld.put(similar, results);
		results = Similarity.compareOnlyMatches(algorithm, similar, getHostname(), threshold);
		ignores_hostname.put(similar, results);
		return hasIgnoreResults();
	}
	
	public boolean hasIgnoreResults() {
		return ignores_fqdn.size() > 0 || ignores_rtld.size() > 0 || ignores_hostname.size() > 0;
	}
	
	public boolean hasMatchingResults() {
		return matches_fqdn.size() > 0 || matches_rtld.size() > 0 || matches_hostname.size() > 0;
	}
	
	public boolean shouldProcessResults() {
		return (!hasIgnoreResults() && hasMatchingResults());
	}
	
	public ArrayList<SimilarityResult> getMatchingResults() {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		if (hasIgnoreResults())
			return results;
		for (ArrayList<SimilarityResult> srs: matches_fqdn.values()) {
			results.addAll(srs);
		}
		for (ArrayList<SimilarityResult> srs: matches_rtld.values()) {
			results.addAll(srs);
		}
		for (ArrayList<SimilarityResult> srs: matches_hostname.values()) {
			results.addAll(srs);
		}
		return results;
	}
	
	public ArrayList<SimilarityResult> getIgnoreResults() {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		if (hasIgnoreResults())
			return results;
		for (ArrayList<SimilarityResult> srs: ignores_fqdn.values()) {
			results.addAll(srs);
		}
		for (ArrayList<SimilarityResult> srs: ignores_rtld.values()) {
			results.addAll(srs);
		}
		for (ArrayList<SimilarityResult> srs: ignores_hostname.values()) {
			results.addAll(srs);
		}
		return results;
	}
	
	public ArrayList<String> getMatchingSimpleStrings() {
		ArrayList<String> results = new ArrayList<String>();
		if (hasIgnoreResults())
			return results;
		for (ArrayList<SimilarityResult> srs: matches_fqdn.values()) {
			for (SimilarityResult sr: srs) {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("%s %s %s %s %s %.03f %.03f", "fqdn", getFqdn(), getFqdn(), sr.left, 
						sr.algorithm, sr.similarity, sr.dilution));
				results.add(sb.toString());
			}
		}
		for (ArrayList<SimilarityResult> srs: matches_rtld.values()) {
			for (SimilarityResult sr: srs) {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("%s %s %s %s %s %.03f %.03f", "rtld", getRtld(), getFqdn(), sr.left, 
						sr.algorithm, sr.similarity, sr.dilution));
				results.add(sb.toString());
			}
		}
		for (ArrayList<SimilarityResult> srs: matches_hostname.values()) {
			for (SimilarityResult sr: srs) {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("%s %s %s %s %s %.03f %.03f", "hostname", getHostname(), getFqdn(), sr.left, 
						sr.algorithm, sr.similarity, sr.dilution));
				results.add(sb.toString());
			}
		}
		return results;
	}
}
