# XposedSafetyNet

This is an Xposed Module that intercepts SafetyNet Attestation calls and passes them to an API which provides a valid attestation with `ctsProfileMatch=true` and `basicIntegrity=true`.

It allows you to use Android applications which use Google SafetyNet Attestation on a device that fails the SafetyNet check. (Usually due to Root or Xposed being installed)

Note: This module communicates with an external API maintained by me, which provides the valid attestations on demand. This external API is closed source and is not free.

## How to Use ?

- Obtain an API Key
- Install XposedSafetyNet APK from Releases
- Enable XposedSafetyNet in Xposed Modules
- Launch XposedSafetyNet
    - Set your API Key
    - Enable the apps you want XposedSafetyNet to provide Attestations
- Reboot Device

## Obtain an API Key ?

Contact me for information on purchasing an API Key.

You will need to provide me with a list of application package names you want to use XposedSafetyNet with.

I will provision an API Key and whitelist package names that I approve.

Packages that are not approved on my end will be rejected access to Attestations.