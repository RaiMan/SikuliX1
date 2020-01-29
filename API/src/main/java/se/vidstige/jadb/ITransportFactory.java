/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package se.vidstige.jadb;

import java.io.IOException;

/**
 * Created by Törcsi on 2016. 03. 01..
 */
public interface ITransportFactory {
    Transport createTransport() throws IOException;
}
