package com.carddemo.config;

import com.carddemo.model.*;
import com.carddemo.repo.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Loads the VSAM/PS sample data (converted to JSON) at startup — replaces the
 *  ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE/TCATBALF/DISCGRP/TRANCATG/TRANTYPE/DUSRSECJ JCL jobs. */
@Component
public class DataLoader implements CommandLineRunner {
    private final UserSecurityRepository users;
    private final AccountRepository accounts;
    private final CardRepository cards;
    private final CardXrefRepository xrefs;
    private final CustomerRepository customers;
    private final TranRecordRepository trans;
    private final TranCatBalRepository catBals;
    private final DisclosureGroupRepository disGroups;
    private final TranTypeRepository tranTypes;
    private final TranCategoryRepository tranCats;
    private final PendingAuthRepository auths;
    private final ObjectMapper om = new ObjectMapper();

    public DataLoader(UserSecurityRepository users, AccountRepository accounts, CardRepository cards,
                      CardXrefRepository xrefs, CustomerRepository customers, TranRecordRepository trans,
                      TranCatBalRepository catBals, DisclosureGroupRepository disGroups,
                      TranTypeRepository tranTypes, TranCategoryRepository tranCats,
                      PendingAuthRepository auths) {
        this.users = users; this.accounts = accounts; this.cards = cards; this.xrefs = xrefs;
        this.customers = customers; this.trans = trans; this.catBals = catBals;
        this.disGroups = disGroups; this.tranTypes = tranTypes; this.tranCats = tranCats;
        this.auths = auths;
    }

    @Override
    public void run(String... args) throws Exception {
        users.saveAll(read("users.json", UserSecurity.class));
        accounts.saveAll(read("accounts.json", Account.class));
        cards.saveAll(read("cards.json", Card.class));
        xrefs.saveAll(read("cardxrefs.json", CardXref.class));
        customers.saveAll(read("customers.json", Customer.class));
        trans.saveAll(read("transactions.json", TranRecord.class));
        catBals.saveAll(read("tranCatBals.json", TranCatBal.class));
        disGroups.saveAll(read("disclosureGroups.json", DisclosureGroup.class));
        tranTypes.saveAll(read("tranTypes.json", TranType.class));
        tranCats.saveAll(read("tranCategories.json", TranCategory.class));
        auths.saveAll(seedPendingAuths());
    }

    private <T> List<T> read(String name, Class<T> type) throws Exception {
        var resource = new ClassPathResource("seed/" + name);
        List<Map<String, Object>> rows = om.readValue(resource.getInputStream(),
                new TypeReference<>() {});
        return rows.stream().map(m -> om.convertValue(m, type)).toList();
    }

    /** Sample pending authorizations standing in for the IMS DBPAUTP0 segment data. */
    private List<PendingAuth> seedPendingAuths() {
        List<CardXref> xr = xrefs.findAll();
        Object[][] spec = {
            // cardIdx, authType, respCode, respReason, amt, merchant, city, st, zip, match, fraud
            {0, "AUTH", "00", "0000", "125.50", "AMAZON.COM", "SEATTLE", "WA", "98109", " ", "N"},
            {1, "AUTH", "00", "0000", "89.99", "DELTA AIR LINES", "ATLANTA", "GA", "30320", " ", "N"},
            {2, "AUTH", "05", "0051", "1500.00", "BEST BUY", "MINNEAPOLIS", "MN", "55423", " ", "N"},
            {3, "AUTH", "00", "0000", "42.18", "SHELL OIL", "HOUSTON", "TX", "77002", " ", "N"},
            {4, "ATST", "00", "0000", "0.00", "HOTEL VERIFY", "NEW YORK", "NY", "10001", "M", "N"},
            {5, "AUTH", "00", "0000", "210.75", "WALMART", "BENTONVILLE", "AR", "72712", " ", "N"},
            {6, "AUTH", "14", "0054", "99.00", "UNKNOWN MERCH", "MIAMI", "FL", "33101", " ", "Y"},
            {7, "AUTH", "00", "0000", "67.30", "STARBUCKS", "SEATTLE", "WA", "98101", "M", "N"},
        };
        java.util.List<PendingAuth> list = new java.util.ArrayList<>();
        int i = 0;
        for (Object[] s : spec) {
            CardXref x = xr.get((Integer) s[0] % xr.size());
            PendingAuth a = new PendingAuth();
            a.setCardNum(x.getCardNum());
            a.setAcctId(x.getAcctId());
            a.setCustId(x.getCustId());
            a.setAuthTs(String.format("2024-06-%02d %02d:15:30.000000", 10 + i, 9 + i));
            a.setAuthType((String) s[1]);
            a.setCardExpiryDate("1226");
            a.setMessageType("0200");
            a.setMessageSource("POS");
            a.setAuthIdCode(String.format("%06d", 100000 + i));
            a.setAuthRespCode((String) s[2]);
            a.setAuthRespReason((String) s[3]);
            a.setProcessingCode("000000");
            BigDecimal amt = new BigDecimal((String) s[4]);
            a.setTransactionAmt(amt);
            a.setApprovedAmt("00".equals(s[2]) ? amt : BigDecimal.ZERO);
            a.setMerchantCategoryCode("5411");
            a.setAcqrCountryCode("840");
            a.setPosEntryMode(5);
            a.setMerchantId(String.format("%015d", 987654000 + i));
            a.setMerchantName((String) s[5]);
            a.setMerchantCity((String) s[6]);
            a.setMerchantState((String) s[7]);
            a.setMerchantZip((String) s[8]);
            a.setTransactionId(String.format("%015d", 777000 + i));
            a.setMatchStatus((String) s[9]);
            a.setAuthFraud((String) s[10]);
            a.setFraudRptDate("Y".equals(s[10]) ? "2024-06-10" : "");
            list.add(a);
            i++;
        }
        return list;
    }
}
