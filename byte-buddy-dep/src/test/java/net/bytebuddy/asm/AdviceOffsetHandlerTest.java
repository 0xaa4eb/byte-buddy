package net.bytebuddy.asm;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AdviceOffsetHandlerTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @Test
    public void testShortMethod() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(ShortMethod.class)
                .visit(Advice.to(EmptyAdvice.class).on(named(FOO)).readerFlags(ClassReader.SKIP_DEBUG))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getDeclaredMethod(FOO, String.class).invoke(type.getDeclaredConstructor().newInstance(), BAR), is((Object) BAR));
    }

    @Test
    public void testLongMethod() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(LongMethod.class)
                .visit(Advice.to(EmptyAdvice.class).on(named(FOO)).readerFlags(ClassReader.SKIP_DEBUG))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getDeclaredMethod(FOO, String.class, String.class, String.class)
                .invoke(type.getDeclaredConstructor().newInstance(), BAR, QUX, BAZ), is((Object) (BAR + QUX + BAZ)));
    }

    @Test
    public void testShortMethodAssignment() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(ShortMethod.class)
                .visit(Advice.to(UsingAdvice.class).on(named(FOO)).readerFlags(ClassReader.SKIP_DEBUG))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getDeclaredMethod(FOO, String.class).invoke(type.getDeclaredConstructor().newInstance(), BAR), is((Object) BAR));
    }

    @Test
    public void testLongMethodAssignment() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(LongMethod.class)
                .visit(Advice.to(UsingAdvice.class).on(named(FOO)).readerFlags(ClassReader.SKIP_DEBUG))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getDeclaredMethod(FOO, String.class, String.class, String.class)
                .invoke(type.getDeclaredConstructor().newInstance(), BAR, QUX, BAZ), is((Object) (BAR + QUX + BAZ)));
    }

    @SuppressWarnings("unused")
    public static class ShortMethod {

        public String foo(String var1) {
            String result = var1;
            var1 = null;
            return result;
        }
    }

    @SuppressWarnings("unused")
    public static class LongMethod {

        public String foo(String arg1, String arg2, String arg3) {
            String result = arg1 + arg2 + arg3;
            arg1 = null;
            return result;
        }
    }

    @SuppressWarnings("unused")
    public static class EmptyAdvice {

        @Advice.OnMethodEnter
        @Advice.OnMethodExit(backupArguments = true)
        private static void advice() {
            /* empty */
        }
    }

    @SuppressWarnings("unused")
    public static class UsingAdvice {

        @Advice.OnMethodEnter
        @Advice.OnMethodExit(backupArguments = true)
        private static void advice(@Advice.Argument(0) String arg) {
            if (!BAR.equals(arg)) {
                throw new AssertionError();
            }
        }
    }
}