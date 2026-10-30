package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.*;
import com.carddemo.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** COACTVWC (Account View) + COACTUPC (Account Update). */
@Service
public class AccountService {
    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final CardXrefRepository xrefs;
    private final CardRepository cards;

    public AccountService(AccountRepository accounts, CustomerRepository customers,
                          CardXrefRepository xrefs, CardRepository cards) {
        this.accounts = accounts;
        this.customers = customers;
        this.xrefs = xrefs;
        this.cards = cards;
    }

    private Account find(Long acctId) {
        return accounts.findById(acctId)
                .orElseThrow(() -> ApiException.notFound("Did not find this account in account master file"));
    }

    /** Account View: account + xref customer + card summary. */
    public Map<String, Object> view(Long acctId) {
        Account a = find(acctId);
        List<CardXref> xr = xrefs.findByAcctId(acctId);
        Customer c = xr.isEmpty() ? null : customers.findById(xr.get(0).getCustId()).orElse(null);
        List<Card> acctCards = cards.findByAcctIdOrderByCardNum(acctId);
        Map<String, Object> out = new HashMap<>();
        out.put("account", a);
        out.put("customer", c);
        out.put("cards", acctCards);
        return out;
    }

    /** Resolve a lookup which may be an account id or a card number (COACTVWC accepts both). */
    public Long resolveAcctId(Long acctId, String cardNum) {
        if (acctId == null && ValidationUtil.isBlank(cardNum)) {
            throw ApiException.badRequest("Account ID can NOT be empty...");
        }
        if (acctId != null) return acctId;
        CardXref x = xrefs.findById(cardNum.trim())
                .orElseThrow(() -> ApiException.notFound("Did not find cards for this search condition"));
        return x.getAcctId();
    }

    /** Account Update: blank/'*'/absent fields keep old values (COBOL '*'-means-keep convention). */
    @Transactional
    public Map<String, Object> update(Long acctId, Map<String, Object> in) {
        Account a = find(acctId);
        List<CardXref> xr = xrefs.findByAcctId(acctId);
        Customer c = xr.isEmpty() ? null : customers.findById(xr.get(0).getCustId()).orElse(null);

        String v;
        if (supplied(in, "activeStatus")) {
            v = str(in, "activeStatus").toUpperCase();
            if (!ValidationUtil.isYesNo(v)) throw ApiException.badRequest("Account Active Status must be Y or N");
            a.setActiveStatus(v);
        }
        if (supplied(in, "openDate")) {
            v = str(in, "openDate");
            if (!ValidationUtil.isValidDate(v)) throw ApiException.badRequest("Open Date must be a valid date in YYYY-MM-DD format");
            a.setOpenDate(v);
        }
        if (supplied(in, "expirationDate")) {
            v = str(in, "expirationDate");
            if (!ValidationUtil.isValidDate(v)) throw ApiException.badRequest("Expiry Date must be a valid date in YYYY-MM-DD format");
            a.setExpirationDate(v);
        }
        if (supplied(in, "reissueDate")) {
            v = str(in, "reissueDate");
            if (!ValidationUtil.isValidDate(v)) throw ApiException.badRequest("Reissue Date must be a valid date in YYYY-MM-DD format");
            a.setReissueDate(v);
        }
        if (supplied(in, "creditLimit")) {
            v = str(in, "creditLimit");
            if (!ValidationUtil.isSignedDecimal(v)) throw ApiException.badRequest("Credit Limit must be a valid signed amount");
            a.setCreditLimit(ValidationUtil.parseDecimal(v));
        }
        if (supplied(in, "cashCreditLimit")) {
            v = str(in, "cashCreditLimit");
            if (!ValidationUtil.isSignedDecimal(v)) throw ApiException.badRequest("Cash Credit Limit must be a valid signed amount");
            a.setCashCreditLimit(ValidationUtil.parseDecimal(v));
        }
        if (supplied(in, "currBal")) {
            v = str(in, "currBal");
            if (!ValidationUtil.isSignedDecimal(v)) throw ApiException.badRequest("Current Balance must be a valid signed amount");
            a.setCurrBal(ValidationUtil.parseDecimal(v));
        }
        if (supplied(in, "currCycCredit")) {
            v = str(in, "currCycCredit");
            if (!ValidationUtil.isSignedDecimal(v)) throw ApiException.badRequest("Current Cycle Credit must be a valid signed amount");
            a.setCurrCycCredit(ValidationUtil.parseDecimal(v));
        }
        if (supplied(in, "currCycDebit")) {
            v = str(in, "currCycDebit");
            if (!ValidationUtil.isSignedDecimal(v)) throw ApiException.badRequest("Current Cycle Debit must be a valid signed amount");
            a.setCurrCycDebit(ValidationUtil.parseDecimal(v));
        }
        if (supplied(in, "groupId")) a.setGroupId(str(in, "groupId"));
        if (supplied(in, "addrZip")) {
            v = str(in, "addrZip");
            if (!v.isBlank() && !ValidationUtil.isValidZip(v)) throw ApiException.badRequest("Zip must be a valid US zip code");
            a.setAddrZip(v);
        }
        accounts.save(a);

        if (c != null) updateCustomer(c, in);

        Map<String, Object> out = new HashMap<>();
        out.put("message", "Update of record successful");
        out.put("account", a);
        out.put("customer", c);
        return out;
    }

    private void updateCustomer(Customer c, Map<String, Object> in) {
        String v;
        if (supplied(in, "firstName")) {
            v = str(in, "firstName");
            if (!ValidationUtil.isAlpha(v)) throw ApiException.badRequest("First Name can only contain alphabets");
            c.setFirstName(v);
        }
        if (supplied(in, "middleName")) {
            v = str(in, "middleName");
            if (!v.isBlank() && !ValidationUtil.isAlpha(v)) throw ApiException.badRequest("Middle Name can only contain alphabets");
            c.setMiddleName(v);
        }
        if (supplied(in, "lastName")) {
            v = str(in, "lastName");
            if (!ValidationUtil.isAlpha(v)) throw ApiException.badRequest("Last Name can only contain alphabets");
            c.setLastName(v);
        }
        if (supplied(in, "addrLine1")) {
            v = str(in, "addrLine1");
            if (v.isBlank()) throw ApiException.badRequest("Address Line 1 cannot be empty");
            c.setAddrLine1(v);
        }
        if (supplied(in, "addrLine2")) c.setAddrLine2(str(in, "addrLine2"));
        if (supplied(in, "addrLine3")) c.setAddrLine3(str(in, "addrLine3"));
        if (supplied(in, "addrStateCd")) {
            v = str(in, "addrStateCd").toUpperCase();
            if (!ValidationUtil.isValidState(v)) throw ApiException.badRequest("State is not a valid US state code");
            c.setAddrStateCd(v);
        }
        if (supplied(in, "addrCountryCd")) {
            v = str(in, "addrCountryCd").toUpperCase();
            if (v.length() != 3 || !ValidationUtil.isAlpha(v)) throw ApiException.badRequest("Country must be a valid 3 letter code");
            c.setAddrCountryCd(v);
        }
        if (supplied(in, "addrZipCust")) {
            v = str(in, "addrZipCust");
            if (!ValidationUtil.isValidZip(v)) throw ApiException.badRequest("Zip must be a valid US zip code");
            c.setAddrZip(v);
        }
        if (supplied(in, "phoneNum1")) {
            v = str(in, "phoneNum1");
            if (!ValidationUtil.isValidPhone(v)) throw ApiException.badRequest("Phone Number 1 must be a valid phone number");
            c.setPhoneNum1(v);
        }
        if (supplied(in, "phoneNum2")) {
            v = str(in, "phoneNum2");
            if (!v.isBlank() && !ValidationUtil.isValidPhone(v)) throw ApiException.badRequest("Phone Number 2 must be a valid phone number");
            c.setPhoneNum2(v);
        }
        if (supplied(in, "ssn")) {
            v = str(in, "ssn");
            if (!v.matches("\\d{9}")) throw ApiException.badRequest("SSN must be a 9 digit number");
            c.setSsn(Long.parseLong(v));
        }
        if (supplied(in, "govtIssuedId")) c.setGovtIssuedId(str(in, "govtIssuedId"));
        if (supplied(in, "dob")) {
            v = str(in, "dob");
            if (!ValidationUtil.isValidDate(v)) throw ApiException.badRequest("Date of Birth must be a valid date in YYYY-MM-DD format");
            c.setDob(v);
        }
        if (supplied(in, "eftAccountId")) {
            v = str(in, "eftAccountId");
            if (!v.matches("\\d{10}")) throw ApiException.badRequest("EFT Account Id must be a 10 digit number");
            c.setEftAccountId(v);
        }
        if (supplied(in, "priCardHolderInd")) {
            v = str(in, "priCardHolderInd").toUpperCase();
            if (!ValidationUtil.isYesNo(v)) throw ApiException.badRequest("Primary Card Holder must be Y or N");
            c.setPriCardHolderInd(v);
        }
        if (supplied(in, "ficoCreditScore")) {
            v = str(in, "ficoCreditScore");
            if (!v.matches("\\d{3}")) throw ApiException.badRequest("FICO Score must be a 3 digit number");
            c.setFicoCreditScore(Integer.parseInt(v));
        }
        customers.save(c);
    }

    static boolean supplied(Map<String, Object> in, String k) {
        return in.containsKey(k) && !ValidationUtil.isBlank(str(in, k));
    }

    static String str(Map<String, Object> in, String k) {
        Object o = in.get(k);
        return o == null ? "" : String.valueOf(o).trim();
    }
}
