package policygen;

public class Group extends Entity {
    public String name;

    public Group(String name, Entity parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String getEntityId() {
        return name;
    }
}
