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
package com.javafx.experiments.dataapp.server.service;


import com.javafx.experiments.dataapp.model.ProductType;
import com.javafx.experiments.dataapp.model.Region;
import com.javafx.experiments.dataapp.model.transit.ProductTypeTransitCumulativeSeriesSales;
import com.javafx.experiments.dataapp.model.transit.RegionTransitCumulativeSales;
import com.javafx.experiments.dataapp.model.transit.StateTransitCumulativeSales;
import com.javafx.experiments.dataapp.model.transit.TransitCumulativeSales;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Path("com.javafx.experiments.dataapp.model.cumulativelivesales")
public class CumulativeLiveSalesFacadeREST {

    @Inject
    private EntityManager em;

    private static final String BASE_RANGE_QUERY = 
            "select "
                + "min(hs.dailySalesId), "
                + "max(hs.dailySalesId), "
                + "sum(hs.quantity * p.cost), "
                + "sum(hs.quantity * p.price), "
                + "hs.date "
            + "from DailySales hs "
            + "inner join hs.product p "
            + "group by hs.date "
            + "order by hs.date desc";
    
    private static final String TYPE_SUM_QUERY = 
              "select sum(hs.quantity * p.cost), "
                + "sum(hs.quantity * p.price), " 
                + "sum(hs.quantity), "
                + "pt.productTypeId "
            + "from DailySales hs "
            + "left join hs.product p "            
            + "left join p.productType pt "    
            + "where hs.dailySalesId >= :startId and hs.dailySalesId <= :endId "
            + "group by pt.productTypeId";

    private static final String TYPE_RANGE_QUERY = 
            "select sum(hs.quantity), "
                + "pt.productTypeId "
            + "from DailySales hs "
            + "left join hs.product p "
            + "left join p.productType pt "
            + "where hs.dailySalesId >= :startId and hs.dailySalesId <= :endId "
            + "group by pt.productTypeId, hs.date ";

    private static final String REGION_SUM_QUERY = 
            "select sum(hs.quantity * p.cost), "
                + "sum(hs.quantity * p.price), " 
                + "sum(hs.quantity), "
                + "r.regionId "
            + "from DailySales hs "
            + "left join hs.product p "
            + "left join hs.region r "
            + "where hs.dailySalesId >= :startId and hs.dailySalesId <= :endId "
            + "group by r.regionId";
    
    
    //begin region queries
    private static final String REGION_RANGE_QUERY = 
            "select "
                + "min(hs.dailySalesId), "
                + "max(hs.dailySalesId), "
                + "sum(hs.quantity * p.cost), "
                + "sum(hs.quantity * p.price), " 
                + "hs.date "
            + "from DailySales hs "
            + "inner join hs.product p "
            + "where hs.region.regionId = :regionId "
            + "group by hs.date "
            + "order by hs.date desc";
    
    private static final String REGION_TYPE_SUM_QUERY = 
            "select sum(hs.quantity * p.cost), "
                + "sum(hs.quantity * p.price), "
                + "sum(hs.quantity), "
                + "pt.productTypeId "
            + "from DailySales hs "
            + "left join hs.product p "            
            + "left join p.productType pt "    
            + "where hs.dailySalesId >= :startId and hs.dailySalesId <= :endId "
            + "and hs.region.regionId = :regionId "
            + "group by pt.productTypeId ";

    private static final String REGION_TYPE_RANGE_QUERY = 
            "select sum(hs.quantity), "
                + "pt.productTypeId "
            + "from DailySales hs "
            + "left join hs.product p "
            + "left join p.productType pt "
            + "where hs.dailySalesId >= :startId and hs.dailySalesId <= :endId "
            + "and hs.region.regionId = :regionId "
            + "group by pt.productTypeId, hs.date ";

    private static final String STATE_SUM_QUERY = 
            "select sum(hs.quantity * p.cost), "
                + "sum(hs.quantity * p.price), " 
                + "sum(hs.quantity), "
                + "hs.stateProvCd "
            + "from DailySales hs "
            + "left join hs.product p "
            + "where hs.dailySalesId >= :startId and hs.dailySalesId <= :endId "
            + "and hs.region.regionId = :regionId "
            + "group by hs.stateProvCd";
    
//end region
    
    @GET
    @Produces({"application/xml", "application/json"})
    public List<TransitCumulativeSales> findAll() {       
        TypedQuery<Object[]> baseRangeQuery = em.createQuery(BASE_RANGE_QUERY, Object[].class);
        List<TransitCumulativeSales> result = new ArrayList<>();
        List<Object[]> resultList = baseRangeQuery.getResultList();
        for (Object[] o : resultList) {
            TransitCumulativeSales t = new TransitCumulativeSales();
            t.setStartDailySalesId((Integer)o[0]);
            t.setEndDailySalesId((Integer)o[1]);
            t.setCost((Double)o[2]);
            t.setSales((Double) o[3]);
            t.setDate((Date)o[4]);
            result.add(t);
        }
        return result;
    }
    
    @GET
    @Path("/recent/")
    @Produces({"application/xml", "application/json"})
    public List<TransitCumulativeSales> findRecent() {     
        TypedQuery<Object[]> baseRangeQuery = em.createQuery(BASE_RANGE_QUERY, Object[].class);
        baseRangeQuery.setMaxResults(200);
        List<TransitCumulativeSales> result = new ArrayList<>();
        List<Object[]> resultList = baseRangeQuery.getResultList();
        for (Object[] o : resultList) {
            TransitCumulativeSales t = new TransitCumulativeSales();
            t.setStartDailySalesId((Integer)o[0]);
            t.setEndDailySalesId((Integer)o[1]);
            t.setCost((Double)o[2]);
            t.setSales((Double) o[3]);
            t.setDate((Date)o[4]);
            result.add(t);
        }
        return result;
    }
    
    
    @GET
    @Path("/type/{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<ProductTypeTransitCumulativeSeriesSales> findTypeRange(@PathParam("from") String from, @PathParam("to") String to) {
        System.out.println("START findTypeRange (from="+from+" , to="+to+")");
        long DIFF, TIME = System.currentTimeMillis(), START_TIME = System.currentTimeMillis();
        TypedQuery<Object[]> q = em.createQuery(TYPE_SUM_QUERY, Object[].class);
      
        DIFF = System.currentTimeMillis() - TIME;
        System.out.println("    Q1 COMP TIME = "+DIFF+"ms");
        TIME = System.currentTimeMillis();
        
        Parameter<Integer> p1 = q.getParameter("startId", Integer.class);
        q.setParameter(p1, Integer.parseInt(from));
        Parameter<Integer> p2 = q.getParameter("endId", Integer.class);
        q.setParameter(p2, Integer.parseInt(to));
        
        List<ProductTypeTransitCumulativeSeriesSales> result = new ArrayList<>();
        List<Object[]> resultList = q.getResultList();
        
        DIFF = System.currentTimeMillis() - TIME;
        System.out.println("    Q1 TIME = "+DIFF+"ms");
        TIME = System.currentTimeMillis();
               
        for (Object[] o : resultList) {
            ProductTypeTransitCumulativeSeriesSales t = new ProductTypeTransitCumulativeSeriesSales();
            t.setCost((Double)o[0]);
            t.setSales((Double) o[1]);
            t.setUnits((Long) o[2]);
            ProductType pt = em.find(ProductType.class, o[3]);
            t.setProductType(pt);
            result.add(t);
        }
        
        //building sales range
        HashMap<ProductType, List<Double>> seriesGenerator = new HashMap<>();
        
        TypedQuery<Object[]> q2 = em.createQuery(TYPE_RANGE_QUERY, Object[].class);
        p1 = q2.getParameter("startId", Integer.class);
        q2.setParameter(p1, Integer.parseInt(from));
        p2 = q2.getParameter("endId", Integer.class);
        q2.setParameter(p2, Integer.parseInt(to));
        resultList = q2.getResultList();
        
        DIFF = System.currentTimeMillis() - TIME;
        System.out.println("    Q2 TIME = "+DIFF+"ms");
        
        for (Object[] o : resultList) {
            ProductType pt = em.find(ProductType.class, o[1]);
            if (!seriesGenerator.containsKey(pt)) {
                seriesGenerator.put(pt, new ArrayList<>());
            }
            seriesGenerator.get(pt).add(((Long)o[0]).doubleValue());
        }
        for (ProductTypeTransitCumulativeSeriesSales tcs : result){
            tcs.setSeries(seriesGenerator.get(tcs.getProductType())) ;
        }
        
        DIFF = System.currentTimeMillis() - START_TIME;
        System.out.println("    TOTAL TIME = "+DIFF+"ms");

        return result;
    }

    @GET
    @Path("/region/{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<RegionTransitCumulativeSales> findRegionRange(@PathParam("from") String from, @PathParam("to") String to) {
        System.out.println("START findRegionRange (from="+from+" , to="+to+")");
        long DIFF, START_TIME = System.currentTimeMillis();
        TypedQuery<Object[]> q = em.createQuery(REGION_SUM_QUERY, Object[].class);
        Parameter<Integer> p1 = q.getParameter("startId", Integer.class);
        q.setParameter(p1, Integer.parseInt(from));
        Parameter<Integer> p2 = q.getParameter("endId", Integer.class);
        q.setParameter(p2, Integer.parseInt(to));
        
        List<RegionTransitCumulativeSales> result = new ArrayList<>();
        List<Object[]> resultList = q.getResultList();

        for (Object[] o : resultList) {
            RegionTransitCumulativeSales t = new RegionTransitCumulativeSales();
            t.setCost((Double)o[0]);
            t.setSales((Double) o[1]);
            t.setUnits((Long) o[2]);
            Region region = em.find(Region.class, o[3]);
            t.setRegion(region);
            result.add(t);
        }
        
        DIFF = System.currentTimeMillis() - START_TIME;
        System.out.println("    TOTAL TIME = "+DIFF+"ms");
        
        return result;
    }
    
    
    //region calls --same as above but with region, this can probably be refactored to make more sense
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/region/{regionId}")
    public List<TransitCumulativeSales> findAllRegion(@PathParam("regionId") Integer regionId) {       
        TypedQuery<Object[]> baseRangeQuery = em.createQuery(REGION_RANGE_QUERY, Object[].class);
        Parameter<Integer> p1 = baseRangeQuery.getParameter("regionId", Integer.class);
        baseRangeQuery.setParameter(p1, regionId);
        
        List<TransitCumulativeSales> result = new ArrayList<>();
        List<Object[]> resultList = baseRangeQuery.getResultList();
        for (Object[] o : resultList) {
            TransitCumulativeSales t = new TransitCumulativeSales();
            t.setStartDailySalesId((Integer)o[0]);
            t.setEndDailySalesId((Integer)o[1]);
            t.setCost((Double)o[2]);
            t.setSales((Double) o[3]);
            t.setDate((Date)o[4]);
            result.add(t);
        }
        return result;
    }
    
    @GET
    @Path("/state/{from}/{to}/{regionId}")
    @Produces({"application/xml", "application/json"})
    public List<StateTransitCumulativeSales> findStateRange(@PathParam("from") String from, @PathParam("to") String to, @PathParam("regionId") Integer regionId) {
        System.out.println("START findRegionRange (from="+from+" , to="+to+")");
        long DIFF, START_TIME = System.currentTimeMillis();
        TypedQuery<Object[]> q = em.createQuery(STATE_SUM_QUERY, Object[].class);
        Parameter<Integer> p1 = q.getParameter("startId", Integer.class);
        q.setParameter(p1, Integer.parseInt(from));
        Parameter<Integer> p2 = q.getParameter("endId", Integer.class);
        q.setParameter(p2, Integer.parseInt(to));
        Parameter<Integer> p3 = q.getParameter("regionId", Integer.class);
        q.setParameter(p3, regionId);
        
        List<StateTransitCumulativeSales> result = new ArrayList<>();
        List<Object[]> resultList = q.getResultList();

        for (Object[] o : resultList) {
            StateTransitCumulativeSales t = new StateTransitCumulativeSales();
            t.setCost((Double)o[0]);
            t.setSales((Double) o[1]);
            t.setUnits((Long) o[2]);
            t.setState((String) o[3]);
            result.add(t);
        }
        
        DIFF = System.currentTimeMillis() - START_TIME;
        System.out.println("    TOTAL TIME = "+DIFF+"ms");
        
        return result;
    }
    
    @GET
    @Path("/type/{from}/{to}/{regionId}")
    @Produces({"application/xml", "application/json"})
    public List<ProductTypeTransitCumulativeSeriesSales> findTypeRegionRange(@PathParam("from") String from, @PathParam("to") String to, @PathParam("regionId") Integer regionId) {
        System.out.println("START findTypeRange (from="+from+" , to="+to+")");
        long DIFF, TIME = System.currentTimeMillis(), START_TIME = System.currentTimeMillis();
        TypedQuery<Object[]> q = em.createQuery(REGION_TYPE_SUM_QUERY, Object[].class);
      
        DIFF = System.currentTimeMillis() - TIME;
        System.out.println("    Q1 COMP TIME = "+DIFF+"ms");
        TIME = System.currentTimeMillis();
        
        Parameter<Integer> p1 = q.getParameter("startId", Integer.class);
        q.setParameter(p1, Integer.parseInt(from));
        Parameter<Integer> p2 = q.getParameter("endId", Integer.class);
        q.setParameter(p2, Integer.parseInt(to));
        Parameter<Integer> p3 = q.getParameter("regionId", Integer.class);
        q.setParameter(p3, regionId);
        
        List<ProductTypeTransitCumulativeSeriesSales> result = new ArrayList<>();
        List<Object[]> resultList = q.getResultList();
        
        DIFF = System.currentTimeMillis() - TIME;
        System.out.println("    Q1 TIME = "+DIFF+"ms");
        TIME = System.currentTimeMillis();
               
        for (Object[] o : resultList) {
            ProductTypeTransitCumulativeSeriesSales t = new ProductTypeTransitCumulativeSeriesSales();
            t.setCost((Double)o[0]);
            t.setSales((Double) o[1]);
            t.setUnits((Long) o[2]);
            ProductType pt = em.find(ProductType.class, o[3]);
            t.setProductType(pt);
            result.add(t);
        }
        
        //building sales range
        HashMap<ProductType, List<Double>> seriesGenerator = new HashMap<>();
        
        TypedQuery<Object[]> q2 = em.createQuery(REGION_TYPE_RANGE_QUERY, Object[].class);
        p1 = q2.getParameter("startId", Integer.class);
        p2 = q2.getParameter("endId", Integer.class);
        p3 = q2.getParameter("regionId", Integer.class);
        q2.setParameter(p1, Integer.parseInt(from));
        q2.setParameter(p2, Integer.parseInt(to));
        q2.setParameter(p3, regionId);
        resultList = q2.getResultList();
        
        DIFF = System.currentTimeMillis() - TIME;
        System.out.println("    Q2 TIME = "+DIFF+"ms");
        
        for (Object[] o : resultList) {
            ProductType pt = em.find(ProductType.class, o[1]);
            if (!seriesGenerator.containsKey(pt)){
                seriesGenerator.put(pt, new ArrayList<>());
            }            
            seriesGenerator.get(pt).add(((Long)o[0]).doubleValue());
        }
        for (ProductTypeTransitCumulativeSeriesSales tcs : result){
            tcs.setSeries(seriesGenerator.get(tcs.getProductType()));
        }
        
        DIFF = System.currentTimeMillis() - START_TIME;
        System.out.println("    TOTAL TIME = "+DIFF+"ms");

        return result;
    }
}
