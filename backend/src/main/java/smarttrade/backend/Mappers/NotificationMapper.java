package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.notification.NotificationResponse;
import smarttrade.backend.entities.notification.NotificationEntity;

@Component
public class NotificationMapper {

    public NotificationResponse map(NotificationEntity entity){

        return NotificationResponse.builder()
                .id(entity.getId())
                .type(entity.getType().name())
                .resourceType(entity.getResourceType().name())
                .resourceId(entity.getResourceId())
                .actorId(
                        entity.getActor()==null
                                ?null
                                :entity.getActor().getUserId()
                )
                .actorName(
                        entity.getActor()==null
                                ?null
                                :entity.getActor().getName()
                )
                .message(entity.getMessage())
                .read(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }

}