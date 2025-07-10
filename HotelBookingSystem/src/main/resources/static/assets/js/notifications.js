/* notifications.js - handles real-time and REST notifications in header dropdown */

(function() {
    if (!window.currentUserId) {
        console.warn('notifications.js: currentUserId not set, aborting');
        return;
    }

    const currentUserId = window.currentUserId;
    let notifications = [];

    // --- Utility helpers ---
    const qs = (sel) => document.querySelector(sel);

    function formatTime(ts) {
        const date = new Date(ts);
        const now  = new Date();
        const diff = now - date;
        const mins = Math.floor(diff / 60000);
        const hrs  = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);
        if (mins < 1)  return 'Vừa xong';
        if (mins < 60) return `${mins} phút trước`;
        if (hrs  < 24) return `${hrs} giờ trước`;
        if (days < 7) return `${days} ngày trước`;
        return date.toLocaleDateString('vi-VN');
    }

    function showLoading(show) {
        const el = qs('#notificationLoading');
        if (el) el.style.display = show ? 'block' : 'none';
    }

    function updateUnread(count) {
        const badge = qs('#notificationBadge');
        const text  = qs('#notificationCountText');
        if (!badge || !text) return;
        if (count > 0) {
            badge.style.display = 'inline';
            badge.textContent = count > 99 ? '99+' : count;
            text.textContent  = `(${count})`;
        } else {
            badge.style.display = 'none';
            text.textContent = '(0)';
        }
    }

    function renderList() {
        const list = qs('#notificationList');
        const empty = qs('#notificationEmpty');
        if (!list) return;
        list.querySelectorAll('li:not(#notificationLoading):not(#notificationEmpty)')
            .forEach(li => li.remove());
        if (!notifications.length) {
            if (empty) empty.style.display = 'block';
            return;
        }
        if (empty) empty.style.display = 'none';
        notifications.forEach(n => {
            const li = document.createElement('li');
            li.innerHTML = `
                <a href="${n.action_url || '#'}" class="list-group-item list-group-item-action rounded ${!n.is_read ? 'notif-unread' : ''} border-0 mb-1 p-3" data-id="${n.notification_id}">
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <h6 class="mb-2"><i class="${n.icon || 'bi bi-bell'} me-2"></i>${n.title}</h6>
                            <p class="mb-1 small">${n.message}</p>
                            <small class="text-muted">${formatTime(n.created_at)}</small>
                        </div>
                        ${!n.is_read ? '<span class="badge bg-primary ms-2">Mới</span>' : ''}
                    </div>
                </a>`;
            li.querySelector('a').addEventListener('click', () => {
                if (!n.is_read) markRead(n.notification_id);
            });
            list.appendChild(li);
        });
    }

    // --- API calls ---
    function fetchAll() {
        showLoading(true);
        Promise.all([
            fetch('/api/notifications?page=0&size=10').then(r => r.json()),
            fetch('/api/notifications/unread-count').then(r => r.json())
        ]).then(([listResp, countResp]) => {
            notifications = listResp.content || [];
            renderList();
            updateUnread(countResp.count || 0);
        }).catch(err => console.error('Notification load error', err))
          .finally(() => showLoading(false));
    }

    function markRead(id) {
        fetch(`/api/notifications/${id}/read`, {method:'POST'})
            .then(() => fetchAll())
            .catch(e => console.error('markRead error', e));
    }

    function markAllRead() {
        fetch('/api/notifications/mark-all-read', {method:'POST'})
            .then(() => { notifications.forEach(n=>n.is_read=true); renderList(); updateUnread(0); })
            .catch(e => console.error('markAllRead error', e));
    }

    // --- WebSocket optional ---
    function setupWS() {
        if (!window.SockJS || !window.StompJs) return; // libs not loaded
        try {
            const sock = new SockJS('/ws');
            const client = StompJs.Stomp.over(sock);
            client.connect({}, () => {
                client.subscribe(`/topic/notifications.${currentUserId}`, msg => {
                    const data = JSON.parse(msg.body);
                    if (data.type === 'new_notification') {
                        notifications.unshift(data.notification);
                        renderList();
                        updateUnread((parseInt(qs('#notificationBadge')?.textContent)||0)+1);
                    }
                    if (data.type === 'notification_deleted') {
                        const idx = notifications.findIndex(n=>n.notification_id===data.id);
                        if (idx !== -1) {
                            notifications.splice(idx,1);
                            renderList();
                        }
                    }
                    if (data.type === 'unread_count_update') {
                        updateUnread(data.count);
                    }
                });
            });
        } catch(e) { console.warn('WS setup failed',e); }
    }

    // --- Event listeners ---
    function init() {
        qs('#notificationDropdownBtn')?.addEventListener('click', fetchAll);
        qs('#markAllReadBtn')?.addEventListener('click', e => {e.preventDefault(); markAllRead();});
        fetchAll();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // lazy-load WS libs after 1s
    setTimeout(() => {
        const s1 = document.createElement('script');
        s1.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
        s1.onload = () => {
            const s2 = document.createElement('script');
            s2.src = 'https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js';
            s2.onload = setupWS;
            document.head.appendChild(s2);
        };
        document.head.appendChild(s1);
    }, 1000);
})(); 