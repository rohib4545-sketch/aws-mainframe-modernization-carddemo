package com.carddemo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

public class DisclosureGroupId implements Serializable {
    private String acctGroupId;
    private String tranTypeCd;
    private Integer tranCatCd;
    public DisclosureGroupId() {}
    public DisclosureGroupId(String g, String t, Integer c) { acctGroupId = g; tranTypeCd = t; tranCatCd = c; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof DisclosureGroupId x)) return false;
        return Objects.equals(acctGroupId, x.acctGroupId) && Objects.equals(tranTypeCd, x.tranTypeCd) && Objects.equals(tranCatCd, x.tranCatCd);
    }
    @Override public int hashCode() { return Objects.hash(acctGroupId, tranTypeCd, tranCatCd); }
}
