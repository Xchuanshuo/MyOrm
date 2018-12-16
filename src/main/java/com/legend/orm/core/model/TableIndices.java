package com.legend.orm.core.model;

import java.util.ArrayList;
import java.util.List;

public class TableIndices {
        private List<Index> indexList = new ArrayList<>();
        private Index primary;

        public TableIndices unique(String...columns) {
            this.indexList.add(new Index(false, true, columns));
            return this;
        }

        public TableIndices primary(String...columns) {
            this.primary = new Index(true, false, columns);
            this.indexList.add(primary);
            return this;
        }

        public Index primary() {
            return primary;
        }

        public TableIndices index(String...columns) {
            this.indexList.add(new Index(false, false, columns));
            return this;
        }

        public List<Index> indices() {
            return indexList;
        }
    }