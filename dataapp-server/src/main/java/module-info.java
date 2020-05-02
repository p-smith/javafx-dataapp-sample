open module javafx.dataapp.sample.server {
    requires javafx.dataapp.sample.library;
    requires hk2.api;
    requires jersey.common;
    requires jersey.server;
    requires java.persistence;
    requires javax.servlet.api;
    requires java.ws.rs;
    requires jakarta.inject;

    exports com.javafx.experiments.dataapp.server;
    exports com.javafx.experiments.dataapp.server.service;
}