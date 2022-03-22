package ru.click.webtikadocsextractor.entity;

import lombok.Data;
import ru.click.webtikadocsextractor.constants.QueueStatus;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="queue")
@Data
public class QueueEntity {

    @Id
    @GeneratedValue //(strategy = GenerationType.IDENTITY)
    @Column(name ="id")
    private UUID id;

    @Column(name="rel_file_dest")
    private String relFileDestination;

    @Column(name = "status")
    private QueueStatus status;

    @Column(name="error_message")
    private String errorMessage;

    @Column(name="create_dt")
    private Date createTime;

    @Column(name="processing_start_dt")
    private Date startTime;

    @Column(name="processing_finish_dt")
    private Date finishTime;

    @Column(name = "ocr")
    private boolean ocr;

}
