const ranges = [
  { key:'7d', label:'أسبوع', days:7 }, { key:'30d', label:'شهر', days:30 },
  { key:'90d', label:'3 شهور', days:90 }, { key:'180d', label:'6 شهور', days:180 },
  { key:'365d', label:'سنة', days:365 }
];
let market, selected='gold', chartRange='30d';

const el = id => document.getElementById(id);
const formatNumber = n => new Intl.NumberFormat('ar-JO', { maximumFractionDigits: n > 1000 ? 0 : 2 }).format(n);
const pct = n => `${n>0?'+':''}${n.toFixed(2)}%`;
const diffText = (current, ref) => {
  const d = current - ref, p = ref ? d/ref*100 : 0;
  return `<span class="${d>=0?'up':'down'}">${d>=0?'+':''}${formatNumber(d)} د.أ (${pct(p)})</span>`;
};

async function init(){
  document.body.dataset.theme = Store.get('theme','light');
  el('themeBtn').onclick = ()=>{ document.body.dataset.theme = document.body.dataset.theme==='dark'?'light':'dark'; Store.set('theme', document.body.dataset.theme); };
  el('refreshBtn').onclick = load;
  el('saveCompare').onclick = saveComparison;
  await load();
  if('serviceWorker' in navigator) navigator.serviceWorker.register('sw.js');
}
async function load(){
  try { market = await fetchMarketData(); Store.set('lastMarket', market); }
  catch(e){ market = Store.get('lastMarket', createDemoData()); }
  renderAll();
}
function renderAll(){
  el('lastUpdate').textContent = new Date(market.updatedAt).toLocaleString('ar-JO');
  renderSelectors(); renderCards(); renderDetails(); renderComparisons();
}
function renderSelectors(){
  const options = Object.entries(market.assets).map(([k,a])=>`<option value="${k}">${a.name}</option>`).join('');
  el('assetSelect').innerHTML = options; el('assetSelect').value=selected; el('assetSelect').onchange=e=>{selected=e.target.value;renderDetails();};
  el('compareAsset').innerHTML = options;
}
function renderCards(){
  el('assetsGrid').innerHTML = Object.entries(market.assets).map(([k,a])=>{
    const last = a.points.at(-1).price, y = a.points.at(-2).price;
    return `<article class="card asset-card" onclick="selectAsset('${k}')">
      <div class="asset-top"><div><h2>${a.name}</h2><p class="muted">${a.subtitle}</p></div><div class="asset-icon">${a.icon}</div></div>
      <div class="price">${formatNumber(last)} د.أ</div>
      <div class="change">مقارنة بأمس: ${diffText(last,y)}</div>
    </article>`;
  }).join('');
}
function selectAsset(k){ selected=k; el('assetSelect').value=k; renderDetails(); window.scrollTo({top:260,behavior:'smooth'}); }
function sliceDays(points, days){ return points.slice(Math.max(0, points.length-days)); }
function calcStats(points, days){
  const s = sliceDays(points, days), prices=s.map(x=>x.price);
  return { high:Math.max(...prices), low:Math.min(...prices), avg:prices.reduce((a,b)=>a+b,0)/prices.length };
}
function renderDetails(){
  const a = market.assets[selected], current=a.points.at(-1).price, yesterday=a.points.at(-2).price;
  el('selectedTitle').textContent = `${a.name} - ${a.subtitle}`;
  const year = calcStats(a.points,365), month=calcStats(a.points,30), week=calcStats(a.points,7);
  const distanceHigh = ((current-year.high)/year.high)*100;
  const distanceLow = ((current-year.low)/year.low)*100;
  el('smartInsights').innerHTML = `
    <div class="mini-card"><span class="muted">مقارنة أمس</span><strong>${diffText(current,yesterday)}</strong></div>
    <div class="mini-card"><span class="muted">متوسط 7 أيام</span><strong>${formatNumber(week.avg)} د.أ</strong><small>${diffText(current,week.avg)}</small></div>
    <div class="mini-card"><span class="muted">عن أعلى سنة</span><strong class="${distanceHigh>=0?'up':'down'}">${pct(distanceHigh)}</strong><small>${formatNumber(year.high)} د.أ</small></div>
    <div class="mini-card"><span class="muted">متوسط 30 يوم</span><strong>${formatNumber(month.avg)} د.أ</strong><small>${diffText(current,month.avg)}</small></div>
    <div class="mini-card"><span class="muted">عن أدنى سنة</span><strong class="up">${pct(distanceLow)}</strong><small>${formatNumber(year.low)} د.أ</small></div>
    <div class="mini-card"><span class="muted">المؤشر</span><strong>${Math.abs(distanceHigh)<5?'قريب من القمة':distanceLow<8?'قريب من القاع':'منطقة وسطية'}</strong></div>`;
  el('rangeTabs').innerHTML = ranges.map(r=>`<button class="tab ${chartRange===r.key?'active':''}" onclick="chartRange='${r.key}';renderDetails()">${r.label}</button>`).join('');
  drawLineChart(el('chart'), sliceDays(a.points, ranges.find(r=>r.key===chartRange).days));
  el('statsBody').innerHTML = ranges.map(r=>{
    const st = calcStats(a.points,r.days);
    return `<tr><td>${r.label}</td><td>${formatNumber(st.high)} د.أ</td><td>${diffText(current,st.high)}</td><td>${formatNumber(st.low)} د.أ</td><td>${diffText(current,st.low)}</td></tr>`;
  }).join('');
}
function saveComparison(){
  const asset = el('compareAsset').value, price = Number(el('customPrice').value);
  if(!price) return;
  const list = Store.get('comparisons',[]);
  list.unshift({ asset, price, date:new Date().toISOString() });
  Store.set('comparisons', list.slice(0,30));
  el('customPrice').value=''; renderComparisons();
}
function renderComparisons(){
  const list = Store.get('comparisons',[]);
  if(!list.length){ el('savedComparisons').innerHTML='<p class="muted">لم تحفظ أسعار مقارنة بعد.</p>'; return; }
  el('savedComparisons').innerHTML = list.map((x,i)=>{
    const a = market.assets[x.asset], current=a.points.at(-1).price;
    return `<div class="saved-item"><div><b>${a.name}</b><br><span class="muted">سعرك: ${formatNumber(x.price)} د.أ</span></div><div>${diffText(current,x.price)}</div><button class="tab" onclick="removeComparison(${i})">حذف</button></div>`;
  }).join('');
}
function removeComparison(i){ const list=Store.get('comparisons',[]); list.splice(i,1); Store.set('comparisons',list); renderComparisons(); }
init();
