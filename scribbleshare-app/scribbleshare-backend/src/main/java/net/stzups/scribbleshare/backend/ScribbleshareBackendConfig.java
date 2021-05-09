package net.stzups.scribbleshare.backend;

import net.stzups.scribbleshare.ScribbleshareConfig;
import net.stzups.scribbleshare.backend.server.http.HttpServerHandler;

public interface ScribbleshareBackendConfig extends ScribbleshareConfig, HttpServerHandler.Config {

}
