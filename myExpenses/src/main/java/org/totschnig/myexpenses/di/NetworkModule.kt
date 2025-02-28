package org.totschnig.myexpenses.di

import android.net.TrafficStats
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import org.totschnig.myexpenses.BuildConfig
import org.totschnig.myexpenses.MyApplication
import org.totschnig.myexpenses.preference.PrefHandler
import org.totschnig.myexpenses.provider.ExchangeRateRepository
import org.totschnig.myexpenses.retrofit.ExchangeRateHost
import org.totschnig.myexpenses.retrofit.ExchangeRateService
import org.totschnig.myexpenses.retrofit.OpenExchangeRates
import org.totschnig.myexpenses.retrofit.RoadmapService
import org.totschnig.myexpenses.room.ExchangeRateDatabase.Companion.getDatabase
import org.totschnig.myexpenses.util.DelegatingSocketFactory
import org.totschnig.myexpenses.viewmodel.repository.RoadmapRepository.Companion.ROADMAP_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException
import java.lang.reflect.Type
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.SocketFactory

@Module
open class NetworkModule {
    @Provides
    @Singleton
    open fun provideSocketFactory(): SocketFactory =
        object : DelegatingSocketFactory(getDefault()) {
            @Throws(IOException::class)
            override fun configureSocket(socket: Socket): Socket {
                TrafficStats.setThreadStatsTag(0)
                TrafficStats.tagSocket(socket)
                return socket
            }
        }

    companion object {
        @JvmStatic
        @Provides
        fun providePicasso(): Picasso = Picasso.get()

        @JvmStatic
        @Provides
        fun provideOkHttpClientBuilder(
            loggingInterceptor: HttpLoggingInterceptor?,
            socketFactory: SocketFactory
        ) = OkHttpClient.Builder().apply {
            if (loggingInterceptor != null) {
                addInterceptor(loggingInterceptor)
            }
            if (BuildConfig.DEBUG) {
                eventListener(object : EventListener() {
                    override fun connectFailed(
                        call: Call,
                        inetSocketAddress: InetSocketAddress,
                        proxy: Proxy,
                        protocol: Protocol?,
                        ioe: IOException
                    ) {
                        super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe)
                        Timber.e(ioe, "Connect failed")
                    }
                })
            }
            socketFactory(socketFactory)
        }

        @JvmStatic
        @Provides
        @Singleton
        fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor? = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }
        } else null

        @JvmStatic
        @Provides
        @Singleton
        fun provideGson(localDateJsonDeserializer: JsonDeserializer<LocalDate?>?): Gson =
            GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, localDateJsonDeserializer)
                .create()

        @JvmStatic
        @Provides
        @Singleton
        fun provideCache(context: MyApplication): Cache? = context.cacheDir?.let {
            Cache(it, 1024 * 1024)
        }

        @JvmStatic
        @Provides
        @Singleton
        fun provideExchangeRateHost(
            builder: OkHttpClient.Builder,
            gson: Gson,
            cache: Cache?
        ): ExchangeRateHost {
            builder.cache(cache)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.exchangerate.host/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder.build())
                .build()
            return retrofit.create(ExchangeRateHost::class.java)
        }

        @JvmStatic
        @Provides
        @Singleton
        fun provideOpenExchangeRates(
            builder: OkHttpClient.Builder,
            gson: Gson,
            cache: Cache?
        ): OpenExchangeRates {
            builder.cache(cache)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://openexchangerates.org/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder.build())
                .build()
            return retrofit.create(OpenExchangeRates::class.java)
        }

        @JvmStatic
        @Provides
        @Singleton
        fun provideExchangeRateService(
            api1: ExchangeRateHost,
            api2: OpenExchangeRates
        ) = ExchangeRateService(api1, api2)

        @JvmStatic
        @Provides
        @Singleton
        fun provideExchangeRateRepository(
            application: MyApplication, service: ExchangeRateService, prefHandler: PrefHandler
        ) = ExchangeRateRepository(
            getDatabase(application).exchangeRateDao(),
            prefHandler,
            service
        )

        @JvmStatic
        @Provides
        @Singleton
        fun provideLocalDateJsonDeserializer(): JsonDeserializer<LocalDate> =
            JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? ->
                LocalDate.parse(
                    json.asJsonPrimitive.asString
                )
            }

        @JvmStatic
        @Provides
        @Singleton
        fun provideRoadmapService(builder: OkHttpClient.Builder): RoadmapService {
            val okHttpClient: OkHttpClient = builder
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(ROADMAP_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
            return retrofit.create(RoadmapService::class.java)
        }
    }
}