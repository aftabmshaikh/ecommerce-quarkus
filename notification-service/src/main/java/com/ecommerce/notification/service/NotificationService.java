package com.ecommerce.notification.service;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class);

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    Mailer mailer;

    @Transactional
    public void sendEmailNotification(String to, String subject, String content, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .recipientEmail(to)
                .subject(subject)
                .content(content)
                .type(type)
                .sent(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        notificationRepository.persist(notification);
        LOG.infof("Notification saved with ID: %s", notification.getId());
    }

    @Scheduled(every = "60s")
    @Transactional
    public void processPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findBySentFalse();
        LOG.infof("Processing %d pending notifications", pendingNotifications.size());
        
        for (Notification notification : pendingNotifications) {
            try {
                Mail mail = Mail.withText(notification.getRecipientEmail(), notification.getSubject(), notification.getContent());
                mailer.send(mail);
                
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.persist(notification);
                
                LOG.infof("Notification sent to: %s", notification.getRecipientEmail());
            } catch (Exception e) {
                LOG.errorf(e, "Failed to send notification: %s", notification.getId());
            }
        }
    }
    
    public long getPendingNotificationCount(String email) {
        return notificationRepository.countByRecipientEmailAndSentFalse(email);
    }
}
