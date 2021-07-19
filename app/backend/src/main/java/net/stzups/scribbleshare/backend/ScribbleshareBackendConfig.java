package net.stzups.scribbleshare.backend;

import net.stzups.netty.http.handler.handlers.FileRequestHandler;
import net.stzups.scribbleshare.ScribbleshareConfig;

public interface ScribbleshareBackendConfig extends ScribbleshareConfig, FileRequestHandler.Config {

}
