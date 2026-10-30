package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.*;
import com.carddemo.repo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/** COTRN00C (list), COTRN01C (view), COTRN02C (add + POSTTRAN posting), CORPT00C (reports). */
@Service
public class TransactionService {
    private final TranRecordRepository trans;
    private final TranCatBalRepository catBals;
    private final CardXrefRepository xrefs;
    private final AccountRepository accounts;
    private final TranTypeRepository tranTypes;
    private final TranCategoryRepository tranCats;

    public TransactionService(TranRecordRepository trans, TranCatBalRepository catBals,
                              CardXrefRepository xrefs, AccountRepository accounts,
                              TranTypeRepository tranTypes, TranCategoryRepository tranCats) {
        this.trans = trans;
        this.catBals = catBals;
        this.xrefs = xrefs;
        this.accounts = accounts;
        this.tranTypes = tranTypes;
        this.tranCats = tranCats;
    }

    /** Browse transactions starting at tranId (or lowest), 10 per page. */
    public Map<String, Object> list(String startId, int page, int size) {
        String start = ValidationUtil.isBlank(startId) ? "" : startId.trim();
        List<TranRecord> rows = trans.browse(start, PageRequest.of(Math.max(page, 0), size));
        boolean more = !trans.browse(start, PageRequest.of(Math.max(page, 0) + 1, size)).isEmpty();
        Map<String, Object> out = new HashMap<>();
        out.put("transactions", rows);
        out.put("page", Math.max(page, 0));
        out.put("pageSize", size);
        out.put("hasNextPage", more);
        out.put("hasPrevPage", page > 0);
        return out;
    }

    public TranRecord view(String tranId) {
        if (ValidationUtil.isBlank(tranId)) throw ApiException.badRequest("Tran ID can NOT be empty...");
        return trans.findById(tranId.trim())
                .orElseThrow(() -> ApiException.notFound("Transaction ID NOT found..."));
    }

    /** COTRN02C input validation; if confirm != 'Y' returns a confirmation prompt (like the BMS confirm step). */
    @Transactional
    public Map<String, Object> add(Map<String, Object> in) {
        String cardNum = AccountService.str(in, "cardNum");
        String acctStr = AccountService.str(in, "acctId");
        String typeCd = AccountService.str(in, "typeCd");
        String catCd = AccountService.str(in, "catCd");
        String source = AccountService.str(in, "source");
        String desc = AccountService.str(in, "desc");
        String amtStr = AccountService.str(in, "amt");
        String merchantId = AccountService.str(in, "merchantId");
        String merchantName = AccountService.str(in, "merchantName");
        String merchantCity = AccountService.str(in, "merchantCity");
        String merchantZip = AccountService.str(in, "merchantZip");
        String origDate = AccountService.str(in, "origDate");
        String procDate = AccountService.str(in, "procDate");
        String confirm = AccountService.str(in, "confirm").toUpperCase();

        if (acctStr.isEmpty() && cardNum.isEmpty()) {
            throw ApiException.badRequest("Account or Card Number must be entered");
        }
        if (!acctStr.isEmpty() && !ValidationUtil.isNumeric(acctStr)) {
            throw ApiException.badRequest("Account ID must be Numeric");
        }
        if (!cardNum.isEmpty() && !ValidationUtil.isNumeric(cardNum)) {
            throw ApiException.badRequest("Card Number must be Numeric");
        }
        if (typeCd.isEmpty()) throw ApiException.badRequest("Type CD can NOT be empty");
        if (catCd.isEmpty()) throw ApiException.badRequest("Category CD can NOT be empty");
        if (source.isEmpty()) throw ApiException.badRequest("Source can NOT be empty");
        if (desc.isEmpty()) throw ApiException.badRequest("Description can NOT be empty");
        if (amtStr.isEmpty()) throw ApiException.badRequest("Amount can NOT be empty");
        if (origDate.isEmpty()) throw ApiException.badRequest("Orig Date can NOT be empty");
        if (procDate.isEmpty()) throw ApiException.badRequest("Proc Date can NOT be empty");
        if (merchantId.isEmpty()) throw ApiException.badRequest("Merchant ID can NOT be empty");
        if (merchantName.isEmpty()) throw ApiException.badRequest("Merchant Name can NOT be empty");
        if (merchantCity.isEmpty()) throw ApiException.badRequest("Merchant City can NOT be empty");
        if (merchantZip.isEmpty()) throw ApiException.badRequest("Merchant Zip can NOT be empty");

        if (!ValidationUtil.isNumeric(typeCd)) throw ApiException.badRequest("Type CD must be Numeric");
        if (!ValidationUtil.isNumeric(catCd)) throw ApiException.badRequest("Category CD must be Numeric");
        if (!amtStr.matches("-?\\d{1,8}(\\.\\d{1,2})?")) {
            throw ApiException.badRequest("Amount should be in format -99999999.99");
        }
        if (!origDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw ApiException.badRequest("Orig Date should be in format YYYY-MM-DD");
        }
        if (!procDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw ApiException.badRequest("Proc Date should be in format YYYY-MM-DD");
        }
        if (!ValidationUtil.isValidDate(origDate)) throw ApiException.badRequest("Orig Date - Not a valid date");
        if (!ValidationUtil.isValidDate(procDate)) throw ApiException.badRequest("Proc Date - Not a valid date");
        if (!ValidationUtil.isNumeric(merchantId)) throw ApiException.badRequest("Merchant ID must be Numeric");

        tranTypes.findById(String.format("%-2.2s", typeCd).trim())
                .orElseThrow(() -> ApiException.badRequest("Tran Type NOT found..."));
        TranCategoryId tcid = new TranCategoryId(typeCd, Integer.parseInt(catCd));
        tranCats.findById(tcid)
                .orElseThrow(() -> ApiException.badRequest("Tran Category NOT found..."));

        if (!"Y".equals(confirm)) {
            if (!confirm.isEmpty() && !"N".equals(confirm)) {
                throw ApiException.badRequest("Invalid value. Valid values are (Y/N)");
            }
            return Map.of("requiresConfirmation", true,
                    "message", "Confirm to add this transaction...");
        }

        // POSTTRAN validation + posting.
        CardXref x = null;
        if (!cardNum.isEmpty()) {
            x = xrefs.findById(cardNum)
                    .orElseThrow(() -> ApiException.badRequest("INVALID CARD NUMBER FOUND"));
        }
        Long acctId = x != null ? x.getAcctId() : Long.parseLong(acctStr);
        Account a = accounts.findById(acctId)
                .orElseThrow(() -> ApiException.badRequest("ACCOUNT RECORD NOT FOUND"));

        BigDecimal amt = ValidationUtil.parseDecimal(amtStr);
        BigDecimal tempBal = nz(a.getCurrCycCredit()).subtract(nz(a.getCurrCycDebit())).add(amt);
        if (nz(a.getCreditLimit()).compareTo(tempBal) < 0) {
            throw ApiException.badRequest("OVERLIMIT TRANSACTION");
        }
        if (a.getExpirationDate() != null && a.getExpirationDate().compareTo(origDate) < 0) {
            throw ApiException.badRequest("TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        TranRecord t = new TranRecord();
        t.setTranId(nextTranId());
        t.setTypeCd(typeCd);
        t.setCatCd(Integer.parseInt(catCd));
        t.setSource(source);
        t.setDesc(desc);
        t.setAmt(amt);
        t.setMerchantId(Long.parseLong(merchantId));
        t.setMerchantName(merchantName);
        t.setMerchantCity(merchantCity);
        t.setMerchantZip(merchantZip);
        t.setCardNum(cardNum.isEmpty() ? "" : cardNum);
        t.setOrigTs(origDate + " 00:00:00.000000");
        t.setProcTs(procDate + " 00:00:00.000000");
        trans.save(t);

        // 2700-UPDATE-TCATBAL
        TranCatBalId balId = new TranCatBalId(acctId, typeCd, Integer.parseInt(catCd));
        TranCatBal bal = catBals.findById(balId).orElseGet(() -> {
            TranCatBal b = new TranCatBal();
            b.setAcctId(acctId);
            b.setTypeCd(typeCd);
            b.setCatCd(Integer.parseInt(catCd));
            b.setBal(BigDecimal.ZERO);
            return b;
        });
        bal.setBal(nz(bal.getBal()).add(amt));
        catBals.save(bal);

        // 2800-UPDATE-ACCOUNT-REC
        a.setCurrBal(nz(a.getCurrBal()).add(amt));
        if (amt.signum() >= 0) {
            a.setCurrCycCredit(nz(a.getCurrCycCredit()).add(amt));
        } else {
            a.setCurrCycDebit(nz(a.getCurrCycDebit()).add(amt));
        }
        accounts.save(a);

        Map<String, Object> out = new HashMap<>();
        out.put("message", "Transaction successful. Your Transaction ID is " + t.getTranId());
        out.put("transaction", t);
        return out;
    }

    public String nextTranId() {
        String max = trans.maxTranId();
        long n = 0;
        try { n = Long.parseLong(max == null ? "0" : max.trim()); } catch (NumberFormatException ignored) { /* unparsable ids count as 0 */ }
        return String.format("%016d", n + 1);
    }

    /** CORPT00C: monthly = current month, yearly = current year, custom = explicit range. */
    public Map<String, Object> report(String kind, String startDate, String endDate) {
        LocalDate today = LocalDate.now();
        String start;
        String end;
        String name;
        switch (kind == null ? "" : kind.toLowerCase()) {
            case "monthly" -> {
                YearMonth ym = YearMonth.from(today);
                start = ym.atDay(1).toString();
                end = ym.atEndOfMonth().toString();
                name = "Monthly";
            }
            case "yearly" -> {
                start = today.getYear() + "-01-01";
                end = today.getYear() + "-12-31";
                name = "Yearly";
            }
            case "custom" -> {
                if (!ValidationUtil.isValidDate(startDate)) throw ApiException.badRequest("Start Date - Not a valid date");
                if (!ValidationUtil.isValidDate(endDate)) throw ApiException.badRequest("End Date - Not a valid date");
                if (endDate.compareTo(startDate) < 0) throw ApiException.badRequest("End Date must not be before Start Date");
                start = startDate;
                end = endDate;
                name = "Custom";
            }
            default -> throw ApiException.badRequest("Please select a report type (monthly, yearly or custom)");
        }
        List<TranRecord> rows = trans.findInDateRange(start, end + " 23:59:59.999999");
        BigDecimal total = rows.stream().map(t -> nz(t.getAmt())).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> out = new HashMap<>();
        out.put("reportName", name);
        out.put("startDate", start);
        out.put("endDate", end);
        out.put("count", rows.size());
        out.put("totalAmount", total);
        out.put("transactions", rows);
        return out;
    }

    static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
