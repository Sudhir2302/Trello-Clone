package com.example.TrelloClone.Models.Task;

public enum TaskStatus {
    TODO{
        public TaskStatus undo() { return TODO;}

        public TaskStatus transition() { return DOING;}
    },
    DOING{
        public TaskStatus undo() { return TODO;}

        public TaskStatus transition() { return DONE;}
    },
    DONE{
        public TaskStatus undo() { return DOING;}

        public TaskStatus transition() { return TODO;}
    };

    public abstract TaskStatus undo();

    public abstract TaskStatus transition();


}
