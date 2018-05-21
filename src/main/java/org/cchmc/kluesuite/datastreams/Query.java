package org.cchmc.kluesuite.datastreams;

/**
 * Query data read from files.
 * Created by MALS7H on 5/18/2017.
 */
public class Query {
    /**
     * Name of Query
     */
    public String queryName;
    /**
     * Name of the sequence
     */
    public String querySequence;

    /**
     * Make a query with public fields.
     * @param queryName Query Name
     * @param querySequence Sequence for query
     */
    public Query(String queryName, String querySequence) {
        this.queryName = queryName;
        this.querySequence = querySequence;
    }
}
