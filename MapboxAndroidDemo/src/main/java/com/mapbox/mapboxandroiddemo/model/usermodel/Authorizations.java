package com.mapbox.mapboxandroiddemo.model.usermodel;

class Authorizations {
  private String id;
  private String[] scopes;
  private String defaultS;
  private String client;
  private String token;
  private String created;
  private String usage;
  private String note;
  private String modified;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getScopes() {
    return scopes;
  }

  public void setScopes(String[] scopes) {
    this.scopes = scopes;
  }

  public String getDefault() {
    return defaultS;
  }

  public void setDefault(String defaultS) {
    this.defaultS = defaultS;
  }

  public String getClient() {
    return client;
  }

  public void setClient(String client) {
    this.client = client;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getUsage() {
    return usage;
  }

  public void setUsage(String usage) {
    this.usage = usage;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  @Override
  public String toString() {
    return "ClassPojo [id = " + id + ", scopes = " + scopes + ", default = " + defaultS
      + ", client = " + client + ", token = " + token + ", created = " + created + ", "
      + "usage = " + usage + ", note = " + note + ", modified = " + modified + "]";
  }
}

