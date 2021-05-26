package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Resource;
import org.jetbrains.annotations.Nullable;

public interface ResourceDatabase {
    /**
     * @return id for the newly added {@link Resource}
     */
    long addResource(long owner, Resource resource) throws DatabaseException;

    void updateResource(long id, long owner, Resource resource) throws DatabaseException;

    /**
     * @return null if the {@link Resource} does not exist for {@param id} and {@param owner}
     */
    @Nullable Resource getResource(long id, long owner);
}
