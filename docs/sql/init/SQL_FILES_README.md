# SQL Seed Files

This directory keeps only small, non-sensitive SQL files that are safe to review in source control.

The following files were intentionally removed from the public tree:

- full PostgreSQL dumps
- remote server export files
- news article dumps containing crawled article text
- generated ETF composition/list datasets
- user, token, login-history, notification, and portfolio data

If you need local data for development, create it from the schema in `db/init.sql` or generate a private seed file outside this repository.

Do not commit:

- production or staging database dumps
- user data
- authentication tokens
- crawled full-text news datasets
- generated market-data snapshots
- SSH commands, private hostnames, or deployment key names
