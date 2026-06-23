// نسخة حقيقية بدون API Key سري
// المصادر:
// - الذهب والفضة: Stooq CSV مجاني بدون مفتاح، أسعار XAUUSD و XAGUSD بالدولار
// - البيتكوين: CoinGecko API مجاني بدون مفتاح، مقابل JOD مباشرة
// - تحويل USD/JOD: Frankfurter مجاني بدون مفتاح، مع احتياط ثابت 0.709

const JOD_FALLBACK = 0.709; // احتياط فقط إذا فشل Frankfurter
const DAYS_BACK = 370;

const ASSETS = {
  gold: { name:"الذهب", subtitle:"الأونصة العالمية XAU", icon:"🟡", stooq:"xauusd" },
  silver: { name:"الفضة", subtitle:"الأونصة العالمية XAG", icon:"⚪", stooq:"xagusd" },
  bitcoin: { name:"البيتكوين", subtitle:"BTC", icon:"₿", coingecko:"bitcoin" }
};

async function fetchMarketData(){
  try{
    const [usdJod, gold, silver, bitcoin] = await Promise.all([
      fetchUsdJodRate(),
      fetchStooqMetal("gold", ASSETS.gold.stooq),
      fetchStooqMetal("silver", ASSETS.silver.stooq),
      fetchBitcoinJod()
    ]);

    gold.points = normalizePoints(gold.points.map(p => ({ date:p.date, price: roundPrice(p.price * usdJod, "gold") })));
    silver.points = normalizePoints(silver.points.map(p => ({ date:p.date, price: roundPrice(p.price * usdJod, "silver") })));
    bitcoin.points = normalizePoints(bitcoin.points);

    return {
      updatedAt: new Date().toISOString(),
      currency: "JOD",
      source: "Stooq + CoinGecko + Frankfurter",
      real: true,
      usdJod,
      assets: { gold, silver, bitcoin }
    };
  }catch(err){
    console.warn("Real APIs failed, using local fallback", err);
    const cached = Store?.get?.('lastMarket', null);
    if(cached) return { ...cached, offline:true };
    return createDemoData();
  }
}

async function fetchUsdJodRate(){
  try{
    const res = await fetch("https://api.frankfurter.dev/v1/latest?base=USD&symbols=JOD", { cache:"no-store" });
    if(!res.ok) throw new Error("Frankfurter failed");
    const json = await res.json();
    return Number(json?.rates?.JOD) || JOD_FALLBACK;
  }catch(e){
    return JOD_FALLBACK;
  }
}

async function fetchStooqMetal(key, symbol){
  // رابط CSV منشور ومجاني. إذا منعه المتصفح بسبب CORS، استخدم worker.js المرفق كوكيل مجاني.
  const url = `https://stooq.com/q/d/l/?s=${symbol}&i=d`;
  const res = await fetch(url, { cache:"no-store" });
  if(!res.ok) throw new Error(`Stooq ${symbol} failed`);
  const csv = await res.text();
  const rows = csv.trim().split(/\r?\n/).slice(1);
  const points = rows.map(line => {
    const [date, open, high, low, close] = line.split(',');
    return { date, price: Number(close) };
  }).filter(p => p.date && Number.isFinite(p.price) && p.price > 0).slice(-DAYS_BACK);

  if(points.length < 30) throw new Error(`Not enough Stooq data for ${symbol}`);
  return { ...ASSETS[key], currency:"JOD", points };
}

async function fetchBitcoinJod(){
  const url = "https://api.coingecko.com/api/v3/coins/bitcoin/market_chart?vs_currency=jod&days=365&interval=daily";
  const res = await fetch(url, { cache:"no-store" });
  if(!res.ok) throw new Error("CoinGecko failed");
  const json = await res.json();
  const byDate = new Map();
  for(const item of json.prices || []){
    const date = new Date(item[0]).toISOString().slice(0,10);
    byDate.set(date, { date, price: roundPrice(Number(item[1]), "bitcoin") });
  }
  const points = Array.from(byDate.values()).slice(-DAYS_BACK);
  if(points.length < 30) throw new Error("Not enough BTC data");
  return { ...ASSETS.bitcoin, currency:"JOD", points };
}

function normalizePoints(points){
  // يرتب ويحذف التكرار ويعطي آخر 365 يوم تقريبًا
  const map = new Map();
  points.forEach(p => { if(p.date && Number.isFinite(p.price)) map.set(p.date, p); });
  return Array.from(map.values()).sort((a,b)=>a.date.localeCompare(b.date)).slice(-365);
}

function roundPrice(n, key){
  return +(key === "bitcoin" ? n.toFixed(0) : n.toFixed(2));
}

function createDemoData(){
  const today = new Date();
  const demo = {
    gold: { ...ASSETS.gold, base:3350, vol:0.012 },
    silver: { ...ASSETS.silver, base:26, vol:0.018 },
    bitcoin: { ...ASSETS.bitcoin, base:76000, vol:0.035 }
  };
  const data = {};
  Object.entries(demo).forEach(([key, meta], idx)=>{
    const points = [];
    let price = meta.base;
    for(let i=364;i>=0;i--){
      const d = new Date(today); d.setDate(today.getDate()-i);
      const wave = Math.sin((365-i+idx*12)/24) * meta.vol * 1.2;
      const drift = Math.sin((365-i)/77) * meta.vol * .9;
      const noise = (Math.sin((365-i)*7.31+idx) * meta.vol);
      price = Math.max(price * (1 + wave/8 + drift/10 + noise/18), meta.base*.55);
      points.push({ date:d.toISOString().slice(0,10), price:roundPrice(price, key) });
    }
    data[key] = { ...meta, currency:"JOD", points };
  });
  return { updatedAt:new Date().toISOString(), assets:data, demo:true, source:"بيانات تجريبية احتياطية" };
}
