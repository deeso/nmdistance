package nmdistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import info.debatty.java.stringsimilarity.*;


public class Similarity {
	public static final String NGRAM_SHINGLES = "ngram";
	public static final String JARO_WINKLER = "jaro_winkler";
	public static final String JACARD = "jacard";
	public static final String COSINE = "cosine";
	public static final String SORENSEN_DICE = "sorensen_dice";

	
	public static final String [] STRIP_CHARS = {".", "-"};
	public static final String [][] REPLACE_LEET = { {"1", "i"}, {"4", "a"}, {"7", "l"}};
	
	public static final double DEFAULT_FUZZY_CONTAINS_THRESHOLD = .86;
	public static final String ALL = "all";
	public static final String[] ALGORITHMS = {
			JARO_WINKLER,
			JACARD, COSINE, SORENSEN_DICE,
	};
	public static final String[] VALID_CHOICES = {
			NGRAM_SHINGLES, JARO_WINKLER,
			JACARD, COSINE, SORENSEN_DICE,
			ALL
	};
	
	static public String preprocess(String input) {
		String output = input;
		
		for (String x: STRIP_CHARS) {
			output = output.replace(x, "");
		}
		for (String []n_a: REPLACE_LEET) {
			output = output.replace(n_a[0], n_a[1]);
		}
		return output;
	}

	static private SimilarityResult execute(String name, String left, String right) {
		
		if (name.equals(JARO_WINKLER)) {
		    return jaro_winkler(left, right); 
		}
		if (name.equals(JACARD)) {
		    return jacard(left, right); 
		}
		if (name.equals(COSINE)) {
		    return cosine(left, right); 
		}
		if (name.equals(SORENSEN_DICE)) {
		    return sorensen_dice(left, right); 
		}		
		return new SimilarityResult(true, "Unknown Algorithm");
	}
	
	static ArrayList<SimilarityResult> executeByName(String name, String left, String right) {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		if (name.equals(ALL))
			results = executeAll(left, right);
		else
			results.add(execute(name, left, right));
		for (SimilarityResult sr: results) {
			sr.left_contains_right(); 	
		}
		return results;
	}
	
	static ArrayList<SimilarityResult> executeByNameNoUpdate(String name, String left, String right) {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		if (name.equals(ALL))
			return executeAll(left, right);
		results.add(execute(name, left, right));
		return results;
	}
	
	static ArrayList<SimilarityResult> executeAll(String left, String right) {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		for (String name: ALGORITHMS) {
			results.add(execute(name, left, right));
		}
		return results;
	}
	
	static ArrayList<SimilarityResult> executeByName(String name, ArrayList<String> lefts, ArrayList<String> rights) {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		for (String left: lefts) {
			for (String right: rights) {
				results.addAll(executeByName(name, left, right));
			}
		}
		return results;
	}
	
	static ArrayList<SimilarityResult> executeByName(String name, String left, ArrayList<String> rights) {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		for (String right: rights) {
			results.addAll(executeByName(name, left, right));
		}
		return results;
	}
	
	static ArrayList<SimilarityResult> executeByNameNoUpdate(String name, String left, ArrayList<String> rights) {
		ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
		for (String right: rights) {
			results.addAll(executeByNameNoUpdate(name, left, right));
		}
		return results;
	}
	
    private static SimilarityResult jaro_winkler(String left, String right){
        return new SimilarityResult(JARO_WINKLER, left, right, new JaroWinkler().similarity(left, preprocess(right)));
    }
    private static SimilarityResult jacard(String left, String right){
        return jacard(left, right, 2);
    }
    private static SimilarityResult jacard(String left, String right, Integer ngram_sequence){
        return new SimilarityResult(JACARD, left, right, new Jaccard(ngram_sequence).similarity(left, preprocess(right)), ngram_sequence);
    }
    private static SimilarityResult cosine(String left, String right){
        return cosine(left, right, 2);
    }
    private static SimilarityResult cosine(String left, String right, Integer ngram_sequence){
        return new SimilarityResult(COSINE, left, right, new Cosine(ngram_sequence).similarity(left, preprocess(right)), ngram_sequence);
    }
    private static SimilarityResult sorensen_dice(String left, String right){
        return sorensen_dice(left, right, 2);
    }   
    private static SimilarityResult sorensen_dice(String left, String right, Integer ngram_sequence){
        return new SimilarityResult(SORENSEN_DICE, left, right, new SorensenDice(ngram_sequence).similarity(left, preprocess(right)), ngram_sequence);
    }
    
    public static ArrayList<SimilarityResult> windowed_similarity(String name, String left, String right){
    	int max_len = left.length();
    	
    	ArrayList<String> ss_rights = new ArrayList<String>();
    	if (right.length() < max_len) {
    		return new ArrayList<SimilarityResult>();
    	}
    	
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (int pos = 0; pos < right.length() && right.length() - pos >= max_len; pos++) {
    		int end = pos + max_len <= right.length() ? pos + max_len  : right.length();
    		String ss = right.substring(pos, end);
    		ss_rights.add(ss);
    	}
    	
        return executeByNameNoUpdate(name, left, ss_rights);
    }
    
    public static ArrayList<SimilarityResult> windowed_similarity(String name, String left, ArrayList<String> rights){
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (String right: rights) {
    		results.addAll(windowed_similarity(name, left, preprocess(right)));
    	}
    	return results;
    }
    
    public static ArrayList<SimilarityResult> windowed_similarity(String name, String left, ArrayList<String> rights, double threshold){
    	ArrayList<SimilarityResult> v_results = new ArrayList<SimilarityResult>();
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (String right: rights) {
    		v_results.addAll(windowed_similarity(name, left, preprocess(right)));
    	}
    	
    	for (SimilarityResult sr: v_results) {
    		if (sr.similarity >= threshold)
    			results.add(sr);
    	}
    	
    	return results;
    }
    
    public static ArrayList<SimilarityResult> windowed_similarity(String name, ArrayList<String> lefts, ArrayList<String> rights){
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (String left: lefts) {
    		results.addAll(windowed_similarity(name, left, rights));
    	}
    	
    	return results;
    }

    public static ArrayList<SimilarityResult> windowed_similarity(String name, ArrayList<String> lefts, ArrayList<String> rights, double threshold){
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (String left: lefts) {
    		results.addAll(windowed_similarity(name, left, rights, threshold));
    	}
    	
    	return results;
    }
    
    public static HashMap<String, Boolean> fuzzy_contains(String name, String left, ArrayList<String> rights, double threshold){
		HashMap<String, Boolean> left_results = new HashMap<String, Boolean>();
		for (String right: rights) {
			left_results.put(right, fuzzy_contains(name, left,  preprocess(right), threshold));
		}
		return left_results;
    }
    
    public static boolean fuzzy_contains(String name, String left, String right, double threshold){
		boolean result = false;
		ArrayList<SimilarityResult> tmp_results = windowed_similarity(name, left, preprocess(right));
		for (SimilarityResult sr: tmp_results) {
			if (sr.similarity >= threshold) {
				result = true;
				break;
			}
		}
		return result;
    }
    
    public static boolean fuzzy_contains(String name, String left, String right){
		return fuzzy_contains(name, left, right, DEFAULT_FUZZY_CONTAINS_THRESHOLD);
    }
    
    public static HashMap<String, Boolean> fuzzy_contains(String name, String left, ArrayList<String> rights){
		return fuzzy_contains(name, left, rights, DEFAULT_FUZZY_CONTAINS_THRESHOLD);
    }
    public static HashMap<String, HashMap<String, Boolean>> fuzzy_contains(String name, ArrayList<String> lefts, ArrayList<String> rights){
    	return fuzzy_contains(name, lefts, rights, DEFAULT_FUZZY_CONTAINS_THRESHOLD);
    }
    public static HashMap<String, HashMap<String, Boolean>> fuzzy_contains(String name, ArrayList<String> lefts, ArrayList<String> rights, double threshold){
    	HashMap<String, HashMap<String, Boolean>> fuzzy_matches = new HashMap<String, HashMap<String, Boolean>>();
    	for (String left: lefts) {
    		fuzzy_matches.put(left, fuzzy_contains(name, left, rights, threshold));
    	}
    	return fuzzy_matches;
    }
    
    
    public static ArrayList<SimilarityResult> compareOnlyMatches(String name, String left, String right, double threshold){
    	HashMap<String, HashMap<String, Boolean>> matches = new HashMap<String, HashMap<String,Boolean>>();
    	HashMap<String, Boolean> left_results = new HashMap<String, Boolean>();
    	matches.put(left, left_results);
    	left_results.put(right, fuzzy_contains(name, left, right, threshold));
    	return similarityForMatchesOnly(name, matches, threshold);
    }
    public static ArrayList<SimilarityResult> compareOnlyMatches(String name, String left, ArrayList<String> rights, double threshold){
    	HashMap<String, HashMap<String, Boolean>> matches = new HashMap<String, HashMap<String,Boolean>>();
    	matches.put(left, fuzzy_contains(name, left, rights, threshold));
    	return similarityForMatchesOnly(name, matches, threshold);
    }
    public static ArrayList<SimilarityResult> compareOnlyMatches(String name, ArrayList<String> lefts, ArrayList<String> rights, double threshold){
    	HashMap<String, HashMap<String, Boolean>> matches = fuzzy_contains(name, lefts, rights, threshold);
    	return similarityForMatchesOnly(name, matches, threshold);
    }
    
    public static ArrayList<SimilarityResult> similarityForMatchesOnly(String name, HashMap<String, HashMap<String, Boolean>> matches, double threshold) {
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (Entry<String, HashMap<String, Boolean>> e: matches.entrySet()) {
    		String left = e.getKey();
    		for (Entry<String, Boolean> f: e.getValue().entrySet()) {
    			String right = f.getKey();
    			
    			if (f.getValue())
    				results.addAll(executeByName(name, left, right));
    		}
    	}
    	
    	if (threshold > 0.0) {
    		ArrayList<SimilarityResult> nresults = new ArrayList<SimilarityResult>();
    		for (SimilarityResult sr: results) {
    			if (sr.similarity >= threshold) {
    				nresults.add(sr);
    			}
    		}
    		results = nresults;
    	}
    	return results;
    }
    public static ArrayList<SimilarityResult> measureSimilarity(String name, String left, String right, double threshold){
    	ArrayList<SimilarityResult> tmp_results = executeByName(name, left, right);
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (SimilarityResult sr: tmp_results) {
    		if (sr.similarity >= threshold)
    			results.add(sr);
    	}
    	return results;
    }
    public static ArrayList<SimilarityResult> measureSimilarity(String name, String left, ArrayList<String> rights, double threshold){
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (String right: rights) {
    		results.addAll(measureSimilarity(name, left, right, threshold));
    	}
    	return results;
    }
    public static ArrayList<SimilarityResult> measureSimilarity(String name, ArrayList<String> lefts, ArrayList<String> rights, double threshold){
    	ArrayList<SimilarityResult> results = new ArrayList<SimilarityResult>();
    	for (String left: lefts) {
    		results.addAll(measureSimilarity(name, left, rights, threshold));
    	}
    	return results;
    }
}

        