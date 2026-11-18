package com.dmytronik.taskmanager.model;

import java.util.EnumSet;
import java.util.Set;

public enum TaskStatus {
    TODO, IN_PROGRESS, DONE, CANCELLED;

    public static final Set<TaskStatus> ACTIVE = EnumSet.of(TODO, IN_PROGRESS);
}