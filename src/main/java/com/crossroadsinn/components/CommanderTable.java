package com.crossroadsinn.components;

import com.crossroadsinn.settings.Role;
import com.crossroadsinn.settings.Roles;
import com.crossroadsinn.settings.Settings;
import com.crossroadsinn.signups.Commander;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a table of commanders where each column is populated with
 * a checkbox and linked to a role allowing enabling and disabling
 * that role for the commander of the row.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class CommanderTable extends TableView<Commander> {

    public CommanderTable(ObservableList<Commander> items) {
        super(items);
        init();
        getStylesheets().add(Settings.getAssetFilePath("style/table-custom.css"));
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Creates the columns and associates the cell factories.
     */
    private void init() {
        TableColumn<Commander, String> checkAll = new TableColumn<>();
        checkAll.setCellValueFactory(f -> f.getValue().getChosenRolesJoined());
        // TODO make this hacky check all work
        checkAll.setCellFactory(p -> new RoleCell(null, true));
        checkAll.setPrefWidth(40);

        TableColumn<Commander, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		getColumns().addAll(checkAll, name);
		
		for(Role role:Roles.getAllRoles()) {
			if (!role.getCommRole()) continue;
			TableColumn<Commander, String> col = new TableColumn<>(role.getRoleName());
            col.setCellValueFactory(f -> f.getValue().getChosenRolesJoined());
			col.setCellFactory(p -> new RoleCell(role, false));
			getColumns().add(col);
		}
    }

}