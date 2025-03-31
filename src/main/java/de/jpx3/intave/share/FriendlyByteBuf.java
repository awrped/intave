package de.jpx3.intave.share;

import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import de.jpx3.intave.klass.locate.MethodSearchBySignature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public final class FriendlyByteBuf {
  public static ByteBuf from256Unpooled() {
    return wrapping(Unpooled.buffer(256, 2048));
  }

  public static ByteBuf wrapping(ByteBuf byteBuf) {
    return (ByteBuf) MinecraftMethods.getFriendlyBufBufConstructor().apply(byteBuf);
  }

  public static String readUtf(ByteBuf friendly, int maxLength) {
    try {
      if (readUtfMethod == null) {
        return "something went wrong";
      }
      return (String) readUtfMethod.invoke(friendly, maxLength);
    } catch (Throwable e) {
      e.printStackTrace();
      return "something went wrong";
    }
  }

  public static void setup() {
  }

  private static final MethodHandle readUtfMethod;

  static {
    MethodHandle method;
    Optional<Class<?>> rfbbclassoptional = MinecraftReflection.getRegistryFriendlyByteBufClass();
    if (!rfbbclassoptional.isPresent()) {
      method = null;
    } else {
      try {
        method = MethodHandles.lookup().unreflect(rfbbclassoptional.get().getDeclaredMethod("readUtf", int.class));
      } catch (NoSuchMethodException e) {
        method = MethodSearchBySignature.ofClass(MinecraftReflection.getPacketDataSerializerClass())
          .withReturnType(String.class)
          .withParameters(new Class[]{int.class})
          .search().findFirst().get();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    readUtfMethod = method;
  }
}
