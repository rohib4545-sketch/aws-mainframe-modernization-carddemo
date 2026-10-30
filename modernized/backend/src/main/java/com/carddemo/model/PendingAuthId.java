package com.carddemo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

public class PendingAuthId implements Serializable {
    private String cardNum;
    private String authTs;
    public PendingAuthId() {}
    public PendingAuthId(String c, String t) { cardNum = c;
    authTs = t;
    }
    @Override public boolean equals(Object o) {
        if (!(o instanceof PendingAuthId x)) return false;
        return Objects.equals(cardNum, x.cardNum) && Objects.equals(authTs, x.authTs);
    }
    @Override public int hashCode() { return Objects.hash(cardNum, authTs); }
}
