package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.database.exception.DatabaseException;
import net.stzups.scribbleshare.data.objects.Resource;
import org.jetbrains.annotations.Nullable;

public interface ResourceDatabase {
    /**
     * @param owner owner of new {@link Resource}
     * @return id of the newly added {@link Resource}
     */
    long addResource(long owner, Resource resource) throws DatabaseException;

    void updateResource(long id, long owner, Resource resource) throws DatabaseException;

    /**
     * @param id id of {@link Resource}
     * @param owner owner of {@link Resource}
     * @return null if the {@link Resource} does not exist
     */
    @Nullable Resource getResource(long id, long owner);
}
