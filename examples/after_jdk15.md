## odysql with JDK 15 or above

In JDK 15, text block is introduced to give readability, therefore it is ok to do this:

```java
String sql = """
    SELECT
        col1, col2
    FROM
        myTable
    WHERE
        id = ? AND col3 = ?
    """

ParamSQL ps = new ParamSQL(sql, Arrays.asList(
    SQLParameter.of(114),
    SQLParameter.of(514)));

// Get a debug log here
logger.atDebug().log(ps.toDebugSQL());

// Prepared statement
try (PreparedStatement stmt = ps.executeWith(myConnection); ResultSet rs = stmt.executeQuery()) {
    // ..... Your object mapping code
}
```

Please note that this library is mainly for messy codebase that still using java8.
