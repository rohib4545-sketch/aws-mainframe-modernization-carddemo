package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.*;
import com.carddemo.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/** COPAUS0C / COPAUS1C — Pending Authorization summary and detail (IMS/DB2/MQ module). */
@Service
public class AuthorizationService {
    private final PendingAuthRepository auths;
    private final CardXrefRepository xrefs;
    private final AccountRepository accounts;

    public AuthorizationService(PendingAuthRepository auths, CardXrefRepository xrefs,
                                AccountRepository accounts) {
        this.auths = auths; this.xrefs = xrefs; this.accounts = accounts;
    }

    /** Summary per account: approved/declined counts and amounts (CIPAUSMY). */
    public Map<String, Object> summary(Long acctId) {
        List<PendingAuth> rows = acctId == null
                ? auths.findAllByOrderByAuthTsDesc()
                : auths.findByAcctIdOrderByAuthTsDesc(acctId);
        Map<Long, List<PendingAuth>> byAcct = new TreeMap<>();
        for (PendingAuth a : rows) byAcct.computeIfAbsent(a.getAcctId(), k -> new ArrayList<>()).add(a);

        List<Map<String, Object>> summaries = new ArrayList<>();
        for (var e : byAcct.entrySet()) {
            int apprCnt = 0, declCnt = 0;
            BigDecimal apprAmt = BigDecimal.ZERO, declAmt = BigDecimal.ZERO;
            for (PendingAuth a : e.getValue()) {
                if ("00".equals(a.getAuthRespCode())) { apprCnt++; apprAmt = apprAmt.add(a.getApprovedAmt()); }
                else { declCnt++; declAmt = declAmt.add(a.getTransactionAmt()); }
            }
            Map<String, Object> s = new HashMap<>();
            s.put("acctId", e.getKey());
            s.put("custId", e.getValue().get(0).getCustId());
            s.put("approvedAuthCount", apprCnt);
            s.put("declinedAuthCount", declCnt);
            s.put("approvedAuthAmt", apprAmt);
            s.put("declinedAuthAmt", declAmt);
            s.put("count", e.getValue().size());
            summaries.add(s);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("summaries", summaries);
        out.put("authorizations", rows);
        return out;
    }

    public Map<String, Object> detail(String cardNum, String authTs) {
        PendingAuth a = auths.findById(new PendingAuthId(cardNum, authTs))
                .orElseThrow(() -> ApiException.notFound("Authorization NOT found..."));
        Account acct = accounts.findById(a.getAcctId()).orElse(null);
        Map<String, Object> out = new HashMap<>();
        out.put("authorization", a);
        out.put("account", acct);
        return out;
    }

    /** COPAUS1C: mark an authorization matched to a transaction (update IMS, insert into DB2 log). */
    @Transactional
    public Map<String, Object> markMatched(String cardNum, String authTs, String transactionId) {
        PendingAuth a = auths.findById(new PendingAuthId(cardNum, authTs))
                .orElseThrow(() -> ApiException.notFound("Authorization NOT found..."));
        if ("M".equals(a.getMatchStatus())) {
            throw ApiException.badRequest("Authorization already matched...");
        }
        a.setMatchStatus("M");
        if (!ValidationUtil.isBlank(transactionId)) a.setTransactionId(transactionId.trim());
        auths.save(a);
        return Map.of("message", "Authorization matched successfully", "authorization", a);
    }

    /** Flag/unflag an authorization as fraud. */
    @Transactional
    public Map<String, Object> markFraud(String cardNum, String authTs, String fraud) {
        PendingAuth a = auths.findById(new PendingAuthId(cardNum, authTs))
                .orElseThrow(() -> ApiException.notFound("Authorization NOT found..."));
        String f = fraud == null ? "Y" : fraud.trim().toUpperCase();
        if (!"Y".equals(f) && !"N".equals(f)) {
            throw ApiException.badRequest("Fraud flag must be Y or N");
        }
        a.setAuthFraud(f);
        a.setFraudRptDate("Y".equals(f) ? ValidationUtil.today() : "");
        auths.save(a);
        return Map.of("message", "Y".equals(f) ? "Authorization reported as fraud" : "Fraud flag cleared",
                "authorization", a);
    }
}
