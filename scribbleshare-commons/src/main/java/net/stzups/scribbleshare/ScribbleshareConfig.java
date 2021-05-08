package net.stzups.scribbleshare;

import net.stzups.scribbleshare.data.database.implementations.PostgresDatabase;
import net.stzups.scribbleshare.server.ServerInitializer;

public interface ScribbleshareConfig extends PostgresDatabase.Config, ServerInitializer.Config {
    String getEnvironmentVariablePrefix();
    String getProperties();
    int getPort();
    String getDomain();
    String getName();
}
