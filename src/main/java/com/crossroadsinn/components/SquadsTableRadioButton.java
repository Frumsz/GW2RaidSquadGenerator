package com.crossroadsinn.components;

import com.crossroadsinn.settings.Squad;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

public class SquadsTableRadioButton extends TableCell<Squad, Boolean> {
    RadioButton radioButton = new RadioButton();
    HBox container = new HBox();

    public SquadsTableRadioButton() {
        super();
        radioButton.setPadding(new Insets(0, 5, 0, 5));
        container.getChildren().add(radioButton);
        container.setAlignment(Pos.CENTER);

        radioButton.setOnAction(e -> {
            if (getTableRow() != null) {
                Squad squad = getTableRow().getItem();
                // Only one can be enabled, disable the rest
                getTableView().getItems().forEach(p -> p.setEnabled(false));
                // And enable this one, if if you click to disable, you are not allowed to do so
                squad.setEnabled(true);
            }
        });
    }

    @Override
    protected void updateItem(Boolean enabled, boolean empty) {
        super.updateItem(enabled, empty);
        if (empty || getTableRow() == null) {
            setGraphic(null);
        } else {
            setGraphic(container);
            Squad squad = getTableRow().getItem();
            if (squad != null) {
                radioButton.setSelected(squad.getEnabled().getValue());
            }
        }
        setText(null);
    }
}