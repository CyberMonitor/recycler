package ovh.corail.recycler.network;

public interface IProxy {
    void updateConfigIfDirty();
    void markConfigDirty();
}
