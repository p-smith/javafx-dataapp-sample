/*
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.javafx.experiments.dataapp.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "DAILY_SALES", schema = "APP", indexes = {
        @Index(columnList = "STATE_PROV_CD"),
        @Index(columnList = "DATE")
})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DailySales.findAll", query = "SELECT d FROM DailySales d"),
    @NamedQuery(name = "DailySales.findByDailySalesId", query = "SELECT d FROM DailySales d WHERE d.dailySalesId = :dailySalesId"),
    @NamedQuery(name = "DailySales.findByStateProvCd", query = "SELECT d FROM DailySales d WHERE d.stateProvCd = :stateProvCd"),
    @NamedQuery(name = "DailySales.findByQuantity", query = "SELECT d FROM DailySales d WHERE d.quantity = :quantity"),
    @NamedQuery(name = "DailySales.findByDate", query = "SELECT d FROM DailySales d WHERE d.date = :date")})
public class DailySales implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "DAILY_SALES_ID")
    private Integer dailySalesId;

    @Column(name = "STATE_PROV_CD")
    private String stateProvCd;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "DATE")
    @Temporal(TemporalType.DATE)
    private Date date;

    @JoinColumn(name = "REGION_ID", referencedColumnName = "REGION_ID")
    @ManyToOne
    private Region region;

    @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
    @ManyToOne
    private Product product;

    public DailySales() {
    }

    public DailySales(Integer dailySalesId) {
        this.dailySalesId = dailySalesId;
    }

    public Integer getDailySalesId() {
        return dailySalesId;
    }

    public void setDailySalesId(Integer dailySalesId) {
        this.dailySalesId = dailySalesId;
    }

    public String getStateProvCd() {
        return stateProvCd;
    }

    public void setStateProvCd(String stateProvCd) {
        this.stateProvCd = stateProvCd;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dailySalesId != null ? dailySalesId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DailySales)) {
            return false;
        }
        DailySales other = (DailySales) object;
        return !((this.dailySalesId == null && other.dailySalesId != null) || (this.dailySalesId != null && !this.dailySalesId.equals(other.dailySalesId)));
    }

    @Override
    public String toString() {
        return "com.javafx.experiments.dataapp.model.DailySales[ dailySalesId=" + dailySalesId + " ]";
    }
    
}
