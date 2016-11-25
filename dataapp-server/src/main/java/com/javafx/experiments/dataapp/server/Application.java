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
package com.javafx.experiments.dataapp.server;

import com.javafx.experiments.dataapp.simulation.DailySalesGenerator;
import com.javafx.experiments.dataapp.simulation.SalesSimulator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.ApplicationPath;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.DateBuilder.IntervalUnit.MILLISECOND;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static  org.quartz.TriggerBuilder.newTrigger;

@WebListener
@ApplicationPath("resources")
public class Application extends ResourceConfig implements ServletContextListener, EntityManagerFactoryHolder {

    private static final String PU_NAME = "DataAppLibraryPU";
    private static EntityManagerFactory entityManagerFactory;
    private static Scheduler scheduler;

    public Application() {
        packages("com.javafx.experiments.dataapp.server.service");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(Application.this).to(EntityManagerFactoryHolder.class);
                bindFactory(EMFactory.class).to(EntityManager.class).in(RequestScoped.class);
            }
        });
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME);
        initDatabaseIfNeeded();

        System.out.println("Starting simulation");
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.scheduleJob(
                    newJob(GeneralJob.class)
                        .usingJobData(
                                new JobDataMap(
                                        Collections.singletonMap("runnable", new SalesSimulator(entityManagerFactory.createEntityManager()))))
                        .build(),
                    newTrigger()
                        .startAt(futureDate(SalesSimulator.TIME_BETWEEN_SALES, MILLISECOND))
                        .withSchedule(simpleSchedule()
                                .withIntervalInMilliseconds(SalesSimulator.TIME_BETWEEN_SALES)
                                .repeatForever())
                        .build());

            scheduler.scheduleJob(
                    newJob(GeneralJob.class)
                        .usingJobData(
                                new JobDataMap(
                                        Collections.singletonMap("runnable", new DailySalesGenerator(entityManagerFactory.createEntityManager()))))
                        .build(),
                    newTrigger()
                        .startAt(futureDate(1000, MILLISECOND))
                        .withSchedule(simpleSchedule()
                            .withIntervalInHours(24)
                            .repeatForever())
                        .build());
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (scheduler != null) {
                scheduler.shutdown(true);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    private static void initDatabaseIfNeeded() {
        EntityManager em = entityManagerFactory.createEntityManager();
        BigInteger count = (BigInteger)em.createNativeQuery("SELECT COUNT(*) AS count FROM information_schema.tables WHERE table_name = 'ADDRESS'").getSingleResult();

        if (count.intValue() > 0) {
            System.out.println("Database already initialized");
            em.close();
            return;
        }

        System.out.println("Initializing database");

        Map<String, String> map = new HashMap<>();
        map.put("javax.persistence.schema-generation.database.action", "create");
        Persistence.generateSchema(PU_NAME, map);
        DataAppLoader.loadAll(em);
        em.close();
    }
}
