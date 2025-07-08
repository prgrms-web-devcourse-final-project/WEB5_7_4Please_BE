package com.deal4u.fourplease;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FailTest {

	@Test
	void failTest(){
		FailObject failObject = new FailObject();
		assertThat(failObject.getValue()).isEqualTo(1);
	}
}
