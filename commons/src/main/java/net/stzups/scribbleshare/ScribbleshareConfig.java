package net.stzups.scribbleshare;

import net.stzups.netty.http.HttpServerInitializer;
import net.stzups.scribbleshare.data.database.implementations.PostgresDatabase;
import net.stzups.scribbleshare.data.objects.authentication.http.HttpConfig;

public interface ScribbleshareConfig extends PostgresDatabase.Config, HttpServerInitializer.Config, HttpConfig {
    String getEnvironmentVariablePrefix();
    String getProperties();
    int getPort();
    String getDomain();
    String getName();
    String getOrigin();
}
