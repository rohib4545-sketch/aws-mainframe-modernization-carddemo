package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.*;
import com.carddemo.repo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** COCRDLIC (list), COCRDSLC (view), COCRDUPC (update). */
@Service
public class CardService {
    private final CardRepository cards;
    private final CardXrefRepository xrefs;
    private final AccountRepository accounts;
    private final CustomerRepository customers;

    public CardService(CardRepository cards, CardXrefRepository xrefs,
                       AccountRepository accounts, CustomerRepository customers) {
        this.cards = cards;
        this.xrefs = xrefs;
        this.accounts = accounts;
        this.customers = customers;
    }

    /** Browse cards with optional 11-digit account filter and 16-digit card filter, 7 per page. */
    public Map<String, Object> list(String acctIdFilter, String cardNumFilter, int page, int size) {
        Long acctId = null;
        String cardNum = null;
        if (!ValidationUtil.isBlank(acctIdFilter)) {
            String f = acctIdFilter.trim();
            if (!ValidationUtil.isNumeric(f) || f.length() != 11) {
                throw ApiException.badRequest("ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER");
            }
            acctId = Long.parseLong(f);
            if (acctId == 0) acctId = null;
        }
        if (!ValidationUtil.isBlank(cardNumFilter)) {
            String f = cardNumFilter.trim();
            if (!ValidationUtil.isNumeric(f) || f.length() != 16) {
                throw ApiException.badRequest("CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER");
            }
            cardNum = f;
        }
        List<Card> page0 = cards.browse(acctId, cardNum, PageRequest.of(Math.max(page, 0), size));
        boolean more = cards.browse(acctId, cardNum, PageRequest.of(Math.max(page, 0) + 1, size)).size() > 0;
        Map<String, Object> out = new HashMap<>();
        out.put("cards", page0);
        out.put("page", Math.max(page, 0));
        out.put("pageSize", size);
        out.put("hasNextPage", more);
        out.put("hasPrevPage", page > 0);
        return out;
    }

    /** Credit Card View: card + xref + account + customer. */
    public Map<String, Object> view(String cardNum, Long acctId) {
        Card c;
        if (!ValidationUtil.isBlank(cardNum)) {
            c = cards.findById(cardNum.trim())
                    .orElseThrow(() -> ApiException.notFound("Did not find cards for this search condition"));
        } else if (acctId != null) {
            List<Card> l = cards.findByAcctIdOrderByCardNum(acctId);
            if (l.isEmpty()) throw ApiException.notFound("Did not find this account in cards database");
            c = l.get(0);
        } else {
            throw ApiException.badRequest("Account or Card Number must be entered");
        }
        Account a = accounts.findById(c.getAcctId()).orElse(null);
        CardXref x = xrefs.findById(c.getCardNum()).orElse(null);
        Customer cust = x != null ? customers.findById(x.getCustId()).orElse(null) : null;
        Map<String, Object> out = new HashMap<>();
        out.put("card", c);
        out.put("account", a);
        out.put("customer", cust);
        return out;
    }

    /** Credit Card Update: name, status Y/N, expiry month 1-12, year 1950-2099. */
    @Transactional
    public Map<String, Object> update(String cardNum, Map<String, Object> in) {
        Card c = cards.findById(cardNum.trim())
                .orElseThrow(() -> ApiException.notFound("Did not find cards for this search condition"));

        if (AccountService.supplied(in, "embossedName")) {
            String v = AccountService.str(in, "embossedName");
            if (!ValidationUtil.isAlpha(v)) throw ApiException.badRequest("Card name can only contain alphabets");
            c.setEmbossedName(v);
        }
        if (AccountService.supplied(in, "activeStatus")) {
            String v = AccountService.str(in, "activeStatus").toUpperCase();
            if (!ValidationUtil.isYesNo(v)) throw ApiException.badRequest("Card Active Status must be Y or N");
            c.setActiveStatus(v);
        }

        // Expiry: month and year validated independently, old day preserved.
        String[] parts = c.getExpirationDate() != null ? c.getExpirationDate().split("-") : new String[0];
        String year = parts.length == 3 ? parts[0] : "";
        String month = parts.length == 3 ? parts[1] : "";
        String day = parts.length == 3 ? parts[2] : "01";
        if (AccountService.supplied(in, "expMonth")) {
            String v = AccountService.str(in, "expMonth");
            if (!v.matches("\\d{1,2}") || Integer.parseInt(v) < 1 || Integer.parseInt(v) > 12) {
                throw ApiException.badRequest("Card expiry month must be between 1 and 12");
            }
            month = String.format("%02d", Integer.parseInt(v));
        }
        if (AccountService.supplied(in, "expYear")) {
            String v = AccountService.str(in, "expYear");
            if (!v.matches("\\d{4}") || Integer.parseInt(v) < 1950 || Integer.parseInt(v) > 2099) {
                throw ApiException.badRequest("Invalid card expiry year");
            }
            year = v;
        }
        if (AccountService.supplied(in, "expirationDate")) {
            String v = AccountService.str(in, "expirationDate");
            if (!ValidationUtil.isValidDate(v)) throw ApiException.badRequest("Card expiry must be a valid date in YYYY-MM-DD format");
            c.setExpirationDate(v);
        } else if (AccountService.supplied(in, "expMonth") || AccountService.supplied(in, "expYear")) {
            c.setExpirationDate(year + "-" + month + "-" + day);
        }
        cards.save(c);
        Map<String, Object> out = new HashMap<>();
        out.put("message", "Update of record successful");
        out.put("card", c);
        return out;
    }
}
