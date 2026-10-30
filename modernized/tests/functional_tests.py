#!/usr/bin/env python3
"""CardDemo functional test suite — exercises the Spring Boot API end-to-end
covering the same functions the CICS/COBOL application exposed.
Run: python3 functional_tests.py [base_url]
"""
import json, os, sys, urllib.request, urllib.error, urllib.parse

BASE = sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080'
RESULTS_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'results.json')
results = []          # (feature, name, passed, detail)
issues_resolved = []
issues_pending = []

def call(method, path, token=None, body=None):
    req = urllib.request.Request(BASE + path, method=method)
    req.add_header('Content-Type', 'application/json')
    if token:
        req.add_header('X-Auth-Token', token)
    data = json.dumps(body).encode() if body is not None else None
    try:
        with urllib.request.urlopen(req, data, timeout=15) as r:
            return r.status, json.loads(r.read() or b'{}')
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read() or b'{}')
        except Exception:
            return e.code, {}

def check(feature, name, cond, detail=''):
    results.append((feature, name, bool(cond), detail))
    return cond

def login(uid, pw):
    s, d = call('POST', '/api/auth/login', body={'userId': uid, 'password': pw})
    return s, d

# ---------------- AUTH / SIGN-ON (COSGN00C) ----------------
F = 'Auth'
s, d = login('USER0001', 'PASSWORD')
UTOK = d.get('token')
check(F, 'user sign-on returns token+profile', s == 200 and UTOK and d.get('userType') == 'U' and d.get('firstName'), f'code={s}')

s, d = login('ADMIN001', 'PASSWORD')
ATOK = d.get('token')
check(F, 'admin sign-on returns token, type A', s == 200 and ATOK and d.get('userType') == 'A', f'code={s}')

s, d = login('USER0001', 'WRONG')
check(F, 'wrong password rejected (COBOL msg)', s == 401 and 'Wrong Password' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = login('NOUSER99', 'PASSWORD')
check(F, 'unknown user rejected', s == 401 and 'User not found' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = login('', 'PASSWORD')
check(F, 'empty user id rejected', s == 400 and 'User ID' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = login('USER0001', '')
check(F, 'empty password rejected', s == 400 and 'Password' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('GET', '/api/menus')
check(F, 'no token → 401', s == 401, f'code={s}')

s, d = call('GET', '/api/menus', token='bogus-token')
check(F, 'invalid token → 401 session expired', s == 401 and 'expired' in d.get('message', '').lower(), f'code={s}')

# ---------------- MENUS (COMEN01C/COADM01C) ----------------
F = 'Menus'
s, d = call('GET', '/api/menus', token=UTOK)
names = [o['name'] for o in d.get('options', [])]
check(F, 'user menu has all 11 options', len(names) == 11, f'got {len(names)}: {names[:4]}...')
check(F, 'user menu option numbering 1..11', [o['num'] for o in d.get('options', [])] == list(range(1, 12)))

s, d = call('GET', '/api/menus', token=ATOK)
anames = [o['name'] for o in d.get('options', [])]
check(F, 'admin menu distinct & ≥6 options', len(anames) >= 6 and any('User List' in n for n in anames), f'{len(anames)}: {anames}')

s, d = call('GET', '/api/system/date', token=UTOK)
check(F, 'system date endpoint', s == 200 and len(d.get('date', '')) >= 8, str(d))

# ---------------- ACCOUNT VIEW (COACTVWC) ----------------
F = 'Account View'
s, d = call('GET', '/api/accounts/1', token=UTOK)
a1 = d.get('account', {})
check(F, 'account 1 view: joined acct+customer+cards', s == 200 and a1.get('acctId') == 1 and d.get('customer') and len(d.get('cards', [])) >= 1, f'bal={a1.get("currBal")} cards={len(d.get("cards",[]))}')
check(F, 'account 1 balance 194.00 matches seed', abs(float(a1.get('currBal', -1)) - 194.00) < 0.01, str(a1.get('currBal')))

s, d = call('GET', '/api/accounts?cardNum=0500024453765740', token=UTOK)
check(F, 'lookup account by card number', s == 200 and d.get('account', {}).get('acctId') == 50, f'code={s}')

s, d = call('GET', '/api/accounts/999999', token=UTOK)
check(F, 'missing account → error', s in (400, 404), f'{s}: {d.get("message")}')

# ---------------- ACCOUNT UPDATE (COACTUPC) ----------------
F = 'Account Update'
s, d = call('PUT', '/api/accounts/2', token=UTOK, body={'firstName': 'TESTFIRST'})
check(F, 'update first name only (others keep)', s == 200 and d.get('customer', {}).get('firstName') == 'TESTFIRST' and d.get('message'), f'{s}: {d.get("message")}')

s, d = call('PUT', '/api/accounts/2', token=UTOK, body={'activeStatus': 'X'})
check(F, 'invalid active status rejected', s == 400, f'{s}: {d.get("message")}')

s, d = call('PUT', '/api/accounts/2', token=UTOK, body={'ssn': '12345'})
check(F, 'SSN must be 9 digits', s == 400 and 'SSN' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('PUT', '/api/accounts/2', token=UTOK, body={'ficoCreditScore': '9999'})
check(F, 'FICO must be 3 digits', s == 400, f'{s}: {d.get("message")}')

# restore
call('PUT', '/api/accounts/2', token=UTOK, body={'firstName': '*'})
s, d2 = call('GET', '/api/accounts/2', token=UTOK)
check(F, "'*' convention keeps original value", d2.get('customer', {}).get('firstName') == 'TESTFIRST', f"fname={d2.get('customer',{}).get('firstName')}")

# ---------------- CARD LIST/VIEW/UPDATE (COCRDLIC/COCRDSLC/COCRDUPC) ----------------
F = 'Cards'
s, d = call('GET', '/api/cards?page=0', token=UTOK)
check(F, 'card list page size 7 (COBOL)', s == 200 and len(d.get('cards', [])) == 7, f'count={len(d.get("cards",[]))}')
check(F, 'card list has next page', d.get('hasNextPage') is True)
s, d = call('GET', '/api/cards?page=1', token=UTOK)
check(F, 'card list page 2 prev flag', s == 200 and d.get('hasPrevPage') is True, f'page1={len(d.get("cards",[]))}')

s, d = call('GET', '/api/cards?acctId=123', token=UTOK)
check(F, 'acct filter must be 11 digits', s == 400 and '11 DIGIT' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('GET', '/api/cards?cardNum=1234', token=UTOK)
check(F, 'card filter must be 16 digits', s == 400 and '16 DIGIT' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('GET', '/api/cards?acctId=00000000001', token=UTOK)
check(F, '11-digit acct filter narrows list', s == 200 and all(c['acctId'] == 1 for c in d.get('cards', [])) and len(d['cards']) >= 1, f'count={len(d.get("cards",[]))}')

s, d = call('GET', '/api/cards/view?cardNum=0500024453765740', token=UTOK)
CARD1 = '0500024453765740'
check(F, 'card view returns card+acct+customer', s == 200 and d.get('card', {}).get('cardNum') == CARD1 and d.get('account', {}).get('acctId') == 50, f'code={s}')

s, d = call('GET', '/api/cards/view?cardNum=9999999999999999', token=UTOK)
check(F, 'unknown card → error', s in (400, 404), f'{s}: {d.get("message")}')

s, d = call('PUT', f'/api/cards/{CARD1}', token=UTOK, body={'expMonth': '13'})
check(F, 'expiry month >12 rejected', s == 400 and 'between 1 and 12' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('PUT', f'/api/cards/{CARD1}', token=UTOK, body={'expYear': '1900'})
check(F, 'expiry year out of range rejected', s == 400 and 'year' in d.get('message', '').lower(), f'{s}: {d.get("message")}')

s, d = call('PUT', f'/api/cards/{CARD1}', token=UTOK, body={'activeStatus': 'Q'})
check(F, 'card status must be Y/N', s == 400, f'{s}: {d.get("message")}')

s, d = call('PUT', f'/api/cards/{CARD1}', token=UTOK, body={'embossedName': 'TEST CARDHOLDER', 'activeStatus': 'Y', 'expMonth': '12', 'expYear': '2030'})
check(F, 'valid card update works', s == 200 and d.get('card', {}).get('embossedName') == 'TEST CARDHOLDER', f'{s}: {d.get("message")}')
s, d2 = call('GET', f'/api/cards/view?cardNum={CARD1}', token=UTOK)
check(F, 'card update persisted (expiry)', d2.get('card', {}).get('expirationDate', '').startswith('2030-12'), f'exp={d2.get("card",{}).get("expirationDate")}')

# ---------------- TRANSACTION LIST/VIEW (COTRN00C/COTRN01C) ----------------
F = 'Transactions'
s, d = call('GET', '/api/transactions?page=0', token=UTOK)
txs = d.get('transactions', [])
check(F, 'transaction list page size 10', s == 200 and len(txs) == 10, f'count={len(txs)}')
check(F, 'transaction list sorted by id', [t['tranId'] for t in txs] == sorted(t['tranId'] for t in txs))
TRAN1 = txs[0]['tranId']
s, d2 = call('GET', f'/api/transactions?startId={txs[5]["tranId"]}&page=0', token=UTOK)
check(F, 'browse from start id (COBOL STARTBR)', s == 200 and d2['transactions'][0]['tranId'] == txs[5]['tranId'], str(d2.get('transactions', [{}])[0].get('tranId')))

s, d = call('GET', f'/api/transactions/{TRAN1}', token=UTOK)
check(F, 'transaction view detail', s == 200 and d.get('tranId') == TRAN1 and 'merchantName' in d, f'code={s}')

s, d = call('GET', '/api/transactions/Z999999999999999', token=UTOK)
check(F, 'transaction not found msg', s in (400, 404) and 'NOT found' in d.get('message', ''), f'{s}: {d.get("message")}')

# ---------------- TRANSACTION ADD (COTRN02C + POSTTRAN) ----------------
F = 'Transaction Add'
s, d = call('GET', '/api/accounts/3', token=UTOK)
acct3_bal = float(d['account']['currBal'])
acct3_card = d['cards'][0]['cardNum']
acct3_exp = d['account']['expirationDate']
VALID_TRAN = {'cardNum': acct3_card, 'typeCd': '01', 'catCd': '1', 'source': 'POS TERM',
              'amt': '25.50', 'origDate': acct3_exp, 'procDate': acct3_exp,
              'merchantId': '123456789', 'merchantName': 'TEST MERCHANT',
              'merchantCity': 'CITY', 'merchantZip': '12345', 'desc': 'FUNC TEST TRAN'}

s, d = call('POST', '/api/transactions', token=UTOK, body={**VALID_TRAN, 'confirm': ''})
check(F, 'add: confirm gate first (requiresConfirmation)', s == 200 and d.get('requiresConfirmation') is True, f'{s}: {d}')

s, d = call('POST', '/api/transactions', token=UTOK, body={})
check(F, 'add: missing fields → validation error', s == 400, f'{s}: {d.get("message")}')

s, d = call('POST', '/api/transactions', token=UTOK, body={
    'cardNum': '9999999999999999', 'typeCd': '01', 'catCd': '1', 'source': 'POS TERM',
    'amt': '10.00', 'origDate': '2026-10-01', 'procDate': '2026-10-01',
    'merchantId': '123456789', 'merchantName': 'M', 'merchantCity': 'C', 'merchantZip': '12345',
    'desc': 'T', 'confirm': 'Y'})
check(F, 'add: invalid card → error 100-class', s == 400 and 'card' in d.get('message', '').lower(), f'{s}: {d.get("message")}')

s, d = call('GET', '/api/accounts/3', token=UTOK)
over_amt = float(d['account']['creditLimit']) + 99999
s, d = call('POST', '/api/transactions', token=UTOK, body={
    'cardNum': acct3_card, 'typeCd': '01', 'catCd': '1', 'source': 'POS TERM',
    'amt': str(over_amt), 'origDate': acct3_exp, 'procDate': acct3_exp,
    'merchantId': '123456789', 'merchantName': 'M', 'merchantCity': 'C', 'merchantZip': '12345',
    'desc': 'OVER', 'confirm': 'Y'})
check(F, 'add: OVERLIMIT rejected', s == 400 and 'OVERLIMIT' in d.get('message', '').upper(), f'{s}: {d.get("message")}')

s, d = call('POST', '/api/transactions', token=UTOK, body={**VALID_TRAN, 'confirm': 'Y'})
NEWTRAN = d.get('transaction', {}).get('tranId')
check(F, 'add: valid post returns new tran id', s == 200 and NEWTRAN and len(NEWTRAN) == 16, f'tran={NEWTRAN}')

s, d = call('GET', '/api/accounts/3', token=UTOK)
new_bal = float(d['account']['currBal'])
check(F, 'posting updated account balance (+25.50)', abs(new_bal - (acct3_bal + 25.50)) < 0.01, f'{acct3_bal}→{new_bal}')

s, d = call('GET', f'/api/transactions/{NEWTRAN}', token=UTOK)
check(F, 'posted tran retrievable w/ fields', s == 200 and d.get('cardNum') == acct3_card and float(d.get('amt', 0)) == 25.50, f'code={s}')

# ---------------- REPORTS (CORPT00C) ----------------
F = 'Reports'
s, d = call('GET', '/api/reports/transactions?type=monthly', token=UTOK)
check(F, 'monthly report runs', s == 200 and d.get('reportName', '').lower().startswith('month'), f'{s}: {d.get("reportName")}')
s, d = call('GET', '/api/reports/transactions?type=yearly', token=UTOK)
check(F, 'yearly report: Jan1→Dec31', s == 200 and d.get('startDate', '').endswith('-01-01') and d.get('endDate', '').endswith('-12-31'), f'{d.get("startDate")}→{d.get("endDate")}')
s, d = call('GET', '/api/reports/transactions?type=custom&startDate=2020-01-01&endDate=2027-12-31', token=UTOK)
cnt_custom = d.get('count', 0)
check(F, 'custom report includes posted tran + seed', s == 200 and cnt_custom >= 300 and any(t['tranId'] == NEWTRAN for t in d.get('transactions', [])), f'count={cnt_custom}')
s, d = call('GET', '/api/reports/transactions?type=custom&startDate=bad&endDate=2026-12-31', token=UTOK)
check(F, 'custom report bad date rejected', s == 400, f'{s}: {d.get("message")}')
s, d = call('GET', '/api/reports/transactions?type=custom&startDate=2026-12-31&endDate=2026-01-01', token=UTOK)
check(F, 'start>end range rejected', s == 400, f'{s}: {d.get("message")}')

# ---------------- BILL PAYMENT (COBIL00C) ----------------
F = 'Bill Payment'
s, d = call('GET', '/api/accounts/4/bill-payment', token=UTOK)
bal4 = float(d.get('currentBalance', 0))
check(F, 'bill pay view returns balance', s == 200 and bal4 >= 0, f'bal={bal4}')

s, d = call('POST', '/api/accounts/4/bill-payment', token=UTOK, body={'confirm': 'X'})
check(F, 'invalid confirm → Y/N error', s == 400 and 'Y/N' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('POST', '/api/accounts/4/bill-payment', token=UTOK, body={'confirm': 'N'})
check(F, 'confirm N → payment cancelled (COBOL exits screen)', s == 200 and d.get('cancelled') is True and 'cancel' in d.get('message', '').lower(), f'{s}: {d.get("message")}')

s, d = call('POST', '/api/accounts/4/bill-payment', token=UTOK, body={'confirm': 'Y'})
check(F, 'confirm Y → payment posted, bal→0', s == 200 and float(d.get('newBalance', -1)) == 0.0, f'{s}: {d}')

s, d = call('GET', '/api/accounts/4', token=UTOK)
check(F, 'account 4 balance now 0.00', abs(float(d['account']['currBal'])) < 0.001, str(d['account']['currBal']))

s, d = call('POST', '/api/accounts/4/bill-payment', token=UTOK, body={'confirm': 'Y'})
check(F, 'zero balance → nothing to pay (COBOL err msg)', s == 400 and 'nothing to pay' in d.get('message', '').lower(), f'{s}: {d.get("message")}')

# ---------------- ADMIN: USER CRUD (COUSR00-03C) ----------------
F = 'Admin Users'
s, d = call('GET', '/api/users?page=0', token=ATOK)
check(F, 'admin user list (10/page)', s == 200 and len(d.get('users', [])) == 10, f'count={len(d.get("users",[]))}')

s, d = call('GET', '/api/users?page=0', token=UTOK)
check(F, 'non-admin → 403 admin only', s == 403 and 'Admin' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('POST', '/api/users', token=ATOK, body={'userId': 'TESTU01', 'firstName': 'TEST', 'lastName': 'USER', 'password': 'PASS1234', 'userType': 'U'})
check(F, 'add user works', s == 200 and 'added' in d.get('message', '').lower(), f'{s}: {d.get("message")}')

s, d = call('POST', '/api/users', token=ATOK, body={'userId': 'TESTU01', 'firstName': 'TEST', 'lastName': 'USER', 'password': 'PASS1234', 'userType': 'U'})
check(F, 'dup user id rejected', s == 400 and 'already exist' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('POST', '/api/users', token=ATOK, body={'userId': 'TESTU02', 'firstName': '', 'lastName': 'USER', 'password': 'PASS1234', 'userType': 'U'})
check(F, 'empty first name rejected', s == 400 and 'First Name' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('POST', '/api/users', token=ATOK, body={'userId': 'TESTU03', 'firstName': 'A', 'lastName': 'B', 'password': 'P', 'userType': 'X'})
check(F, 'user type must be A or U', s == 400 and 'A or U' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('GET', '/api/users/TESTU01', token=ATOK)
check(F, 'user view hides password', s == 200 and d.get('userId') == 'TESTU01' and 'password' not in d, f'keys={list(d)}')

s, d = call('PUT', '/api/users/TESTU01', token=ATOK, body={'firstName': 'UPDATED'})
check(F, 'update user works', s == 200 and d.get('user', {}).get('firstName') == 'UPDATED', f'{s}: {d.get("message")}')

s, d = call('DELETE', '/api/users/ADMIN001', token=ATOK)
check(F, 'self-delete blocked', s == 400 and 'currently signed on' in d.get('message', ''), f'{s}: {d.get("message")}')

s, d = call('DELETE', '/api/users/TESTU01', token=ATOK)
check(F, 'delete user works', s == 200 and 'deleted' in d.get('message', '').lower(), f'{s}: {d.get("message")}')

s, d = login('TESTU01', 'PASS1234')
check(F, 'deleted user cannot sign on', s == 401, f'code={s}')

# ---------------- TRAN TYPES & CATEGORIES (Db2 modules) ----------------
F = 'Tran Types'
s, d = call('GET', '/api/tran-types', token=ATOK)
check(F, 'tran type list ≥7', s == 200 and len(d) >= 7, f'count={len(d)}')
s, d = call('GET', '/api/tran-categories?typeCd=01', token=UTOK)
check(F, 'tran categories for type 01', s == 200 and len(d) >= 1, f'count={len(d)}')
s, d = call('POST', '/api/tran-types', token=ATOK, body={'typeCd': '99', 'desc': 'TEST TYPE'})
check(F, 'add tran type', s == 200, f'{s}: {d.get("message")}')
s, d = call('POST', '/api/tran-types', token=ATOK, body={'typeCd': '99', 'desc': 'TEST TYPE'})
check(F, 'dup tran type rejected', s == 400 and 'already exist' in d.get('message', ''), f'{s}: {d.get("message")}')
s, d = call('POST', '/api/tran-types', token=ATOK, body={'typeCd': '123', 'desc': 'BAD'})
check(F, 'type cd must be 2 digits', s == 400 and '2 digit' in d.get('message', ''), f'{s}: {d.get("message")}')
s, d = call('PUT', '/api/tran-types/99', token=ATOK, body={'desc': 'UPDATED TYPE'})
check(F, 'update tran type', s == 200 and d.get('desc') == 'UPDATED TYPE', f'{s}: {d}')
s, d = call('PUT', '/api/tran-types/XX', token=ATOK, body={'desc': 'NOPE'})
check(F, 'tran type not found → error', s in (400, 404) and 'NOT found' in d.get('message', ''), f'{s}: {d.get("message")}')
s, d = call('DELETE', '/api/tran-types/99', token=ATOK)
check(F, 'delete tran type', s == 200, f'{s}: {d.get("message")}')

# ---------------- BATCH: INTEREST & STATEMENT (CBACT04C) ----------------
F = 'Batch'
s, d = call('POST', '/api/batch/interest', token=ATOK, body={})
# seed tran-cat-bal balances are all zero → interest amounts are 0 by design (CBACT04C faithful)
check(F, 'interest calc processes all tcatbal recs', s == 200 and d.get('recordsProcessed', 0) == 50 and len(d.get('details', [])) == 50, f'{s}: {str(d)[:150]}')
INT_TRAN = d.get('details', [{}])[0].get('tranId')
s, d = call('GET', f'/api/transactions/{INT_TRAN}', token=UTOK)
check(F, 'interest tran posted (type 01 cat 5, source System)', s == 200 and d.get('catCd') == 5 and d.get('typeCd') == '01' and d.get('source') == 'System', f'{s}: {d}')
s, d = call('POST', '/api/batch/interest', token=UTOK, body={})
check(F, 'interest calc is admin-only', s == 403, f'code={s}')
s, d = call('POST', '/api/batch/statement/1', token=ATOK, body={'startDate': '2020-01-01', 'endDate': '2027-12-31'})
stmt = d.get('statement', '')
check(F, 'statement generated w/ header+totals', s == 200 and 'STATEMENT' in stmt.upper() and 'Total for period' in stmt and 'Current Balance' in stmt and d.get('transactionCount', 0) > 0, f'len={len(stmt)} count={d.get("transactionCount")}')
s, d = call('POST', '/api/batch/statement/999999', token=ATOK, body={'startDate': '2026-01-01', 'endDate': '2026-12-31'})
check(F, 'statement bad acct → error', s in (400, 404), f'{s}: {d.get("message")}')

# ---------------- AUTHORIZATIONS (optional IMS/DB2 module) ----------------
F = 'Authorizations'
s, d = call('GET', '/api/authorizations?page=0', token=UTOK)
summaries = d.get('summaries', [])
check(F, 'auth summary grouped by account', s == 200 and len(summaries) >= 1 and 'approvedAuthCount' in summaries[0], f'accts={len(summaries)}')
ACCT = summaries[0]['acctId']
s, d = call('GET', f'/api/authorizations?acctId={ACCT}', token=UTOK)
rows = d.get('authorizations', [])
check(F, 'auth detail rows for account', s == 200 and len(rows) >= 1, f'rows={len(rows)}')
if rows:
    r0 = rows[0]
    s, d = call('POST', f"/api/authorizations/{r0['cardNum']}/{urllib.parse.quote(str(r0['authTs']))}/match", token=UTOK, body={})
    check(F, 'match authorization → M', s == 200, f'{s}: {d.get("message")}')
    s, d = call('POST', f"/api/authorizations/{r0['cardNum']}/{urllib.parse.quote(str(r0['authTs']))}/match", token=UTOK, body={})
    check(F, 'dup match rejected', s == 400, f'{s}: {d.get("message")}')
    s, d = call('POST', f"/api/authorizations/{r0['cardNum']}/{urllib.parse.quote(str(r0['authTs']))}/fraud", token=UTOK, body={'fraud': 'Y'})
    check(F, 'mark fraud Y', s == 200, f'{s}: {d.get("message")}')
s, d = call('POST', '/api/batch/purge-authorizations?days=36500', token=ATOK)
check(F, 'purge authorizations job', s == 200, f'{s}: {d}')

# ---------------- LOGOUT ----------------
F = 'Auth'
s, d = call('POST', '/api/auth/logout', token=UTOK)
check(F, 'logout ok', s == 200, f'code={s}')
s, d = call('GET', '/api/menus', token=UTOK)
check(F, 'token dead after logout', s == 401, f'code={s}')

# ---------------- SCORE ----------------
total = len(results)
passed = sum(1 for r in results if r[2])
failed = total - passed
score = round(100.0 * passed / total, 1)

print(f'\n{"="*72}\nFUNCTIONAL TEST RESULTS  base={BASE}\n{"="*72}')
cur = None
for feat, name, ok, detail in results:
    if feat != cur:
        print(f'\n--- {feat} ---')
        cur = feat
    mark = 'PASS' if ok else 'FAIL'
    print(f'[{mark}] {name}' + (f'   ({detail})' if detail and not ok else ''))
print(f'\n{"="*72}')
print(f'TOTAL: {total}  PASSED: {passed}  FAILED: {failed}  SCORE: {score}%')
print(f'{"="*72}')
with open(RESULTS_FILE, 'w') as fp:
    json.dump({'base': BASE, 'total': total, 'passed': passed, 'failed': failed, 'score': score,
               'results': [{'feature': f, 'name': n, 'passed': p, 'detail': dt} for f, n, p, dt in results]}, fp, indent=2)
sys.exit(0 if failed == 0 else 1)
