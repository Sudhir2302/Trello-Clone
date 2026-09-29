package com.example.TrelloClone.Service;

public class NoUndoHistoryException extends IllegalStateException {
    public NoUndoHistoryException(long taskID) {
        super("No unapplied history found for taskId: " + taskID);
    }
}
