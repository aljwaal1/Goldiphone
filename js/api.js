// ضع رابط Cloudflare Worker الحقيقي هنا لاحقاً
const WORKER_URL = "";
const JOD_RATE = 0.709; // الدينار الأردني مثبت تقريباً مقابل الدولار، للتجربة فقط.

const ASSETS = {
  gold: { name:"الذهب", subtitle:"الأونصة العالمية XAU", icon:"🟡", base:2350, vol:0.012 },
  silver: { name:"الفضة", subtitle:"الأونصة العالمية XAG", icon:"⚪", base:24.8, vol:0.018 },
  bitcoin: { name:"البيتكوين", subtitle:"BTC", icon:"₿", base:67000, vol:0.035 }
};

async function fetchMarketData(){
  if(WORKER_URL){
    const res = await fetch(WORKER_URL, { cache:"no-store" });
    if(!res.ok) throw new Error("تعذر جلب الأسعار");
    return await res.json();
  }
  return createDemoData();
}

function createDemoData(){
  const today = new Date();
  const data = {};
  Object.entries(ASSETS).forEach(([key, meta], idx)=>{
    const points = [];
    let price = meta.base * JOD_RATE;
    for(let i=364;i>=0;i--){
      const d = new Date(today); d.setDate(today.getDate()-i);
      const wave = Math.sin((365-i+idx*12)/24) * meta.vol * 1.2;
      const drift = Math.sin((365-i)/77) * meta.vol * .9;
      const noise = (Math.sin((365-i)*7.31+idx) * meta.vol);
      price = Math.max(price * (1 + wave/8 + drift/10 + noise/18), meta.base*JOD_RATE*.55);
      points.push({ date:d.toISOString().slice(0,10), price:+price.toFixed(key==='bitcoin'?0:2) });
    }
    data[key] = { ...meta, currency:"JOD", points };
  });
  return { updatedAt:new Date().toISOString(), assets:data, demo:true };
}
