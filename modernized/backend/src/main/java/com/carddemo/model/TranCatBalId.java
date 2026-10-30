package com.carddemo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

public class TranCatBalId implements Serializable {
    private Long acctId;
    private String typeCd;
    private Integer catCd;
    public TranCatBalId() {}
    public TranCatBalId(Long a, String t, Integer c) {
        acctId = a;
        typeCd = t;
        catCd = c;
    }
    @Override public boolean equals(Object o) {
        if (!(o instanceof TranCatBalId x)) return false;
        return Objects.equals(acctId, x.acctId) && Objects.equals(typeCd, x.typeCd) && Objects.equals(catCd, x.catCd);
    }
    @Override public int hashCode() { return Objects.hash(acctId, typeCd, catCd); }
}
