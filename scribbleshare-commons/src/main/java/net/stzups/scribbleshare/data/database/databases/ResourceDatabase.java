package net.stzups.scribbleshare.data.database.databases;

import net.stzups.scribbleshare.data.objects.Resource;

public interface ResourceDatabase {
    /**
     * Add resource to database and return the corresponding id for the new resource
     */
    long addResource(long owner, Resource resource);

    /** update resource */
    void updateResource(long id, long owner, Resource resource);

    /**
     * Gets resource, or null if the resource does not exist
     */
    Resource getResource(long id, long owner);
}
