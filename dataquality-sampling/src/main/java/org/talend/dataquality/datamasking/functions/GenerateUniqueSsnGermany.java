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
package org.talend.dataquality.datamasking.functions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dprot class global comment. Detailled comment
 * 
 * German pattern: aaaaaaaaaaaa
 * aaaaaaaaaaaa: 1 -> 100000000000 - 1
 */

public class GenerateUniqueSsnGermany extends AbstractGenerateUniqueSsn {

    private static final long serialVersionUID = -2321693247791991249L;

    @Override
    protected List<AbstractField> createFieldsListFromPattern() {
        List<AbstractField> fields = new ArrayList<>();
        fields.add(new FieldInterval(1, 100000000000L - 1));
        return fields;
    }

    @Override
    protected StringBuilder doValidGenerateMaskedField(String str) {
        // read the input str
        List<String> strs = new ArrayList<>();
        strs.add(str.substring(0, 11));

        if (super.ssnPattern == null) {
            List<AbstractField> fields = createFieldsListFromPattern();
            super.ssnPattern = new GenerateUniqueRandomPatterns(fields);
        }

        return super.ssnPattern.generateUniqueString(strs);
    }
}
