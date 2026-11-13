(function () {
  const { createApp } = Vue;
  const API = window.LibraryApi;

  const PALETTES = [
    'linear-gradient(145deg, #1f4e5c, #12333c)',
    'linear-gradient(145deg, #7b4432, #4d2a20)',
    'linear-gradient(145deg, #536b45, #31432a)',
    'linear-gradient(145deg, #6b4f6f, #44304a)',
    'linear-gradient(145deg, #8a6a35, #59401c)',
    'linear-gradient(145deg, #375d72, #203f50)',
    'linear-gradient(145deg, #8a4f50, #5c3032)',
    'linear-gradient(145deg, #3f6b65, #244540)',
    'linear-gradient(145deg, #665b46, #413a2b)',
    'linear-gradient(145deg, #4a567d, #2d3656)'
  ];

  const ROLE_COLORS = ['#b04b4b', '#38627d', '#2b7355', '#c79a4e'];

  function emptyPage(size) {
    return { records: [], total: 0, page: 1, size: size || 10, pages: 0 };
  }

  function emptyBookForm() {
    return {
      title: '',
      author: '',
      type: '',
      publisher: '',
      isbn: '',
      days: 30,
      price: 0,
      totalCopies: 1,
      status: 'ACTIVE',
      introduction: '',
      shelfNo: ''
    };
  }

  function toBookFormData(book) {
    return {
      title: book.title || '',
      author: book.author || '',
      type: book.type || '',
      publisher: book.publisher || '',
      isbn: book.isbn || '',
      days: book.days || 30,
      price: book.price == null ? 0 : Number(book.price),
      totalCopies: book.totalCopies == null ? 0 : book.totalCopies,
      status: book.status || 'ACTIVE',
      introduction: book.introduction || '',
      shelfNo: book.shelfNo || ''
    };
  }

  function pad(value) {
    return String(value).padStart(2, '0');
  }

  const app = createApp({
    data() {
      const savedToken = localStorage.getItem('library_token') || '';
      return {
        apiBase: window.APP_CONFIG.apiBase,
        booting: !!savedToken,
        token: savedToken,
        user: null,
        authMode: 'login',
        authLoading: false,
        loginForm: { name: '', password: '' },
        registerForm: {
          name: '', password: '', phone: '', role: 'STUDENT', major: '',
          grade: '', college: '', title: '', area: '', duty: '', email: ''
        },
        sidebarOpen: false,
        activeView: 'dashboard',
        globalSearch: '',
        viewMeta: {
          dashboard: { eyebrow: 'OVERVIEW', title: '数据总览' },
          books: { eyebrow: 'COLLECTION', title: '馆藏目录' },
          users: { eyebrow: 'READERS', title: '用户管理' },
          loans: { eyebrow: 'CIRCULATION', title: '借阅记录' },
          reservations: { eyebrow: 'RESERVATION', title: '图书预约' },
          stats: { eyebrow: 'INSIGHTS', title: '数据统计' },
          logs: { eyebrow: 'AUDIT', title: '操作日志' },
          profile: { eyebrow: 'ACCOUNT', title: '个人资料' }
        },

        bookTypes: [],
        bookFilters: { keyword: '', type: '', status: '', onlyAvailable: false },
        bookPage: 1,
        bookSize: 10,
        books: emptyPage(10),

        userFilters: { keyword: '', role: '', status: '', major: '' },
        userPage: 1,
        userSize: 10,
        users: emptyPage(10),

        loanScope: 'all',
        loanFilters: { keyword: '', status: '', start: '', end: '' },
        loanPage: 1,
        loanSize: 10,
        loans: emptyPage(10),

        reservationScope: 'all',
        reservationFilters: { keyword: '', status: '' },
        reservationPage: 1,
        reservationSize: 10,
        reservations: emptyPage(10),

        logFilters: { module: '', keyword: '', success: '', start: '', end: '' },
        logPage: 1,
        logSize: 10,
        logs: emptyPage(10),

        dashboard: {
          overview: null,
          recentLoans: [],
          popular: [],
          popularUnit: '次',
          myActive: 0,
          myDueSoon: 0,
          myReservations: 0,
          totalBooks: 0
        },
        stats: {
          loading: false,
          overview: null,
          monthly: [],
          bookTypes: [],
          loanTypes: [],
          topBooks: [],
          roles: [],
          recent: []
        },
        roleColors: ROLE_COLORS,

        notifications: [],
        unreadCount: 0,
        notificationPanelOpen: false,
        sseConnected: false,
        eventSource: null,
        pollTimer: null,
        unauthorizedNotified: false,

        bookDetail: { show: false, loading: false, data: null },
        bookForm: {
          show: false,
          mode: 'create',
          id: null,
          saving: false,
          data: emptyBookForm(),
          existing: [],
          coverFile: null,
          coverPreview: '',
          galleryFiles: []
        },
        userDetail: { show: false, data: null },
        confirmState: { show: false, title: '', message: '', confirmText: '确定', tone: 'primary', action: null },
        lightbox: { show: false, images: [], index: 0 },
        toasts: [],
        toastSeq: 0
      };
    },

    computed: {
      isStaff() {
        return !!this.user && (this.user.role === 'ADMIN' || this.user.role === 'OPERATOR');
      },
      isAdmin() {
        return !!this.user && this.user.role === 'ADMIN';
      },
      canManageBooks() {
        return this.isStaff;
      },
      userInitial() {
        return this.user && this.user.name ? this.user.name.slice(0, 1).toUpperCase() : '?';
      },
      currentViewMeta() {
        return this.viewMeta[this.activeView] || this.viewMeta.dashboard;
      },
      navGroups() {
        const groups = [
          {
            label: '工作台',
            items: [{ id: 'dashboard', label: '数据总览', icon: 'grid' }]
          },
          {
            label: '馆藏服务',
            items: [
              { id: 'books', label: '馆藏目录', icon: 'book' },
              { id: 'loans', label: this.isStaff ? '借阅管理' : '我的借阅', icon: 'swap' },
              { id: 'reservations', label: this.isStaff ? '预约管理' : '我的预约', icon: 'bookmark' }
            ]
          }
        ];
        if (this.isStaff) {
          groups.push({
            label: '运营分析',
            items: [{ id: 'stats', label: '数据统计', icon: 'chart' }]
          });
        }
        if (this.isAdmin) {
          groups.push({
            label: '系统管理',
            items: [
              { id: 'users', label: '用户管理', icon: 'users' },
              { id: 'logs', label: '操作日志', icon: 'history' }
            ]
          });
        }
        groups.push({
          label: '账户',
          items: [{ id: 'profile', label: '个人资料', icon: 'user' }]
        });
        return groups;
      },
      greeting() {
        const hour = new Date().getHours();
        if (hour < 6) return '夜深了';
        if (hour < 11) return '早上好';
        if (hour < 14) return '中午好';
        if (hour < 18) return '下午好';
        return '晚上好';
      },
      todayText() {
        const now = new Date();
        const weeks = ['日', '一', '二', '三', '四', '五', '六'];
        return now.getFullYear() + ' 年 ' + (now.getMonth() + 1) + ' 月 ' + now.getDate() + ' 日 · 星期' + weeks[now.getDay()];
      },
      dashboardCards() {
        if (this.isStaff) {
          const o = this.dashboard.overview || {};
          return [
            { label: '馆藏品种', value: o.totalBooks || 0, icon: 'book', tone: '', foot: '可借副本 ' + (o.availableCopies || 0) + ' 本' },
            { label: '注册用户', value: o.totalUsers || 0, icon: 'users', tone: 'gold', foot: '预约等待 ' + (o.waitingReservations || 0) + ' 位' },
            { label: '当前借阅', value: o.activeLoans || 0, icon: 'swap', tone: 'blue', foot: '累计借阅 ' + (o.totalLoans || 0) + ' 次' },
            { label: '超期未还', value: o.overdueLoans || 0, icon: 'alert', tone: 'red', foot: '请及时联系读者归还' }
          ];
        }
        return [
          { label: '我的借阅', value: this.dashboard.myActive, icon: 'swap', tone: '', foot: '即将到期 ' + this.dashboard.myDueSoon + ' 本' },
          { label: '我的预约', value: this.dashboard.myReservations, icon: 'bookmark', tone: 'gold', foot: '到书后实时提醒' },
          { label: '馆藏品种', value: this.dashboard.totalBooks, icon: 'book', tone: 'blue', foot: '在架图书均可检索' },
          { label: '借阅期限', value: '30', icon: 'calendar', tone: '', foot: '默认借阅天数（天）' }
        ];
      },
      statsCards() {
        const o = this.stats.overview || {};
        return [
          { label: '馆藏品种', value: o.totalBooks || 0, icon: 'book', tone: '', foot: '馆藏副本 ' + (o.totalCopies || 0) + ' 本' },
          { label: '注册用户', value: o.totalUsers || 0, icon: 'users', tone: 'gold', foot: '预约等待 ' + (o.waitingReservations || 0) + ' 位' },
          { label: '累计借阅', value: o.totalLoans || 0, icon: 'swap', tone: 'blue', foot: '当前在借 ' + (o.activeLoans || 0) + ' 本' },
          { label: '超期未还', value: o.overdueLoans || 0, icon: 'alert', tone: 'red', foot: '可借副本 ' + (o.availableCopies || 0) + ' 本' }
        ];
      },
      roleDonutStyle() {
        const roles = this.stats.roles || [];
        const total = roles.reduce(function (sum, item) { return sum + Number(item.value || 0); }, 0);
        if (!total) {
          return 'conic-gradient(#e9e3d8 0% 100%)';
        }
        let current = 0;
        const parts = roles.map((item, index) => {
          const start = current / total * 100;
          current += Number(item.value || 0);
          const end = current / total * 100;
          return ROLE_COLORS[index % ROLE_COLORS.length] + ' ' + start.toFixed(2) + '% ' + end.toFixed(2) + '%';
        });
        return 'conic-gradient(' + parts.join(', ') + ')';
      }
    },

    mounted() {
      window.addEventListener('library-unauthorized', this.handleUnauthorized);
      window.addEventListener('library-toast', this.onGlobalToast);
      this.boot();
    },

    beforeUnmount() {
      window.removeEventListener('library-unauthorized', this.handleUnauthorized);
      window.removeEventListener('library-toast', this.onGlobalToast);
      this.stopSse();
      if (this.pollTimer) {
        clearInterval(this.pollTimer);
      }
    },

    methods: {
      /* ------------------------- 基础工具 ------------------------- */
      assetUrl(path) {
        if (!path) return '';
        if (/^https?:\/\//i.test(path)) return path;
        return window.APP_CONFIG.uploadOrigin + path;
      },
      coverGradient(value) {
        let key = 0;
        if (value && typeof value === 'object') {
          key = value.id || 0;
        } else if (typeof value === 'number') {
          key = value;
        } else if (typeof value === 'string') {
          for (let i = 0; i < value.length; i++) {
            key = (key + value.charCodeAt(i) * (i + 1)) % 997;
          }
        }
        return PALETTES[Math.abs(Number(key) || 0) % PALETTES.length];
      },
      stockPercent(book) {
        if (!book || !book.totalCopies) return 0;
        return Math.max(0, Math.min(100, Math.round(book.availableCopies / book.totalCopies * 100)));
      },
      barPercent(value, list) {
        const max = Math.max.apply(null, (list || []).map(function (item) { return Number(item.value || 0); }).concat([0]));
        if (!max) return 0;
        return Math.max(0, Math.min(100, Math.round(Number(value || 0) / max * 100)));
      },
      barHeight(value, list) {
        const max = Math.max.apply(null, (list || []).map(function (item) { return Number(item.value || 0); }).concat([0]));
        if (!max) return 4;
        return Math.max(4, Math.round(Number(value || 0) / max * 100));
      },
      abs(value) {
        return Math.abs(Number(value || 0));
      },
      fmtDateTime(value) {
        if (!value) return '';
        const date = new Date(value);
        if (isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16);
        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
          + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes());
      },
      fmtDate(value) {
        if (!value) return '';
        const date = new Date(value);
        if (isNaN(date.getTime())) return String(value).slice(0, 10);
        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate());
      },
      fromNow(value) {
        if (!value) return '';
        const date = new Date(value);
        if (isNaN(date.getTime())) return '';
        const diff = Math.floor((Date.now() - date.getTime()) / 1000);
        if (diff < 60) return '刚刚';
        if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前';
        if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前';
        if (diff < 86400 * 7) return Math.floor(diff / 86400) + ' 天前';
        return this.fmtDate(value);
      },
      roleLabel(role) {
        return ({ ADMIN: '系统管理员', OPERATOR: '图书管理员', STUDENT: '学生', TEACHER: '教师' })[role] || role || '未知角色';
      },
      roleBadgeClass(role) {
        return ({ ADMIN: 'badge-red', OPERATOR: 'badge-blue', STUDENT: 'badge-green', TEACHER: 'badge-gold' })[role] || 'badge-gray';
      },
      loanStatusLabel(status) {
        return ({ BORROWED: '借阅中', RETURNED: '已归还', OVERDUE: '已超期' })[status] || status;
      },
      loanBadgeClass(status) {
        return ({ BORROWED: 'badge-blue', RETURNED: 'badge-gray', OVERDUE: 'badge-red' })[status] || 'badge-gray';
      },
      reservationStatusLabel(status) {
        return ({ WAITING: '排队中', NOTIFIED: '已通知', FULFILLED: '已借出', CANCELLED: '已取消' })[status] || status;
      },
      reservationBadgeClass(status) {
        return ({ WAITING: 'badge-amber', NOTIFIED: 'badge-green', FULFILLED: 'badge-blue', CANCELLED: 'badge-gray' })[status] || 'badge-gray';
      },
      galleryImages(book) {
        if (!book || !book.attachments) return [];
        return book.attachments.filter(function (item) { return item.imageType === 'GALLERY'; });
      },
      bookFormCoverUrl() {
        if (this.bookForm.coverPreview) return this.bookForm.coverPreview;
        if (this.bookForm.data && this.bookForm.data.coverUrl) return this.assetUrl(this.bookForm.data.coverUrl);
        return '';
      },

      /* ------------------------- 提示 / 会话 ------------------------- */
      notify(title, message, type) {
        const id = ++this.toastSeq;
        this.toasts.push({ id: id, title: title || '提示', message: message || '', type: type || 'success', leaving: false });
        const self = this;
        setTimeout(function () { self.dismissToast(id); }, 4200);
      },
      dismissToast(id) {
        const toast = this.toasts.find(function (item) { return item.id === id; });
        if (!toast || toast.leaving) return;
        toast.leaving = true;
        const self = this;
        setTimeout(function () {
          self.toasts = self.toasts.filter(function (item) { return item.id !== id; });
        }, 240);
      },
      onGlobalToast(event) {
        const detail = event.detail || {};
        if (!detail.message) return;
        this.notify(detail.type === 'error' ? '操作未完成' : '提示', detail.message, detail.type === 'error' ? 'error' : 'info');
      },
      handleUnauthorized() {
        if (!localStorage.getItem('library_token')) return;
        this.clearSession();
        if (!this.unauthorizedNotified) {
          this.unauthorizedNotified = true;
          this.notify('登录已过期', '请重新登录后再继续操作', 'error');
          const self = this;
          setTimeout(function () { self.unauthorizedNotified = false; }, 3000);
        }
      },
      clearSession() {
        localStorage.removeItem('library_token');
        localStorage.removeItem('library_user');
        this.token = '';
        this.user = null;
        this.booting = false;
        this.authMode = 'login';
        this.loginForm.password = '';
        this.notifications = [];
        this.unreadCount = 0;
        this.notificationPanelOpen = false;
        this.activeView = 'dashboard';
        this.stopSse();
        if (this.pollTimer) {
          clearInterval(this.pollTimer);
          this.pollTimer = null;
        }
      },
      boot() {
        const token = localStorage.getItem('library_token');
        if (!token) {
          this.booting = false;
          return;
        }
        this.token = token;
        this.booting = true;
        const self = this;
        API.get('/auth/me', {}, { silent: true }).then(function (user) {
          self.afterLogin({ token: token, user: user }, true);
        }).catch(function () {
          self.clearSession();
        }).finally(function () {
          self.booting = false;
        });
      },
      afterLogin(data, silent) {
        this.token = data.token;
        this.user = data.user;
        localStorage.setItem('library_token', data.token);
        localStorage.setItem('library_user', JSON.stringify(data.user));
        this.booting = false;
        this.authMode = 'login';
        this.loginForm.password = '';
        const initialView = new URLSearchParams(location.search).get('view');
        if (initialView && this.viewMeta[initialView]) {
          this.activeView = initialView;
        } else {
          this.activeView = 'dashboard';
        }
        this.loanScope = this.isStaff ? 'all' : 'my';
        this.reservationScope = this.isStaff ? 'all' : 'my';
        this.startSse();
        this.startPolling();
        this.loadNotifications();
        this.loadBookTypes();
        this.loadView(this.activeView);
        if (!silent) {
          this.notify('登录成功', '欢迎回来，' + data.user.name, 'success');
        }
      },
      switchAuth(mode) {
        this.authMode = mode;
      },
      fillDemo(role) {
        const accounts = {
          admin: { name: 'admin', password: 'admin123' },
          operator: { name: 'operator', password: 'operator123' },
          student1: { name: 'student1', password: 'student123' },
          teacher: { name: 'teacher', password: 'teacher123' }
        };
        const account = accounts[role];
        if (!account) return;
        this.authMode = 'login';
        this.loginForm.name = account.name;
        this.loginForm.password = account.password;
      },
      async handleLogin() {
        if (!this.loginForm.name || !this.loginForm.password) {
          this.notify('请填写登录信息', '用户名和密码不能为空', 'info');
          return;
        }
        this.authLoading = true;
        try {
          const data = await API.post('/auth/login', {
            name: this.loginForm.name,
            password: this.loginForm.password
          });
          this.afterLogin(data, false);
        } catch (e) {
          // 统一由 Axios 拦截器提示
        } finally {
          this.authLoading = false;
        }
      },
      async handleRegister() {
        const form = this.registerForm;
        if (!form.name || !form.password || !form.phone) {
          this.notify('请完善注册信息', '用户名、密码和手机号为必填项', 'info');
          return;
        }
        if (form.password.length < 6) {
          this.notify('密码长度不足', '密码至少需要 6 位字符', 'info');
          return;
        }
        this.authLoading = true;
        try {
          await API.post('/auth/register', {
            name: form.name,
            password: form.password,
            phone: form.phone,
            role: form.role,
            major: form.major,
            grade: form.grade,
            college: form.college,
            title: form.title,
            area: form.area,
            duty: form.duty,
            email: form.email
          });
          this.notify('注册成功', '请使用新账号登录图书馆', 'success');
          this.authMode = 'login';
          this.loginForm.name = form.name;
          this.registerForm = {
            name: '', password: '', phone: '', role: 'STUDENT', major: '',
            grade: '', college: '', title: '', area: '', duty: '', email: ''
          };
        } catch (e) {
          // 统一提示
        } finally {
          this.authLoading = false;
        }
      },
      logout() {
        const self = this;
        this.askConfirm('退出登录', '确定要退出当前账号吗？未归还的图书仍会保留在借阅记录中。', async function () {
          try {
            await API.post('/auth/logout');
          } catch (e) {
            // 即使接口失败也清理本地登录状态
          }
          self.clearSession();
          self.notify('已退出登录', '期待下次与你相遇', 'info');
        }, 'primary', '退出登录');
      },

      /* ------------------------- 导航与数据加载 ------------------------- */
      navigate(view, bookId) {
        if (view === 'users' && !this.isAdmin) view = 'dashboard';
        if ((view === 'logs' || view === 'stats') && !this.isStaff) view = 'dashboard';
        this.activeView = view;
        this.sidebarOpen = false;
        this.notificationPanelOpen = false;
        const self = this;
        if (view === 'books' && bookId) {
          this.loadBookTypes();
          this.loadBooks().then(function () {
            self.openBook(bookId);
          });
        } else {
          this.loadView(view);
        }
      },
      loadView(view) {
        switch (view) {
          case 'dashboard':
            return this.loadDashboard();
          case 'books':
            this.loadBookTypes();
            return this.loadBooks();
          case 'users':
            return this.loadUsers();
          case 'loans':
            return this.loadLoans();
          case 'reservations':
            return this.loadReservations();
          case 'stats':
            return this.loadStats();
          case 'logs':
            return this.loadLogs();
          case 'profile':
            return this.refreshMe();
          default:
            return Promise.resolve();
        }
      },
      refreshMe() {
        const self = this;
        return API.get('/auth/me', {}, { silent: true }).then(function (user) {
          self.user = user;
          localStorage.setItem('library_user', JSON.stringify(user));
        }).catch(function () {});
      },
      globalSearchGo() {
        if (!this.globalSearch) return;
        this.bookFilters.keyword = this.globalSearch;
        this.bookPage = 1;
        this.activeView = 'books';
        this.loadBookTypes();
        this.loadBooks();
      },

      loadDashboard() {
        const self = this;
        if (this.isStaff) {
          return Promise.all([
            API.get('/stats/overview'),
            API.get('/stats/top-books'),
            API.get('/stats/recent-loans')
          ]).then(function (result) {
            self.dashboard.overview = result[0];
            self.dashboard.popular = result[1] || [];
            self.dashboard.popularUnit = '次';
            self.dashboard.recentLoans = (result[2] || []).slice(0, 5);
          }).catch(function () {});
        }
        return Promise.all([
          API.get('/loans/my', { page: 1, size: 100 }),
          API.get('/reservations/my', { page: 1, size: 100 }),
          API.get('/books', { page: 1, size: 6 })
        ]).then(function (result) {
          const loanRecords = (result[0] && result[0].records) || [];
          const reservationRecords = (result[1] && result[1].records) || [];
          self.dashboard.recentLoans = loanRecords.slice(0, 5);
          self.dashboard.myActive = loanRecords.filter(function (item) { return item.status !== 'RETURNED'; }).length;
          self.dashboard.myDueSoon = loanRecords.filter(function (item) {
            return item.status !== 'RETURNED' && item.daysLeft !== null && item.daysLeft >= 0 && item.daysLeft <= 3;
          }).length;
          self.dashboard.myReservations = reservationRecords.filter(function (item) {
            return item.status === 'WAITING' || item.status === 'NOTIFIED';
          }).length;
          self.dashboard.totalBooks = (result[2] && result[2].total) || 0;
          self.dashboard.popular = ((result[2] && result[2].records) || []).map(function (book) {
            return { name: book.title, value: book.availableCopies };
          });
          self.dashboard.popularUnit = '本可借';
        }).catch(function () {});
      },
      loadBookTypes() {
        const self = this;
        return API.get('/books/types', {}, { silent: true }).then(function (types) {
          self.bookTypes = types || [];
        }).catch(function () {});
      },
      loadBooks() {
        const self = this;
        return API.get('/books', {
          keyword: this.bookFilters.keyword,
          type: this.bookFilters.type,
          status: this.bookFilters.status,
          onlyAvailable: this.bookFilters.onlyAvailable,
          page: this.bookPage,
          size: this.bookSize
        }).then(function (data) {
          self.books = data;
        }).catch(function () {});
      },
      searchBooks() {
        this.bookPage = 1;
        this.loadBooks();
      },
      resetBookFilters() {
        this.bookFilters = { keyword: '', type: '', status: '', onlyAvailable: false };
        this.bookPage = 1;
        this.loadBooks();
      },
      changeBookPage(page) {
        this.bookPage = page;
        this.loadBooks();
      },
      loadUsers() {
        const self = this;
        return API.get('/users', {
          keyword: this.userFilters.keyword,
          role: this.userFilters.role,
          status: this.userFilters.status,
          major: this.userFilters.major,
          page: this.userPage,
          size: this.userSize
        }).then(function (data) {
          self.users = data;
        }).catch(function () {});
      },
      searchUsers() {
        this.userPage = 1;
        this.loadUsers();
      },
      resetUserFilters() {
        this.userFilters = { keyword: '', role: '', status: '', major: '' };
        this.userPage = 1;
        this.loadUsers();
      },
      changeUserPage(page) {
        this.userPage = page;
        this.loadUsers();
      },
      loadLoans() {
        const self = this;
        const url = this.loanScope === 'my' ? '/loans/my' : '/loans';
        return API.get(url, {
          status: this.loanFilters.status,
          keyword: this.loanFilters.keyword,
          start: this.loanFilters.start,
          end: this.loanFilters.end,
          page: this.loanPage,
          size: this.loanSize
        }).then(function (data) {
          self.loans = data;
        }).catch(function () {});
      },
      switchLoanScope(scope) {
        this.loanScope = scope;
        this.loanPage = 1;
        this.loadLoans();
      },
      searchLoans() {
        this.loanPage = 1;
        this.loadLoans();
      },
      resetLoanFilters() {
        this.loanFilters = { keyword: '', status: '', start: '', end: '' };
        this.loanPage = 1;
        this.loadLoans();
      },
      changeLoanPage(page) {
        this.loanPage = page;
        this.loadLoans();
      },
      loadReservations() {
        const self = this;
        const url = this.reservationScope === 'my' ? '/reservations/my' : '/reservations';
        return API.get(url, {
          status: this.reservationFilters.status,
          keyword: this.reservationFilters.keyword,
          page: this.reservationPage,
          size: this.reservationSize
        }).then(function (data) {
          self.reservations = data;
        }).catch(function () {});
      },
      switchReservationScope(scope) {
        this.reservationScope = scope;
        this.reservationPage = 1;
        this.loadReservations();
      },
      searchReservations() {
        this.reservationPage = 1;
        this.loadReservations();
      },
      resetReservationFilters() {
        this.reservationFilters = { keyword: '', status: '' };
        this.reservationPage = 1;
        this.loadReservations();
      },
      changeReservationPage(page) {
        this.reservationPage = page;
        this.loadReservations();
      },
      loadStats() {
        const self = this;
        this.stats.loading = true;
        return Promise.all([
          API.get('/stats/overview'),
          API.get('/stats/monthly'),
          API.get('/stats/book-types'),
          API.get('/stats/loan-types'),
          API.get('/stats/top-books'),
          API.get('/stats/roles'),
          API.get('/stats/recent-loans')
        ]).then(function (result) {
          self.stats.overview = result[0];
          self.stats.monthly = result[1] || [];
          self.stats.bookTypes = result[2] || [];
          self.stats.loanTypes = result[3] || [];
          self.stats.topBooks = result[4] || [];
          self.stats.roles = result[5] || [];
          self.stats.recent = result[6] || [];
        }).catch(function () {}).finally(function () {
          self.stats.loading = false;
        });
      },
      loadLogs() {
        const self = this;
        const success = this.logFilters.success === '' ? undefined : this.logFilters.success === 'true';
        return API.get('/logs', {
          module: this.logFilters.module,
          keyword: this.logFilters.keyword,
          success: success,
          start: this.logFilters.start,
          end: this.logFilters.end,
          page: this.logPage,
          size: this.logSize
        }).then(function (data) {
          self.logs = data;
        }).catch(function () {});
      },
      searchLogs() {
        this.logPage = 1;
        this.loadLogs();
      },
      resetLogFilters() {
        this.logFilters = { module: '', keyword: '', success: '', start: '', end: '' };
        this.logPage = 1;
        this.loadLogs();
      },
      changeLogPage(page) {
        this.logPage = page;
        this.loadLogs();
      },

      /* ------------------------- 通知 / SSE ------------------------- */
      startSse() {
        this.stopSse();
        if (!this.token || typeof EventSource === 'undefined' || location.search.indexOf('noSse=1') >= 0) {
          return;
        }
        const self = this;
        const url = window.APP_CONFIG.apiBase + '/notifications/stream?token=' + encodeURIComponent(this.token);
        const source = new EventSource(url);
        this.eventSource = source;
        source.addEventListener('connected', function () {
          self.sseConnected = true;
        });
        source.addEventListener('unread', function (event) {
          const count = Number(event.data);
          if (!isNaN(count)) self.unreadCount = count;
        });
        source.addEventListener('notification', function (event) {
          try {
            const item = JSON.parse(event.data);
            self.notifications.unshift(item);
            if (self.notifications.length > 20) self.notifications.pop();
            self.unreadCount += 1;
            self.notify(item.title, item.content, item.type === 'RESERVATION' ? 'success' : 'info');
            if (self.activeView === 'reservations') self.loadReservations();
            if (self.activeView === 'loans') self.loadLoans();
            if (self.activeView === 'dashboard') self.loadDashboard();
          } catch (e) {
            // 忽略无法解析的消息
          }
        });
        source.onerror = function () {
          self.sseConnected = false;
        };
      },
      stopSse() {
        if (this.eventSource) {
          this.eventSource.close();
          this.eventSource = null;
        }
        this.sseConnected = false;
      },
      startPolling() {
        if (this.pollTimer) clearInterval(this.pollTimer);
        const self = this;
        this.pollTimer = setInterval(function () {
          if (self.user) self.fetchUnread();
        }, 30000);
      },
      fetchUnread() {
        const self = this;
        return API.get('/notifications/unread-count', {}, { silent: true }).then(function (count) {
          self.unreadCount = Number(count) || 0;
        }).catch(function () {});
      },
      loadNotifications() {
        const self = this;
        if (!this.user) return Promise.resolve();
        return Promise.all([
          API.get('/notifications/latest', {}, { silent: true }),
          API.get('/notifications/unread-count', {}, { silent: true })
        ]).then(function (result) {
          self.notifications = result[0] || [];
          self.unreadCount = Number(result[1]) || 0;
        }).catch(function () {});
      },
      toggleNotifications() {
        this.notificationPanelOpen = !this.notificationPanelOpen;
        if (this.notificationPanelOpen) {
          this.loadNotifications();
        }
      },
      markAllRead() {
        const self = this;
        API.post('/notifications/read-all').then(function () {
          self.notifications.forEach(function (item) { item.readFlag = true; });
          self.unreadCount = 0;
          self.notify('消息已全部标记为已读', '', 'info');
        }).catch(function () {});
      },
      openNotification(item) {
        const self = this;
        const go = function () {
          if (!item.readFlag) {
            API.post('/notifications/' + item.id + '/read').then(function () {
              item.readFlag = true;
              self.unreadCount = Math.max(0, self.unreadCount - 1);
            }).catch(function () {});
          }
          self.notificationPanelOpen = false;
          const link = item.link || '';
          if (link.indexOf('/books') === 0) {
            const match = link.match(/bookId=(\d+)/);
            self.navigate('books', match ? Number(match[1]) : null);
          } else if (link.indexOf('loans') >= 0) {
            self.navigate('loans');
          } else if (link.indexOf('reservations') >= 0) {
            self.navigate('reservations');
          } else {
            self.navigate('dashboard');
          }
        };
        go();
      },

      /* ------------------------- 图书操作 ------------------------- */
      openBook(id) {
        if (!id) return;
        this.bookDetail.show = true;
        this.bookDetail.loading = true;
        this.bookDetail.data = null;
        const self = this;
        API.get('/books/' + id).then(function (data) {
          self.bookDetail.data = data;
        }).catch(function () {
          self.bookDetail.show = false;
        }).finally(function () {
          self.bookDetail.loading = false;
        });
      },
      closeBookDetail() {
        this.bookDetail.show = false;
        this.bookDetail.data = null;
      },
      borrowBook(book) {
        const self = this;
        this.askConfirm('确认借阅', '确认借阅《' + book.title + '》吗？请在到期日前归还，逾期将记录在借阅历史中。', async function () {
          await API.post('/loans/borrow', { bookId: book.id });
          self.notify('借阅成功', '《' + book.title + '》已加入你的借阅记录', 'success');
          self.closeBookDetail();
          self.loadBooks();
          self.loadDashboard();
          self.loadNotifications();
          if (self.activeView === 'loans') self.loadLoans();
        }, 'primary', '确认借阅');
      },
      reserveBook(book) {
        const self = this;
        this.askConfirm('预约图书', '《' + book.title + '》当前暂无可借副本。预约成功后将进入排队，到书时会通过消息中心实时通知你。', async function () {
          await API.post('/reservations', { bookId: book.id });
          self.notify('预约成功', '已加入《' + book.title + '》的预约队列', 'success');
          self.closeBookDetail();
          self.loadBooks();
          self.loadReservations();
          self.loadNotifications();
        }, 'primary', '确认预约');
      },
      openBookForm(book) {
        this.bookForm.coverPreview = '';
        this.bookForm.coverFile = null;
        this.bookForm.galleryFiles = [];
        this.bookForm.existing = [];
        this.bookForm.saving = false;
        this.bookForm.show = true;
        if (!book) {
          this.bookForm.mode = 'create';
          this.bookForm.id = null;
          this.bookForm.data = emptyBookForm();
          return;
        }
        this.bookForm.mode = 'edit';
        this.bookForm.id = book.id;
        this.bookForm.data = toBookFormData(book);
        const self = this;
        const load = book.attachments ? Promise.resolve(book) : API.get('/books/' + book.id);
        load.then(function (detail) {
          self.bookForm.data = toBookFormData(detail);
          self.bookForm.existing = detail.attachments || [];
        }).catch(function () {
          self.bookForm.show = false;
        });
      },
      closeBookForm() {
        if (this.bookForm.saving) return;
        this.bookForm.show = false;
        if (this.bookForm.coverPreview && this.bookForm.coverPreview.indexOf('blob:') === 0) {
          URL.revokeObjectURL(this.bookForm.coverPreview);
        }
        this.bookForm.coverPreview = '';
      },
      onCoverSelect(event) {
        const file = event.target.files && event.target.files[0];
        if (!file) return;
        if (file.size > 8 * 1024 * 1024) {
          this.notify('图片过大', '单张封面不能超过 8MB', 'error');
          event.target.value = '';
          return;
        }
        if (this.bookForm.coverPreview && this.bookForm.coverPreview.indexOf('blob:') === 0) {
          URL.revokeObjectURL(this.bookForm.coverPreview);
        }
        this.bookForm.coverFile = file;
        this.bookForm.coverPreview = URL.createObjectURL(file);
        event.target.value = '';
      },
      onGallerySelect(event) {
        const files = Array.from(event.target.files || []);
        const existingGallery = this.bookForm.existing.filter(function (item) { return item.imageType === 'GALLERY'; }).length;
        if (existingGallery + files.length > 12) {
          this.notify('图片数量超出限制', '每本图书最多保留 12 张相关图片', 'error');
          event.target.value = '';
          return;
        }
        this.bookForm.galleryFiles = files;
        event.target.value = '';
      },
      async saveBook() {
        const data = this.bookForm.data;
        if (!data.title || !data.author) {
          this.notify('请完善图书信息', '书名和作者为必填项', 'info');
          return;
        }
        this.bookForm.saving = true;
        try {
          const payload = {
            title: data.title,
            author: data.author,
            type: data.type,
            publisher: data.publisher,
            isbn: data.isbn,
            days: Number(data.days || 30),
            price: Number(data.price || 0),
            totalCopies: Number(data.totalCopies || 0),
            status: data.status,
            introduction: data.introduction,
            shelfNo: data.shelfNo
          };
          let id = this.bookForm.id;
          if (this.bookForm.mode === 'create') {
            const created = await API.post('/books', payload);
            id = created.id;
          } else {
            await API.put('/books/' + id, payload);
          }
          if (this.bookForm.coverFile) {
            const formData = new FormData();
            formData.append('file', this.bookForm.coverFile);
            await API.upload('/books/' + id + '/cover', formData);
          }
          if (this.bookForm.galleryFiles.length) {
            const formData = new FormData();
            this.bookForm.galleryFiles.forEach(function (file) { formData.append('files', file); });
            await API.upload('/books/' + id + '/gallery', formData);
          }
          this.notify('保存成功', this.bookForm.mode === 'create' ? '新图书已加入馆藏' : '图书信息与图片已更新', 'success');
          this.closeBookForm();
          this.loadBooks();
          this.loadBookTypes();
          if (this.activeView === 'dashboard') this.loadDashboard();
          if (this.bookDetail.show && this.bookDetail.data && this.bookDetail.data.id === id) {
            this.openBook(id);
          }
        } catch (e) {
          // 统一提示
        } finally {
          this.bookForm.saving = false;
        }
      },
      removeAttachment(image) {
        const self = this;
        this.askConfirm('删除图片', '删除后图片文件与附件记录都会被移除，且不可恢复。', async function () {
          const result = await API.del('/books/' + self.bookForm.id + '/gallery/' + image.id);
          self.bookForm.existing = result.attachments || [];
          self.notify('图片已删除', '', 'info');
          self.loadBooks();
          if (self.bookDetail.show) self.openBook(self.bookForm.id);
        }, 'danger', '删除图片');
      },
      toggleBookStatus(book) {
        const self = this;
        const next = book.status === 'ACTIVE' ? 'DISCARDED' : 'ACTIVE';
        const title = next === 'DISCARDED' ? '下架图书' : '重新上架';
        const message = next === 'DISCARDED'
          ? '下架后读者将无法借阅或预约《' + book.title + '》，已有借阅记录会保留。'
          : '确认将《' + book.title + '》重新上架供读者借阅吗？';
        this.askConfirm(title, message, async function () {
          await API.put('/books/' + book.id + '/status', { status: next });
          self.notify(next === 'DISCARDED' ? '图书已下架' : '图书已上架', '《' + book.title + '》', 'success');
          self.loadBooks();
          if (self.bookDetail.show) self.openBook(book.id);
        }, next === 'DISCARDED' ? 'danger' : 'primary', title);
      },

      /* ------------------------- 用户 / 借阅 / 预约操作 ------------------------- */
      openUser(id) {
        this.userDetail.show = true;
        this.userDetail.data = null;
        const self = this;
        API.get('/users/' + id).then(function (data) {
          self.userDetail.data = data;
        }).catch(function () {
          self.userDetail.show = false;
        });
      },
      closeUserDetail() {
        this.userDetail.show = false;
        this.userDetail.data = null;
      },
      toggleUserStatus(user) {
        const self = this;
        const next = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
        const title = next === 'DISABLED' ? '停用账号' : '启用账号';
        const message = next === 'DISABLED'
          ? '停用后用户 ' + user.name + ' 将无法登录系统，确认继续吗？'
          : '确认恢复用户 ' + user.name + ' 的登录权限吗？';
        this.askConfirm(title, message, async function () {
          const updated = await API.put('/users/' + user.id + '/status', { status: next });
          self.notify('用户状态已更新', updated.name + ' 当前状态：' + (updated.status === 'ACTIVE' ? '正常' : '已停用'), 'success');
          self.loadUsers();
          if (self.userDetail.data && self.userDetail.data.id === updated.id) {
            self.userDetail.data = updated;
          }
        }, next === 'DISABLED' ? 'danger' : 'primary', title);
      },
      returnLoan(loan) {
        const self = this;
        this.askConfirm('确认还书', '确认归还《' + loan.bookTitle + '》吗？归还后该副本将重新变为可借状态，并自动通知预约队列中的读者。', async function () {
          await API.post('/loans/' + loan.id + '/return');
          self.notify('还书成功', '《' + loan.bookTitle + '》已归还，感谢爱护馆藏', 'success');
          self.loadLoans();
          self.loadDashboard();
          self.loadBooks();
          self.loadNotifications();
        }, 'primary', '确认还书');
      },
      cancelReservation(item) {
        const self = this;
        this.askConfirm('取消预约', '确认取消《' + item.bookTitle + '》的预约吗？取消后将退出当前排队。', async function () {
          await API.del('/reservations/' + item.id);
          self.notify('预约已取消', '《' + item.bookTitle + '》', 'info');
          self.loadReservations();
          self.loadDashboard();
        }, 'danger', '取消预约');
      },

      /* ------------------------- 确认框 / 图片预览 ------------------------- */
      askConfirm(title, message, action, tone, confirmText) {
        this.confirmState = {
          show: true,
          title: title,
          message: message,
          tone: tone || 'primary',
          confirmText: confirmText || '确定',
          action: action
        };
      },
      closeConfirm() {
        this.confirmState.show = false;
        this.confirmState.action = null;
      },
      async runConfirm() {
        const action = this.confirmState.action;
        this.confirmState.show = false;
        this.confirmState.action = null;
        if (action) {
          try {
            await action();
          } catch (e) {
            // 统一提示
          }
        }
      },
      openLightbox(images, index) {
        this.lightbox.images = images || [];
        this.lightbox.index = index || 0;
        this.lightbox.show = true;
      },
      closeLightbox() {
        this.lightbox.show = false;
        this.lightbox.images = [];
        this.lightbox.index = 0;
      },
      lightboxPrev() {
        if (!this.lightbox.images.length) return;
        this.lightbox.index = (this.lightbox.index - 1 + this.lightbox.images.length) % this.lightbox.images.length;
      },
      lightboxNext() {
        if (!this.lightbox.images.length) return;
        this.lightbox.index = (this.lightbox.index + 1) % this.lightbox.images.length;
      }
    }
  });

  app.component('pager', {
    props: {
      data: {
        type: Object,
        default: function () { return emptyPage(10); }
      }
    },
    emits: ['change'],
    computed: {
      pageList() {
        const pages = Number(this.data && this.data.pages) || 0;
        const current = Number(this.data && this.data.page) || 1;
        if (pages <= 7) {
          const list = [];
          for (let i = 1; i <= pages; i++) list.push(i);
          return list;
        }
        let start = Math.max(1, current - 2);
        let end = Math.min(pages, start + 4);
        start = Math.max(1, end - 4);
        const list = [];
        for (let i = start; i <= end; i++) list.push(i);
        return list;
      }
    },
    template: `
      <div class="pager">
        <span class="pager-info">共 <strong>{{ data.total || 0 }}</strong> 条 · 第 {{ data.page || 1 }} / {{ data.pages || 1 }} 页</span>
        <div class="pager-buttons">
          <button type="button" :disabled="!data.page || data.page <= 1" @click="$emit('change', data.page - 1)">上一页</button>
          <button type="button" v-for="page in pageList" :key="page" :class="{ active: page === data.page }" @click="$emit('change', page)">{{ page }}</button>
          <button type="button" :disabled="!data.pages || data.page >= data.pages" @click="$emit('change', data.page + 1)">下一页</button>
        </div>
      </div>
    `
  });

  app.mount('#app');
})();
