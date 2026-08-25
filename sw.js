const CACHE_NAME = "market-pulse-v6";
const FILES = ["./", "./index.html", "./manifest.webmanifest", "./icon-192.png", "./icon-512.png", "./hotfix-v6.js"];

self.addEventListener("install", event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(FILES)).catch(()=>{}));
  self.skipWaiting();
});

self.addEventListener("activate", event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.map(k => k !== CACHE_NAME ? caches.delete(k) : null))));
  self.clients.claim();
});

async function injectHotfix(response){
  try{
    const text = await response.clone().text();
    if(text.includes('hotfix-v6.js')) return response;
    const patched = text.replace('</body>', '<script src="./hotfix-v6.js?v=6"></script></body>');
    return new Response(patched, {
      status: response.status,
      statusText: response.statusText,
      headers: { 'Content-Type':'text/html; charset=utf-8', 'Cache-Control':'no-cache' }
    });
  }catch(e){ return response; }
}

self.addEventListener("fetch", event => {
  const url = event.request.url;

  if(url.includes("gold-api.com") || url.includes("coingecko.com") || url.includes("frankfurter.dev") || url.includes("stooq.com")){
    event.respondWith(fetch(event.request));
    return;
  }

  if(event.request.mode === 'navigate' || url.endsWith('/Goldiphone/') || url.endsWith('/Goldiphone/index.html')){
    event.respondWith(
      fetch(event.request, {cache:'no-store'})
        .then(injectHotfix)
        .catch(async ()=> {
          const cached = await caches.match('./index.html');
          return cached ? injectHotfix(cached) : new Response('Offline', {status:503});
        })
    );
    return;
  }

  event.respondWith(
    fetch(event.request).then(res => {
      const copy = res.clone();
      caches.open(CACHE_NAME).then(c => c.put(event.request, copy)).catch(()=>{});
      return res;
    }).catch(() => caches.match(event.request))
  );
});
