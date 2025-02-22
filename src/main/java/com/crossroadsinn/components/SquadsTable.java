package com.crossroadsinn.components;

import com.crossroadsinn.settings.Squad;
import com.crossroadsinn.settings.Settings;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Creates a table for all available SquadTypes to select and set an mount
 * @author moon
 * @version 1.1
 */
public class SquadsTable extends TableView<Squad> {

    public SquadsTable(ObservableList<Squad> items) {
        super(items);
        init();
        getStylesheets().add(Settings.getAssetFilePath("style/table-custom.css"));
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Creates the columns and associates the cell factories.
     */
    private void init() {
		TableColumn<Squad, String> name = new TableColumn<>("Name");
		name.setCellValueFactory(new PropertyValueFactory<>("name"));
		getColumns().add(name);
		
        TableColumn<Squad, Boolean> check = new TableColumn<>("Enable");
		check.setCellValueFactory(p -> p.getValue().getEnabled());
        check.setCellFactory(p -> new SquadsTableRadioButton());
        check.setPrefWidth(40);
		getColumns().add(check);
	
    }

}


	