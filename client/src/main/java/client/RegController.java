package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegController {
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nickField;
    Controller controller;

    String regTitle = "Registration";
    String changeTitle = "Change nick name";

    public void clickOk(ActionEvent actionEvent) {

        String title = ((Stage) loginField.getScene().getWindow()).getTitle();
        if (title.equals(regTitle)) {
            controller.tryRegistr(loginField.getText(),
                    passwordField.getText(), nickField.getText());
        }
        if (title.equals(changeTitle)) {
            controller.tryChangeNick(loginField.getText(),
                    passwordField.getText(), nickField.getText());
        }
    }
}
