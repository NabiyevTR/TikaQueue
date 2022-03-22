package ru.click.webtikadocsextractor.constants;

import lombok.Getter;

public enum QueueStatus {
    WAITING(0, "Waiting"),
    PROCESSING(1, "Processing"),
    ERROR(2, "Error"),
    PROCESSED(3, "Processed"),
    DELETING(4, "Deleting");

    @Getter
    private int code;
    @Getter
    private String status;

    QueueStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }
}
