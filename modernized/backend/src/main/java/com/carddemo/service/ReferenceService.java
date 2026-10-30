package com.carddemo.service;

import com.carddemo.config.ApiException;
import com.carddemo.model.*;
import com.carddemo.repo.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** COTRTLIC / COTRTUPC — Transaction Type maintenance (Db2 module) + category lookups. */
@Service
public class ReferenceService {
    private final TranTypeRepository tranTypes;
    private final TranCategoryRepository tranCats;

    public ReferenceService(TranTypeRepository tranTypes, TranCategoryRepository tranCats) {
        this.tranTypes = tranTypes;
        this.tranCats = tranCats;
    }

    public List<TranType> listTypes() { return tranTypes.findAll(); }
    public List<TranCategory> listCategories(String typeCd) {
        return typeCd == null ? tranCats.findAll() : tranCats.findByTypeCdOrderByCatCd(typeCd);
    }

    public TranType addType(String typeCd, String desc) {
        if (ValidationUtil.isBlank(typeCd) || typeCd.trim().length() != 2 || !ValidationUtil.isNumeric(typeCd.trim())) {
            throw ApiException.badRequest("Type CD must be a 2 digit code");
        }
        String cd = typeCd.trim();
        if (ValidationUtil.isBlank(desc)) throw ApiException.badRequest("Description can NOT be empty");
        if (tranTypes.existsById(cd)) throw ApiException.badRequest("Tran Type already exist...");
        TranType t = new TranType();
        t.setTypeCd(cd);
        t.setDesc(desc.trim());
        return tranTypes.save(t);
    }

    public TranType updateType(String typeCd, String desc) {
        TranType t = tranTypes.findById(typeCd.trim())
                .orElseThrow(() -> ApiException.notFound("Tran Type NOT found..."));
        if (ValidationUtil.isBlank(desc)) throw ApiException.badRequest("Description can NOT be empty");
        t.setDesc(desc.trim());
        return tranTypes.save(t);
    }

    public Map<String, Object> deleteType(String typeCd) {
        TranType t = tranTypes.findById(typeCd.trim())
                .orElseThrow(() -> ApiException.notFound("Tran Type NOT found..."));
        tranTypes.delete(t);
        Map<String, Object> out = new HashMap<>();
        out.put("message", "Tran Type " + t.getTypeCd() + " has been deleted ...");
        return out;
    }
}
