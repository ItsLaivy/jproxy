package codes.laivy.proxy.http.impl;

import codes.laivy.proxy.http.connection.HttpProxyClient;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class HttpProxyClients implements Collection<HttpProxyClient> {

    private final @NotNull Set<HttpProxyClient> clients = ConcurrentHashMap.newKeySet();

    @Override
    public int size() {
        return clients.size();
    }

    @Override
    public boolean isEmpty() {
        return clients.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return clients.contains(o);
    }

    @Override
    public @NotNull Iterator<HttpProxyClient> iterator() {
        return clients.iterator();
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return clients.toArray();
    }
    @Override
    public <T> @NotNull T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return clients.toArray(a);
    }

    @Override
    public boolean add(@NotNull HttpProxyClient httpProxyClient) {
        return clients.add(httpProxyClient);
    }

    @Override
    public boolean remove(Object o) {
        return clients.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return clients.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends HttpProxyClient> c) {
        return clients.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return clients.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return clients.retainAll(c);
    }

    @Override
    public void clear() {
        clients.clear();
    }
}
