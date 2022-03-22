package ru.click.webtikadocsextractor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.click.webtikadocsextractor.constants.QueueStatus;
import ru.click.webtikadocsextractor.entity.QueueEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QueueRepository extends JpaRepository<QueueEntity, UUID> {

    Optional<QueueEntity> findFirstByStatusOrderByCreateTimeAsc(QueueStatus queueStatus);

    Optional<QueueEntity> findFirstByStatusAndOcrOrderByCreateTimeAsc(QueueStatus queueStatus, boolean ocr);

    Optional<List<QueueEntity>> findQueueEntityByStatusAndFinishTimeIsNotNullAndFinishTimeIsBefore(QueueStatus queueStatus, Date date);

    Optional<List<QueueEntity>> findQueueEntityByStatusAndStartTimeIsNotNullAndStartTimeIsBefore(QueueStatus queueStatus, Date date);

}
