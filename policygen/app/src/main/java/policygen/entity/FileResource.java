package policygen.entity;

import java.util.UUID;

public class FileResource extends Entity {
    String fileName;

    public FileResource(Entity parent) {
        fileName = UUID.randomUUID().toString();
        this.parent = parent;
    }

    @Override
    public String getEntityId() {
        return fileName;
    }

    @Override
    public String getEntityType() {
        return "File";
    }

}
