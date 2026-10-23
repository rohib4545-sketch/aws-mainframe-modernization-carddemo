package com.carddemo.model;

import jakarta.persistence.*;

/** TRAN-CAT-RECORD (CVTRA04Y) */
@Entity
@Table(name = "tran_category")
@IdClass(TranCategoryId.class)
public class TranCategory {
    @Id
    @Column(length = 2)
    private String typeCd;
    @Id
    private Integer catCd;
    @Column(length = 50)
    private String desc;

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String v) { typeCd = v; }
    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer v) { catCd = v; }
    public String getDesc() { return desc; }
    public void setDesc(String v) { desc = v; }
}
