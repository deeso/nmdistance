package nmdistance;

public class SimilarityResult {
		public String algorithm;
		public String left, right;
		public double similarity;
		public int ngram = 0;
		public boolean contains = false;
		public double dilution;
		
		private boolean error;
		private String error_msg;
		
		
		public SimilarityResult(String algorithm, String left, String right, double similarity, int ngram) {
			this.algorithm = algorithm;
			this.left = left;
			this.right = right;
			this.similarity = similarity;
			this.ngram = ngram;
			
			dilution = 1 - ((double)left.length() / (double)right.length());
		}
		public SimilarityResult(String algorithm, String left, String right, double similarity) {
			this.algorithm = algorithm;
			this.left = left;
			this.right = right;
			this.similarity = similarity;
			this.ngram = 0;
			dilution = 1 - ((double)left.length() / (double)right.length());			
		}
		
		public SimilarityResult(boolean error, String error_msg) {
			this.error = error;
			this.error_msg = error_msg;
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("[%s] ", algorithm)); 
			sb.append(String.format(" [L:\"%s\"] ==> [R:\"%s\"]", left, right));
			sb.append(String.format(" = %.3f", similarity));
			sb.append(String.format(" fuzzy_match =  %s", contains));
			return sb.toString();
		}
		
		public String toSimpleString() {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%s %s %s %.3f", algorithm, left, right, similarity));
			return sb.toString();
		}
		
		public boolean left_contains_right() {
			return this.left_contains_right(Similarity.DEFAULT_FUZZY_CONTAINS_THRESHOLD);
		}
		
		public boolean left_contains_right(double threshold) {
			contains = Similarity.fuzzy_contains(this.algorithm, this.left, this.right, threshold);
			return this.contains;
		}
		
		public void set_left_contains_right(boolean contains) {
			this.contains = contains;
		}
		
		public boolean hasError() {
			return this.error;
		}
		public String getError() {
			return this.error_msg;
		}
		public boolean isValid() {
			return this.left != null && this.right != null && this.algorithm != null;
		}
}
