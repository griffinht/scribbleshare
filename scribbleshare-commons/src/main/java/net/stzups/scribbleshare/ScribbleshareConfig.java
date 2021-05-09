package net.stzups.scribbleshare;

import net.stzups.scribbleshare.data.database.implementations.PostgresDatabase;
import net.stzups.scribbleshare.data.objects.session.HttpConfig;
import net.stzups.scribbleshare.server.HttpServerInitializer;

public interface ScribbleshareConfig extends PostgresDatabase.Config, HttpServerInitializer.Config, HttpConfig {
    String getEnvironmentVariablePrefix();
    String getProperties();
    int getPort();
    String getDomain();
    String getName();
}
