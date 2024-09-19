module edu.erciyes.vp.hexgame {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.erciyes.vp.hexgame to javafx.fxml;
    exports edu.erciyes.vp.hexgame;
}