package edu.chenjin.library.service;

import edu.chenjin.library.common.BizException;
import edu.chenjin.library.common.PageResult;
import edu.chenjin.library.common.UserContext;
import edu.chenjin.library.dto.NotificationView;
import edu.chenjin.library.entity.Notification;
import edu.chenjin.library.entity.User;
import edu.chenjin.library.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public SseEmitter connect() {
        User user = UserContext.require();
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(user.getId(), key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(user.getId(), emitter));
        emitter.onTimeout(() -> removeEmitter(user.getId(), emitter));
        emitter.onError(error -> removeEmitter(user.getId(), emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of(
                    "userId", user.getId(),
                    "message", "消息推送已连接"
            )));
            emitter.send(SseEmitter.event().name("unread").data(unreadCount(user.getId())));
        } catch (IOException e) {
            removeEmitter(user.getId(), emitter);
        }
        return emitter;
    }

    @Transactional
    public NotificationView sendToUser(Long userId, String title, String content, String type, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setLink(link);
        notification.setReadFlag(false);
        Notification saved = notificationRepository.save(notification);
        NotificationView view = NotificationView.from(saved);
        push(userId, view);
        return view;
    }

    public PageResult<NotificationView> page(int page, int size) {
        User user = UserContext.require();
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.min(Math.max(size, 1), 50);
        Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(pageIndex, pageSize));
        return PageResult.of(result.map(NotificationView::from));
    }

    public List<NotificationView> latest() {
        User user = UserContext.require();
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationView::from).toList();
    }

    public long unreadCount() {
        return unreadCount(UserContext.require().getId());
    }

    private long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }

    @Transactional
    public void markRead(Long id) {
        User user = UserContext.require();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "通知不存在"));
        if (!notification.getUserId().equals(user.getId())) {
            throw new BizException(403, "无权操作该通知");
        }
        notification.setReadFlag(true);
        notificationRepository.save(notification);
        pushUnread(user.getId());
    }

    @Transactional
    public void markAllRead() {
        User user = UserContext.require();
        notificationRepository.markAllRead(user.getId());
        pushUnread(user.getId());
    }

    private void push(Long userId, NotificationView view) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list != null) {
            for (SseEmitter emitter : list) {
                try {
                    synchronized (emitter) {
                        emitter.send(SseEmitter.event()
                                .name("notification")
                                .id(String.valueOf(view.id()))
                                .data(view));
                    }
                } catch (Exception e) {
                    removeEmitter(userId, emitter);
                }
            }
        }
        pushUnread(userId);
    }

    private void pushUnread(Long userId) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list == null) {
            return;
        }
        long unread = unreadCount(userId);
        for (SseEmitter emitter : list) {
            try {
                synchronized (emitter) {
                    emitter.send(SseEmitter.event().name("unread").data(unread));
                }
            } catch (Exception e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    @Scheduled(fixedRate = 25000)
    public void heartbeat() {
        emitters.forEach((userId, list) -> {
            for (SseEmitter emitter : list) {
                try {
                    synchronized (emitter) {
                        emitter.send(SseEmitter.event().comment("keep-alive"));
                    }
                } catch (Exception e) {
                    removeEmitter(userId, emitter);
                }
            }
        });
        log.debug("SSE 心跳完成，在线用户数：{}", emitters.size());
    }
}
