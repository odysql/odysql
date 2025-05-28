# odysql

Odysql is a opinionated dynamic SQL builder, which support Java 8+.

This builder will help user to create prepared statement dynamically, provide feature:
- lightweight SQL builder, as only `slf4j` dependency used
- reduce boilerplate invloved when using `java.sql`
- fluent API for builder

This library is completly opinionated as:
- Not allow some syntax like `SELECT *`, `RIGHT JOIN`
- Encourage CTE instead of subquery
- Create SQL string and statement only, no ORM pattern
- Developer need to control `Connection` and `ResultSet` themselves
