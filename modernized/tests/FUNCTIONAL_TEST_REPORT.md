# CardDemo Modernization — Functional Test Report

**Date:** 2026-10-23
**System under test:** React.js + Spring Boot conversion of the COBOL/CICS CardDemo application
**Base URL:** `http://localhost:8080` (Spring Boot 3.3.5, Java 17, H2 in-memory DB seeded from the mainframe VSAM/PS sample data)
**Test suite:** `modernized/tests/functional_tests.py` — 89 automated functional test cases exercising every REST endpoint over HTTP.

---

## Final Score

| Metric | Result |
|---|---|
| **Functional test score** | **100.0%** |
| Total test cases | 89 |
| Passed | 89 |
| Failed | 0 |
| Application status | **PASS — all functions verified end-to-end** |

## Coverage by feature (all passing)

| Area (original program) | Tests | What was verified |
|---|---|---|
| Sign-on / auth (COSGN00C) | 10 | User+admin login, wrong password, unknown user, empty fields, missing/invalid token, logout invalidates session |
| Menus (COMEN01C/COADM01C) | 3 | User menu (11 options), admin menu (8 options incl. batch), system date |
| Account view (COACTVWC) | 4 | Joined account+customer+cards, card-number lookup, missing account error, seed values (bal 194.00) |
| Account update (COACTUPC) | 5 | Field updates, `*` keep convention, status/SSN/FICO validation errors |
| Card list/view/update (COCRDLIC/COCRDSLC/COCRDUPC) | 13 | 7-per-page paging + filters, 11-digit acct & 16-digit card filter rules, expiry month 1–12, year 1950–2099, Y/N status, update persistence |
| Transaction list/view (COTRN00C/01C) | 5 | 10-per-page, sorted, browse-from-id (STARTBR), detail, not-found message |
| Transaction add (COTRN02C/POSTTRAN) | 7 | Confirm gate, field validation order, invalid card (100-class), OVERLIMIT, posting updates balance, 16-char zero-padded ID |
| Reports (CORPT00C) | 5 | Monthly, yearly (Jan1–Dec31), custom range, bad-date and inverted-range rejection |
| Bill payment (COBIL00C) | 6 | Balance view, Y/N validation, N=cancel, Y pays full balance → 0, "nothing to pay" |
| Admin user CRUD (COUSR00–03C) | 11 | List, admin-only 403, add/dup/field errors, update, self-delete blocked, delete + sign-on revoked |
| Tran types/categories (Db2 modules) | 8 | List, categories, add/dup/2-digit rules, update, not-found, delete |
| Batch jobs (CBACT04C, CBSTM03A, CBPAUP0J) | 6 | Interest calc over all 50 tran-cat-bal records, type-01/cat-5 'System' interest trans, statement generation, purge authorizations, admin-only |
| Pending authorizations (IMS/DB2 module) | 6 | Account-grouped summary, detail, match→'M', duplicate-match rejection, fraud flagging |

## Issues found during testing and resolved

1. **PendingAuth seed silently empty** — `DataLoader.seedPendingAuths()` built `PendingAuth` objects but never called `list.add(a)`; the summary endpoint returned empty. **Fixed** — confirmed 8 rows seeded.
2. **Bill-payment `confirm='N'` semantics** — service treated 'N' as "ask again" instead of cancelling. Per `COBIL00C`, 'N' clears the screen and exits (cancel). **Fixed** — now returns `cancelled: true`, and 'N' is evaluated before the zero-balance check, matching the EVALUATE order in COBOL.
3. **Initial 429 from Maven Central** — repo build rate-limited on maven.org; resolved with a Google Cloud mirror in `~/.m2/settings.xml`.

## Known notes / pending items (non-blocking)

- Interest amounts post as `0.00` — faithful to `CBACT04C`: the seeded `TRAN-CAT-BAL` records genuinely carry zero balances (`{` = +0 zoned decimal in `tcatbal.txt`), so `bal × rate / 1200 = 0`. Correct behavior, not a bug.
- Pending-authorization seed rows are synthesized in the backend (the IMS EBCDIC dataset isn't directly loadable); the 8 generated rows exercise the full summary/detail/match/fraud flow.
- All work is **uncommitted** under `modernized/` (plus `.devin/blueprint.yaml`) inside the repo — awaiting your approval before any commit/branch/PR.

## How to re-run

```bash
# start API (reseeds H2 in-memory DB each boot)
java -jar modernized/backend/target/carddemo-backend-1.0.0.jar &

# functional suite
python3 modernized/tests/functional_tests.py

# UI: dev server on :5173 proxies /api → :8080
cd modernized/frontend && npm install && npm run dev
```

Logins: `ADMIN001`/`PASSWORD` (admin), `USER0001`/`PASSWORD` (user).
