/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sikuli.scriptrunner;

public class RemoteRunner {

  private boolean valid = false;

  public RemoteRunner(String adr, String p) {
    init(adr, p);
  }

  private void init(String adr, String p) {

  }

  public boolean isValid() {
    return valid;
  }

  public int runRemote(String[] args) {
    return 0;
  }

}
