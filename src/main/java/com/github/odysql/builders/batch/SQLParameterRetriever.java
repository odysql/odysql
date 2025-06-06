package com.github.odysql.builders.batch;

import com.github.odysql.models.SQLParameter;

/**
 * Function to get SQL parameter from given data.
 * 
 * @param <DataT> data type for data
 */
@FunctionalInterface
public interface SQLParameterRetriever<DataT> {
    /**
     * Retrieve SQL Parameter for a column value in item with specified data type.
     *
     * @param item data in type of DataT
     * @return SQL Parameter
     */
    SQLParameter retrieve(DataT item);
}