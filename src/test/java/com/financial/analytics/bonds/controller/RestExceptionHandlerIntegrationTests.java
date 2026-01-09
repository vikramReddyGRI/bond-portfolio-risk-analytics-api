package com.financial.analytics.bonds.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.analytics.bonds.dto.BondDTO;

@SpringBootTest
@AutoConfigureMockMvc
public class RestExceptionHandlerIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void validationErrorsReturnStructuredPayload() throws Exception {
        BondDTO dto = new BondDTO();
        // missing required fields like isin
        dto.maturityDate = "";

        String json = mapper.writeValueAsString(dto);

        mvc.perform(post("/api/bonds/ytm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonTriggersBadRequest() throws Exception {
        String bad = "{not a json]";
        mvc.perform(post("/api/bonds/ytm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bad))
                .andExpect(status().isBadRequest());
    }
}
