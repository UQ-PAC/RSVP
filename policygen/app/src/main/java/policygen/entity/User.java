package policygen.entity;

public class User extends Entity {
    public String name;
    public int age;
    public int accessLevel;

    public User(String name, int age, int accessLevel, Entity parent) {
        this.name = name;
        this.age = age;
        this.accessLevel = accessLevel;
        this.parent = parent;
    }

    @Override
    public String getEntityId() {
        return name;
    }
}
