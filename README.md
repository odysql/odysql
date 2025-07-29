# odysql
[![javadoc](https://javadoc.io/badge2/io.github.odysql/odysql/javadoc.svg)](https://javadoc.io/doc/io.github.odysql/odysql)

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
-   Create SQL string and fill prepared statement only, no ORM pattern
-   Developer need to control `Connection` and `ResultSet` themselves

## Example

-   [After JDK 15](examples/after_jdk15.md)
-   [SELECT](examples/SELECT.md)
-   [INSERT](examples/INSERT.md)

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

Please google, this has been state by other people that is much smarter.

**Why not `RIGHT JOIN`**

It can always be rewrite to better `LEFT JOIN`.

**Why encourage CTE instead of sub-query**

To ensure readability, too many sub-query will increase complexity to read and debug.
CTE is much better that ensure all part can be easily tested separately.
Modern database has similar optimization approach on CTE and subquery.

**It is so ugly.**

Thanks. But I think it is better than Java original approach.
