(function () {
  const client = axios.create({
    baseURL: window.APP_CONFIG.apiBase,
    timeout: 20000
  });

  function toast(message, type) {
    window.dispatchEvent(new CustomEvent('library-toast', {
      detail: { message: message, type: type || 'error' }
    }));
  }

  client.interceptors.request.use(function (config) {
    const token = localStorage.getItem('library_token');
    if (token) {
      config.headers.Authorization = 'Bearer ' + token;
    }
    return config;
  });

  client.interceptors.response.use(
    function (response) {
      const body = response.data;
      if (body && typeof body.code !== 'undefined') {
        if (body.code === 200) {
          return body.data;
        }
        toast(body.message || '操作失败');
        return Promise.reject(new Error(body.message || '操作失败'));
      }
      return body;
    },
    function (error) {
      const status = error.response && error.response.status;
      const message = (error.response && error.response.data && error.response.data.message)
        || (error.code === 'ECONNABORTED' ? '请求超时，请检查后端服务' : '网络异常，请稍后重试');
      if (status === 401) {
        window.dispatchEvent(new Event('library-unauthorized'));
      }
      if (!error.config || !error.config.silent) {
        toast(message);
      }
      return Promise.reject(new Error(message));
    }
  );

  function clean(params) {
    const result = {};
    Object.keys(params || {}).forEach(function (key) {
      const value = params[key];
      if (value !== '' && value !== null && value !== undefined) {
        result[key] = value;
      }
    });
    return result;
  }

  window.LibraryApi = {
    get: function (url, params, config) {
      return client.get(url, Object.assign({ params: clean(params) }, config || {}));
    },
    post: function (url, data, config) {
      return client.post(url, data, config || {});
    },
    put: function (url, data, config) {
      return client.put(url, data, config || {});
    },
    del: function (url, config) {
      return client.delete(url, config || {});
    },
    upload: function (url, formData) {
      return client.post(url, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 60000
      });
    },
    raw: client
  };
})();
