/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
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
package com.javafx.experiments.dataapp.client.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.javafx.experiments.dataapp.client.DataApplication;
import java.util.Date;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class HeatMapClient {
    private final WebTarget rootTarget;
    private final Client client;

    public HeatMapClient() {
        client = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class);
        rootTarget = client.target(DataApplication.SERVER_URI).path("com.javafx.experiments.dataapp.model.heatmap");
    }

    public <T> T getDateRange(Class<T> responseType) {
        WebTarget target = rootTarget.path("range");
        return target.request(MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T getHeatMap(Class<T> responseType, Date date1, Date date2) {
        WebTarget target = rootTarget.path("base").path(Long.toString(date1.getTime())).path(Long.toString(date2.getTime()));
        return target.request(MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T getProductTypeHeatMap(Class<T> responseType, Date date1, Date date2, Integer productTypeId1, Integer productTypeId2) {
        WebTarget target = rootTarget
                .path("producttype")
                .path(Long.toString(date1.getTime()))
                .path(Long.toString(date2.getTime()))
                .path(productTypeId1.toString())
                .path(productTypeId2.toString());
        return target.request(MediaType.APPLICATION_JSON).get(responseType);
    }

    public void close() {
        client.close();
    }

}
