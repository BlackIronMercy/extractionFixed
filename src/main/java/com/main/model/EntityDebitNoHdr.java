/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.main.model;

/**
 *
 * @author shash
 */
public class EntityDebitNoHdr {

    String FILE_DEPARTMENT;
    String BILL_CURRENCY;
    String INVOICE_BILLED_DATE;
    String INVOICE_BILL_AMOUNT;
    String INVOICENO;
    String INVOICE_RS_REFERENCE;
    String BILL_CLIENT_REF;

    public String getFILE_DEPARTMENT() {
        return FILE_DEPARTMENT;
    }

    public void setFILE_DEPARTMENT(String FILE_DEPARTMENT) {
        this.FILE_DEPARTMENT = FILE_DEPARTMENT;
    }

    public String getBILL_CURRENCY() {
        return BILL_CURRENCY;
    }

    public void setBILL_CURRENCY(String BILL_CURRENCY) {
        this.BILL_CURRENCY = BILL_CURRENCY;
    }

    public String getINVOICE_BILLED_DATE() {
        return INVOICE_BILLED_DATE;
    }

    public void setINVOICE_BILLED_DATE(String INVOICE_BILLED_DATE) {
        this.INVOICE_BILLED_DATE = INVOICE_BILLED_DATE;
    }

    public String getINVOICE_BILL_AMOUNT() {
        return INVOICE_BILL_AMOUNT;
    }

    public void setINVOICE_BILL_AMOUNT(String INVOICE_BILL_AMOUNT) {
        this.INVOICE_BILL_AMOUNT = INVOICE_BILL_AMOUNT;
    }

    public String getINVOICENO() {
        return INVOICENO;
    }

    public void setINVOICENO(String INVOICENO) {
        this.INVOICENO = INVOICENO;
    }

    public String getINVOICE_RS_REFERENCE() {
        return INVOICE_RS_REFERENCE;
    }

    public void setINVOICE_RS_REFERENCE(String INVOICE_RS_REFERENCE) {
        this.INVOICE_RS_REFERENCE = INVOICE_RS_REFERENCE;
    }

    public String getBILL_CLIENT_REF() {
        return BILL_CLIENT_REF;
    }

    public void setBILL_CLIENT_REF(String BILL_CLIENT_REF) {
        this.BILL_CLIENT_REF = BILL_CLIENT_REF;
    }

}
