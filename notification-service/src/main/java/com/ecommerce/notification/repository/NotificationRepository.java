package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.Notification;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationRepository implements PanacheRepository<Notification> {

    public List<Notification> findByRecipientEmailAndSentFalse(String email) {
        return list("recipientEmail = ?1 and sent = false", email);
    }

    public List<Notification> findBySentFalse() {
        return list("sent = false");
    }

    public long countByRecipientEmailAndSentFalse(String email) {
        return count("recipientEmail = ?1 and sent = false", email);
    }
}
