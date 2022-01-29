package fantalive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageServiceTest {

    @DisplayName("Testa ....")
    @Test
    void testGet() {
    	System.out.println("TESTA");
        assertEquals("Hello JUnit 5", "Hello JUnit 5xxx");
    }

}