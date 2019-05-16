package org.openqa.selenium.devtools.network.types;

import org.openqa.selenium.json.JsonInput;

public class AuthChallenge {

  /**
   * Origin of the challenger.
   */
  private String origin;
  /**
   * The realm of the challenge. May be empty.
   */
  private String realm;
  /**
   * The authentication scheme used, such as basic or digest
   */
  private String scheme;
  /**
   * Source of the authentication challenge.
   * Optional
   */
  private Source source;

  public static AuthChallenge parseRequest(JsonInput input) {
    AuthChallenge authChallenge = new AuthChallenge();
    while (input.hasNext()){
      switch (input.nextName()) {
        case "origin" :
          authChallenge.setOrigin(input.nextString());
          break;
        case "realm" :
          authChallenge.setRealm(input.nextString());
          break;
        case "scheme" :
          authChallenge.setScheme(input.nextString());
          break;
        case "source" :
          authChallenge.setSource(Source.getSource(input.nextString()));
          break;
        default:
          input.skipValue();
          break;
      }
    }
    return authChallenge;
  }


  public String getScheme() {
    return scheme;
  }

  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }
}
