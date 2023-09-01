module org.simulation {
    requires javafx.fxml;
    requires javafx.controls;

    opens org.simulation to javafx.fxml;

    exports org.simulation;
}

