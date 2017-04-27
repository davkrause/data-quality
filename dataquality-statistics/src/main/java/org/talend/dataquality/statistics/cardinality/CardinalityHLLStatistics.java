// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.statistics.cardinality;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;
import com.clearspring.analytics.stream.cardinality.HyperLogLog;

/**
 * Cardinality statistics bean of hyper log log .
 *
 * @author zhao
 */
public class CardinalityHLLStatistics extends AbstractCardinalityStatistics<CardinalityHLLStatistics> {

    private HyperLogLog hyperLogLog = null;

    public HyperLogLog getHyperLogLog() {
        return hyperLogLog;
    }

    public void setHyperLogLog(HyperLogLog hyperLogLog2) {
        this.hyperLogLog = hyperLogLog2;
    }

    public long getDistinctCount() {
        return hyperLogLog.cardinality();
    }

    public void add(Object colObj) {
        this.hyperLogLog.offer(colObj);
    }

    /**
     * <b>This method merges two instances of CardinalityHLLStatistics. </b>
     * <p>
     * If the other instance is a instance of CardinalityHLLStatistics but its {@link HyperLogLog} instance
     * cannot be merged with the current HyperLogLog instance, this method will throw the exception triggered by
     * the {@link HyperLogLog#addAll(HyperLogLog)} method.
     * </p>
     *
     * @param other An other instance of CardinalityHLLStatistics
     */
    public void merge(CardinalityHLLStatistics other) throws CardinalityMergeException {
        this.hyperLogLog.addAll(other.hyperLogLog);
        super.count += other.count;
    }
}
