#!/usr/bin/env python3
"""Convert CardDemo fixed-width ASCII data files into JSON seed files.

Handles COBOL zoned-decimal signed numerics: last digit encodes sign
({ = +0, } = -0, A-I = +1..+9, J-R = -1..-9).
"""
import json, os, sys

DATA = os.path.join(os.path.dirname(__file__), '..', '..', '..', '..', '..', '..', 'app', 'data', 'ASCII')
OUT = os.path.dirname(__file__)

POS = {'{': 0, '}': 0}
for i, c in enumerate('ABCDEFGHI'):
    POS[c] = i + 1
for i, c in enumerate('JKLMNOPQR'):
    POS[c] = i + 1


def znum(s, decimals=2):
    """Parse zoned decimal signed field into a float."""
    s = s.strip()
    if not s:
        return 0.0
    sign = 1
    last = s[-1]
    if last in POS:
        digit = POS[last]
        if last == '}' or last in 'JKLMNOPQR':
            sign = -1
        s = s[:-1] + str(digit)
    try:
        v = int(s)
    except ValueError:
        v = 0
    return round(sign * v / (10 ** decimals), 2)


def num(s):
    s = s.strip()
    return int(s) if s.isdigit() else 0


def txt(s):
    return s.rstrip()


def parse(path, spec):
    rows = []
    with open(path, 'r', encoding='ascii', errors='replace') as f:
        for line in f:
            line = line.rstrip('\n')
            if not line.strip():
                continue
            row = {}
            for name, (a, b), kind in spec:
                row[name] = kind(line[a:b])
            rows.append(row)
    return rows


N = num
T = txt


def Z(a, b):
    return znum


specs = {
    'accounts': ('acctdata.txt', [
        ('acctId', (0, 11), N), ('activeStatus', (11, 12), T),
        ('currBal', (12, 24), Z(12, 24)), ('creditLimit', (24, 36), Z(24, 36)),
        ('cashCreditLimit', (36, 48), Z(36, 48)), ('openDate', (48, 58), T),
        ('expirationDate', (58, 68), T), ('reissueDate', (68, 78), T),
        ('currCycCredit', (78, 90), Z(78, 90)), ('currCycDebit', (90, 102), Z(90, 102)),
        ('groupId', (102, 112), T), ('addrZip', (112, 122), T)]),
    'cards': ('carddata.txt', [
        ('cardNum', (0, 16), T), ('acctId', (16, 27), N),
        ('cvvCode', (27, 30), N), ('embossedName', (30, 80), T),
        ('expirationDate', (80, 90), T), ('activeStatus', (90, 91), T)]),
    'cardxrefs': ('cardxref.txt', [
        ('cardNum', (0, 16), T), ('custId', (16, 25), N), ('acctId', (25, 36), N)]),
    'customers': ('custdata.txt', [
        ('custId', (0, 9), N), ('firstName', (9, 34), T), ('middleName', (34, 59), T),
        ('lastName', (59, 84), T), ('addrLine1', (84, 134), T),
        ('addrLine2', (134, 184), T), ('addrLine3', (184, 234), T),
        ('addrStateCd', (234, 236), T), ('addrCountryCd', (236, 239), T),
        ('addrZip', (239, 249), T), ('phoneNum1', (249, 264), T),
        ('phoneNum2', (264, 279), T), ('ssn', (279, 288), N),
        ('govtIssuedId', (288, 308), T), ('dob', (308, 318), T),
        ('eftAccountId', (318, 328), T), ('priCardHolderInd', (328, 329), T),
        ('ficoCreditScore', (329, 332), N)]),
    'transactions': ('dailytran.txt', [
        ('tranId', (0, 16), T), ('typeCd', (16, 18), T), ('catCd', (18, 22), N),
        ('source', (22, 32), T), ('desc', (32, 132), T), ('amt', (132, 143), Z(132, 143)),
        ('merchantId', (143, 152), N), ('merchantName', (152, 202), T),
        ('merchantCity', (202, 252), T), ('merchantZip', (252, 262), T),
        ('cardNum', (262, 278), T), ('origTs', (278, 304), T), ('procTs', (304, 330), T)]),
    'disclosureGroups': ('discgrp.txt', [
        ('acctGroupId', (0, 10), T), ('tranTypeCd', (10, 12), T),
        ('tranCatCd', (12, 16), N), ('intRate', (16, 22), Z(16, 22))]),
    'tranCatBals': ('tcatbal.txt', [
        ('acctId', (0, 11), N), ('typeCd', (11, 13), T), ('catCd', (13, 17), N),
        ('bal', (17, 28), Z(17, 28))]),
    'tranCategories': ('trancatg.txt', [
        ('typeCd', (0, 2), T), ('catCd', (2, 6), N), ('desc', (6, 56), T)]),
    'tranTypes': ('trantype.txt', [
        ('typeCd', (0, 2), T), ('desc', (2, 52), T)]),
}

USRSEC = """ADMIN001MARGARET            GOLD                PASSWORDA
ADMIN002RUSSELL             RUSSELL             PASSWORDA
ADMIN003RAYMOND             WHITMORE            PASSWORDA
ADMIN004EMMANUEL            CASGRAIN            PASSWORDA
ADMIN005GRANVILLE           LACHAPELLE          PASSWORDA
USER0001LAWRENCE            THOMAS              PASSWORDU
USER0002AJITH               KUMAR               PASSWORDU
USER0003LAURITZ             ALME                PASSWORDU
USER0004AVERARDO            MAZZI               PASSWORDU
USER0005LEE                 TING                PASSWORDU"""

os.makedirs(OUT, exist_ok=True)
for name, (fname, spec) in specs.items():
    rows = parse(os.path.join(DATA, fname), spec)
    with open(os.path.join(OUT, name + '.json'), 'w') as f:
        json.dump(rows, f, indent=1)
    print(name, len(rows))

users = []
for line in USRSEC.splitlines():
    users.append({'userId': line[0:8].strip(), 'firstName': line[8:28].strip(),
                  'lastName': line[28:48].strip(), 'password': line[48:56].strip(),
                  'userType': line[56:57].strip()})
with open(os.path.join(OUT, 'users.json'), 'w') as f:
    json.dump(users, f, indent=1)
print('users', len(users))
