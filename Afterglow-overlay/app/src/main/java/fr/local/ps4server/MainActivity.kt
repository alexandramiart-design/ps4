package fr.local.ps4server

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import java.net.Inet4Address
import java.net.NetworkInterface

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var statusDot: TextView
    private lateinit var address: TextView
    private lateinit var toggle: TextView
    private var requestedRunning = false
    private val port = ServerService.PORT
    private val bg = Color.rgb(8,11,24)
    private val panel = Color.rgb(21,26,43)
    private val muted = Color.rgb(153,164,190)
    private val white = Color.rgb(243,245,255)
    private val purple = Color.rgb(167,139,250)
    private val green = Color.rgb(74,222,128)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refresh()
    }

    private fun dp(v:Int) = (v * resources.displayMetrics.density).toInt()
    private fun rounded(c:Int,r:Float=22f,stroke:Int=0) = GradientDrawable().apply {
        setColor(c)
        cornerRadius=r*resources.displayMetrics.density
        if(stroke!=0) setStroke(dp(1),stroke)
    }
    private fun gradient() = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(Color.rgb(139,92,246),Color.rgb(37,99,235))).apply { cornerRadius=dp(18).toFloat() }
    private fun label(v:String,s:Float,c:Int=white,b:Boolean=false) = TextView(this).apply {
        text=v
        textSize=s
        setTextColor(c)
        if(b) setTypeface(typeface,Typeface.BOLD)
        includeFontPadding=true
    }

    private fun buildUi() {
        val scroll=ScrollView(this).apply { setBackgroundColor(bg); isFillViewport=true }
        val root=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(22),dp(28),dp(22),dp(24))
            setBackgroundColor(bg)
        }
        scroll.addView(root)

        val brand=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        val logo=ImageView(this).apply {
            setImageResource(R.drawable.afterglow_icon)
            scaleType=ImageView.ScaleType.CENTER_CROP
            background=rounded(panel,18f)
            clipToOutline=true
        }
        brand.addView(logo,LinearLayout.LayoutParams(dp(62),dp(62)))
        val brandText=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(14),0,0,0)
            addView(label("AFTERGLOW",24f,white,true))
            addView(label("PS4 LOCAL HOST  •  FIRMWARE 14.02",10f,muted,true))
        }
        brand.addView(brandText)
        root.addView(brand)
        root.addView(label("Ton serveur PS4,\ndirectement depuis ton téléphone.",16f,muted).apply {
            setPadding(0,dp(25),0,dp(22))
        })

        val card=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(20),dp(19),dp(20),dp(19))
            background=rounded(panel,22f,Color.rgb(45,51,76))
        }
        val line=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        statusDot=label("●",16f,Color.rgb(251,191,36),true)
        line.addView(statusDot)
        status=label("Serveur arrêté",18f,white,true).apply { setPadding(dp(9),0,0,0) }
        line.addView(status)
        card.addView(line)
        card.addView(label("ÉTAT DU SERVEUR",10f,muted,true).apply { setPadding(0,dp(4),0,dp(15)) })
        card.addView(label("ADRESSE À OUVRIR SUR LA PS4",10f,muted,true))
        address=label(serverUrl(),15f,white,true).apply {
            setPadding(0,dp(8),0,dp(15))
            typeface=Typeface.MONOSPACE
        }
        card.addView(address)
        root.addView(card)

        toggle=label("▶   DÉMARRER LE SERVEUR",14f,Color.WHITE,true).apply {
            gravity=Gravity.CENTER
            background=gradient()
            setPadding(dp(16),dp(18),dp(16),dp(18))
            setOnClickListener { toggleServer() }
        }
        root.addView(toggle,LinearLayout.LayoutParams(-1,dp(58)).apply { topMargin=dp(18) })

        val copy=label("▣   COPIER L’ADRESSE",13f,white,true).apply {
            gravity=Gravity.CENTER
            background=rounded(Color.rgb(30,37,59),18f,Color.rgb(61,70,102))
            setPadding(dp(16),dp(16),dp(16),dp(16))
            setOnClickListener {
                val u=serverUrl()
                val cb=getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cb.setPrimaryClip(ClipData.newPlainText("Adresse Afterglow",u))
                Toast.makeText(this@MainActivity,"Adresse copiée !",Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(copy,LinearLayout.LayoutParams(-1,dp(54)).apply { topMargin=dp(11) })

        val info=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(18),dp(16),dp(18),dp(16))
            background=rounded(Color.rgb(15,20,36),18f)
        }
        info.addView(label("COMMENT SE CONNECTER",11f,purple,true))
        info.addView(label(
            "1. Connecte ton téléphone et ta PS4 au même Wi-Fi.\n2. Démarre le serveur Afterglow.\n3. Ouvre l’adresse affichée dans le navigateur de la PS4.",
            12f,muted
        ).apply { setLineSpacing(dp(4).toFloat(),1f); setPadding(0,dp(10),0,0) })
        root.addView(info,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(22) })
        root.addView(label("AFTERGLOW  •  HÉBERGEMENT LOCAL",10f,Color.rgb(93,104,133)).apply {
            gravity=Gravity.CENTER
            setPadding(0,dp(25),0,0)
        })
        setContentView(scroll)
    }

    private fun toggleServer() {
        if(requestedRunning) {
            stopService(Intent(this,ServerService::class.java))
            requestedRunning=false
        } else {
            startForegroundService(Intent(this,ServerService::class.java).setAction("START"))
            requestedRunning=true
        }
        refresh()
    }

    private fun refresh() {
        if(!::status.isInitialized) return
        status.text=if(requestedRunning) "Serveur en cours de démarrage" else "Serveur arrêté"
        statusDot.setTextColor(if(requestedRunning) green else Color.rgb(251,191,36))
        address.text=serverUrl()
        toggle.text=if(requestedRunning) "■   ARRÊTER LE SERVEUR" else "▶   DÉMARRER LE SERVEUR"
        toggle.background=if(requestedRunning) rounded(Color.rgb(119,42,72),18f) else gradient()
    }

    private fun serverUrl() = "http://" + localIp() + ":" + port + "/"
    private fun localIp():String = try {
        NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
            .firstOrNull { it is Inet4Address && !it.isLoopbackAddress && it.isSiteLocalAddress }?.hostAddress ?: "IP indisponible"
    } catch(_:Exception) { "IP indisponible" }
}
