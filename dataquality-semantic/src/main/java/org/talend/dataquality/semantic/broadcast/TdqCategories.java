package org.talend.dataquality.semantic.broadcast;

import java.io.Serializable;
import java.util.Map;

import org.talend.dataquality.semantic.model.DQCategory;

/**
 * A container object for DQ dictionaries.
 */
public class TdqCategories implements Serializable {

    private static final long serialVersionUID = 8077049508746278932L;

    private final Map<String, DQCategory> metadata;

    private final BroadcastIndexObject dictionary;

    private final BroadcastIndexObject keyword;

    private final BroadcastRegexObject regex;

    /**
     * Constructor
     * 
     * @param metadata
     * @param dictionary
     * @param keyword
     * @param regex
     */
    public TdqCategories(Map<String, DQCategory> metadata, BroadcastIndexObject dictionary, BroadcastIndexObject keyword,
            BroadcastRegexObject regex) {
        this.metadata = metadata;
        this.dictionary = dictionary;
        this.keyword = keyword;
        this.regex = regex;
    }

    public Map<String, DQCategory> getCategoryMetadata() {
        return metadata;
    }

    public BroadcastIndexObject getDictionary() {
        return dictionary;
    }

    public BroadcastIndexObject getKeyword() {
        return keyword;
    }

    public BroadcastRegexObject getRegex() {
        return regex;
    }

}
