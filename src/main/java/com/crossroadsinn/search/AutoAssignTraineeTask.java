package com.crossroadsinn.search;

import com.crossroadsinn.squadassigning.SelectionAssigner;
import javafx.concurrent.Task;

public class AutoAssignTraineeTask extends Task<SelectionAssigner> {

    private final SelectionAssigner selectionAssigner;

    public AutoAssignTraineeTask(SelectionAssigner selectionAssigner) {
        this.selectionAssigner = selectionAssigner;
    }

    @Override
    protected SelectionAssigner call() {
        return selectionAssigner.assignToSquads();
    }

}
