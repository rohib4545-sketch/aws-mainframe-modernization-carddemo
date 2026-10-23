package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.*;
import com.carddemo.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.carddemo.service.TransactionService.nz;

/** COBIL00C — Bill Payment. */
@Service
public class BillPaymentService {
    private final AccountRepository accounts;
    private final CardXrefRepository xrefs;
    private final TranRecordRepository trans;
    private final TransactionService tranService;

    public BillPaymentService(AccountRepository accounts, CardXrefRepository xrefs,
                              TranRecordRepository trans, TransactionService tranService) {
        this.accounts = accounts; this.xrefs = xrefs;
        this.trans = trans; this.tranService = tranService;
    }

    public Map<String, Object> view(Long acctId) {
        Account a = accounts.findById(acctId)
                .orElseThrow(() -> ApiException.notFound("Account ID NOT found..."));
        Map<String, Object> out = new HashMap<>();
        out.put("account", a);
        out.put("currentBalance", a.getCurrBal());
        out.put("message", "Confirm to make a bill payment...");
        return out;
    }

    @Transactional
    public Map<String, Object> pay(Long acctId, String confirm) {
        if (acctId == null) throw ApiException.badRequest("Acct ID can NOT be empty...");
        Account a = accounts.findById(acctId)
                .orElseThrow(() -> ApiException.notFound("Account ID NOT found..."));

        if (confirm != null && !confirm.isBlank()
                && !"Y".equalsIgnoreCase(confirm) && !"N".equalsIgnoreCase(confirm)) {
            throw ApiException.badRequest("Invalid value. Valid values are (Y/N)...");
        }
        if ("N".equalsIgnoreCase(confirm)) {
            // COBIL00C: 'N' clears the screen and exits — the payment is cancelled.
            return Map.of("cancelled", true, "message", "Payment cancelled.");
        }
        if (a.getCurrBal() == null || a.getCurrBal().signum() <= 0) {
            throw ApiException.badRequest("You have nothing to pay...");
        }
        if (!"Y".equalsIgnoreCase(confirm)) {
            return Map.of("requiresConfirmation", true,
                    "message", "Confirm to make a bill payment...",
                    "currentBalance", a.getCurrBal());
        }

        CardXref x = xrefs.findFirstByAcctId(acctId)
                .orElseThrow(() -> ApiException.notFound("Account ID NOT found..."));

        TranRecord t = new TranRecord();
        t.setTranId(tranService.nextTranId());
        t.setTypeCd("02");
        t.setCatCd(2);
        t.setSource("POS TERM");
        t.setDesc("BILL PAYMENT - ONLINE");
        t.setAmt(a.getCurrBal());
        t.setCardNum(x.getCardNum());
        t.setMerchantId(999999999L);
        t.setMerchantName("BILL PAYMENT");
        t.setMerchantCity("N/A");
        t.setMerchantZip("N/A");
        String now = ValidationUtil.now();
        t.setOrigTs(now);
        t.setProcTs(now);
        trans.save(t);

        a.setCurrBal(nz(a.getCurrBal()).subtract(t.getAmt()));
        accounts.save(a);

        Map<String, Object> out = new HashMap<>();
        out.put("message", "Payment successful. Your Transaction ID is " + t.getTranId());
        out.put("transaction", t);
        out.put("newBalance", a.getCurrBal());
        return out;
    }
}
