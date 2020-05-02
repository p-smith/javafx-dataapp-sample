module javafx.dataapp.sample.library {
    requires static java.persistence;
    requires static java.xml.bind;

    exports com.javafx.experiments.dataapp.model;
    exports com.javafx.experiments.dataapp.model.transit;
}