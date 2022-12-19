package de.jpx3.intave.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class LockingLayer implements Resource {
  private final Lock javaLock = new ReentrantLock();

  private final File lockFile;
  private final Resource target;
  private FileLock lock;
  private FileChannel lockChannel;

  public LockingLayer(File targetFile, Resource target) {
    this.lockFile = new File(targetFile + ".lock");
    this.target = target;
  }

  @Override
  public boolean available() {
    return target.available() && !lockFile.exists() && !locked();
  }

  @Override
  public long lastModified() {
    return target.lastModified();
  }

  @Override
  public void write(InputStream inputStream) {
    try {
      lock();
      target.write(inputStream);
    } finally {
      unlock();
    }
  }

  @Override
  public InputStream read() {
    try {
      lock();
      return target.read();
    } finally {
      unlock();
    }
  }

  @Override
  public void delete() {
    try {
      lock();
      target.delete();
    } finally {
      unlock();
    }
  }

  private boolean locked() {
    return lockFile.exists() && lockChannel != null && lockChannel.isOpen();
  }

  private void lock() {
    try {
      javaLock.lock();
      if (lockFile.exists() && System.currentTimeMillis() - lockFile.lastModified() > 60 * 1000) {
        try {
          lockFile.delete();
        } catch (Exception ignored) {}
      }
      int attemptsRemaining = 30 * 1000 / 50;
      while (lockFile.exists() && attemptsRemaining-- > 0) {
//        System.out.println("Waiting for lockfile release... " + attemptsRemaining + "rem");
        try {
          Thread.sleep(ThreadLocalRandom.current().nextLong(25, 100));
        } catch (InterruptedException exception) {
          exception.printStackTrace();
        }
      }
      lockFile.delete();
      lockFile.createNewFile();
      lockFile.deleteOnExit();
      RandomAccessFile accessFile = new RandomAccessFile(lockFile, "rw");
      lockChannel = accessFile.getChannel();
      lock = lockChannel.lock();
      String hash = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE));
      lockChannel.write(ByteBuffer.wrap(hash.getBytes(StandardCharsets.UTF_8)));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void unlock() {
    try {
      javaLock.unlock();
      lock.close();
      lockChannel.close();
      lockFile.delete();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }
}