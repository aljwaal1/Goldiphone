package com.explapp.marketpulse

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var bottomBar: LinearLayout
    private val prefs by lazy { getSharedPreferences("market_pulse", MODE_PRIVATE) }

    data class IntroPage(val icon:String,val eyebrow:String,val title:String,val description:String,val a:Int,val b:Int)
    private val introPages = listOf(
        IntroPage("✦","كل السوق في شاشة واحدة","تابع الذهب والعملات بثقة","ذهب وفضة وبيتكوين وعملات عربية وعالمية في واجهة واحدة سريعة وواضحة.",Color.rgb(255,207,85),Color.rgb(255,139,74)),
        IntroPage("↕","عملة التطبيق","اختر العملة التي تناسبك","الدولار افتراضيًا، ويمكن تغيير العملة من قائمة منسدلة واضحة دون استهلاك مساحة الشاشة.",Color.rgb(67,216,255),Color.rgb(88,126,255)),
        IntroPage("⌁","تحليل زمني حقيقي","اعرف أين كان السعر","التغير في بطاقات الأصول يقارن بسعر أمس، والتحليل التاريخي لا يعرض نقطة غير متوفرة كأنها حقيقية.",Color.rgb(155,124,255),Color.rgb(218,92,255)),
        IntroPage("🔕","أنت تتحكم بالإشعارات","لا إزعاج دون اختيارك","لن يرسل التطبيق أي إشعار تلقائيًا. أنت تحدد تاريخ البدء والوقت والأيام التي تريد وصول الإشعار فيها، ويمكنك تعديلها أو إيقافها في أي وقت.",Color.rgb(75,229,167),Color.rgb(67,216,255)),
        IntroPage("🎯","إشعار حسب اهتمامك","اختر الأسعار التي تريدها","يمكنك اختيار كل الأسعار، أو الذهب والفضة والبيتكوين، أو عملات محددة فقط. ويمكن تعديل الاختيارات لاحقًا من قسم الإشعارات.",Color.rgb(255,196,80),Color.rgb(255,126,90)),
        IntroPage("✉","الدعم والتواصل","المطور قريب منك","يمكنك مراسلة المطور في أي وقت من أيقونة المراسلة في الشريط السفلي.",Color.rgb(255,157,76),Color.rgb(155,124,255))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=Color.rgb(8,9,19)
        window.navigationBarColor=Color.rgb(8,9,19)
        if(!prefs.getBoolean("onboarding_done",false)) showOnboarding(0) else showWebApp(savedInstanceState)
    }

    private fun gradient(c1:Int,c2:Int,r:Float=32f)=GradientDrawable(
        GradientDrawable.Orientation.TL_BR,intArrayOf(c1,c2)
    ).apply{cornerRadius=r}

    private fun showOnboarding(i:Int){
        val p=introPages[i]
        val root=FrameLayout(this).apply{setBackgroundColor(Color.rgb(8,9,19))}
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(26),dp(28),dp(26),dp(28))}
        root.addView(content,FrameLayout.LayoutParams(-1,-1))
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        top.addView(TextView(this).apply{text="M+  مؤشر الأسواق";setTextColor(Color.WHITE);textSize=20f;setTypeface(typeface,Typeface.BOLD)},LinearLayout.LayoutParams(0,dp(48),1f))
        top.addView(TextView(this).apply{text="تخطي";setTextColor(Color.rgb(170,176,199));gravity=Gravity.CENTER;setOnClickListener{finishOnboarding()}},LinearLayout.LayoutParams(dp(64),dp(48)))
        content.addView(top,LinearLayout.LayoutParams(-1,-2))
        content.addView(View(this),LinearLayout.LayoutParams(1,0,.45f))
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
            setLayerType(View.LAYER_TYPE_HARDWARE,null);addJavascriptInterface(AppBridge(),"MarketPulseAndroid");webChromeClient=WebChromeClient()
            webViewClient=object:WebViewClient(){override fun onPageFinished(v:WebView?,u:String?){injectUiFixes()}}
            settings.apply{javaScriptEnabled=true;domStorageEnabled=true;databaseEnabled=true;cacheMode=WebSettings.LOAD_NO_CACHE;builtInZoomControls=false;displayZoomControls=false;setSupportZoom(false);loadWithOverviewMode=true;useWideViewPort=false;allowFileAccess=false;allowContentAccess=false;mixedContentMode=WebSettings.MIXED_CONTENT_NEVER_ALLOW;userAgentString="$userAgentString MarketPulseAndroid/2.3.1"}
            clearCache(true)
        }
        root.addView(webView,LinearLayout.LayoutParams(-1,0,1f));bottomBar=createBottomBar();root.addView(bottomBar,LinearLayout.LayoutParams(-1,dp(64)));setContentView(root)
        if(saved==null)webView.loadUrl("https://aljwaal1.github.io/Goldiphone/?app=android&v=231") else webView.restoreState(saved)
    }

    private fun createBottomBar():LinearLayout{
        val bar=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;layoutDirection=View.LAYOUT_DIRECTION_LTR;gravity=Gravity.CENTER;setPadding(dp(6),dp(6),dp(6),dp(6));setBackgroundColor(Color.rgb(10,12,24))}
        fun add(label:String,size:Float=11f,action:()->Unit){bar.addView(Button(this).apply{text=label;isAllCaps=false;textSize=size;setTextColor(Color.WHITE);background=gradient(Color.rgb(26,30,50),Color.rgb(18,21,37),20f);setOnClickListener{action()}},LinearLayout.LayoutParams(0,dp(50),1f).apply{marginStart=dp(3);marginEnd=dp(3)})}
        add("✉",23f){contactDeveloper()};add("🔔\nالإشعارات"){showNotificationSettings()};add("⌁\nالتحليل"){showChartsPage()};add("⌂\nالرئيسية"){showMainPage()}
        return bar
    }

    private fun showMainPage(){webView.evaluateJavascript("document.body.classList.remove('charts-page');window.scrollTo(0,0);",null)}
    private fun showChartsPage(){webView.evaluateJavascript("document.body.classList.add('charts-page');var c=document.querySelector('.card');if(c)c.scrollIntoView({block:'start'});",null)}

    private fun showNotificationSettings(){
        val enabled=prefs.getBoolean("daily_enabled",false)
        val panel=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(4),dp(18),dp(8))}
        panel.addView(TextView(this).apply{
            text=if(enabled) "● الإشعارات مفعّلة\nمن ${prefs.getString("notify_start_date","—")}  •  ${prefs.getString("daily_time","—")}\n${selectedDaysLabel()}\n${summaryItemsLabel()}" else "○ الإشعارات متوقفة\nلن يصلك أي إشعار حتى تقوم بتفعيلها بنفسك."
            setTextColor(if(enabled) Color.rgb(53,190,132) else Color.rgb(120,126,145));textSize=14f;setPadding(0,dp(4),0,dp(12))
        })
        lateinit var dialog:android.app.AlertDialog
        fun addAction(title:String,action:()->Unit){
            panel.addView(Button(this).apply{
                text=title;isAllCaps=false;textSize=14f;gravity=Gravity.CENTER_VERTICAL or Gravity.START;setTextColor(Color.WHITE)
                background=gradient(Color.rgb(31,35,58),Color.rgb(20,23,41),18f)
                setOnClickListener{dialog.dismiss();action()}
            },LinearLayout.LayoutParams(-1,dp(52)).apply{bottomMargin=dp(8)})
        }
        if(enabled){
            addAction("📅  تعديل تاريخ البدء  •  ${prefs.getString("notify_start_date","—")}"){pickStartDate(false)}
            addAction("🕒  تعديل وقت الإشعار  •  ${prefs.getString("daily_time","—")}"){pickNotificationTime(false)}
            addAction("📆  تعديل أيام الإشعار"){pickNotificationDays(false)}
            addAction("🎯  تعديل الأسعار التي تظهر في الإشعار"){pickNotificationItems(false)}
            addAction("⏸  إيقاف الإشعارات مع الاحتفاظ بالإعدادات"){
                cancelDailyNotification();Toast.makeText(this,"تم إيقاف الإشعارات ويمكنك تفعيلها لاحقًا",Toast.LENGTH_LONG).show();showNotificationSettings()
            }
            addAction("🗑  حذف جدول الإشعارات بالكامل"){
                NotificationScheduler.cancel(this)
                prefs.edit().putBoolean("daily_enabled",false).remove("notify_start_date").remove("daily_time").remove("notify_days").remove("notify_items").apply()
                Toast.makeText(this,"تم حذف جدول الإشعارات",Toast.LENGTH_LONG).show();showNotificationSettings()
            }
        }else{
            addAction("🔔  إعداد وتفعيل الإشعارات"){
                if(prefs.contains("daily_time")&&prefs.contains("notify_start_date")){
                    prefs.edit().putBoolean("daily_enabled",true).apply();requestNotificationPermission();rescheduleIfEnabled();Toast.makeText(this,"تم تفعيل الإشعارات من جديد",Toast.LENGTH_LONG).show();showNotificationSettings()
                }else pickStartDate(true)
            }
            if(prefs.contains("daily_time")||prefs.contains("notify_start_date")){
                addAction("✏  تعديل الإعدادات المحفوظة قبل التفعيل"){
                    prefs.edit().putBoolean("daily_enabled",true).apply();showNotificationSettings()
                }
            }
        }
        dialog=android.app.AlertDialog.Builder(this).setTitle("🔔 إدارة الإشعارات").setView(panel).setNegativeButton("إغلاق",null).create()
        dialog.show()
    }

    private fun pickStartDate(continueSetup:Boolean){
        val cal=Calendar.getInstance()
        val saved=prefs.getString("notify_start_date",null)
        if(!saved.isNullOrBlank())runCatching{SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(saved)?.let{cal.time=it}}
        val dialog=DatePickerDialog(this,{_,y,m,d->
            val value=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)
            prefs.edit().putString("notify_start_date",value).apply()
            if(continueSetup)pickNotificationTime(true) else{rescheduleIfEnabled();showNotificationSettings()}
        },cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis
        dialog.show()
    }

    private fun pickNotificationTime(continueSetup:Boolean=false){
        val saved=prefs.getString("daily_time",null)?.split(":");val h=saved?.getOrNull(0)?.toIntOrNull()?:20;val m=saved?.getOrNull(1)?.toIntOrNull()?:0
        TimePickerDialog(this,{_,hour,minute->
            val time=String.format(Locale.US,"%02d:%02d",hour,minute);prefs.edit().putString("daily_time",time).apply()
            if(continueSetup)pickNotificationDays(true) else{rescheduleIfEnabled();showNotificationSettings()}
        },h,m,true).show()
    }

    private fun pickNotificationDays(continueSetup:Boolean=false){
        val labels=arrayOf("الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت")
        val values=intArrayOf(Calendar.SUNDAY,Calendar.MONDAY,Calendar.TUESDAY,Calendar.WEDNESDAY,Calendar.THURSDAY,Calendar.FRIDAY,Calendar.SATURDAY)
        val current=prefs.getString("notify_days","1,2,3,4,5,6,7")!!.split(",").mapNotNull{it.toIntOrNull()}.toMutableSet()
        val checked=BooleanArray(values.size){values[it] in current}
        android.app.AlertDialog.Builder(this).setTitle("اختر أيام الإشعار").setMultiChoiceItems(labels,checked){_,which,isChecked->if(isChecked)current.add(values[which]) else current.remove(values[which])}
            .setPositiveButton("حفظ"){_,_->
                if(current.isEmpty()){Toast.makeText(this,"اختر يومًا واحدًا على الأقل",Toast.LENGTH_LONG).show();return@setPositiveButton}
                prefs.edit().putString("notify_days",current.sorted().joinToString(",")).apply();if(continueSetup)pickNotificationItems(true) else{rescheduleIfEnabled();showNotificationSettings()}
            }.setNegativeButton("إلغاء"){_,_->if(!continueSetup)showNotificationSettings()}.show()
    }

    private fun pickNotificationItems(finalize:Boolean=false){
        val labels=arrayOf("كل الأسعار","الذهب","الفضة","بيتكوين","الدولار USD","اليورو EUR","الجنيه GBP","الدينار الأردني JOD","الريال السعودي SAR","الدرهم الإماراتي AED","الدينار الكويتي KWD","الريال القطري QAR","الدينار البحريني BHD","الريال العماني OMR")
        val keys=arrayOf("all","gold","silver","bitcoin","USD","EUR","GBP","JOD","SAR","AED","KWD","QAR","BHD","OMR")
        val current=prefs.getString("notify_items","gold,silver,bitcoin")!!.split(",").filter{it.isNotBlank()}.toMutableSet()
        val checked=BooleanArray(keys.size){keys[it] in current}
        android.app.AlertDialog.Builder(this).setTitle("ماذا تريد في الإشعار؟").setMultiChoiceItems(labels,checked){_,which,isChecked->
            if(which==0&&isChecked){current.clear();current.add("all")}else if(which==0){current.remove("all")}else{current.remove("all");if(isChecked)current.add(keys[which]) else current.remove(keys[which])}
        }.setPositiveButton("حفظ"){_,_->
            if(current.isEmpty()){Toast.makeText(this,"اختر سعرًا واحدًا على الأقل",Toast.LENGTH_LONG).show();return@setPositiveButton}
            prefs.edit().putString("notify_items",current.joinToString(",")).apply();if(finalize)finishNotificationSetup() else{rescheduleIfEnabled();showNotificationSettings()}
        }.setNegativeButton("إلغاء"){_,_->if(!finalize)showNotificationSettings()}.show()
    }

    private fun finishNotificationSetup(){
        prefs.edit().putBoolean("daily_enabled",true).apply();requestNotificationPermission();rescheduleIfEnabled();Toast.makeText(this,"تم تفعيل الإشعارات حسب اختياراتك",Toast.LENGTH_LONG).show();showNotificationSettings()
    }

    private fun rescheduleIfEnabled(){
        if(!prefs.getBoolean("daily_enabled",false))return
        val hm=prefs.getString("daily_time","20:00")!!.split(":");NotificationScheduler.scheduleNext(this,hm.getOrNull(0)?.toIntOrNull()?:20,hm.getOrNull(1)?.toIntOrNull()?:0)
    }

    private fun cancelDailyNotification(){prefs.edit().putBoolean("daily_enabled",false).apply();NotificationScheduler.cancel(this)}

    private fun selectedDaysLabel():String{
        val map=mapOf(1 to "الأحد",2 to "الاثنين",3 to "الثلاثاء",4 to "الأربعاء",5 to "الخميس",6 to "الجمعة",7 to "السبت")
        return prefs.getString("notify_days","1,2,3,4,5,6,7")!!.split(",").mapNotNull{it.toIntOrNull()?.let(map::get)}.joinToString("، ")
    }

    private fun summaryItemsLabel():String{
        val raw=prefs.getString("notify_items","gold,silver,bitcoin")?:"gold,silver,bitcoin";if(raw.split(",").contains("all"))return "كل الأسعار"
        val map=mapOf("gold" to "الذهب","silver" to "الفضة","bitcoin" to "بيتكوين","USD" to "USD","EUR" to "EUR","GBP" to "GBP","JOD" to "JOD","SAR" to "SAR","AED" to "AED","KWD" to "KWD","QAR" to "QAR","BHD" to "BHD","OMR" to "OMR")
        return raw.split(",").mapNotNull{map[it]}.joinToString("، ")
    }

    private fun requestNotificationPermission(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),9001)}

    inner class AppBridge{
        @JavascriptInterface fun saveSummary(summary:String,base:String){prefs.edit().putString("daily_summary",summary.take(900)).putString("base_currency",base).apply()}
        @JavascriptInterface fun saveSnapshot(snapshot:String,base:String){prefs.edit().putString("market_snapshot",snapshot.take(12000)).putString("base_currency",base).apply()}
    }

    private fun injectUiFixes(){
        val js="""
        (function(){
          try{
            document.body.classList.add('android-app');
            var oldStyle=document.getElementById('android-v21-style');if(oldStyle)oldStyle.remove();
            var st=document.createElement('style');st.id='android-v21-style';st.textContent=`
            html,body{overflow-x:hidden!important;scroll-behavior:auto!important}body.android-app:before{display:none!important}.wrap{max-width:100%!important;padding:10px!important}
            .basePanel,.heroCard,.card,.market,.fx,.metric,.tableWrap,.iconbtn{backdrop-filter:none!important;-webkit-backdrop-filter:none!important;box-shadow:0 4px 12px rgba(0,0,0,.12)!important}
            .baseTop{align-items:center!important}.baseScroll{display:block!important;overflow:visible!important;margin-top:12px!important}.baseSelect{width:100%!important;min-height:52px!important;border:1px solid var(--line)!important;border-radius:16px!important;background:var(--card2)!important;color:var(--ink)!important;padding:0 14px!important;font:inherit!important;font-size:15px!important;font-weight:850!important;outline:none!important}
            .currencyScroller{display:flex!important;flex-direction:column!important;overflow:visible!important;gap:8px!important}.fx{width:100%!important;min-width:0!important;flex:none!important;border-radius:15px!important;display:grid!important;grid-template-columns:auto 1fr auto!important;align-items:center!important;padding:12px 13px!important}.fxTop{display:contents!important}.fx .flag{grid-column:1;font-size:28px!important}.fx .code{grid-column:2;font-size:15px!important}.fxName{grid-column:2;white-space:normal!important;font-size:11px!important}.fxVal{grid-column:3;grid-row:1/3;margin:0!important;font-size:17px!important}.fxHint{display:none!important}
            .marketGrid{grid-template-columns:1fr!important;gap:10px!important}.market{min-height:124px!important;padding:16px!important;border-radius:20px!important}.mi{font-size:34px!important}.mn{font-size:16px!important;font-weight:950!important;color:var(--ink)!important}.mp{font-size:22px!important;font-weight:1000!important;letter-spacing:0!important;margin-top:12px!important}.md{font-size:13px!important;font-weight:950!important;margin-top:8px!important}.md.up{color:#18b878!important}.md.down{color:#e34b69!important}.md.flat{color:var(--muted)!important}.dayLabel{font-size:10px!important;font-weight:700!important;color:var(--muted)!important;margin-inline-start:5px!important}
            .metrics{grid-template-columns:repeat(2,minmax(0,1fr))!important}.big{font-size:24px!important;letter-spacing:0!important}.status,.sectionHead small,.detailTitle span,.source,th{color:#747b91!important}
            body[data-theme=light]{--muted:#555d73!important;--line:rgba(20,24,40,.16)!important;--green:#087a53!important;--red:#b52e50!important;--cyan:#087c9d!important;--gold:#9b6a00!important;--violet:#6650ba!important;--orange:#b86216!important}body[data-theme=light] .up{color:#087a53!important}body[data-theme=light] .down{color:#b52e50!important}
            body.charts-page .basePanel,body.charts-page .heroCard,body.charts-page .marketGrid,body.charts-page .currencyScroller,body.charts-page>.wrap>.sectionHead{display:none!important}body.charts-page .hero{display:flex!important}body.charts-page .card{margin-top:12px!important}.chartBox{min-height:210px!important;overflow:hidden!important}canvas{max-width:100%!important;height:auto!important}
            @media(max-width:380px){.wrap{padding:8px!important}.baseTop{align-items:flex-start!important;flex-direction:column!important}.hero{gap:7px!important}.logo{width:42px!important;height:42px!important}h1{font-size:17px!important}.sub{font-size:9px!important}.mp{font-size:20px!important}}
            `;document.head.appendChild(st);
            window.nearest=function(points,target){if(!points||!points.length)return null;var t=new Date(target).getTime(),best=null,bd=Infinity;for(var i=0;i<points.length;i++){var pt=new Date(points[i].date+'T12:00:00').getTime();var d=Math.abs(pt-t);if(d<bd){bd=d;best=points[i]}}return bd<=4*86400000?best:null;};
            window.renderBase=function(){var b=meta(baseCurrency);$('baseName').textContent=b.flag+' '+b.name+' — '+b.code;$('assetCaption').textContent='بالـ '+b.name;$('currencyCaption').textContent='مقابل 1 '+b.code;var select='<select id="baseSelectAndroid" class="baseSelect" aria-label="عملة التطبيق">';for(var i=0;i<CURRENCIES.length;i++){var c=CURRENCIES[i];select+='<option value="'+c.code+'"'+(c.code===baseCurrency?' selected':'')+'>'+c.flag+'  '+c.name+' — '+c.code+'</option>';}select+='</select>';$('baseScroll').innerHTML=select;var el=document.getElementById('baseSelectAndroid');if(el)el.onchange=function(){baseCurrency=this.value;localStorage.setItem('base_currency_v1',baseCurrency);render();toast('تم تغيير عملة التطبيق إلى '+baseCurrency)};};
            window.renderMarkets=function(){$('marketGrid').innerHTML=Object.entries(METALS).map(function(entry){var k=entry[0],a=entry[1],pts=convertedAssetPoints(k),c=pts.length?pts[pts.length-1].price:null;var target=new Date();target.setDate(target.getDate()-1);var yp=nearest(pts,target);var ch=change(c,yp?yp.price:null);var dp=(k==='bitcoin'&&c>1000)?0:2;if(k==='gold'){$('heroLabel').textContent='سعر الذهب الآن • '+baseCurrency;$('heroPrice').textContent=fmt(c,dp)+' '+baseCurrency;}var move='— <span class="dayLabel">مقارنة بأمس</span>';if(ch.p!=null){move=(ch.p>0?'▲ ':'▼ ')+fmt(Math.abs(ch.p),2)+'% <span class="dayLabel">مقارنة بأمس</span>';}return '<div class="market '+(selected.type==='asset'&&selected.key===k?'selected':'')+'" style="--accent:'+a.accent+'" data-asset="'+k+'"><span class="mi">'+a.icon+'</span><div class="mn">'+a.name+'</div><div class="mp">'+fmt(c,dp)+' '+baseCurrency+'</div><div class="md '+ch.cls+'">'+move+'</div></div>';}).join('');document.querySelectorAll('[data-asset]').forEach(function(e){e.onclick=function(){selected={type:'asset',key:e.dataset.asset};render();$('detailName').scrollIntoView({behavior:'smooth',block:'center'})}});};
            function enhance(){try{var gold=document.getElementById('heroPrice'),update=document.getElementById('lastUpdate');var fx=[].slice.call(document.querySelectorAll('.fx'),0,4).map(function(x){var a=x.querySelector('.code'),b=x.querySelector('.fxVal');return(a?a.textContent:'')+' '+(b?b.textContent:'')}).join(' • ');var base=localStorage.getItem('base_currency_v1')||'USD';var summary='الذهب '+(gold?gold.textContent:'')+(fx?' | '+fx:'')+(update?' | تحديث '+update.textContent:'');if(window.MarketPulseAndroid){MarketPulseAndroid.saveSummary(summary,base);var items=[];document.querySelectorAll('.market[data-asset]').forEach(function(x){items.push({key:x.getAttribute('data-asset'),name:(x.querySelector('.mn')||{}).textContent||'',price:(x.querySelector('.mp')||{}).textContent||'',change:((x.querySelector('.md')||{}).textContent||'').replace('مقارنة بأمس','').trim()});});document.querySelectorAll('.fx').forEach(function(x){var code=(x.querySelector('.code')||{}).textContent||'';if(code)items.push({key:code.trim(),name:code.trim(),price:(x.querySelector('.fxVal')||{}).textContent||'',change:''});});MarketPulseAndroid.saveSnapshot(JSON.stringify(items),base);}}catch(e){}}
            var ob=new MutationObserver(enhance);ob.observe(document.body,{childList:true,subtree:true});enhance();if(window.render)render();
          }catch(e){console.log(e)}
        })();
        """.trimIndent()
        webView.evaluateJavascript(js,null)
    }

    override fun onSaveInstanceState(out:Bundle){if(::webView.isInitialized)webView.saveState(out);super.onSaveInstanceState(out)}
    @Deprecated("Deprecated in Java") override fun onBackPressed(){if(::webView.isInitialized&&webView.canGoBack())webView.goBack() else super.onBackPressed()}
    override fun onDestroy(){if(::webView.isInitialized)webView.destroy();super.onDestroy()}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
