package search.analyzers;

import datastructures.concrete.ChainedHashSet;
import datastructures.concrete.KVPair;
import datastructures.concrete.dictionaries.ChainedHashDictionary;

import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;
import datastructures.interfaces.ISet;
import search.models.Webpage;

import java.net.URI;

/**
 * This class is responsible for computing how "relevant" any given document is
 * to a given search query.
 *
 */
public class TfIdfAnalyzer {
    // This field must contain the IDF score for every single word in all
    // the documents.
    private IDictionary<String, Double> idfScores;

    // This field must contain the TF-IDF vector for each webpage you were given
    // in the constructor.
    //
    // We will use each webpage's page URI as a unique key.
    private IDictionary<URI, IDictionary<String, Double>> documentTfIdfVectors;
    private IDictionary<String, Double> queryVector;


    public TfIdfAnalyzer(ISet<Webpage> webpages) {
        this.idfScores = this.computeIdfScores(webpages);
        this.documentTfIdfVectors = this.computeAllDocumentTfIdfVectors(webpages);
        this.queryVector = new ChainedHashDictionary<String, Double>();
    }

    public IDictionary<URI, IDictionary<String, Double>> getDocumentTfIdfVectors() {
        return this.documentTfIdfVectors;
    }


    /**
     * @author Catherine Yoo
     * 
     * Returns a dictionary mapping every single unique word found
     * in every single document to their IDF score.
     * 
     * @param pages - A set of pages representing documents 
     * 
     */
    private IDictionary<String, Double> computeIdfScores(ISet<Webpage> pages) {
        IDictionary<String, Double> result = new ChainedHashDictionary<String, Double>();
        
        for (Webpage page : pages) {
            ISet<String> words = new ChainedHashSet<String>();
            for (String word : page.getWords()) {
                words.add(word);
            }
            
            for (String word : words) {
                //if (result.getOrDefault(word, null) == null) {
                if (!result.containsKey(word)) { 
                    result.put(word, 1.0);
                } else {
                    result.put(word, result.get(word) + 1.0);
                }
            }
        }
        
        for (KVPair<String, Double> pair : result) {
            double idf = Math.log(pages.size() / pair.getValue());
            result.put(pair.getKey(), idf);
        }
        return result;
    }


    /**
     * @author Catherine Yoo
     * 
     * Returns a dictionary mapping every unique word found in the given list
     * to their term frequency (TF) score.
     * 
     * @param words - A list of all the words in a single document. 
     *
     */ 
    private IDictionary<String, Double> computeTfScores(IList<String> words) {
        IDictionary<String, Double> result = new ChainedHashDictionary<String, Double>();
        
        for (String word : words) {
            //if (result.getOrDefault(word, null) != null) {
            if (result.containsKey(word)) {
                result.put(word, result.get(word) + 1.0);
            } else {
                result.put(word, 1.0);
            }
        }
        
        for (KVPair<String, Double> pair : result) {
            double tf = pair.getValue() / words.size();
            result.put(pair.getKey(), tf);
        }

        return result; 
    }

    /**
     * @author Catherine Yoo
     * 
     * Returns a dictionary mapping each document to its TF-IDF vectors. 
     * 
     * @param pages - A set of pages representing documents 
     * 
     * */
    private IDictionary<URI, IDictionary<String, Double>> computeAllDocumentTfIdfVectors(ISet<Webpage> pages) {
        IDictionary<URI, IDictionary<String, Double>> result = 
                new ChainedHashDictionary<URI, IDictionary<String, Double>>();
        for (Webpage page : pages) {
            IDictionary<String, Double> tfScores = computeTfScores(page.getWords());
            IDictionary<String, Double> vector = new ChainedHashDictionary<String, Double>();
            for (KVPair<String, Double> pair : tfScores) {
                double idfScore = this.idfScores.get(pair.getKey());
                vector.put(pair.getKey(), idfScore * pair.getValue());
            }
            //if (result.getOrDefault(page.getUri(), null) == null) {
            if (!result.containsKey(page.getUri())) {
                result.put(page.getUri(), vector);
            }
        }
        
        return result; 
    }

    /**
     * @author Catherine Yoo
     * 
     * Returns the cosine similarity between the TF-IDF vector for the given query and the
     * URI's document.
     *
     * Precondition: the given uri must have been one of the uris within the list of
     *               webpages given to the constructor.
     */
    
    public double computeRelevance(IList<String> query, URI pageUri) {
        IDictionary<String, Double> documentVector = this.documentTfIdfVectors.get(pageUri);
        IDictionary<String, Double> wordTfScores = computeTfScores(query);
        
        for (KVPair<String, Double> pair : wordTfScores) {
            String word = pair.getKey();
            double wordTf = pair.getValue();
            //if (idfScores.getOrDefault(word, null) != null) {
            if (idfScores.containsKey(word)) {
                double wordIdf = idfScores.get(word);
                double wordTfIdf = wordTf * wordIdf;
                queryVector.put(word, wordTfIdf);
            } else {
                queryVector.put(word, 0.0);
            }
        }
       
        double denominator = norm(documentVector) * norm(queryVector);
        if (denominator != 0) {
            double numerator = 0.0;
            for (String word : query) {
                double docWordScore = 0.0;
                //if (documentVector.getOrDefault(word, null) != null) {
                if (documentVector.containsKey(word)) {
                    docWordScore = documentVector.get(word);
                } 
                double queryWordScore = queryVector.get(word);
                numerator += docWordScore * queryWordScore;
            }
            return numerator / denominator;
        } else {
            return 0.0;
        }
    }

    private double norm(IDictionary<String, Double> vector) {
        double output = 0.0;
        for (KVPair<String, Double> pair : vector) {
            double score = pair.getValue();
            output += score * score;
        }
        return Math.sqrt(output);
    }
}
