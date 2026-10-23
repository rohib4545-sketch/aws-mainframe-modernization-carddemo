package com.carddemo.model;

import jakarta.persistence.*;

/** TRAN-TYPE-RECORD (CVTRA03Y) */
@Entity
@Table(name = "tran_type")
public class TranType {
    @Id
    @Column(length = 2)
    private String typeCd;
    @Column(length = 50)
    private String desc;

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String v) { typeCd = v; }
    public String getDesc() { return desc; }
    public void setDesc(String v) { desc = v; }
}
