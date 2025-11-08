<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Transacao"%>
<%@ page import="java.util.List"%>
<%@ page import="java.math.BigDecimal"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> <%-- Adicione esta linha --%>

<%
    // Segurança
    if (!(session.getAttribute("usuarioLogado") instanceof Cliente)) {
        response.sendRedirect("login.jsp");
        return;
    }
    // As datas de início e fim serão passadas como atributos da requisição pelo ExtratoServlet
    String dataInicioParam = (String) request.getAttribute("dataInicio");
    String dataFimParam = (String) request.getAttribute("dataFim");

    // As variáveis de formatação Java não são mais estritamente necessárias se usando JSTL fmt tags
    // NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    // DateTimeFormatter formatoDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Extrato - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>
    <body>
        <div class="d-flex">

            <%-- Menu lateral --%>
            <jsp:include page="/includes/sidebar.jsp" />

            <main class="main-content d-flex align-items-center">
                <div class="container-lg">

                    <div class="row justify-content-center">
                        <div class="col-lg-10 col-xl-9">
                            <div class="card card-balance p-4 mb-4">

                                <div class="text-center mb-4">
                                    <i class="bi bi-receipt-cutoff text-warning" style="font-size: 2rem;"></i>
                                    <h3 class="mt-2">Extrato de Transações</h3>
                                </div>

                                <form action="ExtratoServlet" method="get">
                                    <div class="row align-items-end g-3 justify-content-center">
                                        <div class="col-md-5">
                                            <label for="dataInicio" class="form-label">Data de Início</label>
                                            <input type="date" class="form-control" id="dataInicio" name="dataInicio" value="<%= dataInicioParam != null ? dataInicioParam : "" %>">
                                        </div>
                                        <div class="col-md-5">
                                            <label for="dataFim" class="form-label">Data de Fim</label>
                                            <input type="date" class="form-control" id="dataFim" name="dataFim" value="<%= dataFimParam != null ? dataFimParam : "" %>">
                                        </div>
                                        <div class="col-md-2 d-grid">
                                            <button type="submit" class="btn btn-custom">Buscar</button>
                                        </div>
                                    </div>
                                </form>
                                <%-- Mensagem de erro do Servlet --%>
                                <% String erro = (String) request.getAttribute("erro");
                                if (erro != null) {%>
                                <div class="alert alert-danger mt-3" role="alert">
                                    <%= erro%>
                                </div>
                                <% }%>
                            </div>

                            <div class="card" style="background-color: #1e1e1e;">
                                <div class="card-header">
                                    Transações
                                </div>
                                <div class="card-body p-0">
                                    <div class="table-responsive">
                                        <table class="table table-dark table-hover mb-0 align-middle">
                                            <thead>
                                                <tr>
                                                    <th scope="col">Data</th>
                                                    <th scope="col">Tipo</th>
                                                    <th scope="col">Valor</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:choose>
                                                    <c:when test="${not empty listaTransacoes}">
                                                        <c:forEach var="transacao" items="${listaTransacoes}">
                                                            <tr>
                                                                <td><fmt:formatDate value="${transacao.dataTransacaoAsDate}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                                                                <td>
                                                                    <c:choose>
                                                                        <c:when test="${transacao.tipo == 'DEPOSITO' || transacao.tipo == 'TRANSF_RECEBIDA'}">
                                                                            <span class="badge bg-success text-white"><c:out value="${transacao.tipo}" /></span>
                                                                        </c:when>
                                                                        <c:when test="${transacao.tipo == 'SAQUE' || transacao.tipo == 'TRANSF_ENVIADA'}">
                                                                            <span class="badge bg-danger text-white"><c:out value="${transacao.tipo}" /></span>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <span class="badge bg-secondary text-white"><c:out value="${transacao.tipo}" /></span>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                                <td class="
                                                                    <c:choose>
                                                                        <c:when test="${transacao.tipo == 'DEPOSITO' || transacao.tipo == 'TRANSF_RECEBIDA'}">
                                                                            text-success
                                                                        </c:when>
                                                                        <c:when test="${transacao.tipo == 'SAQUE' || transacao.tipo == 'TRANSF_ENVIADA'}">
                                                                            text-danger
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            text-white
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                    ">
                                                                    <fmt:formatNumber value="${transacao.valor}" type="currency" currencySymbol="R$ "/>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <tr>
                                                            <td colspan="3" class="text-center py-4">Nenhuma transação encontrada para o período.</td>
                                                        </tr>
                                                    </c:otherwise>
                                                </c:choose>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>