package com.liamcottle.xposed.safetynet.hooks;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.util.Base64;
import com.liamcottle.xposed.safetynet.api.AttestationAPIFactory;
import com.liamcottle.xposed.safetynet.api.response.AttestationAttestResponse;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import retrofit2.Response;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.MessageDigest;

public class SafetyNetHook extends Hook {

    // classes
    private Class<?> mSafetyNetClass;
    private Class<?> mSafetyNetApiImplClass;
    private Class<?> mGoogleApiClientClass;
    private Class<?> mAttestationResultClass;
    private Class<?> mAttestationResultImplClass;
    private Class<?> mStatusClass;

    // constructors
    private Constructor<?> mStatusConstructor;

    // methods
    private Method mAttestMethod;
    private Method mGetStatusMethod;
    private Method mGetJwsResultMethod;

    // static variables
    private static final Object sSynchronizeObject = new Object();
    private static String sCachedAttestation = null;

    public SafetyNetHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        super(loadPackageParam);
    }

    @Override
    public String tag() {
        return "SafetyNetHook";
    }

    @Override
    public void load() throws Throwable {

        // hook application attach
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                // find classes
                mSafetyNetClass = findClass("com.google.android.gms.safetynet.SafetyNet");
                mSafetyNetApiImplClass = mSafetyNetClass.getDeclaredField("SafetyNetApi").get(null).getClass();
                mGoogleApiClientClass = findClass("com.google.android.gms.common.api.GoogleApiClient");
                mAttestationResultClass = findClass("com.google.android.gms.safetynet.SafetyNetApi.AttestationResult");
                mAttestationResultImplClass = findAttestationResultImplClass();
                mStatusClass = findStatusClass();

                // find constructors
                mStatusConstructor = mStatusClass.getDeclaredConstructor(int.class);

                // find methods
                mAttestMethod = mSafetyNetApiImplClass.getDeclaredMethod("attest", mGoogleApiClientClass, byte[].class);
                mGetStatusMethod = mAttestationResultImplClass.getDeclaredMethod("getStatus");
                mGetJwsResultMethod = mAttestationResultImplClass.getDeclaredMethod("getJwsResult");

                // hook AttestationResult getStatus method
                XposedBridge.hookMethod(mGetStatusMethod, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                        // check if cached attestation is null
                        if(sCachedAttestation == null || sCachedAttestation.isEmpty()){

                            // return new Status with code 8 (INTERNAL_ERROR)
                            mStatusConstructor.setAccessible(true);
                            return mStatusConstructor.newInstance(8);

                        }

                        // return new Status with code 0 (SUCCESS)
                        mStatusConstructor.setAccessible(true);
                        return mStatusConstructor.newInstance(0);

                    }
                });

                // hook AttestationResult getJwsResult method
                XposedBridge.hookMethod(mGetJwsResultMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        // return cached attestation
                        param.setResult(sCachedAttestation);

                    }
                });

                // hook attest method
                XposedBridge.hookMethod(mAttestMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                        try {

                            // get nonce from attest method call
                            byte[] nonce = (byte[]) methodHookParam.args[1];

                            // get current thread policy
                            StrictMode.ThreadPolicy oldThreadPolicy = StrictMode.getThreadPolicy();

                            // change thread policy to allow network on main thread
                            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

                            // get attestation from api
                            Response<AttestationAttestResponse> response = AttestationAPIFactory.api().attest(
                                    Base64.encodeToString(nonce, Base64.DEFAULT),
                                    AndroidAppHelper.currentPackageName(),
                                    getApkDigest(),
                                    getApkCertificateDigest()
                            ).execute();

                            // revert thread policy
                            StrictMode.setThreadPolicy(oldThreadPolicy);

                            // check if response was successful
                            if(!response.isSuccessful()){
                                throw new Exception("Attestation was not Successful");
                            }

                            // get parsed attestation attest response
                            AttestationAttestResponse attestationAttestResponse = response.body();

                            // check if attestation attest response is ok
                            if(!attestationAttestResponse.isOk()){
                                throw new Exception(attestationAttestResponse.getMessage());
                            }

                            // set cached attestation
                            sCachedAttestation = attestationAttestResponse.getAttestation();

                        } catch(Throwable throwable) {

                            // unset cached attestation which will cause a status of 8 (INTERNAL_ERROR)
                            sCachedAttestation = null;

                            XposedBridge.log(throwable);

                        }

                    }
                });

            }
        });

    }

    private String getApkDigest() {

        try {

            // get file input stream for this package's apk file
            FileInputStream fileInputStream = new FileInputStream(new File(AndroidAppHelper.currentApplicationInfo().sourceDir));

            // get sha256 message digest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] data = new byte[16384];
            while(true){
                int read = fileInputStream.read(data);
                if(read < 0){
                    break;
                }
                digest.update(data, 0, read);
            }

            // convert sha256 digest to base64
            return Base64.encodeToString(digest.digest(), Base64.DEFAULT);

        } catch (Exception e) {
            XposedBridge.log(e);
            return null;
        }

    }

    @SuppressLint("PackageManagerGetSignatures")
    private String getApkCertificateDigest() {

        try {

            // get package info including signatures for the current application
            PackageInfo packageInfo = AndroidAppHelper.currentApplication().getPackageManager().getPackageInfo(AndroidAppHelper.currentApplication().getPackageName(), PackageManager.GET_SIGNATURES);

            // get sha256 message digest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // convert first signature to base64
            if(packageInfo.signatures != null && packageInfo.signatures.length > 0){
                return Base64.encodeToString(digest.digest(packageInfo.signatures[0].toByteArray()), Base64.DEFAULT);
            }

        } catch (Exception e){
            XposedBridge.log(e);
        }

        return null;

    }

    private Class<?> findAttestationResultImplClass() {

        // get classes declared inside the SafetyNetApi impl class
        Class<?>[] classes = mSafetyNetApiImplClass.getDeclaredClasses();

        // iterate each declared class
        for(Class<?> clazz : classes){

            // find and return the class that is a subclass of the AttestationResult class
            if(mAttestationResultClass.isAssignableFrom(clazz)){
                return clazz;
            }

        }

        return null;

    }

    private Constructor<?> findAttestationResultImplConstructor() {

        // get constructors for AttestationResult impl class
        Constructor<?>[] constructors = mAttestationResultImplClass.getDeclaredConstructors();

        // iterate each constructor
        for(Constructor<?> constructor : constructors){

            // get parameter types for this constructor
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            // the constructor that has two parameters is the one we want
            if(parameterTypes.length == 2){
                return constructor;
            }

        }

        return null;

    }

    private Class<?> findStatusClass() {

        // find the AttestationResult impl constructor
        Constructor<?> constructor = findAttestationResultImplConstructor();

        // null check
        if(constructor != null){

            // the first parameter type is the status class
            return constructor.getParameterTypes()[0];

        }

        return null;

    }

}
