package com.example.androidgamekt.widget

import android.app.IntentService
import android.content.Intent
import android.util.Xml
import com.example.androidgamekt.network.CbrService
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import org.xmlpull.v1.XmlPullParser
import com.example.androidgamekt.util.GoldRateStore

class GoldRateWidgetUpdateService : IntentService("GoldRateWidgetUpdateService") {
    override fun onHandleIntent(intent: Intent?) {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        val api = retrofit.create(CbrService::class.java)
        val today = java.time.LocalDate.now()
        val fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateStr = today.format(fmt)
        val xml = runBlocking { api.getMetalsXml(dateStr, dateStr) }
        val rate = parseGoldRate(xml)
        if (rate != null) {
            GoldRateStore.save(this, rate)
        }
        val shown = (rate ?: GoldRateStore.load(this).first)
        GoldRateWidget.updateAll(this, if (shown > 0) "Золото: $shown" else "Золото: —")
    }

    private fun parseGoldRate(xml: String): Double? {
        // CBR metals XML: <Record Date="..." Code="1"><Buy>xxxx,xx</Buy><Sell>yyyy,yy</Sell></Record>
        return try {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setInput(xml.reader())
            var event = parser.eventType
            var result: Double? = null
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name.equals("Record", true)) {
                    var codeAttr: String? = null
                    for (i in 0 until parser.attributeCount) {
                        if (parser.getAttributeName(i).equals("Code", true)) {
                            codeAttr = parser.getAttributeValue(i)
                            break
                        }
                    }
                    if (codeAttr == "1") {
                        var buy: Double? = null
                        var sell: Double? = null
                        // Iterate inside this Record
                        var innerEvent = parser.next()
                        while (!(innerEvent == XmlPullParser.END_TAG && parser.name.equals("Record", true))) {
                            if (innerEvent == XmlPullParser.START_TAG) {
                                val tagName = parser.name
                                if (tagName.equals("Buy", true) || tagName.equals("Sell", true) || tagName.equals("Value", true)) {
                                    val text = parser.nextText()
                                    val num = text.replace(',', '.').toDoubleOrNull()
                                    if (tagName.equals("Buy", true)) buy = num
                                    if (tagName.equals("Sell", true)) sell = num
                                    if (tagName.equals("Value", true)) {
                                        // Some feeds may use <Value>
                                        buy = buy ?: num
                                        sell = sell ?: num
                                    }
                                }
                            }
                            innerEvent = parser.next()
                        }
                        // Prefer Sell, then Buy
                        result = sell ?: buy
                        if (result != null) break
                    }
                }
                event = parser.next()
            }
            result
        } catch (e: Exception) {
            null
        }
    }
}


