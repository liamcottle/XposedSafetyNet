package com.liamcottle.xposed.safetynet.api;

import android.app.AndroidAppHelper;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import com.liamcottle.xposed.safetynet.preferences.Preferences;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Locale;

public class AttestationAPIFactory {

    private AttestationAPIFactory() {
        // Prevent access to Constructor
    }

    private static Retrofit retrofit() {

        Retrofit.Builder builder = new Retrofit.Builder();
        OkHttpClient okHttpClient = new OkHttpClient();

        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request.Builder request = chain.request().newBuilder();

                // add headers
                request.header("Accept", "application/json");
                request.header("Accept-Language", acceptLanguage());
                request.header("Accept-Locale", acceptLocale());
                request.header("User-Agent", userAgent());

                // add api key
                String apiKey = Preferences.API_KEY.withinXposed().stringValue();
                if(!TextUtils.isEmpty(apiKey)){
                    request.header("X-API-Key", apiKey);
                }

                return chain.proceed(request.build());

            }
        };

        okHttpClient = okHttpClient.newBuilder().addInterceptor(interceptor).build();

        builder.client(okHttpClient);
        builder.baseUrl("https://attest.liamcottle.com/api/");
        builder.addConverterFactory(GsonConverterFactory.create());

        return builder.build();

    }

    private static String userAgent() {

        String version = "unknown";

        try {

            // get XposedSafetyNet version
            PackageInfo packageInfo = AndroidAppHelper.currentApplication().getPackageManager().getPackageInfo("com.liamcottle.xposed.safetynet", 0);
            version = packageInfo.versionName;

        } catch(Exception ignore) {}

        return String.format("XposedSafetyNet/%s", version);

    }

    private static String acceptLanguage() {
        String str = Locale.getDefault().getLanguage();
        if(!str.equals(Locale.ENGLISH.getLanguage())){
            str = str + ";q=1, en;q=0.9";
        }
        return str;
    }

    private static String acceptLocale() {
        return Locale.getDefault().toString();
    }

    public static AttestationAPI api() {
        return retrofit().create(AttestationAPI.class);
    }

}