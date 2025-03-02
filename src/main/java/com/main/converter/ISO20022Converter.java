/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.main.converter;

/**
 *
 * @author gmilandou
 */

import com.prowidesoftware.swift.model.field.*;
import com.prowidesoftware.swift.model.mt.mt1xx.MT103;
import com.prowidesoftware.swift.model.mx.MxPain00100108;
import com.prowidesoftware.swift.model.mx.dic.CustomerCreditTransferInitiationV08;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ISO20022Converter {

    public static void main(String[] args) {
        String iso20022Message = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.08\">" +
                "<CstmrCdtTrfInitn>" +
                "<GrpHdr>" +
                "<MsgId>123456789</MsgId>" +
                "<CreDtTm>2025-02-20T15:30:00</CreDtTm>" +
                "<NbOfTxs>1</NbOfTxs>" +
                "<CtrlSum>1000.00</CtrlSum>" +
                "</GrpHdr>" +
                "<PmtInf>" +
                "<PmtInfId>987654321</PmtInfId>" +
                "<PmtMtd>TRF</PmtMtd>" +
                "<NbOfTxs>1</NbOfTxs>" +
                "<CtrlSum>1000.00</CtrlSum>" +
                "<PmtTpInf>" +
                "<SvcLvl>" +
                "<Cd>SEPA</Cd>" +
                "</SvcLvl>" +
                "</PmtTpInf>" +
                "<ReqdExctnDt>2025-02-21</ReqdExctnDt>" +
                "<Dbtr>" +
                "<Nm>John Doe</Nm>" +
                "<PstlAdr>" +
                "<Ctry>US</Ctry>" +
                "</PstlAdr>" +
                "</Dbtr>" +
                "<DbtrAcct>" +
                "<Id>" +
                "<IBAN>US12345678901234567890123456</IBAN>" +
                "</Id>" +
                "</DbtrAcct>" +
                "<CdtTrfTxInf>" +
                "<PmtId>" +
                "<EndToEndId>abcdef123456</EndToEndId>" +
                "</PmtId>" +
                "<Amt>" +
                "<InstdAmt Ccy=\"USD\">1000.00</InstdAmt>" +
                "</Amt>" +
                "<Cdtr>" +
                "<Nm>Jane Smith</Nm>" +
                "</Cdtr>" +
                "<CdtrAcct>" +
                "<Id>" +
                "<IBAN>GB12345678901234567890123456</IBAN>" +
                "</Id>" +
                "</CdtrAcct>" +
                "</CdtTrfTxInf>" +
                "</PmtInf>" +
                "</CstmrCdtTrfInitn>" +
                "</Document>";

        String mt103Message = convertISO20022ToMT103(iso20022Message);
        System.out.println(mt103Message);
    }

    public static String convertISO20022ToMT103(String iso20022Message) {
        try {
            // Parse the ISO20022 message using MxPain00100108
            MxPain00100108 mxMessage = MxPain00100108.parse(iso20022Message);

            // Access the CustomerCreditTransferInitiationV08
            CustomerCreditTransferInitiationV08 paymentInitn = mxMessage.getCstmrCdtTrfInitn();

            // Extract relevant information
            String endToEndId = paymentInitn.getPmtInf().get(0).getCdtTrfTxInf().get(0).getPmtId().getEndToEndId();
            String debtorName = paymentInitn.getPmtInf().get(0).getDbtr().getNm();
            String creditorName = paymentInitn.getPmtInf().get(0).getCdtTrfTxInf().get(0).getCdtr().getNm();
            BigDecimal amount = paymentInitn.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().getInstdAmt().getValue();
            String currency = paymentInitn.getPmtInf().get(0).getCdtTrfTxInf().get(0).getAmt().getInstdAmt().getCcy();
            OffsetDateTime dateStr = paymentInitn.getGrpHdr().getCreDtTm(); // Extract creation date/time as String
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            /*Date date = sdfInput.parse(String.valueOf(dateStr));
            SimpleDateFormat sdfOutput = new SimpleDateFormat("yyMMdd"); // Format for Field32A (YYMMDD)
            String formattedDate = sdfOutput.format(date);*/

            // Format Field32A value as YYMMDDCCYAMOUNT
            //String field32AValue = formattedDate + currency + amount.toString().replace(".", ","); -- Removing the date for now
            String field32AValue =  currency + amount.toString().replace(".", ",");

            // Create MT103 message
            MT103 mt103 = new MT103();

            // Set fields
            mt103.addField(new Field20(endToEndId));
            mt103.addField(new Field32A(field32AValue)); // Use String constructor
            mt103.addField(new Field50K(debtorName));
            mt103.addField(new Field59(creditorName));
            mt103.addField(new Field70("Payment for services"));
            mt103.addField(new Field71A("OUR"));
            mt103.addField(new Field72("Additional info"));

            // Return MT103 as string
            return mt103.message();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}