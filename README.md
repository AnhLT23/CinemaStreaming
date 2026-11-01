# CinemaStreaming

## Java runtime and build

This application targets Java 21 (LTS). Build and deploy it using a Java 21 JDK,
and use a Jakarta EE 10-compatible application server that supports Java 21.

The Maven compiler is configured with `maven.compiler.release=21`; the WAR is
packaged with the Maven WAR plugin. Run `mvn clean test-compile` to compile the
application and test sources, then `mvn clean test` to run the Maven test suite.

## Database connection

Set `CINEMA_DB_USER` and `CINEMA_DB_PASSWORD` before starting the application.
The database URL defaults to the local SQL Server database `CinemaStreamingDB`;
override it with `CINEMA_DB_URL` when needed. Do not commit database credentials.

On PowerShell, configure these variables for the current terminal session:

```powershell
$env:CINEMA_DB_USER = "your-database-user"
$env:CINEMA_DB_PASSWORD = "your-database-password"
# Optional:
$env:CINEMA_DB_URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=CinemaStreamingDB;encrypt=false"
```

## Demo payments

Choose **Ngân hàng ảo (Demo)** on the membership page to test a payment without
transferring real money. The generated QR opens a confirmation page; confirming
the order records it as successful and activates the VIP plan or movie rental.
For QR scans from another device, set `DEMO_PAYMENT_BASE_URL` to the public
HTTP(S) origin that forwards to this application (for example, your dev tunnel).
The optional VietQR transfer method still requires the `PAYMENT_BANK_NAME`,
`PAYMENT_BANK_CODE`, `PAYMENT_BANK_ACCOUNT`, and `PAYMENT_ACCOUNT_NAME`
environment variables and a real payment webhook for automatic confirmation.

## Demo accounts

After loading `CinemaStreamingDB.sql`, the login page offers four guest accounts
(`guest_01` through `guest_04`) with the shared demo password `111111`.
These accounts and all seeded user records are for local testing only; do not
use them in a public deployment. Seeded payment account details are placeholders
and must never be used to transfer money.

## Account workspaces

The account page presents role-specific workspaces for administrators, staff,
registered customers, and guest accounts. Administrators can open the admin
console, staff can open rental support, and customer/guest pages show the
account's own membership, rental, and viewing information where applicable.
Portal visibility does not grant access: servlet filters continue to enforce
the role permissions for every protected route.
