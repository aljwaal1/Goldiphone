const CACHE_NAME = "jm-prices-v1";
const FILES = ["./", "./index.html", "./manifest.webmanifest", "./icon-192.png", "./icon-512.png"];

self.addEventListener("install", event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(FILES)).catch(()=>{}));
  self.skipWaiting();
});

self.addEventListener("activate", event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.map(k => k !== CACHE_NAME ? caches.delete(k) : null))));
  self.clients.claim();
});

self.addEventListener("fetch", event => {
  const url = event.request.url;
  // أسعار ومصادر البيانات يجب أن تُجلب من الشبكة دائمًا، لا نخزّنها مؤقتًا أبدًا
  if(url.includes("gold-api.com") || url.includes("coingecko.com")){
    event.respondWith(fetch(event.request).catch(()=> new Response("{}", {headers:{"Content-Type":"application/json"}})));
    return;
  }
  // باقي ملفات الواجهة: شبكة أولاً، ثم التخزين المؤقت كخطة بديلة عند انقطاع الإنترنت
  event.respondWith(
    fetch(event.request).then(res => {
      const copy = res.clone();
      caches.open(CACHE_NAME).then(c => c.put(event.request, copy)).catch(()=>{});
      return res;
    }).catch(() => caches.match(event.request))
  );
});
