const CACHE = 'jordan-market-pwa-v1';
const FILES = ['./','index.html','css/style.css','js/storage.js','js/api.js','js/charts.js','js/app.js','manifest.json'];
self.addEventListener('install', e => e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES))));
self.addEventListener('fetch', e => e.respondWith(caches.match(e.request).then(r => r || fetch(e.request))));
