//package io.arex.attacher;
//
//import com.sun.tools.attach.AgentLoadException;
//import com.sun.tools.attach.AttachNotSupportedException;
//import com.sun.tools.attach.VirtualMachine;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.io.IOException;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.params.provider.Arguments.arguments;
//import static org.mockito.ArgumentMatchers.anyString;
//
//@ExtendWith(MockitoExtension.class)
//class AgentAttacherTest {
//
//    static VirtualMachine virtualMachine;
//
//    @BeforeAll
//    static void setUp() throws IOException, AttachNotSupportedException {
//        virtualMachine = Mockito.mock(VirtualMachine.class);
//        Mockito.mockStatic(VirtualMachine.class);
//        Mockito.when(VirtualMachine.attach(anyString())).thenReturn(virtualMachine);
//    }
//
//    @AfterAll
//    static void tearDown() {
//        virtualMachine = null;
//        Mockito.clearAllCaches();
//    }
//
//    @ParameterizedTest
//    @MethodSource("mainCase")
//    void main(String[] args, Runnable mocker) {
//        mocker.run();
//        // Expected behavior: e.printStackTrace();
//        // will be returned as error stream to the caller
//        assertDoesNotThrow(() -> AgentAttacher.main(args));
//    }
//
//    static Stream<Arguments> mainCase() {
//        Runnable emptyMocker = () -> {};
//        Runnable mocker1 = () -> {
//            try {
//                Mockito.doThrow(new IOException("Non-numeric value found")).when(virtualMachine).loadAgent(anyString(), anyString());
//            } catch (Exception e) {}
//        };
//        Runnable mocker2 = () -> {
//            try {
//                Mockito.doThrow(new IOException("mock")).when(virtualMachine).loadAgent(anyString(), anyString());
//            } catch (Exception e) {}
//        };
//        Runnable mocker3 = () -> {
//            try {
//                Mockito.doThrow(new AgentLoadException("0")).when(virtualMachine).loadAgent(anyString(), anyString());
//            } catch (Exception e) {}
//        };
//        Runnable mocker4 = () -> {
//            try {
//                Mockito.doThrow(new AgentLoadException("mock")).when(virtualMachine).loadAgent(anyString(), anyString());
//            } catch (Exception e) {}
//        };
//        return Stream.of(
//                arguments(null, emptyMocker),
//                arguments(new String[]{"mock"}, emptyMocker),
//                arguments(new String[]{"mock", "mock", "mock"}, mocker1),
//                arguments(new String[]{"mock", "mock", "mock"}, mocker2),
//                arguments(new String[]{"mock", "mock", "mock"}, mocker3),
//                arguments(new String[]{"mock", "mock", "mock"}, mocker4)
//        );
//    }
//}