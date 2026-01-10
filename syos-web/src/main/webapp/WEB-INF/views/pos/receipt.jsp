<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Receipt - ${bill.serialNumberString}</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: 'Courier New', Courier, monospace;
                        font-size: 12px;
                        background: #fff;
                        padding: 20px;
                        max-width: 320px;
                        margin: 0 auto;
                    }

                    .receipt {
                        border: 1px dashed #000;
                        padding: 15px;
                    }

                    .header {
                        text-align: center;
                        border-bottom: 1px dashed #000;
                        padding-bottom: 10px;
                        margin-bottom: 10px;
                    }

                    .store-name {
                        font-size: 18px;
                        font-weight: bold;
                    }

                    .store-type {
                        font-size: 10px;
                        color: #666;
                    }

                    .bill-info {
                        margin-bottom: 15px;
                    }

                    .bill-info p {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 3px;
                    }

                    .items-table {
                        width: 100%;
                        margin-bottom: 15px;
                        border-collapse: collapse;
                    }

                    .items-table th {
                        text-align: left;
                        border-bottom: 1px solid #000;
                        padding-bottom: 5px;
                    }

                    .items-table td {
                        padding: 5px 0;
                        vertical-align: top;
                    }

                    .items-table .qty {
                        text-align: center;
                        width: 30px;
                    }

                    .items-table .price {
                        text-align: right;
                        width: 70px;
                    }

                    .totals {
                        border-top: 1px dashed #000;
                        padding-top: 10px;
                    }

                    .totals p {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 5px;
                    }

                    .totals .grand-total {
                        font-size: 16px;
                        font-weight: bold;
                        border-top: 1px solid #000;
                        padding-top: 5px;
                        margin-top: 5px;
                    }

                    .payment-info {
                        border-top: 1px dashed #000;
                        margin-top: 10px;
                        padding-top: 10px;
                    }

                    .footer {
                        text-align: center;
                        margin-top: 15px;
                        padding-top: 10px;
                        border-top: 1px dashed #000;
                        font-size: 10px;
                    }

                    .print-btn {
                        display: block;
                        width: 100%;
                        padding: 10px;
                        margin-top: 20px;
                        background: #4CAF50;
                        color: white;
                        border: none;
                        cursor: pointer;
                        font-size: 14px;
                    }

                    .print-btn:hover {
                        background: #45a049;
                    }

                    .back-btn {
                        display: block;
                        width: 100%;
                        padding: 10px;
                        margin-top: 10px;
                        background: #2196F3;
                        color: white;
                        border: none;
                        cursor: pointer;
                        font-size: 14px;
                        text-align: center;
                        text-decoration: none;
                    }

                    @media print {
                        .no-print {
                            display: none;
                        }

                        body {
                            padding: 0;
                        }
                    }
                </style>
            </head>

            <body>
                <div class="receipt">
                    <!-- Header -->
                    <div class="header">
                        <div class="store-name">SYOS STORE</div>
                        <div class="store-type">
                            <c:choose>
                                <c:when test="${bill.storeType.name() eq 'PHYSICAL'}">Physical Store</c:when>
                                <c:otherwise>Online Store</c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Bill Information -->
                    <div class="bill-info">
                        <p>
                            <span>Bill #:</span>
                            <span><strong>${bill.serialNumberString}</strong></span>
                        </p>
                        <p>
                            <span>Date:</span>
                            <span>${bill.billDateFormatted}</span>
                        </p>
                        <p>
                            <span>Time:</span>
                            <span>${bill.billTime}</span>
                        </p>
                        <c:if test="${not empty bill.cashierId}">
                            <p>
                                <span>Cashier:</span>
                                <span>${bill.cashierId}</span>
                            </p>
                        </c:if>
                        <c:if test="${not empty bill.customerName}">
                            <p>
                                <span>Customer:</span>
                                <span>${bill.customerName}</span>
                            </p>
                        </c:if>
                    </div>

                    <!-- Items Table -->
                    <table class="items-table">
                        <thead>
                            <tr>
                                <th>Item</th>
                                <th class="qty">Qty</th>
                                <th class="price">Price</th>
                                <th class="price">Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${items}">
                                <tr>
                                    <td>${item.productName}</td>
                                    <td class="qty">${item.quantity}</td>
                                    <td class="price">
                                        <c:if test="${item.unitPrice != null}">
                                            <fmt:formatNumber value="${item.unitPrice.amount}" pattern="#,##0.00" />
                                        </c:if>
                                    </td>
                                    <td class="price">
                                        <c:if test="${item.lineTotal != null}">
                                            <fmt:formatNumber value="${item.lineTotal.amount}" pattern="#,##0.00" />
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <!-- Totals -->
                    <div class="totals">
                        <p>
                            <span>Subtotal:</span>
                            <span>Rs.
                                <c:choose>
                                    <c:when test="${bill.subtotal != null}">
                                        <fmt:formatNumber value="${bill.subtotal.amount}" pattern="#,##0.00" />
                                    </c:when>
                                    <c:otherwise>0.00</c:otherwise>
                                </c:choose>
                            </span>
                        </p>
                        <c:if test="${bill.discountAmount != null and bill.discountAmount.amount > 0}">
                            <p>
                                <span>Discount:</span>
                                <span>- Rs.
                                    <fmt:formatNumber value="${bill.discountAmount.amount}" pattern="#,##0.00" />
                                </span>
                            </p>
                        </c:if>
                        <c:if test="${bill.taxAmount != null and bill.taxAmount.amount > 0}">
                            <p>
                                <span>Tax:</span>
                                <span>Rs.
                                    <fmt:formatNumber value="${bill.taxAmount.amount}" pattern="#,##0.00" />
                                </span>
                            </p>
                        </c:if>
                        <p class="grand-total">
                            <span>TOTAL:</span>
                            <span>Rs.
                                <c:choose>
                                    <c:when test="${bill.totalAmount != null}">
                                        <fmt:formatNumber value="${bill.totalAmount.amount}" pattern="#,##0.00" />
                                    </c:when>
                                    <c:otherwise>0.00</c:otherwise>
                                </c:choose>
                            </span>
                        </p>
                    </div>

                    <!-- Payment Information -->
                    <c:if test="${bill.transactionType.name() eq 'CASH'}">
                        <div class="payment-info">
                            <p>
                                <span>Cash Tendered:</span>
                                <span>Rs.
                                    <c:if test="${bill.tenderedAmount != null}">
                                        <fmt:formatNumber value="${bill.tenderedAmount.amount}" pattern="#,##0.00" />
                                    </c:if>
                                </span>
                            </p>
                            <p>
                                <span><strong>Change:</strong></span>
                                <span><strong>Rs.
                                        <c:if test="${bill.changeAmount != null}">
                                            <fmt:formatNumber value="${bill.changeAmount.amount}" pattern="#,##0.00" />
                                        </c:if>
                                    </strong></span>
                            </p>
                        </div>
                    </c:if>
                    <c:if test="${bill.transactionType.name() eq 'CREDIT'}">
                        <div class="payment-info">
                            <p>
                                <span>Payment:</span>
                                <span>Card/Credit</span>
                            </p>
                        </div>
                    </c:if>

                    <!-- Footer -->
                    <div class="footer">
                        <p>Thank you for shopping with us!</p>
                        <p>Please keep this receipt for your records.</p>
                    </div>
                </div>

                <!-- Action Buttons (not printed) -->
                <div class="no-print">
                    <button class="print-btn" onclick="window.print()">Print Receipt</button>
                    <a href="${pageContext.request.contextPath}/pos/new" class="back-btn">New Sale</a>
                    <a href="${pageContext.request.contextPath}/pos" class="back-btn" style="background: #666;">Back to
                        POS</a>
                </div>
            </body>

            </html>