package com.mapbox.mapboxandroiddemo.model.usermodel;

/**
 * Created by LangstonSmith on 5/9/17.
 */

public class User {
    private Authorizations[] authorizations;

    private String flags;

    private String mfa;

    private String extraStorage;

    private String mfaRecover;

    private String passmod;

    private String avatar;

    private String id;

    private String lastLogin;

    private Plan plan;

    private String email;

    private String created;

    private String extraTm2z;

    private String accountLevel;

    private String customerID;

    private String disabled;

    public Authorizations[] getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(Authorizations[] authorizations) {
        this.authorizations = authorizations;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getMfa() {
        return mfa;
    }

    public void setMfa(String mfa) {
        this.mfa = mfa;
    }

    public String getExtraStorage() {
        return extraStorage;
    }

    public void setExtraStorage(String extraStorage) {
        this.extraStorage = extraStorage;
    }

    public String getMfaRecover() {
        return mfaRecover;
    }

    public void setMfaRecover(String mfaRecover) {
        this.mfaRecover = mfaRecover;
    }

    public String getPassmod() {
        return passmod;
    }

    public void setPassmod(String passmod) {
        this.passmod = passmod;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getExtraTm2z() {
        return extraTm2z;
    }

    public void setExtraTm2z(String extraTm2z) {
        this.extraTm2z = extraTm2z;
    }

    public String getAccountLevel() {
        return accountLevel;
    }

    public void setAccountLevel(String accountLevel) {
        this.accountLevel = accountLevel;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getDisabled() {
        return disabled;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
}