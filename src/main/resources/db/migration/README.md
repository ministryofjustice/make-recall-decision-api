# Database Migration Scripts

This directory contains database migration scripts...

They need to be in `SQL` format and have the following naming conventions:

```
V<version_number>__<description>.sql
```

Where:

- `<version_number>` is an incrementing integer - i.e. `1`, `2`, `3` etc
- `<description>` describes what the migration is doing

These migrations will be applied on application startup.

## Warnings on Backwards Compatibility

Keep your migrations **backwards compatible** - the reason for this is that when you deploy your application onto kubernetes it will do a "rolling deployment". This means that **2 versions** of the application are running bat the same time (the old version, plus the new version). So whilst the new version of the application is starting up and performing its migrations, the old version must be able to continue to run - if not, the pods will crash, kubernetes will get into a mixed state, your deployment will fail and the app **will go offline** - you've been warned. :)  

So...

If you need to rename a database field or change its type, simply create a **new** field and copy across the data into it (as part of the migration) whilst also changing the application code to use the new field. Once the application version is deployed and happily running, create a second migration to remove the old field.

If you need to rename a table, the approach is the same - create and copy in the first migration/release, then clean up in a second migration/release.
