package io.github.odysql.builders.single;

import java.util.ArrayList;
import java.util.List;

import io.github.odysql.models.SQLParameter;
import io.github.odysql.models.SQLUnionType;

/** SQL builder for select statement that involve UNION. */
public class SQLUnionSelectBuilder implements SingleSQLBuildable {

    private List<SQLSelectBuilder> selects = new ArrayList<>();
    private List<SQLUnionType> types = new ArrayList<>();
    private List<String> orderCols = new ArrayList<>();

    private SQLUnionSelectBuilder(SQLSelectBuilder firstSelect) {
        this.selects.add(firstSelect);
    }

    /**
     * Create a UNION select builder with first SELECT statement. Developer should
     * call {@link #union(SQLSelectBuilder)} or {@link #unionAll(SQLSelectBuilder)}
     * after this function for at least once.
     * 
     * @param firstSelect 1st SELECT statement
     * @return new SQL union select builder
     */
    public static SQLUnionSelectBuilder create(SQLSelectBuilder firstSelect) {
        return new SQLUnionSelectBuilder(firstSelect);
    }

    /**
     * Append UNION statement with another SELECT with this SQL builder.
     * 
     * @param another another SELECT SQL builder
     * @return this
     */
    public SQLUnionSelectBuilder union(SQLSelectBuilder another) {
        this.selects.add(another);
        this.types.add(SQLUnionType.UNION);
        return this;
    }

    /**
     * Append UNION ALL statement with another SELECT with this SQL builder.
     * 
     * @param another another SELECT SQL builder
     * @return this
     */
    public SQLUnionSelectBuilder unionAll(SQLSelectBuilder another) {
        this.selects.add(another);
        this.types.add(SQLUnionType.UNION_ALL);
        return this;
    }

    /**
     * Use ORDER BY syntax. Developer can specific ASC or DESC when input the column
     * name, e.g. "col1 DESC" or "col2 ASC".
     * <p>
     * Please note that ORDER BY syntax is affected by the column ordering.
     * 
     * @param columnName column name, can include alias (AS), or more complex
     *                   statement
     * @return this
     */
    public SQLUnionSelectBuilder orderBy(String columnName) {
        this.orderCols.add(columnName);
        return this;
    }

    /**
     * Check if the builder is valid.
     * 
     * @return true if valid, false otherwise
     */
    private boolean checkIfValid() {
        // Ensure at least two SQL select
        if (selects.size() < 2) {
            return false;
        }

        // Ensure size of types + 1 = size of select
        return types.size() + 1 == selects.size();
    }

    /**
     * Construct SQL string from builder.
     * 
     * @return constructed SQL, which is parameterized.
     * @throws IllegalStateException when sql builder is invalid
     */
    private String constructSQL() throws IllegalStateException {
        if (!checkIfValid()) {
            throw new IllegalStateException("SQL builder is invalid");
        }

        StringBuilder sb = new StringBuilder();

        int i = 0;
        while (i < selects.size()) {
            sb.append(selects.get(i).toSQL());

            // Prevent out of index of UNION type list
            if (i < types.size()) {
                sb.append(" " + types.get(i).asSQL() + " ");
            }

            i++;
        }

        // ORDER BY
        if (!this.orderCols.isEmpty()) {
            sb.append(" ORDER BY " + String.join(", ", this.orderCols));
        }

        return sb.toString();
    }

    @Override
    public List<SQLParameter> getParams() {
        List<SQLParameter> params = new ArrayList<>();

        // Loop all selects
        for (SQLSelectBuilder s : selects) {
            params.addAll(s.getParams());
        }

        return params;
    }

    @Override
    public ParamSQL toParamSQL() {
        String sql = constructSQL();
        return new ParamSQL(sql, this.getParams());
    }

    @Override
    public String toSQL() {
        return constructSQL();
    }
}
