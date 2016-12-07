package de.mcmainiac.webconsole.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class ChannelGroup extends ArrayList<Channel> {
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public boolean add(Channel channel) {
        boolean success;

        try {
            lock.writeLock().lock();

            success = super.add(channel);
        } finally {
            lock.writeLock().unlock();
        }

        return success;
    }

    @Override
    public Channel get(int index) {
        Channel channel;

        try {
            lock.readLock().lock();

            channel = super.get(index);
        } finally {
            lock.readLock().unlock();
        }

        return channel;
    }

    @Override
    public int indexOf(Object o) {
        int index;

        try {
            lock.readLock().unlock();

            index = super.indexOf(o);
        } finally {
            lock.readLock().unlock();
        }

        return index;
    }

    @Override
    public boolean remove(Object o) {
        boolean success;

        try {
            lock.writeLock().lock();

            success = super.remove(o);
        } finally {
            lock.writeLock().unlock();
        }

        update();

        return success;
    }

    @Override
    public int size() {
        int size;

        update();

        try {
            lock.readLock().lock();

            size = super.size();
        } finally {
            lock.readLock().unlock();
        }

        return size;
    }

    @Override
    public boolean removeIf(Predicate<? super Channel> filter) {
        boolean success;

        try {
            lock.writeLock().lock();
            lock.readLock().lock();

            success = super.removeIf(filter);
        } finally {
            lock.writeLock().unlock();
            lock.readLock().unlock();
        }

        return success;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        try {
            lock.writeLock().lock();

            super.removeRange(fromIndex, toIndex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void closeAll() throws IOException {
        try {
            lock.readLock().lock();
            lock.writeLock().lock();

            // close all channels
            for (Channel channel : this)
                channel.close();
        } finally {
            lock.readLock().unlock();
            lock.writeLock().unlock();
        }

        update();
    }

    void update() {
        // remove all channels that aren't connected
        this.removeIf(c -> !c.isConnected());
    }
}
