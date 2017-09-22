package com.nextfaze.daggie.devproxy

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper.getMainLooper
import android.security.KeyChain
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers.io
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Proxy.Type.HTTP
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private val CERT_MEDIA_TYPE = MediaType.parse("application/x-x509-ca-cert")!!
private const val CHARLES_CERT_URI = "http://www.charlesproxy.com/getssl/"
private const val NON_PROXY_HOSTS = "127.0.0.1|localhost"
private const val MAX_RETRIES = 10

/** Configures the process HTTP proxy settings, including installation of a Charles Proxy SSL certificate. */
internal class DevProxy(private val host: String, private val port: Int) {

    private val log = LoggerFactory.getLogger(DevProxy::class.java)!!

    private val okHttpClient = OkHttpClient.Builder().build()!!

    private val alias = "Charles on $host"

    private val handler = Handler(getMainLooper())

    internal fun install(context: Context) {
        if (installToSystemProperties()) {
            loadCharlesCert()
                    .filter { !isCertificateTrusted(it) }
                    .subscribe({ onCertLoaded(context, it) }, { onCertError(context) })
        }
    }

    internal fun asProxy() = Proxy(HTTP, InetSocketAddress.createUnresolved(host, port))

    private fun onCertLoaded(context: Context, certBytes: ByteArray) =
            installCertWithKeyChainIntent(context, certBytes, alias)

    private fun onCertError(context: Context) = context.showToast("Failed to install certificate")

    private fun installToSystemProperties(): Boolean {
        if (host.isEmpty() || port <= 0) {
            return false
        }
        System.setProperty("proxyHost", host)
        System.setProperty("proxyPort", port.toString())
        System.setProperty("http.nonProxyHosts", NON_PROXY_HOSTS)
        System.setProperty("https.nonProxyHosts", NON_PROXY_HOSTS)
        return true
    }

    private fun loadCharlesCert(): Maybe<ByteArray> = Maybe.fromCallable<ByteArray> { loadCharlesCertBody()?.bytes() }
            .subscribeOn(io())
            .retryWhen { it.delayedRetry() }

    private fun loadCharlesCertBody(): ResponseBody? {
        val call = okHttpClient.newCall(Request.Builder().url(CHARLES_CERT_URI).build())
        val response = call.execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP " + response.code())
        }
        val body = response.body()
        val mediaType = body?.contentType()
        if (mediaType != CERT_MEDIA_TYPE) {
            return null
        }
        return body
    }

    private fun isCertificateTrusted(certBytes: ByteArray): Boolean {
        try {
            val certificate = certBytes.toX509Certificate()
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(null as KeyStore?)
            val tms = tmf.trustManagers
            val tm = tms[0] as X509TrustManager
            tm.checkServerTrusted(arrayOf(certificate), "RSA")
            return true
        } catch (e: CertificateException) {
            log.debug("Cert not trusted")
            return false
        } catch (e: Exception) {
            log.error("Exception", e)
            return false
        }
    }

    private fun ByteArray.toX509Certificate() =
            CertificateFactory.getInstance("X.509").generateCertificate(ByteArrayInputStream(this)) as X509Certificate

    private fun Context.showToast(message: String, vararg args: Any) {
        handler.post { Toast.makeText(this, String.format(message, args), LENGTH_SHORT).show() }
    }
}

private fun installCertWithKeyChainIntent(
        context: Context,
        certBytes: ByteArray,
        alias: String
) = context.startActivity(KeyChain.createInstallIntent()
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(KeyChain.EXTRA_CERTIFICATE, certBytes)
        .putExtra(KeyChain.EXTRA_NAME, alias))

private fun <T : Throwable> Flowable<T>.delayedRetry(): Flowable<Long> =
        zipWith(Flowable.range(0, MAX_RETRIES - 1), BiFunction<Throwable, Int, Pair<Throwable, Int>> { e, attempt -> e to attempt })
                .flatMap {
                    if (it is IOException) Flowable.timer(3, TimeUnit.SECONDS)
                    else Flowable.error(it.first)
                }
