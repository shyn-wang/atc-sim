module com.example.wangatc {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.wangatc to javafx.fxml;
    exports com.example.wangatc;
}