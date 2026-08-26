# Snowman

## Database

Run the complete migration chain with:

```shell
alembic upgrade head
```

Roll back the latest revision with `alembic downgrade -1`, or remove all
migration-managed tables with:

```shell
alembic downgrade base
```

Set `SNOWMAN_DATABASE_URL` to use MySQL, using the credentials from
`src/main/resources/db/liquibase.properties`:

```shell
export SNOWMAN_DATABASE_URL='mysql+pymysql://username:password@localhost:3306/snowman'
```
