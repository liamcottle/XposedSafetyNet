package com.liamcottle.xposed.safetynet.api.response;

import com.google.gson.annotations.SerializedName;

public class AttestationAttestResponse extends Response {

    @SerializedName("attestation")
    private String attestation;

    public String getAttestation() {
        return attestation;
    }

}