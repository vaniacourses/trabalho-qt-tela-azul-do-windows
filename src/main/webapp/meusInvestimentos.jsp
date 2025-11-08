<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Investimento"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    // Segurança
    if (!(session.getAttribute("usuarioLogado") instanceof Cliente)) {
        response.sendRedirect("login.jsp");
        return;
    }
    // As datas de início e fim serão passadas como atributos da requisição pelo MeusInvestimentosServlet
    String dataInicioParam = (String) request.getAttribute("dataInicio");
    String dataFimParam = (String) request.getAttribute("dataFim");
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Meus Investimentos - Banco ATM</title>
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
                                    <i class="bi bi-piggy-bank text-warning" style="font-size: 2rem;"></i>
                                    <h3 class="mt-2">Meus Investimentos</h3>
                                </div>

                                <form action="MeusInvestimentosServlet" method="get">
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
                                    Investimentos
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
                                                    <c:when test="${not empty listaInvestimentos}">
                                                        <c:forEach var="investimento" items="${listaInvestimentos}">
                                                            <tr>
                                                                <td><fmt:formatDate value="${investimento.dataTransacaoAsDate}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                                                                <td>
                                                                    <span class="badge bg-success text-white"><c:out value="${investimento.tipoInvestimento}" /></span>
                                                                </td>
                                                                <td class="text-success">
                                                                    <fmt:formatNumber value="${investimento.valorAplicado}" type="currency" currencySymbol="R$ "/>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <tr>
                                                            <td colspan="3" class="text-center py-4">Nenhum investimento encontrado para o período.</td>
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