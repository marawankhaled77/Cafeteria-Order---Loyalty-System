module main {
    requires javafx.controls;
    requires javafx.fxml;

    opens cafeteria to javafx.fxml;
    exports  cafeteria;
}