package com.explapp.marketpulse

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("market_pulse", MODE_PRIVATE) }

    data class IntroPage(
        val icon: String,
        val eyebrow: String,
        val title: String,
        val description: String,
        val accent1: Int,
        val accent2: Int
    )

    private val introPages = listOf(
        IntroPage(
            "✦", "كل السوق في شاشة واحدة", "تابع الذهب والعملات بثقة",
            "أسعار الذهب والفضة والبيتكوين والعملات الخليجية والدينار الأردني والدولار واليورو، في واجهة واحدة سريعة وواضحة.",
            Color.rgb(255, 207, 85), Color.rgb(255, 139, 74)
        ),
        IntroPage(
            "↕", "عملة التطبيق", "اختر العملة التي تناسبك",
            "الدولار هو الاختيار الافتراضي. غيّره إلى الدينار الأردني أو الريال السعودي أو الدرهم الإماراتي أو غيرها، وستتحول كل الأسعار تلقائيًا.",
            Color.rgb(67, 216, 255), Color.rgb(88, 126, 255)
        ),
        IntroPage(
            "⌁", "تحليل زمني ذكي", "اعرف أين كان السعر",
            "قارن السعر الحالي مع أمس، قبل أسبوع، شهر، 3 أشهر، 6 أشهر وسنة، مع الفرق ونسبة التغير والرسم البياني.",
            Color.rgb(155, 124, 255), Color.rgb(218, 92, 255)
        ),
        IntroPage(
            "✉", "الدعم والتواصل", "نحن قريبون منك",
            "إذا كان لديك اقتراح أو ملاحظة يمكنك مراسلة المطور مباشرة من داخل التطبيق.",
            Color.rgb(75, 229, 167), Color.rgb(67, 216, 255)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 9, 19)
        window.navigationBarColor = Color.rgb(8, 9, 19)
        if (!prefs.getBoolean("onboarding_done", false)) showOnboarding(0) else showWebApp(savedInstanceState)
    }

    private fun gradient(c1: Int, c2: Int, radius: Float = 42f): GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, intArrayOf(c1, c2)
    ).apply { cornerRadius = radius }

    private fun showOnboarding(index: Int) {
        val page = introPages[index]
        val root = FrameLayout(this).apply { setBackgroundColor(Color.rgb(8, 9, 19)) }

        val glow = View(this).apply {
            background = gradient(page.accent1, page.accent2, 999f)
            alpha = 0.16f
        }
        root.addView(glow, FrameLayout.LayoutParams(dp(310), dp(310), Gravity.TOP or Gravity.END).apply {
            topMargin = dp(-90); marginEnd = dp(-100)
        })

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(26), dp(28), dp(26), dp(28))
        }
        root.addView(content, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        val brand = TextView(this).apply {
            text = "M+  مؤشر الأسواق"
            setTextColor(Color.WHITE)
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
        }
        top.addView(brand, LinearLayout.LayoutParams(0, dp(48), 1f))
        val skip = TextView(this).apply {
            text = "تخطي"
            setTextColor(Color.rgb(164, 169, 194))
            textSize = 14f
            gravity = Gravity.CENTER
            setOnClickListener { finishOnboarding() }
        }
        top.addView(skip, LinearLayout.LayoutParams(dp(64), dp(48)))
        content.addView(top, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val spacer = View(this)
        content.addView(spacer, LinearLayout.LayoutParams(1, 0, 0.55f))

        val iconBox = TextView(this).apply {
            text = page.icon
            gravity = Gravity.CENTER
            textSize = 54f
            setTextColor(Color.rgb(18, 16, 28))
            background = gradient(page.accent1, page.accent2, 48f)
            elevation = dp(12).toFloat()
        }
        content.addView(iconBox, LinearLayout.LayoutParams(dp(118), dp(118)).apply { bottomMargin = dp(28) })

        val eyebrow = TextView(this).apply {
            text = page.eyebrow
            setTextColor(page.accent1)
            textSize = 13f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
        }
        content.addView(eyebrow)

        val title = TextView(this).apply {
            text = page.title
            setTextColor(Color.WHITE)
            textSize = 31f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(10), 0, dp(12))
        }
        content.addView(title)

        val desc = TextView(this).apply {
            text = page.description
            setTextColor(Color.rgb(166, 171, 196))
            textSize = 16f
            gravity = Gravity.CENTER
            setLineSpacing(dp(4).toFloat(), 1f)
        }
        content.addView(desc, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val spacer2 = View(this)
        content.addView(spacer2, LinearLayout.LayoutParams(1, 0, 0.45f))

        val dots = TextView(this).apply {
            text = introPages.indices.joinToString("  ") { if (it == index) "●" else "○" }
            setTextColor(page.accent1)
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(18))
        }
        content.addView(dots)

        if (index == introPages.lastIndex) {
            val email = Button(this).apply {
                text = "✉  مراسلة المطور"
                setTextColor(Color.WHITE)
                textSize = 15f
                isAllCaps = false
                background = gradient(Color.rgb(31, 35, 58), Color.rgb(20, 23, 42), 28f)
                setOnClickListener { contactDeveloper() }
            }
            content.addView(email, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)).apply { bottomMargin = dp(10) })
        }

        val next = Button(this).apply {
            text = if (index == introPages.lastIndex) "ابدأ الآن" else "التالي"
            setTextColor(Color.rgb(18, 16, 28))
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            isAllCaps = false
            background = gradient(page.accent1, page.accent2, 30f)
            setOnClickListener {
                if (index == introPages.lastIndex) finishOnboarding() else showOnboarding(index + 1)
            }
        }
        content.addView(next, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)))
        setContentView(root)
    }

    private fun finishOnboarding() {
        prefs.edit().putBoolean("onboarding_done", true).apply()
        showWebApp(null)
    }

    private fun contactDeveloper() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:fastunllocked2017@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "مراسلة مطور مؤشر الأسواق")
        }
        runCatching { startActivity(intent) }
    }

    private fun showWebApp(savedInstanceState: Bundle?) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(8, 9, 19))
        }

        webView = WebView(this)
        webView.setBackgroundColor(Color.rgb(8, 9, 19))
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = "$userAgentString MarketPulseAndroid/1.4"
        }
        webView.clearCache(true)

        root.addView(webView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        val contactBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(7), dp(12), dp(7))
            setBackgroundColor(Color.rgb(10, 12, 24))
        }
        val contactButton = Button(this).apply {
            text = "✉  مراسلة المطور"
            setTextColor(Color.WHITE)
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            isAllCaps = false
            background = gradient(Color.rgb(82, 67, 155), Color.rgb(45, 157, 190), 26f)
            setOnClickListener { contactDeveloper() }
        }
        contactBar.addView(contactButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(46)
        ))
        root.addView(contactBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(60)
        ))

        setContentView(root)
        if (savedInstanceState == null) webView.loadUrl("https://aljwaal1.github.io/Goldiphone/?app=android&v=8")
        else webView.restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::webView.isInitialized) webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
