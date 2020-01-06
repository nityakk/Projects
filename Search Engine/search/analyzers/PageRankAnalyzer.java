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
 * This class is responsible for computing the 'page rank' of all available webpages.
 * If a webpage has many different links to it, it should have a higher page rank.
 * See the spec for more details.
 */
public class PageRankAnalyzer {
    private IDictionary<URI, Double> pageRanks;

    /**
     * Computes a graph representing the internet and computes the page rank of all
     * available webpages.
     *
     * @param webpages  A set of all webpages we have parsed.
     * @param decay     Represents the "decay" factor when computing page rank (see spec).
     * @param epsilon   When the difference in page ranks is less then or equal to this number,
     *                  stop iterating.
     * @param limit     The maximum number of iterations we spend computing page rank. This value
     *                  is meant as a safety valve to prevent us from infinite looping in case our
     *                  page rank never converges.
     */
    public PageRankAnalyzer(ISet<Webpage> webpages, double decay, double epsilon, int limit) {
        // Implementation note: We have commented these method calls out so your
        // search engine doesn't immediately crash when you try running it for the
        // first time.
        //
        // You should uncomment these lines when you're ready to begin working
        // on this class.

        // Step 1: Make a graph representing the 'internet'
        IDictionary<URI, ISet<URI>> graph = this.makeGraph(webpages);

        // Step 2: Use this graph to compute the page rank for each webpage
        this.pageRanks = this.makePageRanks(graph, decay, limit, epsilon);

        // Note: we don't store the graph as a field: once we've computed the
        // page ranks, we no longer need it!
    }

    /**
     * This method converts a set of webpages into an unweighted, directed graph,
     * in adjacency list form.
     *
     * You may assume that each webpage can be uniquely identified by its URI.
     *
     * Note that a webpage may contain links to other webpages that are *not*
     * included within set of webpages you were given. You should omit these
     * links from your graph: we want the final graph we build to be
     * entirely "self-contained".
     */
    private IDictionary<URI, ISet<URI>> makeGraph(ISet<Webpage> webpages) {
        IDictionary<URI, ISet<URI>> webpageGraph = new ChainedHashDictionary<URI, ISet<URI>>();
        for (Webpage page : webpages) {
            URI pageUri = page.getUri();
            webpageGraph.put(pageUri, null);
        }
        for (Webpage page : webpages) {
            ISet<URI> pageUniqueLinks = new ChainedHashSet<URI>();
            URI pageUri = page.getUri();
            IList<URI> pageLinks = page.getLinks();
            for (URI link : pageLinks) {
                if (!link.equals(pageUri) && webpageGraph.containsKey(link)) {
                    pageUniqueLinks.add(link);
                }
            }
            
            webpageGraph.put(pageUri, pageUniqueLinks);
        }
        return webpageGraph;
    }

    /**
     * Computes the page ranks for all webpages in the graph.
     *
     * Precondition: assumes 'this.graphs' has previously been initialized.
     *
     * @param decay     Represents the "decay" factor when computing page rank (see spec).
     * @param epsilon   When the difference in page ranks is less then or equal to this number,
     *                  stop iterating.
     * @param limit     The maximum number of iterations we spend computing page rank. This value
     *                  is meant as a safety valve to prevent us from infinite looping in case our
     *                  page rank never converges.
     */
    private IDictionary<URI, Double> makePageRanks(IDictionary<URI, ISet<URI>> graph,
                                                   double decay,
                                                   int limit,
                                                   double epsilon) {
        IDictionary<URI, Double> initialPageRank = new ChainedHashDictionary<URI, Double>();
        int numPages = graph.size();
        
        for (KVPair<URI, ISet<URI>> pair : graph) {
            URI pageUri = pair.getKey();
            double initialRank = (1.0 / numPages);
            initialPageRank.put(pageUri, initialRank);
        }

        for (int i = 0; i < limit; i++) {
            IDictionary<URI, Double> newPageRank = new ChainedHashDictionary<URI, Double>();
            
            for (KVPair<URI, ISet<URI>> pair : graph) {
                newPageRank.put(pair.getKey(), 0.0);
            }
              
            for (KVPair<URI, ISet<URI>> page: graph) {
                URI currUri = page.getKey();
                double oldPageRank = initialPageRank.get(currUri);
                ISet<URI> outEdges = graph.get(currUri);
                int numLinks = outEdges.size();
                if (numLinks == 0) {
                    double increaseBy = (decay * oldPageRank) / numPages;
                    newPageRank = increaseAllPageRanks(newPageRank, increaseBy);
                } else {
                    double toIncrease = (decay * oldPageRank) / numLinks;
                    for (URI outEdgeLink : outEdges) {
                        double newRank = newPageRank.get(outEdgeLink) + toIncrease;
                        newPageRank.put(outEdgeLink, newRank);
                    }
                }
            }
            
            Double addToRank = (1.0 - decay)/numPages;
            newPageRank = increaseAllPageRanks(newPageRank, addToRank);
            
            boolean lessThanEpsilon = true;
            for (KVPair<URI, Double> link : newPageRank) {
                URI currUri = link.getKey();
                Double oldPageRank = initialPageRank.get(currUri);
                Double currPageRank = link.getValue();
                Double difference = Math.abs(currPageRank - oldPageRank);
                if (difference >= epsilon) {
                    lessThanEpsilon = false;
                }   
            } 

            if (lessThanEpsilon) {
                return newPageRank;
            }
            initialPageRank = newPageRank;
            newPageRank = null;
        }
        return initialPageRank;
    }

    private IDictionary<URI, Double> increaseAllPageRanks(IDictionary<URI, Double> newPageRank, Double increaseBy){
        for (KVPair<URI, Double> link : newPageRank) {
            Double currRank = newPageRank.get(link.getKey());
            newPageRank.put(link.getKey(), (currRank + increaseBy));
        }
        return newPageRank;
    }

    /**
     * Returns the page rank of the given URI.
     *
     * Precondition: the given uri must have been one of the uris within the list of
     *               webpages given to the constructor.
     */
    public double computePageRank(URI pageUri) {
        return this.pageRanks.get(pageUri);
    }
}
