package com.deal4u.fourplease.domain.notification.pushnotification.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxy;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PushNotification extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @AttributeOverrides(
            value = {
                    @AttributeOverride(name = "memberId", column = @Column(nullable = false))
            }
    )
    @Embedded
    private Receiver receiver;

    @Column(nullable = false, columnDefinition = "json")
    @Type(JsonType.class)
    private Map<String, Object> message;

    private Boolean clicked;

    @Builder
    private PushNotification(Long memberId, String type, Map<String, Object> message) {
        this.receiver = Receiver.of(memberId);
        this.type = type;
        this.message = message;
        this.clicked = false;
    }

    public boolean isSameReceiver(Receiver receiver) {
        return this.receiver.equals(receiver);
    }

    public void click() {
        clicked = true;
    }

    public boolean isClicked() {
        return clicked;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> effectiveClass = o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer()
                .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != effectiveClass) {
            return false;
        }
        PushNotification that = (PushNotification) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer()
                .getPersistentClass().hashCode() : getClass().hashCode();
    }
}
