package policygen;

public class CedarField {

    private String name;
    private CedarType type;

    public CedarField(String name, CedarType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public CedarType getType() {
        return type;
    }
}
