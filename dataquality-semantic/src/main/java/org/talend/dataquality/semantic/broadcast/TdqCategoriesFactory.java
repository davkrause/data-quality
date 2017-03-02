package org.talend.dataquality.semantic.broadcast;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.index.ClassPathDirectory;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

/**
 * Factory to produce serializable object containing DQ categories.
 */
public class TdqCategoriesFactory {

    private static final Logger LOGGER = Logger.getLogger(TdqCategoriesFactory.class);

    /**
     * Load categories from local lucene index and produce a TdqCategories object.
     * 
     * @return the serializable object
     */
    public static final TdqCategories createTdqCategories() {
        Collection<DQCategory> dqCats = CategoryRegistryManager.getInstance().listCategories(false);
        return createTdqCategories(dqCats.stream().map(o -> o.getName()).collect(Collectors.toSet()));
    }

    /**
     * Load categories from local lucene index and produce a TdqCategories object.
     * 
     * @param categories
     * @return the serializable object
     */
    public static final TdqCategories createTdqCategories(Set<String> categories) {
        CategoryRegistryManager crm = CategoryRegistryManager.getInstance();
        final Map<String, DQCategory> dqCategoryMap = new HashMap<>();
        for (DQCategory dqCat : crm.listCategories()) {
            if (categories.contains(dqCat.getName())) {
                dqCategoryMap.put(dqCat.getName(), dqCat);
            }
        }
        final BroadcastIndexObject dictionary;
        final BroadcastIndexObject keyword;
        final BroadcastRegexObject regex;
        try {
            try (Directory ddDir = ClassPathDirectory.open(crm.getDictionaryURI())) {
                dictionary = new BroadcastIndexObject(ddDir, categories);
                LOGGER.debug("Returning dictionary at path '{" + crm.getDictionaryURI() + "}'");
            }
            final URI kwPath = TdqCategoriesFactory.class.getResource(CategoryRecognizerBuilder.DEFAULT_KW_PATH).toURI();
            try (Directory kwDir = ClassPathDirectory.open(kwPath)) {
                keyword = new BroadcastIndexObject(kwDir, categories);
                LOGGER.debug("Returning keywords at path '{" + crm.getRegexURI() + "}'");
            }
            UserDefinedClassifier classifiers = crm.getRegexClassifier(true);
            regex = new BroadcastRegexObject(classifiers, categories);
            LOGGER.debug("Returning regexes at path '{" + crm.getRegexURI() + "}'");
            return new TdqCategories(dqCategoryMap, dictionary, keyword, regex);
        } catch (URISyntaxException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
