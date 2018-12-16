package com.legend.orm.core.model;

import java.util.ArrayList;
import java.util.List;

public class TableOptions {

        private List<Option> optionList = new ArrayList<>();

        public TableOptions options(String key, String value) {
            this.optionList.add(new Option(key, value));
            return this;
        }

        public List<Option> options() {
            return optionList;
        }
    }