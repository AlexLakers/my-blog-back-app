package com.alex.blog.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class QueryBuilder {

        private final StringBuilder baseSql;
        private final List<String> conditions = new ArrayList<>();
        private String appendedSql;

        public QueryBuilder(String baseQuery) {
            this.baseSql = new StringBuilder(baseQuery);
        }

        public QueryBuilder addJoinIf(boolean condition, String joinClause) {
            if (condition) {
                baseSql.append(" ").append(joinClause);
            }
            return this;
        }

        public QueryBuilder addConditionIf(boolean condition, String conditionClause, Runnable paramSetter) {
            if (condition) {
                conditions.add(conditionClause);
                paramSetter.run();
            }
            return this;
        }
        public QueryBuilder append(String additionalSql) {
           this.appendedSql = additionalSql;
           return this;
        }

        public String build() {
                applyConditionsIfPresent();
                baseSql.append(" ").append(appendedSql);
            return baseSql.toString();
        }

        private void applyConditionsIfPresent() {
            if (!conditions.isEmpty()) {
                String whereClause = conditions.stream()
                        .collect(Collectors.joining(" AND ", " WHERE ", ""));
                baseSql.append(" ").append(whereClause);
            }
        }
    }
