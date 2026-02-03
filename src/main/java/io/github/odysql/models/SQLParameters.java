package io.github.odysql.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utils to create list of {@code SQLParameter}, which ensure not using java
 * stream due to possible performance issue, and reduce boilerplate involved
 * when write convert functions.
 * <p>
 * This utils class only provide method to convert list of common type, which
 * should enough for most real life usage. Some class like {@code java.sql.Date}
 * is ignored as real data rarely use them when storing data.
 */
public class SQLParameters {
    private SQLParameters() {
    }

    /**
     * Create list of {@code SQLParameter} from list of Integer.
     * 
     * @param list list of Integer
     * @return list of {@code SQLParameter}
     */
    public static final List<SQLParameter> ints(List<Integer> list) {
        List<SQLParameter> result = new ArrayList<>();

        for (Integer item : list) {
            result.add(SQLParameter.of(item));
        }

        return result;
    }

    /**
     * Create list of {@code SQLParameter} from list of Long.
     * 
     * @param list list of Long
     * @return list of {@code SQLParameter}
     */
    public static final List<SQLParameter> longs(List<Long> list) {
        List<SQLParameter> result = new ArrayList<>();

        for (Long item : list) {
            result.add(SQLParameter.of(item));
        }

        return result;
    }

    /**
     * Create list of {@code SQLParameter} from list of Double.
     * 
     * @param list list of Double
     * @return list of {@code SQLParameter}
     */
    public static final List<SQLParameter> doubles(List<Double> list) {
        List<SQLParameter> result = new ArrayList<>();

        for (Double item : list) {
            result.add(SQLParameter.of(item));
        }

        return result;
    }

    /**
     * Create list of {@code SQLParameter} from list of String.
     * 
     * @param list list of String
     * @return list of {@code SQLParameter}
     */
    public static final List<SQLParameter> strings(List<String> list) {
        List<SQLParameter> result = new ArrayList<>();

        for (String item : list) {
            result.add(SQLParameter.of(item));
        }

        return result;
    }

    /**
     * Create list of {@code SQLParameter} from list of {@code LocalDate}.
     * 
     * @param list list of {@code LocalDate}
     * @return list of {@code SQLParameter}
     */
    public static final List<SQLParameter> localDates(List<LocalDate> list) {
        List<SQLParameter> result = new ArrayList<>();

        for (LocalDate item : list) {
            result.add(SQLParameter.of(item));
        }

        return result;
    }

    /**
     * Create list of {@code SQLParameter} from list of {@code LocalDateTime}.
     * 
     * @param list list of {@code LocalDateTime}
     * @return list of {@code SQLParameter}
     */
    public static final List<SQLParameter> localDateTimes(List<LocalDateTime> list) {
        List<SQLParameter> result = new ArrayList<>();

        for (LocalDateTime item : list) {
            result.add(SQLParameter.of(item));
        }

        return result;
    }

    /**
     * Create list of {@code SQLParameter} from list of {@code BigDecimal}.
     * 
     * @param list list of {@code BigDecimal}
     * @return list of {@code SQLParameter}
     */
    public static final List<SQLParameter> decimals(List<BigDecimal> list) {
        List<SQLParameter> result = new ArrayList<>();

        for (BigDecimal item : list) {
            result.add(SQLParameter.of(item));
        }

        return result;
    }
}
