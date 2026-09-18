package com.realestate.due_diligence_agent.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realestate.due_diligence_agent.entity.Notification;
import com.realestate.due_diligence_agent.entity.User;
import com.realestate.due_diligence_agent.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getNotificationsForUser(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public long getUnreadCount(User user) {
        if (user == null || user.getId() == null) {
            return 0;
        }
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public Notification markAsRead(Long notificationId, User user) {
        if (user == null || user.getId() == null) {
            throw new SecurityException("User authentication required");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied: Notification does not belong to the current user");
        }

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        if (user == null || user.getId() == null) {
            throw new SecurityException("User authentication required");
        }
        notificationRepository.markAllAsReadByUserId(user.getId());
    }

    @Transactional
    public Notification createNotification(User user, String title, String message) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when creating notification");
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }
}
