# This is a sample spring boot 3 multitenancy using separate database.

In this example, we are maintaining **Application Users** and **Tenancy config** in the master database. Other business entities like **Employee** will be created in its own database.

## Master DB
In the master application, we create admin user, and he can only create tenants and app users assigned for each tenant. When user logged in, we obtain tenant_id for the user and attach appropriate datasource to the tenant database.

## Tenant DB
- Prerequisites: Tenant db should be created upfront in the database with necessary privileges.
  
We need to create tenant database manually. Though we can automate this process, this is not done, because create a new database and assigning privileges to the user will depends on company's policy. So we don't want to give full access to the database for the user.

After database is created, you can call /tenants api to store the database Url, username, password to connect to the database.

