# `INSERT`

## Simple Insert

Suppose we have these SQL script:

```sql
INSERT INTO
    my_table (col1, col2, col3)
VALUES
    (123, "abc", 456.78)
```

Then it is equal to

```java
SQLInsertBuilder builder = new SQLInsertBuilder()
    .from("my_table")
    .insert("col1", SQLParameter.of(123))
    .insert("col2", SQLParameter.of("abc"))
    .insert("col3", SQLParameter.of(456.78));
ParamSQL ps = builder.toParamSQL();

// Get a debug log here
logger.atDebug().log(ps.toDebugSQL());

// Build prepared statement by inject connection
try (PreparedStatement stmt = ps.prepare(myConnection)) {
    stmt.executeUpdate();

    // OR any code as developer like
}
```

## Bulk Insert

`odysql` has a experimental feature for easily bulk insert to database.

Suppose we have this SQL:

```sql
INSERT INTO
    my_table (id, username, amount)
VALUES
    (?, ?, ?)
```

And here is data bean in java

```java
public class BeanUserAmount {
    private int id;
    private String username;
    private double amount;

    // Getter & Setter...
}
```

Then `odysql`'s `SQLBatchInsertBuilder` can make your life easier:

```java
// Your data list
List<BeanUserAmount> data;

// ... Prepare your data

// Create runner with SQL builder pattern
SQLBatchInsertRunner<BeanUserAmount> runner = new SQLBatchInsertBuilder<BeanUserAmount>()
    .into("my_table")
    .insert("id", item -> SQLParameter.of(item.getId()))
    .insert("username", item -> SQLParameter.of(item.getUsername()))
    .insert("amount", item -> SQLParameter.of(item.getAmount()))
    .toBatchRunner()

    // Config Runner
    .setData(data)
    .setMaxBatchSize(1000);

// Run runner
try {
    int totalAffected = runner.executeWith(myConnection);
    logger.debug("{}", totalAffected);

    myConnection.commit();

} catch (SQLException ex) {
    myConnection.rollback();
}
```

`odysql` design is to leave the moment to `commit` to developer instead of library.

If you are interested on getting logs, then:

```java
runner.setLogEnabled(true); // It will increase some overhead

// Run runner
try {
    int totalAffected = runner.executeWith(myConnection);
    logger.debug("{}", totalAffected);

    for (String str : runner.getDebugSQL()) {
        logger.trace(str);
    }

    myConnection.commit();

} catch (SQLException ex) {
    myConnection.rollback();
}
```

Each `str` represent a batch executed. The batch SQL will have some rewrite like:

```sql
-- Debug log
INSERT INTO
    my_table (id, username, amount)
VALUES
    (1, "Alice", 123.45),
    (2, "Bob", 2000.0),
    (3, "Charlie", 400.0),
    (4, "David", 500.0)
```
