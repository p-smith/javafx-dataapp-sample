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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.ApplicationPath;

import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@WebListener
@ApplicationPath("resources")
public class Application extends ResourceConfig implements ServletContextListener {

    private static final String PU_NAME = "DataAppLibraryPU";
    private EntityManagerFactory entityManagerFactory;
    private ScheduledThreadPoolExecutor scheduler;

    public Application() {
        entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME);
        packages("com.javafx.experiments.dataapp.server.service");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(entityManagerFactory).to(EntityManagerFactory.class);
                bindFactory(EntityManagerSource.class).to(EntityManager.class).in(RequestScoped.class);
            }
        });
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initDatabaseIfNeeded();

        System.out.println("Starting simulation");

        scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.scheduleWithFixedDelay(
                new SalesSimulator(entityManagerFactory.createEntityManager()),
                SalesSimulator.TIME_BETWEEN_SALES,
                SalesSimulator.TIME_BETWEEN_SALES,
                TimeUnit.MILLISECONDS
        );
        scheduler.scheduleWithFixedDelay(() -> {
            var em = entityManagerFactory.createEntityManager();
            new DailySalesGenerator(em).run();
            em.close();
        }, 1000, Duration.ofHours(24).toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdown();

            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    private void initDatabaseIfNeeded() {
        var em = entityManagerFactory.createEntityManager();
        var count = (BigInteger)em.createNativeQuery("SELECT COUNT(*) AS count FROM information_schema.tables WHERE table_name = 'ADDRESS'").getSingleResult();

        if (count.intValue() > 0) {
            System.out.println("Database already initialized");
            em.close();
            return;
        }

        System.out.println("Initializing database");

        var map = new HashMap<String, String>();
        map.put("javax.persistence.schema-generation.database.action", "create");
        Persistence.generateSchema(PU_NAME, map);
        DataAppLoader.loadAll(em);
        em.close();
    }
}
