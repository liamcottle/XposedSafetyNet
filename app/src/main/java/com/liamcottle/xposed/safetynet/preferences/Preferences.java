package com.liamcottle.xposed.safetynet.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liamcottle.xposed.safetynet.XposedSafetyNetApplication;
import de.robv.android.xposed.XSharedPreferences;

import java.util.ArrayList;
import java.util.List;

public enum Preferences {

    // package names enabled
    PACKAGE_NAMES_ENABLED("package_names_enabled", List.class, new ArrayList<>());

    private String mKey;
    private Class mClazz;
    private Object mDefaultValue;

    public static SharedPreferences sSharedPreferences;

    /**
     * Enum based SharedPreference
     * @param key String SharedPreference Key
     * @param clazz Class SharedPreference Value Class Type
     * @param defaultValue Object SharedPreference Default Value
     */
    Preferences(String key, Class clazz, Object defaultValue) {

        mKey = key;
        mClazz = clazz;
        mDefaultValue = defaultValue;

        // default value must be same type as class passed in for generics
        if(mDefaultValue != null && !mClazz.isInstance(mDefaultValue)){
            throw new IllegalArgumentException(String.format(
                    "Expected class of type %s for default value but got %s for key %s",
                    mClazz.getSimpleName(),
                    mDefaultValue.getClass().getSimpleName(),
                    mKey
            ));
        }

    }

    private String getPreferencesName() {
        return String.format("%s_preferences", XposedSafetyNetApplication.class.getPackage().getName());
    }

    public Preferences withinSelf() {
        sSharedPreferences = XposedSafetyNetApplication.get().getSharedPreferences(getPreferencesName(), Context.MODE_WORLD_READABLE);
        return this;
    }

    public Preferences withinXposed() {
        String packageName = XposedSafetyNetApplication.class.getPackage().getName();
        sSharedPreferences = new XSharedPreferences(packageName, getPreferencesName());
        return this;
    }

    private Gson getGson() {
        return new Gson();
    }

    public boolean booleanValue() {
        return sSharedPreferences.getBoolean(mKey, (boolean) mDefaultValue);
    }

    public float floatValue() {
        return sSharedPreferences.getFloat(mKey, (float) mDefaultValue);
    }

    public int intValue() {
        return sSharedPreferences.getInt(mKey, (int) mDefaultValue);
    }

    public long longValue() {
        return sSharedPreferences.getLong(mKey, (long) mDefaultValue);
    }

    public String stringValue() {
        return sSharedPreferences.getString(mKey, (String) mDefaultValue);
    }

    public List<String> stringListValue() {
        return (List<String>) typedValue(new TypeToken<List<String>>(){}.getRawType());
    }

    public <T> T typedValue(Class<T> clazz) {

        // class passed in for generics must be same as class associated with this key
        if(mClazz != clazz){
            throw new IllegalArgumentException(String.format(
                    "Expected class of type %s but got %s for key %s",
                    mClazz.getSimpleName(),
                    clazz.getSimpleName(),
                    mKey
            ));
        }

        // convert json string to instance of class with gson
        T typedResult = getGson().fromJson(sSharedPreferences.getString(mKey, null), clazz);

        // check if gson result is non null
        if(typedResult != null){
            return typedResult;
        }

        // return default value (casting should be fine as default value is checked in constructor)
        return (T) mDefaultValue;

    }

    public boolean remove() {
        return sSharedPreferences.edit().remove(mKey).commit();
    }

    public boolean exists() {
        return sSharedPreferences.contains(mKey);
    }

    public boolean set(Object value) {

        // if value is null, remove the key
        if(value == null){
            return remove();
        }

        // value must be required class type
        if(!mClazz.isInstance(value)){
            throw new IllegalArgumentException(String.format(
                    "Expected object of type %s but got %s for key %s",
                    mClazz.getSimpleName(),
                    value.getClass().getSimpleName(),
                    mKey
            ));
        }

        if(mClazz == Boolean.class){
            return sSharedPreferences.edit().putBoolean(mKey, (boolean) value).commit();
        }

        if(mClazz == Float.class){
            return sSharedPreferences.edit().putFloat(mKey, (float) value).commit();
        }

        if(mClazz == Integer.class){
            return sSharedPreferences.edit().putInt(mKey, (int) value).commit();
        }

        if(mClazz == Long.class){
            return sSharedPreferences.edit().putLong(mKey, (long) value).commit();
        }

        if(mClazz == String.class){
            return sSharedPreferences.edit().putString(mKey, (String) value).commit();
        }

        // store all other types as json string
        return sSharedPreferences.edit().putString(mKey, getGson().toJson(value)).commit();

    }

}
