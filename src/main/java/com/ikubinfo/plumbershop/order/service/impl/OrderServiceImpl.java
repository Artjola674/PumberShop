package com.ikubinfo.plumbershop.order.service.impl;

import com.ikubinfo.plumbershop.common.util.UtilClass;
import com.ikubinfo.plumbershop.order.dto.Bill;
import com.ikubinfo.plumbershop.order.dto.OrderDto;
import com.ikubinfo.plumbershop.order.mapper.OrderMapper;
import com.ikubinfo.plumbershop.order.model.OrderDocument;
import com.ikubinfo.plumbershop.order.repo.OrderRepository;
import com.ikubinfo.plumbershop.order.service.OrderService;
import com.ikubinfo.plumbershop.product.model.ProductDocument;
import com.ikubinfo.plumbershop.product.repo.ProductRepository;
import com.ikubinfo.plumbershop.security.CustomUserDetails;
import com.ikubinfo.plumbershop.user.dto.Address;
import com.ikubinfo.plumbershop.user.enums.Role;
import com.ikubinfo.plumbershop.user.model.UserDocument;
import com.ikubinfo.plumbershop.user.service.UserService;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.stream.Stream;

import static com.ikubinfo.plumbershop.common.constants.Constants.*;
import static com.ikubinfo.plumbershop.order.constants.OrderConstants.*;
import static com.itextpdf.text.Rectangle.NO_BORDER;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final ProductRepository productRepository;
    @Value("${documents.folder}")
    private String documentPath;

    public OrderServiceImpl(OrderRepository orderRepository, UserService userService, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productRepository = productRepository;
        this.orderMapper = Mappers.getMapper((OrderMapper.class));
    }

    @Transactional
    @Override
    public OrderDto save(OrderDto orderDto, CustomUserDetails loggedUser) throws DocumentException, IOException, URISyntaxException {
        OrderDocument orderDocument = orderMapper.toOrderDocument(orderDto);

        double totalPrice = calculateTotalOrderPrice(orderDocument);

        double buyingPriceSum = calculateTotalProductsBuyingPrice(orderDocument);

        if (UtilClass.userHasGivenRole(loggedUser, Role.PLUMBER)
                && orderDocument.getCustomer() != null) {
            totalPrice = totalPrice *
                    (1-orderDocument.getCustomer().getDiscountPercentage()/100);
        }

        double earnings = totalPrice - buyingPriceSum;

        orderDocument.setCustomer(userService.getUserByEmail(loggedUser.getEmail()));
        orderDocument.setTotalPrice(totalPrice);
        orderDocument.setEarnings(earnings);
        orderDocument.setDate(LocalDate.now());

        subtractOrderAmountFromTotal(orderDocument);

        generateTheBill(orderDocument);
        //sendEmailToUser

        OrderDocument savedOrder = orderRepository.save(orderDocument);

        return orderMapper.toOrderDto(savedOrder);
    }

    private double calculateTotalProductsBuyingPrice(OrderDocument orderDocument) {
        return orderDocument.getOrderItems()
                .stream()
                .mapToDouble(orderItemDocument ->
                        orderItemDocument.getAmount()
                                * orderItemDocument.getProduct().getBuyingPrice())
                .sum();
    }

    private double calculateTotalOrderPrice(OrderDocument orderDocument) {
        return orderDocument.getOrderItems()
                .stream()
                .mapToDouble(orderItemDocument ->
                        orderItemDocument.getAmount()
                                * orderItemDocument.getProduct().getSellingPrice())
                .sum();
    }

    private void subtractOrderAmountFromTotal(OrderDocument orderDocument){
        orderDocument.getOrderItems()
                .forEach(orderItemDocument -> {
                    ProductDocument productDocument = orderItemDocument.getProduct();
                    productDocument.setCount(productDocument.getCount()-orderItemDocument.getAmount());
                    productRepository.save(productDocument);
                });
    }


    private void generateTheBill(OrderDocument order) throws IOException, DocumentException, URISyntaxException {

        String filename = createRandomString() + EXTENSION;

        createBillDoc(order, filename);

        log.info("Writing file {} to directory {} ", filename, documentPath);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(documentPath + filename));

        document.open();

        Paragraph header = createParagraph();

        PdfPTable itemList = createItemListTable(order);
        PdfPTable total = createTotalTable(order);
        PdfPTable companyInformation = createCompanyInformation();
        PdfPTable customerInformation = createCustomerInformation(order.getCustomer());

        document.add(header);
        document.add(companyInformation);
        document.add(customerInformation);
        document.add(itemList);
        document.add(total);
        document.close();

    }



    private Paragraph createParagraph() {
        Font paragraphFont = FontFactory.getFont(FontFactory.TIMES_BOLD);
        paragraphFont.setColor(BaseColor.BLACK);
        paragraphFont.setSize(16);


        Paragraph paragraph = new Paragraph();
        paragraph.add(new Phrase(BILL ,paragraphFont));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(10);
        paragraph.setSpacingAfter(20);
        return paragraph;

    }

    private PdfPTable createCompanyInformation() {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(95);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        PdfPCell header = createCell();
        header.setPhrase(new Phrase(COMPANY_INFORMATION));
        header.setBackgroundColor(BaseColor.CYAN);
        header.setColspan(2);

        PdfPCell cell = createCell();
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(NO_BORDER);

        table.addCell(header);
        addCell(table,cell,NID);
        addCell(table,cell,COMPANY_NID);
        addCell(table, cell, NAME);
        addCell(table, cell, COMPANY_NAME);
        addCell(table, cell, EMAIL);
        addCell(table, cell, COMPANY_EMAIL);
        addCell(table, cell, CITY);
        addCell(table, cell, COMPANY_ADDRESS_CITY);
        addCell(table, cell, POSTAL_CODE);
        addCell(table, cell, COMPANY_ADDRESS_POSTAL_CODE);
        addCell(table, cell, STREET);
        addCell(table, cell, COMPANY_ADDRESS_STREET);

        return table;

    }


    private PdfPTable createCustomerInformation(UserDocument customer) {
        Address address = customer.getAddress();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(95);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        PdfPCell header = createCell();
        header.setPhrase(new Phrase(CUSTOMER_INFORMATION));
        header.setBackgroundColor(BaseColor.CYAN);
        header.setColspan(2);

        PdfPCell cell = createCell();
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(NO_BORDER);

        table.addCell(header);
        addCell(table, cell, NAME);
        addCell(table, cell, customer.getFirstName().concat(" ").concat(customer.getLastName()));
        addCell(table, cell, EMAIL);
        addCell(table, cell, customer.getEmail());
        addCell(table, cell, CITY);
        addCell(table, cell, address.getCity());
        addCell(table, cell, POSTAL_CODE);
        addCell(table, cell, address.getPostalCode().toString());
        addCell(table, cell, STREET);
        addCell(table, cell, address.getStreet());

        return table;

    }


    private PdfPTable createItemListTable(OrderDocument order) {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(95);
        addItemListTableHeader(table);
        addItemListRows(table, order);
        return table;
    }

    private void addItemListTableHeader(PdfPTable table) {
        Stream.of(PRODUCT_NAME, PRODUCT_CODE, AMOUNT, PRICE, DISCOUNT, TOTAL_PRICE)
                .forEach(columnTitle -> {
                    PdfPCell header = createCell();
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setBackgroundColor(BaseColor.CYAN);
                    addCell(table,header,columnTitle);
                });
    }

    private void addItemListRows(PdfPTable table, OrderDocument order) {
        order.getOrderItems().forEach(orderItemDocument -> {
            double totalPrice = orderItemDocument.getAmount()
                    * orderItemDocument.getProduct().getSellingPrice()
                    * (1-order.getCustomer().getDiscountPercentage()/100);

            PdfPCell cell = createCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);

            addCell(table,cell,orderItemDocument.getProduct().getName());
            addCell(table,cell,orderItemDocument.getProduct().getCode());
            addCell(table,cell,orderItemDocument.getAmount().toString());
            addCell(table,cell,String.valueOf(orderItemDocument.getProduct().getSellingPrice()).concat(DOLLAR));
            addCell(table,cell,String.valueOf(order.getCustomer().getDiscountPercentage()).concat(PERCENTAGE));
            addCell(table,cell,String.valueOf(totalPrice).concat(DOLLAR));
        });
    }

    private PdfPTable createTotalTable(OrderDocument order) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(95);
        PdfPCell cell = createCell();
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingRight(25);

        Font font = new Font(Font.FontFamily.HELVETICA,12,Font.BOLD);
        addCell(table, cell, TOTAL, font);
        addCell(table, cell, String.valueOf(order.getTotalPrice()).concat(DOLLAR), font);

        return table;
    }

    private void addCell(PdfPTable pdfTable,PdfPCell cell,String text){
        cell.setPhrase(new Phrase(text));
        pdfTable.addCell(cell);
    }

    private void addCell(PdfPTable pdfTable,PdfPCell cell,String text, Font font){
        cell.setPhrase(new Phrase(text,font));
        pdfTable.addCell(cell);
    }

    private PdfPCell createCell(){
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        return cell;
    }

    private void createBillDoc(OrderDocument order, String filename) {
        Bill bill = Bill.builder()
                .fileLocation(documentPath)
                .fileName(filename)
                .build();
        order.setBill(bill);
    }

    private String createRandomString() {
        return RandomStringUtils.randomAlphanumeric(12);
    }





}
