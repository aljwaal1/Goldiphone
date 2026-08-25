package com.explapp.marketpulse

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var bottomBar: LinearLayout
    private val prefs by lazy { getSharedPreferences("market_pulse", MODE_PRIVATE) }

    data class IntroPage(val icon:String,val eyebrow:String,val title:String,val description:String,val a:Int,val b:Int)
    private val introPages = listOf(
        IntroPage("✦","كل السوق في شاشة واحدة","تابع الذهب والعملات بثقة","ذهب وفضة وبيتكوين وعملات عربية وعالمية في واجهة واحدة سريعة وواضحة.",Color.rgb(255,207,85),Color.rgb(255,139,74)),
        IntroPage("↕","عملة التطبيق","اختر العملة التي تناسبك","الدولار افتراضيًا، ويمكن تغييره إلى الدينار الأردني أو العملات الخليجية أو اليورو والجنيه.",Color.rgb(67,216,255),Color.rgb(88,126,255)),
        IntroPage("⌁","تحليل زمني حقيقي","اعرف أين كان السعر","اليوم وأمس وأسبوع وشهر و3 أشهر و6 أشهر وسنة، دون تكرار سعر حالي على أنه تاريخي.",Color.rgb(155,124,255),Color.rgb(218,92,255)),
        IntroPage("🔔","ملخص يومي","اختر الوقت الذي يناسبك","فعّل إشعارًا يوميًا اختياريًا بآخر ملخص محفوظ للأسواق وبعملة التطبيق التي اخترتها.",Color.rgb(75,229,167),Color.rgb(67,216,255)),
        IntroPage("✉","الدعم والتواصل","المطور قريب منك","يمكنك مراسلة المطور في أي وقت من الشريط السفلي داخل التطبيق.",Color.rgb(255,157,76),Color.rgb(155,124,255))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=Color.rgb(8,9,19); window.navigationBarColor=Color.rgb(8,9,19)
        if(!prefs.getBoolean("onboarding_done",false)) showOnboarding(0) else showWebApp(savedInstanceState)
    }

    private fun gradient(c1:Int,c2:Int,r:Float=32f)=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(c1,c2)).apply{cornerRadius=r}

    private fun showOnboarding(i:Int){
        val p=introPages[i]
        val root=FrameLayout(this).apply{setBackgroundColor(Color.rgb(8,9,19))}
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(26),dp(28),dp(26),dp(28))}
        root.addView(content,FrameLayout.LayoutParams(-1,-1))
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        top.addView(TextView(this).apply{text="M+  مؤشر الأسواق";setTextColor(Color.WHITE);textSize=20f;setTypeface(typeface,Typeface.BOLD)},LinearLayout.LayoutParams(0,dp(48),1f))
        top.addView(TextView(this).apply{text="تخطي";setTextColor(Color.rgb(170,176,199));gravity=Gravity.CENTER;setOnClickListener{finishOnboarding()}},LinearLayout.LayoutParams(dp(64),dp(48)))
        content.addView(top,LinearLayout.LayoutParams(-1,-2)); content.addView(View(this),LinearLayout.LayoutParams(1,0,.45f))
        content.addView(TextView(this).apply{text=p.icon;gravity=Gravity.CENTER;textSize=52f;setTextColor(Color.rgb(18,16,28));background=gradient(p.a,p.b,44f);elevation=dp(8).toFloat()},LinearLayout.LayoutParams(dp(116),dp(116)).apply{bottomMargin=dp(26)})
        content.addView(TextView(this).apply{text=p.eyebrow;setTextColor(p.a);textSize=13f;gravity=Gravity.CENTER;setTypeface(typeface,Typeface.BOLD)})
        content.addView(TextView(this).apply{text=p.title;setTextColor(Color.WHITE);textSize=29f;gravity=Gravity.CENTER;setTypeface(typeface,Typeface.BOLD);setPadding(0,dp(10),0,dp(12))})
        content.addView(TextView(this).apply{text=p.description;setTextColor(Color.rgb(180,185,207));textSize=16f;gravity=Gravity.CENTER;setLineSpacing(dp(4).toFloat(),1f)},LinearLayout.LayoutParams(-1,-2))
        content.addView(View(this),LinearLayout.LayoutParams(1,0,.55f))
        content.addView(TextView(this).apply{text=introPages.indices.joinToString("  "){if(it==i)"●" else "○"};setTextColor(p.a);textSize=16f;gravity=Gravity.CENTER;setPadding(0,0,0,dp(16))})
        if(i==introPages.lastIndex) content.addView(Button(this).apply{text="✉  مراسلة المطور";isAllCaps=false;setTextColor(Color.WHITE);background=gradient(Color.rgb(42,46,72),Color.rgb(25,28,48));setOnClickListener{contactDeveloper()}},LinearLayout.LayoutParams(-1,dp(52)).apply{bottomMargin=dp(9)})
        content.addView(Button(this).apply{text=if(i==introPages.lastIndex)"ابدأ الآن" else "التالي";isAllCaps=false;textSize=17f;setTypeface(typeface,Typeface.BOLD);setTextColor(Color.rgb(18,16,28));background=gradient(p.a,p.b);setOnClickListener{if(i==introPages.lastIndex)finishOnboarding() else showOnboarding(i+1)}},LinearLayout.LayoutParams(-1,dp(56)))
        setContentView(root)
    }

    private fun finishOnboarding(){prefs.edit().putBoolean("onboarding_done",true).apply();showWebApp(null)}
    private fun contactDeveloper(){runCatching{startActivity(Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:fastunllocked2017@gmail.com?subject="+Uri.encode("مراسلة مطور مؤشر الأسواق"))))}}

    private fun showWebApp(saved:Bundle?){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(8,9,19))}
        webView=WebView(this).apply{
            setBackgroundColor(Color.rgb(8,9,19));overScrollMode=View.OVER_SCROLL_NEVER;isVerticalScrollBarEnabled=false;isHorizontalScrollBarEnabled=false
            setLayerType(View.LAYER_TYPE_HARDWARE,null);addJavascriptInterface(AppBridge(),"MarketPulseAndroid")
            webChromeClient=WebChromeClient();webViewClient=object:WebViewClient(){override fun onPageFinished(v:WebView?,u:String?){injectUiFixes()}}
            settings.apply{javaScriptEnabled=true;domStorageEnabled=true;databaseEnabled=true;cacheMode=WebSettings.LOAD_NO_CACHE;builtInZoomControls=false;displayZoomControls=false;setSupportZoom(false);loadWithOverviewMode=true;useWideViewPort=false;allowFileAccess=false;allowContentAccess=false;mixedContentMode=WebSettings.MIXED_CONTENT_NEVER_ALLOW;userAgentString="$userAgentString MarketPulseAndroid/2.0"}
            clearCache(true)
        }
        root.addView(webView,LinearLayout.LayoutParams(-1,0,1f)); bottomBar=createBottomBar();root.addView(bottomBar,LinearLayout.LayoutParams(-1,dp(64)));setContentView(root)
        if(saved==null)webView.loadUrl("https://aljwaal1.github.io/Goldiphone/?app=android&v=20") else webView.restoreState(saved)
    }

    private fun createBottomBar():LinearLayout{
        val bar=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(dp(6),dp(6),dp(6),dp(6));setBackgroundColor(Color.rgb(10,12,24))}
        fun add(label:String,action:()->Unit){bar.addView(Button(this).apply{text=label;isAllCaps=false;textSize=11f;setTextColor(Color.WHITE);background=gradient(Color.rgb(26,30,50),Color.rgb(18,21,37),20f);setOnClickListener{action()}},LinearLayout.LayoutParams(0,dp(50),1f).apply{marginStart=dp(3);marginEnd=dp(3)})}
        add("⌂\nالرئيسية"){showMainPage()}; add("⌁\nالرسوم"){showChartsPage()}; add("🔔\nالملخص"){showNotificationSettings()}; add("✉\nالمطور"){contactDeveloper()}
        return bar
    }

    private fun showMainPage(){webView.evaluateJavascript("document.body.classList.remove('charts-page');window.scrollTo(0,0);",null)}
    private fun showChartsPage(){webView.evaluateJavascript("document.body.classList.add('charts-page');var c=document.querySelector('.card');if(c)c.scrollIntoView({block:'start'});",null)}

    private fun showNotificationSettings(){
        val enabled=prefs.getBoolean("daily_enabled",false)
        if(enabled){
            android.app.AlertDialog.Builder(this).setTitle("الملخص اليومي").setMessage("الإشعار اليومي مفعّل الساعة ${prefs.getString("daily_time","20:00")}. ماذا تريد؟")
                .setPositiveButton("تغيير الوقت"){_,_->pickNotificationTime()}.setNegativeButton("إيقاف"){_,_->cancelDailyNotification();Toast.makeText(this,"تم إيقاف الملخص اليومي",Toast.LENGTH_SHORT).show()}.setNeutralButton("إلغاء",null).show()
        }else pickNotificationTime()
    }

    private fun pickNotificationTime(){
        val saved=prefs.getString("daily_time",null)?.split(":")
        val h=saved?.getOrNull(0)?.toIntOrNull()?:20;val m=saved?.getOrNull(1)?.toIntOrNull()?:0
        TimePickerDialog(this,{_,hour,minute->
            prefs.edit().putBoolean("daily_enabled",true).putString("daily_time",String.format("%02d:%02d",hour,minute)).apply();scheduleDailyNotification(hour,minute);requestNotificationPermission();Toast.makeText(this,"تم ضبط الملخص اليومي ${String.format("%02d:%02d",hour,minute)}",Toast.LENGTH_LONG).show()
        },h,m,true).show()
    }

    private fun scheduleDailyNotification(hour:Int,minute:Int){
        val alarm=getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi=PendingIntent.getBroadcast(this,7001,Intent(this,DailySummaryReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cal=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,hour);set(Calendar.MINUTE,minute);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0);if(timeInMillis<=System.currentTimeMillis())add(Calendar.DAY_OF_YEAR,1)}
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.timeInMillis,AlarmManager.INTERVAL_DAY,pi)
    }
    private fun cancelDailyNotification(){prefs.edit().putBoolean("daily_enabled",false).apply();val alarm=getSystemService(Context.ALARM_SERVICE) as AlarmManager;val pi=PendingIntent.getBroadcast(this,7001,Intent(this,DailySummaryReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);alarm.cancel(pi)}
    private fun requestNotificationPermission(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),9001)}

    inner class AppBridge{
        @JavascriptInterface fun saveSummary(summary:String,base:String){prefs.edit().putString("daily_summary",summary.take(900)).putString("base_currency",base).apply()}
    }

    private fun injectUiFixes(){
        val js="""
        (function(){
          try{
            document.body.classList.add('android-app');
            var oldStyle=document.getElementById('android-v20-style');if(oldStyle)oldStyle.remove();
            var st=document.createElement('style');st.id='android-v20-style';st.textContent=`
            html,body{overflow-x:hidden!important;scroll-behavior:auto!important}body.android-app:before{display:none!important}.wrap{max-width:100%!important;padding:10px!important}
            .basePanel,.heroCard,.card,.market,.fx,.metric,.tableWrap,.iconbtn{backdrop-filter:none!important;-webkit-backdrop-filter:none!important;box-shadow:0 4px 12px rgba(0,0,0,.12)!important}
            .baseScroll,.currencyScroller{display:flex!important;flex-direction:column!important;overflow:visible!important;gap:8px!important}.baseBtn,.fx{width:100%!important;min-width:0!important;flex:none!important;border-radius:15px!important}
            .baseBtn{display:flex!important;align-items:center!important;gap:12px!important;text-align:right!important;padding:11px 13px!important}.baseBtn span{font-size:24px!important}.baseBtn b{font-size:13px!important;margin:0!important}.baseBtn em{font-style:normal!important;color:var(--muted)!important;font-size:11px!important;margin-inline-start:auto!important}
            .fx{display:grid!important;grid-template-columns:auto 1fr auto!important;align-items:center!important;padding:12px 13px!important}.fxTop{display:contents!important}.fx .flag{grid-column:1;font-size:25px!important}.fx .code{grid-column:2;font-size:13px!important}.fxName{grid-column:2;white-space:normal!important}.fxVal{grid-column:3;grid-row:1/3;margin:0!important;font-size:15px!important}.fxHint{display:none!important}
            .marketGrid{grid-template-columns:repeat(2,minmax(0,1fr))!important;gap:8px!important}.metrics{grid-template-columns:repeat(2,minmax(0,1fr))!important}.market{min-height:100px!important;padding:11px!important}.mp{font-size:13px!important;letter-spacing:0!important}.big{font-size:22px!important;letter-spacing:0!important}
            .mn,.md,.fxName,.fxHint,.status,.sectionHead small,.detailTitle span,.source,th{color:#747b91!important}body[data-theme=light]{--muted:#555d73!important;--line:rgba(20,24,40,.16)!important}body[data-theme=light] .up{color:#087a53!important}body[data-theme=light] .down{color:#b52e50!important}
            body.charts-page .basePanel,body.charts-page .heroCard,body.charts-page .marketGrid,body.charts-page .currencyScroller,body.charts-page>.wrap>.sectionHead{display:none!important}body.charts-page .hero{display:flex!important}body.charts-page .card{margin-top:12px!important}.chartBox{min-height:210px!important;overflow:hidden!important}canvas{max-width:100%!important;height:auto!important}
            @media(max-width:380px){.wrap{padding:8px!important}.marketGrid{grid-template-columns:1fr!important}.baseTop{align-items:flex-start!important;flex-direction:column!important}.hero{gap:7px!important}.logo{width:42px!important;height:42px!important}h1{font-size:17px!important}.sub{font-size:9px!important}}
            `;document.head.appendChild(st);
            window.nearest=function(points,target){if(!points||!points.length)return null;var t=new Date(target).getTime(),best=null,bd=Infinity;for(var i=0;i<points.length;i++){var pt=new Date(points[i].date+'T12:00:00').getTime(),d=Math.abs(pt-t);if(d<bd){bd=d;best=points[i]}}return bd<=4*86400000?best:null};
            function enhance(){try{document.querySelectorAll('[data-base]').forEach(function(btn){if(btn.querySelector('em'))return;var code=btn.getAttribute('data-base');var c=window.CURRENCIES&&CURRENCIES.find(function(x){return x.code===code});if(c){var e=document.createElement('em');e.textContent=c.name;btn.appendChild(e)}});var gold=document.getElementById('heroPrice');var update=document.getElementById('lastUpdate');var fx=[].slice.call(document.querySelectorAll('.fx'),0,4).map(function(x){var a=x.querySelector('.code'),b=x.querySelector('.fxVal');return(a?a.textContent:'')+' '+(b?b.textContent:'')}).join(' • ');var base=localStorage.getItem('base_currency_v1')||'USD';var summary='الذهب '+(gold?gold.textContent:'')+(fx?' | '+fx:'')+(update?' | تحديث '+update.textContent:'');if(window.MarketPulseAndroid)MarketPulseAndroid.saveSummary(summary,base)}catch(e){}}
            var ob=new MutationObserver(enhance);ob.observe(document.body,{childList:true,subtree:true});enhance();if(window.render)render();
          }catch(e){console.log(e)}
        })();
        """.trimIndent();webView.evaluateJavascript(js,null)
    }

    override fun onSaveInstanceState(out:Bundle){if(::webView.isInitialized)webView.saveState(out);super.onSaveInstanceState(out)}
    @Deprecated("Deprecated in Java") override fun onBackPressed(){if(::webView.isInitialized&&webView.canGoBack())webView.goBack() else super.onBackPressed()}
    override fun onDestroy(){if(::webView.isInitialized)webView.destroy();super.onDestroy()}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
