module com.example.scrill {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign;
    requires java.desktop;

    requires jdk.httpserver;
    requires java.net.http;
    requires org.json;
    requires jaudiotagger;

    opens com.example.scrill to javafx.fxml;
    exports com.example.scrill;
}