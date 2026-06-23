function drawLineChart(canvas, points){
  const ctx = canvas.getContext('2d');
  const w = canvas.width, h = canvas.height;
  ctx.clearRect(0,0,w,h);
  if(!points.length) return;
  const pad = 42;
  const prices = points.map(p=>p.price);
  const min = Math.min(...prices), max = Math.max(...prices);
  const scaleX = (w-pad*2)/(points.length-1 || 1);
  const scaleY = (h-pad*2)/((max-min)||1);
  ctx.lineWidth = 1;
  ctx.strokeStyle = 'rgba(148,163,184,.35)';
  for(let i=0;i<5;i++){ const y=pad+i*(h-pad*2)/4; ctx.beginPath(); ctx.moveTo(pad,y); ctx.lineTo(w-pad,y); ctx.stroke(); }
  ctx.lineWidth = 4;
  ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--gold') || '#d4a017';
  ctx.beginPath();
  points.forEach((p,i)=>{
    const x = pad + i*scaleX;
    const y = h-pad - (p.price-min)*scaleY;
    i?ctx.lineTo(x,y):ctx.moveTo(x,y);
  });
  ctx.stroke();
  ctx.fillStyle = getComputedStyle(document.body).color;
  ctx.font = '20px system-ui';
  ctx.fillText(formatNumber(max), pad, 28);
  ctx.fillText(formatNumber(min), pad, h-10);
}
