<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="br.uff.ic.grupo6.banco.model.Cliente"%>
<%
    // Segurança
    if (!(session.getAttribute("usuarioLogado") instanceof Cliente)) {
        response.sendRedirect("login.jsp");
        return;
    }
    String erro = request.getParameter("erro");
    String tipoInvestimento = request.getParameter("tipoInvestimento");
    if (tipoInvestimento == null || tipoInvestimento.trim().isEmpty()) {
        response.sendRedirect("investimento.jsp?erro=Selecione+um+tipo+de+investimento.");
        return;
    }
%>
<!DOCTYPE html>
<html lang="pt-BR" data-bs-theme="dark">
    <head>
        <meta charset="UTF-8">
        <title>Realizar Investimento - Banco ATM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="style.css" rel="stylesheet">
    </head>
    <body>
        <div class="d-flex">
            <jsp:include page="/includes/sidebar.jsp" />

            <main class="main-content d-flex align-items-center">
                <div class="container">
                    <div class="row justify-content-center">
                        <div class="col-md-8 col-lg-7">
                            <div class="login-box p-4 p-md-5">
                                <div class="text-center mb-4">
                                    <i class="bi bi-graph-up-arrow text-warning fs-2"></i>
                                    <h3 class="mt-1">Realizar Investimento</h3>
                                </div>

                                <% if (erro != null && !erro.isEmpty()) {%>
                                <div class="alert alert-danger" role="alert">
                                    <%= erro %>
                                </div>
                                <% } %>

                                <form action="InvestimentoServlet" method="post">
                                    <div class="form-floating mb-3">
                                        <select class="form-control" id="tipoInvestimento" name="tipoInvestimento" required disabled>
                                            <option value="SELIC" <%= tipoInvestimento.equals("SELIC") ? "selected" : "" %>>Tesouro Selic</option>
                                            <option value="CDB" <%= tipoInvestimento.equals("CDB") ? "selected" : "" %>>CDB 110% do CDI</option>
                                            <option value="FII" <%= tipoInvestimento.equals("FII") ? "selected" : "" %>>Fundos Imobiliários</option>
                                            <option value="POUPANCA" <%= tipoInvestimento.equals("POUPANCA") ? "selected" : "" %>>Poupança</option>
                                        </select>
                                        <label for="tipoInvestimento">Tipo de Investimento</label>
                                        <input type="hidden" name="tipoInvestimento" value="<%= tipoInvestimento %>">
                                    </div>
                                    <div class="form-floating mb-3">
                                        <input type="number" step="0.01" min="0.01" class="form-control" id="valor" name="valor" placeholder="Valor do investimento" required>
                                        <label for="valor">Valor do Investimento (R$)</label>
                                    </div>
                                    <div class="d-grid gap-2 mt-4">
                                        <button type="submit" class="btn btn-custom btn-lg">Confirmar Investimento</button>
                                        <a href="investimento.jsp" class="btn btn-outline-secondary btn-lg">Cancelar</a>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>