package com.liamcottle.xposed.safetynet.api;

import com.liamcottle.xposed.safetynet.api.response.AttestationAttestResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AttestationAPI {

    @FormUrlEncoded
    @POST("v1/attestation/attest")
    Call<AttestationAttestResponse> attest(@Field("nonce") String nonceBase64, @Field("package") String packageName, @Field("apk_digest") String apkDigestBase64, @Field("apk_certificate_digest") String apkCertificateDigestBase64);

}

