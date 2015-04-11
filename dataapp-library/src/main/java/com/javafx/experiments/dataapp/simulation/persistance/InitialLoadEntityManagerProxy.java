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
package com.javafx.experiments.dataapp.simulation.persistance;

import com.javafx.experiments.dataapp.model.*;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import java.util.*;

/**
 * This class acts as a proxy for an entity manager and should ONLY be used on an
 * load scenario with the goal of generating DailySales. The general idea is that persist only 'persists' sales
 * order lines in an accumulation that corresponds to an entry in the DailySales.
 * <p/>
 * Flush should be called at the end of every day, to insure that the ids generated
 * in the daily sales table are correct.
 * <p/>
 * This proxy is a bit of a hack but allows us to use the current Persistence heavy
 * framework of the SalesSimulator without changing the code.
 */
public class InitialLoadEntityManagerProxy implements EntityManager {

    private final EntityManager em;

    //Accumulates daily sales to be persisted out to the database.
    private TreeMap<Date, HashMap<Product, HashMap<String, HashMap<Region, Integer>>>> dailySalesCounter;

    public InitialLoadEntityManagerProxy(EntityManager em) {
        this.em = em;

        this.dailySalesCounter = new TreeMap<>();
    }

    @Override
    public void persist(Object o) {
        if (o instanceof SalesOrderLine) {
            doBlackMagic((SalesOrderLine) o);
        }
    }

    private void doBlackMagic(SalesOrderLine sol) {
        SalesOrder so = sol.getOrder();

        //all we care about is the day the sale happened
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(so.getDate().getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date date = cal.getTime();

        if (!dailySalesCounter.containsKey(date)) {
            dailySalesCounter.put(date, new HashMap<Product, HashMap<String, HashMap<Region, Integer>>>());
        }
        if (!dailySalesCounter.get(date).containsKey(sol.getProduct())) {
            dailySalesCounter.get(date).put(sol.getProduct(), new HashMap<String, HashMap<Region, Integer>>());
        }
        if (!dailySalesCounter.get(date).get(sol.getProduct()).containsKey(so.getCustomer().getAddress().getStateProvCd())) {
            dailySalesCounter.get(date).get(sol.getProduct()).put(so.getCustomer().getAddress().getStateProvCd(), new HashMap<Region, Integer>());
        }
        if (!dailySalesCounter.get(date).get(sol.getProduct()).get(so.getCustomer().getAddress().getStateProvCd()).containsKey(so.getRegion())) {
            dailySalesCounter.get(date).get(sol.getProduct()).get(so.getCustomer().getAddress().getStateProvCd()).put(so.getRegion(), sol.getQuantity());
        } else {
            Integer quantity = dailySalesCounter.get(date).get(sol.getProduct()).get(so.getCustomer().getAddress().getStateProvCd()).put(so.getRegion(), sol.getQuantity());
            quantity = quantity + sol.getQuantity();
            dailySalesCounter.get(date).get(sol.getProduct()).get(so.getCustomer().getAddress().getStateProvCd()).put(so.getRegion(), quantity);
        }
    }

    private void persistBlackMagic() {
        for (Date date : dailySalesCounter.keySet()) {
            for (Product product : dailySalesCounter.get(date).keySet()) {
                for (String state : dailySalesCounter.get(date).get(product).keySet()) {
                    for (Region region : dailySalesCounter.get(date).get(product).get(state).keySet()) {
                        DailySales dailySales = new DailySales();
                        dailySales.setRegion(region);
                        dailySales.setProduct(product);
                        dailySales.setStateProvCd(state);
                        dailySales.setQuantity(dailySalesCounter.get(date).get(product).get(state).get(region));
                        dailySales.setDate(date);
                        em.persist(dailySales);
                    }
                }
            }
            System.out.println(date);
        }
    }

    /* There is a huge assumption that the person calling this method knows what it is doing
     * Call every day and at the end of the day.
     */
    @Override
    public void flush() {
        persistBlackMagic();
        em.flush();
        dailySalesCounter = new TreeMap<>();
    }


    //----the rest of the class is delegated to the internal EntityManager
    @Override
    public <T> T merge(T t) {
        return em.merge(t);
    }

    @Override
    public void remove(Object o) {
        em.remove(o);
    }

    @Override
    public <T> T find(Class<T> type, Object o) {
        return em.find(type, o);
    }

    @Override
    public <T> T find(Class<T> type, Object o, Map<String, Object> map) {
        return em.find(type, o, map);
    }

    @Override
    public <T> T find(Class<T> type, Object o, LockModeType lmt) {
        return em.find(type, o, lmt);
    }

    @Override
    public <T> T find(Class<T> type, Object o, LockModeType lmt, Map<String, Object> map) {
        return em.find(type, o, lmt, map);
    }

    @Override
    public <T> T getReference(Class<T> type, Object o) {
        return em.getReference(type, o);
    }

    @Override
    public void setFlushMode(FlushModeType fmt) {
        em.setFlushMode(fmt);
    }

    @Override
    public FlushModeType getFlushMode() {
        return em.getFlushMode();
    }

    @Override
    public void lock(Object o, LockModeType lmt) {
        em.lock(o, lmt);
    }

    @Override
    public void lock(Object o, LockModeType lmt, Map<String, Object> map) {
        em.lock(o, lmt, map);
    }

    @Override
    public void refresh(Object o) {
        em.refresh(o);
    }

    @Override
    public void refresh(Object o, Map<String, Object> map) {
        em.refresh(o, map);
    }

    @Override
    public void refresh(Object o, LockModeType lmt) {
        em.refresh(o, lmt);
    }

    @Override
    public void refresh(Object o, LockModeType lmt, Map<String, Object> map) {
        em.refresh(o, lmt, map);
    }

    @Override
    public void clear() {
        em.clear();
    }

    @Override
    public void detach(Object o) {
        em.detach(o);
    }

    @Override
    public boolean contains(Object o) {
        return em.contains(o);
    }

    @Override
    public LockModeType getLockMode(Object o) {
        return em.getLockMode(o);
    }

    @Override
    public void setProperty(String string, Object o) {
        em.setProperty(string, o);
    }

    @Override
    public Map<String, Object> getProperties() {
        return em.getProperties();
    }

    @Override
    public Query createQuery(String string) {
        return em.createQuery(string);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> cq) {
        return em.createQuery(cq);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String string, Class<T> type) {
        return em.createQuery(string, type);
    }

    @Override
    public Query createNamedQuery(String string) {
        return em.createNamedQuery(string);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String string, Class<T> type) {
        return em.createNamedQuery(string, type);
    }

    @Override
    public Query createNativeQuery(String string) {
        return em.createNamedQuery(string);
    }

    @Override
    public Query createNativeQuery(String string, Class type) {
        return em.createNativeQuery(string, type);
    }

    @Override
    public Query createNativeQuery(String string, String string1) {
        return em.createNativeQuery(string, string1);
    }

    @Override
    public void joinTransaction() {
        em.joinTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return em.unwrap(type);
    }

    @Override
    public Object getDelegate() {
        return em.getDelegate();
    }

    @Override
    public void close() {
        em.close();
    }

    @Override
    public boolean isOpen() {
        return em.isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return em.getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return em.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return em.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return em.getMetamodel();
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return em.createQuery(updateQuery);
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return em.createQuery(deleteQuery);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return em.createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return em.createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return em.createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return em.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public boolean isJoinedToTransaction() {
        return em.isJoinedToTransaction();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return em.createEntityGraph(rootType);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return em.createEntityGraph(graphName);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return em.getEntityGraph(graphName);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return em.getEntityGraphs(entityClass);
    }

}
