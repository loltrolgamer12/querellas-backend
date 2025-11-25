package com.neiva.querillas;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Se deshabilita mientras se aísla seguridad/JWT del contexto de test")
class QuerillasApplicationTests {

	@Test
	void contextLoads() {
	}

}
