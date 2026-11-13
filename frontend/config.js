/*
 * 前端独立部署配置。
 * 默认自动使用“当前访问主机 + 8081 端口”的后端地址，便于局域网访问。
 * 如需修改，可在浏览器 localStorage 中设置 library_api_base，
 * 例如：localStorage.setItem('library_api_base', 'http://192.168.1.10:8081/api')
 */
(function () {
  const saved = localStorage.getItem('library_api_base');
  const host = location.hostname || 'localhost';
  const protocol = location.protocol === 'https:' ? 'https:' : 'http:';
  const apiBase = (saved || protocol + '//' + host + ':8081/api').replace(/\/+$/, '');
  window.APP_CONFIG = {
    apiBase: apiBase,
    uploadOrigin: apiBase.replace(/\/api$/, ''),
    appName: '拾光图书馆',
    version: '2026.1'
  };
})();
