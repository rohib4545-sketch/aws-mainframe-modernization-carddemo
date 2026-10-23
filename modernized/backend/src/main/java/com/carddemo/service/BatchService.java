package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.*;
import com.carddemo.repo.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static com.carddemo.service.TransactionService.nz;

/** Batch jobs: INTCALC (CBACT04C interest), CREASTMT (statements), CBPAUP0J (purge auths). */
@Service
public class BatchService {
    private final TranCatBalRepository catBals;
    private final AccountRepository accounts;
    private final CardXrefRepository xrefs;
    private final DisclosureGroupRepository disGroups;
    private final TranRecordRepository trans;
    private final CustomerRepository customers;
    private final PendingAuthRepository auths;
    private final TransactionService tranService;

    public BatchService(TranCatBalRepository catBals, AccountRepository accounts,
                        CardXrefRepository xrefs, DisclosureGroupRepository disGroups,
                        TranRecordRepository trans, CustomerRepository customers,
                        PendingAuthRepository auths, TransactionService tranService) {
        this.catBals = catBals; this.accounts = accounts; this.xrefs = xrefs;
        this.disGroups = disGroups; this.trans = trans; this.customers = customers;
        this.auths = auths; this.tranService = tranService;
    }

    /** CBACT04C: monthly interest = TRAN-CAT-BAL * rate / 1200 on every category balance;
     *  writes a type-01/cat-0005 "Int. for a/c N" transaction per balance and updates the account. */
    @Transactional
    public Map<String, Object> interestCalculation() {
        List<TranCatBal> all = catBals.findAll(
                Sort.by("acctId").and(Sort.by("typeCd")).and(Sort.by("catCd")));
        int processed = 0, intTrans = 0;
        BigDecimal grandTotal = BigDecimal.ZERO;
        Long lastAcct = null;
        BigDecimal acctInt = BigDecimal.ZERO;
        Account acct = null;
        String cardNum = null;

        List<Map<String, Object>> details = new ArrayList<>();
        for (TranCatBal tb : all) {
            if (lastAcct == null || !tb.getAcctId().equals(lastAcct)) {
                if (acct != null) {
                    acct.setCurrBal(nz(acct.getCurrBal()).add(acctInt));
                    acct.setCurrCycCredit(BigDecimal.ZERO);
                    acct.setCurrCycDebit(BigDecimal.ZERO);
                    accounts.save(acct);
                }
                acctInt = BigDecimal.ZERO;
                lastAcct = tb.getAcctId();
                acct = accounts.findById(lastAcct).orElse(null);
                cardNum = xrefs.findFirstByAcctId(lastAcct).map(CardXref::getCardNum).orElse("");
            }
            if (acct == null) continue;
            processed++;

            DisclosureGroup dg = disGroups.findById(
                    new DisclosureGroupId(nzs(acct.getGroupId()), tb.getTypeCd(), tb.getCatCd()))
                    .orElseGet(() -> disGroups.findById(
                            new DisclosureGroupId("DEFAULT", tb.getTypeCd(), tb.getCatCd())).orElse(null));
            BigDecimal rate = dg == null ? BigDecimal.ZERO : nz(dg.getIntRate());
            if (rate.signum() == 0) continue;

            BigDecimal monthlyInt = nz(tb.getBal()).multiply(rate)
                    .divide(new BigDecimal(1200), 2, RoundingMode.HALF_UP);
            acctInt = acctInt.add(monthlyInt);
            grandTotal = grandTotal.add(monthlyInt);

            TranRecord t = new TranRecord();
            t.setTranId(tranService.nextTranId());
            t.setTypeCd("01");
            t.setCatCd(5);
            t.setSource("System");
            t.setDesc("Int. for a/c " + acct.getAcctId());
            t.setAmt(monthlyInt);
            t.setMerchantId(0L);
            t.setCardNum(cardNum);
            String now = ValidationUtil.now();
            t.setOrigTs(now);
            t.setProcTs(now);
            trans.save(t);
            intTrans++;
            details.add(Map.of("acctId", acct.getAcctId(), "tranId", t.getTranId(), "amt", monthlyInt));
        }
        if (acct != null) {
            acct.setCurrBal(nz(acct.getCurrBal()).add(acctInt));
            acct.setCurrCycCredit(BigDecimal.ZERO);
            acct.setCurrCycDebit(BigDecimal.ZERO);
            accounts.save(acct);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("recordsProcessed", processed);
        out.put("interestTransactions", intTrans);
        out.put("totalInterest", grandTotal);
        out.put("details", details);
        out.put("message", "END OF EXECUTION OF PROGRAM CBACT04C");
        return out;
    }

    private String nzs(String s) { return s == null ? "" : s; }

    /** CREASTMT/CBSTM03A: produce a statement for an account for a date range (default current month). */
    public Map<String, Object> statement(Long acctId, String startDate, String endDate) {
        Account a = accounts.findById(acctId)
                .orElseThrow(() -> ApiException.notFound("Account ID NOT found..."));
        Customer c = xrefs.findFirstByAcctId(acctId)
                .flatMap(x -> customers.findById(x.getCustId())).orElse(null);

        LocalDate today = LocalDate.now();
        String start = ValidationUtil.isBlank(startDate) ? today.withDayOfMonth(1).toString() : startDate;
        String end = ValidationUtil.isBlank(endDate) ? today.toString() : endDate;
        if (!ValidationUtil.isValidDate(start)) throw ApiException.badRequest("Start Date - Not a valid date");
        if (!ValidationUtil.isValidDate(end)) throw ApiException.badRequest("End Date - Not a valid date");

        Set<String> acctCards = new HashSet<>();
        xrefs.findByAcctId(acctId).forEach(x -> acctCards.add(x.getCardNum()));
        List<TranRecord> rows = trans.findInDateRange(start, end + " 23:59:59.999999").stream()
                .filter(t -> acctCards.contains(t.getCardNum())).toList();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(72)).append('\n');
        sb.append(center("CardDemo - Monthly Statement")).append('\n');
        sb.append("=".repeat(72)).append('\n');
        sb.append(String.format("Account ID : %011d%n", a.getAcctId()));
        if (c != null) {
            sb.append(String.format("Name       : %s %s%n", c.getFirstName(), c.getLastName()));
            sb.append(String.format("Address    : %s, %s, %s %s%n",
                    c.getAddrLine1(), c.getAddrLine3(), c.getAddrStateCd(), c.getAddrZip()));
        }
        sb.append(String.format("Period     : %s to %s%n", start, end));
        sb.append("-".repeat(72)).append('\n');
        sb.append(String.format("%-16s %-10s %-40s %12s%n", "Tran ID", "Date", "Description", "Amount"));
        sb.append("-".repeat(72)).append('\n');
        BigDecimal total = BigDecimal.ZERO;
        for (TranRecord t : rows) {
            total = total.add(nz(t.getAmt()));
            sb.append(String.format("%-16s %-10s %-40.40s %12s%n",
                    t.getTranId(), t.getOrigTs() == null ? "" : t.getOrigTs().substring(0, Math.min(10, t.getOrigTs().length())),
                    nzs(t.getDesc()), t.getAmt().toPlainString()));
        }
        sb.append("-".repeat(72)).append('\n');
        sb.append(String.format("%-57s %12s%n", "Total for period", total.toPlainString()));
        sb.append(String.format("%-57s %12s%n", "Current Balance", nz(a.getCurrBal()).toPlainString()));
        sb.append("=".repeat(72)).append('\n');

        Map<String, Object> out = new HashMap<>();
        out.put("accountId", acctId);
        out.put("startDate", start);
        out.put("endDate", end);
        out.put("transactionCount", rows.size());
        out.put("totalAmount", total);
        out.put("statement", sb.toString());
        return out;
    }

    /** CBPAUP0J: purge pending authorizations older than the given number of days. */
    @Transactional
    public Map<String, Object> purgeAuthorizations(int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        List<PendingAuth> all = auths.findAll();
        List<PendingAuth> expired = all.stream()
                .filter(a -> a.getAuthTs() != null && a.getAuthTs().length() >= 10
                        && a.getAuthTs().substring(0, 10).compareTo(cutoff.toString()) < 0)
                .toList();
        auths.deleteAll(expired);
        Map<String, Object> out = new HashMap<>();
        out.put("purged", expired.size());
        out.put("cutoffDate", cutoff.toString());
        out.put("message", "Purged " + expired.size() + " expired authorizations");
        return out;
    }

    private String center(String s) {
        int pad = Math.max(0, (72 - s.length()) / 2);
        return " ".repeat(pad) + s;
    }
}
