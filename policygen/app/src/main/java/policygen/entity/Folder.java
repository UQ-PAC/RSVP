package policygen.entity;

import java.util.UUID;

public class Folder extends Entity {
    String folderName;

    public Folder(Entity parent) {
        folderName = UUID.randomUUID().toString();
        this.parent = parent;
    }

    @Override
    public String getEntityId() {
        return folderName;
    }
}
