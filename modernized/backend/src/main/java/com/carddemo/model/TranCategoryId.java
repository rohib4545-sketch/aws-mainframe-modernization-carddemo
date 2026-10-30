package com.carddemo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

public class TranCategoryId implements Serializable {
    private String typeCd;
    private Integer catCd;
    public TranCategoryId() {}
    public TranCategoryId(String t, Integer c) {
        typeCd = t;
        catCd = c;
    }
    @Override public boolean equals(Object o) {
        if (!(o instanceof TranCategoryId x)) return false;
        return Objects.equals(typeCd, x.typeCd) && Objects.equals(catCd, x.catCd);
    }
    @Override public int hashCode() { return Objects.hash(typeCd, catCd); }
}
