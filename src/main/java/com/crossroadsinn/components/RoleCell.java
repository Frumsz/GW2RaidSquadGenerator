package com.crossroadsinn.components;

import com.crossroadsinn.settings.Role;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import com.crossroadsinn.signups.Commander;

import java.util.ArrayList;

/**
 * A table cell for a CommanderTable that represents a role for that commander.
 * The checkbox it contains can be selected/deselected to (un)choose that role
 * for the associated commander.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class RoleCell extends TableCell<Commander, String> {

    CheckBox checkBox = new CheckBox();
    HBox container = new HBox();
    Role role;

    // TODO fix the "isAllRoles" hack
    boolean isAllRoles;

    public RoleCell(Role role, boolean isAllRoles) {
        super();
        this.role = role;
        checkBox.setPadding(new Insets(0, 5, 0, 5));

        container.getChildren().add(checkBox);
        container.setAlignment(Pos.CENTER);
        this.isAllRoles = isAllRoles;

        checkBox.setOnAction(e -> {
            if (getTableRow() != null) {
                Commander player = getTableRow().getItem();
                if (checkBox.isSelected()) {
                    if(this.isAllRoles) {
                        player.setAllChosenRoles(new ArrayList<>(player.getRoles()));
                    } else {
                        player.addChosenRole(role);
                    }
                } else {
                    if (isAllRoles) {
                        player.setAllChosenRoles(new ArrayList<>());
                    } else {
                        player.removeChosenRole(role);
                    }
                }
            }
        });
    }

    @Override
    protected void updateItem(String rolesSelected, boolean empty) {
        super.updateItem(rolesSelected, empty);
        if (empty || getTableRow() == null) {
            setGraphic(null);
        } else {
            setGraphic(container);
            Commander player = getTableRow().getItem();
            if (player != null) {
                checkBox.setDisable(!isAllRoles && !player.getRoles().contains(role));
                checkBox.setSelected(player.getChosenRoles().contains(role) || (isAllRoles && player.getChosenRoles().size() == player.getRoles().size()));
            }
        }
        setText(null);
    }
}
