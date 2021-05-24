package trab;

public class NodeFile {
    public NodeFile() {
    }

    public NodeFile(String name, String path, String hash) {
        this.name = name;
        this.path = path;
        this.hash = hash;
    }

    private String name;
    private String path;
    private String hash;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
