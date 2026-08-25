// Hotfix v6: لا تسمح بفشل مصدر واحد بإيقاف التطبيق كله.
(function(){
  const race = (p, ms=9000) => Promise.race([
    p,
    new Promise((_,reject)=>setTimeout(()=>reject(new Error('timeout')), ms))
  ]);

  const appendToday = (arr, price) => {
    if(!Number.isFinite(price)) return arr || [];
    const list = Array.isArray(arr) ? [...arr] : [];
    const d = iso(new Date());
    const i = list.findIndex(x=>x.date===d);
    if(i>=0) list[i] = {date:d, price}; else list.push({date:d, price});
    return list.slice(-370);
  };

  window.load = async function(){
    const status = $('marketStatus');
    if(status) status.textContent='● تحديث';

    try {
      if(!data || typeof data !== 'object') data={assets:{},fx:{},updatedAt:null};
      data.assets = data.assets || {};
      data.fx = data.fx || {};

      const jobs = await Promise.allSettled([
        race(fetchGold('XAU'),7000),
        race(fetchGold('XAG'),7000),
        race(fetchStooq('xauusd'),7000),
        race(fetchStooq('xagusd'),7000),
        race(fetchBitcoin(),9000),
        race(fetchFx(),9000)
      ]);

      const [xau,xag,goldHist,silverHist,btc,fx] = jobs;
      let ok = 0;

      if(goldHist.status==='fulfilled' && goldHist.value?.length){ data.assets.gold=goldHist.value; ok++; }
      if(silverHist.status==='fulfilled' && silverHist.value?.length){ data.assets.silver=silverHist.value; ok++; }
      if(btc.status==='fulfilled' && btc.value?.length){ data.assets.bitcoin=btc.value; ok++; }
      if(fx.status==='fulfilled' && fx.value && Object.keys(fx.value).length){ data.fx=fx.value; ok++; }

      if(xau.status==='fulfilled' && Number.isFinite(xau.value)){
        data.assets.gold = appendToday(data.assets.gold, xau.value); ok++;
      }
      if(xag.status==='fulfilled' && Number.isFinite(xag.value)){
        data.assets.silver = appendToday(data.assets.silver, xag.value); ok++;
      }

      // USD يجب أن يبقى متاحًا دائمًا حتى إذا تعذر Frankfurter.
      if(!data.fx.USD) data.fx.USD=[{date:iso(new Date(Date.now()-365*864e5)),price:1},{date:iso(new Date()),price:1}];

      if(ok>0){
        data.updatedAt=new Date().toISOString();
        localStorage.setItem('market_cache_v5',JSON.stringify(data));
        if(status) status.textContent = ok>=4 ? '● مباشر' : '● تحديث جزئي';
      } else {
        const cached=localStorage.getItem('market_cache_v5')||localStorage.getItem('market_cache_v4');
        if(cached){ data=JSON.parse(cached); if(status) status.textContent='● آخر نسخة محفوظة'; }
        else if(status) status.textContent='● تعذر الاتصال';
      }
    } catch(err){
      console.warn('hotfix load failed',err);
      const cached=localStorage.getItem('market_cache_v5')||localStorage.getItem('market_cache_v4');
      if(cached){ try{data=JSON.parse(cached)}catch(e){} }
      if(status) status.textContent = data?.updatedAt ? '● آخر نسخة محفوظة' : '● تعذر الاتصال';
    }

    try{ render(); }catch(e){ console.warn('render failed',e); }
  };

  // أعد التحميل بعد تعريف الـhotfix حتى لو كانت المحاولة الأصلية قد فشلت.
  setTimeout(()=>window.load(),80);
})();
