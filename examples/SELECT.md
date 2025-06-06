# `SELECT`

Suppose we have these SQL script:

```sql
SELECT DISTINCT
    field1,
    field2
FROM
    table1
    LEFT JOIN table2 ON table1.id = table2.id
WHERE
    table1.id = 123
    AND table1.name = 'abc'
ORDER BY
    field1
LIMIT
    1 OFFSET 10
```

Then it is equal to

```java
SQLSelectBuilder builder = new SQLSelectBuilder()
    .select("field1")
    .select("field2")
    .distinct()

    .from("table1")

    .leftJoin("table2", SQLCondition.create("table1.id = table2.id"))

    .where(SQLCondition
        .create("table1.id = ? AND table1.name = ?"))

    .param(
        SQLParameter.of(123),
        SQLParameter.of("abc"))

    .orderBy("field1")
    .limitOffset(1, 10);

ParamSQL ps = builder.toParamSQL();

// Get a debug log here
logger.atDebug().log(ps.toDebugSQL());

// Prepared statement
try (PreparedStatement stmt = ps.executeWith(myConnection); ResultSet rs = stmt.executeQuery()) {
    // ..... Your object mapping code
}
```

If developer only want to have parameterized query and map them by other code, developer can use these method:

```java
// Keep support of SQL Parameter
ParamSQL ps = builder.toParamSQL();
String sql = ps.getPreparedSQL();

// Only string only
String rawSql = builder.toSQL();
```

## CTE Over subquery

Suggest we have this SQL:

```sql
SELECT
    table1.id,
    table1.name,
    p_total.product_total
FROM
    table1
    LEFT JOIN (
        SELECT
            id,
            COUNT(*) AS product_total
        FROM
            table2
    ) AS p_total ON table1.id = p_total.id
WHERE
    table1.id = 123
```

You SHOULD not do this:

```java
SQLSelectBuilder builder = new SQLSelectBuilder()
    .select("table1.id")
    .select("table1.name")
    .select("p_total.product_total")

    .from("table1")

    // DON'T
    .leftJoin("(SELECT id, COUNT(*) AS product_total FROM table2) AS p_total",
        SQLCondition.create("table1.id = p_total.id"))

    .where(SQLCondition.create("table1.id = ?"))
    .param(SQLParameter.of(123));
```

Consider rewrite it as CTE:

```sql
WITH p_total as (
    SELECT
        id,
        COUNT(*) AS product_total
    FROM
        table2
)

SELECT
    table1.id,
    table1.name,
    p_total.product_total
FROM
    table1
    LEFT JOIN  p_total ON table1.id = p_total.id
WHERE
    table1.id = 123
```

Then you can write this in java

```java
SQLSelectBuilder builder = new SQLSelectBuilder()
    .from("table1")
    .select("table1.id")
    .select("table1.name")

    // DO
    .with("p_total", new SQLSelectBuilder()
        .select("id")
        .select("COUNT(*) AS product_total")
        .from("table2"))
    .leftJoin("p_total", SQLCondition.create("table1.id = p_total.id"))
    .select("p_total.product_total")

    .where(SQLCondition.create("table1.id = ?"))
    .param(SQLParameter.of(123));
```
