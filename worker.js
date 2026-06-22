// Cloudflare Worker اختياري ومجاني، بدون أي API Key
// استخدمه فقط إذا منع المتصفح جلب Stooq بسبب CORS.
// بعد نشره، غيّر روابط fetch في js/api.js إلى رابط Worker أو اجعل التطبيق يطلب /?url=...

export default {
  async fetch(request) {
    const url = new URL(request.url);
    const target = url.searchParams.get('url');
    const allowed = [
      'https://stooq.com/',
      'https://api.coingecko.com/',
      'https://api.frankfurter.dev/'
    ];

    if (!target || !allowed.some(a => target.startsWith(a))) {
      return new Response('Missing or blocked url', { status: 400, headers: corsHeaders() });
    }

    const upstream = await fetch(target, { headers: { 'User-Agent': 'JordanMarketPWA/1.0' } });
    const body = await upstream.text();
    return new Response(body, {
      status: upstream.status,
      headers: {
        ...corsHeaders(),
        'content-type': upstream.headers.get('content-type') || 'text/plain; charset=utf-8',
        'cache-control': 'public, max-age=300'
      }
    });
  }
};

function corsHeaders(){
  return {
    'access-control-allow-origin': '*',
    'access-control-allow-methods': 'GET, OPTIONS',
    'access-control-allow-headers': '*'
  };
}
