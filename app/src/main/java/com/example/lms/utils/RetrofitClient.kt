import android.util.Log
import com.example.lms.apies.Apies
import com.example.lms.utils.Cons
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RetrofitClient {


    companion object {

        private var retrofitAuth: Retrofit? = null
        private var retrofitUser: Retrofit? = null
        private var retrofit: Retrofit? = null
        // var PROFILE_IMAGE_URL: String = Cons.BASEURL + "/images/user_image/"


        fun getLoginUserApi(): Apies {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(Cons.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .client(getClient(true))
                    .build()
            }
            return retrofit!!.create(Apies::class.java)
        }

//        fun getNewUserApi(): RetrofitApi {
//
//            if (retrofitUser == null) {
//                retrofitUser = Retrofit.Builder()
//                    .baseUrl(BASEURL)
//                    .addConverterFactory(
//                        GsonConverterFactory.create(
//                            GsonBuilder().setLenient().create()
//                        )
//                    )
//                    .client(getClient(false))
//                    .build()
//            }
//            return retrofitUser!!.create(RetrofitApi::class.java)
//        }


        private fun getClient(addHeaders: Boolean): OkHttpClient {



            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
            if (addHeaders) {
                httpClient.addInterceptor { chain: Interceptor.Chain ->
                    val request = chain.request().newBuilder()
                    request.addHeader("Content-Type", "application/json")
                    chain.proceed(request.build())
                }
            }
            return httpClient.build()

        }



    }

}

