# odysql

[![javadoc](https://javadoc.io/badge2/io.github.odysql/odysql/javadoc.svg)](https://javadoc.io/doc/io.github.odysql/odysql) [![Maven Central Version](https://img.shields.io/maven-central/v/io.github.odysql/odysql)](https://central.sonatype.com/artifact/io.github.odysql/odysql)

Odysql is a opinionated dynamic SQL builder, which support Java 8+.

This builder will help user to create prepared statement dynamically, provide feature:

-   lightweight SQL builder, using standard library only
-   reduce boilerplate involved when using `java.sql`
-   fluent API for builder

This library is completely opinionated as:

-   Not allow some syntax like `SELECT *`, `RIGHT JOIN`
-   Encourage CTE instead of subquery
-   Only allow limited type by `SQLParameter`, which are:
    -   `Integer`
    -   `Long`
    -   `Double`
    -   `String`,
    -   `java.sql.Date`,
    -   `java.sql.TimeStamp`
    -   `java.time.LocalDate`
    -   `java.time.LocalDateTime`
    -   `java.math.BigDecimal`
-   Create SQL string and fill prepared statement only, no ORM pattern
-   Developer need to control `Connection` and `ResultSet` themselves

## Example

Before using this library, you will need this in JDK 8:

```java
// Long SQL, easy for typo and create invalid SQL
String sql = "SELECT my_table.id, name, age, country_code, contact_table.primary_address,"
    + "create_at FROM customers LEFT JOIN contact_table ON contact_table.id = my_table.id "
    + "WHERE id = ? AND age BETWEEN ? AND ? AND country_code IN (?, ?, ?) ORDER BY age LIMIT 100";

// Log not include true values but only placeholders
log.trace(sql);

try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    // Require correct function and number used
    stmt.setInt(1, 123);
    stmt.setInt(2, 20);
    stmt.setInt(3, 30);
    stmt.setString(4, "USA");
    stmt.setString(5, "CAN");
    stmt.setString(6, "GBR");

    // Log that may not able to show true value, depend on database driver
    log.trace("{}", stmt);

    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            // .....
        }
    }
}
```

After:

```java
// Create flexible list 1st
List<String> countryCodes = Arrays.asList(
    SQLParameter.of("USA"),
    SQLParameter.of("CAN"),
    SQLParameter.of("GBR"));

// Fluent pattern
SQLSelectBuilder builder = new SQLSelectBuilder()
    .from("customers")
    .select("my_table.id")
    .select("name")
    .select("age")
    .select("country_code")
    .select("create_at")

    // Allow grouping by code as flexible order
    .leftJoin("contact_table", SQLCondition.create("contact_table.id = my_table.id"))
    .select("contact_table.primary_address")

    .where(SQLCondition
        .create("id = ?")
        // Comment is allowed between line of code, but not shown in log
        .and("age BETWEEN ? AND ?")
        // Support method to easily create IN (?, ?) with correct number
        .and(SQLCondition.inPlaceHolders("country_code", countryCodes.size())))

    // Support adding actual values on needs
    .param(
        SQLParameter.of(123),
        SQLParameter.of(20), SQLParameter.of(30))
    .param(countryCodes)

    .orderBy("age")
    .limit(100);

// Checking will perform to ensure essential element exist, e.g. table name
ParamSQL ps = builder.toParamSQL();

// Include true value in logging, not placeholders, improve debug experience
log.trace(ps.getDebugSQL());

// Simplified Code, reduce nested level
try (PreparedStatement stmt = ps.prepare(conn); ResultSet rs = stmt.executeQuery()) {
    while (rs.next()) {
        // .....
    }
}
```

Or in JDK 15 Later, you can use another approach:

```java
// Blocked String, but lost advantage for comments at middle
String sql = """
    SELECT
        my_table.id,
        name,
        age,
        country_code,
        contact_table.primary_address,
        create_at
    FROM
        customers
    LEFT JOIN
        contact_table ON contact_table.id = my_table.id
    WHERE
        id = ?
        AND age BETWEEN ? AND ?
        AND country_code IN (?, ?, ?)
    ORDER BY
        age
    LIMIT
        100
    """;

ParamSQL ps = new ParamSQL(ps,
    SQLParameter.of(123),
    SQLParameter.of(20),
    SQLParameter.of(30),
    SQLParameter.of("USA"), SQLParameter.of("CAN"), SQLParameter.of("GBR"));

// Include true value, not placeholders, improve debug experience
log.trace(ps.getDebugSQL());

// Simplified Code, reduce nested level
try (PreparedStatement stmt = ps.prepare(conn); ResultSet rs = stmt.executeQuery()) {
    while (rs.next()) {
        // .....
    }
}
```

You can view detail example here.

-   [After JDK 15](examples/after_jdk15.md)
-   [SELECT](examples/SELECT.md)
-   [INSERT](examples/INSERT.md)

## Development

### Java runtime

This project is require Java 21 or later when developing, but it will create a Java 8 compatible library.

Developer should be careful on the compatibility, and prevent to use API not belong to java 8, e.g.

-   `List.of(...)`
-   `String.isBlank()`, `String.repeat(...)`
-   `Stream.toList()`

### Versioning

This project will use semver.

## FAQ

**How about my object mapper?**

Come on, do it yourself.

**Is this an ORM?**

No. Personally, I hate ORM that hide too many details from me. It should be at least 2 separate library:

-   SQL builder
-   Object Mapper

This is SQL builder only, or you can call it `PrepareStatement` builder & filler. You have to do `ResultSet` job yourself.

**Why not support more type?**

1. Keep it simple and stupid, you actually not need to use so many type
2. It take effort to ensure it works. This library is just non-commercial project

**Why not `SELECT *`**

Please google, this has been state by other smarter people.

**Why not `RIGHT JOIN`**

It can always be rewrite to better `LEFT JOIN`.

**Why encourage CTE instead of sub-query**

To ensure readability, too many sub-query will increase complexity to read and debug.
CTE is much better that ensure all part can be easily tested separately.
Modern database has similar optimization approach on CTE and subquery.

**It is so ugly.**

Thanks. But I think it is better than Java original approach.
