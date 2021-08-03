package com.alextherapeutics.diga;
import com.alextherapeutics.diga.DigaApiClient;
import com.alextherapeutics.diga.DigaCodeValidationException;
import com.alextherapeutics.diga.DigaApiException;
import com.alextherapeutics.diga.DigaXmlWriterException;
import com.alextherapeutics.diga.model.DigaApiClientSettings;
import com.alextherapeutics.diga.model.DigaInformation;
import com.alextherapeutics.diga.model.DigaInvoice;
import com.alextherapeutics.diga.model.DigaApiTestCode;
import java.math.BigDecimal;

import lombok.extern.slf4j.Slf4j;

public class Main {
    public static void main(String[] args) throws DigaCodeValidationException, DigaApiException, DigaXmlWriterException {  
        var mappingFile = Main.class.getClassLoader().getResourceAsStream("mappings.xml");
        var healthCompaniesKeyStore = Main.class.getClassLoader().getResourceAsStream("keystore.p12");
        var privateKeyStore = Main.class.getClassLoader().getResourceAsStream("keystore.p12"); // you need one inputstream for each

        var apiClientSettings = DigaApiClientSettings.builder() // settings required for the client to operate
                .healthInsuranceMappingFile(mappingFile)
                .privateKeyStoreFile(privateKeyStore)
                .healthInsurancePublicKeyStoreFile(healthCompaniesKeyStore)
                .privateKeyStorePassword("PLACEHOLDER")
                .privateKeyAlias("private") // you must create this when creating the keystore
                .healthInsurancePublicKeyStorePassword("PLACEHOLDER")
                .build();
                
        var digaInformation = DigaInformation.builder() // information about your DiGA and your company required to easily
                                                        // create invoices and send requests to the API
                .digaName("digaName")
                .digaId("12345") // if you arent accepted as DiGA yet, just put 12345
                .manufacturingCompanyName("manufacturingCompanyName")
                .manufacturingCompanyIk("PLACEHOLDER")
                .netPricePerPrescription(new BigDecimal(100)) // net price per diga code validated
                .applicableVATpercent(new BigDecimal(19)) // how much VAT should be applied to the invoices
                .manufacturingCompanyVATRegistration("DE 123 456 789")
                .contactPersonForBilling(
                    DigaInformation.ContactPersonForBilling.builder()
                        .fullName("fullName")
                        .phoneNumber("phoneNumber")
                        .emailAddress("svensvensson@awesomedigacompany.com")
                        .build()
                )
                .companyTradeAddress(
                    DigaInformation.CompanyTradeAddress.builder()
                        .adressLine("adressLine")
                        .postalCode("postalCode")
                        .city("city")
                        .countryCode("DE")
                        .build()
                )
                .build();

        var apiClient = new DigaApiClient(apiClientSettings, digaInformation);

        var codeValidationResponse = apiClient.sendTestCodeValidationRequest(DigaApiTestCode.VALID, "CH");

        // System.out.println(new String(codeValidationResponse.getRawXmlResponseBody()));
        
        var testInvoiceResponse = apiClient.sendTestInvoiceRequest(
            DigaInvoice.builder().invoiceId("1").validatedDigaCode(DigaApiTestCode.VALID.getCode()).digavEid("12345000").build(),
            "AH"
            );
        System.out.println(new String(testInvoiceResponse.getRawXmlResponseBody()));
        
    }
}